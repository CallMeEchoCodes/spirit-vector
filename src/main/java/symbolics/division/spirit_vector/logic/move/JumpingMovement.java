package symbolics.division.spirit_vector.logic.move;

import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.ability.WaterRunAbility;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.vector.VectorType;
import symbolics.division.spirit_vector.mixin.LivingEntityAccessor;

public class JumpingMovement extends NeutralMovement {

    public JumpingMovement(Identifier id) { super(id); }

    @Override
    public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
        if ((sv.user.groundCollision || WaterRunAbility.isWaterRunning(sv))
                && sv.inputManager().consume(Input.JUMP)) {
            // reset other buttons for abilities
            sv.inputManager().consume(Input.CROUCH);
            sv.inputManager().consume(Input.SPRINT);
            return true;
        }
        return false;
    }

    @Override
    public void travel(SpiritVector sv, TravelMovementContext ctx) {
        float f = ((LivingEntityAccessor) sv.user).callGetJumpVelocity() * 1.2f;
        if (f <= 0.00001) return;
        sv.user.addVelocity(0, f, 0);

		if (sv.inputManager().rawInput(Input.SPRINT)) {
			Vec3d kickoff  = ctx.inputDir().multiply(0.3f);
			float bounce = sv.getType().equals(VectorType.BURST) ? 0.3f : 0;
			sv.user.addVelocity(kickoff.x, bounce, kickoff.z);
			sv.effectsManager().kickoff(sv.user.getPos());
		}

        sv.user.velocityDirty = true;
        super.travel(sv, ctx);
    }

    @Override
    public void updateValues(SpiritVector sv) {}
}
