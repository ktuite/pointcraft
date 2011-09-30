package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import javax.swing.JFileChooser;

import org.lwjgl.input.Mouse;

public class Save {

	private static JFileChooser fc;

	public static void attemptToSave() {
		Mouse.setGrabbed(false);
		System.out.println("save");
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			System.out.println("saving to file name: " + file.getName());
			save(file);
		}
		Mouse.setGrabbed(true);
	}

	public static void save(File file) {
		try {
			Writer output = new BufferedWriter(new FileWriter(file));

			int VERTEX_COUNT = Main.pellets.size();
			int FACE_COUNT = 0;
			for (Primitive geom : Main.geometry) {
				if (geom.isPolygon())
					FACE_COUNT++;
			}
			String header = "ply\n" + "format ascii 1.0\n" + "element vertex "
					+ VERTEX_COUNT + "\n" + "property float x\n"
					+ "property float y\n" + "property float z\n"
					+ "property uchar diffuse_red\n"
					+ "property uchar diffuse_green\n"
					+ "property uchar diffuse_blue\n" + "element face "
					+ FACE_COUNT + "\n"
					+ "property list uchar int vertex_index\n" + "end_header\n";
			output.write(header);

			for (Pellet pellet : Main.pellets) {
				output.write(pellet.pos.x + " " + pellet.pos.y + " "
						+ pellet.pos.z + " ");
				output.write("150 150 150\n");
			}
			for (Primitive geom : Main.geometry) {
				if (geom.isPolygon()){
					output.write(geom.plyFace());
				}
			}

			output.close();

		} catch (IOException e) {
			System.out.println("could not write to file");
			e.printStackTrace();
		}

	}

}
