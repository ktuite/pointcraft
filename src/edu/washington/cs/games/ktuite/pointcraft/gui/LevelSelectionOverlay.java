package edu.washington.cs.games.ktuite.pointcraft.gui;

import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.ResizableFrame.ResizableAxis;
import de.matthiasmann.twl.ScrollPane;
import de.matthiasmann.twl.TextArea;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.textarea.HTMLTextAreaModel;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.ActivityMode;
import edu.washington.cs.games.ktuite.pointcraft.levels.BaseLevel;
import edu.washington.cs.games.ktuite.pointcraft.levels.CubeLevel;

public class LevelSelectionOverlay extends Widget {

	final ResizableFrame frame;
	final ScrollPane scroll;
	private Main main;

	public LevelSelectionOverlay(Main main_program) {
		main = main_program;
		frame = new ResizableFrame();
		frame.setTitle("Choose a level");
		frame.setResizableAxis(ResizableAxis.NONE);

		
		String level_description_html = "<a href='random'><p>Level 1: random</p><img src='level1' /></a>" +
				"<br />" +
				"<a href='cube'><p>Level 2: cube</p><img src='level2' /></a>";
		
		
		final HTMLTextAreaModel textAreaModel = new HTMLTextAreaModel(level_description_html);
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

	@Override
	protected void layout() {
		super.layout();
		
		frame.setSize(400, 300);
		frame.setPosition(getInnerX() + (getInnerWidth() - frame.getWidth())
				/ 2, getInnerY() + (getInnerHeight() - frame.getHeight())/2);
		
		
        scroll.updateScrollbarSizes();
	}
	
	public void launchNewLevel(String level_name){
		if (level_name.contentEquals("random")){
			main.current_level = new BaseLevel(main);
		}
		else if (level_name.contentEquals("cube")){
			main.current_level = new CubeLevel(main);
		}
		Main.setActivityMode(ActivityMode.MODELING);
	}
}
