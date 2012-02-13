package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Stack;
import java.util.zip.ZipEntry;
import java.util.zip.ZipException;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import javax.swing.JFileChooser;

import org.json.JSONException;
import org.json.JSONObject;
import org.lwjgl.input.Mouse;

public class Save {

	public static int VERSION = 4;

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
			writeZipOfModelAndTextures(file);
			// OutputStream out = new FileOutputStream(file);
			// writeModel(out);
			// out.close();
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

	public static void writeZipOfModelAndTextures(File file) {
		try {
			FileOutputStream dest = new FileOutputStream(file.getAbsolutePath()
					+ ".zip");

			ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(
					dest));
			out.setLevel(5);
			out.setMethod(ZipOutputStream.DEFLATED);

			// give it a higher level folder?
			String dir = file.getName() + "/";

			// write json thing to zip
			ZipEntry entry = new ZipEntry(dir + "geometry.txt");
			out.putNextEntry(entry);
			writeModel(out);
			out.closeEntry();

			// write images to zip
			for (Primitive geom : Main.geometry) {
				if (geom.local_textures != null) {
					for (int i = 0; i < geom.local_textures.length; i++) {
						String filename = geom.local_textures[i];
						byte[] data = geom.texture_data.get(i);
						if (filename != null && data != null) {
							try {
								ZipEntry tex_entry = new ZipEntry(dir
										+ filename);
								out.putNextEntry(tex_entry);
								saveTexture(out, filename, data);
								out.closeEntry();
							} catch (ZipException e) {
								e.printStackTrace();
							} finally {

							}
						}
					}
				}
			}

			// close zip
			out.close();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveTexture(OutputStream out, String filename,
			byte[] data) {
		if (!Main.IS_SIGGRAPH_DEMO)
			return;

		try {
			out.write(data);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
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
			if (file.getAbsolutePath().endsWith(".zip")) {
				loadZipOfModelAndTexture(file);
			} else {
				try {
					loadModelAndFetchNewTextures(file);
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

	private static void readGeometryFile(BufferedReader in) throws IOException {
		Main.geometry.clear();
		Main.geometry_v.clear();
		Main.all_pellets_in_world.clear();
		
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
			} else if (file_version == 4) {
				loadV4(line);
			}

		}
	}

	private static void loadModelAndFetchNewTextures(File file)
			throws IOException {
		BufferedReader in = new BufferedReader(new FileReader(file));
		readGeometryFile(in);
		in.close();

		for (Primitive geom : Main.geometry) {
			geom.startDownloadingTexture();
		}
	}

	private static void loadZipOfModelAndTexture(File file) {

		ZipFile zf;
		try {
			zf = new ZipFile(file);
			Enumeration<? extends ZipEntry> entries = zf.entries();

			HashMap<String, ZipEntry> zipped_entries = new HashMap<String, ZipEntry>();

			while (entries.hasMoreElements()) {
				ZipEntry ze = (ZipEntry) entries.nextElement();
				if (ze.getName().endsWith("geometry.txt")) {
					BufferedReader br = new BufferedReader(
							new InputStreamReader(zf.getInputStream(ze)));
					readGeometryFile(br);
					br.close();
				} else {
					String entry_name = ze.getName();
					entry_name = entry_name
							.substring(entry_name.indexOf("/") + 1);
					zipped_entries.put(entry_name, ze);
				}

			}

			for (Primitive geom : Main.geometry) {
				for (int i = 0; i < geom.local_textures.length; i++) {

					ZipEntry tex_entry = zipped_entries
							.get(geom.local_textures[i]);
					if (tex_entry != null) {
						InputStream in = zf.getInputStream(tex_entry);

						System.out.println("attempting to read texture"
								+ tex_entry + ", size: " + in.available());

						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						byte[] bytes = new byte[4096];
						int n;
						while ((n = in.read(bytes)) != -1) {
							baos.write(bytes, 0, n);
						}
						byte[] tex_byte_data = baos.toByteArray();
						geom.texture_data.set(i, tex_byte_data);
						geom.texture_count++;
					}

				}
			}

		} catch (ZipException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

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

	private static void loadV4(String line) {
		// version has pellet indices as well as pellet positions
		JSONObject obj;
		try {
			obj = new JSONObject(line);
			String type = obj.getString("type");
			System.out.println(type);
			if (type.contains("pellet")) {
				Pellet.loadFromJSON(obj);
			} else if (type.contains("primitive")) {
				Primitive.loadFromJSONv4(obj);
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

	public static void loadCinematics() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showOpenDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			InputStream in;
			try {
				in = new FileInputStream(file);
				try {
					readCinematicsFromFile(in);
					in.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}

	private static void readCinematicsFromFile(InputStream in) {
		BufferedReader br = new BufferedReader(new InputStreamReader(in));
		try {
			String first_line = br.readLine();
			JSONObject version_obj;
			float file_version = 0;
			try {
				version_obj = new JSONObject(first_line);
				file_version = (float) version_obj.getDouble("version");
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}

			while (br.ready()) {
				String line = br.readLine();

				if (file_version == 4) {
					loadCinematicsFromFile(line);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void loadCinematicsFromFile(String line) {
		try {
			Cinematics.loadFromJSON(new JSONObject(line));
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private static void writeCinematicsToFile(OutputStream out) {
		try {
			out.write(("{\"version\":" + VERSION + "}\n").getBytes());
			out.write(Cinematics.toJSONString().getBytes());
			out.write("\n".getBytes());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void saveCinematics() {
		boolean mouseGrabbed = Mouse.isGrabbed();
		Mouse.setGrabbed(false);
		if (fc == null) {
			fc = new JFileChooser();
		}
		int returnVal = fc.showSaveDialog(fc);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File file = fc.getSelectedFile();
			OutputStream out;
			try {
				out = new FileOutputStream(file);
				try {
					writeCinematicsToFile(out);
					out.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		Mouse.setGrabbed(mouseGrabbed);
	}
}
