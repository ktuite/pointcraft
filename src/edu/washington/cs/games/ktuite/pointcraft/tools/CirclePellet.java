package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.LinkedList;

import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;
import static org.lwjgl.opengl.GL11.*;

public class CirclePellet extends Pellet {

	protected static LinkedList<Pellet> user_pellets = new LinkedList<Pellet>();
	protected static LinkedList<Pellet> pellets = new LinkedList<Pellet>();
	protected static LinkedList<Primitive> primitives = new LinkedList<Primitive>();
	protected static LinkedList<Scaffold> scaffolds = new LinkedList<Scaffold>();
	protected static float circle_radius = 0;
	protected static Vector3f circle_center = new Vector3f();
	protected static Vector3f circle_norm = new Vector3f();
	protected static int circle_segments = 16;

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public CirclePellet() {
		super();
		pellet_type = Main.GunMode.CIRCLE;
	}

	public CirclePellet(Pellet lp) {
		super();
		pos.set(lp.pos);
		radius = lp.radius;
		max_radius = lp.max_radius;
		constructing = lp.constructing;
		pellet_type = Main.GunMode.CIRCLE;
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

	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.6f, .1f, .6f, alpha);
			drawSphere(radius);
		} else {
			glColor4f(.7f, .1f, .7f, 1f);
			drawSphere(radius);
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
			makeCirclePolygon();
			Main.all_dead_pellets_in_world.addAll(user_pellets);
			startNew();
		}
		
		displayInstructions();
	}

	public static void displayInstructions() {
		System.out.println("INSTRUCTIONS for CIRCLE gun");
		if (user_pellets.size() < 3) {
			System.out.println("shoot 3 pellets to define a circle");
		}
	}

	public static void startNew() {
		System.out.println("starting new circle");
		user_pellets.clear();
		pellets.clear();
		scaffolds.clear();
		primitives.clear();
	}

	protected static void fitCircle(Vector3f p1, Vector3f p2, Vector3f p3) {
		// radius
		float a = Vector3f.sub(p1, p2, null).length();
		float b = Vector3f.sub(p2, p3, null).length();
		float c = Vector3f.sub(p3, p1, null).length();
		circle_radius = (float) (a * b * c / Math.sqrt(2 * a * a * b * b + 2
				* b * b * c * c + 2 * c * c * a * a - a * a * a * a - b * b * b
				* b - c * c * c * c));

		// center
		Vector3f p1p2 = Vector3f.sub(p2, p1, null);
		Vector3f p2p3 = Vector3f.sub(p3, p2, null);
		Vector3f norm = (Vector3f) Vector3f.cross(p1p2, p2p3, null).normalise();

		// first bisector
		Vector3f b1_mid = (Vector3f) Vector3f.add(p1, p2, null).scale(.5f);
		Vector3f b1_dir = (Vector3f) Vector3f.cross(p1p2, norm, null)
				.normalise();

		// second bisector
		Vector3f b2_mid = (Vector3f) Vector3f.add(p2, p3, null).scale(.5f);
		Vector3f b2_dir = (Vector3f) Vector3f.cross(p2p3, norm, null)
				.normalise();

		float x1 = b1_mid.x;
		float y1 = b1_mid.y;
		float x2 = b2_mid.x;
		float y2 = b2_mid.y;
		float a1 = b1_dir.x;
		float b1 = b1_dir.y;
		float a2 = b2_dir.x;
		float b2 = b2_dir.y;

		float t2 = (x1 - x2 + a1 * (x2 - x1 + y1 - y2) / (a1 - b1))
				/ (a2 - a1 * (a2 - b2) / (a1 - b1));

		circle_center = Vector3f.add(b2_mid, (Vector3f) b2_dir.scale(t2), null);
		circle_norm.set(norm);

	}
	
	private static void makeCirclePolygon() {
		LinkedList<Pellet> polygon_pellets = computeCirclePellets(circle_segments, circle_center, user_pellets.get(0).pos);
		Main.new_pellets_to_add_to_world.addAll(polygon_pellets);
		polygon_pellets.add(polygon_pellets.get(0));
		Primitive circle = new Primitive(GL_POLYGON, polygon_pellets);
		primitives.add(circle);
		Main.geometry.add(circle);
	}

	protected static LinkedList<Pellet> computeCirclePellets(float n,
			Vector3f center, Vector3f edge) {
		LinkedList<Pellet> circle_pellets = new LinkedList<Pellet>();

		Matrix4f mat = new Matrix4f();
		Vector3f inverse_circle_center = new Vector3f(center);
		inverse_circle_center.scale(-1);

		for (int i = 0; i < n; i++) {
			mat.setIdentity();
			mat.translate(center);
			Matrix4f.rotate((float) (Math.PI * 2f / n * i), circle_norm, mat,
					mat);
			mat.translate(inverse_circle_center);
			Pellet center_pellet = new CirclePellet(user_pellets.get(0));

			Vector4f start_pt = new Vector4f(edge.x, edge.y, edge.z, 1);
			Vector4f end_pt = new Vector4f();
			Matrix4f.transform(mat, start_pt, end_pt);

			center_pellet.pos.set(end_pt.x, end_pt.y, end_pt.z);

			circle_pellets.add(center_pellet);
		}

		return circle_pellets;

	}
}
