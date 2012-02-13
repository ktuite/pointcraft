package edu.washington.cs.games.ktuite.pointcraft.tools;


import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.ActionTracker;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import static org.lwjgl.opengl.GL11.*;

public class ScaffoldPellet extends Pellet {

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public ScaffoldPellet() {
		super();
		pellet_type = Main.GunMode.PELLET;
	}

	// these constructors are for making plane/line intersection points
	public ScaffoldPellet(LinePellet p) {
		super();
		alive = true;
		constructing = true;
		pos.set(p.pos);
		radius = p.radius;
		max_radius = p.max_radius;
		pellet_type = p.pellet_type;
	}

	public ScaffoldPellet(PlanePellet p) {
		super();
		alive = true;
		constructing = true;
		pos.set(p.pos);
		radius = p.radius;
		max_radius = p.max_radius;
		pellet_type = p.pellet_type;
	}

	@Override
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
				// if the pellet is not dead yet, see if it intersected anything

				// did it hit another pellet?
				Pellet neighbor_pellet = queryOtherPellets();

				// did it hit a line or plane?
				Vector3f closest_point = queryScaffoldGeometry();

				if (neighbor_pellet != null) {
					System.out.println("pellet stuck to another pellet");
					pos.set(neighbor_pellet.pos);
					alive = false;
				} else if (closest_point != null) {
					System.out.println("pellet stuck to some geometry");
					constructing = true;
					pos.set(closest_point);
				} else if (Main.draw_points) {
					// it didn't hit some existing geometry or pellet
					// so check the point cloud
					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

					// is it near some points?!
					if (neighbors > 0) {
						snapToCenterOfPoints();
						constructing = true;
						setInPlace();
					}
				}
				
				if (constructing == true){
					ActionTracker.newScaffoldPellet(this);
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

	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.2f, .7f, .7f, alpha);
			drawSphere(radius);
		} else {
			glColor4f(.2f, .7f, .7f, 1f);
			drawSphere(radius);
		}
	}
}
