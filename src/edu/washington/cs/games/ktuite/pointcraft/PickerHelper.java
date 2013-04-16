package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.gluPerspective;
import static org.lwjgl.util.glu.GLU.gluPickMatrix;

import java.nio.IntBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;

import edu.washington.cs.games.ktuite.pointcraft.geometry.Primitive;
import edu.washington.cs.games.ktuite.pointcraft.tools.Pellet;

public class PickerHelper {
	
	public static int picked_polygon = -1;

	static int processHits(int hits, int buffer[]) {
		int names, ptr = 0;
	
		int selected_geometry = -1;
		int min_dist = Integer.MAX_VALUE;
	
		// System.out.println("hits = " + hits);
		// ptr = (GLuint *) buffer;
		for (int i = 0; i < hits; i++) { /* for each hit */
			names = buffer[ptr];
			// System.out.println(" number of names for hit = " + names);
			ptr++;
			// System.out.println("  z1 is " + buffer[ptr]);
			int temp_min_dist = buffer[ptr];
			ptr++;
			// System.out.println(" z2 is " + buffer[ptr]);
			ptr++;
	
			// System.out.print("\n   the name is ");
			for (int j = 0; j < names; j++) { /* for each name */
				// System.out.println("" + buffer[ptr]);
				if (temp_min_dist < min_dist) {
					min_dist = temp_min_dist;
					selected_geometry = buffer[ptr];
				}
				ptr++;
			}
			// System.out.println();
		}
	
		return selected_geometry;
	}

	static void pickPolygon() {
		int x = Display.getDisplayMode().getWidth() / 2;
		int y = Display.getDisplayMode().getHeight() / 2;
		final int BUFSIZE = 512;
		int[] selectBuf = new int[BUFSIZE];
		IntBuffer selectBuffer = BufferUtils.createIntBuffer(BUFSIZE);
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		int hits;
	
		glGetInteger(GL_VIEWPORT, viewport);
		glSelectBuffer(selectBuffer);
		glRenderMode(GL_SELECT);
	
		glInitNames();
		glPushName(-1);
	
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		/* create 5x5 pixel picking region near cursor location */
		gluPickMatrix((float) x, (float) (viewport.get(3) - y), 5.0f, 5.0f,
				viewport);
		gluPerspective(60, Display.getDisplayMode().getWidth()
				/ Display.getDisplayMode().getHeight(), .001f, 1000.0f);
	
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glRotatef(Main.tilt_angle, 1.0f, 0.0f, 0.0f); // rotate our camera up/down
		glRotatef(Main.pan_angle, 0.0f, 1.0f, 0.0f); // rotate our camera left/right
		glTranslated(-Main.pos.x, -Main.pos.y, -Main.pos.z); // translate the screen
		glMultMatrix(Main.rotated_pointcloud_matrix);
		
		// draw polygons for picking
		for (int i = 0; i < Main.geometry.size(); i++) {
			Primitive g = Main.geometry.get(i);
			if (g.isPolygon()) {
				glLoadName(i);
				g.draw();
			}
		}
	
		glPopMatrix();
		glMatrixMode(GL_PROJECTION);
	
		glPopMatrix();
		glFlush();
	
		hits = glRenderMode(GL_RENDER);
		selectBuffer.get(selectBuf);
		picked_polygon = processHits(hits, selectBuf); // which polygon actually
														// selected
	}

	public static int pickPellet() {
		int x = Display.getDisplayMode().getWidth() / 2;
		int y = Display.getDisplayMode().getHeight() / 2;
		final int BUFSIZE = 512;
		int[] selectBuf = new int[BUFSIZE];
		IntBuffer selectBuffer = BufferUtils.createIntBuffer(BUFSIZE);
		IntBuffer viewport = BufferUtils.createIntBuffer(16);
		int hits;
	
		glGetInteger(GL_VIEWPORT, viewport);
		glSelectBuffer(selectBuffer);
		glRenderMode(GL_SELECT);
		glDisable(GL_FOG);
	
		glInitNames();
		glPushName(-1);
	
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		/* create 5x5 pixel picking region near cursor location */
		gluPickMatrix((float) x, (float) (viewport.get(3) - y), 5.0f, 5.0f,
				viewport);
		gluPerspective(60, Display.getDisplayMode().getWidth()
				/ Display.getDisplayMode().getHeight(),.001f, 100000.0f);
		
	
		glMatrixMode(GL_MODELVIEW);
		glPushMatrix();
		glRotatef(Main.tilt_angle, 1.0f, 0.0f, 0.0f); // rotate our camera up/down
		glRotatef(Main.pan_angle, 0.0f, 1.0f, 0.0f); // rotate our camera left/right
		glTranslated(-Main.pos.x, -Main.pos.y, -Main.pos.z); // translate the screen
		glMultMatrix(Main.rotated_pointcloud_matrix);
		
		// draw polygons for picking
		for (int i = 0; i < Main.all_pellets_in_world.size(); i++) {
			Pellet pellet = Main.all_pellets_in_world.get(i);
			if (pellet.alive) {
				if (pellet.visible) {
					glPushMatrix();
					glTranslatef(pellet.pos.x, pellet.pos.y, pellet.pos.z);
					glLoadName(i);
					pellet.coloredDraw();
					glPopMatrix();
				}
			}
		}
	
		glPopMatrix();
		glMatrixMode(GL_PROJECTION);
	
		glPopMatrix();
		glFlush();
	
		hits = glRenderMode(GL_RENDER);
		selectBuffer.get(selectBuf);
		
		glMatrixMode(GL_MODELVIEW);
		return processHits(hits, selectBuf); 
	
	}

}
