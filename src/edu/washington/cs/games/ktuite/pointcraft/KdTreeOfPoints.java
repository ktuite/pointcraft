package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.util.ArrayList;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import toxi.geom.PointOctree;
import toxi.geom.Vec3D;

public class KdTreeOfPoints {

	// stuff about the point cloud
	public static int num_points;
	public static DoubleBuffer point_positions;
	public static DoubleBuffer point_colors;
	private static PointOctree tree;
	public static float min_corner[] = { Float.MAX_VALUE, Float.MAX_VALUE,
			Float.MAX_VALUE };
	public static float max_corner[] = { -1 * Float.MAX_VALUE,
			-1 * Float.MAX_VALUE, -1 * Float.MAX_VALUE };

	public static void loadRandom() {
		num_points = 2000;
		point_colors = BufferUtils.createDoubleBuffer(num_points * 3);
		point_positions = BufferUtils.createDoubleBuffer(num_points * 3);

		for (int i = 0; i < num_points; i++) {
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 3 + k, Math.random());
				point_positions.put(i * 3 + k, Math.random() * 0.01 - 0.005);
			}
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
		point_colors = BufferUtils.createDoubleBuffer(num_points * 3);
		point_positions = BufferUtils.createDoubleBuffer(num_points * 3);

		for (int i = 0; i < num_points; i++) {
			double[] pos = new double[3];
			for (int k = 0; k < 3; k++) {
				pos[k] = Math.random();
			}

			for (int k = 0; k < 3; k++) {
				double b = pos[k] * 2 * s - s;
				point_positions.put(i * 3 + k, b);
			}

		}

		for (int i = n * 0; i < n * 1; i++) {
			point_positions.put(i * 3 + 0, -s);
			double[] color = {0.6, .33, 0.6};
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 3 + k, color[k]);
			}
		}
		for (int i = n * 1; i < n * 2; i++) {
			point_positions.put(i * 3 + 0, s);
			double[] color = {1, 1, 1};
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 3 + k, color[k]);
			}
		}
		for (int i = n * 2; i < n * 3; i++) {
			point_positions.put(i * 3 + 1, -s);
			double[] color = {.46, .33, 0.533};
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 3 + k, color[k]);
			}
		}
		for (int i = n * 3; i < n * 4; i++) {
			point_positions.put(i * 3 + 1, s);
			double[] color = {.2, .1, 0.2};
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 3 + k, color[k]);
			}
		}
		for (int i = n * 4; i < n * 5; i++) {
			point_positions.put(i * 3 + 2, -s);
			double[] color = {.46, .4, 0.533};
			for (int k = 0; k < 3; k++) {
				point_colors.put(i * 3 + k, color[k]);
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

	public static void load(String filename) {
		try {
			BufferedReader buf = new BufferedReader(new FileReader(filename));

			if (buf.readLine().startsWith("ply")) {
				parsePlyFile(buf, filename);
				buildLookupTree();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static int queryKdTree(float x, float y, float z, float radius) {
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
		System.out.println("starting to build lookup tree");
		Vec3D center = new Vec3D(min_corner[0], min_corner[1], min_corner[2]);
		float max_span = max_corner[0] - min_corner[0];
		if (max_corner[1] - min_corner[1] > max_span)
			max_span = max_corner[1] - min_corner[1];
		if (max_corner[2] - min_corner[2] > max_span)
			max_span = max_corner[2] - min_corner[2];

		tree = new PointOctree(center, max_span);
		tree.setMinNodeSize(max_span / 2);

		for (int i = 0; i < num_points; i++) {
			tree.addPoint(new Vec3D((float) point_positions.get(i * 3 + 0),
					(float) point_positions.get(i * 3 + 1),
					(float) point_positions.get(i * 3 + 2)));
		}
		System.out.println("done adding " + tree.getPoints().size()
				+ " points to lookup tree");
	}

	private static void parsePlyFile(BufferedReader buf, String filename) {
		try {
			int r_idx, g_idx, b_idx, a_idx;
			int x_idx, y_idx, z_idx;
			r_idx = g_idx = b_idx = x_idx = y_idx = z_idx = 0;
			a_idx = -1;

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

				if (line.contains("property") && vert_properties_started
						&& !vert_properties_finished)
					count++;

				line = buf.readLine();
				chars_read += (line.length() + 1);
			}
			chars_read += 4;

			System.out.println(x_idx + "," + y_idx + "," + z_idx + "    "
					+ r_idx + "," + g_idx + "," + b_idx);

			point_colors = BufferUtils.createDoubleBuffer(num_points * 3);
			point_positions = BufferUtils.createDoubleBuffer(num_points * 3);
			point_colors.rewind();
			point_positions.rewind();

			len += count * 4;

			if (!binary) {
				int i = 0;
				line = buf.readLine();
				while (line != null) {
					String[] split_line = line.split("\\s+");
					point_colors
							.put(Integer.parseInt(split_line[r_idx]) / 255.0);
					point_colors
							.put(Integer.parseInt(split_line[g_idx]) / 255.0);
					point_colors
							.put(Integer.parseInt(split_line[b_idx]) / 255.0);

					point_positions.put(Double.parseDouble(split_line[x_idx]));
					point_positions.put(Double.parseDouble(split_line[y_idx]));
					point_positions.put(Double.parseDouble(split_line[z_idx]));

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
					if (i % 10000 == 0)
						System.out.println("points loaded: " + i + "/"
								+ num_points);
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

				System.out.println("reading " + count + " values with " + len
						+ " bytes");
				for (int h = 0; h < num_points; h++) {
					stream.read(pt, 0, len);
					bb.rewind();
					bb.put(pt, 0, len);
					bb.rewind();

					byte r = 0, g = 0, b = 0;
					double x = 0, y = 0, z = 0;
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
						} else {
							bb.getFloat();
							// System.out.print(f + ", ");
						}
					}
					// System.out.println("");

					point_colors.put((r & 0xFF) / 255.0);
					point_colors.put((g & 0xFF) / 255.0);
					point_colors.put((b & 0xFF) / 255.0);

					point_positions.put(x);
					point_positions.put(y);
					point_positions.put(z);

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

			}

			point_colors.rewind();
			point_positions.rewind();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
}
