package symbolics.division.spirit_vector.sfx;

import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.joml.Vector3f;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

import java.util.function.Consumer;

/*
    Nightmare static sludge DO NOT READ
    This would be a lot better but there's a week left
 */
public class EffectsManager {
    
    public static void acceptC2SPayload(SFXRequestPayload payload, ServerPlayNetworking.Context ctx) {
        var player = ctx.player();
        if (payload.type().equals(SFXRequestPayload.PARTICLE_EFFECT_TYPE)) {
			spawnParticleImpl((ServerWorld) player.getWorld(), payload.pack(), new Vec3d(payload.pos()), new Vec3d(payload.dir()));
        } else if (payload.type().equals(SFXRequestPayload.RING_EFFECT_TYPE)) {
            spawnRingImpl((ServerWorld)player.getWorld(), payload.pack(), new Vec3d(payload.pos()), new Vec3d(payload.dir()));
        } else if (payload.type().equals(SFXRequestPayload.BURST_SOUND_TYPE)) {
            playBurstImpl(player, new Vec3d(payload.pos()));
        } else if (payload.type().equals(SFXRequestPayload.KICKOFF_EFFECT_TYPE)) {
			kickoffImpl((ServerWorld) player.getWorld(), new Vec3d(payload.pos()));
		}
    }

    private static Consumer<SFXRequestPayload> requestCallback = c -> {};
    public static void registerSFXRequestC2SCallback(Consumer<SFXRequestPayload> cb) {
        requestCallback = cb;
    }

    private final SpiritVector sv;

    public EffectsManager(SpiritVector sv) {
        this.sv = sv;
    }

	public void spawnParticle(World world, Vec3d pos, Vec3d dir) {
        if (world.isClient) {
			world.addParticle(sv.getSFX().particleEffect(), pos.x, pos.y, pos.z, dir.x, dir.y, dir.z);
			requestCallback.accept(new SFXRequestPayload(SFXRequestPayload.PARTICLE_EFFECT_TYPE, sv.getSFX(), pos.toVector3f(), dir.toVector3f()));
        } else {
			spawnParticleImpl((ServerWorld) world, sv.getSFX(), pos, dir);
        }
    }

    public void spawnRing(Vec3d pos, Vec3d dir) {
        World world = sv.user.getWorld();
        if (world.isClient) {
            requestCallback.accept(new SFXRequestPayload(SFXRequestPayload.RING_EFFECT_TYPE, sv.getSFX(), pos.toVector3f(), dir.toVector3f()));
            requestCallback.accept(new SFXRequestPayload(SFXRequestPayload.BURST_SOUND_TYPE, sv.getSFX(), pos.toVector3f(), dir.toVector3f()));
        } else {
            spawnRingImpl((ServerWorld) world, sv.getSFX(), pos, dir);
        }
    }

	public void kickoff(Vec3d pos) {
		World world = sv.user.getWorld();
		if (world.isClient) {
			requestCallback.accept(new SFXRequestPayload(SFXRequestPayload.KICKOFF_EFFECT_TYPE, sv.getSFX(), pos.toVector3f(), new Vector3f()));
		} else {
			kickoffImpl((ServerWorld) world, pos);
		}
	}

    // TODO a nonstaticified version of this
	private static void spawnParticleImpl(ServerWorld world, SFXPack<?> sfx, Vec3d pos, Vec3d dir) {
        world.spawnParticles(
			sfx.particleEffect(), pos.x, pos.y, pos.z, 0, dir.x, dir.y, dir.z, 1
        );
    }

    private static void spawnRingImpl(ServerWorld world, SFXPack<?> sfx, Vec3d pos, Vec3d dir) {
        var speed = dir.length();
        Vec3d[] uv = basis(dir.normalize());
        for (float i = 0; i <= Math.PI*2; i += Math.PI/12) {
            Vec3d p = pos.add(uv[0].multiply(Math.cos(i))).add(uv[1].multiply(Math.sin(i)));
            Vec3d d = p.subtract(pos);
            world.spawnParticles(
                    sfx.particleEffect(), p.x, p.y, p.z, 1, d.x, d.y, d.z, 0.4  * speed
            );
        }
    }

	private static void kickoffImpl(ServerWorld world, Vec3d pos) {
		world.playSound(null, BlockPos.ofFloored(pos), SoundEvents.ITEM_SHIELD_BLOCK, SoundCategory.PLAYERS, 0.3f, 0.8f);
		world.spawnParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.x, pos.y, pos.z, 10, 0.3, 0.1, 0.3, 0.01);
	}

    // return u, v for Householder reflector
    private static Vec3d[] basis(Vec3d vec) {
        double l = vec.length();
        double sigma = Math.signum(l);
        double h = vec.x + sigma;
        double beta = -1d / (sigma * h);

        Vec3d[] out = new Vec3d[2];
        double f = beta * vec.y;
        out[0] = new Vec3d(f*h, 1d+f*vec.y, f*vec.z);
        double g = beta * vec.z;
        out[1] = new Vec3d(g*h, g*vec.y, 1d+g*vec.z);
        return out;
    }

    private static void playBurstImpl(PlayerEntity player, Vec3d pos) {
        AudioGirl.burst(player, BlockPos.ofFloored(pos.x, pos.y, pos.z));
    }

}
