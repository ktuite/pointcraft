package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;

import java.awt.image.BufferedImage;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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

		String base_filename = "pointcraft_" + Main.server.player_id + "_" + Main.server.session_id + "_" + count;
		
		BufferedImage bi = transformPixelsRGBBuffer2ARGB_ByHand(pixels, Display.getDisplayMode().getWidth(),  Display.getDisplayMode().getHeight());
		
		try {
			//File file = new File(filename);
			//ImageIO.write(bi, "png", file);
			upload(bi, base_filename);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	// stolen from my sketchabit code
	public static boolean upload(BufferedImage bi, String basename) throws Exception {
		final String BOUNDARY = "====CATSCATSCATS====";
		//URL url = new URL("http://www.postbin.org/q7oqzc");
		URL url = new URL("http://phci03.cs.washington.edu/pointcraft/upload.php?player_id="+Main.server.player_id+"&cloud_id="+Main.server.cloud_id);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+BOUNDARY);
		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		
		// png screenshot
		outStream.writeBytes("--"+BOUNDARY+"\r\n");
		outStream.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"" + basename + ".png\"\r\n");
		outStream.writeBytes("Content-Type: image/png\r\n");
		outStream.writeBytes("\r\n");
		ImageIO.write(bi, "png", outStream);
		outStream.writeBytes("\r\n");
		
		// model file
		outStream.writeBytes("--"+BOUNDARY+"\r\n");
		outStream.writeBytes("Content-Disposition: form-data; name=\"modelfile\";filename=\"" + basename + ".txt\"\r\n");
		outStream.writeBytes("Content-Type: text/plain\r\n");
		outStream.writeBytes("\r\n");
		Save.writeModel(outStream);
		outStream.writeBytes("\r\n");
		
		outStream.writeBytes("--"+BOUNDARY+"--\r\n");
		
		
		if(conn.getResponseCode() == 200) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean uploadBitmap(File image_file) throws Exception {
		final String BOUNDARY = "====CATSCATSCATS==";
		//URL url = new URL("http://www.postbin.org/q7oqzc");
		URL url = new URL("http://phci03.cs.washington.edu/pointcraft/upload.php?player_id="+Main.server.player_id);
		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "multipart/form-data;boundary="+BOUNDARY);
		DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
		outStream.writeBytes("--"+BOUNDARY+"\r\n");
		outStream.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"upload.png\"\r\n");
		outStream.writeBytes("Content-Type: image/png\r\n");
		outStream.writeBytes("\r\n");
		
		FileInputStream inStream = new FileInputStream(image_file);
		
		int buf_size = 1024;
		int temp_size;
		byte[] buf = new byte[buf_size];
		while (inStream.available() > 0){
			temp_size = Math.min(buf_size, inStream.available());
			inStream.read(buf, 0, temp_size);
			outStream.write(buf, 0, temp_size);
			System.out.println("transmitting bytes " + temp_size);
		}
		
		
		outStream.writeBytes("\r\n");
		outStream.writeBytes("--"+BOUNDARY+"--\r\n");
		if(conn.getResponseCode() == 200) {
			return true;
		} else {
			return false;
		}
	}
	
	// taken from: http://www.felixgers.de/teaching/jogl/imagingProg.html
	public static BufferedImage transformPixelsRGBBuffer2ARGB_ByHand(
			ByteBuffer pixelsRGB, int frameWidth, int frameHeight) {
		// Transform the ByteBuffer and get it as pixeldata.

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
