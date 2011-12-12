package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.util.List;
import java.util.Stack;

import org.lwjgl.util.vector.Vector3f;

/* these primitives built out of pellets...
 * keep a list of pellets and then draw lines or polygons between them.
 */
public class Scaffold {
	public List<Pellet> pellets;
	public List<Pellet> intersection_pellets; 
	
	public Scaffold(){
		pellets = new Stack<Pellet>();
	}
	
	public float distanceToPoint(Vector3f pos) {
		return -100;
	}
	
	public Vector3f closestPoint(Vector3f pos) {
		return null;
	}
	
	public void draw() {
		// to be overwritten
	}
	
	public void addNewPellet(Pellet p){
		
	}
	
	/*
	private int gl_type;
	private List<Vector3f> vertices;
	private float line_width = 5f;
	public Vector3f pt_1;
	public Vector3f pt_2;
	public float a, b, c, d;

	public Scaffold(int _gl_type, List<Vector3f> _vertices) {
		gl_type = _gl_type;
		vertices = _vertices;
	}

	public Scaffold(int _gl_type, List<Vector3f> _vertices,
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

	public boolean isLine() {
		if (pt_1 != null && pt_2 != null)
			return true;
		else
			return false;
	}

	public boolean isPlane() {
		if (a == 0 && b == 0 && c == 0 && d == 0)
			return false;
		else
			return true;
	}

	public void setLine(Vector3f a, Vector3f b) {
		pt_1 = new Vector3f(a);
		pt_2 = new Vector3f(b);
	}

	public void setPlane(float _a, float _b, float _c, float _d) {
		a = _a;
		b = _b;
		c = _c;
		d = _d;
	}

	public void draw() {
		if (gl_type == GL_LINES) {
			glColor4f(0f, .1f, .3f, .6f);
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

	public float distanceToPoint(Vector3f pos) {
		float dist = Float.MAX_VALUE;
		if (isLine()) {
			// OH GOD THIS LOOKS SO SLOW
			// TODO: make faster
			Vector3f temp = new Vector3f();
			Vector3f sub1 = new Vector3f();
			Vector3f sub2 = new Vector3f();
			Vector3f sub3 = new Vector3f();
			Vector3f.sub(pos, pt_1, sub1);
			Vector3f.sub(pos, pt_2, sub2);
			Vector3f.sub(pt_2, pt_1, sub3);
			Vector3f.cross(sub1, sub2, temp);
			dist = temp.length() / sub3.length();
		} else if (isPlane()) {
			dist = (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math
					.sqrt(a * a + b * b + d * d));
		}
		// System.out.println("distancE: " + dist);
		return Math.abs(dist);
	}

	public float distanceToPlane(Vector3f pos) {
		return (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math.sqrt(a
				* a + b * b + c * c));
	}

	public Vector3f closestPoint(Vector3f pos) {
		Vector3f pt = new Vector3f();
		if (isLine()) {
			Vector3f line = new Vector3f();
			Vector3f.sub(pt_2, pt_1, line);
			line.normalise();

			Vector3f diag = new Vector3f();
			Vector3f.sub(pos, pt_1, diag);

			float dot = Vector3f.dot(line, diag);
			Vector3f.add(pt_1, (Vector3f) line.scale(dot), pt);
		} else if (isPlane()) {
			Vector3f norm = new Vector3f(a, b, c);
			norm.normalise();
			norm.scale(distanceToPlane(pos));
			Vector3f.sub(pos, norm, pt);
		}
		return pt;
	}

	public Vector3f checkForIntersectionLineWithPlane(Vector3f p1, Vector3f p2) {
		Vector3f i = null;

		if (isPlane()) {
			float u_denom = a * (p1.x - p2.x) + b * (p1.y - p2.y) + c
					* (p1.z - p2.z);
			if (u_denom != 0) {
				float u_num = a * p1.x + b * p1.y + c * p1.z + d;
				float u = u_num / u_denom;
				if (u > 0 && u < 1) {
					i = new Vector3f();
					i.x = p1.x + u * (p2.x - p1.x);
					i.y = p1.y + u * (p2.y - p1.y);
					i.z = p1.z + u * (p2.z - p1.z);
				}
			}
		}

		return i;
	}

	public Vector3f checkForIntersectionPlaneWithLine(float a, float b,
			float c, float d) {
		Vector3f i = null;

		if (isLine()) {
			float u_denom = a * (pt_1.x - pt_2.x) + b * (pt_1.y - pt_2.y) + c
					* (pt_1.z - pt_2.z);
			if (u_denom != 0) {
				float u_num = a * pt_1.x + b * pt_1.y + c * pt_1.z + d;
				float u = u_num / u_denom;
				if (u > 0 && u < 1) {
					i = new Vector3f();
					i.x = pt_1.x + u * (pt_2.x - pt_1.x);
					i.y = pt_1.y + u * (pt_2.y - pt_1.y);
					i.z = pt_1.z + u * (pt_2.z - pt_1.z);
				}
			}
		}

		return i;
	}
*/
}
