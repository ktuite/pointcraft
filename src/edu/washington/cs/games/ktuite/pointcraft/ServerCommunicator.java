package edu.washington.cs.games.ktuite.pointcraft;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

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

	@SuppressWarnings("unused")
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

	public void actionUpdate(final ActionTracker.Action action) {
		new Thread() {
			public void run() {
				try {
					String data = "";
					data += URLEncoder.encode("player_id", "UTF-8")
							+ "="
							+ URLEncoder.encode(Integer.toString(player_id),
									"UTF-8");
					data += "&"
							+ URLEncoder.encode("session_id", "UTF-8")
							+ "="
							+ URLEncoder.encode(Integer.toString(session_id),
									"UTF-8");
					data += "&"
							+ URLEncoder.encode("action_type", "UTF-8")
							+ "="
							+ URLEncoder.encode(action.action_type.toString(),
									"UTF-8");
					data += "&" + URLEncoder.encode("action_details", "UTF-8")
							+ "="
							+ URLEncoder.encode(action.toString(), "UTF-8");

					URL url = new URL(server_url + "actionupdate.php");
					URLConnection conn = url.openConnection();
					conn.setDoOutput(true);
					OutputStreamWriter wr = new OutputStreamWriter(
							conn.getOutputStream());
					wr.write(data);
					wr.flush();

					// Get the response
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					String line;
					while ((line = rd.readLine()) != null) {
						System.out.println(line);
					}
					wr.close();
					rd.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}
	
	public void undoUpdate() {
		new Thread() {
			public void run() {
				try {
					String data = "";
					data += URLEncoder.encode("player_id", "UTF-8")
							+ "="
							+ URLEncoder.encode(Integer.toString(player_id),
									"UTF-8");
					data += "&"
							+ URLEncoder.encode("session_id", "UTF-8")
							+ "="
							+ URLEncoder.encode(Integer.toString(session_id),
									"UTF-8");
					data += "&"
							+ URLEncoder.encode("action_type", "UTF-8")
							+ "="
							+ URLEncoder.encode("UNDO",
									"UTF-8");

					URL url = new URL(server_url + "actionupdate.php");
					URLConnection conn = url.openConnection();
					conn.setDoOutput(true);
					OutputStreamWriter wr = new OutputStreamWriter(
							conn.getOutputStream());
					wr.write(data);
					wr.flush();

					// Get the response
					BufferedReader rd = new BufferedReader(
							new InputStreamReader(conn.getInputStream()));
					String line;
					while ((line = rd.readLine()) != null) {
						System.out.println(line);
					}
					wr.close();
					rd.close();

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}.start();

	}

}
