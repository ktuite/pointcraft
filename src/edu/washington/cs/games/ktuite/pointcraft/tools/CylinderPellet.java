package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.LinkedList;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.geometry.PlaneScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;
import static org.lwjgl.opengl.GL11.*;

public class CylinderPellet extends CirclePellet {

	private static Vector3f cylinder_edge;

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public CylinderPellet() {
		super();
		pellet_type = Main.GunMode.CYLINDER;
	}

	public CylinderPellet(Pellet lp) {
		super();
		pellet_type = Main.GunMode.CYLINDER;
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
					alive = false;
					// do something with NEIGHBOR PELLET
					useNewPellet(neighbor_pellet);

				} else if (closest_point != null) {
					System.out.println("pellet stuck to some geometry");
					constructing = true;
					pos.set(closest_point);
					// do something with THIS
					useNewPellet(this);

				} else if (Main.draw_points) {
					// if it's not dead yet and also didn't hit a
					// neighboring
					// pellet, look for nearby points in model
					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

					// is it near some points?!
					if (neighbors > 0) {
						constructing = true;
						setInPlace();
						snapToCenterOfPoints();

						// do something with THIS
						useNewPellet(this);
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

	public static void useNewPellet(Pellet pellet) {
		for (Pellet up : user_pellets) {
			if (up.equals(pellet))
				return;
		}
		user_pellets.add(pellet);
		if (user_pellets.size() == 2) {
			fitLine();
		} else if (user_pellets.size() == 4) {
			fitCylinder();
			stitchPolygons();
			Main.all_dead_pellets_in_world.addAll(user_pellets);
			startNew();
		}
		displayInstructions();
	}

	public static void displayInstructions() {
		System.out.println("INSTRUCTIONS for CYLINDER gun");
		if (user_pellets.size() < 2) {
			System.out
					.println("shoot 2 pellets to define the vertical axis (edge) of a cylinder");
		} else if (user_pellets.size() < 4) {
			System.out
					.println("shoot 2 more pellets to define the shape/radius of the cylinder");
		}
	}

	private static void fitLine() {
		Vector3f p1 = user_pellets.get(0).pos;
		Vector3f p2 = user_pellets.get(1).pos;

		cylinder_edge = Vector3f.sub(p2, p1, null);

		Vector3f norm = new Vector3f(cylinder_edge);
		norm.normalise();

		float a, b, c, d;
		a = norm.x;
		b = norm.y;
		c = norm.z;
		d = -1 * (a * p1.x + b * p1.y + c * p1.z);

		PlaneScaffold plane = new PlaneScaffold();
		plane.a = a;
		plane.b = b;
		plane.c = c;
		plane.d = d;

		scaffolds.add(plane);
	}

	private static void fitCylinder() {
		PlaneScaffold plane = (PlaneScaffold) scaffolds.get(0);

		Vector3f p1 = user_pellets.get(0).pos;
		Vector3f p3 = plane.closestPoint(user_pellets.get(2).pos);
		Vector3f p4 = plane.closestPoint(user_pellets.get(3).pos);

		fitCircle(p1, p3, p4);

		LinkedList<Pellet> new_pellets = computeCirclePellets(circle_segments,
				circle_center, p1);
		Main.new_pellets_to_add_to_world.addAll(new_pellets);
		pellets.addAll(new_pellets);

		LinkedList<Pellet> duplicate_pellets = new LinkedList<Pellet>();
		for (Pellet p : new_pellets) {
			CylinderPellet p2 = new CylinderPellet(p);
			p2.constructing = true;
			p2.pos = Vector3f.add(p.pos, cylinder_edge, null);
			duplicate_pellets.add(p2);
		}

		Main.new_pellets_to_add_to_world.addAll(duplicate_pellets);
		pellets.addAll(duplicate_pellets);
	}

	private static void stitchPolygons() {
		for (int i = 0; i < circle_segments; i++) {
			LinkedList<Pellet> polygon_pellets = new LinkedList<Pellet>();

			polygon_pellets.add(pellets.get(i % circle_segments));
			polygon_pellets.add(pellets.get((i + 1) % circle_segments));
			polygon_pellets.add(pellets.get(circle_segments + (i + 1) % circle_segments));
			polygon_pellets.add(pellets.get(circle_segments + (i) % circle_segments));
			polygon_pellets.add(polygon_pellets.get(0));

			Primitive poly = new Primitive(GL_POLYGON, polygon_pellets);
			primitives.add(poly);
			Main.geometry.add(poly);
		}

	}

}
