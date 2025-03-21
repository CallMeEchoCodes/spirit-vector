package symbolics.division.spirit_vector.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import symbolics.division.spirit_vector.sfx.sound.EngineSoundInstance;
import symbolics.division.spirit_vector.sfx.sound.SlidingSoundInstance;
import symbolics.division.spirit_vector.sfx.sound.SpellDimensionSoundInstance;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class AbstractClientPlayerEntityMixin extends PlayerEntity {
    public AbstractClientPlayerEntityMixin(World world, BlockPos pos, float yaw, GameProfile gameProfile) {
        super(world, pos, yaw, gameProfile);
    }

    private EngineSoundInstance engineSound;
    private SlidingSoundInstance slidingSound;
	private SpellDimensionSoundInstance spellSound;
    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if (slidingSound == null) {
            if (SlidingSoundInstance.shouldPlayFor((AbstractClientPlayerEntity) (PlayerEntity) this)) {
                slidingSound = new SlidingSoundInstance(this);
                MinecraftClient.getInstance().getSoundManager().play(slidingSound);
            }
        } else if (slidingSound.isDone()) {
            slidingSound = null;
        }

        if (engineSound == null) {
            if (EngineSoundInstance.shouldPlayFor((AbstractClientPlayerEntity) (PlayerEntity) this)) {
                engineSound = new EngineSoundInstance(this);
                MinecraftClient.getInstance().getSoundManager().play(engineSound);
            }
        } else if (engineSound.isDone()) {
            engineSound = null;
        }

		if (spellSound == null) {
			if (SpellDimensionSoundInstance.shouldPlayFor((AbstractClientPlayerEntity) (PlayerEntity) this)) {
				spellSound = new SpellDimensionSoundInstance(this);
				MinecraftClient.getInstance().getSoundManager().play(spellSound);
			}
		} else if (spellSound.isDone() || spellSound.getVolume() == 0.0) {
			spellSound = null;
		}
    }
}
