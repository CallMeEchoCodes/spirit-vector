package symbolics.division.spirit_vector;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.EntityShapeContext;
import net.minecraft.block.MapColor;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class SpiritVectorBlocks {
	public static BooleanProperty REAL = BooleanProperty.of("real");

	public static class Materia extends Block {
		public Materia(Settings settings) {
			super(settings);
			setDefaultState(getDefaultState().with(REAL, false));
		}

		@Override
		protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
			builder.add(REAL);
		}

		@Override
		public void randomDisplayTick(BlockState state, World world, BlockPos pos, Random random) {
			if (!state.get(REAL) && !world.spellDimension().isCasting()) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
			}
		}

		@Override
		protected void randomTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
			if (state.get(REAL) && world.spellDimension().isCasting()) {
				world.setBlockState(pos, Blocks.AIR.getDefaultState(), Block.NOTIFY_LISTENERS);
			}
		}

		@Override
		protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean notify) {
			super.neighborUpdate(state, world, pos, sourceBlock, sourcePos, notify);
		}

		public static boolean removable(BlockState state, boolean real) {
			return state.isOf(MATERIA) && state.get(REAL) == real;
		}

		@Override
		protected VoxelShape getCollisionShape(BlockState state, BlockView world, BlockPos pos, ShapeContext context) {
			// sorry jas
			if (context instanceof EntityShapeContext esc &&
				esc.getEntity() instanceof LivingEntity living &&
				SpiritVector.hasEquipped(living)) {
				return super.getCollisionShape(state, world, pos, context);
			}
			return VoxelShapes.empty();
		}
	}

	public static final Block MATERIA = of("materia", new Materia(
		AbstractBlock.Settings.create()
			.mapColor(MapColor.STONE_GRAY)
			.strength(-1, 3600000)
			.dropsNothing().allowsSpawning(Blocks::never)
			.solidBlock(Blocks::never)
			.nonOpaque()
			.ticksRandomly()
	));

	private static Block of(String id, Block block) {
		return Registry.register(Registries.BLOCK, SpiritVectorMod.id(id), block);
	}

	public static void init() {
	}
}
