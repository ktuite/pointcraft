package edu.washington.cs.games.ktuite.pointcraft;

import java.util.LinkedList;
import java.util.List;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

public class VerticalLinePellet extends Pellet {

	public static VerticalLinePellet top_pellet = null;
	public static VerticalLinePellet bottom_pellet = null;
	public boolean is_upward_pellet = false;
	public boolean is_downward_pellet = false;
	private Vector3f last_good_position = new Vector3f();

	/*
	 * A Pellet is a magical thing that you can shoot out of a gun that will
	 * travel towards the model and stick to the first point it intersects.
	 * 
	 * Soon it will even stick to other pellets.
	 */
	public VerticalLinePellet(List<Pellet> _pellets) {
		super(_pellets);
		pellet_type = Main.GunMode.VERTICAL_LINE;
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

			if (!is_upward_pellet && !is_downward_pellet) {
				// if it's too old, kill it
				if (Main.timer.getTime() - birthday > 5) {
					alive = false;
				} else {
					// if the pellet is not dead yet, see if it intersected
					// anything

					// did it hit another pellet?
					Pellet neighbor_pellet = queryOtherPellets();

					// did it hit a line or plane?
					Vector3f closest_point = queryScaffoldGeometry();

					if (neighbor_pellet != null) {
						System.out.println("pellet stuck to another pellet");
						pos.set(neighbor_pellet.pos);
						alive = false;
						attachVerticalLine();
					} else if (closest_point != null) {
						boolean new_up_set = trySettingNewUp();
						if (new_up_set) {
							alive = false;
						} else {
							System.out.println("pellet stuck to some geometry");
							constructing = true;
							pos.set(closest_point);
							attachVerticalLine();
						}
					} else {
						// it didn't hit some existing geometry or pellet
						// so check the point cloud
						int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

						// is it near some points?!
						if (neighbors > 0) {
							snapToCenterOfPoints();
							constructing = true;
							Main.attach_effect.playAsSoundEffect(1.0f, 1.0f,
									false);

							attachVerticalLine();
						}
					}
				}
			} else {
				// this is one of the upward or downward pellets
				if (Main.timer.getTime() - birthday > 2) {
					if (last_good_position != null) {
						constructing = true;
						pos.set(last_good_position);
					} else {
						alive = false;
					}
				} else {
					// did it hit another pellet?
					Pellet neighbor_pellet = queryOtherPellets();

					// did it hit a line or plane?
					Vector3f closest_point = queryScaffoldGeometry();

					if (neighbor_pellet != null) {
						// something like top pellet = this neighbor pellet, or
						// the position anyway
					} else if (closest_point != null) {
						// something like leave the pellet here... set
						// constructing = true
						// call some makeactualline-or-try-to function
						constructing = true;
					} else {
						// it didn't hit some existing geometry or pellet
						// so check the point cloud
						int neighbors = queryKdTree(pos.x, pos.y, pos.z, radius);

						// is it near some points?!
						if (neighbors > 2) {
							last_good_position.set(pos);

						} else {
							pos.set(last_good_position);
							if (last_good_position.x != 0
									&& last_good_position.y != 0
									&& last_good_position.z != 0)
								constructing = true;
							else {
								alive = false;
								if (is_downward_pellet)
									bottom_pellet = null;
								if (is_upward_pellet)
									top_pellet = null;
							}
						}
					}

					if (constructing && alive)
						ActionTracker.newVerticalLinePellet(this);
					if (top_pellet != null && bottom_pellet != null
							&& top_pellet.constructing && top_pellet.alive
							&& bottom_pellet.constructing
							&& bottom_pellet.alive) {
						ActionTracker.newVerticalHeightSet();
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
		if (geom instanceof LineScaffold){
			LineScaffold line = (LineScaffold)geom;
			Vector3f.sub(line.pellets.get(0).pos, line.pellets.get(1).pos, Main.up_vec);
			Main.up_vec.normalise();
			System.out.println("new up vector: " + Main.up_vec);
			return true;
		}
		return false;
	}

	private void attachVerticalLine() {
		if (bottom_pellet == null || top_pellet == null) {
			float speed = vel.length() / 2;

			Vector3f new_up = new Vector3f(0, 1, 0);
			Main.rotateVector(new_up);
			new_up.scale(radius * 1.5f);

			top_pellet = new VerticalLinePellet(main_pellets);
			top_pellet.pos.set(pos);
			Vector3f.add(top_pellet.pos, new_up, top_pellet.pos);
			top_pellet.vel.set(0, speed, 0);
			Main.rotateVector(top_pellet.vel);
			top_pellet.is_upward_pellet = true;
			Main.new_pellets_to_add_to_world.add(top_pellet);

			bottom_pellet = new VerticalLinePellet(main_pellets);
			bottom_pellet.pos.set(pos);
			Vector3f.sub(bottom_pellet.pos, new_up, bottom_pellet.pos);
			bottom_pellet.vel.set(0, -1 * speed, 0);
			Main.rotateVector(bottom_pellet.vel);
			bottom_pellet.is_downward_pellet = true;
			Main.new_pellets_to_add_to_world.add(bottom_pellet);

		} else {
			Vector3f new_up = new Vector3f();
			Vector3f.sub(top_pellet.pos, bottom_pellet.pos, new_up);
			float height = new_up.length();
			new_up.normalise();

			Vector3f center = new Vector3f();
			Vector3f.add(top_pellet.pos, bottom_pellet.pos, center);
			center.scale(.5f);

			// change pos to be where plane defined by normal and midpoint of
			// line intersects ...
			PlaneScaffold temp_plane = new PlaneScaffold();
			temp_plane.a = new_up.x;
			temp_plane.b = new_up.y;
			temp_plane.c = new_up.z;
			temp_plane.d = -1
					* (temp_plane.a * center.x + temp_plane.b * center.y + temp_plane.c
							* center.z);

			Vector3f new_pos = temp_plane.closestPoint(pos);

			new_up.scale(height / 2f);

			VerticalLinePellet new_top_pellet = new VerticalLinePellet(
					main_pellets);
			new_top_pellet.pos.set(new_pos);
			Vector3f.add(new_top_pellet.pos, new_up, new_top_pellet.pos);
			new_top_pellet.constructing = true;
			Main.new_pellets_to_add_to_world.add(new_top_pellet);

			VerticalLinePellet new_bottom_pellet = new VerticalLinePellet(
					main_pellets);
			new_bottom_pellet.pos.set(new_pos);
			Vector3f.sub(new_bottom_pellet.pos, new_up, new_bottom_pellet.pos);
			new_bottom_pellet.constructing = true;
			Main.new_pellets_to_add_to_world.add(new_bottom_pellet);

			// make polygon
			List<Pellet> cycle = new LinkedList<Pellet>();
			cycle.add(top_pellet);
			cycle.add(bottom_pellet);
			cycle.add(new_bottom_pellet);
			cycle.add(new_top_pellet);
			cycle.add(top_pellet);

			Primitive polygon = new Primitive(GL_POLYGON, cycle);
			polygon.setPlayerPositionAndViewingDirection(pos, vel);
			Main.geometry.add(polygon);

			// fit the new polygon's plane
			polygon.getPlane().pellets.add(new_bottom_pellet);
			polygon.getPlane().pellets.add(new_top_pellet);
			polygon.getPlane().pellets.add(bottom_pellet);
			polygon.getPlane().fitPlane();

			ActionTracker.newPolygon(polygon, null);

			ActionTracker.newVerticalWall(top_pellet, bottom_pellet);

			top_pellet = new_top_pellet;
			bottom_pellet = new_bottom_pellet;
		}
		alive = false;
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

	public static void clearAllVerticalLines() {
		System.out.println("clearing vertical line heights");
		bottom_pellet = null;
		top_pellet = null;
	}

}
