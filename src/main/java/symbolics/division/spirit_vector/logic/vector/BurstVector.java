package symbolics.division.spirit_vector.logic.vector;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

public class BurstVector extends SpiritVector {
	public static final int BURST_MOMENTUM_MULTIPLIER = 3;

	public BurstVector(LivingEntity user, ItemStack itemStack) {
		super(user, itemStack, VectorType.BURST);
	}

	@Override
	public void travel(Vec3d movementInput, CallbackInfo ci) {
		if (groundTicks > 20) setMomentum(0); // one second grace period
		super.travel(movementInput, ci);
	}

	@Override
	public void modifyMomentum(int v) {
		super.modifyMomentum(v > 0 ? v * BURST_MOMENTUM_MULTIPLIER : v);
	}

	@Override
	public float getMovementSpeed(float slip) {
		return user.getMovementSpeed() * (0.21600002F / (slip * slip * slip));
	}
}
