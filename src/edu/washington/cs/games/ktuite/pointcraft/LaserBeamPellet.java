package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;

import static org.lwjgl.opengl.GL11.*;

public class LaserBeamPellet extends PolygonPellet {

	private static Vector3f orb_direction;
	private Vector3f scaled_orb_direction;
	private float orb_distance;
	private static Vector3f player_position;
	private static Vector3f pt_1 = new Vector3f();
	private static Vector3f pt_2 = new Vector3f();

	// having to do with orb gun only
	public static LaserBeamPellet laser_beam_pellet;

	/*
	 * This pellet doesnt get shot, it just sits in front of the player and gets
	 * placed in 3d space when they choose to place it somewhere
	 */
	public LaserBeamPellet(List<Pellet> _pellets) {
		super();
		orb_direction = new Vector3f();
		scaled_orb_direction = new Vector3f();
		player_position = new Vector3f();
		orb_distance = 0.03f;
		max_radius = radius;
		setInPlace();
		pellet_type = GunMode.LASER_BEAM;
	}

	public void setGunDirection(Vector3f _direction) {
		orb_direction = _direction;
	}

	public void setPlayerPosition(Vector3f _player_pos) {
		player_position = _player_pos;
		pt_1.set(player_position);
		Vector3f.add(pt_1, orb_direction, pt_2);
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
			glColor4f(.9f, 0f, .05f, alpha);
			drawSphere(radius);
		} else {
			glColor4f(1f, 0f, 0f, .6f);
			drawSphere(radius);
		}
	}

	public static void updateLaserBeamPellet(Vector3f pos,
			Vector3f gun_direction) {
		laser_beam_pellet.setGunDirection(gun_direction);
		laser_beam_pellet.setPlayerPosition(pos);

		Vector3f closest_point = null;
		float min_dist_to_player = Float.MAX_VALUE;
		for (int i = 0; i < PointStore.num_points; i++) {
			Vector3f pt = PointStore.getIthPoint(i);
			float dist_to_line = distanceToPoint(pt);
			if (dist_to_line < laser_beam_pellet.radius) {
				float dist_to_player = distanceToPlayer(pt);
				if (dist_to_player < min_dist_to_player) {
					min_dist_to_player = dist_to_player;
					closest_point = pt;
				}
			}
		}

		if (closest_point != null)
			laser_beam_pellet.pos.set(closestPoint(closest_point));
	}

	public static float distanceToPoint(Vector3f pos) {
		float dist = Float.MAX_VALUE;

		Vector3f temp = new Vector3f();
		Vector3f sub1 = new Vector3f();
		Vector3f sub2 = new Vector3f();
		Vector3f sub3 = new Vector3f();
		Vector3f.sub(pos, pt_1, sub1);
		Vector3f.sub(pos, pt_2, sub2);
		Vector3f.sub(pt_2, pt_1, sub3);
		Vector3f.cross(sub1, sub2, temp);
		dist = temp.length() / sub3.length();
		return Math.abs(dist);
	}

	public static Vector3f closestPoint(Vector3f pos) {
		Vector3f pt = new Vector3f();

		Vector3f line = new Vector3f();
		Vector3f.sub(pt_2, pt_1, line);
		line.normalise();

		Vector3f diag = new Vector3f();
		Vector3f.sub(pos, pt_1, diag);

		float dot = Vector3f.dot(line, diag);
		Vector3f.add(pt_1, (Vector3f) line.scale(dot), pt);

		return pt;
	}

	public static float distanceToPlayer(Vector3f pos) {
		Vector3f temp = new Vector3f();
		Vector3f.sub(pos, player_position, temp);
		float dot_prod = Vector3f.dot(temp, orb_direction);
		if (dot_prod > 0)
			return temp.length();
		else 
			return 10;
	}

	public static void drawLaserBeamPellet() {
		glPushMatrix();
		glTranslatef(laser_beam_pellet.pos.x, laser_beam_pellet.pos.y,
				laser_beam_pellet.pos.z);
		laser_beam_pellet.coloredDraw();
		glPopMatrix();
	}
}
