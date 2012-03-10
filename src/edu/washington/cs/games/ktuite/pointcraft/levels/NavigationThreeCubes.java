package edu.washington.cs.games.ktuite.pointcraft.levels;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Ground;

public class NavigationThreeCubes extends BaseLevel {

	private float world_extent = 0.1f;
	private float cube_extent = 0.005f;
	private int points_per_cube = 10000;
	private List<Vector3f> cube_positions;
	private boolean[] cube_touched;
	private boolean level_won = false;

	public NavigationThreeCubes(Main main) {
		super();

		cube_positions = new LinkedList<Vector3f>();
		cube_positions.add(new Vector3f(0f, 0f, -0.05f));
		cube_positions.add(new Vector3f(0.015f, 0f, -0.05f));
		cube_positions.add(new Vector3f(-0.015f, 0f, -0.05f));
		PointStore.loadCubes(cube_positions, cube_extent, points_per_cube,
				world_extent);

		main.initData();

		cube_touched = new boolean[cube_positions.size()];
		for (int i = 0; i < cube_touched.length; i++) {
			cube_touched[i] = false;
		}

		Ground.enabled = true;
		Ground.impenetrable = true;
		
		Main.gui_manager.setScoreText("Score: 0 out of " + cube_positions.size());
	}

	public void checkLevelState() {
		for (Vector3f cube_center : cube_positions) {
			Vector3f diff = Vector3f.sub(Main.pos, cube_center, null);
			if (Math.abs(diff.x) < cube_extent
					&& Math.abs(diff.y) < cube_extent
					&& Math.abs(diff.z) < cube_extent) {
				int i = cube_positions.indexOf(cube_center);
				if (cube_touched[i] == false) {
					cube_touched[i] = true;
					PointStore.changeColorOfPointSubsetToGreen(i
							* points_per_cube, (i + 1) * points_per_cube);
					Main.gui_manager.setScoreText("Score: " + numCubesTouched()
							+ " out of " + cube_positions.size());
				}
			}
		}
		if (!level_won && numCubesTouched() == cube_positions.size()){
			levelWon();
		}
	}

	private int numCubesTouched() {
		int n = 0;
		for (int i = 0; i < cube_touched.length; i++){
			if (cube_touched[i])
				n++;
		}
		return n;
	}
	
	private void levelWon(){
		level_won = true;
		System.out.println("Level won!! Yay!");
	}

}
