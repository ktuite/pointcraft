package edu.washington.cs.games.ktuite.pointcraft;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

public class PolygonPellet extends Pellet {

	public static List<PolygonPellet> current_cycle = new LinkedList<PolygonPellet>();
	
	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public PolygonPellet(List<Pellet> _pellets) {
		super(_pellets);
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
					current_cycle.add((PolygonPellet) neighbor_pellet);
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

	public void makeLine() {
		// make a line between 2 pellets
		List<Pellet> last_two = new LinkedList<Pellet>();
		last_two.add(current_cycle.get(current_cycle.size() - 2));
		last_two.add(current_cycle.get(current_cycle.size() - 1));
		Main.geometry.add(new Primitive(GL_LINES, last_two));
	}

	public void makePolygon() {
		// make the polygon
		List<Pellet> cycle = new LinkedList<Pellet>();
		for (PolygonPellet p : current_cycle) {
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
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(.9f, .1f, .4f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}
}
