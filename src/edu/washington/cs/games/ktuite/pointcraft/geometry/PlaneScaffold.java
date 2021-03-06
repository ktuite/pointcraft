package edu.washington.cs.games.ktuite.pointcraft.geometry;

import static org.lwjgl.opengl.GL11.GL_LINES;
import static org.lwjgl.opengl.GL11.glBegin;
import static org.lwjgl.opengl.GL11.glColor4f;
import static org.lwjgl.opengl.GL11.glEnd;
import static org.lwjgl.opengl.GL11.glLineWidth;
import static org.lwjgl.opengl.GL11.glVertex3f;

import java.nio.DoubleBuffer;
import java.util.LinkedList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.ActionTracker;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.tools.LinePellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.Pellet;

public class PlaneScaffold extends Scaffold {

	private float line_width = 1f;
	public float a, b, c, d;
	public Vector3f center;
	public float plane_extent;
	private List<Vector3f> grid_vertices;
	private List<Vector3f> corner_vertices;

	public PlaneScaffold() {
		super();
		a = 0;
		b = 0;
		c = 0;
		d = 0;
	}

	public PlaneScaffold(List<Vector3f> v) {
		super();
		a = 0;
		b = 0;
		c = 0;
		d = 0;
		grid_vertices = v;
		fitPlane();
	}

	public boolean isReady() {
		if ((a == 0 && b == 0 && c == 0 && d == 0))
			return false;
		else
			return true;
	}

	public void addNewPellet(Pellet p) {
		if (pellets.contains(p)) {
			System.out.println("the line already contains this pellet");
		} else {
			System.out.println("A NEW THING ADDED TO THE PLANE!");
			pellets.add(p);
		}
		fitPlane();
	}

	public float distanceToPoint(Vector3f pos) {
		float dist = Float.MAX_VALUE;

		// check and see if pt is anywhere near displayed bounds of plane
		if (center != null) {
			Vector3f dist_to_center = new Vector3f();
			Vector3f.sub(pos, center, dist_to_center);
			if (outOfPlaneBounds(pos))
				return dist;

			if (isReady()) {
				dist = (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math
						.sqrt(a * a + b * b + c * c));
			}
		}

		return Math.abs(dist);
	}

	public float distanceToPointNoBounds(Vector3f pos) {
		float dist = Float.MAX_VALUE;

		if (isReady()) {
			dist = (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math
					.sqrt(a * a + b * b + c * c));
		}

		return Math.abs(dist);
	}

	public Vector3f closestPoint(Vector3f pos) {
		Vector3f pt = new Vector3f();
		Vector3f norm = new Vector3f(a, b, c);
		if (norm.length() == 0) {
			return null;
		}
		norm.normalise();
		norm.scale(signedDistanceToPlane(pos));
		Vector3f.sub(pos, norm, pt);
		return pt;
	}

	public float signedDistanceToPlane(Vector3f pos) {
		return (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math.sqrt(a
				* a + b * b + c * c));
	}

	public void fitPlane() {
		int n = pellets.size();
		if (n < 3)
			return;

		System.out.println("fitting plane...");

		DoubleBuffer plane_points = BufferUtils.createDoubleBuffer(n * 3);
		for (Pellet pellet : pellets) {
			plane_points.put(pellet.pos.x);
			plane_points.put(pellet.pos.y);
			plane_points.put(pellet.pos.z);
		}

		Vector3f leg_1 = new Vector3f();
		Vector3f.sub(pellets.get(0).pos, pellets.get(1).pos, leg_1);
		Vector3f leg_2 = new Vector3f();
		Vector3f.sub(pellets.get(0).pos, pellets.get(2).pos, leg_2);
		Vector3f norm = new Vector3f();
		Vector3f.cross(leg_1, leg_2, norm);
		if (norm.length() == 0) {
			return;
		}
		norm.normalise();

		a = norm.x;
		b = norm.y;
		c = norm.z;
		d = -1
				* (a * pellets.get(0).pos.x + b * pellets.get(0).pos.y + c
						* pellets.get(0).pos.z);

		// System.out.println("plane parameters: " + a + "," + b + "," + c + ","
		// + d);

		plane_extent = findPlaneExtent();

		center = findPlaneCenter();

		buildGrid();

		// checkForIntersections();
	}

	public void buildGrid() {
		float[] corner_points = computeCornerPoints();

		grid_vertices = new LinkedList<Vector3f>();

		float grid = 40;
		for (int i = 0; i <= grid; i++) {
			Vector3f begin = new Vector3f();
			Vector3f end = new Vector3f();
			begin.x = corner_points[0 * 3 + 0] * i / grid
					+ corner_points[1 * 3 + 0] * (1 - i / grid);
			begin.y = corner_points[0 * 3 + 1] * i / grid
					+ corner_points[1 * 3 + 1] * (1 - i / grid);
			begin.z = corner_points[0 * 3 + 2] * i / grid
					+ corner_points[1 * 3 + 2] * (1 - i / grid);
			end.x = corner_points[3 * 3 + 0] * i / grid
					+ corner_points[2 * 3 + 0] * (1 - i / grid);
			end.y = corner_points[3 * 3 + 1] * i / grid
					+ corner_points[2 * 3 + 1] * (1 - i / grid);
			end.z = corner_points[3 * 3 + 2] * i / grid
					+ corner_points[2 * 3 + 2] * (1 - i / grid);
			grid_vertices.add(begin);
			grid_vertices.add(end);
		}
		for (int i = 0; i <= grid; i++) {
			Vector3f begin = new Vector3f();
			Vector3f end = new Vector3f();
			begin.x = corner_points[0 * 3 + 0] * i / grid
					+ corner_points[3 * 3 + 0] * (1 - i / grid);
			begin.y = corner_points[0 * 3 + 1] * i / grid
					+ corner_points[3 * 3 + 1] * (1 - i / grid);
			begin.z = corner_points[0 * 3 + 2] * i / grid
					+ corner_points[3 * 3 + 2] * (1 - i / grid);
			end.x = corner_points[1 * 3 + 0] * i / grid
					+ corner_points[2 * 3 + 0] * (1 - i / grid);
			end.y = corner_points[1 * 3 + 1] * i / grid
					+ corner_points[2 * 3 + 1] * (1 - i / grid);
			end.z = corner_points[1 * 3 + 2] * i / grid
					+ corner_points[2 * 3 + 2] * (1 - i / grid);
			grid_vertices.add(begin);
			grid_vertices.add(end);
		}
	}

	public void checkForIntersections() {
		if (pellets.size() < 3)
			return;

		for (Scaffold geom : Main.geometry_v) {
			if (geom instanceof LineScaffold) {
				Vector3f intersect = ((LineScaffold) geom)
						.checkForIntersectionPlaneWithLine(a, b, c, d);
				if (intersect != null) {
					LinePellet i = new LinePellet();
					i.alive = true;
					i.constructing = true;
					i.is_intersection = true;
					i.pos.set(intersect);
					i.radius = pellets.get(0).radius;

					pellets.add(i);
					Main.new_pellets_to_add_to_world.add(i);
					ActionTracker.newLinePlaneIntersection(i);
				}
			} else if (geom instanceof PlaneScaffold) {
				// ((PlaneScaffold)
				// geom).checkForIntersectionPlaneWithPlane(this);
				// or perhaps switch the order:
				if (geom != this && geom.isReady()) {
					LineScaffold line = checkForIntersectionPlaneWithPlane((PlaneScaffold) geom);
					if (line != null) {
						Main.new_geometry_v_to_add.add(line);
					}
				}
			}
		}
	}

	public float[] computeCornerPoints() {
		float[] corner_points = new float[12];
		if (Math.abs(a) > Math.abs(b) && Math.abs(a) > Math.abs(c)) {
			// set y and z
			corner_points[0 * 3 + 1] = 1 * plane_extent + center.y;
			corner_points[0 * 3 + 2] = 1 * plane_extent + center.z;
			corner_points[0 * 3 + 0] = -1
					* (corner_points[0 * 3 + 1] * b + corner_points[0 * 3 + 2]
							* c + d) / a;

			corner_points[1 * 3 + 1] = 1 * plane_extent + center.y;
			corner_points[1 * 3 + 2] = -1 * plane_extent + center.z;
			corner_points[1 * 3 + 0] = -1
					* (corner_points[1 * 3 + 1] * b + corner_points[1 * 3 + 2]
							* c + d) / a;

			corner_points[2 * 3 + 1] = -1 * plane_extent + center.y;
			corner_points[2 * 3 + 2] = -1 * plane_extent + center.z;
			corner_points[2 * 3 + 0] = -1
					* (corner_points[2 * 3 + 1] * b + corner_points[2 * 3 + 2]
							* c + d) / a;

			corner_points[3 * 3 + 1] = -1 * plane_extent + center.y;
			corner_points[3 * 3 + 2] = 1 * plane_extent + center.z;
			corner_points[3 * 3 + 0] = -1
					* (corner_points[3 * 3 + 1] * b + corner_points[3 * 3 + 2]
							* c + d) / a;
		} else if (Math.abs(b) > Math.abs(a) && Math.abs(b) > Math.abs(c)) {
			// horizontal plane! set x and z
			corner_points[0 * 3 + 0] = 1 * plane_extent + center.x;
			corner_points[0 * 3 + 2] = 1 * plane_extent + center.z;
			corner_points[0 * 3 + 1] = -1
					* (corner_points[0 * 3 + 0] * a + corner_points[0 * 3 + 2]
							* c + d) / b;

			corner_points[1 * 3 + 0] = 1 * plane_extent + center.x;
			corner_points[1 * 3 + 2] = -1 * plane_extent + center.z;
			corner_points[1 * 3 + 1] = -1
					* (corner_points[1 * 3 + 0] * a + corner_points[1 * 3 + 2]
							* c + d) / b;

			corner_points[2 * 3 + 0] = -1 * plane_extent + center.x;
			corner_points[2 * 3 + 2] = -1 * plane_extent + center.z;
			corner_points[2 * 3 + 1] = -1
					* (corner_points[2 * 3 + 0] * a + corner_points[2 * 3 + 2]
							* c + d) / b;

			corner_points[3 * 3 + 0] = -1 * plane_extent + center.x;
			corner_points[3 * 3 + 2] = 1 * plane_extent + center.z;
			corner_points[3 * 3 + 1] = -1
					* (corner_points[3 * 3 + 0] * a + corner_points[3 * 3 + 2]
							* c + d) / b;
		} else {
			// set x and y
			corner_points[0 * 3 + 0] = 1 * plane_extent + center.x;
			corner_points[0 * 3 + 1] = 1 * plane_extent + center.y;
			corner_points[0 * 3 + 2] = -1
					* (corner_points[0 * 3 + 0] * a + corner_points[0 * 3 + 1]
							* b + d) / c;

			corner_points[1 * 3 + 0] = 1 * plane_extent + center.x;
			corner_points[1 * 3 + 1] = -1 * plane_extent + center.y;
			corner_points[1 * 3 + 2] = -1
					* (corner_points[1 * 3 + 0] * a + corner_points[1 * 3 + 1]
							* b + d) / c;

			corner_points[2 * 3 + 0] = -1 * plane_extent + center.x;
			corner_points[2 * 3 + 1] = -1 * plane_extent + center.y;
			corner_points[2 * 3 + 2] = -1
					* (corner_points[2 * 3 + 0] * a + corner_points[2 * 3 + 1]
							* b + d) / c;

			corner_points[3 * 3 + 0] = -1 * plane_extent + center.x;
			corner_points[3 * 3 + 1] = 1 * plane_extent + center.y;
			corner_points[3 * 3 + 2] = -1
					* (corner_points[3 * 3 + 0] * a + corner_points[3 * 3 + 1]
							* b + d) / c;
		}

		corner_vertices = new LinkedList<Vector3f>();
		for (int i = 0; i < 4; i++) {
			corner_vertices.add(new Vector3f(corner_points[i * 3 + 0],
					corner_points[i * 3 + 1], corner_points[i * 3 + 2]));
		}

		return corner_points;
	}

	public LineScaffold checkForIntersectionPlaneWithPlane(
			PlaneScaffold plane_to_intersect) {
		System.out.println("checking for interesction with plane, "
				+ plane_to_intersect.a + ", " + plane_to_intersect.b + ","
				+ plane_to_intersect.c);

		LineScaffold line = null;

		// this is the plane of interest
		// the passed-in plane is the plane to intersect
		Vector3f norm = new Vector3f(plane_to_intersect.a,
				plane_to_intersect.b, plane_to_intersect.c);
		float angle_1 = Vector3f.angle(Vector3f.sub(corner_vertices.get(0),
				corner_vertices.get(1), null), norm);
		float angle_2 = Vector3f.angle(Vector3f.sub(corner_vertices.get(0),
				corner_vertices.get(3), null), norm);
		Vector3f a1, a2, b1, b2;
		
		System.out.println("angles: " + angle_1 + ", " + angle_2);
		if (angle_1 > 1.57079)
			angle_1 = 3.14159f - angle_1;
		if (angle_2 > 1.57079)
			angle_2 = 3.14159f - angle_2;
		
		if (angle_1 > angle_2) {
			a1 = corner_vertices.get(0);// 0, 3
			a2 = corner_vertices.get(3);
			b1 = corner_vertices.get(1);
			b2 = corner_vertices.get(2);
		} else {
			a1 = corner_vertices.get(0);
			a2 = corner_vertices.get(1);
			b1 = corner_vertices.get(3);
			b2 = corner_vertices.get(2);
		}
		Vector3f i1 = plane_to_intersect
				.checkForIntersectionLineWithPlaneNoBounds(a1, a2);
		Vector3f i2 = plane_to_intersect
				.checkForIntersectionLineWithPlaneNoBounds(b1, b2);
		
		//System.out.println("i1 and i2: " + i1 + ", " + i2);
		
		if (i1 != null && i2 != null) {
			line = new LineScaffold();

			LinePellet p1 = new LinePellet();
			p1.alive = true;
			p1.constructing = true;
			p1.is_intersection = true;
			p1.pos.set(i1);
			p1.radius = Pellet.default_radius;
			pellets.add(p1);
			// Main.new_pellets_to_add_to_world.add(p1);

			LinePellet p2 = new LinePellet();
			p2.alive = true;
			p2.constructing = true;
			p2.is_intersection = true;
			p2.pos.set(i2);
			p2.radius = Pellet.default_radius;
			pellets.add(p2);
			// Main.new_pellets_to_add_to_world.add(p2);

			line.addNewPellet(p1);
			line.addNewPellet(p2);
			//line.fitLine();
		}

		return line;
	}

	private float findPlaneExtent() {
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

	private Vector3f findPlaneCenter() {
		Vector3f center = new Vector3f();
		for (Pellet p : pellets) {
			Vector3f.add(p.pos, center, center);
		}
		center.scale(1f / pellets.size());
		return center;
	}

	public Vector3f checkForIntersectionLineWithPlane(Vector3f p1, Vector3f p2) {
		Vector3f i = null;
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

				if (outOfPlaneBounds(i))
					return null;
			}
		}

		return i;
	}

	private boolean outOfPlaneBounds(Vector3f i) {
		Vector3f corner1 = corner_vertices.get(1);
		Vector3f corner2 = corner_vertices.get(2);
		Vector3f corner3 = corner_vertices.get(3);

		Vector3f line1 = Vector3f.sub(corner1, corner2, null);
		Vector3f diag1 = Vector3f.sub(i, corner2, null);
		float dot1 = Vector3f.dot(line1, diag1) / line1.lengthSquared();

		Vector3f line2 = Vector3f.sub(corner3, corner2, null);
		Vector3f diag2 = Vector3f.sub(i, corner2, null);
		float dot2 = Vector3f.dot(line2, diag2) / line2.lengthSquared();

		if (dot1 < 0 || dot1 > 1 || dot2 < 0 || dot2 > 1)
			return true;
		else
			return false;
	}

	public Vector3f checkForIntersectionLineWithPlaneNoBounds(Vector3f p1,
			Vector3f p2) {
		Vector3f i = null;
		float u_denom = a * (p1.x - p2.x) + b * (p1.y - p2.y) + c
				* (p1.z - p2.z);
		if (Math.abs(u_denom) > 0.001f) {
			System.out.println("u denom: " + u_denom);
			float u_num = a * p1.x + b * p1.y + c * p1.z + d;
			float u = u_num / u_denom;

			i = new Vector3f();
			i.x = p1.x + u * (p2.x - p1.x);
			i.y = p1.y + u * (p2.y - p1.y);
			i.z = p1.z + u * (p2.z - p1.z);

		}

		return i;
	}

	public float planeNormalDotVector(Vector3f v) {
		Vector3f norm = new Vector3f(a, b, c);
		norm.normalise();
		return Vector3f.dot(v, norm);
	}

	public void draw() {
		if (grid_vertices != null) {
			glColor4f(0f, .1f, .3f, .6f);
			glLineWidth(line_width);

			glBegin(GL_LINES);
			for (Vector3f v : grid_vertices) {
				glVertex3f(v.x, v.y, v.z);
			}
			glEnd();
		}
	}

	public void nullifyPlane() {
		a = 0;
		b = 0;
		c = 0;
		d = 0;
		grid_vertices = null;
	}

	public void removeLastPointAndRefit() {
		Main.all_dead_pellets_in_world.add(pellets.pop());
		fitPlane();
	}

	@Override
	public String toJSONString() {
		JSONStringer s = new JSONStringer();
		try {
			s.object();
			s.key("type");
			s.value("scaffold");
			s.key("scaffold_type");
			s.value("plane");
			s.key("plane_parameters");
			s.array();
			s.value(a);
			s.value(b);
			s.value(c);
			s.value(d);
			s.endArray();
			s.key("pellets");
			s.array();
			for (Pellet p : pellets) {
				s.value(Main.all_pellets_in_world.indexOf(p));
			}
			s.endArray();
			s.key("pellet_objs");
			s.array();
			for (Pellet p : pellets) {
				s.value(p);
			}
			s.endArray();
			s.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public static void loadFromJSONv2(JSONObject obj) throws JSONException {
		PlaneScaffold plane = new PlaneScaffold();

		JSONArray json_verts = obj.getJSONArray("pellets");
		for (int i = 0; i < json_verts.length(); i++) {
			plane.pellets.add(Main.all_pellets_in_world.get(json_verts
					.getInt(i)));
		}
		plane.fitPlane();
		Main.geometry_v.add(plane);
	}

	public static void loadFromJSONv3(JSONObject obj) throws JSONException {
		PlaneScaffold plane = new PlaneScaffold();

		JSONArray json_verts = obj.getJSONArray("pellet_objs");
		for (int i = 0; i < json_verts.length(); i++) {
			plane.pellets.add(Pellet.loadFromJSON(json_verts.getJSONObject(i)));
		}
		plane.fitPlane();
		Main.geometry_v.add(plane);
	}
}
