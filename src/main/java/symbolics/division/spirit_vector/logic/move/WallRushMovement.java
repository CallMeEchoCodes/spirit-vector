package symbolics.division.spirit_vector.logic.move;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.state.ManagedState;
import symbolics.division.spirit_vector.logic.state.ParticleTrailEffectState;
import symbolics.division.spirit_vector.logic.vector.DreamVector;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.logic.vector.VectorType;

/*
Mixture of sliding and walljump

when crouching at wall in midair, do sliding movement (setting to
zero speed under a certain threshold for cling) and maintain until
cooldown.
 */

public class WallRushMovement extends AbstractMovementType {
	private static final Identifier WALL_CLING_STATE = SpiritVectorMod.id("wall_cling");
	private static final Identifier WALL_CLING_CD_STATE = SpiritVectorMod.id("wall_cling_cd");
	private static final int WALL_CLING_TICKS = 20 * 3;
	private static final int WALL_CLING_COOLDOWN_TICKS = 20 * 1;
	private static final float WALL_CLING_SPEED_THRESHOLD = 0.1f;
	private static boolean ready = false;

	public WallRushMovement(Identifier id) {
		super(id);
	}

	@Override
	public final void configure(SpiritVector sv) {
		sv.stateManager().register(WALL_CLING_STATE, new ManagedState(sv));
		sv.stateManager().register(WALL_CLING_CD_STATE, new ManagedState(sv));
		ready = true;
	}

	@Override
	public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
		if (!ready) {
			throw new RuntimeException("Wall rush movement was not configured!");
		}

		if (!sv.user.isOnGround()
			&& MovementUtils.idealWallrunningConditions(sv)
			&& sv.inputManager().rawInput(Input.CROUCH)) { // sticky, ignores consumption
			sv.inputManager().consume(Input.CROUCH);
			sv.stateManager().enableStateFor(
				WALL_CLING_STATE,
				sv.is(VectorType.DREAM) ? Integer.MAX_VALUE : WALL_CLING_TICKS
			);
			return true;
		}
		return false;
	}

	@Override
	public boolean testMovementCompleted(SpiritVector sv, TravelMovementContext ctx) {
		boolean completed = sv.inputManager().rawInput(Input.JUMP)
			|| !sv.inputManager().rawInput(Input.CROUCH)
			|| !MovementUtils.idealWallrunningConditions(sv);
		return completed;
	}

	@Override
	public void exit(SpiritVector sv) {
		if (!sv.stateManager().isActive(WALL_CLING_STATE)) { // avoid spam
			sv.stateManager().enableStateFor(WALL_CLING_CD_STATE, WALL_CLING_COOLDOWN_TICKS);
		}
	}

	@Override
	public void travel(SpiritVector sv, TravelMovementContext ctx) {
		// No DI, you either got it or you don't
		Vec3d vel = sv.user.getVelocity();
		double vy = 0;
		if (vel.y > 0) {
			// apply gravity only going up
			vy = vel.y - sv.user.getFinalGravity();
		} else if (!sv.stateManager().isActive(WALL_CLING_STATE) || sv.stateManager().isActive(WALL_CLING_CD_STATE)) {
			// apply small gravity (wall slide) if clinging too long
			vy = -Math.abs(sv.user.getFinalGravity() * 0.5f);
		}

		double speed = Math.sqrt(vel.x * vel.x + vel.z * vel.z);
		if (speed < WALL_CLING_SPEED_THRESHOLD) {
			vel = new Vec3d(0, vy, 0);

		} else {
			vel = vel.withAxis(Direction.Axis.Y, vy);
		}

		sv.user.setVelocity(vel);
		sv.user.move(net.minecraft.entity.MovementType.SELF, sv.user.getVelocity());
		if (speed > SlideMovement.MIN_SPEED_FOR_TRAIL) {
			sv.stateManager().enableStateFor(ParticleTrailEffectState.ID, 1);
		}

		ctx.ci().cancel();
	}

	@Override
	public void updateValues(SpiritVector sv) {
		if (sv.is(VectorType.BURST)) {
			Vec3d vel = sv.user.getVelocity();
			if (MathHelper.approximatelyEquals(Math.abs(vel.x) + Math.abs(vel.y) + Math.abs(vel.z), 0)) {
				return;
			}
		}
		if (sv.user.age % 10 == 0 && sv.stateManager().isActive(WALL_CLING_STATE)) {
			if (sv.is(VectorType.DREAM)) {
				sv.modifyMomentum(DreamVector.MOMENTUM_GAIN_PER_SECOND / 4);
			} else {
				sv.modifyMomentum(1);
			}
		}
	}
}
