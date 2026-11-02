package twilightforest.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * The short-lived, sinking angry puff over a Lich in its melee phase and over a powered Lich trophy (backported from TF
 * 4.8 ae3bfbb62, c263bc82e). 4.8 extends HeartParticle, whose constructor is package-private in 1.20.1, so its setup is
 * copied here.
 */
@OnlyIn(Dist.CLIENT)
public class AngryLichParticle extends TextureSheetParticle {
	protected AngryLichParticle(ClientLevel level, double x, double y, double z) {
		super(level, x, y, z, 0.0D, 0.0D, 0.0D);
		// [VanillaCopy] HeartParticle constructor
		this.speedUpWhenYMotionIsBlocked = true;
		this.friction = 0.86F;
		this.xd *= 0.01F;
		this.yd *= 0.01F;
		this.zd *= 0.01F;
		this.yd += 0.1D;
		this.quadSize *= 1.5F;
		this.lifetime = 16;
		this.hasPhysics = false;

		this.lifetime = 10;

		this.yd -= 0.05D;
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
	}

	@Override
	public float getQuadSize(float partialTicks) {
		return this.quadSize * Mth.clamp(((float) this.age + partialTicks) / (float) this.lifetime * 32.0F, 0.0F, 1.0F);
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements ParticleProvider<SimpleParticleType> {
		private final SpriteSet sprite;

		public Factory(SpriteSet sprites) {
			this.sprite = sprites;
		}

		@Override
		public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			AngryLichParticle lichParticle = new AngryLichParticle(level, x, y, z);
			lichParticle.pickSprite(this.sprite);
			lichParticle.setColor(1.0F, 1.0F, 1.0F);
			lichParticle.scale(0.75F);
			return lichParticle;
		}
	}
}
