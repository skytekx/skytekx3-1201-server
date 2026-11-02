package twilightforest.asmhooks;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.fluids.FluidType;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersGearLogic;

import java.util.function.BiPredicate;

/**
 * Entity hooks of the Traveller's Boots modifiers, called from {@code META-INF/asm/travellers_boots.js}. Backport of
 * the Water Walk and Unrestrained part of 4.8's EntityHooks; they only take common classes, so loading this class never
 * pulls client code onto a dedicated server.
 */
@SuppressWarnings("unused")
public final class EntityHooks {

	private EntityHooks() {
	}

	/**
	 * Water Walk: an entity whose boots carry it stands on water it is at most
	 * {@link TravellersGearLogic#WATER_WALKING_MAX_SUBMERGED_HEIGHT} deep in, unless it sneaks, and splashes while it walks.
	 * <p>
	 * Injection Point (before every return, 4.8 WaterWalkTransformer):<br/>
	 * {@code net.minecraft.world.entity.LivingEntity#canStandOnFluid(FluidState)}
	 */
	public static boolean processWaterWalking(boolean o, LivingEntity livingEntity, FluidState fluidState) {
		if (!fluidState.is(FluidTags.WATER))
			return o;

		if (!TravellersModifiersManager.isModifierActive(livingEntity, TravellersModifiersManager.WATER_WALK_MODIFIER))
			return o;

		boolean isWaterWalking = TravellersGearLogic.isBelowMaxWaterWalkingSubmergedHeight(livingEntity) && !livingEntity.isShiftKeyDown();
		if (livingEntity.getFluidTypeHeight(ForgeMod.WATER_TYPE.get()) > 0 && isWaterWalking && livingEntity.level().getGameTime() % 3 == 1)
			TravellersGearLogic.waterWalkingSplashEffect(livingEntity);
		return isWaterWalking;
	}

	/**
	 * Unrestrained (with Water Walk): a player standing on water does not count as in water for sprinting.
	 * <p>
	 * Injection Point (after every {@code isInWater()} call, 4.8 WaterSprintTransformer):<br/>
	 * {@code net.minecraft.client.player.LocalPlayer#aiStep()}
	 */
	public static boolean unrestrainedSprintingInWater(boolean isInWater, LivingEntity livingEntity) {
		if (!TravellersModifiersManager.isModifierActive(livingEntity, TravellersModifiersManager.UNRESTRAINED_MODIFIER))
			return isInWater;
		return !livingEntity.canStandOnFluid(livingEntity.level().getFluidState(livingEntity.blockPosition())) && isInWater;
	}

	/**
	 * Injection Point (before every Forge {@code isInFluidType(BiPredicate)} call, 4.8 WaterSprintTransformer):<br/>
	 * {@code net.minecraft.client.player.LocalPlayer#aiStep()}
	 */
	public static BiPredicate<FluidType, Double> unrestrainedSwimPredicate(BiPredicate<FluidType, Double> o, LivingEntity livingEntity) {
		return (fluidType, height) -> {
			boolean oResult = o.test(fluidType, height);
			if (fluidType != ForgeMod.WATER_TYPE.get())
				return oResult;
			return unrestrainedSprintingInWater(oResult, livingEntity);
		};
	}

	/**
	 * Unrestrained: soul sand, honey and other slowing or sticky blocks keep their effect off the wearer.
	 * <p>
	 * Injection Points (before every return, 4.8 UnrestrainedBlockSpeedAndJumpFactorTransformer):<br/>
	 * {@code net.minecraft.world.entity.Entity#getBlockJumpFactor()}<br/>
	 * {@code net.minecraft.world.entity.Entity#getBlockSpeedFactor()}
	 */
	public static float resetFactorWithUnrestrained(float o, Entity entity) {
		return TravellersModifiersManager.isModifierActive(entity, TravellersModifiersManager.UNRESTRAINED_MODIFIER) ? 1.0F : o;
	}

	/**
	 * Unrestrained: cobwebs, sweet berry bushes and powder snow do not slow the wearer. Entity#move applies the slowdown
	 * set by the previous move, which on a server happens while handling the player's own movement packets, so clearing
	 * it once per tick is not enough to keep the server in step with the client.
	 * <p>
	 * Injection Point (before every read of {@code stuckSpeedMultiplier}, 4.8 ResetStuckUnrestrainedTransformer):<br/>
	 * {@code net.minecraft.world.entity.Entity#move(MoverType, Vec3)}
	 */
	public static Entity resetStuckUnrestrained(Entity entity) {
		if (!(entity instanceof LivingEntity living) || living.stuckSpeedMultiplier.lengthSqr() <= 1.0E-7 || !TravellersModifiersManager.isModifierActive(entity, TravellersModifiersManager.UNRESTRAINED_MODIFIER))
			return entity;
		living.stuckSpeedMultiplier = Vec3.ZERO;

		return entity;
	}
}
