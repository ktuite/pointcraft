package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Stack;

import javax.swing.JFileChooser;

import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;

public class Save {

	public static int VERSION = 3;

	private static JFileChooser fc;

	// Actually used
	public static File loadPointCloud() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			Mouse.setGrabbed(mouseGrabbed);
			return file;
		} else {
			Mouse.setGrabbed(mouseGrabbed);
			return null;
		}
	}

	public static void saveModel() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				OutputStream out = new FileOutputStream(file);
				writeModel(out);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

	public static void writeModel(OutputStream out) throws IOException {
		out.write(("{\"version\":" + VERSION + "}\n").getBytes());
		for (Pellet p : Main.all_pellets_in_world) {
			out.write(p.toJSONString().getBytes());
			out.write("\n".getBytes());
		}
		for (Primitive p : Main.geometry) {
			if (p.isPolygon()) {
				out.write(p.toJSONString().getBytes());
				out.write("\n".getBytes());
			}
		}
		for (Scaffold p : Main.geometry_v) {
			if (p.isReady()) {
				out.write(p.toJSONString().getBytes());
				out.write("\n".getBytes());
			}
		}
	}

	public static void loadModel() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				BufferedReader in = new BufferedReader(new FileReader(file));
				String first_line = in.readLine();
				JSONObject version_obj;
				float file_version = 0;
				try {
					version_obj = new JSONObject(first_line);
					file_version = (float) version_obj.getDouble("version");
				} catch (JSONException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}

				while (in.ready()) {
					String line = in.readLine();

					if (file_version == 2) {
						loadV2(line);
					} else if (file_version == 3) {
						loadV3(line);
					}

				}

			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

	private static void loadV2(String line) {
		// version 2 just has pellet indices in things like planes and
		// primitives
		JSONObject obj;
		try {
			obj = new JSONObject(line);
			String type = obj.getString("type");
			System.out.println(type);
			if (type.contains("pellet")) {
				Pellet.loadFromJSON(obj);
			} else if (type.contains("primitive")) {
				Primitive.loadFromJSONv2(obj);
			} else if (type.contains("scaffold")) {
				Scaffold.loadFromJSONv2(obj);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	private static void loadV3(String line) {
		// version has pellet indices as well as pellet positions
		JSONObject obj;
		try {
			obj = new JSONObject(line);
			String type = obj.getString("type");
			System.out.println(type);
			if (type.contains("pellet")) {
				Pellet.loadFromJSON(obj);
			} else if (type.contains("primitive")) {
				Primitive.loadFromJSONv3(obj);
			} else if (type.contains("scaffold")) {
				Scaffold.loadFromJSONv3(obj);
			}
			// don't know how much pellet ids are really used but lets keep them
			// big so we can keep track of them
			for (Pellet p : Main.all_pellets_in_world)
				if (p.id > Pellet.ID)
					Pellet.ID = p.id + 1;
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	public static void saveHeckaData() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				ObjectOutputStream out = new ObjectOutputStream(
						new FileOutputStream(file));
				out.writeObject(Main.all_pellets_in_world);
				out.writeObject(Main.geometry);
				out.writeObject(Main.geometry_v);
				out.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

	@SuppressWarnings("unchecked")
	public static void loadHeckaData() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				ObjectInputStream in = new ObjectInputStream(
						new FileInputStream(file));
				try {
					Main.all_pellets_in_world = (Stack<Pellet>) in.readObject();
					Main.geometry = (Stack<Primitive>) in.readObject();
					Main.geometry_v = (Stack<Scaffold>) in.readObject();
				} catch (ClassNotFoundException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

	public static void savePly() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			try {
				Writer output = new BufferedWriter(new FileWriter(file));
				int VERTEX_COUNT = 0;
				int FACE_COUNT = 0;
				for (Primitive g : Main.geometry) {
					if (g.isPolygon()) {
						VERTEX_COUNT += (g.numVertices() - 1);
						FACE_COUNT += 1;
					}
				}

				String header = "ply\n" + "format ascii 1.0\n"
						+ "element vertex " + VERTEX_COUNT + "\n"
						+ "property float x\n" + "property float y\n"
						+ "property float z\n" + "property uchar diffuse_red\n"
						+ "property uchar diffuse_green\n"
						+ "property uchar diffuse_blue\n" + "element face "
						+ FACE_COUNT + "\n"
						+ "property list uchar int vertex_index\n"
						+ "end_header\n";
				output.write(header);

				for (Primitive geom : Main.geometry) {
					if (geom.isPolygon()) {
						for (int i = 0; i < geom.numVertices() - 1; i++) {
							Pellet pellet = geom.getVertices().get(i);
							output.write(pellet.pos.x + " " + pellet.pos.y
									+ " " + pellet.pos.z + " ");
							output.write("150 150 150\n");
						}
					}
				}

				int current_vertex = 0;
				for (Primitive geom : Main.geometry) {
					if (geom.isPolygon()) {
						output.write(geom.numVertices() - 1 + " ");
						for (int i = current_vertex; i < geom.numVertices() - 1
								+ current_vertex; i++) {
							output.write(i + " ");
						}
						output.write("\n");
						current_vertex += (geom.numVertices() - 1);
					}
				}

				output.close();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

}
