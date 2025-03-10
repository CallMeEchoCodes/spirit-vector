package symbolics.division.spirit_vector.logic.spell;

import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.input.Arrow;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.List;

public class Spell {
	public static void cast(SpiritVector sv, List<Arrow> eigenCode) {
		StringBuilder code = new StringBuilder();
		for (Arrow a : eigenCode) code.append(a.sym);
		SpiritVectorMod.LOGGER.info("casting spell: " + code);
	}
}
