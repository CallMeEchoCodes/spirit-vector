package symbolics.division.spirit_vector.mixin.client;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.SVEntityState;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin extends AbstractClientPlayerEntity implements ISpiritVectorUser {
    private ClientPlayerEntityMixin(ClientWorld world, GameProfile profile) {
        super(world, profile);
    }

	@Unique
	public SpiritVector dbsv$spiritVector;

	@Unique
	public ItemStack dbsv$prevStack = ItemStack.EMPTY;

	@Override
    public SpiritVector spiritVector() {
        ItemStack item = SpiritVector.getEquippedItem(this);
        if (item == null) {
			dbsv$spiritVector = null;
		} else if (dbsv$spiritVector == null || !ItemStack.areItemsAndComponentsEqual(item, dbsv$prevStack)) {
			dbsv$spiritVector = SpiritVector.of((LivingEntity) (Entity) this, item);
			dbsv$prevStack = item;
            setWingState(false);
        }
		return dbsv$spiritVector;
    }

    @Override
    public void setWingState(boolean visible) {
        SVEntityState state = new SVEntityState(visible);
        this.setAttached(SVEntityState.ATTACHMENT, state);
        SVEntityState.Payload payload = new SVEntityState.Payload(this.getId(), state);
        ClientPlayNetworking.send(payload);
    }

    @Inject(method = "shouldSlowDown", at = @At("HEAD"), cancellable = true)
    public void shouldSlowDown(CallbackInfoReturnable<Boolean> ci) {
        // don't let crouching or crawling
        // get in the way of our fun
        if (getSpiritVector().isPresent()) {
            ci.setReturnValue(false);
            ci.cancel();
        }
    }
}
