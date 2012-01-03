package edu.washington.cs.games.ktuite.pointcraft;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

public class Pellet implements org.json.JSONString {

	static public int ID = 0;
	public Vector3f pos;
	public Vector3f vel;
	public transient Sphere sphere;
	public float radius;
	public float max_radius;
	public boolean alive;
	public boolean visible;
	public boolean hover;
	public int[] color = new int[3];
	public boolean constructing;
	public float birthday;
	protected List<Pellet> main_pellets;
	public int id;
	public Main.GunMode pellet_type;

	public static boolean CONNECT_TO_PREVIOUS = true;

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
		sphere = new Sphere();
	}

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public Pellet(List<Pellet> _pellets) {
		pos = new Vector3f();
		sphere = new Sphere();
		vel = new Vector3f();
		radius = .0005f * Main.world_scale * Main.pellet_scale;
		max_radius = radius * 1.5f;
		birthday = Main.timer.getTime();
		alive = true;
		visible = true;
		constructing = false;
		main_pellets = _pellets;
		id = ID;
		ID++;
		pellet_type = null;

		// if (Main.launch_effect != null)
		// Main.launch_effect.playAsSoundEffect(1.0f, 1.0f, false);
	}

	public String getType() {
		return pellet_type.toString();
	}

	public void finalize() {
		System.out.println("deletng pellet " + id);
	}

	public void delete() {
		alive = false;
	}

	public void update() {
		// meant to be overwritten
		System.out.println("the wrong update function is getting called");
	}

	public void setInPlace() {
		if (Main.attach_effect != null) {
			Main.attach_effect.playAsSoundEffect(1.0f, 1.0f, false);
		}
	}

	protected void snapToCenterOfPoints() {
		pos.set(PointStore.getCenter(pos.x, pos.y, pos.z, radius));
	}

	protected int queryKdTree(float x, float y, float z, float radius) {
		return PointStore.queryKdTree(x, y, z, radius);
	}

	public Pellet queryOtherPellets() {
		if (!Main.draw_pellets)
			return null;

		//for (Pellet pellet : main_pellets) {
		for (int i = main_pellets.size()-1; i >= 0; i--){
			Pellet pellet = main_pellets.get(i);
			if (pellet != this && pellet.visible) {
				Vector3f dist = new Vector3f();
				Vector3f.sub(pos, pellet.pos, dist);
				if (dist.length() < radius * 2.5) {
					System.out.println("yes, i hit another pellet");
					return pellet;
				}
			}
		}
		return null;
	}

	public Vector3f queryScaffoldGeometry() {
		if (!Main.draw_scaffolding)
			return null;

		Vector3f closest_point = null;
		for (Scaffold geom : Main.geometry_v) {
			if (radius > geom.distanceToPoint(pos)) {
				closest_point = geom.closestPoint(pos);
				break;
			}
		}
		return closest_point;
	}

	public Vector3f stickPelletToScaffolding() {
		Vector3f closest_point = null;
		for (Scaffold geom : Main.geometry_v) {
			if (radius > geom.distanceToPoint(pos)) {
				geom.addNewPellet(this);
				if (geom instanceof LineScaffold)
					ActionTracker.extendedLine(geom);
				else if (geom instanceof PlaneScaffold)
					ActionTracker.extendedPlane(geom);
				break;
			}
		}
		return closest_point;
	}

	public Scaffold getIntersectedScaffoldGeometry() {
		if (!Main.draw_scaffolding)
			return null;

		for (Scaffold geom : Main.geometry_v) {
			if (radius > geom.distanceToPoint(pos)) {
				return geom;
			}
		}

		return null;
	}

	public Primitive getIntersectedPolygon() {
		for (int i = 0; i < Main.geometry.size(); i++) {
			Primitive geom = Main.geometry.get(i);
			if (geom.isPolygon()) {
				if (radius > geom.distanceToPolygonPlane(pos)) {
					if (i == Main.picked_polygon)
						return geom;
				}
			}
		}
		return null;
	}

	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.2f, .2f, .2f, alpha);
			// glColor4f(.9f, .6f, .7f, alpha);
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(.3f, .3f, .3f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}

	public void draw() {
		if (hover) {
			glColor4f(1f, 1f, .3f, .5f);
			sphere.draw(radius, 32, 32);
		} else {
			coloredDraw();
		}
	}

	@Override
	public String toJSONString() {
		try {
			return "{" + JSONObject.quote("type") + ":"
					+ JSONObject.quote("pellet") + ","
					+ JSONObject.quote("pellet_type") + ":"
					+ JSONObject.quote(pellet_type.toString()) + ","
					+ JSONObject.quote("pos") + ":" + JSONVector3f(pos) + ","
					+ JSONObject.quote("world_index") + ":"
					+ JSONObject.numberToString(id) + ","
					+ JSONObject.quote("radius") + ":"
					+ JSONObject.doubleToString(radius) + ", " + "}";
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
	}

	public static JSONStringer JSONVector3f(Vector3f v) {
		if (v != null) {
			JSONStringer s = new JSONStringer();
			try {
				s.array();
				s.value(v.x);
				s.value(v.y);
				s.value(v.z);
				s.endArray();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return s;
		} else
			return null;
	}

	public static Pellet loadFromJSON(JSONObject obj) throws JSONException {
		String pellet_type = obj.getString("pellet_type");
		int world_idx = obj.getInt("world_index");
		Vector3f temp_pos = new Vector3f();
		temp_pos.x = (float) obj.getJSONArray("pos").getDouble(0);
		temp_pos.y = (float) obj.getJSONArray("pos").getDouble(1);
		temp_pos.z = (float) obj.getJSONArray("pos").getDouble(2);
		for (Pellet p : Main.all_pellets_in_world){
			if (p.id == world_idx && p.pos == temp_pos){
				System.out.println("found existing pellet");
				return p;
			}
		}
		
		Pellet p = null;
		if (pellet_type.contains("POLYGON"))
			p = new PolygonPellet(Main.all_pellets_in_world);
		else if (pellet_type.contains("VERTICAL_LINE"))
			p = new VerticalLinePellet(Main.all_pellets_in_world);
		else if (pellet_type.contains("LINE"))
			p = new LinePellet(Main.all_pellets_in_world);
		else if (pellet_type.contains("PLANE"))
			p = new PlanePellet(Main.all_pellets_in_world);
		
		else 
			p = new ScaffoldPellet(Main.all_pellets_in_world);
		
		p.radius = (float) obj.getDouble("radius");
		p.alive = true;
		p.constructing = true;
		p.visible = true;
		p.pos = new Vector3f();
		p.id = world_idx;
		p.pos.x = (float) obj.getJSONArray("pos").getDouble(0);
		p.pos.y = (float) obj.getJSONArray("pos").getDouble(1);
		p.pos.z = (float) obj.getJSONArray("pos").getDouble(2);
		Main.all_pellets_in_world.add(p);
		return p;
	}
}
