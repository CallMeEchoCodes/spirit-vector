package symbolics.division.spirit_vector.logic.spell;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.input.Arrow;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

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
	private final List<BlockPos> anchors = new ArrayList<>();
	private final BlockPos center;

	public Spell(SpiritVector sv, List<Arrow> eigenCode) {
		this.sv = sv;
		StringBuilder code = new StringBuilder();
		for (Arrow a : eigenCode) code.append(a.sym);
		String unique = code.toString().replaceAll("(.)(?=.*?\\1)", "");

		this.complexity = (float)(Math.min(eigenCode.size(), 8) + unique.length()) / (float)(8 + 4);
		// base 0.9 is ideal for sky runes
		this.decay= 0.9f * (1f - complexity);
		this.core = makeCore(eigenCode);

		this.maxSpellRadius = (int)(MIN_SPELL_DIMENSION_RADIUS + (MAX_SPELL_DIMENSION_RADIUS - MIN_SPELL_DIMENSION_RADIUS) * complexity);
		this.ticksLeft = (int)(MIN_SPELL_TICKS + (MAX_SPELL_TICKS - MIN_SPELL_TICKS) * complexity);
		SpiritVectorMod.LOGGER.info("casting spell: " + code);
		SpiritVectorMod.LOGGER.info("radius: " + this.maxSpellRadius);
		SpiritVectorMod.LOGGER.info("complexity: " + complexity);
		SpiritVectorMod.LOGGER.info("decay: " + decay);

//		BlockPos ppos = sv.user.getBlockPos();
//		BlockPos.Mutable bp = new BlockPos.Mutable();
//		for (int x = -(int)spellRadius; x < spellRadius; x++) {
//			for (int y = -(int)spellRadius; y < spellRadius; y++) {
//				for (int z = -(int)spellRadius; z < spellRadius; z++) {
//					if (x % EIDOS_SPACING_HORIZONTAL == 0 && z % EIDOS_SPACING_HORIZONTAL == 0 && y % EIDOS_SPACING_VERTICAL == 0) {
//						Direction d = Direction.byId((x + y * 2 + z * 3) % 4 + 2);
//						bp.set(ppos.getX() + x, ppos.getY() + y, ppos.getZ() + z);
//						spell.core.emplace(world, bp, d, decay);
//					}
//				}
//			}
//		}

		center = sv.user.getBlockPos();
		for (int x = -(int)maxSpellRadius; x < maxSpellRadius; x++) {
			for (int y = -(int)maxSpellRadius; y < maxSpellRadius; y++) {
				for (int z = -(int)maxSpellRadius; z < maxSpellRadius; z++) {
					if (x % EIDOS_SPACING_HORIZONTAL == 0 && z % EIDOS_SPACING_HORIZONTAL == 0 && y % EIDOS_SPACING_VERTICAL == 0) {
						anchors.add(new BlockPos(center.getX() + x, center.getY() + y, center.getZ() + z));
					}
				}
			}
		}
	}

	public void tick() {
		ticksLeft--;
		if (ticksLeft < 0) return;
		if (!sv.user.isAlive() || sv.user.isRemoved() || !SpiritVector.hasEquipped(sv.user)) {
			ticksLeft = 0;
			return;
		}
		spellRadius++;
		if (spellRadius > maxSpellRadius) return;

		List<BlockPos> check = List.copyOf(anchors);
		for (BlockPos bp : check) {
			if (bp.isWithinDistance(this.center, spellRadius)) {
				this.anchors.remove(bp);
				Direction d = Direction.byId((bp.getX() + bp.getY() * 2 + bp.getZ() * 3) % 4 + 2);
				this.core.emplace(sv.user.getWorld(), bp, d, decay);
			}
		}
//
//		BlockPos.Mutable bp = new BlockPos.Mutable();
//		for (int x = -(int)spellRadius; x < spellRadius; x++) {
//			for (int y = -(int)spellRadius; y < spellRadius; y++) {
//				for (int z = -(int)spellRadius; z < spellRadius; z++) {
//					// only update shell
//					if (Math.abs(x) < spellRadius - 4 || Math.abs(y) < spellRadius - 4 || Math.abs(z) < spellRadius - 4) continue;
//					if (x % EIDOS_SPACING_HORIZONTAL == 0 && z % EIDOS_SPACING_HORIZONTAL == 0 && y % EIDOS_SPACING_VERTICAL == 0) {
//						Direction d = Direction.byId((x + y * 2 + z * 3) % 4 + 2);
//						bp.set(ppos.getX() + x, ppos.getY() + y, ppos.getZ() + z);
//						this.core.emplace(sv.user.getWorld(), bp, d, decay);
//					}
//				}
//			}
//		}


	}

	public int ticksLeft() {
		return ticksLeft;
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

		for (int i = 0; i+1 < eigenCode.size(); i += 2) {
			Arrow side = eigenCode.get(i);
			Arrow action = eigenCode.get(i+1);
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
