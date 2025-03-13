package symbolics.division.spirit_vector.logic.input;

import net.minecraft.util.math.MathHelper;

public enum Arrow {
	DOWN("key.down", "↓", "down", 1),
	RIGHT("key.right", "→", "right", 3),
	UP("key.up", "↑", "up", 2),
	LEFT("key.left", "←", "left", 0);

	public final String key;
	public final String sym;
	public final String id;
	public final int ddrIndex;
	Arrow(String key, String sym, String id, int ddrIndex) {
		this.key = key; this.sym = sym; this.id = id; this.ddrIndex = ddrIndex;
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
