package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class UpPellet extends Pellet {

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public UpPellet(List<Pellet> _pellets) {
		super(_pellets);
		pellet_type = Main.GunMode.DIRECTION_PICKER;
	}

	@Override
	public void update() {
		// constructing means the pellet has triggered something to be built at
		// its sticking location
		if (!constructing) {
			// not constructing means the pellet is still traveling through
			// space

			// move the pellet
			Vector3f.add(pos, vel, pos);

			// if it's too old, kill it
			if (Main.timer.getTime() - birthday > 5) {
				alive = false;
			} else {
				// if the pellet is not dead yet, see if it intersected
				// anything

				// did it hit a line?
				Vector3f closest_point = queryScaffoldGeometry();

				if (closest_point != null) {
					boolean new_up_set = trySettingNewUp();
					if (new_up_set) {
						alive = false;
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

	private boolean trySettingNewUp() {
		Scaffold geom = getIntersectedScaffoldGeometry();
		if (geom instanceof LineScaffold) {
			LineScaffold line = (LineScaffold) geom;
			Vector3f.sub(line.pellets.get(0).pos, line.pellets.get(1).pos,
					Main.up_vec);
			Main.up_vec.normalise();
			System.out.println("new up vector: " + Main.up_vec);
			return true;
		}
		return false;
	}


	public void coloredDraw() {
		if (constructing) {
			float alpha = 1 - radius / max_radius * .2f;
			glColor4f(.3f, .4f, .7f, alpha);
			sphere.draw(radius, 32, 32);
		} else {
			glColor4f(.3f, .4f, .7f, 1f);
			sphere.draw(radius, 32, 32);
		}
	}


}
