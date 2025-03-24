package symbolics.division.spirit_vector.api;

import com.google.common.collect.ImmutableMap;
import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import symbolics.division.spirit_vector.logic.move.MovementType;

import java.util.List;
import java.util.Map;

public final class SpiritVectorApi {
	public enum MovementOrder {
		AFTER, BEFORE
	}

	private static final Map<Pair<MovementType, MovementOrder>, List<MovementType>> modifications = new Object2ObjectLinkedOpenHashMap<>();

	public static void registerMovement(MovementType newMovement, MovementType base, MovementOrder order) {
		var key = new Pair<>(base, order);
		modifications.computeIfAbsent(key, k -> new ObjectArrayList<>()).add(newMovement);
	}

	public static List<MovementType> getRegisteredMovements(MovementType base, MovementOrder order) {
		return List.copyOf(modifications.getOrDefault(new Pair<>(base, order), List.of()));
	}

	public static Map<Pair<MovementType, MovementOrder>, List<MovementType>> getRegisteredMovements() {
		return ImmutableMap.copyOf(modifications);
	}
}
