package symbolics.division.spirit_vector.logic.move;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.input.InputManager;
import symbolics.division.spirit_vector.logic.state.ManagedState;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class SpellMovement extends NeutralMovement {
	private static final Identifier CASTING_STATE_ID = SpiritVectorMod.id("casting_spell");
	private static final int MAX_CASTING_TICKS = 20 * 5;

	public SpellMovement(Identifier id) {
		super(id);
	}

	@Override
	public void configure(SpiritVector sv) {
		sv.stateManager().register(CASTING_STATE_ID, new ManagedState(sv));
	}

	@Override
	public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
		InputManager input = sv.inputManager();
		if (
			!sv.user.isOnGround() &&
			input.pressed(Input.CROUCH) &&
			input.pressed(Input.SPRINT) &&
			input.pressed(Input.JUMP)
		) {
			input.consume(Input.CROUCH);
			input.consume(Input.SPRINT);
			input.consume(Input.JUMP);
//			SpiritVectorMod.LOGGER.info("spell dimension!");
			sv.stateManager().enableStateFor(CASTING_STATE_ID, MAX_CASTING_TICKS);
			return true;
		}
		return false;
	}

	@Override
	public boolean testMovementCompleted(SpiritVector sv, TravelMovementContext ctx) {
		return !sv.stateManager().isActive(CASTING_STATE_ID);
	}

	@Override
	public void exit(SpiritVector sv, TravelMovementContext ctx) {
		sv.effectsManager().spawnRing(sv.user.getPos(), new Vec3d(0 ,1 ,0));
		sv.user.setVelocity(
			MovementUtils.augmentedInput(sv, ctx).multiply(0.6).add(0, 0.5, 0)
		);
	}

	@Override
	public void travel(SpiritVector sv, TravelMovementContext ctx) {
		if (sv.inputManager().consume(Input.JUMP)) {
//			SpiritVectorMod.LOGGER.info("spell cast!");
			sv.stateManager().clearTicks(CASTING_STATE_ID);
		}
		SlideMovement.travelWithInput(sv, Vec3d.ZERO);
		ctx.ci().cancel();
	}
}
