package symbolics.division.spirit_vector;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerWorldEvents;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import symbolics.division.spirit_vector.compat.ModCompatibility;
import symbolics.division.spirit_vector.logic.SVEntityState;
import symbolics.division.spirit_vector.logic.ability.SlamPacketC2S;
import symbolics.division.spirit_vector.logic.ability.TeleportAbilityC2SPayload;
import symbolics.division.spirit_vector.logic.spell.SpellDimension;
import symbolics.division.spirit_vector.networking.ModifyMomentumPayloadS2C;
import symbolics.division.spirit_vector.networking.OpenRMConfigRequestPayloadC2S;
import symbolics.division.spirit_vector.networking.PhysicalizeMateriaPayloadC2S;
import symbolics.division.spirit_vector.registry.SpiritVectorRegistration;
import symbolics.division.spirit_vector.sfx.EffectsManager;
import symbolics.division.spirit_vector.sfx.SFXRequestPayload;

public final class SpiritVectorMod implements ModInitializer {
	public static final String MODID = "spirit_vector";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

	public static Identifier id(String identifier) { return Identifier.of(MODID, identifier); }
	public static <T extends CustomPayload> CustomPayload.Id<T> payloadId(String identififer) {
		return new CustomPayload.Id<>(id(identififer));
	}

	@Override
	public void onInitialize() {
		LOGGER.info("Fly forth, Spirit Vector!");
		SpiritVectorRegistration.init();
		SpiritVectorItems.init();
		SpiritVectorBlocks.init();
		SpiritVectorSounds.init();
		SpiritVectorTags.init();
		SpiritVectorLoot.init();

		PayloadTypeRegistry.playC2S().register(SFXRequestPayload.ID, SFXRequestPayload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SFXRequestPayload.ID, EffectsManager::acceptC2SPayload);

		registerC2S(TeleportAbilityC2SPayload.ID, TeleportAbilityC2SPayload.CODEC, TeleportAbilityC2SPayload::HANDLER);
		registerC2S(SlamPacketC2S.ID, SlamPacketC2S.CODEC, SlamPacketC2S::HANDLER);

		registerC2S(OpenRMConfigRequestPayloadC2S.ID, OpenRMConfigRequestPayloadC2S.CODEC, OpenRMConfigRequestPayloadC2S::HANDLER);

		PayloadTypeRegistry.playC2S().register(SVEntityState.Payload.ID, SVEntityState.Payload.CODEC);
		PayloadTypeRegistry.playS2C().register(SVEntityState.Payload.ID, SVEntityState.Payload.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(SVEntityState.Payload.ID, SVEntityState::handleStateSyncC2S);

		PayloadTypeRegistry.playC2S().register(PhysicalizeMateriaPayloadC2S.ID, PhysicalizeMateriaPayloadC2S.CODEC);
		PayloadTypeRegistry.playS2C().register(PhysicalizeMateriaPayloadC2S.ID, PhysicalizeMateriaPayloadC2S.CODEC);
		ServerPlayNetworking.registerGlobalReceiver(PhysicalizeMateriaPayloadC2S.ID, PhysicalizeMateriaPayloadC2S::HANDLER);

		ServerTickEvents.START_WORLD_TICK.register(SpellDimension::worldTick);
		ServerLifecycleEvents.SERVER_STARTED.register(server -> SpellDimension.SPELL_DIMENSION.clearEidos());

		// hoping this covers all cases where players should be updated on wing state
		// TODO doesn't seem to work, figure out during integration with larger sample size
		ServerEntityEvents.ENTITY_LOAD.register(((entity, world) -> {
			if (entity instanceof ServerPlayerEntity player) SVEntityState.updatePlayerStatesOnSpawn(player);
		}));
		ServerPlayConnectionEvents.JOIN.register(((handler, sender, server) -> SVEntityState.updatePlayerStatesOnSpawn(handler.player)));

		PayloadTypeRegistry.playS2C().register(ModifyMomentumPayloadS2C.ID, ModifyMomentumPayloadS2C.CODEC);

		ModCompatibility.init();
	}
	//<T extends CustomPayload> CustomPayload.Type<? super B, T>
	private <T extends CustomPayload>
	void registerC2S(CustomPayload.Id<T> pid, PacketCodec<? super RegistryByteBuf, T> codec, ServerPlayNetworking.PlayPayloadHandler<T> handler) {
		PayloadTypeRegistry.playC2S().register(pid, codec);
		ServerPlayNetworking.registerGlobalReceiver(pid, handler);
	}

//	registerS2C
}
