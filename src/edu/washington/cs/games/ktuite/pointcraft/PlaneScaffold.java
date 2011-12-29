package edu.washington.cs.games.ktuite.pointcraft;

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

public class PlaneScaffold extends Scaffold {

	private float line_width = 1f;
	public float a, b, c, d;
	private Vector3f center;
	private float plane_extent;
	private List<Vector3f> grid_vertices;

	public PlaneScaffold() {
		super();
		a = 0;
		b = 0;
		c = 0;
		d = 0;
	}
	
	public boolean isReady(){
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
			if (dist_to_center.length() > plane_extent * 1.2)
				return dist;

			if (isReady()) {
				dist = (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math
						.sqrt(a * a + b * b + c * c));
			}
		}
		
		return Math.abs(dist);
	}

	public Vector3f closestPoint(Vector3f pos) {
		Vector3f pt = new Vector3f();
		Vector3f norm = new Vector3f(a, b, c);
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
		norm.normalise();

		a = norm.x;
		b = norm.y;
		c = norm.z;
		d = -1
				* (a * pellets.get(0).pos.x + b * pellets.get(0).pos.y + c
						* pellets.get(0).pos.z);

		float pts[] = new float[12];
		plane_extent = findPlaneExtent();

		center = findPlaneCenter();

		if (Math.abs(a) > Math.abs(b) && Math.abs(a) > Math.abs(c)) {
			// set y and z
			pts[0 * 3 + 1] = 1 * plane_extent + center.y;
			pts[0 * 3 + 2] = 1 * plane_extent + center.z;
			pts[0 * 3 + 0] = -1 * (pts[0 * 3 + 1] * b + pts[0 * 3 + 2] * c + d)
					/ a;

			pts[1 * 3 + 1] = 1 * plane_extent + center.y;
			pts[1 * 3 + 2] = -1 * plane_extent + center.z;
			pts[1 * 3 + 0] = -1 * (pts[1 * 3 + 1] * b + pts[1 * 3 + 2] * c + d)
					/ a;

			pts[2 * 3 + 1] = -1 * plane_extent + center.y;
			pts[2 * 3 + 2] = -1 * plane_extent + center.z;
			pts[2 * 3 + 0] = -1 * (pts[2 * 3 + 1] * b + pts[2 * 3 + 2] * c + d)
					/ a;

			pts[3 * 3 + 1] = -1 * plane_extent + center.y;
			pts[3 * 3 + 2] = 1 * plane_extent + center.z;
			pts[3 * 3 + 0] = -1 * (pts[3 * 3 + 1] * b + pts[3 * 3 + 2] * c + d)
					/ a;
		} else if (Math.abs(b) > Math.abs(a) && Math.abs(b) > Math.abs(c)) {
			// horizontal plane! set x and z
			pts[0 * 3 + 0] = 1 * plane_extent + center.x;
			pts[0 * 3 + 2] = 1 * plane_extent + center.z;
			pts[0 * 3 + 1] = -1 * (pts[0 * 3 + 0] * a + pts[0 * 3 + 2] * c + d)
					/ b;

			pts[1 * 3 + 0] = 1 * plane_extent + center.x;
			pts[1 * 3 + 2] = -1 * plane_extent + center.z;
			pts[1 * 3 + 1] = -1 * (pts[1 * 3 + 0] * a + pts[1 * 3 + 2] * c + d)
					/ b;

			pts[2 * 3 + 0] = -1 * plane_extent + center.x;
			pts[2 * 3 + 2] = -1 * plane_extent + center.z;
			pts[2 * 3 + 1] = -1 * (pts[2 * 3 + 0] * a + pts[2 * 3 + 2] * c + d)
					/ b;

			pts[3 * 3 + 0] = -1 * plane_extent + center.x;
			pts[3 * 3 + 2] = 1 * plane_extent + center.z;
			pts[3 * 3 + 1] = -1 * (pts[3 * 3 + 0] * a + pts[3 * 3 + 2] * c + d)
					/ b;
		} else {
			// set x and y
			pts[0 * 3 + 0] = 1 * plane_extent + center.x;
			pts[0 * 3 + 1] = 1 * plane_extent + center.y;
			pts[0 * 3 + 2] = -1 * (pts[0 * 3 + 0] * a + pts[0 * 3 + 1] * b + d)
					/ c;

			pts[1 * 3 + 0] = 1 * plane_extent + center.x;
			pts[1 * 3 + 1] = -1 * plane_extent + center.y;
			pts[1 * 3 + 2] = -1 * (pts[1 * 3 + 0] * a + pts[1 * 3 + 1] * b + d)
					/ c;

			pts[2 * 3 + 0] = -1 * plane_extent + center.x;
			pts[2 * 3 + 1] = -1 * plane_extent + center.y;
			pts[2 * 3 + 2] = -1 * (pts[2 * 3 + 0] * a + pts[2 * 3 + 1] * b + d)
					/ c;

			pts[3 * 3 + 0] = -1 * plane_extent + center.x;
			pts[3 * 3 + 1] = 1 * plane_extent + center.y;
			pts[3 * 3 + 2] = -1 * (pts[3 * 3 + 0] * a + pts[3 * 3 + 1] * b + d)
					/ c;
		}

		grid_vertices = new LinkedList<Vector3f>();

		float grid = 40;
		for (int i = 0; i <= grid; i++) {
			Vector3f begin = new Vector3f();
			Vector3f end = new Vector3f();
			begin.x = pts[0 * 3 + 0] * i / grid + pts[1 * 3 + 0]
					* (1 - i / grid);
			begin.y = pts[0 * 3 + 1] * i / grid + pts[1 * 3 + 1]
					* (1 - i / grid);
			begin.z = pts[0 * 3 + 2] * i / grid + pts[1 * 3 + 2]
					* (1 - i / grid);
			end.x = pts[3 * 3 + 0] * i / grid + pts[2 * 3 + 0] * (1 - i / grid);
			end.y = pts[3 * 3 + 1] * i / grid + pts[2 * 3 + 1] * (1 - i / grid);
			end.z = pts[3 * 3 + 2] * i / grid + pts[2 * 3 + 2] * (1 - i / grid);
			grid_vertices.add(begin);
			grid_vertices.add(end);
		}
		for (int i = 0; i <= grid; i++) {
			Vector3f begin = new Vector3f();
			Vector3f end = new Vector3f();
			begin.x = pts[0 * 3 + 0] * i / grid + pts[3 * 3 + 0]
					* (1 - i / grid);
			begin.y = pts[0 * 3 + 1] * i / grid + pts[3 * 3 + 1]
					* (1 - i / grid);
			begin.z = pts[0 * 3 + 2] * i / grid + pts[3 * 3 + 2]
					* (1 - i / grid);
			end.x = pts[1 * 3 + 0] * i / grid + pts[2 * 3 + 0] * (1 - i / grid);
			end.y = pts[1 * 3 + 1] * i / grid + pts[2 * 3 + 1] * (1 - i / grid);
			end.z = pts[1 * 3 + 2] * i / grid + pts[2 * 3 + 2] * (1 - i / grid);
			grid_vertices.add(begin);
			grid_vertices.add(end);
		}

		//checkForIntersections();
	}

	public void checkForIntersections() {
		if (pellets.size() < 3)
			return;
		
		for (Scaffold geom : Main.geometry_v) {
			if (geom instanceof LineScaffold) {
				Vector3f intersect = ((LineScaffold) geom)
						.checkForIntersectionPlaneWithLine(a,b,c,d);
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
				
				Vector3f dist = new Vector3f();
				Vector3f.sub(i, center, dist);
				if (dist.length() > plane_extent)
					return null;
			}
		}

		return i;
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
	
	public void removeLastPointAndRefit(){
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
			for (Pellet p : pellets){
				s.value(Main.all_pellets_in_world.indexOf(p));
			}
			s.endArray();
			s.key("pellet_objs");
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
	
	public static void loadFromJSONv2(JSONObject obj) throws JSONException {
		PlaneScaffold plane = new PlaneScaffold();
		
		JSONArray json_verts = obj.getJSONArray("pellets");
		for (int i = 0; i < json_verts.length(); i++){
			plane.pellets.add(Main.all_pellets_in_world.get(json_verts.getInt(i)));
		}
		plane.fitPlane();
		Main.geometry_v.add(plane);
	}
	
	public static void loadFromJSONv3(JSONObject obj) throws JSONException {
		PlaneScaffold plane = new PlaneScaffold();
		
		JSONArray json_verts = obj.getJSONArray("pellet_objs");
		for (int i = 0; i < json_verts.length(); i++){
			plane.pellets.add(Pellet.loadFromJSON(json_verts.getJSONObject(i)));
		}
		plane.fitPlane();
		Main.geometry_v.add(plane);
	}
}
