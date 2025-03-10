package symbolics.division.spirit_vector.logic.input;

import net.minecraft.util.math.MathHelper;

public enum Arrow {
	DOWN("key.down", "↓", "down"),
	RIGHT("key.right", "→", "right"),
	UP("key.up", "↑", "up"),
	LEFT("key.left", "←", "left");

	public final String key;
	public final String sym;
	public final String id;
	Arrow(String key, String sym, String id) {
		this.key = key; this.sym = sym; this.id = id;
	}

	public Arrow leftNeighbor() {
		return switch (this) {
			case RIGHT -> UP;
			case UP -> LEFT;
			case LEFT -> DOWN;
			case DOWN -> RIGHT;
		};
	}

	public Arrow rightNeighbor() {
		return switch (this) {
			case RIGHT -> DOWN;
			case DOWN -> LEFT;
			case LEFT -> UP;
			case UP -> RIGHT;
		};
	}
}
