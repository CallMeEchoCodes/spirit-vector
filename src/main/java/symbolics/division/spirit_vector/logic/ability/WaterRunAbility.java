package symbolics.division.spirit_vector.logic.ability;

import net.minecraft.util.Identifier;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.move.MovementType;
import symbolics.division.spirit_vector.logic.state.ManagedState;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class WaterRunAbility extends AbstractSpiritVectorAbility {
	protected static final Identifier WATER_RUN_FLAG = SpiritVectorMod.id("water_run_flag");

	public WaterRunAbility(Identifier id) {
		super(id, Integer.MAX_VALUE);
	}

	public static boolean canWaterRun(SpiritVector sv) {
		return sv.stateManager().getOptional(WATER_RUN_FLAG).isPresent()
			&& sv.user.getVelocity().lengthSquared() > 0.3
			&& sv.getMomentum() > 0;
	}

	public static boolean isWaterRunning(SpiritVector sv) {
		return canWaterRun(sv) && sv.getMoveState() == MovementType.SLIDE && sv.user.isInFluid();
	}

	@Override
	public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
		return false;
	}

	@Override
	public void configure(SpiritVector sv) {
		sv.stateManager().register(WATER_RUN_FLAG, new ManagedState(sv));
	}

	@Override
	public void travel(SpiritVector sv, TravelMovementContext ctx) {
	}
}
