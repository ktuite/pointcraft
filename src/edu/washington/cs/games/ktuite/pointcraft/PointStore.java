package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Matrix3f;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

import toxi.geom.PointOctree;
import toxi.geom.Vec3D;

public class PointStore {

	// stuff about the point cloud
	public static int num_points, num_cameras;
	public static FloatBuffer point_positions;
	public static FloatBuffer point_normals;
	public static ByteBuffer point_colors;
	private static ByteBuffer backup_colors;
	// VBO state tracking
	private static boolean point_vbo_dirty = false;

	public static ByteBuffer point_properties;
	private static PointOctree tree;
	public static float min_corner[] = { Float.MAX_VALUE, Float.MAX_VALUE,
			Float.MAX_VALUE };
	public static float max_corner[] = { -1 * Float.MAX_VALUE,
			-1 * Float.MAX_VALUE, -1 * Float.MAX_VALUE };
	private static Map<Vec3D, Integer> index_map;
	public static FloatBuffer camera_frusta_lines;
	public static LinkedList<Camera> cameras = new LinkedList<Camera>();
	public static HashMap<String, Camera> cameraMap = new HashMap<String, Camera>();
	public static boolean[][] camera_matches;
	public static FloatBuffer camera_match_lines;
	public static int num_camera_matches;

	// draw pts as black or greenish based on bundle track length
	private static boolean draw_spooky_track_colors = false;

	public static class Camera {
		Matrix3f r;
		Vector3f t;
		Vector3f pos;
		float focal_length;
		int w;
		int h;
		public Vector3f pt0;
		public Vector3f pt1;
		public Vector3f pt2;
		public Vector3f pt3;
		public Vector3f center;
		public boolean draw = false;

		public Camera(float focal_length2, Matrix3f r2, Vector3f t2,
				Vector3f pos2, int w2, int h2) {
			focal_length = focal_length2;
			r = r2;
			t = t2;
			pos = pos2;
			w = w2;
			h = h2;
		}

		public void setFrustaVertices(Vector3f pt0, Vector3f pt1, Vector3f pt2,
				Vector3f pt3, Vector3f pt4) {
			this.pt0 = pt0;
			this.pt1 = pt1;
			this.pt2 = pt2;
			this.pt3 = pt3;
			this.center= pt4;
		}

		public void draw() {

			glColor3f(.3f, 0, .6f);
			glLineWidth(2);
			glBegin(GL_LINES);
			glVertex3f(center.x, center.y, center.z);
			glVertex3f(pt0.x, pt0.y, pt0.z);
			glVertex3f(center.x, center.y, center.z);
			glVertex3f(pt1.x, pt1.y, pt1.z);
			glVertex3f(center.x, center.y, center.z);
			glVertex3f(pt2.x, pt2.y, pt2.z);
			glVertex3f(center.x, center.y, center.z);
			glVertex3f(pt3.x, pt3.y, pt3.z);
			glEnd();
			
			glBegin(GL_LINE_LOOP);
			glVertex3f(pt0.x, pt0.y, pt0.z);
			glVertex3f(pt1.x, pt1.y, pt1.z);
			glVertex3f(pt2.x, pt2.y, pt2.z);
			glVertex3f(pt3.x, pt3.y, pt3.z);
			glEnd();
			

		}
	}

	private static void initBuffers() {
		point_colors = BufferUtils.createByteBuffer(num_points * 4);
		backup_colors = BufferUtils.createByteBuffer(num_points * 4);

		markPointVBODirty();
		point_positions = BufferUtils.createFloatBuffer(num_points * 3);
		point_normals = BufferUtils.createFloatBuffer(num_points * 3);
		point_properties = BufferUtils.createByteBuffer(num_points * 3);
		camera_frusta_lines = BufferUtils
				.createFloatBuffer(num_cameras * 3 * 16);
		point_colors.rewind();
		point_positions.rewind();
		point_normals.rewind();
		point_properties.rewind();

		for (int i = 0; i < num_points; i++) {
			point_properties.put(i, (byte) 0);
		}
	}

	public static void markPointVBODirty() {
		point_vbo_dirty = true;
	}

	public static void markPointVBOClean() {
		point_vbo_dirty = false;
	}

	public static boolean isPointVBODirty() {
		return point_vbo_dirty;
	}

	public static void loadRandom() {
		num_points = 2000;
		initBuffers();

		for (int i = 0; i < num_points; i++) {
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 4 + k, (byte) (Math.random() * 255));
				point_positions.put(i * 3 + k,
						(float) (Math.random() * 0.01 - 0.005));
			}
			point_colors.put(i * 4 + 3, (byte) 255);
		}
		point_colors.rewind();
		point_positions.rewind();
		for (int k = 0; k < 3; k++) {
			min_corner[k] = -0.005f;
			max_corner[k] = 0.005f;
		}
		buildLookupTree();
	}

	public static void loadCube() {
		int n = 50000;
		num_points = n * 6;
		float s = 0.05f;
		initBuffers();

		for (int i = 0; i < num_points; i++) {
			double[] pos = new double[3];
			for (int k = 0; k < 3; k++) {
				pos[k] = Math.random();
			}

			for (int k = 0; k < 3; k++) {
				float b = (float) (pos[k] * 2 * s - s);
				point_positions.put(i * 3 + k, b);
				point_colors.put(i * 4 + 3, (byte) 255);
			}

		}

		for (int i = n * 0; i < n * 1; i++) {
			point_positions.put(i * 3 + 0, -s);
			byte[] color = { (byte) 153, 84, (byte) 153 };
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 4 + k, color[k]);
			}
		}
		for (int i = n * 1; i < n * 2; i++) {
			point_positions.put(i * 3 + 0, s);
			byte[] color = { -1, -1, -1 };
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 4 + k, color[k]);
			}
		}
		for (int i = n * 2; i < n * 3; i++) {
			point_positions.put(i * 3 + 1, -s);
			byte[] color = { 120, 84, (byte) 136 };
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 4 + k, color[k]);
			}
		}
		for (int i = n * 3; i < n * 4; i++) {
			point_positions.put(i * 3 + 1, s);
			byte[] color = { 51, 25, 51 };
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 4 + k, color[k]);
			}
		}
		for (int i = n * 4; i < n * 5; i++) {
			point_positions.put(i * 3 + 2, -s);
			byte[] color = { 120, 100, (byte) 153 };
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 4 + k, color[k]);
			}
		}
		for (int i = n * 5; i < n * 6; i++) {
			point_positions.put(i * 3 + 2, s);
		}

		point_colors.rewind();
		point_positions.rewind();
		for (int k = 0; k < 3; k++) {
			min_corner[k] = (-1 * s) * 2;
			max_corner[k] = s * 2;
		}
		buildLookupTree();
	}

	public static void loadCubes(List<Vector3f> cube_positions,
			float cube_extent, int points_per_cube, float world_extent) {
		num_points = cube_positions.size() * points_per_cube;
		initBuffers();

		for (int c = 0; c < cube_positions.size(); c++) {
			float[] cube_center = new float[3];
			cube_center[0] = cube_positions.get(c).getX();
			cube_center[1] = cube_positions.get(c).getY();
			cube_center[2] = cube_positions.get(c).getZ();

			for (int i = 0; i < points_per_cube; i++) {
				double pos[] = new double[3];
				for (int k = 0; k < 3; k++) {
					pos[k] = cube_extent * (Math.random() * 2 - 1)
							+ cube_center[k];
				}

				int side = (int) Math.floor(Math.random() * 6f);
				switch (side) {
				case 0:
					pos[0] = cube_center[0] + cube_extent;
					break;
				case 1:
					pos[0] = cube_center[0] - cube_extent;
					break;
				case 2:
					pos[1] = cube_center[1] + cube_extent;
					break;
				case 3:
					pos[1] = cube_center[1] - cube_extent;
					break;
				case 4:
					pos[2] = cube_center[2] + cube_extent;
					break;
				case 5:
					pos[2] = cube_center[2] - cube_extent;
					break;
				}

				for (int k = 0; k < 3; k++) {
					point_positions.put((float) pos[k]);
				}
				point_colors.put((byte) 200);
				point_colors.put((byte) 0);
				point_colors.put((byte) 20);
				point_colors.put((byte) 255);
			}
		}

		point_colors.rewind();
		point_positions.rewind();

		for (int k = 0; k < 3; k++) {
			min_corner[k] = -1f * world_extent;
			max_corner[k] = world_extent;
		}

		buildLookupTree();
	}

	public static void loadCubesOfDifferentSizes(List<Vector3f> cube_positions,
			float[] cube_extent, int[] points_per_cube, float world_extent) {

		int num_cubes = cube_positions.size();
		for (int i = 0; i < num_cubes; i++) {
			num_points += points_per_cube[i];
		}

		initBuffers();

		for (int c = 0; c < num_cubes; c++) {
			float[] cube_center = new float[3];
			cube_center[0] = cube_positions.get(c).getX();
			cube_center[1] = cube_positions.get(c).getY();
			cube_center[2] = cube_positions.get(c).getZ();

			for (int h = 0; h < points_per_cube[c]; h++) {

				double pos[] = new double[3];
				for (int k = 0; k < 3; k++) {
					pos[k] = cube_extent[c] * (Math.random() * 2 - 1)
							+ cube_center[k];
				}

				int side = (int) Math.floor(Math.random() * 6f);
				switch (side) {
				case 0:
					pos[0] = cube_center[0] + cube_extent[c];
					break;
				case 1:
					pos[0] = cube_center[0] - cube_extent[c];
					break;
				case 2:
					pos[1] = cube_center[1] + cube_extent[c];
					break;
				case 3:
					pos[1] = cube_center[1] - cube_extent[c];
					break;
				case 4:
					pos[2] = cube_center[2] + cube_extent[c];
					break;
				case 5:
					pos[2] = cube_center[2] - cube_extent[c];
					break;
				}

				for (int k = 0; k < 3; k++) {
					point_positions.put((float) pos[k]);
				}
				point_colors.put((byte) 200);
				point_colors.put((byte) 0);
				point_colors.put((byte) 20);
				point_colors.put((byte) 255);
			}
		}

		point_colors.rewind();
		point_positions.rewind();

		for (int k = 0; k < 3; k++) {
			min_corner[k] = -1f * world_extent;
			max_corner[k] = world_extent;
		}

		buildLookupTree();
	}

	public static void load(String filename) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader(filename));
			String first_line = buf.readLine();

			if (first_line.startsWith("ply")) {
				parsePlyFile(buf, filename);
				buildLookupTree();
			} else if (first_line.contains("# Bundle file")) {
				parseBundleFile(first_line, buf, filename);
				buildLookupTree();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static Matrix3f testRotate() {
		Vector3f p1 = new Vector3f(-0.005176955f, 0.0143530285f,-0.013278407f);
		Vector3f p2 = new Vector3f(-0.021221291f, -0.03875064f, -0.010480127f);
		Vector3f v1 = new Vector3f(0,1,0);
		Vector3f v2 = Vector3f.sub(p1, p2, null);
		v2.normalise();
		Vector3f v3 = Vector3f.cross(v1, v2, null);
		v3.normalise();
		Vector3f v4 = Vector3f.cross(v3, v1, null);
		float cos = Vector3f.dot(v2, v1);
		float sin = Vector3f.dot(v2, v4);
		
		Matrix3f m1 = new Matrix3f();
		m1.m00 = v1.x;
		m1.m10 = v1.y;
		m1.m20 = v1.z;
		m1.m01 = v4.x;
		m1.m11 = v4.y;
		m1.m21 = v4.z;
		m1.m02 = v3.x;
		m1.m12 = v3.y;
		m1.m22 = v3.z;
		
		Matrix3f m2 = new Matrix3f();
		m2.setIdentity();
		m2.m00 = cos;
		m2.m10 = sin;
		m2.m01 = -sin;
		m2.m11 = cos;
		
		Matrix3f m1i = new Matrix3f();
		m1i.load(m1);
		m1i.invert();
		
		Matrix3f t = Matrix3f.mul(Matrix3f.mul(m1i, m2, null), m1, null);
		
		return t;
	}

	public static int getNearestPoint(float x, float y, float z, float radius) {
		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
				new Vec3D(x, y, z), radius);
		if (results != null)
			return index_map.get(results.get(0));
		else
			return 0;
	}

	public static int[] getNearestPoints(float x, float y, float z, float radius) {
		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
				new Vec3D(x, y, z), radius);
		if (results != null) {
			int[] indices = new int[results.size()];
			for (int i = 0; i < results.size(); i++) {
				indices[i] = index_map.get(results.get(i));
			}
			return indices;
		} else
			return null;
	}

	public static int getNumPointsInSphere(float x, float y, float z,
			float radius) {
		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
				new Vec3D(x, y, z), radius);
		if (results != null)
			return results.size();
		else
			return 0;
	}

	public static Vector3f getCenter(float x, float y, float z, float radius) {
		Vector3f center = new Vector3f();
		ArrayList<Vec3D> results = tree.getPointsWithinSphere(
				new Vec3D(x, y, z), radius);
		if (results != null) {
			for (Vec3D p : results) {
				center.x += p.x;
				center.y += p.y;
				center.z += p.z;
			}
			center.scale(1f / results.size());
			return center;
		} else {
			return null;
		}
	}

	private static void buildLookupTree() {
		point_positions.rewind();
		point_colors.rewind();

		backup_colors.put(point_colors);
		backup_colors.rewind();
		point_colors.rewind();

		Main.world_scale = (float) ((float) ((PointStore.max_corner[1] - PointStore.min_corner[1])) / 0.071716);
		if (Main.world_scale == 0)
			Main.world_scale = 1;
		// lewis hall height for scale ref...

		System.out.println("starting to build lookup tree");
		Vec3D center = new Vec3D(min_corner[0] - 0.001f,
				min_corner[1] - 0.001f, min_corner[2] - 0.001f);
		float max_span = max_corner[0] - min_corner[0];
		if (max_corner[1] - min_corner[1] > max_span)
			max_span = max_corner[1] - min_corner[1];
		if (max_corner[2] - min_corner[2] > max_span)
			max_span = max_corner[2] - min_corner[2];

		tree = new PointOctree(center, max_span + 0.001f);
		tree.setMinNodeSize(max_span / 2);

		index_map = new HashMap<Vec3D, Integer>();

		for (int i = 0; i < num_points; i++) {
			final Vec3D vec = new Vec3D((float) point_positions.get(i * 3 + 0),
					(float) point_positions.get(i * 3 + 1),
					(float) point_positions.get(i * 3 + 2));
			index_map.put(vec, i);
			tree.addPoint(vec);
		}
		System.out.println("done adding " + tree.getPoints().size()
				+ " points to lookup tree");

		// tree.intersectsRay(arg0, arg1, arg2)
	}

	private static void parsePlyFile(BufferedReader buf, String filename) {
		try {
			int r_idx, g_idx, b_idx, a_idx;
			int x_idx, y_idx, z_idx;
			int nx_idx, ny_idx, nz_idx;
			r_idx = g_idx = b_idx = x_idx = y_idx = z_idx = 0;
			a_idx = -1;
			nx_idx = ny_idx = nz_idx = 0;

			initPointStore();

			boolean binary = false;
			int chars_read = 0;
			int len = 0;
			boolean vert_properties_started = false;
			boolean vert_properties_finished = false;

			int count = 0;
			String line = buf.readLine();
			chars_read += (line.length() + 1);
			while (!line.startsWith("end_header")) {
				if (line.startsWith("element vertex")) {
					num_points = Integer.parseInt(line.split("\\s+")[2]);
					count = 0;
					vert_properties_started = true;
				} else if (line.startsWith("element")) {
					if (vert_properties_started) {
						vert_properties_finished = true;
					}
				} else if (line.contains("cloud_id")) {
					int cloud_id = Integer.parseInt(line.split(" ")[2]);
					Main.server.setTextureServer(cloud_id);
				}

				if (line.contains("binary_little_endian")) {
					binary = true;
				}

				if (line.contains(" x"))
					x_idx = count;
				else if (line.contains(" y"))
					y_idx = count;
				else if (line.contains(" z"))
					z_idx = count;
				else if (line.contains("red")) {
					r_idx = count;
					len -= 3;
				} else if (line.contains("green")) {
					g_idx = count;
					len -= 3;
				} else if (line.contains("blue")) {
					b_idx = count;
					len -= 3;
				} else if (line.contains("alpha")) {
					a_idx = count;
					len -= 3;
				}
				else if (line.contains(" nx")) {
					nx_idx = count;
				}
				else if (line.contains(" ny")) {
					ny_idx = count;
				}
				else if (line.contains(" nz")) {
					nz_idx = count;
				}

				if (line.contains("property") && vert_properties_started
						&& !vert_properties_finished)
					count++;

				line = buf.readLine();
				chars_read += (line.length() + 1);
			}
			chars_read += 4;

			System.out.println(x_idx + "," + y_idx + "," + z_idx + "    "
					+ r_idx + "," + g_idx + "," + b_idx);

			initBuffers();

			len += count * 4;

			if (!binary) {
				int i = 0;
				line = buf.readLine();
				for (int j = 0; j < num_points; j++) {
					String[] split_line = line.split("\\s+");
					point_colors
							.put((byte) Integer.parseInt(split_line[r_idx]));
					point_colors
							.put((byte) Integer.parseInt(split_line[g_idx]));
					point_colors
							.put((byte) Integer.parseInt(split_line[b_idx]));
					point_colors.put((byte) 255); // alpha

					point_positions.put(Float.parseFloat(split_line[x_idx]));
					point_positions.put(Float.parseFloat(split_line[y_idx]));
					point_positions.put(Float.parseFloat(split_line[z_idx]));
					
					if (nx_idx > 0 || ny_idx > 0 || nz_idx > 0) {
						point_normals.put(Float.parseFloat(split_line[nx_idx]));
						point_normals.put(Float.parseFloat(split_line[ny_idx]));
						point_normals.put(Float.parseFloat(split_line[nz_idx]));
					}
					else {
						point_normals.put(0f);
						point_normals.put(1f);
						point_normals.put(0f);
					}
					
					for (int k = 0; k < 3; k++) {
						if (point_positions.get(i * 3 + k) < min_corner[k]) {
							min_corner[k] = (float) point_positions.get(i * 3
									+ k);
						}
						if (point_positions.get(i * 3 + k) > max_corner[k]) {
							max_corner[k] = (float) point_positions.get(i * 3
									+ k);
						}
					}
					line = buf.readLine();
					i++;
					if (i % 10000 == 0){
						System.out.println("points loaded: " + i + "/"
								+ num_points);
					}
				}
			} else {
				// advance the binary thing forward past the ascii
				DataInputStream stream = new DataInputStream(
						new FileInputStream(filename));
				byte[] header = new byte[chars_read];
				stream.read(header, 0, chars_read);

				byte[] pt = new byte[len];
				ByteBuffer bb = BufferUtils.createByteBuffer(len);
				bb.order(ByteOrder.LITTLE_ENDIAN);

				// System.out.println("reading " + count + " values with " + len
				// + " bytes");

				for (int h = 0; h < num_points; h++) {
					stream.read(pt, 0, len);
					bb.rewind();
					bb.put(pt, 0, len);
					bb.rewind();

					byte r = 0, g = 0, b = 0;
					float x = 0, y = 0, z = 0;
					float nx = 0, ny = 1, nz = 0;
					for (int i = 0; i < count; i++) {
						if (i == r_idx) {
							r = bb.get();
							// System.out.print(r + ", ");
						} else if (i == g_idx) {
							g = bb.get();
							// System.out.print(g + ", ");
						} else if (i == b_idx) {
							b = bb.get();
							// System.out.print(b + ", ");
						} else if (i == a_idx) {
							bb.get();
						} else if (i == x_idx) {
							x = bb.getFloat();
						} else if (i == y_idx) {
							y = bb.getFloat();
						} else if (i == z_idx) {
							z = bb.getFloat();
						} else if (i == nx_idx) {
							nx = bb.getFloat();
						} else if (i == ny_idx) {
							ny = bb.getFloat();
						} else if (i == nz_idx) {
							nz = bb.getFloat();
						} else {
							bb.getFloat();
							// System.out.print(f + ", ");
						}
					}
					// System.out.println("");

					point_colors.put(r);
					point_colors.put(g);
					point_colors.put(b);
					point_colors.put((byte) 255);

					point_positions.put(x);
					point_positions.put(y);
					point_positions.put(z);
					
					point_normals.put(nx);
					point_normals.put(ny);
					point_normals.put(nz);

					for (int k = 0; k < 3; k++) {
						if (point_positions.get(h * 3 + k) < min_corner[k]) {
							min_corner[k] = (float) point_positions.get(h * 3
									+ k);
						}
						if (point_positions.get(h * 3 + k) > max_corner[k]) {
							max_corner[k] = (float) point_positions.get(h * 3
									+ k);
						}
					}

					if (h % 10000 == 0) {
						System.out.println("points loaded: " + h + "/"
								+ num_points);
						// System.out.println(" " + r + "," + g + "," + b );

					}
				}

				stream.close();
			}

			point_colors.rewind();
			point_positions.rewind();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void initPointStore() {
		for (int k = 0; k < 3; k++) {
			min_corner[k] = Float.MAX_VALUE;
			max_corner[k] = -1 * Float.MAX_VALUE;
		}

		if (Main.server != null)
			Main.server.texture_server = null;
	}

	private static void parseBundleFile(String first_line, BufferedReader buf,
			String filename) throws IOException {
		System.out.println("Reading bundle file");

		String version = "";
		if (first_line.contains("v0.3"))
			version = "0.3";
		else if (first_line.contains("v0.5"))
			version = "0.5";

		String second_line = buf.readLine();
		num_cameras = 0;
		num_points = 0;

		num_cameras = Integer.parseInt(second_line.split("\\s+")[0]);
		num_points = Integer.parseInt(second_line.split("\\s+")[1]);

		camera_matches = new boolean[num_cameras][num_cameras];

		System.out.println("Version #: " + version + ", Reading " + num_cameras
				+ " cameras and " + num_points + " points");

		initBuffers();

		Main.draw_cameras = true;
		
		Matrix3f testRotate = testRotate();
		testRotate.setIdentity();
		Matrix3f testRotateTrans = new Matrix3f();
		testRotateTrans.load(testRotate);
		testRotateTrans.transpose();

		for (int i = 0; i < num_cameras; i++) {
			float focal_length;
			Matrix3f r = new Matrix3f();
			Vector3f t = new Vector3f();
			Vector2f rd = new Vector2f();
			String[] s;
			int w = 1600; // default image width
			int h = 1200; // and height
			String path = "";

			if (version == "0.5") {
				s = buf.readLine().split("\\s+"); // file name
				path = s[0];
				w = Integer.parseInt(s[1]);
				h = Integer.parseInt(s[2]);
			}

			s = buf.readLine().split("\\s+");
			focal_length = Float.parseFloat(s[0]);
			rd.x = Float.parseFloat(s[1]);
			rd.y = Float.parseFloat(s[2]);

			s = buf.readLine().split("\\s+");

			r.m00 = Float.parseFloat(s[0]);
			r.m10 = Float.parseFloat(s[1]);
			r.m20 = Float.parseFloat(s[2]);

			s = buf.readLine().split("\\s+");

			r.m01 = Float.parseFloat(s[0]);
			r.m11 = Float.parseFloat(s[1]);
			r.m21 = Float.parseFloat(s[2]);

			s = buf.readLine().split("\\s+");

			r.m02 = Float.parseFloat(s[0]);
			r.m12 = Float.parseFloat(s[1]);
			r.m22 = Float.parseFloat(s[2]);

			s = buf.readLine().split("\\s+");

			Matrix3f.mul(r, testRotateTrans, r);
			
			t.x = Float.parseFloat(s[0]);
			t.y = Float.parseFloat(s[1]);
			t.z = Float.parseFloat(s[2]);
			
			//Matrix3f.transform(testRotate, t, t);

			Vector3f pos = (Vector3f) Matrix3f.transform(
					Matrix3f.transpose(r, null), t, null).scale(-1f);

			Camera c = new Camera(focal_length, r, t, pos, w, h);
			cameras.add(c);
			cameraMap.put(path, c);
		}

		for (int i = 0; i < num_points; i++) {
			if (version == "0.5") {
				buf.readLine(); // player id who owns the point
			}
			System.out.println("point number: " + i);
			String[] point = buf.readLine().split("\\s+");
			String[] color = buf.readLine().split("\\s+");
			String[] tracks = buf.readLine().split("\\s+");

			int track_count = Integer.parseInt(tracks[0]);

			if (track_count < 30) {
				for (int j = 0; j < track_count; j++) {
					for (int k = j + 1; k < track_count; k++) {
						int a, b;
						if (version == "0.3") {
							a = Integer.parseInt(tracks[j * 4 + 1]);
							b = Integer.parseInt(tracks[k * 4 + 1]);
						} else {
							a = Integer.parseInt(tracks[j * 2 + 1]);
							b = Integer.parseInt(tracks[k * 2 + 1]);
						}
						boolean m = camera_matches[a][b];
						if (m == false) {
							camera_matches[a][b] = true;
							num_camera_matches++;
						}
					}
				}
			}

			if (draw_spooky_track_colors) {
				point_colors.put((byte) 0);
				if (track_count >= 12) {
					point_colors.put((byte) (track_count * 3));
				} else {
					point_colors.put((byte) 0);
				}
				point_colors.put((byte) 0);

			} else {
				point_colors.put((byte) Integer.parseInt(color[0]));
				point_colors.put((byte) Integer.parseInt(color[1]));
				point_colors.put((byte) Integer.parseInt(color[2]));

			}

			if (track_count > 0)
				point_colors.put((byte) 255);
			else
				point_colors.put((byte) 0);

			Vector3f p = new Vector3f(Float.parseFloat(point[0]), Float.parseFloat(point[1]), Float.parseFloat(point[2]));
			Matrix3f.transform(testRotate, p, p);
			point_positions.put(p.x);
			point_positions.put(p.y);
			point_positions.put(p.z);
			/*
			point_positions.put(Float.parseFloat(point[0]));
			point_positions.put(Float.parseFloat(point[1]));
			point_positions.put(Float.parseFloat(point[2]));
			*/

			for (int k = 0; k < 3; k++) {
				if (point_positions.get(i * 3 + k) < min_corner[k]) {
					min_corner[k] = (float) point_positions.get(i * 3 + k);
				}
				if (point_positions.get(i * 3 + k) > max_corner[k]) {
					max_corner[k] = (float) point_positions.get(i * 3 + k);
				}
			}
		}

		if (Main.draw_matches){
			buildCameraMatchLines();
		}

		buildCameraFrustaWithWorldScale();

	}

	private static void buildCameraMatchLines() {
		camera_match_lines = BufferUtils
				.createFloatBuffer(num_camera_matches * 2 * 3);
		camera_match_lines.rewind();
		int edges_added = 0;
		for (int i = 0; i < num_cameras; i++) {
			for (int j = 0; j < num_cameras; j++) {
				if (i != j) {
					boolean m = camera_matches[i][j];
					if (m == true) {
						camera_match_lines.put(cameras.get(i).pos.x);
						camera_match_lines.put(cameras.get(i).pos.y);
						camera_match_lines.put(cameras.get(i).pos.z);
						camera_match_lines.put(cameras.get(j).pos.x);
						camera_match_lines.put(cameras.get(j).pos.y);
						camera_match_lines.put(cameras.get(j).pos.z);
						edges_added++;
					}
				}
			}
		}

		camera_match_lines.rewind();

		System.out.println("edges_added: " + edges_added
				+ ", camera match lines; " + num_camera_matches);
	}

	private static void buildCameraFrustaWithWorldScale() {
		for (int i = 0; i < cameras.size(); i++) {
			Camera c = cameras.get(i);
			float scale = Main.world_scale / 2000f;
			float ref_length = (float) (2.0e-3 * Math
					.min(8000f, c.focal_length));
			float xExt = 0.5f * c.w * ref_length * scale / c.focal_length;
			float yExt = 0.5f * c.h * ref_length * scale / c.focal_length;
			Vector3f pos = c.pos;

			Vector3f pt0 = new Vector3f(-xExt, -yExt, -ref_length * scale);
			pt0 = Vector3f.add(Matrix3f.transform(
					Matrix3f.transpose(c.r, null), pt0, null), pos, null);

			Vector3f pt1 = new Vector3f(-xExt, yExt, -ref_length * scale);
			pt1 = Vector3f.add(Matrix3f.transform(
					Matrix3f.transpose(c.r, null), pt1, null), pos, null);

			Vector3f pt2 = new Vector3f(xExt, yExt, -ref_length * scale);
			pt2 = Vector3f.add(Matrix3f.transform(
					Matrix3f.transpose(c.r, null), pt2, null), pos, null);

			Vector3f pt3 = new Vector3f(xExt, -yExt, -ref_length * scale);
			pt3 = Vector3f.add(Matrix3f.transform(
					Matrix3f.transpose(c.r, null), pt3, null), pos, null);

			// center of plane
			Vector3f pt4 = new Vector3f(0, 0, -ref_length * scale);
			pt4 = Vector3f.add(Matrix3f.transform(c.r, pt4, null), pos, null);

			c.setFrustaVertices(pt0, pt1, pt2, pt3, pos);

			int n = 16;
			addFrustaVertex(i, n, 0, pos);
			addFrustaVertex(i, n, 1, pt0);
			addFrustaVertex(i, n, 2, pos);
			addFrustaVertex(i, n, 3, pt1);
			addFrustaVertex(i, n, 4, pos);
			addFrustaVertex(i, n, 5, pt2);
			addFrustaVertex(i, n, 6, pos);
			addFrustaVertex(i, n, 7, pt3);

			addFrustaVertex(i, n, 8, pt0);
			addFrustaVertex(i, n, 9, pt1);

			addFrustaVertex(i, n, 10, pt1);
			addFrustaVertex(i, n, 11, pt2);

			addFrustaVertex(i, n, 12, pt2);
			addFrustaVertex(i, n, 13, pt3);

			addFrustaVertex(i, n, 14, pt3);
			addFrustaVertex(i, n, 15, pt0);
		}
	}

	private static void addFrustaVertex(int i, int n, int s, Vector3f v) {
		camera_frusta_lines.put(i * n * 3 + s * 3 + 0, v.x);
		camera_frusta_lines.put(i * n * 3 + s * 3 + 1, v.y);
		camera_frusta_lines.put(i * n * 3 + s * 3 + 2, v.z);
	}

	public static Vector3f getIthPoint(int i) {
		return new Vector3f((float) point_positions.get(i * 3 + 0),
				(float) point_positions.get(i * 3 + 1),
				(float) point_positions.get(i * 3 + 2));
	}

	public static void changePointColorToRed(int i) {
		point_colors.put(i * 4 + 0, (byte) 255);
		point_colors.put(i * 4 + 1, (byte) 0);
		point_colors.put(i * 4 + 2, (byte) 0);
		markPointVBODirty();
	}

	public static void makePointTransparent(int i) {
		point_colors.put(i * 4 + 3, (byte) 100);
		markPointVBODirty();
	}

	public static void changePointColorToGray(int i) {
		point_colors.put(i * 4 + 0, (byte) 130);
		point_colors.put(i * 4 + 1, (byte) 130);
		point_colors.put(i * 4 + 2, (byte) 130);
		markPointVBODirty();
	}

	public static void changeColorOfPointSubsetToGreen(int start, int finish) {
		for (int i = start; i < finish; i++) {
			point_colors.put(i * 4 + 0, (byte) 10);
			point_colors.put(i * 4 + 1, (byte) 180);
			point_colors.put(i * 4 + 2, (byte) 40);
			markPointVBODirty();
		}

	}

	public static void changeColorOfPointSubsetToGray(int start, int finish,
			int gray) {
		for (int i = start; i < finish; i++) {
			point_colors.put(i * 4 + 0, (byte) gray);
			point_colors.put(i * 4 + 1, (byte) gray);
			point_colors.put(i * 4 + 2, (byte) gray);
			markPointVBODirty();
		}
	}

	public static void updateColors(BitSet bs) {
		point_colors.rewind();
		backup_colors.rewind();
		point_colors.put(backup_colors);
		point_colors.rewind();
		backup_colors.rewind();

		for (int i = bs.nextSetBit(0); i >= 0; i = bs.nextSetBit(i + 1)) {
			changePointColorToRed(i);
		}

	}

}
