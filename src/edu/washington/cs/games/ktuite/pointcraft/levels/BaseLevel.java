package edu.washington.cs.games.ktuite.pointcraft.levels;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;

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
	}

	// event loop that gets called from main event loop
	public void checkLevelState() {
		score++;
	}
}
