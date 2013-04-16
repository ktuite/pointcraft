package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedList;

import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;

import static org.lwjgl.opengl.GL11.*;

import edu.washington.cs.games.ktuite.pointcraft.PointStore.Camera;

public class FaceManager {

	public static LinkedList<Face> faces = new LinkedList<Face>();

	public static void loadFacesFromFile(String filename) {
		try {
			BufferedReader br = new BufferedReader(new FileReader(filename));
			String line;
			while ((line = br.readLine()) != null) {
				String[] arr = line.split(" ");
				String imagePath = arr[0];
				int numFaces = (arr.length - 1) / 4;
				for (int i = 0; i < numFaces; i++) {
					Face f = new Face(imagePath, arr[i * 4 + 1],
							arr[i * 4 + 2], arr[i * 4 + 3], arr[i * 4 + 4]);
					faces.add(f);
				}
			}
			br.close();

		} catch (FileNotFoundException e) {
			System.out
					.println("Face file not found, not loading at this time.");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static class Face {

		public String filename;
		public float top, left, bottom, right;
		public Vector2f center;
		public Vector3f faceOnImagePlane;
		public Camera camera;

		public Face(String filename, String left, String top, String right,
				String bottom) {
			System.out.println("loading face for file: " + filename);
			this.filename = filename;
			this.top = Float.parseFloat(top);
			this.left = Float.parseFloat(left);
			this.bottom = Float.parseFloat(bottom);
			this.right = Float.parseFloat(right);

			center = new Vector2f((this.left + this.right) / 2f,
					(this.top + this.bottom) / 2f);
			System.out.println("face center:" + center.x + "," + center.y);
			camera = PointStore.cameraMap.get(filename);
			if (camera != null) {
				camera.draw = true;

				// some bilaterial interpolation here
				Vector3f br = new Vector3f(camera.pt3);
				br.scale(center.x);
				Vector3f bl = new Vector3f(camera.pt0);
				bl.scale(1 - center.x);
				Vector3f b = Vector3f.add(bl, br, null);

				Vector3f tr = new Vector3f(camera.pt2);
				tr.scale(center.x);
				Vector3f tl = new Vector3f(camera.pt1);
				tl.scale(1 - center.x);
				Vector3f t = Vector3f.add(tl, tr, null);

				Vector3f imageBottom = new Vector3f(b);
				Vector3f imageTop = new Vector3f(t);
				
				// know top and bottom positions... 
				Vector3f imageHorizon = Vector3f.add(imageBottom, imageTop, null);
				imageHorizon.scale(.5f);
				
				Vector3f faceTop = Vector3f.add((Vector3f)imageTop.scale(this.top), (Vector3f)imageBottom.scale(1-this.top), null);
				
				imageTop.set(t);
				imageBottom.set(b);
				Vector3f faceBottom = Vector3f.add((Vector3f)imageTop.scale(this.bottom), (Vector3f)imageBottom.scale(1-this.bottom), null);
				
				Vector3f.sub(imageHorizon, camera.center, imageHorizon);
				Vector3f.sub(faceTop, camera.center, faceTop);
				Vector3f.sub(faceBottom, camera.center, faceBottom);
				
				float angleAlpha = Vector3f.angle(faceTop, faceBottom);
				float angleBeta = Vector3f.angle(faceTop, imageHorizon);
				
				double distanceInMeters = .4f / Math.sin(angleAlpha) * Math.sin(angleBeta); // in meters
				System.out.println("Distance to face in meters: " + distanceInMeters);
				float distanceInModelUnits = (float) (distanceInMeters * 0.006647056698124502); //hard coded for trevi 
				
				// back to our originally scheduled program
				// pointing at the actual face on the image plane
				b.scale(center.y);
				t.scale(1 - center.y);

				faceOnImagePlane = Vector3f.add(b, t, null);
				
				Vector3f scalableVectorToFace = Vector3f.sub(faceOnImagePlane, camera.center, null);
				scalableVectorToFace.normalise();
				scalableVectorToFace.scale(distanceInModelUnits);
				Vector3f.add(scalableVectorToFace, camera.center, scalableVectorToFace);
				
				faceOnImagePlane = scalableVectorToFace;
			}
		}

		public void drawCameraToImagePlanePoint() {
			if (faceOnImagePlane != null) {
				glColor3f(0, 0, 0);
				glBegin(GL_LINES);
				glVertex3f(camera.center.x, camera.center.y, camera.center.z);
				glVertex3f(faceOnImagePlane.x, faceOnImagePlane.y,
						faceOnImagePlane.z);
				glEnd();
			}
		}
	}

	public static void drawFaces() {
		for (Face f : faces) {
			f.drawCameraToImagePlanePoint();
		}
	}

}
