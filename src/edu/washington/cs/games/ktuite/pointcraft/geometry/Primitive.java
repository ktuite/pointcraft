package edu.washington.cs.games.ktuite.pointcraft.geometry;

import static org.lwjgl.opengl.GL11.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.tools.Pellet;

/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class Primitive implements org.json.JSONString {

	private int gl_type;
	private List<Pellet> vertices;
	private float line_width = 5f;
	public Vector<byte[]> texture_data = null;
	private Vector<Texture> textures = null;
	private Vector3f player_position;
	private Vector3f player_viewing_direction;
	private String[] texture_url = null;
	public String[] local_textures = null;
	private int num_textures = 0;
	private boolean textures_loaded;
	public int texture_count;
	private boolean is_quad = false;
	private PlaneScaffold plane = null;
	private static int unique_id = 0;

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
			plane = new PlaneScaffold();
			if (vertices.size() == 5) {
				is_quad = true;
				num_textures = 1;
				texture_data = new Vector<byte[]>();
				texture_data.setSize(num_textures);
				textures = new Vector<Texture>();
				textures.setSize(num_textures);
			} else {
				num_textures = vertices.size() - 3;
				texture_data = new Vector<byte[]>();
				texture_data.setSize(num_textures);
				textures = new Vector<Texture>();
				textures.setSize(num_textures);
			}
			System.out.println("making new polygon Primitive");
			System.out.println("number of vertices: " + vertices.size());
		}

	}

	public Primitive(int _gl_type, List<Pellet> _vertices, float _line_width) {
		gl_type = _gl_type;
		vertices = _vertices;
		line_width = _line_width;
		num_textures = vertices.size() - 3;
	}

	public void initTextureArrays() {
		texture_url = new String[num_textures];
		local_textures = new String[num_textures];
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

	public PlaneScaffold getPlane() {
		return plane;
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
			glColor4f(.9f, .9f, .9f, 1);
			if (!Main.draw_textures) {
				glDisable(GL_TEXTURE_2D);
			} else if (textures_loaded) {
				glEnable(GL_TEXTURE_2D);
			} else {
				glDisable(GL_TEXTURE_2D);
				if (texture_count == num_textures) {
					try {
						for (int i = 0; i < num_textures; i++) {
							if (texture_data.get(i) == null)
								break;
							System.out.println(" texture set!!");
							textures.set(i, TextureLoader.getTexture(
									"PNG",
									new ByteArrayInputStream(texture_data
											.get(i))));
						}
						// texture_data.clear(); // dont clear these anymore
						// because we want to save them at the end
						textures_loaded = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		Vector2f[] tex_coords = null;
		if (is_quad) {
			tex_coords = new Vector2f[] { new Vector2f(0, 0),
					new Vector2f(1, 0), new Vector2f(1, 1), new Vector2f(0, 1) };
		} else {
			tex_coords = new Vector2f[] { new Vector2f(0, 0),
					new Vector2f(1, 0), new Vector2f(1, 1) };
		}

		if (Main.draw_polygons) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glEnable(GL_POLYGON_OFFSET_FILL);
			glPolygonOffset(1f, 1f);
			for (int h = 0; h < num_textures; h++) {
				if (textures_loaded && Main.draw_textures) {
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
			glDisable(GL_POLYGON_OFFSET_FILL);
		}

		// draw a border around the polygons
		if (Main.draw_lines) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			for (int h = 0; h < num_textures; h++) {
				glDisable(GL_TEXTURE_2D);
				glColor3f(.6f, .6f, .6f);
				glLineWidth(1);
				Pellet pellet;
				Vector3f vertex;
				glBegin(gl_type);
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

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}

	public void drawSolid() {
		if (gl_type == GL_LINES) {
			return;
		} else if (gl_type == GL_POLYGON) {
			glColor4f(.9f, .9f, .9f, 1);
			if (!Main.draw_textures) {
				glDisable(GL_TEXTURE_2D);
			} else if (textures_loaded) {
				glEnable(GL_TEXTURE_2D);
			} else {
				glDisable(GL_TEXTURE_2D);
				if (texture_count == num_textures) {
					try {
						for (int i = 0; i < num_textures; i++) {
							if (texture_data.get(i) == null)
								break;
							System.out.println(" texture set!!");
							textures.set(i, TextureLoader.getTexture(
									"PNG",
									new ByteArrayInputStream(texture_data
											.get(i))));
						}
						// texture_data.clear(); // dont clear these anymore
						// because we want to save them at the end
						textures_loaded = true;
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}

		}

		Vector2f[] tex_coords = null;
		if (is_quad) {
			tex_coords = new Vector2f[] { new Vector2f(0, 0),
					new Vector2f(1, 0), new Vector2f(1, 1), new Vector2f(0, 1) };
		} else {
			tex_coords = new Vector2f[] { new Vector2f(0, 0),
					new Vector2f(1, 0), new Vector2f(1, 1) };
		}

		if (Main.draw_polygons) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
			glEnable(GL_POLYGON_OFFSET_FILL);
			glPolygonOffset(1f, 1f);
			for (int h = 0; h < num_textures; h++) {
				if (textures_loaded && Main.draw_textures) {
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
			glDisable(GL_POLYGON_OFFSET_FILL);
		}

	}

	public void drawWireframe() {
		if (gl_type == GL_LINES) {
			glColor3f(0, 0, 0);
			glLineWidth(line_width);

			glBegin(gl_type);
			for (Pellet p : vertices) {
				Vector3f vertex = p.pos;
				glVertex3f(vertex.x, vertex.y, vertex.z);
			}
			glEnd();
			return;
		}

		Vector2f[] tex_coords = null;
		if (is_quad) {
			tex_coords = new Vector2f[] { new Vector2f(0, 0),
					new Vector2f(1, 0), new Vector2f(1, 1), new Vector2f(0, 1) };
		} else {
			tex_coords = new Vector2f[] { new Vector2f(0, 0),
					new Vector2f(1, 0), new Vector2f(1, 1) };
		}
		// draw a border around the polygons
		if (Main.draw_lines) {
			glPolygonMode(GL_FRONT_AND_BACK, GL_LINE);
			for (int h = 0; h < num_textures; h++) {
				glDisable(GL_TEXTURE_2D);
				glColor3f(.6f, .6f, .6f);
				glLineWidth(1);
				Pellet pellet;
				Vector3f vertex;
				glBegin(gl_type);
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

		glPolygonMode(GL_FRONT_AND_BACK, GL_FILL);
	}

	public void refreshTexture() {
		System.out.println("refreshing texture");
		texture_data = new Vector<byte[]>();
		texture_data.setSize(num_textures);
		textures = new Vector<Texture>();
		textures.setSize(num_textures);
		texture_url = null;
		textures_loaded = false;
		texture_count = 0;
		startDownloadingTexture();
	}

	public synchronized void startDownloadingTexture() {
		if (texture_url == null) {
			texture_url = new String[num_textures];
		}

		local_textures = new String[num_textures];

		for (int i = 0; i < num_textures; i++) {
			local_textures[i] = "tex_" + Main.server.session_id + "_"
					+ unique_id + "_" + i + ".png";
			unique_id++;
		}

		if (Main.server.texture_server == null) {
			// TODO shit this only works for the first quad in a thing right now
			// System.out.println("making local texture");
			//TextureMaker.makeTexture(this);
		} else {

			for (int i = 0; i < num_textures; i++) {

				if (is_quad)
					texture_url[i] = "quad.png?&v=";
				else
					texture_url[i] = "texture.png?&v=";

				// triangle fan going on here
				Pellet p = vertices.get(0);
				Vector3f v = p.pos;
				texture_url[i] += v.x + "," + v.y + "," + v.z + ",";

				for (int j = i + 1; j < i + (is_quad ? 4 : 3); j++) {
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

			texture_count = 0;
			for (int i = 0; i < num_textures; i++) {
				final int f_i = i;
				System.out.println("fetching texture from remote server"
						+ texture_url[f_i]);
				final String final_url_string = Main.server.texture_server
						+ texture_url[f_i];
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

		if (norm.length() == 0) {
			return Float.MAX_VALUE;
		}

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

	public void setPlane(PlaneScaffold pl) {
		plane.a = pl.a;
		plane.b = pl.b;
		plane.c = pl.c;
		plane.d = pl.d;
	}

	public static void addBackDeletedPrimitive(Primitive primitive) {

		Main.geometry.add(primitive);
	}

	@Override
	public String toJSONString() {
		JSONStringer s = new JSONStringer();
		try {
			s.object();
			s.key("type");
			s.value("primitive");
			s.key("is_polygon");
			s.value(isPolygon());
			s.key("vertices");
			s.array();
			for (Pellet p : vertices) {
				s.value(Main.all_pellets_in_world.indexOf(p));
			}
			s.endArray();
			s.key("vertex_objs");
			s.array();
			for (Pellet p : vertices) {
				s.value(p);
			}
			s.endArray();
			s.key("texture_url");
			s.value(texture_url);
			s.key("local_textures");
			s.value(local_textures);
			s.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public static void loadFromJSONv2(JSONObject obj) throws JSONException {
		JSONArray json_verts = obj.getJSONArray("vertices");
		List<Pellet> vertices = new LinkedList<Pellet>();
		for (int i = 0; i < json_verts.length(); i++) {
			vertices.add(Main.all_pellets_in_world.get(json_verts.getInt(i)));
		}
		Primitive p = new Primitive(GL_POLYGON, vertices);
		// p.startDownloadingTexture();

		Main.geometry.add(p);
	}

	public static void loadFromJSONv3(JSONObject obj) throws JSONException {
		JSONArray json_verts = obj.getJSONArray("vertex_objs");
		List<Pellet> vertices = new LinkedList<Pellet>();
		for (int i = 0; i < json_verts.length(); i++) {
			vertices.add(Pellet.loadFromJSON(json_verts.getJSONObject(i)));
		}
		Primitive p = new Primitive(GL_POLYGON, vertices);
		// p.startDownloadingTexture();

		Main.geometry.add(p);
	}

	public static void loadFromJSONv4(JSONObject obj) throws JSONException {
		JSONArray json_verts = obj.getJSONArray("vertex_objs");
		List<Pellet> vertices = new LinkedList<Pellet>();
		for (int i = 0; i < json_verts.length(); i++) {
			vertices.add(Pellet.loadFromJSON(json_verts.getJSONObject(i)));
		}
		Primitive p = new Primitive(GL_POLYGON, vertices);
		p.initTextureArrays();
		p.fitPlaneWithVertices();
		for (int i = 0; i < p.num_textures; i++) {
			p.local_textures[i] = obj.getJSONArray("local_textures").getString(
					i);
		}

		Main.geometry.add(p);
	}

	private void fitPlaneWithVertices() {
		List<Vector3f> v = new LinkedList<Vector3f>();
		for (Pellet p : vertices) {
			v.add(p.pos);
		}
		setPlane(new PlaneScaffold(v));
	}
}
