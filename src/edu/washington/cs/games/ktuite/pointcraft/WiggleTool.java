package edu.washington.cs.games.ktuite.pointcraft;

import java.util.LinkedList;
import java.util.List;

import org.lwjgl.util.vector.Vector3f;

public class WiggleTool {

	public static void testWiggle() {
		// basically makes shapes fly away as if they were live crazy birds...
		for (Pellet p : Main.all_pellets_in_world) {
			Vector3f d = new Vector3f((float) Math.random() * 0.001f,
					(float) Math.random() * 0.001f,
					(float) Math.random() * 0.001f);
			Vector3f.add(p.pos, d, p.pos);
		}
	}

	public static void fixModel() {
		System.out.println("fixing up the model");

		for (Primitive g : Main.geometry) {
			// only do it for quads
			if (g.isPolygon() && g.numVertices() == 5)
				makeAllPointsCoPlanar(g);
		}

		// TODO: for all lines that look like they might be vertical or
		// horizontal, make them become vertical or horizontal.

		/*
		 * for (Primitive g : Main.geometry) { // only do it for quads if
		 * (g.isPolygon() && g.numVertices() == 5) rectangularify(g); }
		 */
	}

	@SuppressWarnings("unused")
	private static void rectangularify(Primitive g) {
		Vector3f pos_to_change = g.getVertices().get(2).pos;
		Vector3f also_on_line = g.getVertices().get(3).pos;
		Vector3f guide_line_pt1 = g.getVertices().get(0).pos;
		Vector3f guide_line_pt2 = g.getVertices().get(1).pos;

		movePosToBePerpendicular(pos_to_change, also_on_line, guide_line_pt1,
				guide_line_pt2);

		pos_to_change = g.getVertices().get(3).pos;
		also_on_line = g.getVertices().get(0).pos;
		guide_line_pt1 = g.getVertices().get(1).pos;
		guide_line_pt2 = g.getVertices().get(2).pos;

		movePosToBePerpendicular(pos_to_change, also_on_line, guide_line_pt1,
				guide_line_pt2);

		pos_to_change = g.getVertices().get(3).pos;
		also_on_line = g.getVertices().get(2).pos;
		guide_line_pt1 = g.getVertices().get(1).pos;
		guide_line_pt2 = g.getVertices().get(0).pos;

		movePosToBePerpendicular(pos_to_change, also_on_line, guide_line_pt1,
				guide_line_pt2);
	}

	private static void movePosToBePerpendicular(Vector3f pos_to_change,
			Vector3f also_on_line, Vector3f guide_line_pt1,
			Vector3f guide_line_pt2) {

		Vector3f guide_normal = new Vector3f();
		Vector3f.sub(guide_line_pt1, guide_line_pt2, guide_normal);
		guide_normal.normalise();
		float a = guide_normal.x;
		float b = guide_normal.y;
		float c = guide_normal.z;
		float d = -1
				* (a * guide_line_pt2.x + b * guide_line_pt2.y + c
						* guide_line_pt2.z);

		Vector3f p1 = pos_to_change;
		Vector3f p2 = also_on_line;

		Vector3f i = new Vector3f();
		float u_denom = a * (p1.x - p2.x) + b * (p1.y - p2.y) + c
				* (p1.z - p2.z);

		if (u_denom != 0) {
			float u_num = a * p1.x + b * p1.y + c * p1.z + d;
			float u = u_num / u_denom;

			i.x = p1.x + u * (p2.x - p1.x);
			i.y = p1.y + u * (p2.y - p1.y);
			i.z = p1.z + u * (p2.z - p1.z);

		}

		pos_to_change.set(i);
	}

	public static void makeAllPointsCoPlanar(Primitive g) {
		Pellet worst_pellet = null;
		int min_neighbors = Integer.MAX_VALUE;

		for (Pellet p : g.getVertices()) {
			int neighbors = PointStore.queryKdTree(p.pos.x, p.pos.y,
					p.pos.z, p.radius);
			if (neighbors < min_neighbors) {
				min_neighbors = neighbors;
				worst_pellet = p;
			}
		}

		// collect all the points excluding the worst one
		List<Pellet> planar_pellets = new LinkedList<Pellet>();
		for (Pellet p : g.getVertices())
			if (p != worst_pellet) {
				planar_pellets.add(p);
			}

		Vector3f v1 = new Vector3f();
		Vector3f.sub(planar_pellets.get(0).pos, planar_pellets.get(1).pos, v1);
		Vector3f v2 = new Vector3f();
		Vector3f.sub(planar_pellets.get(0).pos, planar_pellets.get(2).pos, v2);
		Vector3f norm = new Vector3f();
		Vector3f.cross(v1, v2, norm);
		if (norm.length() != 0) {
			norm.normalise();

			float a = norm.x;
			float b = norm.y;
			float c = norm.z;
			float d = -1
					* (a * planar_pellets.get(0).pos.x + b
							* planar_pellets.get(0).pos.y + c
							* planar_pellets.get(0).pos.z);

			Vector3f pos = worst_pellet.pos;
			float distance = (float) ((a * pos.x + b * pos.y + c * pos.z + d) / Math
					.sqrt(a * a + b * b + c * c));
			norm.scale(distance);
			Vector3f.sub(pos, norm, pos);
		}
	}
}
