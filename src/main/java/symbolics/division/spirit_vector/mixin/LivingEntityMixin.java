package symbolics.division.spirit_vector.mixin;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.ability.WaterRunAbility;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity {
	@Shadow
	public abstract boolean isInCreativeMode();

	@Shadow
	public abstract boolean isClimbing();

	private LivingEntityMixin(EntityType<?> type, World world) {
		super(type, world);
	}

	// cache instanceof check
	// maybe generalize into caching object
	@Unique
	private boolean dbsv$checked = false;
	@Unique
	private boolean dbsv$isSVUser = false;

	@Nullable
	public SpiritVector maybeGetSpiritVector() {
		if (!dbsv$checked) {
			dbsv$checked = true;
			if (this instanceof ISpiritVectorUser user) {
				dbsv$isSVUser = true;
			}
		}
		if (dbsv$isSVUser) {
			if (((LivingEntity) (Object) this) instanceof PlayerEntity player && player.getAbilities().flying)
				return null;
			return ((ISpiritVectorUser) this).spiritVector();
		}
		return null;
	}

	@Inject(method = "travel", at = @At("HEAD"), cancellable = true)
	public void travel(Vec3d movementInput, CallbackInfo ci) {
		SpiritVector sv = maybeGetSpiritVector();
		if (sv != null) {
			if (!this.isLogicalSideForUpdatingMovement() || this.hasVehicle() || ((LivingEntity) (Entity) this).isFallFlying() || this.isSubmergedInWater() || this.isClimbing()) {
				return;
			}
			sv.travel(movementInput, ci);
		}
	}

	@Inject(method = "getMovementSpeed", at = @At("HEAD"), cancellable = true)
	public void getMovementSpeed(float slip, CallbackInfoReturnable<Float> ci) {
		SpiritVector sv = maybeGetSpiritVector();
		if (sv != null && !this.isClimbing()) {
			ci.setReturnValue(sv.getMovementSpeed(slip));
			ci.cancel();
		}
	}

	@Inject(method = "jump", at = @At("HEAD"), cancellable = true)
	public void jump(CallbackInfo ci) {
		if (maybeGetSpiritVector() != null) {
			// take over jump movement completely
			ci.cancel();
		}
	}

	@Inject(method = "getStepHeight", at = @At("HEAD"), cancellable = true)
	public void getStepHeight(CallbackInfoReturnable<Float> ci) {
		SpiritVector sv = maybeGetSpiritVector();
		if (sv != null) {
			ci.setReturnValue(sv.getStepHeight());
			ci.cancel();
		}
	}

	@Inject(method = "handleFallDamage", at = @At("HEAD"), cancellable = true)
	public void handleFallDamage(float fallDistance, float damageMultiplier, DamageSource damageSource, CallbackInfoReturnable<Boolean> ci) {
		if (SpiritVector.hasEquipped((LivingEntity) (Object) this)) {
			// needs networking
//            if (this instanceof ISpiritVectorUser user) {
//                var sv = user.spiritVector();
//                if (sv.safeFallDistance() >= fallDistance) {
//                    ci.setReturnValue(false);
//                    ci.cancel();
//                }
//            } else
			ci.setReturnValue(false);
			ci.cancel();
		}
	}

	@Inject(method = "hasNoDrag", at = @At("HEAD"), cancellable = true)
	public void hasNoDrag(CallbackInfoReturnable<Boolean> ci) {
		SpiritVector sv = maybeGetSpiritVector();
		if (sv != null && sv.getMoveState().disableDrag(sv) && !this.isClimbing()) {
			ci.setReturnValue(true);
			ci.cancel();
		}
	}

	@WrapWithCondition(
		method = "damage",
		at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/LivingEntity;scheduleVelocityUpdate()V")
	)
	public boolean preventLowDamageVelocityUpdate(LivingEntity instance) {
		return !SpiritVector.hasEquipped((LivingEntity) (Object) this);
	}

	@Inject(method = "setSprinting", at = @At("HEAD"), cancellable = true)
	public void preventSprintingWithSpiritVectorEquipped(boolean sprinting, CallbackInfo ci) {
		if (sprinting) {
			SpiritVector sv = maybeGetSpiritVector();
			if (sv != null && !sv.user.isSubmergedInWater()) {
				ci.cancel();
			}
		}
	}

	@Inject(method = "canWalkOnFluid", at = @At("HEAD"), cancellable = true)
	public void applyWaveDancer(FluidState state, CallbackInfoReturnable<Boolean> cir) {
		SpiritVector sv = maybeGetSpiritVector();
		if (sv != null && WaterRunAbility.canWaterRun(sv)) {
			cir.setReturnValue(true);
			cir.cancel();
		}
	}
}
