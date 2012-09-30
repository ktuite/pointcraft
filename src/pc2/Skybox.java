package pc2;

import static org.lwjgl.opengl.GL11.*;

import java.io.IOException;

import org.newdawn.slick.Color;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;


public class Skybox {

	public static Skybox skybox;
	private Texture skybox_texture;

	public Skybox(){
		// skybox texture loaded
		try {
			skybox_texture = TextureLoader.getTexture("JPG",
					ResourceLoader.getResourceAsStream("gray_sky_orig.jpg"));
			/*
			System.out.println("Texture loaded: " + skybox_texture);
			System.out.println(">> Image width: " + skybox_texture.getImageWidth());
			System.out.println(">> Image height: " + skybox_texture.getImageHeight());
			System.out.println(">> Texture width: " + skybox_texture.getTextureWidth());
			System.out.println(">> Texture height: "
					+ skybox_texture.getTextureHeight());
			System.out.println(">> Texture ID: " + skybox_texture.getTextureID());
			*/
			
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Couldn't load skybox");
			System.exit(1);
		}
	}

	private void draw() {
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_FOG);
		glDisable(GL_DEPTH_TEST);

		Color.white.bind();
		skybox_texture.bind();
		glPointSize(10);
		float s = .1f;
		glBegin(GL_QUADS);
		// tex coords of .99 and .01 used here and there
		// to prevent wrap around and dark edges on light things

		// top
		glTexCoord2f(.75f, 0.01f);
		glVertex3f(s, s, s);
		glTexCoord2f(.5f, 0.01f);
		glVertex3f(-s, s, s);
		glTexCoord2f(.5f, .5f);
		glVertex3f(-s, s, -s);
		glTexCoord2f(.75f, .5f);
		glVertex3f(s, s, -s);

		// one side....
		glTexCoord2f(0f, .5f);
		glVertex3f(s, s, s);
		glTexCoord2f(.25f, .5f);
		glVertex3f(-s, s, s);
		glTexCoord2f(.25f, .99f);
		glVertex3f(-s, -s, s);
		glTexCoord2f(0f, .99f);
		glVertex3f(s, -s, s);

		// two side....
		glTexCoord2f(.25f, .5f);
		glVertex3f(-s, s, s);
		glVertex3f(-s, s, -s);
		glTexCoord2f(.5f, .99f);
		glVertex3f(-s, -s, -s);
		glTexCoord2f(.25f, .99f);
		glVertex3f(-s, -s, s);

		// red side.... (third side)
		glTexCoord2f(.5f, .5f);
		glVertex3f(-s, s, -s);
		glTexCoord2f(.75f, .5f);
		glVertex3f(s, s, -s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, -s);
		glTexCoord2f(.5f, .99f);
		glVertex3f(-s, -s, -s);

		// blue side.... (fourth side)
		glTexCoord2f(.75f, .5f);
		glVertex3f(s, s, -s);
		glTexCoord2f(1.0f, .5f);
		glVertex3f(s, s, s);
		glTexCoord2f(1.0f, .99f);
		glVertex3f(s, -s, s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, -s);

		// down side....
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(-s, -s, s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(-s, -s, -s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, -s);
		glEnd();

		glDisable(GL_TEXTURE_2D);
		// glEnable(GL_FOG);
		glEnable(GL_DEPTH_TEST);
	}
	
	public static void drawSkybox(){
		if (skybox == null){
			skybox = new Skybox();
		}
		skybox.draw();
	}

}
