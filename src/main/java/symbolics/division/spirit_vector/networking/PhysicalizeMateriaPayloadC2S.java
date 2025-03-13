package symbolics.division.spirit_vector.networking;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.block.Block;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import symbolics.division.spirit_vector.SpiritVectorBlocks;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.ISpiritVectorUser;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.List;

public record PhysicalizeMateriaPayloadC2S(int ticksLeft, List<BlockPos> blocks) implements CustomPayload {
	public static final CustomPayload.Id<PhysicalizeMateriaPayloadC2S> ID = SpiritVectorMod.payloadId("physicalize_materia_c2s");
	public static final PacketCodec<PacketByteBuf, PhysicalizeMateriaPayloadC2S> CODEC =
		CustomPayload.codecOf(
			(p, b) -> b.writeInt(p.ticksLeft).writeCollection(p.blocks, BlockPos.PACKET_CODEC),
			(b) -> new PhysicalizeMateriaPayloadC2S(b.readInt(), b.readList(BlockPos.PACKET_CODEC))
		);

	@Override
	public Id<? extends CustomPayload> getId() { return ID; }

	public static void HANDLER (PhysicalizeMateriaPayloadC2S payload, ServerPlayNetworking.Context context) {
 		PlayerEntity player = context.player();
		if (SpiritVector.hasEquipped(player)) {
			World world = player.getWorld();
			int size = 1;
			BlockPos bp = player.getBlockPos();
			for (BlockPos pos : payload.blocks) {
				world.setBlockState(pos, SpiritVectorBlocks.MATERIA.getDefaultState().with(SpiritVectorBlocks.REAL, true), Block.NOTIFY_LISTENERS);
				size = (int)Math.ceil(Math.max(size, MathHelper.sqrt((float)bp.getSquaredDistance(pos))));
			}
			SpellDimension.SPELL_DIMENSION.eidosPlaced(world, bp, size, payload.ticksLeft);
		}
	}
}
