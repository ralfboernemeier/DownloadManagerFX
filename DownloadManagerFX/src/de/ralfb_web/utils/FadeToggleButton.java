package de.ralfb_web.utils;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.scene.control.Button;
import javafx.util.Duration;

public class FadeToggleButton {

	private FadeTransition fadeShowButtonOne;
	private FadeTransition fadeShowButtonTwo;
	private Button buttonOne;
	private Button buttonTwo;

	public FadeToggleButton(Button buttonOne, Button buttonTwo) {
		super();
		this.buttonOne = buttonOne;
		this.buttonTwo = buttonTwo;

		// Initialize FadTransition Show/Hide Server Root Password Button
		fadeShowButtonOne = new FadeTransition(Duration.millis(300), buttonOne);
		fadeShowButtonTwo = new FadeTransition(Duration.millis(300), buttonTwo);
	}

	/**
	 * Method that set the opacity of the Show/Hide button relative to the given
	 * option. If start = true set the opacity of the start button to 0 and of the
	 * stop button to 1 and vice versa in case start is false. This is like a
	 * ToggleButton using two different images and a 300ms Fade Transition.
	 * 
	 * @param show Boolean show = true: Show Button disabled and invisible Hide
	 *             Button enabled and visible / show = false: Show Button enabled
	 *             and visible Hide Button disable and invisible
	 */

	public void setFadeTransitionShowButtonOne(boolean showButton) {
		if (!showButton) {
			fadeShowButtonTwo.setFromValue(0.0);
			fadeShowButtonTwo.setToValue(1.0);
			fadeShowButtonTwo.setAutoReverse(false);
			fadeShowButtonTwo.setCycleCount(1);
			fadeShowButtonTwo.setOnFinished(e -> buttonTwo.setOpacity(1.0));
			fadeShowButtonTwo.setOnFinished(e -> buttonTwo.setDisable(false));
			fadeShowButtonOne.setFromValue(1.0);
			fadeShowButtonOne.setToValue(0.0);
			fadeShowButtonOne.setAutoReverse(false);
			fadeShowButtonOne.setCycleCount(1);
			fadeShowButtonOne.setOnFinished(e -> buttonOne.setOpacity(0.0));
			fadeShowButtonOne.setOnFinished(e -> buttonOne.setDisable(true));
			ParallelTransition pt = new ParallelTransition(fadeShowButtonTwo, fadeShowButtonOne);
			pt.play();
		} else {
			fadeShowButtonTwo.setFromValue(1.0);
			fadeShowButtonTwo.setToValue(0.0);
			fadeShowButtonTwo.setAutoReverse(false);
			fadeShowButtonTwo.setCycleCount(1);
			fadeShowButtonTwo.setOnFinished(e -> buttonTwo.setOpacity(0.0));
			fadeShowButtonTwo.setOnFinished(e -> buttonTwo.setDisable(true));
			fadeShowButtonOne.setFromValue(0.0);
			fadeShowButtonOne.setToValue(1.0);
			fadeShowButtonOne.setAutoReverse(false);
			fadeShowButtonOne.setCycleCount(1);
			fadeShowButtonOne.setOnFinished(e -> buttonOne.setOpacity(1.0));
			fadeShowButtonOne.setOnFinished(e -> buttonOne.setDisable(false));
			ParallelTransition pt = new ParallelTransition(fadeShowButtonOne, fadeShowButtonTwo);
			pt.play();
		}
	}

}