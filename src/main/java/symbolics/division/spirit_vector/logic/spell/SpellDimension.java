package symbolics.division.spirit_vector.logic.spell;

import com.mojang.datafixers.util.Pair;
import it.unimi.dsi.fastutil.objects.Object2ObjectLinkedOpenHashMap;
import net.fabricmc.api.EnvType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import symbolics.division.spirit_vector.SpiritVectorBlocks;
import symbolics.division.spirit_vector.SpiritVectorMod;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class SpellDimension {
	// SpellDimension instance lasts for the length of a spell
	// since Materia only exists clientside, we don't need to worry
	// about persistence between sessions.

	private static Consumer<Spell> spellCallback = s -> {
	};
	public static final int EIDOS_PER_TICK = 40;
	private static final boolean DEDICATED_SERVER = FabricLoader.getInstance().getEnvironmentType().equals(EnvType.SERVER);

	public static void setSpellCallback(Consumer<Spell> spellConsumer) {
		spellCallback = spellConsumer;
	}

	private final World world;
	private final List<Spell> activeSpells = new ArrayList<>();
	private final Map<Pair<World, BlockPos>, EidosInfo> eidosTracker = new Object2ObjectLinkedOpenHashMap<>();

	public SpellDimension(World world) {
		this.world = world;
	}

	public void cast(Spell spell) {
		activeSpells.add(spell);
		spellCallback.accept(spell);
	}

	public void tick() {
		if (!(world.isClient || SpiritVectorMod.PHYSICAL_SERVER)) {
			throw new RuntimeException("Integrated server spell dimension must never be ticked.");
		}
		List<Spell> toTick = List.copyOf(activeSpells);
		for (Spell spell : toTick) {
			if (spell.cancelled()) {
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
			if (entry.getKey() == null) {
				SpiritVectorMod.LOGGER.error("KEY IS NULL FIX ME");
				continue;
			}
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
					if (SpiritVectorBlocks.Materia.removable(world.getBlockState(blockPos), !world.isClient)) {
						world.setBlockState(blockPos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
					}
				}
			}
		}
		for (var key : toRemove) eidosTracker.remove(key);
	}

	public boolean isCasting() {
		return !eidosTracker.isEmpty();
	}

	public int ticksLeft() {
		int left = 0;
		for (Spell spell : activeSpells) {
			left = Math.max(spell.ticksLeft(), left);
		}
		return left;
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
