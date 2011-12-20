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
				//target_pellet.visible = false;
				
				fixPolygonOfPellet(target_pellet);

				target_pellet = null;
				destination_pellet = null;

			}
		}

	}

	public static void fixPolygonOfPellet(Pellet p) {
		for (Primitive g : Main.geometry){
			if (g.isPolygon() && g.getVertices().contains(p)){
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
			old_pos = new Vector3f(target_pellet.pos);
		}
	}

	public static void endDrag() {
		if (drag_started) {
			ActionTracker.movedPellet(target_pellet, old_pos);
			fixPolygonOfPellet(target_pellet);
			drag_started = false;
			target_pellet = null;
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
	
	public static void handleDrag(){
		if (drag_started && target_pellet != null)
			updatePelletConstantDistance();
	}
	
	public static void computeDistToPellet(){
		dist_to_pellet = 1;
		Vector3f dist = new Vector3f();
		Vector3f.sub(target_pellet.pos, Main.pos, dist);
		dist_to_pellet = dist.length();
	}
	
	public static void updatePelletConstantDistance(){
		Vector3f direction = new Vector3f(Main.gun_direction);
		direction.normalise();
		direction.scale(dist_to_pellet);
		Vector3f.add(Main.pos, direction, target_pellet.pos);
	}

}
