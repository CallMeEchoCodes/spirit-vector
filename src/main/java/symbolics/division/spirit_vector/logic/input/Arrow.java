package symbolics.division.spirit_vector.logic.input;

import net.minecraft.util.math.MathHelper;

public enum Arrow {
	DOWN("key.down", "↓"),
	RIGHT("key.right", "→"),
	UP("key.up", "↑"),
	LEFT("key.left", "←");

	public final String key;
	public final String sym;
	Arrow(String key, String sym) {
		this.key = key; this.sym = sym;
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
