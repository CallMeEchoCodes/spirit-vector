package symbolics.division.spirit_vector.logic.spell;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.PriorityQueue;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.ObjectHeapPriorityQueue;
import net.minecraft.block.Blocks;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.command.FillCommand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.dimension.DimensionTypes;
import net.minecraft.world.explosion.Explosion;
import symbolics.division.spirit_vector.SpiritVectorBlocks;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SpellDimension {
	// SpellDimension instance lasts for the length of a spell
	// since Materia only exists clientside, we don't need to worry
	// about persistence between sessions.

	public static final SpellDimension SPELL_DIMENSION = new SpellDimension();

	private static Consumer<Spell> spellCallback = s -> {};
	public static final int EIDOS_PER_TICK = 40;

	public static void setSpellCallback(Consumer<Spell> spellConsumer) {
		spellCallback = spellConsumer;
	}

	public static void cast(Spell spell) {
		SPELL_DIMENSION.activeSpells.add(spell);
		spellCallback.accept(spell);
	}

	public static void worldTick(World world) {
		SPELL_DIMENSION.tick(world);
	}

	private final List<Spell> activeSpells = new ArrayList<>();
	private final Map<Pair<World, BlockPos>, EidosInfo> eidosTracker = new Object2ObjectLinkedOpenHashMap<>();

	public void tick(World world) {
		List<Spell> toTick = List.copyOf(activeSpells);
		for (Spell spell : toTick) {
			if (spell.ticksLeft() <= 0) {
				activeSpells.remove(spell);
			} else {
				spell.tick(this);
			}
		}
		this.cullEidos(world);
	}

	public void eidosPlaced(World world, BlockPos anchor, int size, int ticksLeft) {
		Pair<World, BlockPos> key = Pair.of(world, anchor);
		EidosInfo info = eidosTracker.get(key);
		if (info == null) {
			info = new EidosInfo(size, ticksLeft);
			eidosTracker.put(key, info);
		} else {
			info.update(size, ticksLeft);
		}
	}

	private void cullEidos(World world) {
		List<Pair<World, BlockPos>> toRemove = new ArrayList<>();
		int removed = EIDOS_PER_TICK;
		for (var entry : eidosTracker.entrySet()) {
			if (!entry.getKey().getFirst().equals(world)) continue;
			if (removed <= 0) break;
			if (entry.getValue().tick()) {
				removed--;
				toRemove.add(entry.getKey());
				BlockPos pos = entry.getKey().getSecond();
				EidosInfo info = entry.getValue();
				for (BlockPos blockPos : BlockPos.iterate(
					pos.getX() - info.size,
					pos.getY() - info.size,
					pos.getZ() - info.size,
					pos.getX() + info.size,
					pos.getY() + info.size,
					pos.getZ() + info.size)) {
					if (world.getBlockState(blockPos).isOf(SpiritVectorBlocks.MATERIA)) {
						world.setBlockState(blockPos, Blocks.AIR.getDefaultState());
					}
				}
			}
		}
		for (var key : toRemove) eidosTracker.remove(key);
	}

	public boolean isCasting() {
		return !activeSpells.isEmpty();
	}

	private static class EidosInfo {
		private int size;
		private int ticksLeft;

		public EidosInfo(int initialSize, int ticksLeft) {
			this.size = initialSize;
			this.ticksLeft = ticksLeft;
		}

		public boolean tick() {
			ticksLeft--;
			return ticksLeft < 0; // true if time to remove
		}

		public void update(int size, int ticksLeft) {
			this.size = Math.max(this.size, size);
			this.ticksLeft = Math.max(this.ticksLeft, ticksLeft);
		}
	}
}
