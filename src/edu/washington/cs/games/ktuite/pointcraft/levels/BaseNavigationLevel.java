package edu.washington.cs.games.ktuite.pointcraft.levels;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Ground;

public class BaseNavigationLevel extends BaseLevel {

	protected float world_extent = 0.1f;
	protected float cube_extent = 0.005f;
	protected int points_per_cube = 10000;
	protected List<Vector3f> cube_positions;
	protected boolean[] cube_touched;
	protected boolean level_won = false;
	protected boolean instructions_moved_up_top = false;

	public BaseNavigationLevel(Main main) {
		super();

		cube_positions = new LinkedList<Vector3f>();

		Ground.enabled = true;
		Ground.impenetrable = true;

		Main.gui_manager.showNoTools();
		Main.gui_manager.showNoLastActivity();
		Main.gui_manager.setObjectiveText("Navigate through each of the cubes");

		Main.which_gun = GunMode.DISABLED;

		// slow things down in this world
		Main.pellet_scale = .5f;
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
					Main.gui_manager.animateRisingText("+1 cube!");
					setScoreText();
				}
			}
		}
		if (!level_won && numCubesTouched() == cube_positions.size()) {
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
			Vector3f center = cube_positions.get(i / points_per_cube);
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
		Main.gui_manager.setScoreText(numCubesTouched() + " / "
				+ cube_positions.size() + " cubes");
	}

	private int numCubesTouched() {
		int n = 0;
		for (int i = 0; i < cube_touched.length; i++) {
			if (cube_touched[i])
				n++;
		}
		return n;
	}

	private void levelWon() {
		level_won = true;
		Main.gui_manager.animateRisingText("Level Complete!");
		Main.gui_manager.clickToAdvanceText();
	}

	protected void initCubesTouched() {
		cube_touched = new boolean[cube_positions.size()];
		for (int i = 0; i < cube_touched.length; i++) {
			cube_touched[i] = false;
		}
		setScoreText();
	}

}
