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
	private LinkedList<String> level_file;
	private LinkedList<Float> level_world_scale;

	public LevelSelectionOverlay(Main main_program) {
		main = main_program;
		frame = new ResizableFrame();
		frame.setTitle("Choose a level");
		frame.setResizableAxis(ResizableAxis.NONE);
		levels = new LinkedList<Class<? extends BaseLevel>>();
		level_file = new LinkedList<String>();
		level_world_scale = new LinkedList<Float>();

		addLevelGroup("Navigation");
		addLevel(NavigationOneCube.class, "Straight Ahead");
		addLevel(NavigationThreeCubes.class, "Left and Right");
		addLevel(NavigationNineCubes.class, "Up and Down");

		addLevelGroup("Shooting");
		addLevel(ShootingOneCube.class, "Shooting Straight");
		addLevel(ShootingFourCubes.class, "Multiple Targets");
		addLevel(ShootingEightCubes.class, "Hit it From Behind");

		addLevelGroup("Advanced Shooting");
		addLevel(ShootingPelletsOneCube.class, "Shooting Pellets");
		addLevel(ShootingPelletsNestedCubes.class,
				"Be Careful What you Aim For");
		addLevel(ShootingPelletsMoreNestedCubes.class, "Hide the Point Cloud");

		addLevelGroup("Simple Polygons");
		addLevel(CustomLevelFromFile.class, "Two Shapes", "data/twoshapes.ply", .25f);
		addLevel(CubeLevel.class, "Colored Cube");
		addLevel(CustomLevelFromFile.class, "Simple House", "data/simplehouse.ply", .25f);
		
		addLevelGroup("Real Models");
		addLevel(CustomLevelFromFile.class, "Lewis Hall", "data/lewis.ply", 1);
		addLevel(CustomLevelFromFile.class, "Red Square (UW)", "data/red_square.ply", 1);
		addLevel(CustomLevelFromFile.class, "Observatory", "data/observatory.ply", 1);
		addLevel(CustomLevelFromFile.class, "Johnson Hall", "data/johnson.ply", 1);
		addLevel(CustomLevelFromFile.class, "Uris Library", "data/uris.ply", 1);
		addLevel(CustomLevelFromFile.class, "Flower", "data/flower.ply", 1);
		

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

	private void addLevel(Class<? extends BaseLevel> c, String name,
			String filename, float scale) {
		addLevel(c, name);
		level_file.removeLast();
		level_file.add(filename);
		level_world_scale.set(level_world_scale.size() - 1, scale);
	}

	private void addLevel(Class<? extends BaseLevel> c, String name) {
		level_description_html += "<a href='" + levels.size() + "'><p>&nbsp;" + name
				+ "</p></a>\n";
		levels.add(c);
		level_file.add(null);
		level_world_scale.add((float) 1);
	}

	private void addLevelGroup(String string) {
		level_description_html += "<h1>" + string + ":</h1>\n";
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
				if (level_file.get(chosen_level) == null) {
					main.current_level = levels.get(chosen_level)
							.getConstructor(Main.class).newInstance(main);
					Main.setActivityMode(ActivityMode.TUTORIAL);
				} else {
					String filename = level_file.get(chosen_level);
					Float scale = level_world_scale.get(chosen_level);
					main.current_level = levels.get(chosen_level)
							.getConstructor(Main.class, String.class, Float.class)
							.newInstance(main, filename, scale);
					Main.setActivityMode(ActivityMode.MODELING);
				}
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
			//Main.setActivityMode(ActivityMode.TUTORIAL);
		}

	}

	public void advanceLevel() {
		setVisible(true);
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
			setVisible(false);
			Main.setActivityMode(ActivityMode.TUTORIAL);
		} else {
			Main.setActivityMode(ActivityMode.LEVEL_SELECTION);
		}
	}
}
