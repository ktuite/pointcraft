package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.List;
import java.util.Vector;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class Primitive {

	private int gl_type;
	private List<Pellet> vertices;
	private float line_width = 5f;
	private Vector<byte[]> texture_data = null;
	private transient Vector<Texture> textures = null;
	private Vector3f player_position;
	private Vector3f player_viewing_direction;
	private String[] texture_url = null;
	private int num_triangles = 0;
	private boolean textures_loaded;
	private int texture_count;

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		if (texture_url != null) {
			// texture_url = "dont_texture-" + texture_url;
			startDownloadingTexture();
		}
	}

	public Primitive(int _gl_type, List<Pellet> _vertices) {
		gl_type = _gl_type;
		vertices = _vertices;
		textures_loaded = false;
		if (gl_type == GL_POLYGON) {
			num_triangles = vertices.size() - 3;
			texture_data = new Vector<byte[]>();
			texture_data.setSize(num_triangles);
			textures = new Vector<Texture>();
			textures.setSize(num_triangles);
			System.out.println("making new polygon Primitive");
			System.out.println("number of vertices: " + vertices.size());
		}

	}

	public Primitive(int _gl_type, List<Pellet> _vertices, float _line_width) {
		gl_type = _gl_type;
		vertices = _vertices;
		line_width = _line_width;
		num_triangles = vertices.size() - 3;
	}

	public void setPlayerPositionAndViewingDirection(Vector3f pos, Vector3f view) {
		player_position = new Vector3f(pos);
		player_viewing_direction = new Vector3f(view);
		player_viewing_direction.normalise();
		if (vertices.size() >= 4)
			startDownloadingTexture();
	}

	public boolean isPolygon() {
		if (gl_type == GL_POLYGON)
			return true;
		else
			return false;
	}

	public int numVertices() {
		return vertices.size();
	}

	public List<Pellet> getVertices() {
		return vertices;
	}

	public void draw() {
		if (gl_type == GL_LINES) {
			glColor3f(0, 0, 0);
			glLineWidth(line_width);

			glBegin(gl_type);
			for (Pellet p : vertices) {
				Vector3f vertex = p.pos;
				glVertex3f(vertex.x, vertex.y, vertex.z);
			}
			glEnd();

		} else if (gl_type == GL_POLYGON) {
			glColor4f(.9f, .9f, .9f, .9f);
			if (textures_loaded) {
				glEnable(GL_TEXTURE_2D);
			} else {
				glDisable(GL_TEXTURE_2D);
				if (texture_count == num_triangles) {
					try {
						for (int i = 0; i < num_triangles; i++) {
							System.out.println(" texture set!!");
							textures.set(i, TextureLoader.getTexture(
									"PNG",
									new ByteArrayInputStream(texture_data
											.get(i))));

						}
						texture_data.clear();
						textures_loaded = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		Vector2f[] tex_coords = new Vector2f[] { new Vector2f(0, 0),
				new Vector2f(1, 0), new Vector2f(0, 1) };// , new Vector2f(0, 1)
															// };

		for (int h = 0; h < num_triangles; h++) {
			if (textures_loaded) {
				textures.get(h).bind();
			}

			glBegin(gl_type);
			Pellet pellet;
			Vector3f vertex;
			for (int i = 0; i < tex_coords.length - 1; i++) {
				pellet = vertices.get(i + h + 1);
				vertex = pellet.pos;
				glTexCoord2f(tex_coords[i + 1].x, tex_coords[i + 1].y);
				glVertex3f(vertex.x, vertex.y, vertex.z);
			}
			pellet = vertices.get(0);
			vertex = pellet.pos;
			glTexCoord2f(tex_coords[0].x, tex_coords[0].y);
			glVertex3f(vertex.x, vertex.y, vertex.z);
			glEnd();

		}
		
		// draw a border around the polygons
		for (int h = 0; h < num_triangles; h++) {
			glDisable(GL_TEXTURE_2D);
			glColor3f(0, 0, 0);
			glLineWidth(3);
			Pellet pellet;
			Vector3f vertex;
			glBegin(GL_LINE_LOOP);
			for (int i = 0; i < tex_coords.length - 1; i++) {
				pellet = vertices.get(i + h + 1);
				vertex = pellet.pos;
				glVertex3f(vertex.x, vertex.y, vertex.z);
			}
			pellet = vertices.get(0);
			vertex = pellet.pos;
			glVertex3f(vertex.x, vertex.y, vertex.z);
			glEnd();
		}
	}

	public void startDownloadingTexture() {
		if (texture_url == null) {
			texture_url = new String[num_triangles];
			for (int i = 0; i < num_triangles; i++) {
				texture_url[i] = "http://mazagran.cs.washington.edu:8081/texture.png?&v=";

				// triangle fan going on here
				Pellet p = vertices.get(0);
				Vector3f v = p.pos;
				texture_url[i] += v.x + "," + v.y + "," + v.z + ",";

				for (int j = i + 1; j < i + 3; j++) {
					p = vertices.get(j);
					v = p.pos;
					texture_url[i] += v.x + "," + v.y + "," + v.z + ",";
				}
				texture_url[i] += "garbage,&w=128,&h=128,";
				if (player_position != null && player_viewing_direction != null) {
					texture_url[i] += "&p=" + player_position.x + ","
							+ player_position.y + "," + player_position.z + ",";
					texture_url[i] += "&e=" + player_viewing_direction.x + ","
							+ player_viewing_direction.y + ","
							+ player_viewing_direction.z + ",";
				}
			}
		}

		texture_count = 0;
		for (int i = 0; i < num_triangles; i++) {
			final int f_i = i;
			System.out.println(texture_url[f_i]);
			final String final_url_string = texture_url[f_i];
			new Thread() {
				public void run() {

					try {
						URL url = new URL(final_url_string);
						InputStream is = url.openStream();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] bytes = new byte[4096];
						int n;
						while ((n = is.read(bytes)) != -1) {
							baos.write(bytes, 0, n);
						}
						byte[] tex_byte_data = baos.toByteArray();
						texture_data.set(f_i, tex_byte_data);
						texture_count++;
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}.start();
		}
	}

	public String plyFace() {
		String s = vertices.size() - 1 + "";
		for (int i = 0; i < vertices.size() - 1; i++) {
			Pellet pellet = vertices.get(i);
			s += " " + Main.all_pellets_in_world.indexOf(pellet);
		}
		s += "\n";
		System.out.println("PLYFACE:" + s);
		return s;
	}

	public void printTriangleVertices() {
		for (int i = 0; i < vertices.size() - 1; i++) {
			Pellet pellet = vertices.get(i);
			System.out.println(pellet.pos.x + " " + pellet.pos.y + " "
					+ pellet.pos.z);
		}
		System.out.println("");
	}

	public float distanceToPolygonPlane(Vector3f pos) {
		Vector3f v1 = new Vector3f();
		Vector3f.sub(vertices.get(0).pos, vertices.get(1).pos, v1);
		Vector3f v2 = new Vector3f();
		Vector3f.sub(vertices.get(0).pos, vertices.get(2).pos, v2);
		Vector3f norm = new Vector3f();
		Vector3f.cross(v1, v2, norm);
		if (norm.lengthSquared() == 0)
			return 0;
		norm.normalise();

		float a = norm.x;
		float b = norm.y;
		float c = norm.z;
		float d = -1
				* (a * vertices.get(0).pos.x + b * vertices.get(0).pos.y + c
						* vertices.get(0).pos.z);

		float distance = (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math
				.sqrt(a * a + b * b + c * c));

		return Math.abs(distance);
	}

	public static void addBackDeletedPrimitive(Primitive primitive) {

		Main.geometry.add(primitive);
	}
}
