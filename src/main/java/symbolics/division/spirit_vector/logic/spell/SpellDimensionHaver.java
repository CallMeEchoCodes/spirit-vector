package symbolics.division.spirit_vector.logic.spell;

import org.apache.commons.lang3.NotImplementedException;

public interface SpellDimensionHaver {
	default SpellDimension spellDimension() {
		throw new NotImplementedException();
	}
}
