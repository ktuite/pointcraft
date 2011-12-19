package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import javax.imageio.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;

public class CameraGun {
	
	static int count = 1;

	public static void takeSnapshot(Main main) {
		main.renderForCamera();
		glReadBuffer(GL_FRONT);
		int width = Display.getDisplayMode().getWidth();
		int height = Display.getDisplayMode().getHeight();
		int buf_size = width * height * 3;
		ByteBuffer pixels = BufferUtils.createByteBuffer(buf_size);
		glReadPixels(0, 0, width, height, GL_RGB, GL_UNSIGNED_BYTE, pixels);

		String filename = "pointcraft_" + Main.server.player_id + "_" + Main.server.session_id + "_" + count + ".png";
		File file = new File(filename);
		BufferedImage bi = transformPixelsRGBBuffer2ARGB_ByHand(pixels);
		try {
			ImageIO.write(bi, "png", file);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// taken from: http://www.felixgers.de/teaching/jogl/imagingProg.html
	private static BufferedImage transformPixelsRGBBuffer2ARGB_ByHand(
			ByteBuffer pixelsRGB) {
		// Transform the ByteBuffer and get it as pixeldata.

		int frameWidth = Display.getDisplayMode().getWidth();
		int frameHeight = Display.getDisplayMode().getHeight();

		int[] pixelInts = new int[frameWidth * frameHeight];

		// Convert RGB bytes to ARGB ints with no transparency.
		// Flip image vertically by reading the
		// rows of pixels in the byte buffer in reverse
		// - (0,0) is at bottom left in OpenGL.
		//
		// Points to first byte (red) in each row.
		int p = frameWidth * frameHeight * 3;
		int q; // Index into ByteBuffer
		int i = 0; // Index into target int[]
		int w3 = frameWidth * 3; // Number of bytes in each row
		for (int row = 0; row < frameHeight; row++) {
			p -= w3;
			q = p;
			for (int col = 0; col < frameWidth; col++) {
				int iR = pixelsRGB.get(q++);
				int iG = pixelsRGB.get(q++);
				int iB = pixelsRGB.get(q++);
				pixelInts[i++] = 0xFF000000 | ((iR & 0x000000FF) << 16)
						| ((iG & 0x000000FF) << 8) | (iB & 0x000000FF);
			}
		}

		// Create a new BufferedImage from the pixeldata.
		BufferedImage bufferedImage = new BufferedImage(frameWidth,
				frameHeight, BufferedImage.TYPE_INT_RGB);
		bufferedImage.setRGB(0, 0, frameWidth, frameHeight, pixelInts, 0,
				frameWidth);

		return bufferedImage;
	}
}
