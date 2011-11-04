package edu.washington.cs.games.ktuite.pointcraft;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.util.glu.GLU.*;

import java.awt.RenderingHints.Key;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.DoubleBuffer;
import java.nio.FloatBuffer;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import org.lwjgl.LWJGLException;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.*;
import org.lwjgl.util.Timer;
import org.lwjgl.util.vector.Vector2f;
import org.lwjgl.util.vector.Vector3f;
import org.newdawn.slick.Color;
import org.newdawn.slick.openal.Audio;
import org.newdawn.slick.openal.AudioLoader;
import org.newdawn.slick.opengl.Texture;
import org.newdawn.slick.opengl.TextureLoader;
import org.newdawn.slick.util.ResourceLoader;

public class Main {
	// stuff about the atmosphere
	private float FOG_COLOR[] = new float[] { .89f, .89f, .89f, 1.0f };
	public static Audio launch_effect;
	public static Audio attach_effect;

	// stuff about the display
	private static float point_size = 2;
	private static float fog_density = 5;

	// stuff about the world and how you move around
	public static float world_scale = 1; // 40f;
	private Vector3f pos;
	private Vector3f vel;
	private float tilt_angle;
	private float pan_angle;
	private float veldecay = .90f;
	private static float walkforce = 1 / 4000f * world_scale;
	private double max_speed = 1 * world_scale;
	private float stilts = 0.01f * world_scale;
	private Texture skybox = null;

	// stuff about the point cloud
	private int num_points;
	private DoubleBuffer point_positions;
	private DoubleBuffer point_colors;

	// stuff about general guns and general list of pellets/things shot
	private Vector3f gun_direction;
	final private float gun_speed = 0.001f * world_scale;
	public static Timer timer = new Timer();
	public static Stack<Pellet> all_pellets_in_world;
	private Stack<Pellet> all_dead_pellets_in_world;

	// TODO: move out of here and put somewhere else since this is a certain
	// kind of geometry
	public static Stack<Primitive> geometry;
	public static Stack<PrimitiveVertex> geometry_v;

	private boolean draw_points = true;

	public enum GunMode {
		PELLET, ORB, LINE, PLANE, ARC, CIRCLE
	}

	public GunMode which_gun;

	public static void main(String[] args) {
		Main main = new Main();

		main.InitDisplay();
		main.InitGraphics();
		main.InitData();
		main.InitGameVariables();
		main.Start();
	}

	private void InitDisplay() {
		try {
			Display.setDisplayMode(new DisplayMode(800, 600));
			Display.setVSyncEnabled(true);
			Display.create();
			Mouse.setGrabbed(true);
		} catch (LWJGLException e) {
			e.printStackTrace();
			System.out.println("ERROR running InitDisplay... game exiting");
			System.exit(1);
		}
	}

	private void InitGraphics() {
		// view matrix
		glMatrixMode(GL_PROJECTION);
		glLoadIdentity();
		gluPerspective(60, 800.0f / 600.0f, .001f, 1000.0f);
		glMatrixMode(GL_MODELVIEW);

		// fog
		FloatBuffer fogColorBuffer = ByteBuffer.allocateDirect(4 * 4)
				.order(ByteOrder.nativeOrder()).asFloatBuffer();
		fogColorBuffer.put(FOG_COLOR);
		fogColorBuffer.rewind();
		glFog(GL_FOG_COLOR, fogColorBuffer);
		glFogi(GL_FOG_MODE, GL_EXP2);
		glFogf(GL_FOG_END, 3.0f);
		glFogf(GL_FOG_START, .25f);
		glFogf(GL_FOG_DENSITY, fog_density);

		// getting the ordering of the points right
		glEnable(GL_DEPTH_TEST);

		glEnable(GL_BLEND);
		glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

		glEnable(GL_LINE_SMOOTH);

		// skybox texture loaded
		try {
			skybox = TextureLoader.getTexture("JPG",
					ResourceLoader.getResourceAsStream("assets/gray_sky.jpg"));
			System.out.println("Texture loaded: " + skybox);
			System.out.println(">> Image width: " + skybox.getImageWidth());
			System.out.println(">> Image height: " + skybox.getImageHeight());
			System.out.println(">> Texture width: " + skybox.getTextureWidth());
			System.out.println(">> Texture height: "
					+ skybox.getTextureHeight());
			System.out.println(">> Texture ID: " + skybox.getTextureID());
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("Couldn't load skybox");
			System.exit(1);
		}
	}

	private void SetGameVariablesFromWorldScale() {
		walkforce = 1 / 4000f * world_scale;
		max_speed = 1 * world_scale;
		stilts = 0.01f * world_scale;
	}

	private void InitGameVariables() {
		pos = new Vector3f();
		vel = new Vector3f();
		tilt_angle = 0;
		pan_angle = 0;
		System.out.println("Starting position: " + pos + " Starting velocity: "
				+ vel);

		gun_direction = new Vector3f();
		all_pellets_in_world = new Stack<Pellet>();
		all_dead_pellets_in_world = new Stack<Pellet>();

		which_gun = GunMode.PELLET;
		OrbPellet.orb_pellet = new OrbPellet(all_pellets_in_world);

		// TODO: Move this crap elsewhere... init the different geometry
		// containers individually
		geometry = new Stack<Primitive>();
		geometry_v = new Stack<PrimitiveVertex>();

		try {
			launch_effect = AudioLoader.getAudio("WAV",
					ResourceLoader.getResourceAsStream("assets/launch.wav"));
			attach_effect = AudioLoader.getAudio("WAV",
					ResourceLoader.getResourceAsStream("assets/attach.wav"));
		} catch (IOException e) {
			e.printStackTrace();
			System.out.println("couldn't load sounds");
			System.exit(1);
		}
	}

	private void InitData() {
		// data of the point cloud itself, loaded in from C++

		LibPointCloud
				.load("/Users/ktuite/Desktop/sketchymodeler/instances/lewis-hall/model.bin");
		// .load("/Users/ktuite/Desktop/sketchymodeler/server_code/Uris.bin");
		// .loadBundle("/Users/ktuite/Desktop/sketchymodeler/texviewer/cse/bundle.out");
		// .load("/Users/ktuite/Desktop/sketchymodeler/server_code/SageChapel.bin");
		System.out.println("number of points: " + LibPointCloud.getNumPoints());

		num_points = LibPointCloud.getNumPoints();
		point_positions = LibPointCloud.getPointPositions()
				.getByteBuffer(0, num_points * 3 * 8).asDoubleBuffer();
		point_colors = LibPointCloud.getPointColors()
				.getByteBuffer(0, num_points * 3 * 8).asDoubleBuffer();

		System.out.println("first point: " + point_positions.get(0));
		System.out.println("first color: " + point_colors.get(0));

		FindMinMaxOfWorld();
		// SetGameVariablesFromWorldScale();

		LibPointCloud.makeKdTree();
	}

	private void FindMinMaxOfWorld() {
		float[] min_point = new float[3];
		float[] max_point = new float[3];
		for (int k = 0; k < 3; k++) {
			min_point[k] = Float.MAX_VALUE;
			max_point[k] = Float.MIN_VALUE;
		}

		for (int i = 0; i < num_points; i++) {
			for (int k = 0; k < 3; k++) {
				float p = (float) point_positions.get(k * num_points + i);
				if (p < min_point[k])
					min_point[k] = p;
				if (p > max_point[k])
					max_point[k] = p;
			}
		}

		world_scale = (float) (((max_point[1] - min_point[1])) / 0.071716);
		// lewis hall height for scale ref...

		System.out.println("world scale: " + world_scale);
	}

	private void Start() {
		while (!Display.isCloseRequested()) {
			Timer.tick();
			UpdateGameObjects();
			EventLoop(); // input like mouse and keyboard
			DisplayLoop(); // draw things on the screen
		}

		Display.destroy();
	}

	private static void undoLastPellet() {
		if (PolygonPellet.current_cycle.size() > 0) {
			PolygonPellet.current_cycle.pop();
			all_pellets_in_world.pop();
		}
		if (geometry.size() > 0 && geometry.peek().isPolygon()) {
			Primitive last_poly = geometry.pop();
			for (int i = 0; i < last_poly.numVertices() - 2; i++) {
				geometry.pop();
				if (all_pellets_in_world.size() > 0) {
					all_pellets_in_world.pop();
				}
			}
			PolygonPellet.current_cycle.clear();
		} else if (geometry.size() > 0) {
			geometry.pop();
		}
		// TODO: horribly broke undoing for making cycles except it wasnt that
		// great to begin with

	}

	private void UpdateGameObjects() {
		for (Pellet pellet : all_pellets_in_world) {
			pellet.update();
		}

		if (which_gun == GunMode.ORB) {
			OrbPellet
					.updateOrbPellet(pos, gun_direction, pan_angle, tilt_angle);
		}
	}

	private void EventLoop() {
		// undoing actions
		if (Keyboard.isKeyDown(219) || Keyboard.isKeyDown(29)) {
			while (Keyboard.next()) {
				if (Keyboard.getEventKeyState()
						&& Keyboard.getEventKey() == Keyboard.KEY_Z) {
					System.out.println("UNDO!");
					undoLastPellet();
				}
				if (Keyboard.getEventKeyState()
						&& Keyboard.getEventKey() == Keyboard.KEY_S) {
					System.out.println("SAVE!");
					Save.attemptToSave();
				}
			}
		}

		// WASD key motion, with a little bit of gliding
		if (Keyboard.isKeyDown(Keyboard.KEY_W)
				|| Keyboard.isKeyDown(Keyboard.KEY_UP)) {
			vel.x += Math.sin(pan_angle * 3.14159 / 180f) * walkforce;
			vel.z -= Math.cos(pan_angle * 3.14159 / 180f) * walkforce;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_S)
				|| Keyboard.isKeyDown(Keyboard.KEY_DOWN)) {
			vel.x -= Math.sin(pan_angle * 3.14159 / 180f) * walkforce;
			vel.z += Math.cos(pan_angle * 3.14159 / 180f) * walkforce;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_A)
				|| Keyboard.isKeyDown(Keyboard.KEY_LEFT)) {
			vel.x -= Math.cos(pan_angle * 3.14159 / 180f) * walkforce / 2;
			vel.z -= Math.sin(pan_angle * 3.14159 / 180f) * walkforce / 2;
		}
		if (Keyboard.isKeyDown(Keyboard.KEY_D)
				|| Keyboard.isKeyDown(Keyboard.KEY_RIGHT)) {
			vel.x += Math.cos(pan_angle * 3.14159 / 180f) * walkforce / 2;
			vel.z += Math.sin(pan_angle * 3.14159 / 180f) * walkforce / 2;
		}

		// this is like putting on or taking off some stilts
		// (numerous pairs of stilts)
		// basically it increases or decreases your vertical world height
		while (Keyboard.next()) {
			if (Keyboard.getEventKeyState()) {
				if (Keyboard.getEventKey() == Keyboard.KEY_Q) {
					pos.y -= stilts;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_E) {
					pos.y += stilts;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_P) {
					draw_points = !draw_points;
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_1) {
					which_gun = GunMode.PELLET;
					System.out.println("regular pellet gun selected");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_2) {
					which_gun = GunMode.LINE;
					System.out.println("line fitting pellet gun selected");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_3) {
					which_gun = GunMode.PLANE;
					System.out.println("plane fitting pellet gun selected");
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_0) {
					which_gun = GunMode.ORB;
					System.out
							.println("orb gun (where you can just place pellets in space without them sticking to things) selected");
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_N) {
					if (which_gun == GunMode.PLANE)
						PlanePellet.startNewPlane();
					else if (which_gun == GunMode.LINE)
						LinePellet.startNewLine();
				}

				if (Keyboard.getEventKey() == Keyboard.KEY_EQUALS) {
					point_size++;
					if (point_size > 10)
						point_size = 10;
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_MINUS) {
					point_size--;
					if (point_size < 1)
						point_size = 1;
				}
				
				if (Keyboard.getEventKey() == Keyboard.KEY_LBRACKET){
					fog_density -= 5;
					if (fog_density < 0)
						fog_density = 0;
					glFogf(GL_FOG_DENSITY, fog_density);
				}
				if (Keyboard.getEventKey() == Keyboard.KEY_RBRACKET){
					fog_density += 5;
					if (fog_density > 50)
						fog_density = 50;
					glFogf(GL_FOG_DENSITY, fog_density);
				}

			}
		}

		// normalize the speed
		double speed = Math.sqrt(vel.length());
		if (speed > 0.000001) {
			float ratio = (float) (Math.min(speed, max_speed) / speed);
			vel.scale(ratio);
		}
		
		// sneak / go slowly
		if (Keyboard.isKeyDown(Keyboard.KEY_SPACE))
			vel.scale(.1f);

		// pos += vel
		Vector3f.add(pos, vel, pos);

		// friction (let player glide to a stop)
		vel.scale(veldecay);

		// use mouse to control where player is looking
		tilt_angle -= Mouse.getDY() / 10f;
		pan_angle += Mouse.getDX() / 10f;

		if (tilt_angle > 90)
			tilt_angle = 90;
		if (tilt_angle < -90)
			tilt_angle = -90;

		if (pan_angle > 360)
			pan_angle -= 360;
		if (pan_angle < -360)
			pan_angle += 360;

		while (Mouse.next()) {
			if (Mouse.getEventButtonState()) {
				if (Mouse.getEventButton() == 0) {
					ShootGun();
				}
			}
		}

		// use scroll wheel to change orb gun distance
		// so far the only gun mode that uses extra stuff to determine its state
		if (which_gun == GunMode.ORB) {
			int wheel = Mouse.getDWheel();
			if (wheel < 0) {
				OrbPellet.orb_pellet.decreaseDistance();
			} else if (wheel > 0) {
				OrbPellet.orb_pellet.increaseDistance();
			}
		}
	}

	private void DisplayLoop() {
		glClearColor(FOG_COLOR[0], FOG_COLOR[1], FOG_COLOR[2], 1.0f);
		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
		glPushMatrix();

		glRotatef(tilt_angle, 1.0f, 0.0f, 0.0f); // rotate our camera up/down
		glRotatef(pan_angle, 0.0f, 1.0f, 0.0f); // rotate our camera left/right

		DrawSkybox(); // draw skybox before translate

		glTranslated(-pos.x, -pos.y, -pos.z); // translate the screen

		glEnable(GL_FOG);
		if (draw_points)
			DrawPoints(); // draw the actual 3d things

		DrawPellets();
		if (which_gun == GunMode.ORB)
			OrbPellet.drawOrbPellet();

		for (Primitive geom : geometry) {
			geom.draw();
		}
		for (PrimitiveVertex geom : geometry_v) {
			geom.draw();
		}
		glDisable(GL_FOG);

		glPopMatrix();

		DrawHud();

		Display.update();
	}

	private void DrawPoints() {
		glPointSize(point_size);
		glBegin(GL_POINTS);
		for (int i = 0; i < num_points; i += 1) {
			float r = (float) (point_colors.get(0 * num_points + i) / 255.0f);
			float g = (float) (point_colors.get(1 * num_points + i) / 255.0f);
			float b = (float) (point_colors.get(2 * num_points + i) / 255.0f);
			glColor3f(r, g, b);
			glVertex3d(point_positions.get(0 * num_points + i),
					point_positions.get(1 * num_points + i),
					point_positions.get(2 * num_points + i));
		}
		glEnd();
	}

	private void DrawPellets() {
		for (Pellet pellet : all_pellets_in_world) {
			if (pellet.alive) {
				glPushMatrix();
				glTranslatef(pellet.pos.x, pellet.pos.y, pellet.pos.z);
				pellet.draw();
				glPopMatrix();
			} else {
				all_dead_pellets_in_world.add(pellet);
			}
		}
		for (Pellet pellet : all_dead_pellets_in_world) {
			all_pellets_in_world.remove(pellet);
		}
		all_dead_pellets_in_world.clear();
	}

	private void DrawSkybox() {
		glEnable(GL_TEXTURE_2D);
		glDisable(GL_FOG);
		glDisable(GL_DEPTH_TEST);

		Color.white.bind();
		skybox.bind();
		glPointSize(10);
		float s = .1f;
		glBegin(GL_QUADS);
		// tex coords of .99 and .01 used here and there
		// to prevent wrap around and dark edges on light things

		// top
		glTexCoord2f(.75f, 0.01f);
		glVertex3f(s, s, s);
		glTexCoord2f(.5f, 0.01f);
		glVertex3f(-s, s, s);
		glTexCoord2f(.5f, .5f);
		glVertex3f(-s, s, -s);
		glTexCoord2f(.75f, .5f);
		glVertex3f(s, s, -s);

		// one side....
		glTexCoord2f(0f, .5f);
		glVertex3f(s, s, s);
		glTexCoord2f(.25f, .5f);
		glVertex3f(-s, s, s);
		glTexCoord2f(.25f, .99f);
		glVertex3f(-s, -s, s);
		glTexCoord2f(0f, .99f);
		glVertex3f(s, -s, s);

		// two side....
		glTexCoord2f(.25f, .5f);
		glVertex3f(-s, s, s);
		glVertex3f(-s, s, -s);
		glTexCoord2f(.5f, .99f);
		glVertex3f(-s, -s, -s);
		glTexCoord2f(.25f, .99f);
		glVertex3f(-s, -s, s);

		// red side.... (third side)
		glTexCoord2f(.5f, .5f);
		glVertex3f(-s, s, -s);
		glTexCoord2f(.75f, .5f);
		glVertex3f(s, s, -s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, -s);
		glTexCoord2f(.5f, .99f);
		glVertex3f(-s, -s, -s);

		// blue side.... (fourth side)
		glTexCoord2f(.75f, .5f);
		glVertex3f(s, s, -s);
		glTexCoord2f(1.0f, .5f);
		glVertex3f(s, s, s);
		glTexCoord2f(1.0f, .99f);
		glVertex3f(s, -s, s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, -s);

		// down side....
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(-s, -s, s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(-s, -s, -s);
		glTexCoord2f(.75f, .99f);
		glVertex3f(s, -s, -s);
		glEnd();

		glDisable(GL_TEXTURE_2D);
		// glEnable(GL_FOG);
		glEnable(GL_DEPTH_TEST);
	}

	private void DrawHud() {
		glDisable(GL_DEPTH_TEST);
		glMatrixMode(GL_PROJECTION);
		glPushMatrix();
		glLoadIdentity();
		glOrtho(-1, 1, 1, -1, -1, 1);
		glColor3f(1f, 1f, 1f);
		float f = 0.05f;

		glLineWidth(2);
		int n = 30;
		switch (which_gun) {
		case PELLET:
			glBegin(GL_LINES);
			glVertex2f(0, f);
			glVertex2f(0, -f);
			glVertex2f(f * 600 / 800, 0);
			glVertex2f(-f * 600 / 800, 0);
			glEnd();

			glBegin(GL_LINE_LOOP);
			for (int i = 0; i < n; i++) {
				float angle = (float) (Math.PI * 2 * i / n);
				float x = (float) (Math.cos(angle) * f * 0.75 * 600 / 800);
				float y = (float) (Math.sin(angle) * f * 0.75);
				glVertex2f(x, y);
			}
			glEnd();
			break;
		case ORB:
			glBegin(GL_LINE_LOOP);
			for (int i = 0; i < n; i++) {
				float angle = (float) (Math.PI * 2 * i / n);
				float x = (float) (Math.cos(angle) * f * 0.75 * 600 / 800);
				float y = (float) (Math.sin(angle) * f * 0.75);
				glVertex2f(x, y);
			}
			glEnd();
			glBegin(GL_LINE_LOOP);
			for (int i = 0; i < n; i++) {
				float angle = (float) (Math.PI * 2 * i / n);
				float x = (float) (Math.cos(angle) * f * 0.55 * 600 / 800);
				float y = (float) (Math.sin(angle) * f * 0.55);
				glVertex2f(x, y);
			}
			glEnd();
			break;
		case PLANE:
			glBegin(GL_LINE_LOOP);
			glVertex2f(0, f);
			glVertex2f(f * 600 / 800, 0);
			glVertex2f(0, -f);
			glVertex2f(-f * 600 / 800, 0);
			glEnd();
			break;
		case LINE:
			glBegin(GL_LINES);
			glVertex2f(f * 600 / 800, f);
			glVertex2f(-f * 600 / 800, -f);
			glVertex2f(-f * .2f * 600 / 800, f * .2f);
			glVertex2f(f * .2f * 600 / 800, -f * .2f);
			glEnd();
			break;
		default:
			break;
		}

		glPopMatrix();
		glMatrixMode(GL_MODELVIEW);
		glEnable(GL_DEPTH_TEST);
	}

	@SuppressWarnings("unused")
	private void ShootPelletGun() {
		System.out.println("shooting gun");

		// do all this extra stuff with horizontal angle so that shooting up in
		// the air makes the pellet go up in the air
		Vector2f horiz = new Vector2f();
		horiz.x = (float) Math.sin(pan_angle * 3.14159 / 180f);
		horiz.y = -1 * (float) Math.cos(pan_angle * 3.14159 / 180f);
		horiz.normalise();
		horiz.scale((float) Math.cos(tilt_angle * 3.14159 / 180f));
		gun_direction.x = horiz.x;
		gun_direction.z = horiz.y;
		gun_direction.y = -1 * (float) Math.sin(tilt_angle * 3.14159 / 180f);
		gun_direction.normalise();

		Pellet pellet = new Pellet(all_pellets_in_world);
		pellet.vel.set(gun_direction);
		pellet.vel.scale(gun_speed);
		pellet.pos.set(pos);
		all_pellets_in_world.add(pellet);
	}

	private void ShootGun() {
		if (which_gun == GunMode.ORB) {
			OrbPellet new_pellet = new OrbPellet(all_pellets_in_world);
			new_pellet.pos.set(OrbPellet.orb_pellet.pos);
			new_pellet.constructing = true;
			all_pellets_in_world.add(new_pellet);
			System.out.println(all_pellets_in_world);
		} else if (which_gun == GunMode.PELLET || which_gun == GunMode.PLANE
				|| which_gun == GunMode.LINE) {
			System.out.println("shooting gun");

			// do all this extra stuff with horizontal angle so that shooting up
			// in
			// the air makes the pellet go up in the air
			Vector2f horiz = new Vector2f();
			horiz.x = (float) Math.sin(pan_angle * 3.14159 / 180f);
			horiz.y = -1 * (float) Math.cos(pan_angle * 3.14159 / 180f);
			horiz.normalise();
			horiz.scale((float) Math.cos(tilt_angle * 3.14159 / 180f));
			gun_direction.x = horiz.x;
			gun_direction.z = horiz.y;
			gun_direction.y = -1
					* (float) Math.sin(tilt_angle * 3.14159 / 180f);
			gun_direction.normalise();

			Pellet pellet = null;
			if (which_gun == GunMode.PLANE) {
				pellet = new PlanePellet(all_pellets_in_world);
			} else if (which_gun == GunMode.LINE) {
				pellet = new LinePellet(all_pellets_in_world);
			} else {
				pellet = new PolygonPellet(all_pellets_in_world);
			}
			pellet.vel.set(gun_direction);
			pellet.vel.scale(gun_speed);
			pellet.pos.set(pos);
			all_pellets_in_world.add(pellet);
		}
	}

}
