package edu.washington.cs.games.ktuite.pointcraft.tools;


import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.ActionTracker;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;
import static org.lwjgl.opengl.GL11.*;

public class DestructorPellet extends Pellet {

	public DestructorPellet() {
		super();
		pellet_type = Main.GunMode.DESTRUCTOR;
	}

	public void update() {
		// not constructing means the pellet is still traveling through
		// space

		// move the pellet
		Vector3f.add(pos, vel, pos);

		// if it's too old, kill it
		if (Main.timer.getTime() - birthday > 5) {
			alive = false;
		} else {

			// if it's not dead yet, see if this pellet was shot at an
			// existing pellet
			Pellet neighbor_pellet = queryOtherPellets();
			if (neighbor_pellet != null) {
				alive = false;
				ActionTracker.deletedPellet(neighbor_pellet);
				neighbor_pellet.delete();
			} else {
				Scaffold geom = getIntersectedScaffoldGeometry();
				if (geom != null) {
					Main.geometry_v.remove(geom);
					ActionTracker.deletedScaffolding(geom);
					alive = false;
				} else {
					Primitive poly = getIntersectedPolygon();
					if (poly != null) {
						ActionTracker.deletedPrimitive(poly);
						int idx = Main.geometry.indexOf(poly);
						System.out.println("geometry idx: " + idx);
						Main.geometry.remove(idx);

						alive = false;
					}
				}
			}
		}
	}

	public void coloredDraw() {
		glColor4f(.6f, 0f, 0f, .3f);
		drawSphere(radius);
	}
}
