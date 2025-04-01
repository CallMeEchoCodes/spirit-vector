package symbolics.division.spirit_vector.logic.state;

import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.function.Supplier;

public class CoyoteTime extends ManagedState {
	private Supplier<Boolean> action = null;
	public CoyoteTime(SpiritVector sv) {
		super(sv);
	}

	public void set(Supplier<Boolean> action, int ticks) {
		this.enableFor(ticks);
		this.action = action;
	}

	@Override
	public void tick() {
		if (ticksLeft > 0 && action != null && action.get()) {
			this.ticksLeft = 0;
			this.action = null;
		}
		super.tick();
	}
}
