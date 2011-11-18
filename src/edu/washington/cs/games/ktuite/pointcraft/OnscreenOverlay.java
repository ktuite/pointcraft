package edu.washington.cs.games.ktuite.pointcraft;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class OnscreenOverlay extends Widget {

	public Label label_current_mode;

	public OnscreenOverlay() {
		label_current_mode = new Label("Current mode: ??");
		add(label_current_mode);
	}

	@Override
	protected void layout() {
		label_current_mode.adjustSize();
		label_current_mode.setPosition(10, 10);
	}
}
