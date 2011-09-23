package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;
import org.lwjgl.util.vector.Vector3f;

import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;

public class Geometry {

	public List<Vector3f> vertices;

	public Geometry() {
		vertices = new LinkedList<Vector3f>();
		for (int i = 0; i < 9; i++) {
			Vector3f v = new Vector3f();
			v.x = (float) (Math.random()-.5f);
			v.y = (float) (Math.random()-.5f);
			v.z = (float) (Math.random()-.5f);
			vertices.add(v);
		}
		System.out.println("new geometry: " + vertices.toString());
	}
	
	public Geometry(FloatBuffer buff, int n){
		vertices = new LinkedList<Vector3f>();
		for (int i = 0; i < n; i+=3){
			Vector3f v = new Vector3f();
			v.x = buff.get(i*3 + 0);
			v.y = buff.get(i*3 + 1);
			v.z = buff.get(i*3 + 2);
			vertices.add(v);
		}
	}

	public void draw() {
		glBegin(GL_TRIANGLES);
		for (int i = 0; i < vertices.size(); i += 1) {
			glColor3f(1f, 0f, 0f);
			glVertex3f(vertices.get(i).x, vertices.get(i).y, vertices.get(i).z);
		}
		glEnd();
	}
}
