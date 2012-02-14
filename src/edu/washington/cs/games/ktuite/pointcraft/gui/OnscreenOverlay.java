package edu.washington.cs.games.ktuite.pointcraft.gui;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.ResizableFrame;
import de.matthiasmann.twl.Widget;
import edu.washington.cs.games.ktuite.pointcraft.Main;
import edu.washington.cs.games.ktuite.pointcraft.Main.GunMode;

public class OnscreenOverlay extends Widget {

	public Label label_current_mode;
	public Label label_last_action;
	public InventoryPanel inventory_panel;
	final ResizableFrame frame;

	public OnscreenOverlay() {
		label_current_mode = new Label("Current mode: ??");
		add(label_current_mode);
		
		label_last_action = new Label("Last action: no actions yet");
		label_last_action.setTheme("sublabel");
		add(label_last_action);
		
		inventory_panel = new InventoryPanel(10, 1);
		
		frame = new ResizableFrame();
		frame.setTitle("Current tool:");
		frame.setResizableAxis(ResizableFrame.ResizableAxis.NONE);
        frame.add(inventory_panel);
		add(frame);
		
		
	}

	@Override
	protected void layout() {
		super.layout();
		
		label_current_mode.adjustSize();
		label_current_mode.setPosition(10, 10);
		
		label_last_action.adjustSize();
		label_last_action.setPosition(10, 30);
		
		positionFrame();
	}
	
	void positionFrame() {
        frame.adjustSize();
        frame.setPosition(
                getInnerX() + (getInnerWidth() - frame.getWidth())/2,
                getInnerHeight() - frame.getHeight() - 10);
    }

	public void updateCurrentTool(GunMode which_gun) {
		label_current_mode.setText("Current Gun: "
				+ Main.which_gun);
		frame.setTitle("Current Tool: " + which_gun);
		inventory_panel.setSlotFromMode(which_gun);
		
	}
}
