package symbolics.division.spirit_vector.logic.spell;

import symbolics.division.spirit_vector.logic.input.Arrow;

import java.util.ArrayList;
import java.util.List;

public class SpellDimension {
	// SpellDimension instance lasts for the length of a spell
	// since Materia only exists clientside, we don't need to worry
	// about persistence between sessions.

	public static final SpellDimension SPELL_DIMENSION = new SpellDimension();

	public static void cast(Spell spell) {
		SPELL_DIMENSION.activeSpells.add(spell);
	}

	public static void worldTick() {
		SPELL_DIMENSION.tick();
	}

	private List<Spell> activeSpells = new ArrayList<>();

	public void tick() {
		List<Spell> toTick = List.copyOf(activeSpells);
		for (Spell spell : toTick) {
			if (spell.ticksLeft() <= 0) {
				activeSpells.remove(spell);
			} else {
				spell.tick();
			}
		}
	}

	public boolean isCasting() {
		return !activeSpells.isEmpty();
	}
}
