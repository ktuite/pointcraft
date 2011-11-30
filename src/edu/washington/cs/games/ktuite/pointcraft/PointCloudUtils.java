package edu.washington.cs.games.ktuite.pointcraft;

import java.util.ArrayList;

import toxi.geom.PointOctree;
import toxi.geom.Vec3D;

public class PointCloudUtils {

	public static void main(String[] args){
		PointOctree tree = new PointOctree(new Vec3D(0,0,0), 1f);
		tree.setMinNodeSize(0.125f);
		System.out.println(tree.addPoint(new Vec3D(.2f, .1f, .4f)));
		ArrayList<Vec3D> results = tree.getPointsWithinSphere(new Vec3D(.5f, .5f, .5f), 1);
		if (results != null)
			System.out.println(results.get(0));
		else 
			System.out.println("NO RESUTSL!!!");
	}
}
