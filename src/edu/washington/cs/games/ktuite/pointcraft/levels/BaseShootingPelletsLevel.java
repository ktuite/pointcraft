package edu.washington.cs.games.ktuite.pointcraft.levels;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Ground;
import edu.washington.cs.games.ktuite.pointcraft.tools.TutorialPellet;

public class BaseShootingPelletsLevel extends BaseLevel {

	protected float world_extent = 0.1f;
	protected float cube_extent = 0.005f;
	protected int points_per_cube = 10000;
	protected List<Vector3f> cube_positions;
	protected boolean level_won = false;
	protected int num_pellets;
	protected boolean instructions_moved_up_top = false;

	public BaseShootingPelletsLevel(Main main) {
		super();

		cube_positions = new LinkedList<Vector3f>();

		Ground.enabled = true;
		Ground.impenetrable = true;
		Main.which_gun = GunMode.TUTORIAL;

		Main.draw_pellets = true;
		Main.draw_points = true;
		Main.pos.set(0, 0, 0);
		Main.pan_angle = 0;
		Main.vel.set(0, 0, 0);

		Main.all_pellets_in_world.clear();
		num_pellets = Main.all_pellets_in_world.size();

		Main.gui_manager.showNoTools();
		Main.gui_manager.showNoLastActivity();
		Main.gui_manager.setObjectiveText("Shoot each of the pellets");

		Main.which_gun = GunMode.TUTORIAL;

		Main.pellet_scale = .5f;
		
		TutorialPellet.tutorial_pellets.clear();
	}

	public void checkLevelState() {

		setScoreText();
		
		if (!level_won && numPelletsTouched() == TutorialPellet.tutorial_pellets.size()) {
			levelWon();
		} else if (level_won) {
			playWinAnimation();
		} else {
			 
			if (!instructions_moved_up_top
					&& !(Main.pos.x == 0 && Main.pos.y == 0 && Main.pos.z == 0)) {
				Main.gui_manager.moveCenterInstructionsToTop();
				instructions_moved_up_top = true;
			}

		}

	}

	private void playWinAnimation() {
		if (Mouse.isButtonDown(0)) {
			Main.gui_manager.level_selection_overlay.advanceLevel();
			return;
		}

		float rand = 0.0001f;
		float[] c = new float[3];
		for (int i = 0; i < PointStore.num_points; i++) {
			Vector3f center = cube_positions.get(0); // hack, just use first cube's center
			c[0] = center.getX();
			c[1] = center.getY();
			c[2] = center.getZ();

			float p = PointStore.point_positions.get(i * 3 + 1);
			if (p < Ground.height + rand) {
				PointStore.point_positions.put(i * 3 + 1, Ground.height + rand);
			} else {
				for (int k = 0; k < 3; k++) {
					p = PointStore.point_positions.get(i * 3 + k);
					p = c[k]
							+ (p - c[k] + (float) Math.random() * rand - rand / 2)
							* 1.015f;
					PointStore.point_positions.put(i * 3 + k, p);
				}
			}
		}
	}

	protected void setScoreText() {
		Main.gui_manager.setScoreText(numPelletsTouched() + " / "
				+ TutorialPellet.tutorial_pellets.size() + " pellets");
	}

	private int numPelletsTouched() {
		return TutorialPellet.numHitPellets();
	}

	private void levelWon() {
		level_won = true;
		Main.gui_manager.animateRisingText("Level Complete!");
		Main.draw_pellets = false;
		Main.gui_manager.clickToAdvanceText();
	}

}
