package pc2;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.util.vector.Matrix4f;
import org.lwjgl.util.vector.Vector3f;
import org.lwjgl.util.vector.Vector4f;

import edu.washington.cs.games.ktuite.pointcraft.geometry.Ground;

public class Player {
	public static Vector3f pos;
	public static Vector3f vel;
	public static float tilt_angle;
	public static float pan_angle;
	public static float veldecay;
	public static float walkforce;
	public static double max_speed;
	public static float pellet_scale;

	public static void initPlayer() {
		pos = new Vector3f();
		vel = new Vector3f();
		pellet_scale = 1;
		tilt_angle = 0;
		pan_angle = 0;
		veldecay = .90f;
		walkforce = 1 / 4000f * Renderer.world_scale;
		max_speed = 1 * Renderer.world_scale;
		System.out.println("Starting position: " + pos + " Starting velocity: "
				+ vel);
	}

	public static void update() {
		// normalize the speed
		double speed = Math.sqrt(vel.length());
		if (speed > 0.000001) {
			float ratio = (float) (Math.min(speed, max_speed) / speed);
			vel.scale(ratio);
		}

		// sneak / go slowly
		if (!InputManager.minecraft_flight
				&& Keyboard.isKeyDown(Keyboard.KEY_SPACE))
			vel.scale(.3f);

		Vector3f.add(pos, vel, pos);

		if (Ground.impenetrable) {
			if (pos.y < Ground.height) {
				pos.y = Ground.height;
				vel.y = 0;
			}
		}

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
		if (pan_angle < 0)
			pan_angle += 360;
	}

	public static void changePelletScale(int i) {
		pellet_scale += .05f * i;
		if (pellet_scale <= 0)
			pellet_scale = 0.05f;
		else if (pellet_scale > 7)
			pellet_scale = 7f;
	}

	public static Vector3f computeGunDirection() {
		// do all this extra stuff with horizontal angle so that shooting up
		// in the air makes the pellet go up in the air

		Vector3f gun_direction = new Vector3f();

		Matrix4f trans = new Matrix4f();
		Vector4f dir = new Vector4f(0, 0, -1, 1);

		trans.setIdentity();
		trans.rotate(-pan_angle * 3.14159f / 180f, new Vector3f(0, 1, 0));
		trans.rotate(-tilt_angle * 3.14159f / 180f, new Vector3f(1, 0, 0));
		Matrix4f.transform(trans, dir, dir);

		//trans.load(Renderer.rotated_pointcloud_matrix);
		//rotated_pointcloud_matrix.rewind();
		trans.setIdentity();
		trans.m30 = 0;
		trans.m31 = 0;
		trans.m32 = 0;
		trans.invert();
		Matrix4f.transform(trans, dir, dir);

		gun_direction.set(dir.x, dir.y, dir.z);
		
		return gun_direction;
	}
}
