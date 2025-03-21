package symbolics.division.spirit_vector.sfx.sound;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.sound.MovingSoundInstance;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.sound.SoundCategory;
import symbolics.division.spirit_vector.SpiritVectorSounds;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;

public class SpellDimensionSoundInstance extends MovingSoundInstance {
	public static boolean shouldPlayFor(PlayerEntity player) {
		return SpellDimension.SPELL_DIMENSION.isCasting() && MinecraftClient.getInstance().player == player;
	}

	private final PlayerEntity player;
	private int ticksPlaying = 0;

	public SpellDimensionSoundInstance(PlayerEntity player) {
		super(SpiritVectorSounds.RUNE_MATRIX_AMBIANCE, SoundCategory.AMBIENT, player.getRandom());
		this.player = player;
		this.repeat = true;
		this.repeatDelay = 0;
		this.volume = 0.1f;
		this.pitch = 1.0f + (player.getRandom().nextFloat() * 0.2f) - 0.1f;
		this.ticksPlaying = 0;
	}

	@Override
	public void tick() {
		int FADE_TICKS = 20 * 3;
		float MAX_VOLUME = 0.6f;
		this.ticksPlaying++;
		if (!this.player.isRemoved() && shouldPlayFor(this.player)) { // && this.ticksPlaying < SOUND_LENGTH) {
			this.volume = Math.min(MAX_VOLUME, volume + (MAX_VOLUME / FADE_TICKS));
			this.x = this.player.getX();
			this.y = this.player.getY();
			this.z = this.player.getZ();
			this.volume = Math.max(0.01f, this.volume - 0.01f);
		} else {
			int fade = FADE_TICKS; // this.ticksPlaying >= SOUND_LENGTH ? FADE_TICKS / 3 : FADE_TICKS;
			this.volume = Math.max(0, volume - (MAX_VOLUME / fade));
			if (this.volume == 0 && !shouldPlayFor(this.player)) {
				this.setDone();
			}
		}
	}
}
