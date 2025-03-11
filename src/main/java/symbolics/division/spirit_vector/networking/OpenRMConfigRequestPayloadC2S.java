package symbolics.division.spirit_vector.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.screen.RuneMatrixScreenHandler;

public record OpenRMConfigRequestPayloadC2S() implements CustomPayload {
	public static  final CustomPayload.Id<OpenRMConfigRequestPayloadC2S> ID = SpiritVectorMod.payloadId("open_rm_config_c2s");
	public static final PacketCodec<PacketByteBuf, OpenRMConfigRequestPayloadC2S> CODEC =
		CustomPayload.codecOf(
			(p, b) -> {},
			(b) -> new OpenRMConfigRequestPayloadC2S()
		);

	@Override
	public Id<? extends CustomPayload> getId() {
		return ID;
	}

	public static void HANDLER(OpenRMConfigRequestPayloadC2S payload, ServerPlayNetworking.Context context) {
		context.player().openHandledScreen(RuneMatrixScreenHandler.createScreenHandlerFactory(
			true, context.player().getWorld(), context.player().getBlockPos()
		));
	}
}
