package edu.washington.cs.games.ktuite.pointcraft.tools;

import org.lwjgl.util.vector.Vector3f;

import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;

public class ModelingGun {
	
	static Main main_prog;

	public enum InteractionMode {
		PELLET_GUN, LASER, ORB, EDITING, PAINTBRUSH
		// TODO: implement/move orb and editing tools over here or into something related like an "editing" gun
	}

	public static InteractionMode mode = InteractionMode.PELLET_GUN;

	public static void setInteractionMode(InteractionMode new_mode) {
		mode = new_mode;
	}

	public static void init() {
		LaserBeamPellet.laser_beam_pellet = new LaserBeamPellet(); 
		OrbPellet.orb_pellet = new OrbPellet(Main.all_pellets_in_world);
	}

	public static void useGun(Main main) {
		main_prog = main;
		setInteractionMode(ModelingGun.InteractionMode.PELLET_GUN);
		init();
	}
	
	public static void useLaser(Main main) {
		main_prog = main;
		setInteractionMode(ModelingGun.InteractionMode.LASER);
		init();
	}
	
	public static void usePaintbrush(){
		setInteractionMode(ModelingGun.InteractionMode.PAINTBRUSH);
		init();
	}
	
	public static void update(Vector3f pos, Vector3f gun_direction, float pan_angle, float tilt_angle){
		if (mode == InteractionMode.ORB) {
			OrbPellet.updateOrbPellet(pos, gun_direction, pan_angle, tilt_angle);
		} else if (mode == InteractionMode.LASER) {
			LaserBeamPellet.updateGun(pos, gun_direction);
			LaserBeamPellet.laser_beam_pellet.pellet_type = Main.which_gun;
		}
	}
	
	public static void shootGun() {
		System.out.println("which gun: " + Main.which_gun);
		// don't shoot when no pellets are there to draw
		if (!Main.draw_pellets || Main.which_gun == GunMode.DISABLED)
			return;

		/*
		if (Main.which_gun == GunMode.ORB) {
			OrbPellet new_pellet = new OrbPellet(Main.all_pellets_in_world);
			new_pellet.pos.set(OrbPellet.orb_pellet.pos);
			new_pellet.constructing = true;
			Main.all_pellets_in_world.add(new_pellet);
			System.out.println(Main.all_pellets_in_world);
		}
		if (Main.which_gun == GunMode.CAMERA) {
			System.out.println("catpure and send a screenshot");
			//CameraGun.takeSnapshot(this);
		} */
		
		if (mode == InteractionMode.PELLET_GUN) {
			firePellet();
		} else if (mode == InteractionMode.LASER) {
			placePellet();
		}

	}

	private static void placePellet() {
		if (InteractionMode.LASER == mode && LaserBeamPellet.laser_beam_pellet.visible){
			if (Main.which_gun == GunMode.CAMERA) {
				CameraGun.takeSnapshot(main_prog);
			}
			else {
				Pellet pellet = makeNewPellet();
				pellet.pos.set(LaserBeamPellet.laser_beam_pellet.pos);
				// pellet.constructing = true;
				pellet.max_radius = pellet.radius; // keep it from growing
				Main.all_pellets_in_world.add(pellet);
				Main.server.pelletFiredActionUpdate(pellet.getType());
			}
		}
		else if (InteractionMode.PAINTBRUSH == mode && PaintbrushPellet.laser_beam_pellet.visible){
			Pellet pellet = makeNewPellet();
			pellet.pos.set(PaintbrushPellet.laser_beam_pellet.pos);
			// pellet.constructing = true;
			pellet.max_radius = pellet.radius; // keep it from growing
			Main.all_pellets_in_world.add(pellet);
			Main.server.pelletFiredActionUpdate(pellet.getType());
		}
	}

	private static void firePellet() {
		// TODO Auto-generated method stub
		System.out.println("shooting gun");
		if (Main.which_gun == GunMode.CAMERA) {
			CameraGun.takeSnapshot(main_prog);
		}
		else {
			Pellet pellet = makeNewPellet();
	
			Main.computeGunDirection();
			pellet.pos.set(Main.getTransformedPos());
			pellet.vel.set(Main.gun_direction);
			pellet.vel.scale(Main.gun_speed);
			pellet.vel.scale(Main.pellet_scale);
	
			Main.all_pellets_in_world.add(pellet);
			Main.server.pelletFiredActionUpdate(pellet.getType());
		}
	}

	public static Pellet makeNewPellet() {
		Pellet pellet = null;
		if (Main.which_gun == GunMode.PELLET) {
			pellet = new ScaffoldPellet();
		} else if (Main.which_gun == GunMode.PLANE) {
			pellet = new PlanePellet();
		} else if (Main.which_gun == GunMode.LINE) {
			pellet = new LinePellet();
		} else if (Main.which_gun == GunMode.VERTICAL_LINE) {
			pellet = new VerticalLinePellet();
		} else if (Main.which_gun == GunMode.DESTRUCTOR) {
			pellet = new DestructorPellet();
		} else if (Main.which_gun == GunMode.DIRECTION_PICKER) {
			pellet = new UpPellet();
		} else if (Main.which_gun == GunMode.TRIANGULATION) {
			pellet = new TriangulationPellet();
		} else if (Main.which_gun == GunMode.TUTORIAL) {
			pellet = new TutorialPellet();
		} else if (Main.which_gun == GunMode.BOX) {
			pellet = new BoxPellet();
		} else if (Main.which_gun == GunMode.CYLINDER) {
			pellet = new CylinderPellet();
		} else if (Main.which_gun == GunMode.CIRCLE) {
			pellet = new CirclePellet();
		} else if (Main.which_gun == GunMode.DOME) {
			pellet = new DomePellet();
		} else if (Main.which_gun == GunMode.EXTRUDE_LINE) {
			pellet = new ExtrudeLinePellet();
		} else if (Main.which_gun == GunMode.EXTRUDE_POLYGON) {
			pellet = new ExtrudePolyPellet();
		} else if (Main.which_gun == GunMode.PAINTBRUSH) {
			pellet = new PaintbrushPellet();
		}  else {
			pellet = new PolygonPellet();
		}
		return pellet;
	}

	public static void shootDeleteGun() {
		// don't shoot when no pellets are there to draw
		if (!Main.draw_pellets)
			return;

		System.out.println("shooting DESTRUCTOR gun");
		Pellet pellet = new DestructorPellet();
		pellet.vel.set(Main.gun_direction);
		pellet.vel.scale(Main.gun_speed);
		pellet.vel.scale(Main.pellet_scale);
		pellet.pos.set(Main.getTransformedPos());
		Main.all_pellets_in_world.add(pellet);

	}
}
