package symbolics.division.spirit_vector.logic.spell;

import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.ObjectArrayPriorityQueue;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.input.Arrow;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.logic.vector.VectorType;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.function.Consumer;

public class Spell {
	public static final int MAX_CODE_LENGTH = 16;
	public static final int EIDOS_SPACING_HORIZONTAL = 9;
	public static final int EIDOS_SPACING_VERTICAL = 7;
	public static final int MIN_SPELL_DIMENSION_RADIUS = 50;
	public static final int MAX_SPELL_DIMENSION_RADIUS = 100;
	public static final int MIN_SPELL_TICKS = 20 * 10;
	public static final int MAX_SPELL_TICKS = 20 * 40;

	public final Eidos core;
	private int maxSpellRadius = 0;
	private int spellRadius = 0;
	private final float decay;
	private final float complexity;
	private int ticksLeft = 0;
	private final SpiritVector sv;
	private final PriorityQueue<BlockPos> anchors;
	private final BlockPos center;
	private boolean cancelled = false;
	private Consumer<BlockPos> placementCallback = b -> {
	};

	public Spell(SpiritVector sv, List<Arrow> eigenCode) {
		this.sv = sv;
		StringBuilder code = new StringBuilder();
		for (Arrow a : eigenCode) code.append(a.sym);
		String unique = code.toString().replaceAll("(.)(?=.*?\\1)", "");

		this.complexity = (float) (Math.min(eigenCode.size(), 8) + unique.length()) / (float) (8 + 4);
		// base 0.9 is ideal for sky runes
		this.decay = 0.9f * (1f - complexity);
		this.core = makeCore(eigenCode);

		this.maxSpellRadius = (int) (MIN_SPELL_DIMENSION_RADIUS + (MAX_SPELL_DIMENSION_RADIUS - MIN_SPELL_DIMENSION_RADIUS) * complexity);
		this.ticksLeft = (int) (MIN_SPELL_TICKS + (MAX_SPELL_TICKS - MIN_SPELL_TICKS) * complexity);
		SpiritVectorMod.LOGGER.debug("casting spell: " + code);
		SpiritVectorMod.LOGGER.debug("radius: " + this.maxSpellRadius);
		SpiritVectorMod.LOGGER.debug("complexity: " + complexity);
		SpiritVectorMod.LOGGER.debug("decay: " + decay);

		this.center = sv.user.getBlockPos();

		if (sv.is(VectorType.DREAM)) {
			// sort spherically
			this.anchors = new ObjectArrayPriorityQueue<>((a1, a2) -> Double.compare(a1.getSquaredDistance(center), a2.getSquaredDistance(center)));
		} else {
			this.anchors = new ObjectArrayPriorityQueue<>((a1, a2) -> Integer.compare(a1.getManhattanDistance(center), a2.getManhattanDistance(center)));
		}

		for (int x = -(int) maxSpellRadius; x < maxSpellRadius; x++) {
			for (int y = -(int) maxSpellRadius; y < maxSpellRadius; y++) {
				for (int z = -(int) maxSpellRadius; z < maxSpellRadius; z++) {
					if (x % EIDOS_SPACING_HORIZONTAL == 0 && z % EIDOS_SPACING_HORIZONTAL == 0 && y % EIDOS_SPACING_VERTICAL == 0) {
						anchors.enqueue(new BlockPos(center.getX() + x, center.getY() + y, center.getZ() + z));
					}
				}
			}
		}
	}

	public void tick(SpellDimension spellDimension) {
		if (!sv.user.isAlive() ||
			sv.user.isRemoved() ||
			!SpiritVector.hasEquipped(sv.user)
		) {
			ticksLeft = 0;
			cancelled = true;
			return;
		}

		ticksLeft--;
		if (ticksLeft < 0 || anchors.isEmpty()) {
			cancelled = true;
			return;
		}

		spellRadius++;
		// creates an octahedron
		if (sv.is(VectorType.BURST) && spellRadius > maxSpellRadius) return;

		for (int i = 0; i < SpellDimension.EIDOS_PER_TICK; i++) {
			if (anchors.isEmpty()) break;
			BlockPos bp = anchors.dequeue();
			// creates a sphere
			if (sv.is(VectorType.DREAM) && !bp.isWithinDistance(this.center, maxSpellRadius)) break;
			Direction d = Direction.byId((bp.getX() + bp.getY() * 2 + bp.getZ() * 3) % 4 + 2);
			this.core.emplace(sv.user.getWorld(), bp, d, decay);

			// merge these
			this.placementCallback.accept(bp);
			spellDimension.eidosPlaced(sv.user.getWorld(), bp, MathHelper.ceil(this.core.size()) + 2, this.ticksLeft);
		}
	}

	public void setPlacementCallback(Consumer<BlockPos> bc) {
		this.placementCallback = bc;
	}

	public int ticksLeft() {
		return ticksLeft;
	}

	public boolean cancelled() {
		return cancelled;
	}

	private Eidos makeCore(List<Arrow> eigenCode) {
		Map<Arrow, EidosEdge> edges = new TreeMap<>();
		edges.put(Arrow.RIGHT, new EidosEdge(3, 0, 1, 0));
		edges.put(Arrow.UP, new EidosEdge(0, 0, 0, -1));
		edges.put(Arrow.LEFT, new EidosEdge(0, 0, -1, 0));
		edges.put(Arrow.DOWN, new EidosEdge(0, 3, 0, 1));
		Set<Vector3fc> core = new HashSet<>();

		// apply arrows (extrude)
		for (int i = 0; i < 4; i++) {
			for (int j = 0; j < 2; j++) {
				for (int k = 0; k < 4; k++) {
					core.add(new Vector3f(i, j, k));
				}
			}
		}

		for (int i = 0; i + 1 < eigenCode.size(); i += 2) {
			Arrow side = eigenCode.get(i);
			Arrow action = eigenCode.get(i + 1);
			switch (action) {
				case UP -> edges.get(side).extrudeUp(core);
				case DOWN -> edges.get(side).extrudeDown(core);
				case RIGHT -> edges.get(side.rightNeighbor()).extrudeForward(core);
				case LEFT -> edges.get(side.leftNeighbor()).extrudeForward(core);
			}
		}

		int size = 1;
		for (var e : edges.values()) size = Math.max(size, e.size());
		return new Eidos(core, size);
	}
}
