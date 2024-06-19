package symbolics.division.spirit.vector.logic;

/*
    Abstract controller for spirit vector state
    Every entity with an SV equipped will have one of these
    associated with it.
 */

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import symbolics.division.spirit.vector.logic.state.IManagedState;
import symbolics.division.spirit.vector.logic.state.ParticleTrailEffectState;
import symbolics.division.spirit.vector.logic.state.StateManager;
import symbolics.division.spirit.vector.logic.state.WingsEffectState;
import symbolics.division.spirit.vector.sfx.EffectsManager;
import symbolics.division.spirit.vector.sfx.SFXPack;
import symbolics.division.spirit.vector.sfx.SFXRegistry;
import symbolics.division.spirit.vector.sfx.SpiritVectorSFX;

public class SpiritVector {

    public static final int MAX_FUEL = 100;
    public static final int MAX_MOMENTUM = 100;
    public static final int MOMENTUM_FAST_THRESHOLD = MAX_MOMENTUM / 2;
    public static final float MINIMUM_SPEED_FOR_TRAIL = 0.2f;

    private int fuel = 0;
    private int momentum = 0;
    private MovementType moveState = MovementType.NEUTRAL;
    private final EffectsManager effectsManager;
    private final StateManager stateManager = new StateManager();
    private final RuneManager runeManager = new RuneManager();
    private final MovementType[] movements = {
            MovementType.SLIDE, MovementType.WALL_JUMP
    };
    private final SFXPack<?> sfx;

    public final LivingEntity user;

    public SpiritVector(LivingEntity user, SFXPack<?> sfx) {
        this.effectsManager = new EffectsManager(this);
        this.sfx = sfx;
        this.user = user;

        stateManager.register(ParticleTrailEffectState.ID, new ParticleTrailEffectState(this));
        stateManager.register(WingsEffectState.ID, new WingsEffectState(this));
    }

    public SpiritVector(LivingEntity user) {
        this(user, SpiritVectorSFX.getDefault());
    }

    public void travel(Vec3d movementInput, CallbackInfo ci, boolean jumping) {
        stateManager.tick();
        var inputDirection = SVMathHelper.movementInputToVelocity(movementInput, 1, user.getYaw());
        var ctx = new MovementContext(movementInput, jumping, ci, inputDirection);
        updateMovementType(ctx);
        moveState.travel(this, ctx);
        moveState.updateValues(this);

        var vel = user.getVelocity();
        if (isSoaring()) {
            getStateManager().enableStateFor(WingsEffectState.ID, 1);
            if (vel.length() > MINIMUM_SPEED_FOR_TRAIL) {
                getStateManager().enableStateFor(ParticleTrailEffectState.ID, 1);
            }
        }
    }

    private void updateMovementType(MovementContext ctx) {
        if (moveState.testMovementCompleted(this, ctx)) {
            moveState = MovementType.NEUTRAL;
            for (MovementType m : movements) {
                if (m.testMovementCondition(this, ctx)) {
                    moveState = m;
                    break;
                }
            }
        }
    }

    public boolean preventSlowdown() {
        return moveState.preventSlowdown(this);
    }

    public float getMovementSpeed(float slip) {
        return moveState.getMovementSpeed(this, slip);// * (1f + 1f * momentum/MAX_MOMENTUM);
    }

    public int getMomentum() {
        return momentum;
    }

    public int getFuel() {
        return fuel;
    }

    public void modifyMomentum(int v) {
        momentum = Math.clamp(momentum + v, 0, MAX_MOMENTUM);
    }

    public void modifyFuel(int v) {
        fuel = Math.clamp(fuel + v, 0, MAX_FUEL);
    }

    public boolean clipAtLedge() {
        return moveState == MovementType.SLIDE;
    }

    public float stepHeight() {
        return moveState == MovementType.SLIDE ? 1 : -1;
    }

    public SFXPack<?> getSFX() {
        return sfx;
    }

    public EffectsManager getEffectsManager() { return effectsManager; }

    public boolean isSoaring() {
        // it means you're really cool
        return getMomentum() >= MOMENTUM_FAST_THRESHOLD;
    }

    public StateManager getStateManager() { return stateManager; }

}