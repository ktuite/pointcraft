package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.nio.DoubleBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

public class LineScaffold extends Scaffold {

	private Vector3f pt_1;
	private Vector3f pt_2;
	private float line_width = 3f;

	public LineScaffold() {
		super();
	}

	public void add(Pellet p) {
		pellets.add(p);
	}

	public void fitLine() {

		int n = pellets.size();
		if (n < 2)
			return;

		if (pt_1 == null)
			pt_1 = new Vector3f();
		if (pt_2 == null)
			pt_2 = new Vector3f();

		System.out.println("fitting line");

		DoubleBuffer line_points = BufferUtils.createDoubleBuffer(n * 3);
		for (Pellet pellet : pellets) {
			line_points.put(pellet.pos.x);
			line_points.put(pellet.pos.y);
			line_points.put(pellet.pos.z);
		}

		float f = findLineExtent();

		Vector3f line_direction = new Vector3f();
		Vector3f.sub(pellets.get(0).pos, pellets.get(1).pos, line_direction);
		line_direction.normalise();

		Vector3f center = new Vector3f();
		for (Pellet pellet : pellets) {
			Vector3f.add(center, pellet.pos, center);
		}
		center.scale(1f / n);

		line_direction.scale(f);
		Vector3f.add(center, line_direction, center);
		pt_1.set(center);
		line_direction.scale(-2);
		Vector3f.add(center, line_direction, center);
		pt_2.set(center);

		// checkForIntersections(line_pellets.get(0), line_pellets.get(1));
	}

	public void addNewPellet(Pellet p) {
		if (pellets.contains(p)) {
			System.out.println("the line already contains this pellet");
		} else {
			pellets.add(p);
		}
		fitLine();
	}

	private float findLineExtent() {
		float max_distance = 0;
		Vector3f dist = new Vector3f();
		for (Pellet a : pellets) {
			for (Pellet b : pellets) {
				Vector3f.sub(a.pos, b.pos, dist);
				float d = dist.length();
				if (d > max_distance)
					max_distance = d;
			}
		}
		return max_distance;
	}

	public Vector3f closestPoint(Vector3f pos) {
		if (pt_1 == null || pt_2 == null)
			return super.closestPoint(pos);

		Vector3f pt = new Vector3f();

		Vector3f line = new Vector3f();
		Vector3f.sub(pt_2, pt_1, line);
		line.normalise();

		Vector3f diag = new Vector3f();
		Vector3f.sub(pos, pt_1, diag);

		float dot = Vector3f.dot(line, diag);
		Vector3f.add(pt_1, (Vector3f) line.scale(dot), pt);

		return pt;
	}

	public float distanceToPoint(Vector3f pos) {
		if (pt_1 == null || pt_2 == null)
			return super.distanceToPoint(pos);

		float dist = Float.MAX_VALUE;

		Vector3f temp = new Vector3f();
		Vector3f sub1 = new Vector3f();
		Vector3f sub2 = new Vector3f();
		Vector3f sub3 = new Vector3f();
		Vector3f.sub(pos, pt_1, sub1);
		Vector3f.sub(pos, pt_2, sub2);
		Vector3f.sub(pt_2, pt_1, sub3);
		Vector3f.cross(sub1, sub2, temp);
		dist = temp.length() / sub3.length();

		// System.out.println("distancE: " + dist);
		return Math.abs(dist);
	}

	public void draw() {
		if (pt_1 != null && pt_2 != null) {
			glColor4f(0f, .1f, .3f, .6f);
			glLineWidth(line_width);

			glBegin(GL_LINES);
			glVertex3f(pt_1.x, pt_1.y, pt_1.z);
			glVertex3f(pt_2.x, pt_2.y, pt_2.z);
			glEnd();
		}
	}

}
