package edu.washington.cs.games.ktuite.pointcraft;


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
	public PlanePellet() {
		super();
		pellet_type = Main.GunMode.PLANE;
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
					
					if (current_plane.pellets.size() >= 3)
						startNewPlane();
					
					pos.set(neighbor_pellet.pos);
					alive = false;
					ActionTracker.newPlanePellet(this);
					current_plane.add(this);
					
					current_plane.fitPlane();
					if (current_plane.pellets.size() >= 3)
						ActionTracker.newPlane(current_plane);
					current_plane.checkForIntersections();

				} else if (closest_point != null) {
					System.out.println("pellet stuck to some geometry");
					
					Scaffold intersected_scaffold = getIntersectedScaffoldGeometry();
					if (intersected_scaffold != current_plane
							&& current_plane.pellets.size() >= 3)
						startNewPlane();
					
					constructing = true;
					pos.set(closest_point);
					
					if (current_plane.pellets.size() < 3)
						ActionTracker.newPlanePellet(this);
					current_plane.add(this);
					current_plane.fitPlane();
					
					if (current_plane.pellets.size() == 3)
						ActionTracker.newPlane(current_plane);

					current_plane.checkForIntersections();

					if (intersected_scaffold == current_plane) {
						ActionTracker.extendedPlane(current_plane);
					}	
				} else if (Main.draw_points) {
					// it didn't hit some existing geometry or pellet
					// so check the point cloud
					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

					// is it near some points?!
					if (neighbors > 0) {
						if (current_plane.pellets.size() >= 3)
							startNewPlane();
						
						snapToCenterOfPoints();
						constructing = true;
						setInPlace();
						ActionTracker.newPlanePellet(this);
						current_plane.add(this);
						
						current_plane.fitPlane();
						if (current_plane.pellets.size() >= 3)
							ActionTracker.newPlane(current_plane);
						current_plane.checkForIntersections();
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

	@SuppressWarnings("unused")
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

	public void coloredDraw() {
		if (is_intersection) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.15f, .45f, .75f, alpha);
			drawSphere(radius);
		} else if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.2f, .2f, .7f, alpha);
			drawSphere(radius);
		} else {
			glColor4f(.2f, .2f, .7f, 1f);
			drawSphere(radius);
		}
	}

	public static void startNewPlane() {
		current_plane = new PlaneScaffold();
		Main.geometry_v.add(current_plane);
		System.out.println("making new plane");
	}
}
