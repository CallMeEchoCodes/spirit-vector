package symbolics.division.spirit_vector;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;

public class SpiritVectorBlocks {
	public static final Block MATERIA = of("materia", new Block(
		AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(-1, 3600000).dropsNothing().allowsSpawning(Blocks::never)
	));

	private static Block of(String id, Block block) {
		return Registry.register(Registries.BLOCK, SpiritVectorMod.id(id), block);
	}

	public static void init() {}
}
