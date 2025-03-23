package symbolics.division.spirit_vector.mixin;

import net.minecraft.registry.DynamicRegistryManager;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;
import symbolics.division.spirit_vector.logic.spell.SpellDimensionHaver;

import java.util.function.Supplier;

@Mixin(World.class)
public class WorldSpellDimensionInjection implements SpellDimensionHaver {
	@Unique
	private SpellDimension spellDimension;

	@Inject(method = "<init>", at = @At("TAIL"))
	public void injectSpellDimension(MutableWorldProperties properties, RegistryKey registryRef, DynamicRegistryManager registryManager, RegistryEntry dimensionEntry, Supplier profiler, boolean isClient, boolean debugWorld, long biomeAccess, int maxChainedNeighborUpdates, CallbackInfo ci) {
		spellDimension = new SpellDimension((World) (Object) this);
	}

	@Override
	public SpellDimension spellDimension() {
		return spellDimension;
	}
}
