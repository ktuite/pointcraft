package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.io.InputStream;
import java.net.URL;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;

/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class PrimitiveVertex {
	private int gl_type;
	private List<Vector3f> vertices;
	private float line_width = 5f;

	public PrimitiveVertex(int _gl_type, List<Vector3f> _vertices) {
		gl_type = _gl_type;
		vertices = _vertices;
	}

	public PrimitiveVertex(int _gl_type, List<Vector3f> _vertices,
			float _line_width) {
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

	public void draw() {
		if (gl_type == GL_LINES) {
			glColor3f(0f, .1f, .3f);
			glLineWidth(line_width);
		} else if (gl_type == GL_POLYGON) {
			glColor4f(.9f, .9f, 0, .5f);
		}

		
		glBegin(gl_type);
		for (Vector3f vertex : vertices) {
			glVertex3f(vertex.x, vertex.y, vertex.z);
		}
		glEnd();
	}

}
