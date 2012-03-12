package edu.washington.cs.games.ktuite.pointcraft.levels;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;

public class NavigationOneCube extends BaseNavigationLevel {
	
	public NavigationOneCube(Main main) {
		super(main);

		cube_positions.add(new Vector3f(0f, 0f, -0.05f));
		
		initCubesTouched();
		
		PointStore.loadCubes(cube_positions, cube_extent, points_per_cube,
				world_extent);

		main.initData();
		
		Main.gui_manager.setInstructionText("Press W or up arrow to move forward");
	}

}
