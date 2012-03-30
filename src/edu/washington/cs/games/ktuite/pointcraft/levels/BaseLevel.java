package edu.washington.cs.games.ktuite.pointcraft.levels;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Ground;
import edu.washington.cs.games.ktuite.pointcraft.tools.LinePellet;
import edu.washington.cs.games.ktuite.pointcraft.tools.PlanePellet;

public class BaseLevel {
	// level-specific state
	protected int score;
	
	// load or generate point cloud and put it in PointStore
	public BaseLevel(Main main) {
		PointStore.loadRandom();
	}

	public BaseLevel() {
		//nothing much happens here
		System.out.println("Initializing level: " + this.getClass().toString());
		Main.draw_points = true;
		Main.draw_pellets = true;
		Main.pos.set(0, 0, 0);
		Main.pan_angle = 0;
		Main.vel.set(0, 0, 0);
		Main.geometry_v.clear();
		Main.geometry.clear();
		Main.geometry_v.push(LinePellet.current_line);
		Main.geometry_v.push(PlanePellet.current_plane);
		Main.all_pellets_in_world.clear();
		Ground.impenetrable = false;
		Ground.enabled = false;
	}

	// event loop that gets called from main event loop
	public void checkLevelState() {
		score++;
	}
	
	public static String getCats(){
		return "cats";
	}
}
