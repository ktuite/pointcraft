package edu.washington.cs.games.ktuite.pointcraft;

import de.matthiasmann.twl.Button;
import de.matthiasmann.twl.FPSCounter;
import de.matthiasmann.twl.Label;
import de.matthiasmann.twl.Widget;

public class OnscreenOverlay extends Widget {

	public Label label_current_mode;
	private Button button;
	private FPSCounter fpsCounter;

	public OnscreenOverlay() {
		button = new Button("epic button");
		label_current_mode = new Label("Current mode: ??");

		fpsCounter = new FPSCounter();

		//add(button);
		//add(fpsCounter);
		add(label_current_mode);
	}

	@Override
	protected void layout() {

		button.adjustSize();
		button.setPosition(
				getInnerX() + (getInnerWidth() - button.getWidth()) / 2,
				getInnerY() + (getInnerHeight() - button.getHeight()) / 2);

		fpsCounter.adjustSize();
		fpsCounter.setPosition(10, 10);
		
		label_current_mode.adjustSize();
		label_current_mode.setPosition(10, 10);
	}
}
