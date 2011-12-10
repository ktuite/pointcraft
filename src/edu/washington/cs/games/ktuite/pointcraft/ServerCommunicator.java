package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.lwjgl.util.vector.Vector3f;

public class ServerCommunicator {

	public int player_id;
	private String server_url;

	public ServerCommunicator(String _server_url) {
		server_url = _server_url;

		URL url;
		try {
			url = new URL(server_url + "signin.php");
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));

			String page_contents = in.readLine();
			player_id = Integer.decode(page_contents);
			System.out.println("session: " + player_id);
		} catch (Exception e) {
			System.out.println("no server available");
		}
	}

	private void sendMessage(String message) {
		System.out.println("Sending message: <<" + message + ">>");
		final String final_message = message;
		new Thread() {
			public void run() {
				try {
					URL url = new URL(final_message);
					url.openStream();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();
	}

	public void sendGenericUpdate() {
		// for now, do this when polygons or maybe even lines and planes are
		// created
		String message = server_url + "genericupdate.php";
		message += "?player_id=" + player_id;

		message += "&pellets=";
		for (Pellet p : Main.all_pellets_in_world) {
			message += p.pos.x + "," + p.pos.y + "," + p.pos.z + ",";
		}

		message += "&polygons=";
		for (Primitive geom : Main.geometry) {
			if (geom.isPolygon()) {
				message += (geom.numVertices() - 1) + ",";
				for (int i = 0; i < geom.numVertices() - 1; i++) {
					Vector3f v = geom.getVertices().get(i).pos;
					message += v.x + "," + v.y + "," + v.z + ",";
				}
			}
		}

		message += "&lines=";
		for (PrimitiveVertex geom : Main.geometry_v) {
			if (geom.isLine()) {
				Vector3f v;
				v = geom.pt_1;
				message += v.x + "," + v.y + "," + v.z + ",";
				v = geom.pt_2;
				message += v.x + "," + v.y + "," + v.z + ",";
			}
		}

		message += "&planes=";
		for (PrimitiveVertex geom : Main.geometry_v) {
			if (geom.isPlane()) {
				message += geom.a + "," + geom.b + "," + geom.c + "," + geom.d;

			}
		}

		sendMessage(message);

	}

	public void newPolygon() {
		String message = server_url + "genericupdate.php";
		message += "?player_id=" + player_id;
		message += "&polygons=";
		Primitive geom = Main.geometry.lastElement();
		if (geom.isPolygon()) {
			message += (geom.numVertices() - 1) + ",";
			for (int i = 0; i < geom.numVertices() - 1; i++) {
				Vector3f v = geom.getVertices().get(i).pos;
				message += v.x + "," + v.y + "," + v.z + ",";
			}
		}
		sendMessage(message);
	}
	
	public void newPellet(Pellet p){
		String message = server_url + "genericupdate.php";
		message += "?player_id=" + player_id;
		message += "&pellets=";
		message += p.getType() + ",";
		message += p.pos.x + "," + p.pos.y + "," + p.pos.z + ",";
		sendMessage(message);
	}
}
