package edu.washington.cs.games.ktuite.pointcraft;

import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class DoublePellet extends Pellet {

	private boolean is_first_pellet = true;
	private Vector3f last_good_position = null;

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public DoublePellet() {
		super();
		pellet_type = Main.GunMode.COMBINE;
	}

	@Override
	public void update() {
		// constructing means the pellet has triggered something to be built at
		// its sticking location
		if (!constructing) {
			// not constructing means the pellet is still traveling through
			// space

			/*
			 * if (Main.timer.getTime() - birthday == 0) { Vector3f back_up =
			 * new Vector3f(vel); back_up.scale(-40 * Main.world_scale);
			 * Vector3f.add(pos, back_up, pos); }
			 */

			// move the pellet
			Vector3f.add(pos, vel, pos);

			// if it's too old, kill it
			if (Main.timer.getTime() - birthday > 2) {
				if (last_good_position == null) {
					alive = false;
				} else {
					constructing = true;
					pos = last_good_position;
					setInPlace();
				}

			} else {
				// if the pellet is not dead yet, see if it intersected anything

				// did it hit a line or plane?
				Vector3f closest_point = queryScaffoldGeometry();

				if (closest_point != null) {
					System.out.println("pellet stuck to some geometry");

					pos.set(closest_point);

					if (is_first_pellet) {
						constructing = true;
						launchSecondPellet();
					} else {
						constructing = true;
					}

				} else if (Main.draw_points) {
					// it didn't hit some existing geometry or pellet
					// so check the point cloud
					int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

					// is it near some points?!
					if (neighbors > 0 && is_first_pellet) {
						constructing = true;
						setInPlace();
						launchSecondPellet();
					} else if (neighbors > 0 && !is_first_pellet) {
						last_good_position = new Vector3f(pos);
					}
				}
			}
		} else {
			// the pellet has stuck... here we just give it a nice growing
			// bubble animation
			if (radius < max_radius) {
				radius *= 1.1;
			}
		}
	}

	private void launchSecondPellet() {
		DoublePellet second_pellet = new DoublePellet();
		second_pellet.pos.set(this.pos);
		second_pellet.vel.set(this.vel);
		second_pellet.vel.scale(.5f);
		Vector3f move_forward = this.vel;
		move_forward.normalise();
		move_forward.scale(this.radius * 2 * 2);
		Vector3f.add(second_pellet.pos, move_forward, second_pellet.pos);
		second_pellet.is_first_pellet = false;
		Main.new_pellets_to_add_to_world.add(second_pellet);
	}

	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.1f, .5f, .5f, alpha);
			drawSphere(radius);
		} else {
			if (is_first_pellet) {
				glColor4f(.2f, .7f, .7f, 1f);
			} else {
				glColor4f(.4f, .7f, .7f, 1f);
			}
			drawSphere(radius);
		}
	}
}
