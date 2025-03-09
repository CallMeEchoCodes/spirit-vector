package symbolics.division.spirit_vector.logic.vector;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;

public class BurstVector extends SpiritVector {
	public static final int BURST_MOMENTUM_MULTIPLIER = 3;

	public BurstVector(LivingEntity user, ItemStack itemStack) {
		super(user, itemStack, VectorType.BURST);
	}

	@Override
	public void modifyMomentum(int v) {
		super.modifyMomentum(v > 0 ? v * BURST_MOMENTUM_MULTIPLIER : v);
	}
}
