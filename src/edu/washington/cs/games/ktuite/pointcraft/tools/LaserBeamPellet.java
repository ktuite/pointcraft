package edu.washington.cs.games.ktuite.pointcraft.tools;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PickerHelper;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;
import edu.washington.cs.games.ktuite.pointcraft.geometry.LineScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.PlaneScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scaffold;

import static org.lwjgl.opengl.GL11.*;

public class LaserBeamPellet extends PolygonPellet {

	private static Vector3f orb_direction;
	private Vector3f scaled_orb_direction;
	private float orb_distance;
	private static Vector3f player_position;
	private static Vector3f pt_1 = new Vector3f();
	private static Vector3f pt_2 = new Vector3f();
	private Pellet colored_pellet;

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
		// setInPlace();
		pellet_type = GunMode.LASER_BEAM;
		colored_pellet = new Pellet();
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
		/*
		 * if (constructing) { float alpha = 1 - radius / max_radius * .2f;
		 * glColor4f(.9f, 0f, .05f, alpha); drawSphere(radius); } else {
		 * glColor4f(1f, 0f, 0f, .6f); drawSphere(radius); }
		 */
		colored_pellet.draw();
	}

	public static void updateLaserBeamPellet(Vector3f pos,
			Vector3f gun_direction) {
		
		Main.computeGunDirection();
		
		if (Main.which_gun == GunMode.DRAG_TO_EDIT
				|| Main.which_gun == GunMode.COMBINE) {
			laser_beam_pellet.visible = false;
			return;
		} else if (laser_beam_pellet.colored_pellet.pellet_type != Main.which_gun) {
			laser_beam_pellet.colored_pellet = ModelingGun.makeNewPellet();
		}

		laser_beam_pellet.colored_pellet.radius = Pellet.default_radius
				* Main.pellet_scale;


		laser_beam_pellet.setGunDirection(gun_direction);
		laser_beam_pellet.setPlayerPosition(pos);

		Vector3f closest_point = closestPoint();

		if (closest_point != null) {
			laser_beam_pellet.pos.set(closest_point);
			laser_beam_pellet.visible = true;
		} else {
			laser_beam_pellet.visible = false;
		}

	}

	private static Vector3f closestPointCloudPoint() {
		Vector3f closest_point = null;
		if (Main.draw_points) {
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
		}
		return closest_point;
	}

	private static Pellet closestPellet() {
		int pellet_id = PickerHelper.pickPellet();
		if (pellet_id >= 0 && pellet_id < Main.all_pellets_in_world.size())
			return Main.all_pellets_in_world.get(pellet_id);
		else
			return null;
	}

	private static Vector3f closestPoint() {
		LinkedList<Vector3f> closest_scaffold_points = closestPointsOnScaffolding();
		Vector3f closest_point_cloud_point = closestPointCloudPoint();
		if (closest_point_cloud_point != null){
			//closest_scaffold_points.add(closestPointInSightLine(closest_point_cloud_point));
			closest_scaffold_points.add(closest_point_cloud_point);
		}
		Vector3f closest_3d_point = closestPointFromList(closest_scaffold_points);

		Pellet closest_pellet = closestPellet();

		Pellet.dimAllPellets();

		if (closest_3d_point == null) {
			if (closest_pellet == null) {
				return null;
			} else {
				closest_pellet.hover = true;
				return closest_pellet.pos;
			}
		} else {
			if (closest_pellet == null) {
				return closest_3d_point;
			} else {
				float d_pellet = Vector3f.sub(closest_pellet.pos, Main.getTransformedPos(),
						null).length()
						- closest_pellet.radius;
				float d_cloud = Vector3f.sub(closest_3d_point, Main.getTransformedPos(), null)
						.length();
				if (d_pellet < d_cloud) {
					closest_pellet.hover = true;
					return closest_pellet.pos;
				} else {
					return closest_3d_point;
				}
			}
		}
	}

	private static Vector3f closestPointFromList(
			LinkedList<Vector3f> closest_scaffold_points) {
		float min_dist = Float.MAX_VALUE;
		Vector3f closest_point = null;
		for (Vector3f v : closest_scaffold_points) {
			if (v != null) {
				float d = Vector3f.sub(v, Main.getTransformedPos(), null).length();
				if (d < min_dist) {
					min_dist = d;
					closest_point = v;
				}
			}
		}
		return closest_point;
	}

	public static LinkedList<Vector3f> closestPointsOnScaffolding() {
		LinkedList<Vector3f> close_points = new LinkedList<Vector3f>();
		for (Scaffold scaffold : Main.geometry_v) {
			if (scaffold instanceof LineScaffold && scaffold.isReady()) {
				LineScaffold line = (LineScaffold) scaffold;

				Vector3f a = Main.getTransformedPos();
				Vector3f b = Main.gun_direction;
				Vector3f c = line.pt_1;
				Vector3f d = Vector3f.sub(line.pt_2, line.pt_1, null);

				Vector3f u = Vector3f.cross(b, d, null);
				u.normalise();
				float g = Vector3f.dot(Vector3f.sub(a, c, null), u);

				if (Math.abs(g) < laser_beam_pellet.radius) {

					double t = (a.x * b.z - b.x * g * u.z - b.x * a.z + b.x
							* c.z - c.x * b.z + g * b.z * u.x)
							/ (d.x * b.z - b.x * d.z);
					if (t > 0 && t < 1) {
						Vector3f p = Vector3f.add(c,
								(Vector3f) d.scale((float) t), null);
						close_points.add(p);
					}
				}
			} else if (scaffold instanceof PlaneScaffold && scaffold.isReady()) {
				PlaneScaffold plane = (PlaneScaffold) scaffold;
				Vector3f p = plane.checkForIntersectionLineWithPlane(Main.getTransformedPos(),
						Vector3f.add(Main.getTransformedPos(), Main.gun_direction, null));
				if (p != null) {
					close_points.add(p);
				}
			}
		}
		return close_points;
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

	public static Vector3f closestPointInSightLine(Vector3f pos) {
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
		if (laser_beam_pellet.visible) {
			glPushMatrix();
			glTranslatef(laser_beam_pellet.pos.x, laser_beam_pellet.pos.y,
					laser_beam_pellet.pos.z);
			laser_beam_pellet.coloredDraw();
			glPopMatrix();
		}
	}
}
