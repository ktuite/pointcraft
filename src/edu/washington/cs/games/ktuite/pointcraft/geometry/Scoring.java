package edu.washington.cs.games.ktuite.pointcraft.geometry;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.PointStore;

public class Scoring {
	public static int points_explained = 0;
	
	private static Vector2f barycentricCoords(double x, double y, double z,
			double ax, double ay, double az, double bx, double by, double bz,
			double cx, double cy, double cz) {
		double v0x, v0y, v0z, v1x, v1y, v1z, v2x, v2y, v2z;
		v0x = cx - ax;
		v0y = cy - ay;
		v0z = cz - az;
		v1x = bx - ax;
		v1y = by - ay;
		v1z = bz - az;
		v2x = x - ax;
		v2y = y - ay;
		v2z = z - az;

		double d00, d01, d02, d11, d12;
		d00 = v0x * v0x + v0y * v0y + v0z * v0z;
		d01 = v0x * v1x + v0y * v1y + v0z * v1z;
		d02 = v0x * v2x + v0y * v2y + v0z * v2z;
		d11 = v1x * v1x + v1y * v1y + v1z * v1z;
		d12 = v1x * v2x + v1y * v2y + v1z * v2z;

		double D = d00 * d11 - d01 * d01;

		return new Vector2f((float) ((d11 * d02 - d01 * d12) / D),
				(float) ((d00 * d12 - d01 * d02) / D));
	}

	private static boolean pointInTri(double x, double y, double z, double ax,
			double ay, double az, double bx, double by, double bz, double cx,
			double cy, double cz, double epsilon) {
		Vector2f bc = barycentricCoords(x, y, z, ax, ay, az, bx, by, bz, cx,
				cy, cz);
		return ((bc.x > -epsilon) && (bc.y > -epsilon) && (bc.x + bc.y < 1 + (2 * epsilon)));
	}

	public static double computeTextureScore(Primitive p) {
		Vector3f a = p.getVertices().get(0).pos;
		Vector3f b = p.getVertices().get(1).pos;
		Vector3f c = p.getVertices().get(2).pos;
		double epsilon = 0.05;
		int count = 0;
		for (int i = 0; i < PointStore.num_points; i++) {
			Vector3f v = PointStore.getIthPoint(i);
			if (pointInTri(v.x, v.y, v.z, a.x, a.y, a.z, b.x, b.y, b.z, c.x,
					c.y, c.z, epsilon)) {
				count++;
			}
		}
		points_explained += count;
		return ((double) count) / 1000.0;
	}
}
