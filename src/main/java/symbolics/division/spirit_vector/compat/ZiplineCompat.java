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
		private static final Identifier ZIP_GRIND_STATE = SpiritVectorMod.id("compat.zip_grind");

		public ZiplineGrindMovement(Identifier id) {
			super(id);
		}

		@Override
		public void configure(SpiritVector sv) {
			sv.stateManager().register(ZIP_CHECKED_STATE, new ManagedState(sv));
			sv.stateManager().register(ZIP_GRIND_STATE, new ZiplineGrindState(sv));
		}

		@Override
		public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
			boolean checked = sv.stateManager().isActive(ZIP_CHECKED_STATE);
			if (!sv.user.isOnGround()) { // if if if if
				boolean pressed = sv.inputManager().pressed(Input.CROUCH);
				if (pressed && !checked) { // try to do zipline
					sv.stateManager().enableState(ZIP_CHECKED_STATE);
					Cable cable = Cables.getClosestCable(sv.user.getPos(), 3);
					if (cable != null) {
						Vec3d point = cable.getClosestPoint(sv.user.getPos());
						if (validPosition(sv.user, point)) {
							ZiplineGrindState grindState = (ZiplineGrindState) sv.stateManager().getState(ZIP_GRIND_STATE);
							grindState.setup(
								cable,
								MovementUtils.augmentedInput(sv, ctx),
								point,
								sv.user.getVelocity().withAxis(Direction.Axis.Y, 0).length()
							);
							grindState.enable();
							sv.inputManager().consume(Input.CROUCH);
							sv.user.playSound(ZiplineSoundEvents.ZIPLINE_ATTACH, 0.4f, 2);
							return true;
						}
					}
				} else if (!pressed && checked) { // wait for next press
					sv.stateManager().disableState(ZIP_CHECKED_STATE);
				}
			} else if (checked) {
				sv.stateManager().disableState(ZIP_CHECKED_STATE);
			}
			return false;
		}

		@Override
		public boolean testMovementCompleted(SpiritVector sv, TravelMovementContext ctx) {
			ZiplineGrindState grindState = (ZiplineGrindState) sv.stateManager().getState(ZIP_GRIND_STATE);
			if (sv.inputManager().consume(Input.JUMP)) {
				double jump = MathHelper.clamp(grindState.speed, 0.6, 1);
				sv.user.setVelocity(
					MovementUtils.augmentedInput(sv, ctx).multiply(grindState.speed).add(0, jump, 0)
				);
				sv.effectsManager().spawnRing(sv.user.getPos(), Vec3d.ZERO);
				return true;
			}
			return !sv.stateManager().isActive(ZIP_GRIND_STATE) || sv.inputManager().consume(Input.CROUCH);
		}

		@Override
		public void exit(SpiritVector sv) {
			if (sv.stateManager().isActive(ZIP_GRIND_STATE)) {
				sv.stateManager().disableState(ZIP_GRIND_STATE);
			}
		}

		@Override
		public void travel(SpiritVector sv, TravelMovementContext ctx) {
			ctx.ci().cancel();

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

				if (nextCable == null) {
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
