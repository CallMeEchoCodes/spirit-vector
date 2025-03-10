package symbolics.division.spirit_vector.mixin.client;

import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private static void spellDimensionSkyOverride(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
		if (SpellDimension.SPELL_DIMENSION.isCasting()) {
			ci.cancel();
		}
	}
}
