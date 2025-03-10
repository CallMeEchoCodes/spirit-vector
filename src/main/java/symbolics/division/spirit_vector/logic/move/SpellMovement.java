package symbolics.division.spirit_vector.logic.move;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.input.ArrowManager;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.input.InputManager;
import symbolics.division.spirit_vector.logic.input.Arrow;
import symbolics.division.spirit_vector.logic.spell.Spell;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;
import symbolics.division.spirit_vector.logic.state.ManagedState;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.ArrayList;
import java.util.List;

public class SpellMovement extends NeutralMovement {
	private static final Identifier CASTING_STATE_ID = SpiritVectorMod.id("casting_spell");
	private static final int MAX_CASTING_TICKS = 20 * 5;

	public SpellMovement(Identifier id) {
		super(id);
	}

	@Override
	public void configure(SpiritVector sv) {
		sv.stateManager().register(CASTING_STATE_ID, new SpellcastingState(sv));
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
			((SpellcastingState)sv.stateManager().getState(CASTING_STATE_ID)).resetInputs();
			sv.arrowManager().consumeAll();
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
		((SpellcastingState)sv.stateManager().getState(CASTING_STATE_ID)).resetInputs();
	}

	@Override
	public void travel(SpiritVector sv, TravelMovementContext ctx) {
		if (sv.inputManager().consume(Input.JUMP)) {
			SpellDimension.cast(new Spell(sv, ((SpellcastingState)sv.stateManager().getState(CASTING_STATE_ID)).eigenCode()));
			sv.stateManager().clearTicks(CASTING_STATE_ID);
		}
		SlideMovement.travelWithInput(sv, Vec3d.ZERO);
		ctx.ci().cancel();
	}

	public static List<Arrow> getCurrentEigenCode(SpiritVector sv) {
		return List.copyOf(((SpellcastingState)sv.stateManager().getState(CASTING_STATE_ID)).eigenCode());
	}

	private static class SpellcastingState extends ManagedState {
		private final List<Arrow> eigenCode = new ArrayList<>();


		public SpellcastingState(SpiritVector sv) {
			super(sv);
		}

		public void resetInputs() {
			eigenCode.clear();
		}

		@Override
		public void tick() {
			super.tick();
			if (isActive() && eigenCode.size() < Spell.MAX_CODE_LENGTH) {
				ArrowManager arrows = sv.arrowManager();
				if (arrows.consume(Arrow.UP)) {
					eigenCode.add(Arrow.UP);
				} else if (arrows.consume(Arrow.DOWN)) {
					eigenCode.add(Arrow.DOWN);
				} else if (arrows.consume(Arrow.LEFT)) {
					eigenCode.add(Arrow.LEFT);
				} else if (arrows.consume(Arrow.RIGHT)) {
					eigenCode.add(Arrow.RIGHT);
				}
			}
		}

		public List<Arrow> eigenCode() {
			return List.copyOf(eigenCode);
		}
	}
}
