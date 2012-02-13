package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.List;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import static org.lwjgl.opengl.GL11.*;

public class OrbPellet extends PolygonPellet {

	private Vector3f orb_direction;
	private Vector3f scaled_orb_direction;
	private float orb_distance;
	private Vector3f player_position;

	// having to do with orb gun only
	public static OrbPellet orb_pellet;

	/*
	 * This pellet doesnt get shot, it just sits in front of the player and gets
	 * placed in 3d space when they choose to place it somewhere
	 */
	public OrbPellet(List<Pellet> _pellets) {
		super();
		orb_direction = new Vector3f();
		scaled_orb_direction = new Vector3f();
		player_position = new Vector3f();
		orb_distance = 0.03f;
		max_radius = radius;
		setInPlace();
		pellet_type = Main.GunMode.ORB;
	}

	public void setGunDirection(Vector3f _direction) {
		orb_direction = _direction;
	}

	public void setPlayerPosition(Vector3f _player_pos) {
		player_position = _player_pos;
	}

	public void increaseDistance() {
		orb_distance *= 1.05;
	}

	public void decreaseDistance() {
		orb_distance /= 1.05;
	}

	@Override
	public void update() {
		if (!constructing) {
			pos.set(player_position);
			scaled_orb_direction.set(orb_direction);
			scaled_orb_direction.scale(orb_distance);
			Vector3f.add(pos, scaled_orb_direction, pos);
		} else {
			if (radius < max_radius) {
				radius *= 1.1;
			}
		}
	}

	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(0f, .4f, .8f, alpha);
			drawSphere(radius);
		} else {
			glColor4f(0f, .5f, .8f, 1f);
			drawSphere(radius);
		}
	}

	public static void updateOrbPellet(Vector3f pos, Vector3f gun_direction,
			float pan_angle, float tilt_angle) {
		Vector2f horiz = new Vector2f();
		horiz.x = (float) Math.sin(pan_angle * 3.14159 / 180f);
		horiz.y = -1 * (float) Math.cos(pan_angle * 3.14159 / 180f);
		horiz.normalise();
		horiz.scale((float) Math.cos(tilt_angle * 3.14159 / 180f));
		gun_direction.x = horiz.x;
		gun_direction.z = horiz.y;
		gun_direction.y = -1 * (float) Math.sin(tilt_angle * 3.14159 / 180f);
		gun_direction.normalise();
		orb_pellet.setGunDirection(gun_direction);
		orb_pellet.setPlayerPosition(pos);
		orb_pellet.update();
	}

	public static void drawOrbPellet() {
		glPushMatrix();
		glTranslatef(orb_pellet.pos.x, orb_pellet.pos.y, orb_pellet.pos.z);
		orb_pellet.coloredDraw();
		glPopMatrix();
	}
}
