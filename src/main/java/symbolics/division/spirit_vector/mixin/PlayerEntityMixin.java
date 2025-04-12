package symbolics.division.spirit_vector.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.sugar.Local;
import net.minecraft.block.BlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import symbolics.division.spirit_vector.logic.ability.LedgeLockAbility;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.sfx.AudioGirl;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin extends LivingEntity {
	protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, World world) {
		super(entityType, world);
	}

	@Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
	public void clipAtLedge(CallbackInfoReturnable<Boolean> ci) {
		ItemStack stack = SpiritVector.getEquippedItem(this);
		if (stack != null && !LedgeLockAbility.hasLedgeLock(stack, this)) {
			ci.setReturnValue(false);
			ci.cancel();
		}
	}

	@Inject(method = "playStepSound", at = @At("HEAD"), cancellable = true)
	public void playStepSound(BlockPos pos, BlockState state, CallbackInfo ci) {
		if (SpiritVector.hasEquipped(this) && !this.isInFluid()) {
			if (!this.isSneaking() && AudioGirl.step((PlayerEntity) (Entity) this, state)) {
				ci.cancel();
			}
		}
	}

	@ModifyReturnValue(
		method = "adjustMovementForSneaking",
		at = @At(value = "RETURN", ordinal = 1)
	)
	public Vec3d modifyLedgeClip(Vec3d modified, @Local(ordinal = 0, argsOnly = true) Vec3d movement) {
		// should only be accessible if ledgelock rune applied
		if (!SpiritVector.hasEquipped(this) || (modified.x == movement.x && modified.z == movement.z) || !this.isSneaking())
			return modified;

		// if we're here, we're modifying movement to keep us from
		// falling off some edge. Solution: If motion is within 30 degrees of
		// a cardinal direction, restore the original motion on that axis
		Direction.Axis axis = Direction.getFacing(this.getVelocity()).getAxis();
		Direction.Axis other = axis == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
		this.setVelocity(this.getVelocity().withAxis(other, 0));
		return modified.withAxis(axis, movement.getComponentAlongAxis(axis));
	}
}
