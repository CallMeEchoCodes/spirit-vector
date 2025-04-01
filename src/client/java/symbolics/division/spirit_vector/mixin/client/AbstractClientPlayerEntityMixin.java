package symbolics.division.spirit_vector.mixin.client;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
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

	@Unique
	private EngineSoundInstance dbsv$engineSound;
	@Unique
	private SlidingSoundInstance dbsv$slidingSound;
	@Unique
	private SpellDimensionSoundInstance dbsv$spellSound;

	@Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
		if (dbsv$slidingSound == null) {
            if (SlidingSoundInstance.shouldPlayFor((AbstractClientPlayerEntity) (PlayerEntity) this)) {
				dbsv$slidingSound = new SlidingSoundInstance(this);
				MinecraftClient.getInstance().getSoundManager().play(dbsv$slidingSound);
            }
		} else if (dbsv$slidingSound.isDone()) {
			dbsv$slidingSound = null;
        }

		if (dbsv$engineSound == null) {
            if (EngineSoundInstance.shouldPlayFor((AbstractClientPlayerEntity) (PlayerEntity) this)) {
				dbsv$engineSound = new EngineSoundInstance(this);
				MinecraftClient.getInstance().getSoundManager().play(dbsv$engineSound);
            }
		} else if (dbsv$engineSound.isDone()) {
			dbsv$engineSound = null;
        }

		if (dbsv$spellSound == null) {
			if (SpellDimensionSoundInstance.shouldPlayFor((AbstractClientPlayerEntity) (PlayerEntity) this)) {
				dbsv$spellSound = new SpellDimensionSoundInstance(this);
				MinecraftClient.getInstance().getSoundManager().play(dbsv$spellSound);
			}
		} else if (dbsv$spellSound.isDone() || dbsv$spellSound.getVolume() == 0.0) {
			dbsv$spellSound = null;
		}
    }
}
