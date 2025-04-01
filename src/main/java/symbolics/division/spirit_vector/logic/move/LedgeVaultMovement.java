package symbolics.division.spirit_vector.logic.move;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.state.ManagedState;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.logic.vector.VectorType;
import symbolics.division.spirit_vector.networking.FootstoolPayloadC2S;

import java.util.List;

public class LedgeVaultMovement extends AbstractMovementType {
    private static final int MOMENTUM_GAINED = SpiritVector.MAX_MOMENTUM / 30;
    private static final int VAULT_WINDOW_TICKS = 5;
    private static final float VAULT_SPEED = 1.2f;
    private static final Identifier VAULT_STATE_ID = SpiritVectorMod.id("vault_window");
	private static final Identifier FOOTSTOOL_COOLDOWN = SpiritVectorMod.id("footstool_cooldown");

    public LedgeVaultMovement(Identifier id) {
        super(id);
    }

    public static void triggerLedge(SpiritVector sv) {
        if (!sv.user.isTouchingWater()) {
            sv.stateManager().enableStateFor(VAULT_STATE_ID, VAULT_WINDOW_TICKS);
        }
    }

    @Override
    public void configure(SpiritVector sv) {
        sv.stateManager().register(VAULT_STATE_ID, new ManagedState(sv));
		sv.stateManager().register(FOOTSTOOL_COOLDOWN, new ManagedState(sv));
    }

    @Override
    public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
		if (sv.stateManager().isActive(VAULT_STATE_ID) && sv.user.isOnGround() && sv.inputManager().consume(Input.JUMP))
			return true;
		if (!sv.user.isOnGround() && sv.inputManager().pressed(Input.JUMP) && !sv.stateManager().isActive(FOOTSTOOL_COOLDOWN)) {
			List<LivingEntity> collisions = sv.user.getWorld().getNonSpectatingEntities(LivingEntity.class, sv.user.getBoundingBox());
			if (collisions.size() > 1) {
				sv.inputManager().consume(Input.JUMP);
				sv.stateManager().enableStateFor(FOOTSTOOL_COOLDOWN, 20);
				float damage = (float) sv.user.getVelocity().lengthSquared();
				for (LivingEntity e : collisions) {
					if (e == sv.user) continue;
					FootstoolPayloadC2S.send(e, damage);
					break;
				}
				sv.modifyMomentum((int) damage);
				return true;
			}
		}
		return false;
    }

    @Override
    public void travel(SpiritVector sv, TravelMovementContext ctx) {
        sv.stateManager().clearTicks(VAULT_STATE_ID);
        Vec3d result;
		boolean vault = false;
        if (ctx.inputDir().lengthSquared() < 0.1) {          // no input, vault
            result = new Vec3d(0, 0.9, 0);
			vault = true;
        } else {                                             // input: ledgetrick
            // remove this check if its too confusing
            double y = Math.abs(sv.user.getRotationVector().y);
            result = ctx.inputDir().withAxis(Direction.Axis.Y, Math.max(0.3, y)).normalize();
        }

		if (!vault && sv.is(VectorType.BURST)) {
			// burst always has fixed velocity change, for precision
			sv.user.setVelocity(result.multiply(VAULT_SPEED *  sv.consumeSpeedMultiplier()));
		} else {
			sv.user.addVelocity(result.multiply(VAULT_SPEED *  sv.consumeSpeedMultiplier()));
		}

        sv.effectsManager().spawnRing(sv.user.getPos(), result);
        NEUTRAL.travel(sv, ctx);
    }

    @Override
    public void updateValues(SpiritVector sv) {
        if (!sv.is(VectorType.DREAM)) {
            sv.modifyMomentum(MOMENTUM_GAINED);
            sv.stateManager().enableStateFor(SpiritVector.MOMENTUM_DECAY_GRACE_STATE, 20);
        }
    }
}
