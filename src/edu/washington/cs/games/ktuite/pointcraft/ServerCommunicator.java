package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import org.lwjgl.util.vector.Vector3f;

public class ServerCommunicator {

	public int player_id, session_id;
	private String server_url;

	public ServerCommunicator(String _server_url) {
		server_url = _server_url;

		/*
		 * URL url; try { url = new URL(server_url + "signin.php");
		 * BufferedReader in = new BufferedReader(new InputStreamReader(
		 * url.openStream()));
		 * 
		 * String page_contents = in.readLine(); player_id =
		 * Integer.decode(page_contents); System.out.println("session: " +
		 * player_id); } catch (Exception e) {
		 * System.out.println("no server available"); }
		 */
	}

	public boolean attemptLogin(String username, String password) {
		URL url;
		try {
			url = new URL(server_url + "signin.php?username=" + username
					+ "&password=" + password);
			BufferedReader in = new BufferedReader(new InputStreamReader(
					url.openStream()));

			String page_contents = in.readLine();
			String[] split = page_contents.split(",");
			
			if (split.length == 2) {
				player_id = Integer.decode(split[0]);
				session_id = Integer.decode(split[1]);
				return true;
			} else {
				System.out.println(split[0]);
			}
		} catch (Exception e) {
			System.out.println("no server available");
		}
		return false;
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
		message += "?player_id=" + player_id + "&session_id=" + session_id;

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

		/*
		 * message += "&lines="; for (Scaffold geom : Main.geometry_v) { if
		 * (geom.isLine()) { Vector3f v; v = geom.pt_1; message += v.x + "," +
		 * v.y + "," + v.z + ","; v = geom.pt_2; message += v.x + "," + v.y +
		 * "," + v.z + ","; } }
		 * 
		 * message += "&planes="; for (Scaffold geom : Main.geometry_v) { if
		 * (geom.isPlane()) { message += geom.a + "," + geom.b + "," + geom.c +
		 * "," + geom.d;
		 * 
		 * } }
		 */

		sendMessage(message);

	}

	public void newPolygon() {
		String message = server_url + "genericupdate.php";
		message += "?player_id=" + player_id + "&session_id=" + session_id;
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

	public void newPellet(Pellet p) {
		String message = server_url + "genericupdate.php";
		message += "?player_id=" + player_id + "&session_id=" + session_id;
		message += "&pellets=";
		message += p.getType() + ",";
		message += p.pos.x + "," + p.pos.y + "," + p.pos.z + ",";
		sendMessage(message);
	}
}
