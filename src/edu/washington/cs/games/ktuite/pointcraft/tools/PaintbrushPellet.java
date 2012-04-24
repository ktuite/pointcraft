package edu.washington.cs.games.ktuite.pointcraft.tools;

import static org.lwjgl.opengl.GL11.glColor4f;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.BitSet;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;
import edu.washington.cs.games.ktuite.pointcraft.Save;

public class PaintbrushPellet extends LaserBeamPellet {

	public static BitSet selected_points = new BitSet();

	public PaintbrushPellet() {
		super();
		pellet_type = GunMode.PAINTBRUSH;
	}

	// I can't bring myself to call this a flamethrower pellet...
	// it's like a laser beam thing but it paints points

	public static void updatePaintbrush(Vector3f pos, Vector3f gun_direction) {

		if (laser_beam_pellet.visible) {
			int num_changed_points = 0;
			if (Mouse.isButtonDown(0)) {
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
					num_changed_points = paintPoints(false);
				} else {
					num_changed_points = paintPoints(true);
				}

			}

			if (num_changed_points > 0) {
				PointStore.updateColors(selected_points);
			}
		}

	}

	private static int paintPoints(boolean select) {
		int[] pts = PointStore.getNearestPoints(laser_beam_pellet.pos.x,
				laser_beam_pellet.pos.y, laser_beam_pellet.pos.z,
				laser_beam_pellet.radius * Main.pellet_scale);
		if (pts == null) {
			return 0;
		}
		for (int i = 0; i < pts.length; i++) {
			if (select) {
				selected_points.set(pts[i]);
			} else {
				selected_points.clear(pts[i]);
			}
		}
		return pts.length;
	}

	public void coloredDraw() {
		glColor4f(.8f, .8f, .8f, .6f);
		drawSphere(radius);

	}

	private static void writeSelectedPoints(String filename) {
		File f = new File(filename);
		try {
			BufferedWriter w = new BufferedWriter(new FileWriter(f));
			for (int i = selected_points.nextSetBit(0); i >= 0; i = selected_points
					.nextSetBit(i + 1)) {
				String s = PointStore.point_positions.get(i * 3 + 0) + " "
						+ PointStore.point_positions.get(i * 3 + 1) + " "
						+ PointStore.point_positions.get(i * 3 + 2) + "\n";
				w.write(s);
			}

			w.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void startPoisson() {
		System.out.println("Running poisson reconstruction on selected points");
		writeSelectedPoints("selectedpoints.txt");
		String cmd_poisson = "/Users/ktuite/Downloads/PoissonRecon/Bin/Linux/PoissonRecon --in selectedpoints.txt --out mesh --depth 5";
		String cmd_parse = "/Users/ktuite/Downloads/PoissonRecon/convertPlyToPointCraftJson.py mesh.ply mesh.json";
		try {
			Process proc_poisson = Runtime.getRuntime().exec(cmd_poisson);
			try {
				proc_poisson.waitFor();
				Process proc_parse = Runtime.getRuntime().exec(cmd_parse);
				proc_parse.waitFor();
			} catch (InterruptedException e) {
				System.err.println("Process was interrupted");
			}
			// Runtime.getRuntime().exec(cmd_parse);
			System.out.println("Loading generated mesh");
			Save.loadModelAndFetchNewTextures(new File("mesh.json"));
			selected_points.clear();
			PointStore.updateColors(selected_points);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
