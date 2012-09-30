package pc2;

import static org.lwjgl.opengl.GL11.*;

import java.nio.IntBuffer;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;

import pc2.PointStore;

public class Paintbrush {

	private static Vector3f pos = new Vector3f();
	public static float radius = 0;
	private static Vector3f viewing_direction;
	public static Mode mode = Mode.DELETE;
	static BitSet selected_points = new BitSet();

	public static IntBuffer highlight_indices;
	public static Map<Paintbrush.Mode, IntBuffer> buffer_map = new HashMap<Paintbrush.Mode, IntBuffer>();

	public static enum Mode {
		DELETE, WALL, GROUND, ROOF
	};

	public static void setRadius(float scale) {
		radius = scale * 0.0005f * Renderer.world_scale;
	}

	public static void update() {
		setRadius(Player.pellet_scale);
		Vector3f closest_point = closestPointCloudPoint();
		if (closest_point != null)
			pos.set(closestPointInSightLine(closest_point));

		int num_changed_points = 0;
		if (Mouse.isButtonDown(0)) {
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				num_changed_points = paintPoints(false);
			} else {
				num_changed_points = paintPoints(true);
			}
			if (num_changed_points > 0) {
				// PointStore.highlightSelectedPoints(selected_points);
				updateHighlightedPoints();
			}
		} else if (selected_points.cardinality() > 0) {
			moveSelectedPointsToBuffer();
			PointStore.hidePoints(selected_points);
			selected_points.clear();
			updateHighlightedPoints();
		}

	}

	private static int paintPoints(boolean select) {
		int[] pts = PointStore.getNearestPoints(pos.x, pos.y, pos.z, radius
				* Main.pellet_scale);
		if (pts == null) {
			return 0;
		}
		for (int i = 0; i < pts.length; i++) {
			if (!PointStore.deleted_points.get(i)) {
				if (select) {
					selected_points.set(pts[i]);
				} else {
					selected_points.clear(pts[i]);
				}
			}
		}
		return pts.length;
	}

	public static void draw() {
		glPushMatrix();
		glTranslatef(pos.x, pos.y, pos.z);
		setColor(mode);
		Renderer.drawSphere(radius);
		glPopMatrix();
	}

	private static void setColor(Mode m) {
		if (m == Mode.DELETE) {
			glColor4f(.9f, .9f, .9f, .5f);
		} else if (m == Mode.WALL) {
			glColor4f(.6f, .3f, .1f, .5f);
		} else if (m == Mode.GROUND) {
			glColor4f(.1f, .6f, .1f, .5f);
		} else if (m == Mode.ROOF) {
			glColor4f(.3f, .5f, .9f, .5f);
		} else {
			glColor4f(.9f, .0f, .4f, .8f);
		}
	}

	protected static Vector3f closestPointCloudPoint() {
		viewing_direction = Player.computeGunDirection();
		Vector3f closest_point = null;
		if (Renderer.draw_points) {
			float min_dist_to_player = Float.MAX_VALUE;
			for (int i = 0; i < PointStore.num_display_points; i++) {
				Vector3f pt = PointStore.getIthPoint(i);
				if (pt != null) {
					float dist_to_line = distanceToPoint(pt);
					if (dist_to_line < radius) {
						float dist_to_player = distanceToPlayer(pt);
						if (dist_to_player < min_dist_to_player) {
							min_dist_to_player = dist_to_player;
							closest_point = pt;
						}
					}
				}
			}
			if (min_dist_to_player == Float.MAX_VALUE) {
				return null;
			}
		}
		return closest_point;
	}

	public static Vector3f closestPointInSightLine(Vector3f pos) {
		Vector3f pt_1 = Player.getTransformedPos();
		Vector3f pt_2 = Vector3f.add(pt_1, viewing_direction, null);
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

	public static float distanceToPoint(Vector3f pos) {
		float dist = Float.MAX_VALUE;
		Vector3f pt_1 = Player.getTransformedPos();
		Vector3f pt_2 = Vector3f.add(pt_1, viewing_direction, null);

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

	public static float distanceToPlayer(Vector3f p) {
		Vector3f temp = Vector3f.sub(p, Player.getTransformedPos(), null);
		float dot = Vector3f.dot(temp, viewing_direction);
		if (dot > 0)
			return temp.length();
		else
			return Float.MAX_VALUE;
	}

	public static void changeMode(int key) {
		if (key == 0) {
			mode = Mode.DELETE;
		} else if (key == 1) {
			mode = Mode.GROUND;
		} else if (key == 2) {
			mode = Mode.WALL;
		} else if (key == 3) {
			mode = Mode.ROOF;
		}
	}

	private static void moveSelectedPointsToBuffer() {
		IntBuffer ib;
		if (buffer_map.get(mode) == null) {
			ib = BufferUtils.createIntBuffer(selected_points.cardinality());
		} else {
			IntBuffer old_ib = buffer_map.get(mode);
			ib = BufferUtils.createIntBuffer(old_ib.capacity()
					+ selected_points.cardinality());
			ib.put(old_ib);
		}
		for (int i = selected_points.nextSetBit(0); i >= 0; i = selected_points
				.nextSetBit(i + 1)) {
			ib.put(i);
		}
		ib.rewind();
		buffer_map.put(mode, ib);
		System.out.println("Points moved to buffer: " + mode);

		if (mode == Mode.WALL) {
			MathTest.fitPlane(selected_points);
		}

		// TODO also add to appropriate array of intbuffers
	}

	private static void updateHighlightedPoints() {
		highlight_indices = BufferUtils.createIntBuffer(selected_points
				.cardinality());
		for (int i = selected_points.nextSetBit(0); i >= 0; i = selected_points
				.nextSetBit(i + 1)) {
			highlight_indices.put(i);
		}
		highlight_indices.rewind();
	}

	public static void drawPoints() {
		glPointSize(Renderer.point_size + 1);
		glDisable(GL_DEPTH_TEST);
		if (highlight_indices != null) {
			setColor(mode);
			glDrawElements(GL_POINTS, highlight_indices);
		}

		glEnable(GL_DEPTH_TEST);
		glPointSize(Renderer.point_size);
		for (Entry<Mode, IntBuffer> entry : buffer_map.entrySet()) {
			Mode key = entry.getKey();
			if (key != Mode.DELETE) {
				IntBuffer value = entry.getValue();
				setColor(key);
				glDrawElements(GL_POINTS, value);
			}
		}

	}
}
