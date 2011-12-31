package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

public class HoverPellet extends Pellet {

	public static int hover_pellet;
	public static Pellet target_pellet;
	public static Pellet destination_pellet;
	private static boolean drag_started = false;
	private static float dist_to_pellet;
	public static Vector3f old_pos = null;
	private static Primitive target_pellet_primitive = null;

	public HoverPellet(List<Pellet> _pellets) {
		super(_pellets);
	}

	public static void click() {
		System.out
				.println("clicked while hover gun in action... clicked pellet: "
						+ hover_pellet);
		if (hover_pellet >= 0
				&& hover_pellet < Main.all_pellets_in_world.size()) {
			if (target_pellet == null) {
				target_pellet = Main.all_pellets_in_world.get(hover_pellet);
			} else if (destination_pellet == null) {
				destination_pellet = Main.all_pellets_in_world
						.get(hover_pellet);

				ActionTracker.combinedPellet(target_pellet, new Vector3f(
						target_pellet.pos));

				target_pellet.pos.set(destination_pellet.pos);
				// target_pellet.visible = false;

				fixPolygonOfPellet(target_pellet);

				target_pellet = null;
				destination_pellet = null;

			}
		}

	}

	public static void fixPolygonOfPellet(Pellet p) {
		for (Primitive g : Main.geometry) {
			if (g.isPolygon() && g.getVertices().contains(p)) {
				System.out.println("a polygon cotnains editied pellet");
				g.refreshTexture();
			}
		}

	}

	public static void startDrag() {
		if (hover_pellet >= 0) {
			drag_started = true;
			target_pellet = Main.all_pellets_in_world.get(hover_pellet);
			computeDistToPellet();
			findPolygonOfPellet();
			old_pos = new Vector3f(target_pellet.pos);
		}
	}

	public static void endDrag() {
		if (drag_started) {
			ActionTracker.movedPellet(target_pellet, old_pos);
			fixPolygonOfPellet(target_pellet);
			drag_started = false;
			target_pellet = null;
			target_pellet_primitive = null;
		}
	}

	public static void dimAllPellets() {
		if (!drag_started) {
			for (Pellet p : Main.all_pellets_in_world) {
				p.hover = false;
			}
		}
	}

	public static void illuminatePellet() {
		if (hover_pellet >= 0
				&& hover_pellet < Main.all_pellets_in_world.size())
			Main.all_pellets_in_world.get(hover_pellet).hover = true;
		if (target_pellet != null)
			target_pellet.hover = true;
	}

	public static void handleDrag() {
		if (drag_started && target_pellet != null) {
			if (target_pellet_primitive != null)
				updatePelletInPlane();
			else
				updatePelletConstantDistance();

		}
	}

	public static void computeDistToPellet() {
		dist_to_pellet = 1;
		Vector3f dist = new Vector3f();
		Vector3f.sub(target_pellet.pos, Main.getTransformedPos(), dist);
		dist_to_pellet = dist.length();
	}

	public static void findPolygonOfPellet() {
		float max_dot = 0;
		for (Primitive p : Main.geometry) {
			if (p.isPolygon()) {
				if (p.getVertices().contains(target_pellet)){
					float dot_with_gun = Math.abs(p.getPlane().planeNormalDotVector(Main.gun_direction));
					if (dot_with_gun > max_dot){
						target_pellet_primitive = p;
						max_dot = dot_with_gun;
					}
				}
			}
		}
	}

	public static void updatePelletConstantDistance() {
		Vector3f direction = new Vector3f(Main.gun_direction);
		direction.normalise();
		direction.scale(dist_to_pellet);
		Vector3f.add(Main.getTransformedPos(), direction, target_pellet.pos);
	}

	public static void updatePelletInPlane() {
		Vector3f p1 = Main.getTransformedPos();
		Vector3f p2 = Main.gun_direction;
		Vector3f.add(p1, p2, p2);
		Vector3f new_pos = target_pellet_primitive.getPlane()
				.checkForIntersectionLineWithPlaneNoBounds(p1, p2);

		if (new_pos != null)
			target_pellet.pos.set(new_pos);
	}

}
