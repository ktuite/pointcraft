package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.LinkedList;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import static org.lwjgl.opengl.GL11.*;

public class DomePellet extends CirclePellet {

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public DomePellet() {
		super();
		pellet_type = Main.GunMode.DOME;
	}

	public DomePellet(Pellet lp) {
		super();
		pellet_type = Main.GunMode.DOME;
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
		if (user_pellets.size() == 3) {
			fitCircle(user_pellets.get(0).pos, user_pellets.get(1).pos, user_pellets.get(2).pos);
		} else if (user_pellets.size() == 4) {
			possiblyFlipNormal();
			fitSemiSphere(5);
			Main.all_dead_pellets_in_world.addAll(user_pellets);
			startNew();
		}
		displayInstructions();
	}

	private static void possiblyFlipNormal() {
		Vector3f dir = Vector3f.sub(user_pellets.get(3).pos, circle_center,
				null);
		if (Vector3f.dot(dir, circle_norm) < 0) {
			circle_norm.scale(-1);
		}

	}

	public static void displayInstructions() {
		System.out.println("INSTRUCTIONS for DOME gun");
		if (user_pellets.size() < 3) {
			System.out.println("shoot 3 pellets to define a dome/semi-sphere");
		}
	}

	private static void fitSemiSphere(float segments) {
		for (int i = 0; i < segments; i++) {
			System.out.println("i/segments " + i);
			float r = circle_radius * i / segments;
			Vector3f part_way_up = Vector3f.add(circle_center,
					(Vector3f) new Vector3f(circle_norm).scale(r), null);

			Vector3f edge_to_center = Vector3f.sub(user_pellets.get(0).pos,
					circle_center, null);
			float r2 = (float) Math.sqrt(circle_radius * circle_radius
					- Math.pow(r, 2));
			edge_to_center.normalise().scale(r2);
			Vector3f part_way_over = Vector3f.add(part_way_up, edge_to_center,
					null);

			LinkedList<Pellet> new_pellets = computeCirclePellets(
					circle_segments, part_way_up, part_way_over);
			System.out.println("number of new pellets: " + new_pellets.size());
			Main.new_pellets_to_add_to_world.addAll(new_pellets);

			pellets.addAll(new_pellets);
		}

		Pellet d = new DomePellet(user_pellets.get(0));
		Vector3f top_of_dome = (Vector3f) new Vector3f(circle_norm)
				.scale(circle_radius);
		Vector3f.add(circle_center, top_of_dome, top_of_dome);
		d.pos.set(top_of_dome);
		pellets.add(d);
		Main.new_pellets_to_add_to_world.add(d);

		stitchPolygons(segments);
	}

	private static void stitchPolygons(float segments) {
		System.out.println("number of pellets: " + pellets.size());
		for (int h = 0; h < segments - 1; h++) {
			for (int i = 0; i < circle_segments; i++) {
				LinkedList<Pellet> polygon_pellets = new LinkedList<Pellet>();

				polygon_pellets.add(pellets.get(circle_segments * h + i
						% circle_segments));
				polygon_pellets.add(pellets.get(circle_segments * h + (i + 1)
						% circle_segments));
				polygon_pellets.add(pellets.get(circle_segments * (h + 1)
						+ (i + 1) % circle_segments));
				polygon_pellets.add(pellets.get(circle_segments * (h + 1) + (i)
						% circle_segments));
				polygon_pellets.add(polygon_pellets.get(0));

				Primitive poly = new Primitive(GL_POLYGON, polygon_pellets);
				primitives.add(poly);
				Main.geometry.add(poly);
			}
		}

		for (int i = 0; i < circle_segments; i++) {
			LinkedList<Pellet> polygon_pellets = new LinkedList<Pellet>();

			int h = (int) (segments - 1);
			polygon_pellets.add(pellets.get(circle_segments * h + i
					% circle_segments));
			polygon_pellets.add(pellets.get(circle_segments * h + (i + 1)
					% circle_segments));
			polygon_pellets.add(pellets.getLast());

			polygon_pellets.add(polygon_pellets.get(0));

			Primitive poly = new Primitive(GL_POLYGON, polygon_pellets);
			primitives.add(poly);
			Main.geometry.add(poly);
		}

	}
}
