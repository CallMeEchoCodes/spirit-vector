package symbolics.division.spirit_vector.compat;

import dev.doublekekse.zipline.Cable;
import dev.doublekekse.zipline.Cables;
import dev.doublekekse.zipline.registry.ZiplineSoundEvents;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.api.SpiritVectorApi;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.move.AbstractMovementType;
import symbolics.division.spirit_vector.logic.move.MovementType;
import symbolics.division.spirit_vector.logic.move.MovementUtils;
import symbolics.division.spirit_vector.logic.state.ManagedState;
import symbolics.division.spirit_vector.logic.state.ParticleTrailEffectState;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.Collection;

public class ZiplineCompat implements ModCompatibility {
	@Override
	public void initialize(String modid, boolean inDev) {
		SpiritVectorMod.LOGGER.debug("Zipline setup");
		SpiritVectorApi.registerMovement(new ZiplineGrindMovement(SpiritVectorMod.id("compat.zipline_grind")), MovementType.JUMP, SpiritVectorApi.MovementOrder.AFTER);
	}

	private static class ZiplineGrindMovement extends AbstractMovementType {
		private static final Identifier ZIP_CHECKED_STATE = SpiritVectorMod.id("compat.zipline_checked");
		private static final Identifier SAME_CABLE_COOLDOWN = SpiritVectorMod.id("compat.zipline_same_cable");
		private static final Identifier ZIP_GRIND_STATE = SpiritVectorMod.id("compat.zip_grind");
		private static final Identifier ZIP_JUMP_COYOTE = SpiritVectorMod.id("compat.zip_jump_coyote");

		public ZiplineGrindMovement(Identifier id) {
			super(id);
		}

		@Override
		public void configure(SpiritVector sv) {
			sv.stateManager().register(ZIP_CHECKED_STATE, new ManagedState(sv));
			sv.stateManager().register(SAME_CABLE_COOLDOWN, new ManagedState(sv));
			sv.stateManager().register(ZIP_GRIND_STATE, new ZiplineGrindState(sv));
			sv.stateManager().register(ZIP_JUMP_COYOTE, new ManagedState(sv));
		}

		@Override
		public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
			ZiplineGrindState grindState = (ZiplineGrindState) sv.stateManager().getState(ZIP_GRIND_STATE);

			// evil: coyote time application in condition testing
			if (sv.stateManager().isActive(ZIP_JUMP_COYOTE) && sv.inputManager().consume(Input.JUMP)) {
				zipJump(sv, grindState, ctx);
			}

			if (sv.inputManager().released(Input.CROUCH)) {
				sv.stateManager().clearTicks(SAME_CABLE_COOLDOWN);
			}

			if (sv.stateManager().isActive(ZIP_CHECKED_STATE)) {
				return false;
			}

			if (!sv.user.isOnGround() && sv.inputManager().rawInput(Input.CROUCH)) {
				Cable cable = Cables.getClosestCable(sv.user.getPos(), 2);
				if (cable != null && cable.isValid()) {

					// hack to guess if this is the last cable we were on
					if (sv.stateManager().isActive(SAME_CABLE_COOLDOWN) && cable.getPoint(grindState.progress).equals(grindState.cable.getPoint(grindState.progress))) {
						return false;
					}

					Vec3d point = cable.getClosestPoint(sv.user.getPos());
					double progress = cable.getProgress(point);
					if (validPosition(sv.user, point) && progress > 0.02 && progress < 0.98) {
						grindState.setup(
							cable,
							MovementUtils.augmentedInput(sv, ctx),
							point,
							sv.user.getVelocity().withAxis(Direction.Axis.Y, 0).length()
						);
						sv.user.setPosition(point);
						grindState.enable();
						sv.inputManager().consume(Input.CROUCH);
						sv.user.playSound(ZiplineSoundEvents.ZIPLINE_ATTACH, 0.4f, 2);
						return true;
					}
				}
			}
			return false;
		}

		@Override
		public boolean testMovementCompleted(SpiritVector sv, TravelMovementContext ctx) {
			ZiplineGrindState grindState = (ZiplineGrindState) sv.stateManager().getState(ZIP_GRIND_STATE);
			if (sv.inputManager().consume(Input.JUMP)) {
				zipJump(sv, grindState, ctx);
				return true;
			}

			if (!sv.inputManager().rawInput(Input.CROUCH)) {
				sv.stateManager().enableStateFor(ZIP_CHECKED_STATE, 5);
				sv.stateManager().enableStateFor(ZIP_JUMP_COYOTE, 5);
				return true;
			}

			if (!sv.stateManager().isActive(ZIP_GRIND_STATE)) {
				sv.stateManager().enableStateFor(ZIP_CHECKED_STATE, 10);
				return true;
			}
			return false;
		}

		@Override
		public void exit(SpiritVector sv) {
			if (sv.stateManager().isActive(ZIP_GRIND_STATE)) {
				sv.stateManager().disableState(ZIP_GRIND_STATE);
			}

			int COOLDOWN_TICKS = 15;
			sv.stateManager().enableStateFor(SAME_CABLE_COOLDOWN, COOLDOWN_TICKS);
		}

		@Override
		public void travel(SpiritVector sv, TravelMovementContext ctx) {
			ctx.ci().cancel();

			sv.stateManager().getState(ParticleTrailEffectState.ID).enableFor(2);

			ZiplineGrindState grindState = (ZiplineGrindState) sv.stateManager().getState(ZIP_GRIND_STATE);
			double MAX_TURN_ANGLE = 0.707;

			double MIN_SPEED = 0.8;
			if (grindState.speed < MIN_SPEED) {
				grindState.speed = MathHelper.lerp(0.03, grindState.speed, MIN_SPEED);
			}

			grindState.progress += grindState.direction * grindState.speed / grindState.cable.length();
			Vec3d nextPoint = grindState.cable.getPoint(grindState.progress);
			Vec3d cableDelta = nextPoint.subtract(sv.user.getPos());
			Vec3d cableDir = cableDelta.normalize();

			if (!validPosition(sv.user, cableDelta)) {
				grindState.disable();
				return;
			}

			sv.user.setPosition(nextPoint);
			sv.user.setVelocity(grindState.cable.direction(grindState.progress).multiply(grindState.speed * grindState.direction));
			sv.user.playSound(ZiplineSoundEvents.ZIPLINE_USE, 1, .7f + (float) grindState.speed);

			if (grindState.progress >= 1 || grindState.progress <= 0) {
				Collection<Cable> nextCandidates = grindState.cable.getNext(grindState.direction == 1);
				Vec3d input = MovementUtils.augmentedInput(sv, ctx);
				double dp = -1;
				Cable nextCable = null;
				for (Cable candidate : nextCandidates) {
					if (grindState.cable.equals(candidate)) continue;

					double lookDP = candidate.direction(0).dotProduct(input);
					double cableDP = candidate.direction(0).dotProduct(cableDir);

					if (lookDP > dp && cableDP > MAX_TURN_ANGLE) {
						dp = lookDP;
						nextCable = candidate;
					}
				}

				if (nextCable == null || !nextCable.isValid()) {
					grindState.disable();
					return;
				}

				grindState.cable = nextCable;
				grindState.direction = 1;
				grindState.progress = 0;
			}
		}

		@Override
		public void updateValues(SpiritVector sv) {
			if (sv.user.age % 5 == 0) {
				sv.modifyMomentum(2);
			}
		}

		@Override
		public boolean disableDrag(SpiritVector sv) {
			return true;
		}

		@Override
		public boolean fluidMovementAllowed(SpiritVector sv) {
			return true;
		}

		private static boolean validPosition(LivingEntity user, Vec3d pos) {
			Box box = user.getBoundingBox().offset(pos);
			for (var other : user.getWorld().getBlockCollisions(user, box)) {
				if (!other.isEmpty()) return false;
			}
			return true;
		}

		private static void zipJump(SpiritVector sv, ZiplineGrindState grindState, TravelMovementContext ctx) {
			double jump = MathHelper.clamp(grindState.speed, 0.6, 1);
			sv.user.setVelocity(
				MovementUtils.augmentedInput(sv, ctx).multiply(grindState.speed).add(0, jump, 0)
			);
			sv.effectsManager().spawnRing(sv.user.getPos(), Vec3d.ZERO);
			sv.stateManager().enableStateFor(ZIP_CHECKED_STATE, 5);
		}
	}

	private static class ZiplineGrindState extends ManagedState {
		public Cable cable = null;
		public double progress = 0;
		public Vec3d point = Vec3d.ZERO;
		public double direction = 1;
		public double speed;

		public void setup(Cable cable, Vec3d input, Vec3d point, double speed) {
			this.cable = cable;
			this.progress = cable.getProgress(point);
			this.point = point;
			this.direction = input.multiply(1, 0, 1).normalize().dotProduct(this.cable.direction(progress)) >= 0 ? 1 : -1;
			this.speed = speed;
		}

		public ZiplineGrindState(SpiritVector sv) {
			super(sv);
		}
	}
}
