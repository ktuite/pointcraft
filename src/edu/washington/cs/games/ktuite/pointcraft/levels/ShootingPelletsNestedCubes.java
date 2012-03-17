package edu.washington.cs.games.ktuite.pointcraft.levels;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.tools.TutorialPellet;

public class ShootingPelletsNestedCubes extends BaseShootingPelletsLevel {

	public ShootingPelletsNestedCubes(Main main) {
		super(main);

		Vector3f center = new Vector3f(0f, 0f, -0.05f);
		cube_positions.add(center);
		cube_positions.add(new Vector3f(center));

		float[] cube_extent_array = { cube_extent, cube_extent * 2 };
		int[] points_per_cube_array = { points_per_cube, points_per_cube * 2 };
		int total_points = 0;
		for (int i = 0; i < points_per_cube_array.length; i++) {
			total_points += points_per_cube_array[i];
		}

		PointStore.loadCubesOfDifferentSizes(cube_positions, cube_extent_array,
				points_per_cube_array, world_extent);
		PointStore.changeColorOfPointSubsetToGray(0, total_points, 90);

		main.initData();

		// add corner pellets
		TutorialPellet corner = new TutorialPellet();
		corner.constructing = true;
		corner.radius = corner.max_radius;

		corner.pos.set(center.x + cube_extent, center.y + cube_extent, center.z
				+ cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));
		corner.pos.set(center.x + cube_extent, center.y + cube_extent, center.z
				- cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));
		corner.pos.set(center.x + cube_extent, center.y - cube_extent, center.z
				+ cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));
		corner.pos.set(center.x + cube_extent, center.y - cube_extent, center.z
				- cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));
		corner.pos.set(center.x - cube_extent, center.y + cube_extent, center.z
				+ cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));
		corner.pos.set(center.x - cube_extent, center.y + cube_extent, center.z
				- cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));
		corner.pos.set(center.x - cube_extent, center.y - cube_extent, center.z
				+ cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));
		corner.pos.set(center.x - cube_extent, center.y - cube_extent, center.z
				- cube_extent);
		TutorialPellet.addTutorialPellet(new TutorialPellet(corner));

		Main.gui_manager
				.setCenterInstructionText("Aim at the existing pellets and click to shoot\nSlow down by pressing Space");

		setScoreText();
	}

}
