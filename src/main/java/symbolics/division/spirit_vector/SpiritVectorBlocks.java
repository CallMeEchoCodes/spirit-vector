package symbolics.division.spirit_vector;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.MapColor;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;

public class SpiritVectorBlocks {
	public static final Block MATERIA = of("materia", new Block(
		AbstractBlock.Settings.create().mapColor(MapColor.STONE_GRAY).strength(-1, 3600000).dropsNothing().allowsSpawning(Blocks::never).solidBlock(Blocks::never).nonOpaque()
	) {
		@Override
		public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
			if (!SpellDimension.SPELL_DIMENSION.isCasting()) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState());
			}
		}
	});

	private static Block of(String id, Block block) {
		return Registry.register(Registries.BLOCK, SpiritVectorMod.id(id), block);
	}

	public static void init() {}
}
