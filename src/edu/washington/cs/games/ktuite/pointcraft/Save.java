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

	private static void writeModel(OutputStream out) throws IOException {
		for (Pellet p : Main.all_pellets_in_world) {
			if (p.pellet_type == Main.GunMode.PELLET) {
				out.write(p.toJSONString().getBytes());
				out.write("\n".getBytes());
			}
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
				while (in.ready()) {
					String line = in.readLine();
					try {
						JSONObject obj = new JSONObject(line);
						String type = obj.getString("type");
						System.out.println(type);
						if (type.contains("pellet")) {
							Pellet.loadFromJSON(obj);
						} else if (type.contains("primitive")) {
							Primitive.loadFromJSON(obj);
						} else if (type.contains("scaffold")) {
							Scaffold.loadFromJSON(obj);
						}
					} catch (JSONException e) {
						e.printStackTrace();
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
