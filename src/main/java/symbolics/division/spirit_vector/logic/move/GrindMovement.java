package symbolics.division.spirit_vector.logic.move;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.SpiritVectorTags;
import symbolics.division.spirit_vector.logic.TravelMovementContext;
import symbolics.division.spirit_vector.logic.input.Input;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class GrindMovement extends NeutralMovement {
	private static enum Card8 {
		E, NE, N, NW, W, SW, S, SE
	}

	public GrindMovement(Identifier id) {
		super(id);
	}

	@Override
	public boolean testMovementCondition(SpiritVector sv, TravelMovementContext ctx) {
		Vec3d vel = sv.user.getVelocity();
		// determine card8 of velocity

		double theta = MathHelper.atan2(-vel.z, vel.x);
		int octant = (int)Math.round(8 * theta  / (2 * Math.PI) + 8) % 8;

		System.out.println("octant: " + octant);
		return false;
//		if (sv.user.isOnGround()
//			&& sv.inputManager().rawInput(Input.CROUCH)
//			&& (!MathHelper.approximatelyEquals(sv.user.getVelocity().x, 0.0)
//			 || !MathHelper.approximatelyEquals(sv.user.getVelocity().z, 0.0))
//			&& sv.user.getSteppingBlockState().isIn(SpiritVectorTags.Blocks.RAIL_GRINDABLE)
//		) {
//			sv.inputManager().consume(Input.CROUCH);
//			return true;
//		}
//		return false;
	}

	@Override
	public void travel(SpiritVector sv, TravelMovementContext ctx) {
		// movement should resemble sliding, but locked to rail axis

		// slide, but in fixed direction based on what's below.
		// don't lock too much to grid, since there's things like
		// wires we might want to grind on too.




		Vec3d vel = sv.user.getVelocity();
		// determine card8 of velocity

		double theta = MathHelper.atan2(-vel.z, vel.x);
		int octant = (int)Math.round(8 * theta  / (2 * Math.PI) + 8) % 8;


		// get nearest checkpoint
		// find next checkpoint

		// project onto centerline

		// unroll motion: straight length is 1, diagonal is 1/sqrt(2)


		Vec3d input = ctx.inputDir(); // non-augmented specifically (change ?)
		SlideMovement.travelWithInput(sv, input);
		ctx.ci().cancel();
	}
}
