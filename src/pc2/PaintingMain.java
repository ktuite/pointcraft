package pc2;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.DisplayMode;

public class PaintingMain {

	public static void main(String[] args) {
		String pc_file = null;

		pc_file = "data/observatory.ply";

		initDisplay();
		if (pc_file == null) {
			PointStore.loadCube();
		} else {
			PointStore.load(pc_file);
		}
		Player.initPlayer();
		Renderer.initGraphics();
		run();

	}

	private static void run() {
		while (!Display.isCloseRequested()) {
			// manage input
			InputManager.handleInput();

			// update elements
			Player.update();
			Paintbrush.update();

			// render
			Renderer.drawSceneAndGUI();

			Display.update();
		}
		Display.destroy();

		// AL.destroy();
		System.out.println("PointCraft is closing");
	}

	public static void initDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.setResizable(true);
			Display.setVSyncEnabled(true);
			Display.create();
			Display.setTitle("PointCraft 2");
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.out.println("ERROR running initDisplay... exiting");
			System.exit(1);
		}

		Mouse.setGrabbed(true);
	}

}
