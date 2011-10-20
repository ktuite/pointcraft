package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;


/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class Primitive {
	private int gl_type;
	private List<Pellet> vertices;

	public Primitive(int _gl_type, List<Pellet> _vertices) {
		gl_type = _gl_type;
		vertices = _vertices;
	}

	public boolean isPolygon(){
		if (gl_type == GL_POLYGON)
			return true;
		else
			return false;
	}
	
	public void draw() {
		if (gl_type == GL_LINES) {
			glColor3f(0, 0, 0);
			glLineWidth(5f);
		} else if (gl_type == GL_POLYGON) {
			glColor4f(.9f, .9f, 0, .5f);
		}

		glBegin(gl_type);
		for (Pellet pellet : vertices) {
			Vector3f vertex = pellet.pos;
			glVertex3f(vertex.x, vertex.y, vertex.z);
		}
		glEnd();
	}
	
	public String plyFace(){
		String s = vertices.size() - 2 + "";
		for (int i = 0; i < vertices.size() - 2; i++){
			Pellet pellet = vertices.get(i);
			s += " " + Main.pellets.indexOf(pellet);
		}
		s += "\n";
		System.out.println("PLYFACE:" + s);
		return s;
	}
	
	public void printTriangleVertices(){
		for (int i = 0; i < vertices.size() - 2; i++){
			Pellet pellet = vertices.get(i);
			System.out.println(pellet.pos.x + " " + pellet.pos.y + " " + pellet.pos.z);
		}
		System.out.println("");
	}
}
