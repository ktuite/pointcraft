package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;


/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class Primitive {
	private int gl_type;
	private List<Vector3f> vertices;

	public Primitive(int _gl_type, List<Vector3f> _vertices) {
		gl_type = _gl_type;
		vertices = _vertices;
	}

	public void draw() {
		if (gl_type == GL_LINES) {
			glColor3f(0, 0, 0);
			glLineWidth(5f);
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
