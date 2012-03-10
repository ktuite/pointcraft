package edu.washington.cs.games.ktuite.pointcraft.geometry;

import static org.lwjgl.opengl.GL11.*;

public class Ground {

	public static boolean enabled = false;
	public static boolean impenetrable = true;
	public static float height = -0.01f;
	public static float extent = 1f;

	public static void setHeight(float h) {
		height = h;
	}

	public static void draw() {
		float height2 = height - 0.001f;
		int n = 20;
		float width = ((float) extent) / n;

		glColor3f(.9f, .9f, .9f);
		for (int i = 0; i < n; i+=2) {
			for (int j = 0; j < n; j+=1) {
				float x = ((float) i / n - .5f) * extent;
				float y = ((float) j / n - .5f) * extent;
				
				if (j % 2 == 1){
					x += width;
				}
				
				glBegin(GL_POLYGON);
				glVertex3f(x, height2, y);
				glVertex3f(x, height2, y + width);
				glVertex3f(x + width, height2, y + width);
				glVertex3f(x + width, height2, y);
				glEnd();
			}
		}
		
		glColor3f(.7f, .7f, .75f);
		for (int i = 0; i < n; i+=2) {
			for (int j = 0; j < n; j+=1) {
				float x = ((float) i / n - .5f) * extent;
				float y = ((float) j / n - .5f) * extent;
				
				if (j % 2 == 0){
					x += width;
				}
				
				glBegin(GL_POLYGON);
				glVertex3f(x, height2, y);
				glVertex3f(x, height2, y + width);
				glVertex3f(x + width, height2, y + width);
				glVertex3f(x + width, height2, y);
				glEnd();
			}
		}

		/*
		// grid lines
		glLineWidth(1f);
		glColor3f(.9f, .9f, .9f);
		for (int i = 0; i < n; i+=2) {
			for (int j = 0; j < n; j+=1) {
				float x = ((float) i / n - .5f) * extent;
				float y = ((float) j / n - .5f) * extent;
				
				if (j % 2 == 1){
					x += width;
				}
				
				glBegin(GL_LINE_LOOP);
				glVertex3f(x, height2, y);
				glVertex3f(x, height2, y + width);
				glVertex3f(x + width, height2, y + width);
				glVertex3f(x + width, height2, y);
				glEnd();
			}
		}
		*/
	
	}
}
