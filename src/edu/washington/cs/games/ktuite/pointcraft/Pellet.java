package edu.washington.cs.games.ktuite.pointcraft;

import java.nio.DoubleBuffer;
import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

public class Pellet {

	public Vector3f pos;
	public Vector3f vel;
	public Sphere sphere;
	public float radius;
	public float max_radius;
	public boolean alive;
	public boolean constructing;
	public float birthday;
	protected List<Pellet> main_pellets;

	public static boolean CONNECT_TO_PREVIOUS = true;
	//public static List<Pellet> current_cycle = new LinkedList<Pellet>();

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
		radius = .0005f * Main.world_scale;
		max_radius = radius * 1.5f;
		birthday = Main.timer.getTime();
		alive = true;
		constructing = false;
		main_pellets = _pellets;

		//if (Main.launch_effect != null)
		//	Main.launch_effect.playAsSoundEffect(1.0f, 1.0f, false);
	}

	public void finalize(){
		System.out.println("deletng this pellet");
	}
	
	public void update() {
		// meant to be overwritten
		System.out.println("the wrong update function is getting called");
	}
	
	protected void snapToCenterOfPoints(){
		DoubleBuffer center_of_points = LibPointCloud.queryKdTreeGetCenter(pos.x, pos.y, pos.z, radius).getByteBuffer(0, 3 * 8).asDoubleBuffer();
		pos.x = (float) center_of_points.get(0);
		pos.y = (float) center_of_points.get(1);
		pos.z = (float) center_of_points.get(2);
	}

	public Pellet queryOtherPellets() {
		for (Pellet pellet : main_pellets) {
			if (pellet != this) {
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
	
	public Vector3f queryScaffoldGeometry(){
		// TODO: make this return the actual point of intersection
		Vector3f closest_point = null;
		for (PrimitiveVertex geom : Main.geometry_v){
			if (radius > geom.distanceToPoint(pos)){
				closest_point = geom.closestPoint(pos);
				break;
			}
		}
		return closest_point;
	}
	
	public void draw() {
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
}
