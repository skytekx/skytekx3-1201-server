package twilightforest.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * A small glowing mote that rises from a block a magic tree core just worked on.
 * The x/y/z "speed" arguments carry the red, green and blue tint instead of a velocity.
 */
@OnlyIn(Dist.CLIENT)
public class LogCoreParticle extends RisingParticle {
	LogCoreParticle(ClientLevel level, double x, double y, double z, double red, double green, double blue) {
		super(level, x, y, z, 0.0, 0.02, 0.0);
		this.rCol = (float) red;
		this.gCol = (float) green;
		this.bCol = (float) blue;
		this.alpha = 0;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void move(double x, double y, double z) {
		this.setBoundingBox(this.getBoundingBox().move(x, y, z));
		this.setLocationFromBoundingbox();
	}

	@Override
	public float getQuadSize(float partialTicks) {
		float f = ((float) this.age + partialTicks) / (float) this.lifetime;
		return this.quadSize * (1.0F - f * f * 0.5F);
	}

	@Override
	public int getLightColor(float partialTicks) {
		float f = ((float) this.age + partialTicks) / (float) this.lifetime;
		f = Mth.clamp(f, 0.0F, 1.0F);
		int i = super.getLightColor(partialTicks);
		int j = i & 255;
		int k = i >> 16 & 255;
		j += (int) (f * 15.0F * 16.0F);
		if (j > 240) {
			j = 240;
		}

		return j | k << 16;
	}

	@Override
	public void tick() {
		super.tick();
		float f = (float) this.age / (float) this.lifetime;
		this.alpha = Math.min(f * 1.35F, 1F);
	}

	@OnlyIn(Dist.CLIENT)
	public record Factory(SpriteSet sprite) implements ParticleProvider<SimpleParticleType> {
		@Override
		public Particle createParticle(SimpleParticleType particleType, ClientLevel level, double x, double y, double z, double x2, double y2, double z2) {
			LogCoreParticle logCoreParticle = new LogCoreParticle(level, x, y, z, x2, y2, z2);
			logCoreParticle.pickSprite(this.sprite);
			return logCoreParticle;
		}
	}

	@Override
	public boolean shouldCull() {
		return false;
	}
}
