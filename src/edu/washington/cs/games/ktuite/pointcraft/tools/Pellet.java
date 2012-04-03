package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.ActionTracker;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PickerHelper;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.geometry.LineScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.PlaneScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;
import edu.washington.cs.games.ktuite.pointcraft.tools.ModelingGun.InteractionMode;
import static org.lwjgl.opengl.GL11.*;

public class Pellet implements org.json.JSONString {

	static public int ID = 0;
	public Vector3f pos;
	public Vector3f vel;
	public float radius;
	public float max_radius;
	public boolean alive;
	public boolean visible;
	public boolean hover;
	public float[] color = new float[3];
	public boolean constructing;
	public float birthday;
	public int id;
	public Main.GunMode pellet_type;
	public int ref_count = 0;
	private static Integer sphere_display_list;

	public static boolean CONNECT_TO_PREVIOUS = true;
	public static float default_radius = .0005f * Main.world_scale;

	private void writeObject(ObjectOutputStream oos) throws IOException {
		oos.defaultWriteObject();
	}

	private void readObject(ObjectInputStream ois)
			throws ClassNotFoundException, IOException {
		ois.defaultReadObject();
	}

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public Pellet() {
		pos = new Vector3f();
		vel = new Vector3f();
		radius = default_radius * Main.pellet_scale;
		max_radius = radius * 1.5f;
		birthday = Main.timer.getTime();
		alive = true;
		visible = true;
		constructing = false;
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
		if (ModelingGun.mode == InteractionMode.PELLET_GUN) {
			pos.set(PointStore.getCenter(pos.x, pos.y, pos.z, radius));
		}
	}

	public static void dimAllPellets() {
		for (Pellet p : Main.all_pellets_in_world) {
			p.hover = false;
		}
	}

	public static void illuminatePellet(int i) {
		if (i >= 0 && i < Main.all_pellets_in_world.size()) {
			Main.all_pellets_in_world.get(i).hover = true;
		}
	}

	protected int queryKdTree(float x, float y, float z, float radius) {
		return PointStore.getNumPointsInSphere(x, y, z, radius);
	}

	public Pellet queryOtherPellets() {
		if (!Main.draw_pellets)
			return null;

		for (int i = Main.all_pellets_in_world.size() - 1; i >= 0; i--) {
			Pellet pellet = Main.all_pellets_in_world.get(i);
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
					if (i == PickerHelper.picked_polygon)
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
			drawSphere(radius);
		} else {
			glColor4f(.3f, .3f, .3f, 1f);
			drawSphere(radius);
		}
	}

	public void draw() {
		if (hover) {
			glColor4f(1f, 1f, .3f, .5f);
			drawSphere(radius);
		} else {
			coloredDraw();
		}
	}

	public static void drawSphere(float radius) {
		glPushMatrix();
		glScalef(radius, radius, radius);
		glCallList(sphere_display_list);
		glPopMatrix();
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
		for (Pellet p : Main.all_pellets_in_world) {
			if (p.pos == temp_pos) {
				System.out.println("found existing pellet");
				return p;
			}
		}

		Pellet p = null;
		if (pellet_type.contains("POLYGON"))
			p = new PolygonPellet();
		else if (pellet_type.contains("VERTICAL_LINE"))
			p = new VerticalLinePellet();
		else if (pellet_type.contains("LINE"))
			p = new LinePellet();
		else if (pellet_type.contains("PLANE"))
			p = new PlanePellet();

		else
			p = new ScaffoldPellet();

		p.radius = default_radius / 2f;
		p.max_radius = p.radius;
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

	public boolean refCountZero() {
		return (ref_count == 0);
	}

	public static void initSphereDisplayList() {
		Sphere sphere = new Sphere();
		sphere_display_list = glGenLists(1);
		glNewList(sphere_display_list, GL_COMPILE);
		sphere.draw(1, 32, 32);
		glEndList();
	}
}
