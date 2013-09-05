package edu.washington.cs.games.ktuite.pointcraft;

import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.Canvas;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.Display;

import edu.washington.cs.games.ktuite.pointcraft.levels.CubeLevel;
import edu.washington.cs.games.ktuite.pointcraft.tools.ModelingGun;

public class PointCraftApplet extends Applet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	Canvas display_parent;

	/** Thread which runs the main game loop */
	Thread gameThread;

	/** Main pointcraft class */
	Main main;

	/** is the game loop running */
	boolean running = false;

	public void startLWJGL() {
		main = new Main();
		gameThread = new Thread() {
			public void run() {
				running = true;

				
				try {
					Display.setVSyncEnabled(true);
					Display.setParent(display_parent);
					Display.create();
				} catch (LWJGLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Main.server = new ServerCommunicator(
						"http://phci03.cs.washington.edu/pointcraft/");

				//main.initDisplay();
				main.initGUI();
				main.initGraphics();
				main.initGameVariables();

				main.current_level = new CubeLevel(main);

				ModelingGun.useLaser(main);

				main.run();
			}
		};
		gameThread.start();
	}

	/**
	 * Tell game loop to stop running, after which the LWJGL Display will be
	 * destoryed. The main thread will wait for the Display.destroy().
	 */
	private void stopLWJGL() {
		running = false;
		try {
			gameThread.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public void start() {

	}

	public void stop() {

	}

	/**
	 * Applet Destroy method will remove the canvas, before canvas is destroyed
	 * it will notify stopLWJGL() to stop the main game loop and to destroy the
	 * Display
	 */
	public void destroy() {
		remove(display_parent);
		super.destroy();
	}

	public void init() {
		System.out.println("applet init!");
		setLayout(new BorderLayout());
		try {
			display_parent = new Canvas() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public final void addNotify() {
					super.addNotify();
					startLWJGL();
				}

				public final void removeNotify() {
					stopLWJGL();
					super.removeNotify();
				}
			};
			display_parent.setSize(getWidth(), getHeight());
			add(display_parent);
			display_parent.setFocusable(true);
			display_parent.requestFocus();
			display_parent.setIgnoreRepaint(true);
			setVisible(true);
		} catch (Exception e) {
			System.err.println(e);
			throw new RuntimeException("Unable to create display");
		}
	}

	protected void initGL() {

	}

	public void gameLoop() {
		while (running) {
			Display.sync(60);
			Display.update();
		}

		Display.destroy();
	}

}
