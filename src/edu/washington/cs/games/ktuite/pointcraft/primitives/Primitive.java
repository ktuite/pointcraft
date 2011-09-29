package edu.washington.cs.games.ktuite.pointcraft.primitives;

import static org.lwjgl.opengl.GL11.*;
import java.util.List;

import edu.washington.cs.games.ktuite.pointcraft.Pellet;

/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class Primitive {
	private int gl_type;
	private List<Pellet> pellets;

	public Primitive(int _gl_type, List<Pellet> _pellets) {
		gl_type = _gl_type;
		pellets = _pellets;
	}

	public void draw() {
		if (gl_type == GL_LINE) {
			glColor3f(0, 0, 0);
		} else if (gl_type == GL_POLYGON) {
			glColor3f(.9f, .9f, 0);
		}

		glBegin(gl_type);
		for (Pellet pellet : pellets) {
			glVertex3f(pellet.pos.x, pellet.pos.y, pellet.pos.z);
		}
		glEnd();
	}
}
