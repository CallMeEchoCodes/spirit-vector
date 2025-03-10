package symbolics.division.spirit_vector.logic.input;

public enum Arrow {
	LEFT("key.left", "←"),
	RIGHT("key.right", "→"),
	DOWN("key.down", "↓"),
	UP("key.up", "↑");

	public final String key;
	public final String sym;
	Arrow(String key, String sym) {
		this.key = key; this.sym = sym;
	}
}
