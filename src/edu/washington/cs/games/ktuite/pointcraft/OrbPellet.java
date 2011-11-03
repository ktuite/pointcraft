package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class OrbPellet extends PolygonPellet {

	private Vector3f orb_direction;
	private Vector3f scaled_orb_direction;
	private float orb_distance;
	private Vector3f player_position;

	/*
	 * This pellet doesnt get shot, it just sits in front of the player and gets
	 * placed in 3d space when they choose to place it somewhere
	 */
	public OrbPellet(List<Pellet> _pellets) {
		super(_pellets);
		orb_direction = new Vector3f();
		scaled_orb_direction = new Vector3f();
		player_position = new Vector3f();
		orb_distance = 0.01f;
		max_radius = radius;
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

	public void draw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(0f, .4f, .8f, alpha);
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(0f, .5f, .8f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}
}
