package edu.washington.cs.games.ktuite.pointcraft;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;
import org.lwjgl.util.vector.Vector3f;

class Scene implements org.json.JSONString {
	float pan, tilt;
	Vector3f pos;

	public Scene() {
		pan = 0.0f;
		tilt = 0.0f;
		pos = new Vector3f(0.0f, 0.0f, 0.0f);
	};

	public String toJSONString() {
		JSONStringer s = new JSONStringer();
		try {
			s.object();
			s.key("pan");
			s.value(pan);
			s.key("tilt");
			s.value(tilt);
			s.key("pos_x");
			s.value((double) (pos.getX()));
			s.key("pos_y");
			s.value((double) (pos.getY()));
			s.key("pos_z");
			s.value((double) (pos.getZ()));
			s.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public void fromJSONString(JSONObject obj) throws JSONException {
		pan = obj.getLong("pan");
		tilt = obj.getLong("tilt");
		pos = new Vector3f(0.0f, 0.0f, 0.0f);
		pos.x = (float) obj.getDouble("pos_x");
		pos.y = (float) obj.getDouble("pos_y");
		pos.z = (float) obj.getDouble("pos_z");
		System.out.println("Loaded: " + pan + ", " + tilt + ", " + pos
				+ " <fromJSONString>");
	}
};

public class Cinematics {
	static float start_pan_angle;
	static float target_pan_angle;
	static float start_tilt_angle;
	static float target_tilt_angle;
	static int interpSteps = 100;
	static Vector3f start_pos;
	static Vector3f target_pos;
	static Vector3f rotation_center;
	static float start_radius;
	static float target_radius;
	static float start_theta;
	static float target_theta;

	static Scene[] scenes = new Scene[10];

	public static void setupInterp() {
		assert (interpSteps == 0);
		/*
		 * float x1 = start_pos.x; float y1 = -start_pos.z; float x2 =
		 * target_pos.x; float y2 = -target_pos.z; float dx1 =
		 * (float)Math.cos(start_tilt_angle); float dy1 =
		 * (float)Math.sin(start_tilt_angle); float mag1 =
		 * (float)Math.sqrt(dx1*dx1 + dy1*dy1); dx1 /= mag1; dy1 /= mag1; float
		 * dx2 = (float)Math.cos(target_tilt_angle); float dy2 =
		 * (float)Math.sin(target_tilt_angle); float mag2 =
		 * (float)Math.sqrt(dx2*dx2 + dy2*dy2); dx2 /= mag2; dy2 /= mag2;
		 * Vector3f solution = Cinematics.linearSolver((float)dx1, (float)-dx2,
		 * (float)(x2 - x1), (float)dy1, (float)-dy2, (float)(y2 - y1));
		 * 
		 * float cx = x1 + solution.x * dx1; float cy = y1 + solution.x * dy1;
		 * assert(Math.abs(cx-(x2 + solution.y * dx2)) < 0.000001);
		 * assert(Math.abs(cy-(y2 + solution.y * dy2)) < 0.000001);
		 * 
		 * Cinematics.rotation_center = new Vector3f(0.0f, 0.0f, 0.0f);
		 * Cinematics.rotation_center.x = cx; Cinematics.rotation_center.y = cy;
		 * 
		 * dx1 = x1 - cx; dy1 = y1 - cy; mag1 = (float)Math.sqrt((dx1*dx1) +
		 * (dy1*dy1)); dx1 /= mag1; dy1 /= mag1; Cinematics.start_radius = mag1;
		 * Cinematics.start_theta = (float)Math.acos(dx1);
		 * 
		 * dx2 = x2 - cx; dy2 = y2 - cy; mag2 = (float)Math.sqrt((dx2*dx2) +
		 * (dy2*dy2)); dx2 /= mag2; dy2 /= mag2; Cinematics.target_radius =
		 * mag2; Cinematics.target_theta = (float)Math.acos(dx2);
		 */
	}

	public static void interpPanTiltPos() {
		float f = (float) interpSteps / 100.0f;
		f = 3 * f * f - 2 * f * f * f;
		Main.pan_angle = Cinematics.start_pan_angle + f
				* (Cinematics.target_pan_angle - Cinematics.start_pan_angle);
		Main.tilt_angle = Cinematics.start_tilt_angle + f
				* (Cinematics.target_tilt_angle - Cinematics.start_tilt_angle);
		Main.pos.x = Cinematics.start_pos.x + f
				* (Cinematics.target_pos.x - Cinematics.start_pos.x);
		Main.pos.y = Cinematics.start_pos.y + f
				* (Cinematics.target_pos.y - Cinematics.start_pos.y);
		Main.pos.z = Cinematics.start_pos.z + f
				* (Cinematics.target_pos.z - Cinematics.start_pos.z);
	}

	public static Vector3f linearSolver(float a1, float b1, float c1, float a2,
			float b2, float c2) {
		float d = a1 * b2 - a2 * b1;
		Vector3f v = new Vector3f((c1 * b2 - c2 * b1) / d, (a1 * c2 - a2 * c1)
				/ d, 0.0f);
		return v;
	}

	public static void recordScene(int n) {
		System.out.println("recording saved scene");
		assert (n >= 0 && n < 10);
		scenes[n] = new Scene();
		scenes[n].pan = Main.pan_angle;
		scenes[n].tilt = Main.tilt_angle;
		scenes[n].pos = new Vector3f(Main.pos);
		System.out.println("recording scene " + n);
		System.out.println("set pan: " + scenes[n].pan + ", tilt: "
				+ scenes[n].tilt + ", pos: " + scenes[n].pos);
	}

	public static void recallScene(int n) {
		System.out.println("recalling saved scene");
		assert (n >= 0 && n < 10);
		if (scenes[n] != null) {
			if (!Main.animatingToSavedView) {
				Main.animatingToSavedView = true;
				Cinematics.start_pan_angle = Main.pan_angle;
				Cinematics.target_pan_angle = scenes[n].pan;
				Cinematics.start_tilt_angle = Main.tilt_angle;
				Cinematics.target_tilt_angle = scenes[n].tilt;
				Cinematics.start_pos = new Vector3f(Main.pos);
				Cinematics.target_pos = new Vector3f(scenes[n].pos);
				Cinematics.interpSteps = 0;
				Cinematics.setupInterp();
			}
		}
	}

	public static String toJSONString() {
		JSONStringer s = new JSONStringer();
		try {
			s.object();
			for (int i = 0; i < 10; i++) {
				if (scenes[i] != null) {
					s.key("scene_" + i);
					s.value(scenes[i]);
				}
			}
			s.endObject();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return s.toString();
	}

	public static void loadFromJSON(JSONObject obj) throws JSONException {

		for (int i = 0; i < 10; i++) {
			scenes[i] = new Scene();
			if (obj.has("scene_" + i)) {
				scenes[i].fromJSONString(obj.getJSONObject("scene_" + i));
			}
		}
	}

	public static void printAvailableScenes() {
		for (int i = 0; i < 10; i++) {
			if (scenes[i] != null) {
				System.out.println("Scene #" + i + ": " + scenes[i].pos + ", "
						+ scenes[i].pan + "," + scenes[i].tilt);
			}
		}

	}
};
