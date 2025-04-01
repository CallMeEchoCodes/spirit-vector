package symbolics.division.spirit_vector.sfx.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;

public class FeatherParticle extends SpiritParticle {

    protected float rotationSpeed;

	protected FeatherParticle(ClientWorld world, double x, double y, double z, double velocityX, double velocityY, double velocityZ, float rotation, float scale, SpriteProvider spriteProvider) {
        super(world, x, y, z, velocityX, velocityY, velocityZ, spriteProvider);
		this.rotationSpeed = ((float) Math.random() - 0.5f) * rotation;
        this.angle = (float)((Math.random() - 0.5f) * Math.PI);
        this.prevAngle = this.angle;
		this.scale = scale;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.dead) {
            this.prevAngle = this.angle;
            this.angle += rotationSpeed;
        }
    }

    public static class FeatherParticleFactory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public FeatherParticleFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double x, double y, double z, double vx, double vy, double vz) {
			x += clientWorld.random.nextFloat() - 0.5;
			y += 1 + clientWorld.random.nextFloat() - 0.5;
			z += clientWorld.random.nextFloat() - 0.5;
			return new FeatherParticle(clientWorld, x, y, z, 0, 0, 0, 0.2f, 0.15f, this.spriteProvider);
		}
	}

	public static class FlitterParticleFactory implements ParticleFactory<SimpleParticleType> {
		private final SpriteProvider spriteProvider;

		public FlitterParticleFactory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}

		public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double x, double y, double z, double vx, double vy, double vz) {
			x += clientWorld.random.nextFloat() - 0.5;
			y += 1 + clientWorld.random.nextFloat() - 0.5;
			z += clientWorld.random.nextFloat() - 0.5;
			return new FeatherParticle(clientWorld, x, y, z, 0, 0, 0, 1f, 0.1f + clientWorld.random.nextFloat() / 5, this.spriteProvider);
		}
	}
}
