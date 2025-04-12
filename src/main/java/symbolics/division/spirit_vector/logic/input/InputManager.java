package symbolics.division.spirit_vector.logic.input;

import java.util.Arrays;
import java.util.function.Supplier;

public final class InputManager extends FeedbackManager<Input> {
	public static Supplier<Boolean> chatInputFilter = () -> true;

	public InputManager() {
		super(Arrays.asList(Input.values()));
	}

	@Override
	public void update(Input input, boolean value) {
		if (input.equals(Input.CROUCH) && chatInputFilter.get()) return;
		super.update(input, value);
	}
}
