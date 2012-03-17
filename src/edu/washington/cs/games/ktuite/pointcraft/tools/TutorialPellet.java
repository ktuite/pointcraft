package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.LinkedList;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.ActionTracker;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import static org.lwjgl.opengl.GL11.*;

public class TutorialPellet extends Pellet {

	public static LinkedList<TutorialPellet> tutorial_pellets = new LinkedList<TutorialPellet>();
	public boolean hit = false;

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public TutorialPellet() {
		super();
		pellet_type = Main.GunMode.PELLET;
		color[0] = 1f;
		color[1] = .01f;
		color[2] = .15f;
	}

	public TutorialPellet(TutorialPellet p) {
		super();
		alive = true;
		constructing = true;
		pos.set(p.pos);
		radius = p.radius;
		max_radius = p.max_radius;
		pellet_type = p.pellet_type;
		for (int k = 0; k < 3; k++){
			color[k] = p.color[k];
		}
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
					((TutorialPellet) neighbor_pellet).setHit();
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

				if (constructing == true) {
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

	private void setHit() {
		hit = true;
		System.out.println("tutorial pellet hit");
		color[0] = 0.1f;
		color[1] = 0.9f;
		color[2] = 0.2f;
	}

	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(color[0], color[1], color[2], alpha);
			drawSphere(radius);
		} else {
			glColor4f(color[0], color[1], color[2], 1f);
			drawSphere(radius);
		}
	}

	public static void addTutorialPellet(TutorialPellet p) {
		Main.all_pellets_in_world.add(p);
		tutorial_pellets.add(p);
		
	}

	public static int numHitPellets() {
		int n = 0;
		for (TutorialPellet p : tutorial_pellets) {
			if (p.hit) {
				n++;
			}
		}
		return n;
	}
}
