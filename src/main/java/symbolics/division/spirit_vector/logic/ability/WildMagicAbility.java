package symbolics.division.spirit_vector.logic.ability;

import net.minecraft.util.Identifier;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.state.ManagedState;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class WildMagicAbility extends AbstractSpiritVectorAbility {
	protected static final Identifier WILD_MAGIC_FLAG = SpiritVectorMod.id("wild_magic");

	public static boolean xyzzy(SpiritVector sv) {
		return sv.stateManager().getOptional(WILD_MAGIC_FLAG).isPresent();
	}

	public WildMagicAbility(Identifier id) {
		super(id, Integer.MAX_VALUE);
	}

	@Override
	public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
		return false;
	}

	@Override
	public void configure(SpiritVector sv) {
		if (sv.stateManager().getOptional(WILD_MAGIC_FLAG).isEmpty())
			sv.stateManager().register(WILD_MAGIC_FLAG, new ManagedState(sv));
	}

	@Override public void travel(SpiritVector sv, TravelMovementContext ctx) {}
}
