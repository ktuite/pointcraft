package edu.washington.cs.games.ktuite.pointcraft;

import java.util.List;

import org.lwjgl.util.vector.Vector3f;
import static org.lwjgl.opengl.GL11.*;

public class DestructorPellet extends Pellet {

	public DestructorPellet(List<Pellet> _pellets) {
		super(_pellets);
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
				// TODO: delete the thing it hit
				alive = false;
				neighbor_pellet.delete();
			} else {
				Scaffold geom = getIntersectedScaffoldGeometry();
				if (geom != null) {
					/*
					if (geom.isLine())
						LinePellet.current_line.clear();
					else if (geom.isPlane())
						PlanePellet.current_plane.clear();
					Main.geometry_v.remove(geom);
					alive = false;
					*/
				}
				else {
					Primitive poly = getIntersectedPolygon();
					if (poly != null){
						int idx = Main.geometry.indexOf(poly);
						int lines_to_remove = poly.numVertices()-1;
						int start_deleting = idx-lines_to_remove;
						for (int i = 0; i <= lines_to_remove; i++){
							Main.geometry.remove(start_deleting);
						}
						
						alive = false;
					}
				}
			}
		}
	}

	public void draw() {
		glColor4f(.6f, 0f, 0f, .3f);
		sphere.draw(radius, 32, 32);
	}
}
