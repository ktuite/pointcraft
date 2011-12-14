package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;

public class HoverPellet extends Pellet {

	public static int hover_pellet;
	public static Pellet target_pellet;
	public static Pellet destination_pellet;

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
			}
			else if (destination_pellet == null){
				destination_pellet = Main.all_pellets_in_world.get(hover_pellet);
				target_pellet.pos.set(destination_pellet.pos);
				destination_pellet.visible = false;
				
				target_pellet = null;
				destination_pellet = null;
			
			}
		}

	}

	public static void dimAllPellets() {
		for (Pellet p : Main.all_pellets_in_world) {
			p.hover = false;
		}
	}

	public static void illuminatePellet() {
		if (hover_pellet >= 0
				&& hover_pellet < Main.all_pellets_in_world.size())
			Main.all_pellets_in_world.get(hover_pellet).hover = true;
		if (target_pellet != null)
			target_pellet.hover = true;
	}

}
