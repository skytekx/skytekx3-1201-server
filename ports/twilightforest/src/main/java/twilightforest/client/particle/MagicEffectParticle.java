package twilightforest.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.client.particle.data.ColorParticleData;

/**
 * The glowing, coloured spell particle of scepters and the Lich (TF 4.8 twilightforest:magic_effect). 4.8 extends
 * SpellParticle, whose constructor is package-private on 1.20.1, so this is a [VanillaCopy] of 1.21's SpellParticle that
 * renders fullbright.
 */
@OnlyIn(Dist.CLIENT)
public class MagicEffectParticle extends TextureSheetParticle {
	private static final RandomSource RANDOM = RandomSource.create();
	private final SpriteSet sprites;
	private float originalAlpha = 1.0F;

	protected MagicEffectParticle(ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed, SpriteSet sprites) {
		super(level, x, y, z, 0.5D - RANDOM.nextDouble(), ySpeed, 0.5D - RANDOM.nextDouble());
		this.friction = 0.96F;
		this.gravity = -0.1F;
		this.speedUpWhenYMotionIsBlocked = true;
		this.sprites = sprites;
		this.yd *= 0.2F;
		if (xSpeed == 0.0D && zSpeed == 0.0D) {
			this.xd *= 0.1F;
			this.zd *= 0.1F;
		}

		this.quadSize *= 0.75F;
		this.lifetime = (int) (8.0D / (Math.random() * 0.8D + 0.2D));
		this.hasPhysics = false;
		this.setSpriteFromAge(sprites);
		if (this.isCloseToScopingPlayer()) {
			this.setAlpha(0.0F);
		}
	}

	@Override
	public ParticleRenderType getRenderType() {
		return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
	}

	@Override
	public void tick() {
		super.tick();
		this.setSpriteFromAge(this.sprites);
		if (this.isCloseToScopingPlayer()) {
			this.alpha = 0.0F;
		} else {
			this.alpha = Mth.lerp(0.05F, this.alpha, this.originalAlpha);
		}
	}

	@Override
	protected void setAlpha(float alpha) {
		super.setAlpha(alpha);
		this.originalAlpha = alpha;
	}

	@Override
	public int getLightColor(float partialTick) {
		return 0xF000F0;
	}

	private boolean isCloseToScopingPlayer() {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer player = minecraft.player;
		return player != null && player.getEyePosition().distanceToSqr(this.x, this.y, this.z) <= 9.0D && minecraft.options.getCameraType().isFirstPerson() && player.isScoping();
	}

	@OnlyIn(Dist.CLIENT)
	public static class Factory implements ParticleProvider<ColorParticleData> {
		private final SpriteSet sprite;

		public Factory(SpriteSet sprite) {
			this.sprite = sprite;
		}

		@Override
		public Particle createParticle(ColorParticleData type, ClientLevel level, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
			MagicEffectParticle particle = new MagicEffectParticle(level, x, y, z, xSpeed, ySpeed, zSpeed, this.sprite);
			particle.setColor(type.getRed(), type.getGreen(), type.getBlue());
			particle.setAlpha(type.getAlpha());
			return particle;
		}
	}
}
