package pc2;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;

import java.nio.FloatBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Sphere;
import org.lwjgl.util.vector.Matrix4f;

import pc2.PointStore;

public class Renderer {

	private static float FOG_COLOR[] = new float[] { .89f, .89f, .89f, 1.0f };

	public static float world_scale = 1f;
	public static float point_size = 2;
	public static float fog_density = 5;
	public static boolean draw_lines = true;
	public static boolean draw_points = true;
	public static boolean draw_scaffolding = true;
	public static boolean draw_pellets = true;
	public static boolean draw_textures = true;
	public static boolean draw_polygons = true;
	public static boolean draw_cameras = false;

	static FloatBuffer rotated_pointcloud_matrix;

	private static int sphere_display_list;

	public static void initGraphics() {
		world_scale = PointStore.world_scale;

		float width = Display.getDisplayMode().getWidth();
		float height = Display.getDisplayMode().getHeight();
		System.out.println("Init Graphics: " + width + "," + height);

		// view matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(60, width / height, .001f, 100000.0f);

		glMatrixMode(GL_MODELVIEW);
		glLoadIdentity();
		rotated_pointcloud_matrix = BufferUtils.createFloatBuffer(16);
		glGetFloat(GL_MODELVIEW_MATRIX, rotated_pointcloud_matrix);

		initSphereDisplayList();

		// depth
		glEnable(GL_DEPTH_TEST);

		// blending
		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		// smooth rendering
		glEnable(GL_SMOOTH);
		glEnable(GL_LINE_SMOOTH);
		glEnable(GL_POLYGON_SMOOTH);

	}

	public static void drawSceneAndGUI() {
		glClearColor(FOG_COLOR[0], FOG_COLOR[1], FOG_COLOR[2], 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glPushMatrix();

		glRotatef(Player.tilt_angle, 1.0f, 0.0f, 0.0f); // rotate up/down
		glRotatef(Player.pan_angle, 0.0f, 1.0f, 0.0f); // rotate left/right

		Skybox.drawSkybox();

		glTranslated(-Player.pos.x, -Player.pos.y, -Player.pos.z);
		glMultMatrix(rotated_pointcloud_matrix);

		if (draw_points)
			drawPoints();
		
		MathTest.draw();

		Paintbrush.draw();

		glPopMatrix();
	}

	private static void drawPoints() {
		glEnable(GL_DEPTH_TEST);
		glEnableClientState(GL_VERTEX_ARRAY);
		glEnableClientState(GL_COLOR_ARRAY);
		glEnableClientState(GL_INDEX_ARRAY);

		GL11.glVertexPointer(3, 0, PointStore.point_positions);
		GL11.glColorPointer(4, true, 0, PointStore.point_colors);

		glPointSize(point_size);
		glDrawElements(GL_POINTS, PointStore.point_indices);

		
		glDisableClientState(GL_COLOR_ARRAY);
		/*
		if (PointStore.highlight_indices != null) {
			glDisable(GL_DEPTH_TEST);
			GL11.glColor4f(1, 1, 1, .7f);
			glDrawElements(GL_POINTS, PointStore.highlight_indices);
		}
		*/
		Paintbrush.drawPoints();

		glDisableClientState(GL_VERTEX_ARRAY);
		glDisable(GL_DEPTH_TEST);
	}

	public static void initSphereDisplayList() {
		Sphere sphere = new Sphere();
		sphere_display_list = glGenLists(1);
		glNewList(sphere_display_list, GL_COMPILE);
		sphere.draw(1, 32, 32);
		glEndList();
	}

	public static void drawSphere(float radius) {
		glPushMatrix();
		glScalef(radius, radius, radius);
		glCallList(sphere_display_list);
		glPopMatrix();
	}

	public static void changePointSize(int i) {
		point_size += i;
		if (point_size > 10)
			point_size = 10;
		else if (point_size < 1)
			point_size = 1;
	}

	public static void changeFog(int i) {
		fog_density -= i / world_scale;
		if (fog_density < 0)
			fog_density = 0;
		else if (fog_density > 50 / world_scale)
			fog_density = 50 / world_scale;
		glFogf(GL_FOG_DENSITY, fog_density);
	}
	
	public static void makeCurrentPositionOrigin() {
		Matrix4f rot = new Matrix4f();

		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();

		glRotatef(Player.tilt_angle, 1.0f, 0.0f, 0.0f); // rotate our camera up/down
		glRotatef(Player.pan_angle, 0.0f, 1.0f, 0.0f); // rotate our camera left/right
		glTranslated(-Player.pos.x, -Player.pos.y, -Player.pos.z); // translate the screen
		glMultMatrix(rotated_pointcloud_matrix);

		glGetFloat(GL_MODELVIEW_MATRIX, rotated_pointcloud_matrix);
		glPopMatrix();
		System.out.println("New matrix: ");

		rot.load(rotated_pointcloud_matrix);
		rotated_pointcloud_matrix.rewind();
		rot.invert();
		rotated_pointcloud_matrix.rewind();
		Player.pos.set(0, 0, 0);
		Player.pan_angle = 0;
		Player.tilt_angle = 0;
	}
}
