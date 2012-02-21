package edu.washington.cs.games.ktuite.pointcraft.gui;

import java.io.IOException;
import java.net.URL;

import org.lwjgl.LWJGLException;
import org.lwjgl.opengl.GL11;
import org.newdawn.slick.util.ResourceLoader;

import de.matthiasmann.twl.GUI;
import de.matthiasmann.twl.renderer.lwjgl.LWJGLRenderer;
import de.matthiasmann.twl.theme.ThemeManager;
import edu.washington.cs.games.ktuite.pointcraft.ActionTracker;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;

public class GuiManager {

	private Main main_program;

	public GUI onscreen_gui;
	public GUI instructional_gui;
	public OnscreenOverlay onscreen_overlay;
	public InstructionalOverlay instruction_overlay;

	private GUI login_gui;

	public static boolean is_logged_in = !Main.IS_RELEASE;

	public GuiManager(Main m) {
		main_program = m;
		setUpGuis();
	}

	public void setUpGuis() {
		LWJGLRenderer renderer;
		try {
			renderer = new LWJGLRenderer();
			onscreen_overlay = new OnscreenOverlay();
			onscreen_gui = new GUI(onscreen_overlay, renderer);
			URL url = ResourceLoader.getResource("theme/onscreen.xml");
			ThemeManager themeManager = ThemeManager.createThemeManager(url,
					renderer);
			onscreen_gui.applyTheme(themeManager);

			instruction_overlay = new InstructionalOverlay();
			instruction_overlay.setPointerToMainProgram(main_program);
			instructional_gui = new GUI(instruction_overlay, renderer);
			URL url2 = ResourceLoader.getResource("theme/guiTheme.xml");
			ThemeManager themeManager2 = ThemeManager.createThemeManager(url2,
					renderer);
			instructional_gui.applyTheme(themeManager2);

			login_gui = new GUI(new LoginOverlay(), renderer);
			URL url3 = ResourceLoader.getResource("theme/login.xml");
			ThemeManager themeManager3 = ThemeManager.createThemeManager(url3,
					renderer);
			login_gui.applyTheme(themeManager3);

		} catch (LWJGLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public void updateLoginGui() {
		login_gui.update();
	}

	public void updateOnscreenGui() {
		if (onscreen_gui != null) {
			onscreen_overlay.updateCurrentTool(Main.which_gun);
			onscreen_overlay.label_last_action.setText("Last Action: "
					+ ActionTracker.showLatestAction());
			onscreen_gui.update();
		}
	}

	public void updateInstructionalGui() {
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
		if (instructional_gui != null) {
			instructional_gui.update();
		}
	}

	public void showOnscreenTools() {
		onscreen_overlay.onscreen_frame.setVisible(true);
		onscreen_overlay.full_frame.setVisible(false);
	}

	public void showFullscreenTools() {
		onscreen_overlay.full_frame.setVisible(true);
		onscreen_overlay.onscreen_frame.setVisible(false);
	}

	public GunMode getGunModeFromOnscreenToolPalette(int i) {
		GunMode mode = onscreen_overlay.onscreen_inventory_panel.slot[i].getGunMode();
		String item = onscreen_overlay.onscreen_inventory_panel.slot[i].getItem();
		if (mode != null && item != null){
			return mode;
		}
		else {
			return null;
		}
	}
}
