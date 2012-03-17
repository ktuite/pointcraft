package edu.washington.cs.games.ktuite.pointcraft;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.tools.CameraGun;
import edu.washington.cs.games.ktuite.pointcraft.tools.Pellet;

public class TextureMaker implements Runnable {

	private Vector3f[] world_vertices;
	private final int n_x = 64;
	private final int n_y = 64;
	private byte[] uncompressed_data;
	private Primitive geom;
	private static ExecutorService executor = Executors.newFixedThreadPool(Math
			.max(1, Runtime.getRuntime().availableProcessors() - 1));

	public static void makeTexture(final Primitive geom) {
		executor.execute(new TextureMaker(geom));
	}

	private TextureMaker(Primitive geom) {
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

		uncompressed_data = new byte[n_x * n_y * 3];
	}

	@Override
	public void run() {
		// tryFillingDataWithRandomCrap();
		samplePointCloud();
		packageUpForPrimitive();
	}

	private void samplePointCloud() {
		for (int i = 0; i < n_x; i++) {
			for (int j = 0; j < n_y; j++) {
				float alpha = (i + 0.5f) / n_x;
				float beta = 1 - (j + 0.5f) / n_y;

				/*
				 * uncompressed_data[3 * (n_x * j + i) + 0] = (byte)(alpha *
				 * 255); uncompressed_data[3 * (n_x * j + i) + 1] = (byte)(beta
				 * * 255); uncompressed_data[3 * (n_x * j + i) + 2] =
				 * (byte)(255);
				 */

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

				float radius = (float) (Pellet.default_radius );

				/*
				 * single point mode int idx =
				 * PointStore.getNearestPoint(interp.x, interp.y, interp.z,
				 * radius);
				 * 
				 * uncompressed_data[3 * (n_x * j + i) + 0] =
				 * PointStore.point_colors .get(idx * 3 + 0);
				 * uncompressed_data[3 * (n_x * j + i) + 1] =
				 * PointStore.point_colors .get(idx * 3 + 1);
				 * uncompressed_data[3 * (n_x * j + i) + 2] =
				 * PointStore.point_colors .get(idx * 3 + 2);
				 */

				int[] indices = PointStore.getNearestPoints(interp.x, interp.y,
						interp.z, radius);
				if (indices != null) {
					long r = 0;
					long g = 0;
					long b = 0;
					float tot_weight = 0;

					for (int k = 0; k < indices.length; k++) {
						int idx = indices[k];
						Vector3f otherPt = new Vector3f(
								PointStore.point_positions.get(idx * 3 + 0),
								PointStore.point_positions.get(idx * 3 + 1),
								PointStore.point_positions.get(idx * 3 + 2));
						Vector3f.sub(otherPt, interp, otherPt);
						float w = 1/otherPt.length();
						r += w*(short)(PointStore.point_colors.get(idx * 4 + 0)& 0xFF);
						g += w*(short)(PointStore.point_colors.get(idx * 4 + 1)& 0xFF);
						b += w*(short)(PointStore.point_colors.get(idx * 4 + 2)& 0xFF);
						tot_weight += w;
					}

					r /= tot_weight;
					g /= tot_weight;
					b /= tot_weight;

					uncompressed_data[3 * (n_x * j + i) + 0] = (byte) r;
					uncompressed_data[3 * (n_x * j + i) + 1] = (byte) g;
					uncompressed_data[3 * (n_x * j + i) + 2] = (byte) b;
				}
			}
		}
	}

	private void packageUpForPrimitive() {
		ByteBuffer buf = BufferUtils.createByteBuffer(n_x * n_y * 3);
		buf.put(uncompressed_data);

		BufferedImage bi = CameraGun.transformPixelsRGBBuffer2ARGB_ByHand(buf,
				n_x, n_y);

		ByteArrayOutputStream bout = new ByteArrayOutputStream(n_x * n_y * 3);
		try {
			ImageIO.write(bi, "png", bout);
			geom.texture_data.set(0, bout.toByteArray());
			geom.texture_count++;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@SuppressWarnings("unused")
	private void tryFillingDataWithRandomCrap() {
		for (int i = 0; i < n_x * n_y * 3; i++) {
			uncompressed_data[i] = (byte) (Math.random() * 255);
		}
	}

}
