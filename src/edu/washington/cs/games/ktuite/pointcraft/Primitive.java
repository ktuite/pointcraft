package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;

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
	private byte[] textureData = null;
	private Texture texture = null;

	public Primitive(int _gl_type, List<Pellet> _vertices) {
		gl_type = _gl_type;
		vertices = _vertices;
		if (gl_type == GL_POLYGON) {
			startDownloadingTexture();
			System.out.println("making new Primitive");
		}
	}

	public Primitive(int _gl_type, List<Pellet> _vertices, float _line_width) {
		gl_type = _gl_type;
		vertices = _vertices;
		line_width = _line_width;
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

	public void draw() {
		if (gl_type == GL_LINES) {
			glColor3f(0, 0, 0);
			glLineWidth(line_width);
		} else if (gl_type == GL_POLYGON) {
			glColor4f(.9f, .9f, 0, .5f);
			if (gl_type == GL_LINES) {
				glColor3f(.5f, .5f, .5f);
				glLineWidth(line_width);
			} else if (gl_type == GL_POLYGON) {
				glColor4f(.9f, .9f, .9f, .5f);
				if (texture != null) {
					glEnable(GL_TEXTURE_2D);
					texture.bind();
				} else {
					glDisable(GL_TEXTURE_2D);
					if(textureData != null) {
						try {
							texture = TextureLoader.getTexture("JPEG", new ByteArrayInputStream(textureData));
							textureData = null;
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}

		Vector2f[] tex_coords = new Vector2f[] { new Vector2f(0, 0),
				new Vector2f(1, 0), new Vector2f(1, 1), new Vector2f(0, 1) };

		glBegin(gl_type);
		for (int i = 0; i < vertices.size() && i < tex_coords.length; i++) {
			Pellet pellet = vertices.get(i);
			Vector3f vertex = pellet.pos;
			glTexCoord2f(tex_coords[i].x, tex_coords[i].y);
			glVertex3f(vertex.x, vertex.y, vertex.z);
		}
		glEnd();
	}

	public void startDownloadingTexture() {
		String url_string = "http://mazagran.cs.washington.edu:9999/texture.png?&v=";
		for (Pellet p : vertices){
			Vector3f v = p.pos;
			url_string += v.x + "," + v.y + "," + v.z + ",";
		}
		url_string += "garbage,&w=128,&h=128";
		final String final_url_string = url_string;
		new Thread() {
			public void run() {

				try {
					URL url = new URL(final_url_string);
					InputStream is = url.openStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					byte[] bytes = new byte[4096];
					int n;
					while((n = is.read(bytes)) != -1) {
						baos.write(bytes, 0, n);
					}
					textureData = baos.toByteArray();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public String plyFace() {
		String s = vertices.size() - 2 + "";
		for (int i = 0; i < vertices.size() - 2; i++) {
			Pellet pellet = vertices.get(i);
			s += " " + Main.all_pellets_in_world.indexOf(pellet);
		}
		s += "\n";
		System.out.println("PLYFACE:" + s);
		return s;
	}

	public void printTriangleVertices() {
		for (int i = 0; i < vertices.size() - 2; i++) {
			Pellet pellet = vertices.get(i);
			System.out.println(pellet.pos.x + " " + pellet.pos.y + " "
					+ pellet.pos.z);
		}
		System.out.println("");
	}
}
