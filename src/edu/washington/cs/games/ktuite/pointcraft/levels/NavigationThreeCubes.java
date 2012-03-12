package edu.washington.cs.games.ktuite.pointcraft.levels;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;

public class NavigationThreeCubes extends BaseNavigationLevel {

	public NavigationThreeCubes(Main main) {
		super(main);

		cube_positions.add(new Vector3f(0f, 0f, -0.05f));
		cube_positions.add(new Vector3f(0.015f, 0f, -0.05f));
		cube_positions.add(new Vector3f(-0.015f, 0f, -0.05f));
		
		initCubesTouched();
		
		PointStore.loadCubes(cube_positions, cube_extent, points_per_cube,
				world_extent);

		main.initData();
		
		Main.gui_manager.setCenterInstructionText("Press WASD keys or arrow keys to move around");
	}


}
