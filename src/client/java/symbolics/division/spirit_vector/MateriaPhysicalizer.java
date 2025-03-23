package symbolics.division.spirit_vector;

import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;
import symbolics.division.spirit_vector.networking.PhysicalizeMateriaPayloadC2S;

import java.util.ArrayList;
import java.util.List;

public class MateriaPhysicalizer {
	public static void tick() {
		PlayerEntity player = MinecraftClient.getInstance().player;
		if (player == null ||
			!player.isAlive() ||
			!SpiritVector.hasEquipped(player) ||
			!player.getWorld().spellDimension().isCasting()
		) return;

		Box bb = player.getBoundingBox().expand(2);
		List<BlockPos> toSend = new ArrayList<>();
		for (BlockPos bp : BlockPos.iterate(BlockPos.ofFloored(bb.getMinPos()), BlockPos.ofFloored(bb.getMaxPos()))) {
			if (player.getWorld().testBlockState(bp, state -> state.isOf(SpiritVectorBlocks.MATERIA))) {
				toSend.add(bp.toImmutable());
			}
		}

		if (!toSend.isEmpty()) {
			int ticksLeft = player.getWorld().spellDimension().ticksLeft();
			if (ticksLeft > 0) ClientPlayNetworking.send(new PhysicalizeMateriaPayloadC2S(ticksLeft, toSend));
		}
	}
}
