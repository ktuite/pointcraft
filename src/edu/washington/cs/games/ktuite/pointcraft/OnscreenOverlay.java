package edu.washington.cs.games.ktuite.pointcraft;

import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class OnscreenOverlay extends Widget {

	public Label label_current_mode;
	public Label label_last_action;

	public OnscreenOverlay() {
		label_current_mode = new Label("Current mode: ??");
		add(label_current_mode);
		
		label_last_action = new Label("Last action: no actions yet");
		label_last_action.setTheme("sublabel");
		add(label_last_action);
	}

	@Override
	protected void layout() {
		label_current_mode.adjustSize();
		label_current_mode.setPosition(10, 10);
		
		label_last_action.adjustSize();
		label_last_action.setPosition(10, 30);
	}
}
