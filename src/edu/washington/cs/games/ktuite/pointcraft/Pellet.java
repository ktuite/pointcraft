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

		if (Main.launch_effect != null)
			Main.launch_effect.playAsSoundEffect(1.0f, 1.0f, false);
	}

	public void update() {
		// constructing means the pellet has triggered something to be built at
		// its sticking location
		if (!constructing) {
			// not constructing means the pellet is still traveling through
			// space

			// move the pellet
			Vector3f.add(pos, vel, pos);

			// if it's too old, kill it
			if (Main.timer.getTime() - birthday > 5) {
				alive = false;
			} else {

				// if it's not dead yet, see if this pellet was shot at an
				// existing pellet
				Pellet neighbor_pellet = queryOtherPellets();
				if (neighbor_pellet != null) {
					alive = false;
				} else {
					// if it's not dead yet and also didn't hit a neighboring
					// pellet, look for nearby points in model
					int neighbors = LibPointCloud.queryKdTree(pos.x, pos.y,
							pos.z, radius);

					// is it near some points?!
					if (neighbors > 0) {
						constructing = true;
						Main.attach_effect.playAsSoundEffect(1.0f, 1.0f, false);
					}
				}
			}
		} else {
			// the pellet has stuck... here we just give it a nice growing
			// bubble animation
			if (radius < max_radius) {
				radius *= 1.1;
			}
		}
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
				if (dist.length() < radius * 2) {
					System.out.println("yes, i hit another pellet");
					return pellet;
				}
			}
		}
		return null;
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
