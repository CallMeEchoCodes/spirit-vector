package symbolics.division.spirit_vector.sfx.sound;

import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import symbolics.division.spirit_vector.SpiritVectorSounds;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;

public class SpellDimensionSoundInstance extends MovingSoundInstance {
	public static boolean shouldPlayFor(PlayerEntity player) {
		return SpellDimension.SPELL_DIMENSION.isCasting();
	}

	private final PlayerEntity player;
//	private static final int FADE_TICKS = 20;
//	private static final float MAX_VOLUME = 0.8f;

	public SpellDimensionSoundInstance(PlayerEntity player) {
		super(SpiritVectorSounds.RUNE_MATRIX_AMBIANCE, SoundCategory.AMBIENT, player.getRandom());
		this.player = player;
		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.1f;
		this.pitch = 1.0f + (player.getRandom().nextFloat() * 0.2f) - 0.1f;
	}

	@Override
	public void tick() {
		int FADE_TICKS = 20;
		float MAX_VOLUME = 0.6f;
		if (!this.player.isRemoved() && shouldPlayFor(this.player)) {
			this.volume = Math.min(MAX_VOLUME, volume + (MAX_VOLUME / FADE_TICKS));
			this.x = this.player.getX();
			this.y = this.player.getY();
			this.z = this.player.getZ();
		} else {
			this.volume = Math.max(0, volume - (MAX_VOLUME / FADE_TICKS));
			if (this.volume == 0) {
				this.setDone();
			}
		}
	}
}
