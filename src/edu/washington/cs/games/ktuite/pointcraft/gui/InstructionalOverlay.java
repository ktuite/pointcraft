package edu.washington.cs.games.ktuite.pointcraft.gui;

import java.io.File;

import org.lwjgl.input.Mouse;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.ToggleButton;
import de.matthiasmann.twl.model.SimpleBooleanModel;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.ActivityMode;
import edu.washington.cs.games.ktuite.pointcraft.Save;

public class InstructionalOverlay extends Widget {

	private Main main_program;

	public Label a_label;
	private Button start_button;

	private Button save_button;
	private Button load_button;

	private Button export_button;

	private Button load_ply_button;

	// minecraft controls / toggle button experiment
	private SimpleBooleanModel toggle_minecraft;
	private Label toggle_minecraft_label;
	private ToggleButton toggle_minecraft_checkbox;
	
	// cinematics mode
	private SimpleBooleanModel toggle_cinematics;
	private Label toggle_cinematics_label;
	private ToggleButton toggle_cinematics_checkbox;

	private Button load_cinematics_button;

	private Button save_cinematics_button;

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
		export_button = new Button("Export");
		load_ply_button = new Button("Load ply");
		save_cinematics_button = new Button("Save Scene");
		load_cinematics_button = new Button("Load Scene");

		// minecraft toggle setup
		toggle_minecraft = new SimpleBooleanModel();
		toggle_minecraft.setValue(Main.minecraft_flight);
		toggle_minecraft_checkbox = new ToggleButton(toggle_minecraft);
		toggle_minecraft_checkbox.setTheme("checkbox");
		toggle_minecraft_label = new Label("Minecraft controls");
		toggle_minecraft.addCallback(new Runnable() {
			public void run() {
				Main.minecraft_flight = toggle_minecraft.getValue();
			}
		});
		
		// cinematics mode setup
		toggle_cinematics = new SimpleBooleanModel();
		toggle_cinematics.setValue(Main.cinematics_mode);
		toggle_cinematics_checkbox = new ToggleButton(toggle_cinematics);
		toggle_cinematics_checkbox.setTheme("checkbox");
		toggle_cinematics_label = new Label("Cinematics mode");
		toggle_cinematics.addCallback(new Runnable() {
			public void run() {
				Main.cinematics_mode = toggle_cinematics.getValue();
			}
		});

		start_button.addCallback(new Runnable() {
			public void run() {
				Main.setActivityMode(ActivityMode.MODELING);
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

		if (Main.IS_SIGGRAPH_DEMO) {
			save_cinematics_button.addCallback(new Runnable() {
				public void run() {
					Save.saveCinematics();
				}
			});

			load_cinematics_button.addCallback(new Runnable() {
				public void run() {
					Save.loadCinematics();
				}
			});
			
			add(toggle_cinematics_label);
			add(toggle_cinematics_checkbox);
			add(save_cinematics_button);
			add(load_cinematics_button);
		}

		add(start_button);
		add(save_button);
		add(load_button);
		add(export_button);
		add(load_ply_button);

		
		add(toggle_minecraft_label);
		add(toggle_minecraft_checkbox);
		


	}

	@Override
	protected void layout() {
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

		save_cinematics_button.adjustSize();
		save_cinematics_button.setPosition(
				load_button.getX() + load_button.getWidth() + 10,
				load_button.getY());

		load_cinematics_button.adjustSize();
		load_cinematics_button.setPosition(save_cinematics_button.getX()
				+ save_cinematics_button.getWidth() + 10,
				save_cinematics_button.getY());
		
		toggle_minecraft_checkbox.adjustSize();
		toggle_minecraft_checkbox.setPosition(
				save_button.getInnerX(),
				getInnerY() + 100);
		
		toggle_minecraft_label.adjustSize();
		toggle_minecraft_label.setPosition(
				toggle_minecraft_checkbox.getInnerX() + 30, toggle_minecraft_checkbox.getInnerY());
		
		toggle_cinematics_checkbox.adjustSize();
		toggle_cinematics_checkbox.setPosition(
				save_button.getInnerX(),
				getInnerY() + 130);
		
		toggle_cinematics_label.adjustSize();
		toggle_cinematics_label.setPosition(
				toggle_cinematics_checkbox.getInnerX() + 30, toggle_cinematics_checkbox.getInnerY());
	}
}
