package edu.washington.cs.games.ktuite.pointcraft.levels;

import edu.washington.cs.games.ktuite.pointcraft.Main;

public class BaseLevel {
	// level-specific state
	protected int score;
	protected String level_name = "Base Level/Random Points";

	// load or generate point cloud and put it in PointStore
	public BaseLevel(Main main) {
		System.out.println("Initializing level: " + level_name);
	}

	// event loop that gets called from main event loop
	public void checkLevelState() {
		score++;
	}
}
