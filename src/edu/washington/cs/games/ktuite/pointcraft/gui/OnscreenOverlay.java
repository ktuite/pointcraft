package edu.washington.cs.games.ktuite.pointcraft.gui;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.Color;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.Widget;
import de.matthiasmann.twl.utils.TintAnimator;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.ActivityMode;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;
import edu.washington.cs.games.ktuite.pointcraft.geometry.Scoring;

public class OnscreenOverlay extends Widget {

	public Label label_current_mode;
	public Label label_last_action;
	public Label label_score;
	public FullInventoryPanel full_inventory_panel;
	public OnscreenInventoryPanel onscreen_inventory_panel;
	final ResizableFrame full_frame;
	final ResizableFrame onscreen_frame;
	private Button instructions_button;
	private Button level_selection_button;
	
	private Label animated_label;
	private final TintAnimator tintAnimator;

	public OnscreenOverlay() {
		label_current_mode = new Label("Current mode: ??");
		add(label_current_mode);

		label_last_action = new Label("Last action: no actions yet");
		label_last_action.setTheme("sublabel");
		add(label_last_action);

		label_score = new Label("Score: 0");
		add(label_score);

		onscreen_inventory_panel = new OnscreenInventoryPanel(10, 1);
		full_inventory_panel = new FullInventoryPanel(onscreen_inventory_panel);
		instructions_button = new Button("Main Menu");
		instructions_button.addCallback(new Runnable() {
			public void run() {
				Main.setActivityMode(ActivityMode.INSTRUCTIONS);
			}
		});
		
		
		level_selection_button = new Button("Choose Level");
		level_selection_button.addCallback(new Runnable() {
			public void run() {
				Main.setActivityMode(ActivityMode.LEVEL_SELECTION);
			}
		});

		full_frame = new ResizableFrame();
		full_frame.setTitle("Drag and drop tools into your tool palette:");
		full_frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
		
		full_inventory_panel.add(level_selection_button);
		full_inventory_panel.add(instructions_button);
		full_frame.add(full_inventory_panel);
		add(full_frame);
		
		onscreen_frame = new ResizableFrame();
		onscreen_frame.setTitle("Current tool:");
		onscreen_frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
		onscreen_frame.add(onscreen_inventory_panel);
		add(onscreen_frame);
		
		animated_label = new Label("+5000");
		animated_label.setTheme("biglabel");
		tintAnimator = new TintAnimator(new TintAnimator.GUITimeSource(this));
		animated_label.setTintAnimator(tintAnimator);  
		animated_label.setPosition(-100, -100);
		add(animated_label);
	}

	@Override
	protected void layout() {
		super.layout();

		label_current_mode.adjustSize();
		label_current_mode.setPosition(10, 10);

		label_last_action.adjustSize();
		label_last_action.setPosition(10, 30);

		label_score.adjustSize();
		label_score.setPosition(getInnerWidth() - label_score.getWidth() - 10,
				10);

		positionFrame();
	}

	void positionFrame() {
		
		full_frame.adjustSize();
		full_frame.setPosition(getInnerX() + (getInnerWidth() - full_frame.getWidth())
				/ 2, getInnerY() + (getInnerHeight() - full_frame.getHeight())/2);
		
		instructions_button.adjustSize();
		instructions_button.setPosition(full_frame.getInnerX() + full_frame.getInnerWidth() - instructions_button.getWidth(), full_frame.getInnerY());
		
		level_selection_button.adjustSize();
		level_selection_button.setPosition(instructions_button.getInnerX() - level_selection_button.getWidth() - 5, full_frame.getInnerY());

		onscreen_frame.adjustSize();
		onscreen_frame.setPosition(getInnerX() + (getInnerWidth() - onscreen_frame.getWidth())
				/ 2, getInnerHeight() - onscreen_frame.getHeight() - 10);
	}

	public void updateCurrentTool(GunMode which_gun) {
		label_current_mode.setText("Current Gun: " + Main.which_gun);
		label_score.setText("Score: " + Scoring.points_explained);
		onscreen_frame.setTitle("Current Tool: " + which_gun);
		onscreen_inventory_panel.setSlotFromMode(which_gun);
	}
	
	public void moveLabelAround(){
		if (animated_label.getY() > 0)
			animated_label.setPosition(animated_label.getX(), animated_label.getY() - 3);
	}
	
	public void animateScore(int score){
		animated_label.setText("+" + score);
		animated_label.adjustSize();
		animated_label.setPosition(getWidth()/2, getHeight()/2);
		tintAnimator.setColor(new Color(0xffffffff));
		tintAnimator.fadeTo(new Color(0x00ffffff), 1500);
	}
	

}
