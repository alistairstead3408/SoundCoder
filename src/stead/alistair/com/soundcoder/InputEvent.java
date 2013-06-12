package stead.alistair.com.soundcoder;

import android.view.MotionEvent;

public class InputEvent {

	private int meAction;
	private int state;
	private float x;
	private float y;
	private long timeSinceDown;
	private long eventTime;

	public InputEvent(MotionEvent meInput, int stateInput, float xInput,
			float yInput, long timeSinceDownInput, long eventTimeInput) {
		meAction = meInput.getAction();
		state = stateInput;
		x = xInput;
		y = yInput;
		timeSinceDown = timeSinceDownInput;
		eventTime = eventTimeInput;
	}

	public int getState() {
		return state;
	}

	public int getMotionEventAction() {
		return meAction;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public long getTimeSinceDown() {
		return timeSinceDown;
	}

	public long getEventTime() {
		return eventTime;
	}

}
