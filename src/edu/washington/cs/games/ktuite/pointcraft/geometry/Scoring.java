package edu.washington.cs.games.ktuite.pointcraft.geometry;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import edu.washington.cs.games.ktuite.pointcraft.PointStore;

public class Scoring {
	public static int points_explained = 0;

	private static Vector3f calcCrossProd(Vector3f va, Vector3f vb)
	{
		Vector3f vc = new Vector3f(); 
		vc.x = va.x*vb.y - va.y*vb.x;
		vc.y = va.y*vb.z - va.z*vb.y;
		vc.z = va.z*vb.x - va.z*vb.z;
		return vc;
	}

	private static Vector3f computeCentroid(Vector3f a, Vector3f b, Vector3f c)
	{
		Vector3f result = new Vector3f();
		result.x = (a.x + b.x + c.x) / 3.0f;
		result.y = (a.y + b.y + c.y) / 3.0f;
		result.x = (a.z + b.z + c.z) / 3.0f;
		return result;
	}
	
	private static Vector3f computeNormal(Vector3f v0, Vector3f v1, Vector3f v2)
	{
		Vector3f va = new Vector3f();
		Vector3f vb = new Vector3f();
		va.x = v1.x - v0.x;
		va.y = v1.y - v0.y;
		va.z = v1.z - v0.z;
		va.normalise();
		vb.x = v2.x - v0.x;
		vb.y = v2.y - v0.y;
		vb.z = v2.z - v0.z;
		vb.normalise();
		return calcCrossProd(va, vb);
	}
	
	private static double distanceSq(double ax, double ay, double az,
			double bx, double by, double bz)
	{
		double dx = ax - bx;
		double dy = ay - by;
		double dz = az - bz;
		return (dx*dx + dy*dy + dz*dz);
	}

	private static double distance(Vector3f a, Vector3f b)
	{
		return Math.sqrt(distanceSq(a.x, a.y, a.z, b.x, b.y, b.z));
	}

	private static double distanceToPoly(Vector3f a, Vector3f c, Vector3f n)
	{
		Vector3f d = new Vector3f(a.x - c.x, a.y - c.y, a.z - c.z);
		return (n.x * d.x) + (n.y * d.y) + (n.z * d.z);
	}

	private static double distanceToPolyPlane(Vector4f plane, Vector3f pt)
	{
		return (plane.x * pt.x) + (plane.y * pt.y) + (plane.z * pt.z) - plane.w;
	}
	
	private static Vector4f computePolyPlane(Vector3f a, Vector3f b, Vector3f c)
	{
		double x1 = a.x, x2 = b.x, x3 = c.x;
		double y1 = a.y, y2 = b.y, y3 = c.y;
		double z1 = a.z, z2 = b.z, z3 = c.z;

		/*
				| x1 y1 z1 |-1             |   z3y2-y3z2  -(z3y1-y3z1)   z2y1-y2z1  |
				| x2 y2 z2 |    =  1/DET * | -(z3x2-x3z2)   z3x1-x3z1  -(z2x1-x2z1) |
				| x3 y3 z3 |               |   y3x2-x3y2  -(y3x1-x3y1)   y2x1-x2y1  |

				with DET  =  x1(z3y2-y3z2)-x2(z3y1-y3z1)+x3(z2y1-y2z1)	
		 */

		double recip_DET = 1 / ((x1*(z3*y2-y3*z2)) - (x2*(z3*y1-y3*z1)) + (x3*(z2*y1-y2*z1)));
		
		double A = recip_DET * ((z3*y2-y3*z2) - (z3*y1 - y3*z1) + (z2*y1 - y2*z1));
		double B = recip_DET * (-(z3*x2-x3*z2) + (z3*x1 - x3*z1) - (z2*x1 - x2*z1));
		double C = recip_DET * ((y3*x2 - x3*y2) - (y3*x1 - x3*y1) + (y2*x1 - x2*y1));
		double D = A*x1 + B*y1 + C*z1;
		Vector4f r = new Vector4f((float)A, (float)B, (float)C, (float)D);
		return r;
	}

	private	static Vector2f barycentricCoords(double x, double y, double z,
			double ax, double ay, double az,
			double bx, double by, double bz,
			double cx, double cy, double cz)
	{
	    double v0x, v0y, v0z, v1x, v1y, v1z, v2x, v2y, v2z;
	    v0x = cx - ax; v0y = cy - ay; v0z = cz - az;
	    v1x = bx - ax; v1y = by - ay; v1z = bz - az;
	    v2x =  x - ax; v2y =  y - ay; v2z =  z - az;

	    double d00, d01, d02, d11, d12;
	    d00 = v0x*v0x + v0y*v0y + v0z*v0z;
	    d01 = v0x*v1x + v0y*v1y + v0z*v1z;
	    d02 = v0x*v2x + v0y*v2y + v0z*v2z;
	    d11 = v1x*v1x + v1y*v1y + v1z*v1z;
	    d12 = v1x*v2x + v1y*v2y + v1z*v2z;

	    double D = d00*d11 - d01*d01;

	    return new Vector2f((float)((d11*d02 - d01*d12) / D), (float)((d00*d12 - d01*d02) / D));
	}

	private static boolean pointInTri(Vector3f v, Vector3f a, Vector3f b, Vector3f c, double epsilon)
	{
	    Vector2f bc = barycentricCoords(v.x, v.y, v.z, a.x, a.y, a.z, b.x, b.y, b.z, c.x, c.y, c.z);
	    return ((bc.x > -epsilon) && (bc.y > -epsilon) && (bc.x + bc.y < 1 + (2*epsilon)));
	}
	
	public static double computeTextureScore(Primitive p)
	{
		double epsilon1 = 0.01, epsilon2 = 0.1;
		int count = 0;
		for (int h = 0; h < p.numVertices() - 3; h++) {
			Vector3f a = p.getVertices().get(0).pos;
			Vector3f b = p.getVertices().get(h + 1).pos;
			Vector3f c = p.getVertices().get(h + 2).pos;
			Vector3f ct = computeCentroid(a, b, c);
			Vector3f n = computeNormal(a, b, c);
			Vector4f pl = computePolyPlane(a, b, c);

			for (int i = 0; i < PointStore.num_points; i++) {
				if (PointStore.point_properties.get(i) == 0) {
					Vector3f v = PointStore.getIthPoint(i);
					if ((Math.abs(distanceToPolyPlane(pl, v)) < epsilon2) && pointInTri(v, a, b, c, epsilon1)) {
						count++;
						PointStore.point_properties.put(i, (byte) 1);
						PointStore.changePointColorToRed(i);
					}
				}
			}
		}
		points_explained += count;
		return ((double) count) / 100.0;
	}
}
