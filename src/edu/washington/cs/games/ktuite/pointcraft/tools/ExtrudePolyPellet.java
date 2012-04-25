package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.LinkedList;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;
import static org.lwjgl.opengl.GL11.*;

public class ExtrudePolyPellet extends Pellet {

	private static LinkedList<Pellet> user_pellets = new LinkedList<Pellet>();
	private static LinkedList<Pellet> pellets = new LinkedList<Pellet>();
	private static LinkedList<Primitive> primitives = new LinkedList<Primitive>();
	private static LinkedList<Scaffold> scaffolds = new LinkedList<Scaffold>();

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public ExtrudePolyPellet() {
		super();
		pellet_type = Main.GunMode.EXTRUDE_POLYGON;
	}

	public ExtrudePolyPellet(Pellet lp) {
		super();
		pos.set(lp.pos);
		radius = lp.radius;
		max_radius = lp.max_radius;
		constructing = lp.constructing;
		pellet_type = Main.GunMode.EXTRUDE_POLYGON;
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
		user_pellets.add(pellet);
		if (user_pellets.size() == 3) {
			makeFrontFaceOfBox();
		} else if (user_pellets.size() == 4) {
			extrudeBox();
		}
		displayInstructions();
	}

	@SuppressWarnings("unchecked")
	private static void makeFrontFaceOfBox() {
		System.out.println("** Making front face of box");
		float max_dist = 0;
		for (int i = 0; i < 3; i++) {
			Pellet a = user_pellets.get(i);
			Pellet b = user_pellets.get((i + 1) % 3);
			Pellet c = user_pellets.get((i + 2) % 3);
			float dist = Vector3f.sub(a.pos, c.pos, null).length();
			if (dist > max_dist) {
				pellets.clear();
				pellets.add(a);
				pellets.add(b);
				pellets.add(c);
				max_dist = dist;
			}
		}
		Pellet d = new ExtrudePolyPellet(user_pellets.get(0));
		Vector3f bc = Vector3f.sub(pellets.get(2).pos, pellets.get(1).pos, null);
		Vector3f.add(pellets.get(0).pos, bc, d.pos);
		pellets.add(d);
		Main.new_pellets_to_add_to_world.add(d);
		
		LinkedList<Pellet> polygon_pellets = (LinkedList<Pellet>) pellets.clone();
		polygon_pellets.add(pellets.get(0));
		Primitive front_face = new Primitive(GL_POLYGON, polygon_pellets);
		Main.geometry.add(front_face);
		primitives.add(front_face);
	}

	private static void extrudeBox() {
		System.out.println("** Extruding box");
		Pellet n = user_pellets.get(3);
		Primitive front_face = primitives.get(0);
		Vector3f p = front_face.getPlane().closestPoint(n.pos);
		Vector3f np = Vector3f.sub(n.pos, p, null);
		
		LinkedList<Pellet> back_face_pellets = new LinkedList<Pellet>();
		for (int i = 0; i < 4; i++){
			Pellet extruded = new ExtrudePolyPellet(pellets.get(i));
			Vector3f.add(extruded.pos, np, extruded.pos);
			back_face_pellets.add(extruded);
			pellets.add(extruded);
		}
		back_face_pellets.add(back_face_pellets.get(0));
		Primitive back_face = new Primitive(GL_POLYGON, back_face_pellets);
		Main.geometry.add(back_face);
		primitives.add(back_face);
		
		Main.new_pellets_to_add_to_world.addAll(back_face_pellets);
		
		for (int i = 0; i < 4; i++){
			LinkedList<Pellet> polygon_pellets = new LinkedList<Pellet>();
			Pellet a = pellets.get(i);
			Pellet b = pellets.get((i + 1)%4);
			Pellet c = pellets.get(4 + (i+1)%4);
			Pellet d = pellets.get(4 + i);
			polygon_pellets.add(a);
			polygon_pellets.add(b);
			polygon_pellets.add(c);
			polygon_pellets.add(d);
			polygon_pellets.add(a);
			Primitive face = new Primitive(GL_POLYGON, polygon_pellets);
			Main.geometry.add(face);
			primitives.add(face);
		}
		
		startNew();
	}

	public static void displayInstructions() {
		System.out.println("INSTRUCTIONS for BOX gun");
		if (user_pellets.size() < 3) {
			System.out
					.println("shoot 3 pellets to define the corner of the front face of the box");
		} else if (user_pellets.size() < 4) {
			System.out
					.println("shoot one more pellet to define the depth of the box");
		}
	}

	public static void startNew() {
		user_pellets.clear();
		pellets.clear();
		scaffolds.clear();
		primitives.clear();
		
	}

}
