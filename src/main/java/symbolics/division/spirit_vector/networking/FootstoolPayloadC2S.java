package symbolics.division.spirit_vector.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.sound.SoundEvents;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.registry.SpiritVectorDamageTypes;

import java.util.function.Consumer;

public record FootstoolPayloadC2S(int targetId, float damage) implements CustomPayload {
	public static final CustomPayload.Id<FootstoolPayloadC2S> ID = SpiritVectorMod.payloadId("footstool_c2s");
	public static final PacketCodec<PacketByteBuf, FootstoolPayloadC2S> CODEC =
		CustomPayload.codecOf(
			(p, b) -> b.writeInt(p.targetId).writeFloat(p.damage),
			(b) -> new FootstoolPayloadC2S(b.readInt(), b.readFloat())
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	private static Consumer<FootstoolPayloadC2S> callback = null;
	public static void register(Consumer<FootstoolPayloadC2S> sendPayloadCallback) {
		callback = sendPayloadCallback;
	}

	public static void send(Entity target, float damage) {
		callback.accept(new FootstoolPayloadC2S(target.getId(), damage));
	}

	public static void HANDLER(FootstoolPayloadC2S payload, ServerPlayNetworking.Context context) {
		PlayerEntity player = context.player();
		Entity target = player.getWorld().getEntityById(payload.targetId);
		if (target != null && SpiritVector.hasEquipped(player)) {
			target.damage(SpiritVectorDamageTypes.of(player.getWorld(), SpiritVectorDamageTypes.FOOTSTOOL), payload.damage);
			target.playSound(SoundEvents.ITEM_MACE_SMASH_AIR, 1, 0.5f + player.getRandom().nextFloat());
		}
	}
}
