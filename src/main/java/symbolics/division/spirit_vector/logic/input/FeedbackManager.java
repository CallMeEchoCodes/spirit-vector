package symbolics.division.spirit_vector.logic.input;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class FeedbackManager<T> {
	/*
    Inputs MUST be consumed at the end of all logic chains testing for activation.
    This means that if an input is consumed, it MUST produce a user-facing action.

    In addition, inputs should always be consumed if they are expected to be the primary
    means of firing some event that expects to be exclusive to that input.
     */

	private final Map<T, Boolean> trackedStates = new HashMap<>();
	private final Map<T, Boolean> publicStates = new HashMap<>();
	private final Map<T, Boolean> releasedStates = new HashMap<>();

	protected FeedbackManager(Collection<T> values) {
		for (T input : values) {
			trackedStates.put(input, false);
			publicStates.put(input, false);
			releasedStates.put(input, false);
		}
	}

	public boolean consume(T input) {
		var result = pressed(input);
		if (result) {
			publicStates.put(input, false);
		}
		return result;
	}

	public boolean pressed(T input) { // check absolute state without consuming
		return publicStates.get(input);
	}

	public boolean released(T input) {
		return releasedStates.get(input);
	}

	public void update(T input, boolean value) {
		releasedStates.put(input, false);
		if (trackedStates.get(input) != value) {
			// if key released and was not consumed, broadcast it was released
			if (!value && publicStates.get(input)){
				releasedStates.put(input, true);
			}

			trackedStates.put(input, value);
			publicStates.put(input, value);
		}
	}

	// mainly for debug, see whether input is consumed
	public boolean rawInput(T input) {
		return trackedStates.get(input);
	}

	public void consumeAll() {
		for (T v : trackedStates.keySet()) consume(v);
	}
}
