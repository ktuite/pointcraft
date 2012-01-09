package edu.washington.cs.games.ktuite.pointcraft;

import java.io.File;

import org.lwjgl.input.Mouse;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class InstructionalOverlay extends Widget {

	private Main main_program;

	public Label a_label;
	private Button start_button;

	private Button save_button;
	private Button load_button;

	private Button export_button;

	private Button load_ply_button;
	
	private Button toggle_minecraft_controls;

	public void setPointerToMainProgram(Main m) {
		main_program = m;
	}

	public InstructionalOverlay() {
		a_label = new Label("Navitation Controls:\n\n  "
				+ "Use the mouse to look around.\n  "
				+ "Walk around with WASD keys.\n  "
				+ "Press SPACE to slow down.\n  "
				+ "Move vertically up and down with E and Q keys." + "\n\n\n"
				+ "Pellet Gun Controls:\n\n  " + "Click to shoot.\n  "
				+ "Press number keys 1-9 to change mode.\n  "
				+ "Right-click to shoot a destructor pellet.\n  "
				+ "Scroll to change size of pellet." + "\n\n\n"
				+ "Press ESC any time to return to this screen.");

		start_button = new Button("Start modeling");
		save_button = new Button("Save progress");
		load_button = new Button("Load progress");
		export_button = new Button("Export as .ply");
		load_ply_button = new Button("Load ply");
		toggle_minecraft_controls = new Button("Use Minecraft controls");

		start_button.addCallback(new Runnable() {
			public void run() {
				Mouse.setGrabbed(true);
			}
		});

		save_button.addCallback(new Runnable() {
			public void run() {
				Save.saveModel();
			}
		});

		load_button.addCallback(new Runnable() {
			public void run() {
				Save.loadModel();
			}
		});

		export_button.addCallback(new Runnable() {
			public void run() {
				Save.savePly();
			}
		});

		load_ply_button.addCallback(new Runnable() {
			public void run() {
				File file = Save.loadPointCloud();
				if (file != null) {
					main_program.loadNewPointCloud(file);
				}
			}
		});
		
		toggle_minecraft_controls.addCallback(new Runnable() {
			public void run() {
				Main.IS_MINECRAFT_CONTROLS = !Main.IS_MINECRAFT_CONTROLS;
			}
		});
		
		add(start_button);
		add(save_button);
		add(load_button);
		add(export_button);
		add(load_ply_button);
		add(toggle_minecraft_controls);
		// add(a_label);
	}

	@Override
	protected void layout() {
		a_label.adjustSize();
		a_label.setPosition(40, 180);

		/*
		 * start_button.adjustSize(); save_button.adjustSize();
		 * load_button.adjustSize();
		 * 
		 * save_button.setPosition(getInnerX() + (getInnerWidth() -
		 * save_button.getWidth()) / 2, getInnerHeight() - 100);
		 * start_button.setPosition(save_button.getX() - 50 -
		 * start_button.getWidth(), save_button.getY());
		 * load_button.setPosition(save_button.getX() + 50 +
		 * load_button.getWidth(), save_button.getY());
		 */
		
		toggle_minecraft_controls.adjustSize();
		toggle_minecraft_controls.setPosition(getInnerWidth() - save_button.getWidth() - 260,
				getInnerY() + 100);

		save_button.adjustSize();
		save_button.setPosition(getInnerWidth() - save_button.getWidth() - 260,
				getInnerY() + 30);

		load_button.adjustSize();
		load_button.setPosition(save_button.getX(), save_button.getY() + 30);

		export_button.adjustSize();
		export_button.setPosition(save_button.getX() + save_button.getWidth()
				+ 10, save_button.getY());

		load_ply_button.adjustSize();
		load_ply_button.setPosition(
				export_button.getX() + export_button.getWidth() + 10,
				export_button.getY());

		start_button.adjustSize();
		start_button.setPosition(
				getInnerX() + (getInnerWidth() - start_button.getWidth()) / 2,
				getInnerHeight() - 70);

	}
}
