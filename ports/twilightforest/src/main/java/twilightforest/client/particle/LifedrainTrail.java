package twilightforest.client.particle;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import twilightforest.item.LifedrainScepterItem;

/**
 * Client half of the Lifedrain Scepter's drain effect, triggered by {@link twilightforest.network.LifedrainParticlePacket}.
 */
@OnlyIn(Dist.CLIENT)
public final class LifedrainTrail {

	private LifedrainTrail() {
	}

	public static void makeRedMagicTrail(Level level, LivingEntity source, Vec3 target) {
		// make particle trail
		Vec3 handPos = getPlayerHandPos(source, Minecraft.getInstance().getPartialTick());
		double distance = handPos.distanceTo(target);

		for (double i = 0; i <= distance * 3; i++) {
			Vec3 particlePos = handPos.subtract(target).scale(i / (distance * 3));
			particlePos = handPos.subtract(particlePos);
			float r = 1.0F;
			float g = 0.5F;
			float b = 0.5F;
			// in 1.20.1 the entity effect particle takes its colour from the speed arguments
			level.addParticle(ParticleTypes.ENTITY_EFFECT, particlePos.x(), particlePos.y(), particlePos.z(), r, g, b);
		}
	}

	/**
	 * Slightly reformatted vanilla copy of
	 * {@link net.minecraft.client.renderer.entity.FishingHookRenderer}#getPlayerHandPos, keyed on the scepter instead of the rod.
	 */
	private static Vec3 getPlayerHandPos(LivingEntity living, float partialTicks) {
		float armSwing = Mth.sin(Mth.sqrt(living.getAttackAnim(partialTicks)) * (float) Math.PI);
		int hand = living.getMainArm() == HumanoidArm.RIGHT ? 1 : -1;
		if (!(living.getMainHandItem().getItem() instanceof LifedrainScepterItem)) hand = -hand;

		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.options.getCameraType().isFirstPerson() && living == minecraft.player) {
			Vec3 vec3 = minecraft.getEntityRenderDispatcher()
					.camera
					.getNearPlane()
					.getPointOnPlane((float) hand * 0.525F, -0.1F)
					.scale(960.0D / (double) minecraft.options.fov().get())
					.yRot(armSwing * 0.5F)
					.xRot(-armSwing * 0.7F);
			return living.getEyePosition(partialTicks).add(vec3);
		} else {
			float yRot = Mth.lerp(partialTicks, living.yBodyRotO, living.yBodyRot) * (float) (Math.PI / 180.0);
			double sin = Mth.sin(yRot);
			double cos = Mth.cos(yRot);
			float scale = living.getScale();
			double offset = (double) hand * 0.35 * (double) scale;
			double factor = 0.8 * (double) scale;
			float crouch = living.isCrouching() ? -0.1875F : 0.0F;
			return living.getEyePosition(partialTicks).add(-cos * offset - sin * factor, (double) crouch - 0.45 * (double) scale, -sin * offset + cos * factor);
		}
	}
}
