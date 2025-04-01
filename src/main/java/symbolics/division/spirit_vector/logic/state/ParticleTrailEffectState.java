package symbolics.division.spirit_vector.logic.state;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.Vec3d;
import symbolics.division.spirit_vector.SpiritVectorMod;
import symbolics.division.spirit_vector.logic.vector.SpiritVector;

public class ParticleTrailEffectState extends ManagedState {
    public static final Identifier ID = SpiritVectorMod.id("particle_trail");

    public ParticleTrailEffectState(SpiritVector sv) {
        super(sv);
    }

    @Override
    public void tick() {
        var user = sv.user;
//        int particleRate = 3 - (3 * sv.getMomentum() / SpiritVector.MAX_MOMENTUM);
		Vec3d vel = user.getVelocity().normalize();
		sv.effectsManager().spawnParticle(user.getWorld(), user.getPos().subtract(vel), vel);
        super.tick();
    }
}
