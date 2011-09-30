package edu.washington.cs.games.ktuite.pointcraft;

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
	private List<Pellet> main_pellets;

	public static boolean CONNECT_TO_PREVIOUS = true;
	public static List<Pellet> current_cycle = new LinkedList<Pellet>();

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
		radius = .0005f;
		max_radius = radius * 1.5f;
		birthday = Main.timer.getTime();
		alive = true;
		constructing = false;
		main_pellets = _pellets;

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
					current_cycle.add(neighbor_pellet);
					if (current_cycle.size() > 1)
						makeLine();

					if (current_cycle.size() > 2
							&& current_cycle.get(0) == current_cycle
									.get(current_cycle.size() - 1))
						makePolygon();

				} else {
					// if it's not dead yet and also didn't hit a neighboring
					// pellet, look for nearby points in model
					int neighbors = LibPointCloud.queryKdTree(pos.x, pos.y,
							pos.z, radius);

					// is it near some points?!
					if (neighbors > 0) {
						constructing = true;
						Main.attach_effect.playAsSoundEffect(1.0f, 1.0f, false);

						if (CONNECT_TO_PREVIOUS)
							current_cycle.add(this);

						if (CONNECT_TO_PREVIOUS && current_cycle.size() > 1) {
							makeLine();
						}
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

	public void makeLine() {
		// make a line between 2 pellets
		/*
		List<Vector3f> last_two = new LinkedList<Vector3f>();
		last_two.add(current_cycle.get(current_cycle.size() - 2).pos);
		last_two.add(current_cycle.get(current_cycle.size() - 1).pos);
		Main.geometry.add(new Primitive(GL_LINES, last_two));
		*/
		List<Pellet> last_two = new LinkedList<Pellet>();
		last_two.add(current_cycle.get(current_cycle.size() - 2));
		last_two.add(current_cycle.get(current_cycle.size() - 1));
		Main.geometry.add(new Primitive(GL_LINES, last_two));
	}

	public void makePolygon() {
		// make the polygon
		List<Pellet> cycle = new LinkedList<Pellet>();
		for (Pellet p : current_cycle) {
			cycle.add(p);
		}
		cycle.add(this);
		Main.geometry.add(new Primitive(GL_POLYGON, cycle));

		current_cycle.clear();
	}
	
	public void draw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.9f, .1f, .4f, alpha);
			// glColor4f(.9f, .6f, .7f, alpha);
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(.9f, .1f, .4f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}
}
