package edu.washington.cs.games.ktuite.pointcraft.geometry;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.PointStore;
import edu.washington.cs.games.ktuite.pointcraft.tools.Pellet;

public class PolygonSampler{

	private Vector3f[] world_vertices;
	private final int n_x = 64;
	private final int n_y = 64;
	private Primitive geom;


	private PolygonSampler(Primitive geom) {
		world_vertices = new Vector3f[4];
		world_vertices[0] = new Vector3f(geom.getVertices().get(0).pos);
		world_vertices[1] = new Vector3f(geom.getVertices().get(1).pos);
		world_vertices[2] = new Vector3f(geom.getVertices().get(2).pos);
		if (geom.numVertices() > 4) {
			world_vertices[3] = geom.getVertices().get(3).pos;
		} else {
			Vector3f beta = new Vector3f();
			Vector3f.sub(world_vertices[0], world_vertices[1], beta);
			Vector3f.add(beta, world_vertices[2], beta);
			world_vertices[3] = beta;
		}

		this.geom = geom;

	}
	
	public void makePoints() {
		samplePointCloud();
	}

	private void samplePointCloud() {
		float radius = (float) (Pellet.default_radius);
		LinkedList<Vector3f> new_points = new LinkedList<Vector3f>();
		LinkedList<Vector3f> new_normals = new LinkedList<Vector3f>();
		
		for (int i = 0; i < n_x; i++) {
			for (int j = 0; j < n_y; j++) {
				float alpha = (i + 0.5f) / n_x;
				float beta = 1 - (j + 0.5f) / n_y;

				// todo: do something if it's a triangle... 
			
				Vector3f top_interp = new Vector3f();
				Vector3f.sub(world_vertices[1], world_vertices[0], top_interp);
				top_interp.scale(alpha);
				Vector3f.add(top_interp, world_vertices[0], top_interp);

				Vector3f bottom_interp = new Vector3f();
				Vector3f.sub(world_vertices[2], world_vertices[3],
						bottom_interp);
				bottom_interp.scale(alpha);
				Vector3f.add(bottom_interp, world_vertices[3], bottom_interp);

				Vector3f interp = new Vector3f();
				Vector3f.sub(bottom_interp, top_interp, interp);
				interp.scale(beta);
				Vector3f.add(interp, top_interp, interp);

				int[] indices = PointStore.getNearestPoints(interp.x, interp.y,
						interp.z, radius*3f);
				
				if (indices == null || indices.length == 0) {
					// no points near by! add one at this place
					new_points.push(interp);
					new_normals.push(geom.normal);
				}
			}
		}
		
		if (new_points.size() > 0){
			int new_num_points = PointStore.num_points + new_points.size();
			System.out.println("number of new points" + new_points.size() + " number of total points now:" +  new_num_points);
			FloatBuffer new_point_positions = BufferUtils.createFloatBuffer(new_num_points * 3);
			FloatBuffer new_point_normals = BufferUtils.createFloatBuffer(new_num_points * 3);
			ByteBuffer new_point_colors = BufferUtils.createByteBuffer(new_num_points * 4);
			new_point_positions.rewind();
			new_point_colors.rewind();
			new_point_normals.rewind();
			
			PointStore.point_positions.rewind();
			PointStore.point_colors.rewind();
			PointStore.point_normals.rewind();
			new_point_positions.put(PointStore.point_positions);
			new_point_colors.put(PointStore.point_colors);
			new_point_normals.put(PointStore.point_normals);
			
			for (int i = 0; i < new_points.size(); i++) {
				new_point_colors.put((i + PointStore.num_points) * 4 + 0, (byte) 0);
				new_point_colors.put((i + PointStore.num_points) * 4 + 1, (byte) 155);
				new_point_colors.put((i + PointStore.num_points) * 4 + 2, (byte) 255);
				new_point_colors.put((i + PointStore.num_points) * 4 + 3, (byte) 255);
				new_point_positions.put((i + PointStore.num_points)*3 + 0, new_points.get(i).x);
				new_point_positions.put((i + PointStore.num_points)*3 + 1, new_points.get(i).y);
				new_point_positions.put((i + PointStore.num_points)*3 + 2, new_points.get(i).z);
				new_point_normals.put((i + PointStore.num_points)*3 + 0, new_normals.get(i).x);
				new_point_normals.put((i + PointStore.num_points)*3 + 1, new_normals.get(i).y);
				new_point_normals.put((i + PointStore.num_points)*3 + 2, new_normals.get(i).z);
			}
			new_point_colors.rewind();
			new_point_positions.rewind();
			new_point_normals.rewind();
			
			PointStore.point_normals = new_point_normals;
			PointStore.point_colors = new_point_colors;
			PointStore.point_positions = new_point_positions;
			PointStore.num_points = new_num_points;
		}
	}
	
	public static void sampleAllPolygons(){
		for (Primitive geom : Main.geometry) {
			PolygonSampler ps = new PolygonSampler(geom);
			ps.makePoints();
		}
	}

}
