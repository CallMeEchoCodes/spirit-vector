package symbolics.division.spirit_vector.sfx;

import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.BlockSoundGroup;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import symbolics.division.spirit_vector.SpiritVectorSounds;

import java.util.function.Predicate;

public class AudioGirl {

	public static Predicate<PlayerEntity> playSound = p -> true;

	public static boolean step(PlayerEntity player, BlockState state) {
		if (!playSound.test(player)) return false;
        BlockSoundGroup blockSoundGroup = state.getSoundGroup();
        player.playSound(SpiritVectorSounds.STEP, blockSoundGroup.getVolume() * 0.1f, blockSoundGroup.getPitch() + (player.getRandom().nextFloat() * 0.066f - 0.033f));
		return true;
    }

    public static void burst(PlayerEntity player, BlockPos pos) {
        player.getWorld().playSound(
                null, pos, SpiritVectorSounds.BURST, SoundCategory.PLAYERS, 0.2f, player.getRandom().nextFloat() * 0.1f + 0.95f
        );
    }

	public static void brake(PlayerEntity player, BlockPos pos) {
		player.playSound(SoundEvents.ENTITY_BAT_TAKEOFF, 0.2f, player.getRandom().nextFloat() * 0.1f + 2f);
	}


}
