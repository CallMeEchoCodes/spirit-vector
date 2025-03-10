package symbolics.division.spirit_vector.logic.input;

import java.util.Arrays;

public final class InputManager extends FeedbackManager<Input> {
	public InputManager() {
		super(Arrays.asList(Input.values()));
	}
}
