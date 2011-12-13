package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class PlanePellet extends Pellet {

	public static PlaneScaffold current_plane = new PlaneScaffold();

	// temporary holder of intersection pellets
	//public static List<PlanePellet> intersection_points = new LinkedList<PlanePellet>();

	private boolean is_intersection = false;

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public PlanePellet(List<Pellet> _pellets) {
		super(_pellets);
		pellet_type = Main.GunMode.PLANE;
	}

	@Override
	public void update() {
		// constructing means the pellet has triggered something to be built at
		// its sticking location
		if (!constructing) {
			// not constructing means the pellet is still traveling through
			// space

			if (current_plane.pellets.size() >= 3)
				startNewPlane();
			
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
					ActionTracker.newPlanePellet(this);
					current_plane.add(this);
					if (current_plane.pellets.size() >= 3)
						ActionTracker.newPlane(current_plane);
					current_plane.fitPlane();

				} else if (closest_point != null) {
					System.out.println("pellet stuck to some geometry");
					constructing = true;
					pos.set(closest_point);
					ActionTracker.newPlanePellet(this);
					
					if (!isAnotherPlane()){
						current_plane.add(this);
						
						if (current_plane.pellets.size() >= 3)
							ActionTracker.newPlane(current_plane);
						current_plane.fitPlane();
					}
					
					
					stickPelletToScaffolding();
				} else if (Main.draw_points) {
					// it didn't hit some existing geometry or pellet
					// so check the point cloud
					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

					// is it near some points?!
					if (neighbors > 0) {
						snapToCenterOfPoints();
						constructing = true;
						setInPlace();
						ActionTracker.newPlanePellet(this);
						current_plane.add(this);
						if (current_plane.pellets.size() >= 3)
							ActionTracker.newPlane(current_plane);
						current_plane.fitPlane();
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

	private boolean isAnotherPlane() {
		for (Scaffold geom : Main.geometry_v) {
			if (radius > geom.distanceToPoint(pos)) {
				geom.addNewPellet(this);
				if (geom instanceof PlaneScaffold)
					return true;
			}
		}
		return false;
	}

	public void delete() {
		super.delete();
	}

	public void draw() {
		if (is_intersection) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.15f, .45f, .75f, alpha);
			sphere.draw(radius, 32, 32);
		} else if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.2f, .2f, .7f, alpha);
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(.2f, .2f, .7f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}

	public static void startNewPlane() {
		current_plane = new PlaneScaffold();
		Main.geometry_v.add(current_plane);
		System.out.println("making new plane");
	}
}
