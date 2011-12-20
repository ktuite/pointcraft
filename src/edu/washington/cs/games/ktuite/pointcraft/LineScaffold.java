package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.nio.DoubleBuffer;

import org.json.JSONException;
import org.json.JSONStringer;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

public class LineScaffold extends Scaffold {

	private Vector3f pt_1;
	private Vector3f pt_2;
	private float line_width = 3f;

	public LineScaffold() {
		super();
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
		
		if (line_direction.lengthSquared() == 0)
			return;
		
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


		//checkForIntersections();
	}

	public void checkForIntersections() {
		if (pellets.size() < 2)
			return;
		
		for (Scaffold geom : Main.geometry_v) {
			if (geom instanceof PlaneScaffold) {
				Vector3f intersect = ((PlaneScaffold) geom)
						.checkForIntersectionLineWithPlane(pt_1, pt_2);
				if (intersect != null) {
					LinePellet i = new LinePellet(Main.all_pellets_in_world);
					i.alive = true;
					i.constructing = true;
					i.is_intersection = true;
					i.pos.set(intersect);
					i.radius = pellets.get(0).radius;

					pellets.add(i);
					Main.new_pellets_to_add_to_world.add(i);
					ActionTracker.newLinePlaneIntersection(i);
				}
			}
		}
	}

	public Vector3f checkForIntersectionPlaneWithLine(float a, float b,
			float c, float d) {
		Vector3f i = null;
		if (pt_1 == null || pt_2 == null)
			return i;

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
		return i;
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

	public void nullifyLine() {
		pt_1 = null;
		pt_2 = null;
	}
	
	public void removeLastPointAndRefit(){
		pellets.pop();
		fitLine();
	}
	
	@Override
	public String toJSONString() {
		JSONStringer s = new JSONStringer();
		try {
			s.object();
			s.key("scaffold_type");
			s.value("line");
			s.key("pt_1");
			s.value(Pellet.JSONVector3f(pt_1));
			s.key("pt_2");
			s.value(Pellet.JSONVector3f(pt_2));
			s.key("pellets");
			s.array();
			for (Pellet p : pellets){
				s.value(p);
			}
			s.endArray();
			s.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
        return s.toString();
	}
}
