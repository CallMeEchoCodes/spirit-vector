package symbolics.division.spirit_vector.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderTickCounter;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.math.Vec3d;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import symbolics.division.spirit_vector.SpiritVectorSounds;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;
import symbolics.division.spirit_vector.sfx.ClientSFX;

@Mixin(WorldRenderer.class)
public class WorldRendererMixin {
	private boolean shouldCancel() {
		return SpellDimension.SPELL_DIMENSION.isCasting();
	}

    @WrapOperation(
            method = "playJukeboxSong",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/sound/PositionedSoundInstance;record(Lnet/minecraft/sound/SoundEvent;Lnet/minecraft/util/math/Vec3d;)Lnet/minecraft/client/sound/PositionedSoundInstance;"
            )
    )
    private PositionedSoundInstance injectCassetteEvent(SoundEvent sound, Vec3d pos, Operation<PositionedSoundInstance> op) {
        if (SpiritVectorSounds.doesSoundLoop(sound)) {
            return ClientSFX.cassette(sound, pos);
        }
        return op.call(sound, pos);
    }

	@WrapOperation(
		method = "render",
		at = @At(
			value = "INVOKE",
			target = "Lnet/minecraft/client/render/BackgroundRenderer;applyFogColor()V")
	) public void cancelBackground(Operation<Void> original) {
		if (!shouldCancel()) original.call();
	}

	@Inject(
		method = "renderSky",
		at = @At("HEAD"),
		cancellable = true
	) public void cancelSky(Matrix4f matrix4f, Matrix4f projectionMatrix, float tickDelta, Camera camera, boolean thickFog, Runnable fogCallback, CallbackInfo ci) {
		if (shouldCancel()) ci.cancel();
	}
}


