package symbolics.division.spirit_vector.sfx.particle;

import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleFactory;
import net.minecraft.client.particle.ParticleTextureSheet;
import net.minecraft.client.particle.SpriteBillboardParticle;
import net.minecraft.client.particle.SpriteProvider;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.math.MathHelper;

public class SpellcastingParticle extends SpriteBillboardParticle {
	protected SpellcastingParticle(ClientWorld clientWorld, double px, double py, double pz, double vx, double vy, double vz, SpriteProvider provider) {
		super(clientWorld, px, py, pz);
		this.velocityMultiplier = 1.1f;
		this.velocityX = vx;
		this.velocityY = vy;
		this.velocityZ = vz;
		this.setSprite(provider);
		int h = hueRotate(clientWorld.getRandom().nextFloat() * MathHelper.PI * 2);
		this.setColor(((h >> 16) & 0xff) + 125, ((h >> 8) & 0xff) + 125, (h & 0xff) + 125);
	}

	// copy [vanilla]
	public static class Factory implements ParticleFactory<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType defaultParticleType, ClientWorld clientWorld,
									   double px, double py, double pz, double vx, double vy, double vz) {
			return new SpellcastingParticle(clientWorld, px, py, pz, vx, vy, vz, this.spriteProvider);
		}

		private final SpriteProvider spriteProvider;

		public Factory(SpriteProvider spriteProvider) {
			this.spriteProvider = spriteProvider;
		}
	}

	@Override
	public ParticleTextureSheet getType() {
		return ParticleTextureSheet.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	protected int getBrightness(float tint) {
		return 0xF000F0;
	}

	private static int hueRotate(float phase) {
		float off = MathHelper.PI * 2 / 3;
		int r = (int) (0xff * MathHelper.clamp(MathHelper.sin(phase), 0, 1));
		int g = (int) (0xff * MathHelper.clamp(MathHelper.sin(phase + off), 0, 1));
		int b = (int) (0xff * MathHelper.clamp(MathHelper.sin(phase + off + off), 0, 1));
		return (r << 16) | (g << 8) | b;
	}
}
