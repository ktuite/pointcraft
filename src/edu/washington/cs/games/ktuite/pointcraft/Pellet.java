package edu.washington.cs.games.ktuite.pointcraft;

import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

public class Pellet {

	public Vector3f pos;
	public Vector3f vel;
	public Sphere sphere;
	public float radius;
	public float max_radius;
	public boolean alive;
	public boolean constructing;
	public float birthday;
	
	public static boolean MAKE_SPLAT = false;

	public Pellet() {
		pos = new Vector3f();
		sphere = new Sphere();
		vel = new Vector3f();
		radius = .0005f;
		max_radius = radius * 1.5f;
		birthday = Main.timer.getTime();
		alive = true;
		constructing = false;
	}

	public void update() {
		if (!constructing) {
			Vector3f.add(pos, vel, pos);
			if (Main.timer.getTime() - birthday > 5) {
				alive = false;
			}
			int neighbors = LibPointCloud.queryKdTree(pos.x, pos.y, pos.z,
					radius);
			if (neighbors > 0) {
				if (MAKE_SPLAT) {
					constructing = true;
					(new Thread(new SplatGeometry(pos, max_radius, this)))
							.start();
				}
				else {
					constructing = true;
				}
				Main.attach_effect.playAsSoundEffect(1.0f, 1.0f, false);
			}
		} else {
			if (radius < max_radius) {
				radius *= 1.1;
			}
		}
	}

	public void draw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.9f, .1f, .4f, alpha);
			//glColor4f(.9f, .6f, .7f, alpha);
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(.9f, .1f, .4f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}
}
