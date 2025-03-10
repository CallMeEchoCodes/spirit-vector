package symbolics.division.spirit_vector.logic.spell;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import org.joml.Matrix2f;
import org.joml.Vector2f;
import org.joml.Vector3fc;
import symbolics.division.spirit_vector.SpiritVectorBlocks;

import java.util.Set;

public record Eidos(Set<Vector3fc> core, float size) {
	public void emplace(World world, BlockPos anchor, Direction dir, float decay) {
		Matrix2f r = new Matrix2f();
		Vector2f zx = new Vector2f();
		r.rotate(-dir.asRotation() * MathHelper.RADIANS_PER_DEGREE);

		BlockPos.Mutable pos = new BlockPos.Mutable();
		for (Vector3fc p : core) {
			// calc decay: % chance that block is not placed
			// d = scale factor with distance
			float d = 1f - Math.max(Math.abs(p.x()), Math.abs(p.z())) / size;
			if (world.getRandom().nextFloat() < decay + (1f - decay) / 2f * d) continue;

			// rotate to correct pos and place
			zx.set(p.z() - 1, p.x() - 1).mul(r);
			pos.set(
				anchor.getX() + zx.y(),
				anchor.getY() + p.y() - 3,
				anchor.getZ() + zx.x()
			);
			if (world.isAir(pos)) {
				world.setBlockState(pos, SpiritVectorBlocks.MATERIA.getDefaultState());
			}
		}
	}
}
