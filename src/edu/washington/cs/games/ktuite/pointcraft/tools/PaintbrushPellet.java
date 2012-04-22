package edu.washington.cs.games.ktuite.pointcraft.tools;

import static org.lwjgl.opengl.GL11.glColor4f;

import java.util.BitSet;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;

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
				if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
					num_changed_points = paintPoints(false);
				}
				else {
					num_changed_points = paintPoints(true);
				}
		
			}
			
			if (num_changed_points > 0){
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

}
