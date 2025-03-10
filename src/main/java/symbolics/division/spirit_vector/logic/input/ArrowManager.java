package symbolics.division.spirit_vector.logic.input;

import java.util.Arrays;

public class ArrowManager extends FeedbackManager<Arrow> {
	public ArrowManager() {
		super(Arrays.asList(Arrow.values()));
	}
}
