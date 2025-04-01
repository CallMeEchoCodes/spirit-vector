package symbolics.division.spirit_vector.sfx.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class GrindsparkParticle extends SpriteBillboardParticle {

    private final SpriteProvider spriteProvider;

    protected GrindsparkParticle(ClientWorld world, double x, double y, double z, double vx, double vy, double vz, float scale, SpriteProvider spriteProvider) {
        super(world, x, y, z, 0.0, 0.0, 0.0);
        this.velocityMultiplier = 1;
        this.spriteProvider = spriteProvider;
        this.x = x;
        this.y = y;
        this.z = z;
        this.velocityX = vx;
        this.velocityY = vy;
        this.velocityZ = vz;
        this.scale *= scale;
        this.maxAge = (int)(20 * (Math.random() * 0.4 + 0.2));
        this.collidesWithWorld = false;
        this.setSprite(this.spriteProvider.getSprite(world.getRandom()));
    }

    @Override
    public ParticleTextureSheet getType() {
        return ParticleTextureSheet.PARTICLE_SHEET_LIT;
    }

    @Override
    public float getSize(float tickDelta) {
        return this.scale;
    }

    @Override
    public void tick() {
//        if (!this.dead) {
//            this.velocityY *= 1.1;
//        }
        super.tick();
    }

    @Override
    public void setSpriteForAge(SpriteProvider spriteProvider) {}

    public static class GrindsparkParticleFactory implements ParticleFactory<SimpleParticleType> {
        private final SpriteProvider spriteProvider;

        public GrindsparkParticleFactory(SpriteProvider spriteProvider) {
            this.spriteProvider = spriteProvider;
        }

        public Particle createParticle(SimpleParticleType simpleParticleType, ClientWorld clientWorld, double x, double y, double z, double vx, double vy, double vz) {
			Vec3d v2 = new Vec3d(vx, vy, vz).rotateY((float)(clientWorld.random.nextFloat() * 1.5 - 0.75));
			vx = (-v2.x )* 0.5;
			vy = -vy*0.3 - MathHelper.sign(vy) * 0.2;
			vz = (-v2.z)* 0.5;
			y += 0.5;
            return new GrindsparkParticle(clientWorld, x, y, z, vx, vy, vz, 2, this.spriteProvider);
        }
    }
}
