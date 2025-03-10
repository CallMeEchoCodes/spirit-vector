package symbolics.division.spirit_vector.render;

import symbolics.division.spirit_vector.SpiritVectorSounds;
import symbolics.division.spirit_vector.logic.spell.SpellFXEvents;

public class SpellFX extends SpellFXEvents {
	@Override
	public void arrowInputCallback() {
		SpiritVectorHUD.playUISound(SpiritVectorSounds.RUNE_MATRIX_CLICK, 1);
	}

	@Override
	public void openSpellDimensionCallback() {
//		SpiritVectorHUD.playUISound(SpiritVectorSounds.RUNE_MATRIX_AMBIANCE, 1);
	}
}
