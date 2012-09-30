package pc2;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class InputManager {

	public static boolean minecraft_flight;
	
	public static void handleInput(){
		handleKeyboardMotion();
		handleKeyboardToggles();
		handleMouseToggles();
	}

	public static void handleKeyboardMotion() {
		if (Keyboard.isKeyDown(Keyboard.KEY_W)
				|| Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			Player.vel.x += Math.sin(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce * Player.pellet_scale;
			Player.vel.z -= Math.cos(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce * Player.pellet_scale;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)
				|| Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			Player.vel.x -= Math.sin(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce * Player.pellet_scale;
			Player.vel.z += Math.cos(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce * Player.pellet_scale;
		}

		if (Keyboard.isKeyDown(Keyboard.KEY_A)
				|| Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			Player.vel.x -= Math.cos(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce / 2 * Player.pellet_scale;
			Player.vel.z -= Math.sin(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce / 2 * Player.pellet_scale;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)
				|| Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			Player.vel.x += Math.cos(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce / 2 * Player.pellet_scale;
			Player.vel.z += Math.sin(Player.pan_angle * 3.14159 / 180f)
					* Player.walkforce / 2 * Player.pellet_scale;
		}

		if (minecraft_flight) {
			if (Keyboard.isKeyDown(Keyboard.KEY_SPACE)) {
				Player.vel.y += Player.walkforce / 2 * Player.pellet_scale;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
				Player.vel.y -= Player.walkforce / 2 * Player.pellet_scale;
			}
		} else {
			if (Keyboard.isKeyDown(Keyboard.KEY_E)) {
				Player.vel.y += Player.walkforce / 2 * Player.pellet_scale;
			}
			if (Keyboard.isKeyDown(Keyboard.KEY_Q)) {
				Player.vel.y -= Player.walkforce / 2 * Player.pellet_scale;
			}
		}

	}

	public static void handleKeyboardToggles() {
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				// PRINT KEY SO I CAN SEE THE KEY CODE
				// System.out.println("Key: " + Keyboard.getEventKey());

				if (Keyboard.getEventKey() == Keyboard.KEY_P
						|| Keyboard.getEventKey() == Keyboard.KEY_C) {
					Renderer.draw_points = !Renderer.draw_points;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_O) {
					Renderer.draw_scaffolding = !Renderer.draw_scaffolding;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_I) {
					Renderer.draw_pellets = !Renderer.draw_pellets;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_T) {
					Renderer.draw_textures = !Renderer.draw_textures;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_U) {
					Renderer.draw_polygons = !Renderer.draw_polygons;
				}
				
				if (Keyboard.getEventKey() == Keyboard.KEY_V) {
					// TODO: make this not broken
					//Renderer.makeCurrentPositionOrigin();
				}

				if (Keyboard.getEventKey() >= Keyboard.KEY_1
						&& Keyboard.getEventKey() <= Keyboard.KEY_9) {
					int key = Keyboard.getEventKey() - Keyboard.KEY_1;
					Paintbrush.changeMode(key);
					// number key pressed
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_0) {
					// 0 (zero) key pressed
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS) {
					Renderer.changePointSize(1);
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_MINUS) {
					Renderer.changePointSize(-1);
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_LBRACKET) {
					Renderer.changeFog(5);
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_RBRACKET) {
					Renderer.changeFog(-5);
				}

			}
		}
	}

	public static void handleMouseToggles() {
		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				handleMouseDown();
			} else {
				handleMouseUp();
			}
		}

		int wheel = Mouse.getDWheel();
		if (wheel < 0) {
			Player.changePelletScale(-1);
		} else if (wheel > 0) {
			Player.changePelletScale(1);
		}

	}

	private static void handleMouseUp() {
		// TODO Auto-generated method stub
		
	}

	private static void handleMouseDown() {
		// TODO Auto-generated method stub

	}
}
