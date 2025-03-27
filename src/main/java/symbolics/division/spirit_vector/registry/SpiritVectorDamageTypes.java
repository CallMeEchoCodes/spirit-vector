package symbolics.division.spirit_vector.registry;

import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.damage.DamageType;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.world.World;
import symbolics.division.spirit_vector.SpiritVectorMod;

public class SpiritVectorDamageTypes {
	public static final RegistryKey<DamageType> FOOTSTOOL = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, SpiritVectorMod.id("footstool"));

	public static DamageSource of(World world, RegistryKey<DamageType> key) {
		return new DamageSource(world.getRegistryManager().get(RegistryKeys.DAMAGE_TYPE).entryOf(key));
	}

	public static void init() {}
}
