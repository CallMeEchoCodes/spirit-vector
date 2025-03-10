package symbolics.division.spirit_vector.logic.spell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SpellDimension {
	// SpellDimension instance lasts for the length of a spell
	// since Materia only exists clientside, we don't need to worry
	// about persistence between sessions.

	public static final SpellDimension SPELL_DIMENSION = new SpellDimension();

	private static Consumer<Spell> spellCallback = s -> {};

	public static void setSpellCallback(Consumer<Spell> spellConsumer) {
		spellCallback = spellConsumer;
	}

	public static void cast(Spell spell) {
		SPELL_DIMENSION.activeSpells.add(spell);
		spellCallback.accept(spell);
	}

	public static void worldTick() {
		SPELL_DIMENSION.tick();
	}

	private final List<Spell> activeSpells = new ArrayList<>();

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

	public List<Spell> activeSpells() {
		return List.copyOf(activeSpells);
	}
}
