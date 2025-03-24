package symbolics.division.spirit_vector.sfx;

import net.fabricmc.fabric.api.particle.v1.FabricParticleTypes;
import net.minecraft.item.Item;
import net.minecraft.particle.SimpleParticleType;
import net.minecraft.util.Identifier;
import symbolics.division.spirit_vector.item.SFXPackItem;

public record SimpleSFX(Identifier id, int color, Identifier wingsTexture) implements SFXPack<SimpleParticleType> {
	public static final SimpleParticleType PARTICLE_TYPE = FabricParticleTypes.simple();

	public SimpleSFX(Identifier id) {
		this(id, 0xffffff);
	}

	public SimpleSFX(Identifier id, int color) {
		this(id, color, "textures/wing/");
	}

	public SimpleSFX(Identifier id, int color, String wingTexturePath) {
		this(id, color, Identifier.of(id.getNamespace(), wingTexturePath + id.getPath() + ".png"));
	}

	@Override
	public Identifier wingsTexture() {
		return wingsTexture;
	}

	@Override
	public SimpleParticleType particleType() {
		return PARTICLE_TYPE;
	}

	@Override
	public SimpleParticleType particleEffect() {
		return PARTICLE_TYPE;
	}

	@Override
	public int color() {
		return this.color;
	}

	public Item asItem() {
		return new SFXPackItem(SFXRegistry.INSTANCE.getEntry(this));
	}
}
