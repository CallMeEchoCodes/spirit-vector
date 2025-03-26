package symbolics.division.spirit_vector.logic.ability;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class LedgeLockAbility extends AbstractSpiritVectorAbility {
	protected static final Identifier LEDGE_LOCK_FLAG = SpiritVectorMod.id("ledge_lock_flag");
	protected static final Identifier LEDGE_LOCK_ID = SpiritVectorMod.id("ledge_lock");
	public static final float CLIP_THRESHOLD = MathHelper.cos(MathHelper.PI / 6);

	public static boolean hasLedgeLock(ItemStack stack, LivingEntity entity) {
		var abilities = stack.getComponents().get(SpiritVectorHeldAbilities.COMPONENT);
		if (abilities == null) return false;
		for (SpiritVectorAbility ability : abilities.getAll()) {
			if (ability instanceof LedgeLockAbility) return true;
		}
		return false;
	}

	public LedgeLockAbility(Identifier id) {
		super(id, Integer.MAX_VALUE);
	}

	@Override
	public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
		return false;
	}

	@Override
	public void travel(SpiritVector sv, TravelMovementContext ctx) {
	}
}
