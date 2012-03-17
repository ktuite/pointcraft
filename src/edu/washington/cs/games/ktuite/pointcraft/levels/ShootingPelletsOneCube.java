package edu.washington.cs.games.ktuite.pointcraft.levels;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.tools.TutorialPellet;

public class ShootingPelletsOneCube extends BaseShootingPelletsLevel {

	public ShootingPelletsOneCube(Main main) {
		super(main);

		Vector3f center = new Vector3f(0f, 0f, -0.05f);
		cube_positions.add(center);

		PointStore.loadCubes(cube_positions, cube_extent, points_per_cube,
				world_extent);
		PointStore.changeColorOfPointSubsetToGray(0, points_per_cube, 90);

		main.initData();

		// add corner pellets
		TutorialPellet corner = new TutorialPellet();
		corner.constructing = true;
		corner.radius = corner.max_radius;
		// corner.color[0] = .9f;
		// corner.color[1] = .05f;
		// corner.color[2] = .2f;

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
				.setCenterInstructionText("Aim at the existing pellets and click to shoot");
		
		setScoreText();
	}

}
