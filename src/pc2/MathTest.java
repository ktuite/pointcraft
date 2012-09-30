package pc2;

import java.util.BitSet;
import java.util.LinkedList;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.geometry.LineScaffold;
import edu.washington.cs.games.ktuite.pointcraft.geometry.PlaneScaffold;

import Jama.Matrix;
import Jama.SingularValueDecomposition;

public class MathTest {

	public static LinkedList<PlaneScaffold> planes = new LinkedList<PlaneScaffold>();
	public static LinkedList<LineScaffold> lines = new LinkedList<LineScaffold>();

	public static void main(String[] args) {
		System.out.println("hello");

		double[][] vals = { { 1, 2, 0 }, { 4, 3, 0 }, { 3, 5, 0 }, { 9, 1, 0 },
				{ 3, 3, 0 }, { 8, 2, 0 } };
		Matrix A = new Matrix(vals);
		SingularValueDecomposition svd = new SingularValueDecomposition(A);
		double[] values = svd.getSingularValues();
		Matrix v = svd.getV();
		System.out.println("svd: " + values[0] + "," + values[1] + ","
				+ values[2]);
		v.print(3, 3);
	}

	public static void fitPlane(BitSet selected_points) {
		// TODO Auto-generated method stub
		double[][] vals = new double[selected_points.cardinality()][3];
		double[] mean = new double[3];

		int num_points = 0;
		for (int i = selected_points.nextSetBit(0); i >= 0; i = selected_points
				.nextSetBit(i + 1)) {
			Vector3f v = PointStore.getIthPoint(i);
			vals[num_points][0] = v.x;
			vals[num_points][1] = v.y;
			vals[num_points][2] = v.z;

			for (int k = 0; k < 3; k++) {
				mean[k] += vals[num_points][k];
			}

			num_points++;
		}

		for (int k = 0; k < 3; k++) {
			mean[k] /= num_points;
		}

		for (int i = 0; i < num_points; i++) {
			for (int k = 0; k < 3; k++) {
				vals[i][k] -= mean[k];
			}
		}

		Matrix A = new Matrix(vals);
		SingularValueDecomposition svd = new SingularValueDecomposition(A);
		Matrix v = svd.getV();
		v.print(3, 3);

		PlaneScaffold plane = new PlaneScaffold();
		plane.a = (float) v.get(0, 2);
		plane.b = (float) v.get(1, 2);
		plane.c = (float) v.get(2, 2);
		plane.d = (float) (-1 * (plane.a * mean[0] + plane.b * mean[1] + plane.c
				* mean[2]));

		plane.center = new Vector3f((float) mean[0], (float) mean[1],
				(float) mean[2]);

		float max_extent = 0;
		for (int i = 0; i < num_points; i++) {
			float extent = 0;
			for (int k = 0; k < 3; k++) {
				extent += vals[i][k] * vals[i][k];
			}
			if (extent > max_extent) {
				max_extent = extent;
			}
		}

		plane.plane_extent = (float) Math.sqrt(max_extent) * 2;
		plane.buildGrid();

		compareAgainstOtherPlanes(plane);
		planes.add(plane);

		System.out.println(plane);
	}

	private static void compareAgainstOtherPlanes(PlaneScaffold plane) {
		for (PlaneScaffold p : planes) {
			LineScaffold line = p.checkForIntersectionPlaneWithPlane(plane);
			if (line != null) {
				int c = countHowManyPointsCloseToLine(line);
				int d = countHowManyPointsCloseToMidpoint(line);
				System.out.println("close pts: " + c);
				if (c > 200 && d > 10) {
					boolean close_to_another_line = false;
					Vector3f mid = (Vector3f) Vector3f.add(line.pt_1, line.pt_2, null).scale(.5f);
					for (LineScaffold l : lines) {
						float dist = l.distanceToPoint(mid);
						if (dist < Paintbrush.radius) {
							close_to_another_line = true;
							System.out
									.println("this line is close to anther line: " + dist + ", radius: " + Paintbrush.radius);
						}
					}
					if (!close_to_another_line) {
						lines.add(line);
					}
				}
			}
		}
	}

	private static int countHowManyPointsCloseToMidpoint(LineScaffold line) {
		Vector3f mid = (Vector3f) Vector3f.add(line.pt_1, line.pt_2, null)
				.scale(.5f);
		return PointStore.getNumPointsInSphere(mid.x, mid.y, mid.z,
				Paintbrush.radius);
	}

	private static int countHowManyPointsCloseToLine(LineScaffold line) {
		int count = 0;
		for (int i = 0; i < PointStore.num_display_points; i++) {
			Vector3f pt = PointStore.getIthPoint(i);
			if (pt != null) {
				float dist_to_line = line.distanceToPoint(pt);
				if (dist_to_line < Paintbrush.radius) {
					count++;
				}
			}
		}
		return count;
	}

	public static void draw() {
		/*
		 * for (PlaneScaffold p : planes) { p.draw(); }
		 */

		for (LineScaffold l : lines) {
			l.draw();
		}

	}
}
