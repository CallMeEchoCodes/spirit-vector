package symbolics.division.spirit_vector.logic.spell;

public class SpellFXEvents {
	public static SpellFXEvents INSTANCE = new SpellFXEvents() {};
	public static void arrowInput() { INSTANCE.arrowInputCallback(); }
	public void arrowInputCallback() {}
	public static void openSpellDimension() { INSTANCE.openSpellDimensionCallback(); }
	public void openSpellDimensionCallback() {}
}
