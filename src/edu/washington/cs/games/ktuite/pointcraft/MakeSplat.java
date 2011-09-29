package edu.washington.cs.games.ktuite.pointcraft;

import java.nio.FloatBuffer;

import org.lwjgl.util.vector.Vector3f;

public class MakeSplat implements Runnable  {

	public Vector3f pos;
	public float radius;
	public Pellet pellet;
	
	public MakeSplat(Vector3f _pos, float _radius, Pellet _pellet){
		pos = _pos;
		radius = _radius;
		pellet = _pellet;
		System.out.println("doing some construction here: " + pos);
		int neighbors = LibPointCloud.queryKdTree(pos.x, pos.y, pos.z, radius);
		System.out.println("number of neighbors: " + neighbors);
	}
	
	@Override
	public void run() {
		System.out.println("generating some geometry!");
		LibPointCloud.makeSplat(pos.x, pos.y, pos.z, radius);
		int num_points = LibPointCloud.getVertexCount();
		System.out.println("number of vertices: " + num_points);
		FloatBuffer geom_buffer = LibPointCloud.getVertices().getByteBuffer(0, num_points * 3 * 4).asFloatBuffer();
		pellet.alive = false;
		Main.geometry.add(new Geometry(geom_buffer, num_points));
	}

}
