package symbolics.division.spirit_vector;

import net.minecraft.registry.*;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public class SpiritVectorSounds {

    public static final SoundEvent BURST = register("sfx.burst");
    public static final SoundEvent STEP = register("sfx.step");
    public static final SoundEvent SLIDE = register("sfx.slide");
    public static final SoundEvent SLIDE_START = register("sfx.slide_start");
    public static final SoundEvent ENGINE = register("sfx.engine");

	public static final SoundEvent RUNE_MATRIX_CLICK = register("sfx.rune_matrix.click");
	public static final SoundEvent RUNE_MATRIX_AMBIANCE = register("sfx.rune_matrix.ambiance");
	public static final SoundEvent RUNE_MATRIX_BUZZ = register("sfx.rune_matrix.buzz");
	public static final SoundEvent RUNE_MATRIX_CAST = register("sfx.rune_matrix.cast");
	public static final SoundEvent RUNE_MATRIX_START = register("sfx.rune_matrix.start");

    public static final SoundEvent TAKE_BREAK_LOOP = register("cassette.take_break");
    public static final SoundEvent TAKE_BREAK_SONG = register("music.take_break");
    public static final SoundEvent SHOW_DONE_LOOP = register("cassette.show_done");
    public static final SoundEvent SHOW_DONE_SONG = register("music.show_done");
	public static final SoundEvent SONIC_BEE_SONG = register("music.sonic_bee");
	public static final SoundEvent IM_DOWN_SONG = register("music.im_down");
	public static final SoundEvent EMULATOR_SONG = register("music.emulator");
    public static final SoundEvent PERCEPTION_REBOUND_SONG = register("music.perception_rebound");
    public static final SoundEvent PERCEPTION_REBOUND_LOOP = register("music.perception_rebound.loop");
    public static final SoundEvent FRATRICIDE_SONG = register("music.fratricide");
    public static final SoundEvent FRATRICIDE_LOOP = register("music.fratricide.loop");

    private static SoundEvent register(String id) {
        return registerWithId(SpiritVectorMod.id(id));
    }

    private static SoundEvent registerWithId(Identifier id) {
        return Registry.register(Registries.SOUND_EVENT, id, SoundEvent.of(id));
    }

    public static void init(){}

    public static boolean doesSoundLoop(SoundEvent event) {
        return Registries.SOUND_EVENT.getEntry(event.getId())
                .map(entry -> entry.isIn(SpiritVectorTags.Misc.JUKEBOX_LOOPING))
                .orElse(false);
    }

//    public static boolean readyToLoop(RegistryEntry<SoundEvent> event, long ticksPlaying) {
//
//    }
}
