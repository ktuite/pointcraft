package edu.washington.cs.games.ktuite.pointcraft.gui;

import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ResizableFrame.ResizableAxis;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.ActivityMode;
import edu.washington.cs.games.ktuite.pointcraft.levels.*;

public class LevelSelectionOverlay extends Widget {

	final ResizableFrame frame;
	final ScrollPane scroll;
	private Main main;
	public LinkedList<Class<? extends BaseLevel>> levels;
	private String level_description_html = "";

	public LevelSelectionOverlay(Main main_program) {
		main = main_program;
		frame = new ResizableFrame();
		frame.setTitle("Choose a level");
		frame.setResizableAxis(ResizableAxis.NONE);
		levels = new LinkedList<Class<? extends BaseLevel>>();

		addLevelGroup("Navigation");
		addLevel(NavigationOneCube.class, "Level 1.1");
		addLevel(NavigationThreeCubes.class, "Level 1.2");
		addLevel(NavigationNineCubes.class, "Level 1.3");

		addLevelGroup("Shooting");
		addLevel(ShootingOneCube.class, "Level 2.1");
		addLevel(ShootingFourCubes.class, "Level 2.2");
		addLevel(ShootingEightCubes.class, "Level 2.3");

		final HTMLTextAreaModel textAreaModel = new HTMLTextAreaModel(
				level_description_html);
		final TextArea textArea = new TextArea(textAreaModel);

		textArea.addCallback(new TextArea.Callback() {
			public void handleLinkClicked(String href) {
				launchNewLevel(href);
			}
		});

		scroll = new ScrollPane(textArea);
		scroll.setExpandContentSize(true);

		frame.add(scroll);
		add(frame);
	}

	private void addLevel(Class<? extends BaseLevel> c, String name) {
		level_description_html += "<a href='" + levels.size() + "'><p>Level "
				+ name + "</p></a>\n";
		levels.add(c);
	}

	private void addLevelGroup(String string) {
		level_description_html += "<h1>--" + string + "--</h1>\n";
	}

	@Override
	protected void layout() {
		super.layout();

		frame.setSize(400, 300);
		frame.setPosition(getInnerX() + (getInnerWidth() - frame.getWidth())
				/ 2, getInnerY() + (getInnerHeight() - frame.getHeight()) / 2);

		scroll.updateScrollbarSizes();
	}

	public void launchNewLevel(String level_name) {
		int chosen_level = Integer.parseInt(level_name);
		if (chosen_level >= 0 && chosen_level < levels.size()) {
			try {
				main.current_level = levels.get(chosen_level)
						.getConstructor(Main.class).newInstance(main);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Main.setActivityMode(ActivityMode.TUTORIAL);
		}

	}

	public void advanceLevel() {
		int chosen_level = levels.indexOf(main.current_level.getClass()) + 1;
		if (chosen_level < levels.size()) {
			try {
				main.current_level = levels.get(chosen_level)
						.getConstructor(Main.class).newInstance(main);
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvocationTargetException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (NoSuchMethodException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Main.setActivityMode(ActivityMode.TUTORIAL);
		} else {
			Main.setActivityMode(ActivityMode.LEVEL_SELECTION);
		}
	}
}
