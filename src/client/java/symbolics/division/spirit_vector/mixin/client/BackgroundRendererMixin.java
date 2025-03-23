package symbolics.division.spirit_vector.mixin.client;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BackgroundRenderer;
import net.minecraft.client.render.Camera;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(BackgroundRenderer.class)
public class BackgroundRendererMixin {
	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	private static void spellDimensionSkyOverride(Camera camera, float tickDelta, ClientWorld world, int viewDistance, float skyDarkness, CallbackInfo ci) {
		if (world.spellDimension().isCasting()) {
			ci.cancel();
		}
	}

	@Inject(
		method = "applyFogColor",
		at = @At("HEAD"),
		cancellable = true
	)
	private static void overrideFogColor(CallbackInfo ci) {
		World world = MinecraftClient.getInstance().world;
		if (world != null && MinecraftClient.getInstance().world.spellDimension().isCasting()) {
			RenderSystem.setShaderFogColor(0, 0, 0);
			ci.cancel();
		}
	}
}
