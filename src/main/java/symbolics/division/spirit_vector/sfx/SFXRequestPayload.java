package symbolics.division.spirit_vector.sfx;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import org.joml.Vector3f;
import symbolics.division.spirit_vector.SpiritVectorMod;

public record SFXRequestPayload(Identifier type, SFXPack<?> pack, Vector3f pos, Vector3f dir) implements CustomPayload {
    public static final Identifier PARTICLE_EFFECT_TYPE = SpiritVectorMod.id("particle_effect");
    public static final Identifier RING_EFFECT_TYPE = SpiritVectorMod.id("ring_effect");
    public static final Identifier BURST_SOUND_TYPE = SpiritVectorMod.id("burst_sound");
	public static final Identifier KICKOFF_EFFECT_TYPE = SpiritVectorMod.id("kickoff");

    public static final CustomPayload.Id<SFXRequestPayload> ID = new CustomPayload.Id<SFXRequestPayload>(SpiritVectorMod.id("sfx_request"));
    public static final PacketCodec<RegistryByteBuf, SFXRequestPayload> CODEC = PacketCodec.tuple(
            Identifier.PACKET_CODEC, SFXRequestPayload::type,
            SFXPack.PACKET_CODEC, SFXRequestPayload::pack,
            PacketCodecs.VECTOR3F, SFXRequestPayload::pos,
            PacketCodecs.VECTOR3F, SFXRequestPayload::dir,
            SFXRequestPayload::new
    );

    @Override
    public Id<? extends CustomPayload> getId() {
        return ID;
    }

}
