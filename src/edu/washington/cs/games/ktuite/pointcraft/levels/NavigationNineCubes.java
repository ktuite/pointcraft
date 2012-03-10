package edu.washington.cs.games.ktuite.pointcraft.levels;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;

public class NavigationNineCubes extends BaseLevel {
	
	private float world_extent = 0.1f;
	private float cube_extent = 0.005f;
	private int points_per_cube = 10000;
	private List<Vector3f> cube_positions;
	
	public NavigationNineCubes(Main main) {
		super();
		
		cube_positions = new LinkedList<Vector3f>();
		cube_positions.add(new Vector3f(0f, 0f, -0.05f));
		cube_positions.add(new Vector3f(0.015f, 0f, -0.05f));
		cube_positions.add(new Vector3f(-0.015f, 0f, -0.05f));
		
		cube_positions.add(new Vector3f(0f, 0.015f, -0.05f));
		cube_positions.add(new Vector3f(0.015f, 0.015f, -0.05f));
		cube_positions.add(new Vector3f(-0.015f, 0.015f, -0.05f));
		
		cube_positions.add(new Vector3f(0f, -0.015f, -0.05f));
		cube_positions.add(new Vector3f(0.015f, -0.015f, -0.05f));
		cube_positions.add(new Vector3f(-0.015f, -0.015f, -0.05f));
		PointStore.loadCubes(cube_positions, cube_extent, points_per_cube, world_extent);
		
		main.initData();
	}

	public void checkLevelState() {
		// check if player is inside the cube 
	}

}
