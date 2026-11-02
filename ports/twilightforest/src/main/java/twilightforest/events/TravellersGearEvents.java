package twilightforest.events;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.TFAdvancements;
import twilightforest.components.entity.SlimySolesAttachment;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.item.travellers_gear.TravellersGearLogic;
import twilightforest.item.travellers_gear.modifiers.InsertableTravellersModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;
import twilightforest.network.GradualGlidePacket;
import twilightforest.network.ParticlePacket;
import twilightforest.network.TFPacketHandler;

import java.util.List;

/**
 * Common events of the Traveller's Gear core, its modifier framework and the modifiers ported so far. Backport of 4.8's
 * TravellersGearEvents (the wing state update, the broken-state attribute switch, the anvil combining block, stripping
 * modifiers at the grindstone, the add_modifier criterion, the modifier cache reload and the Traveller's Vest, Wings and
 * Boots modifiers); the listeners of the other pieces' modifiers come with their travellers-* features.
 * <p>
 * 1.20.1 has no entity tick post event: 4.8's EntityTickEvent.Post work runs at the start of the next tick instead
 * (LivingTickEvent), its PlayerTickEvent.Pre work in the START phase of Forge's PlayerTickEvent.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class TravellersGearEvents {

	@SubscribeEvent
	public static void updateWingState(LivingEvent.LivingTickEvent event) {
		LivingEntity livingEntity = event.getEntity();
		// 4.8 runs the state machine for every living entity; only an entity wearing the wings can show it, and
		// resetWingStateWhenTakenOff keeps the state of the others idle
		if (!livingEntity.level().isClientSide() && TravellersGearLogic.isWearingWings(livingEntity))
			TravellersGearLogic.determineWingState(livingEntity);
	}

	/**
	 * Refills the Double Jump on the ground, in liquids and on climbable blocks, ends a Gradual Glide whose modifier
	 * stopped working and plays the Sidestep recharge sound. Backport of 4.8 TravellersGearEvents#tickMovementModifiers.
	 */
	@SubscribeEvent
	public static void tickMovementModifiers(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.START)
			return;
		Player player = event.player;
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(player);
		if (attachment == null)
			return;

		Boolean hasDoubleJump = null;
		if (!TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.DOUBLE_JUMP_MODIFIER))
			hasDoubleJump = false;
		else if (player.onGround() || TravellersGearLogic.isInLiquid(player) || player.onClimbable())
			hasDoubleJump = true;

		if (hasDoubleJump != null && hasDoubleJump != attachment.hasDoubleJump) {
			attachment.hasDoubleJump = hasDoubleJump;
			attachment.doubleJumpValidator.count = 0;
			attachment.hasDoubleJumpSafeFallDistance = false;
		}
		// Local fix over 4.8: the safe fall distance of a double jump ends with the landing even when the Double Jump
		// modifier was lost mid-air (wings taken off, modifier removed). 4.8 only drops it when hasDoubleJump changes,
		// so it stayed on for every later fall. The landing's fall event has already fired by this tick start.
		if (attachment.hasDoubleJumpSafeFallDistance && (player.onGround() || TravellersGearLogic.isInLiquid(player) || player.onClimbable()))
			attachment.hasDoubleJumpSafeFallDistance = false;

		if (!player.level().isClientSide()) {
			boolean modifierActive = TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER);
			if (!modifierActive && attachment.isGraduallyGliding) {
				attachment.isGraduallyGliding = false;
				TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new GradualGlidePacket(false, player.getUUID()));
			}
		}

		TravellersGearLogic.travellersWingsSidestepCooldownSound(player);
	}

	/**
	 * The per-tick modifiers of every living entity wearing Traveller's Gear. Backport of 4.8
	 * TravellersGearEvents#updateOtherModifiers, which runs after the entity tick; here it runs before the next one.
	 */
	@SubscribeEvent
	public static void updateOtherModifiers(LivingEvent.LivingTickEvent event) {
		LivingEntity livingEntity = event.getEntity();
		TravellersGearLogic.travellersWingsGradualGlide(livingEntity);
		TravellersGearLogic.travellersBootsUnrestrained(livingEntity);
		TravellersGearLogic.travellersBootsSlimySolesBounce(livingEntity);

		if (livingEntity.level().isClientSide())
			return;
		if (livingEntity instanceof ServerPlayer player)
			TravellersGearLogic.updateKnownMovement(player);
		TravellersGearLogic.travellersWingsHighJump(livingEntity);
		TravellersGearLogic.travellersBootsStraightAhead(livingEntity);
	}

	/**
	 * Step-Up stops while the player sneaks, so they can creep along edges. Backport of 4.8
	 * TravellersGearEvents#disableHighStepWhileSneaking; the modifier is the one the boots give in their slot, so taking
	 * them off removes it as usual.
	 */
	@SubscribeEvent
	public static void disableHighStepWhileSneaking(TickEvent.PlayerTickEvent event) {
		if (event.phase != TickEvent.Phase.START)
			return;
		Player player = event.player;
		if (!TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.STEP_UP_ABILITY))
			return;
		AttributeInstance attribute = player.getAttribute(ForgeMod.STEP_HEIGHT_ADDITION.get());
		if (attribute == null)
			return;

		AttributeModifier highStep = TravellersGearLogic.HIGH_STEP_MODIFIER.get();
		boolean shouldHaveHighStepModifier = !player.isCrouching();
		boolean hasHighStepModifier = attribute.getModifier(highStep.getId()) != null;
		if (!shouldHaveHighStepModifier && hasHighStepModifier)
			attribute.removeModifier(highStep.getId());
		if (shouldHaveHighStepModifier && !hasHighStepModifier)
			attribute.addTransientModifier(highStep);
	}

	/**
	 * Slimy Soles: a fall that would hurt bounces the wearer back up instead, unless they sneak. Once bouncing, every
	 * landing bounces until the bounces die down. Backport of 4.8 TravellersGearEvents#reduceSlimySolesFallDamage.
	 */
	@SubscribeEvent
	public static void reduceSlimySolesFallDamage(LivingFallEvent event) {
		LivingEntity livingEntity = event.getEntity();
		ItemStack boots = livingEntity.getItemBySlot(EquipmentSlot.FEET);
		Float coefficient = TravellersGearData.SLIMY_SOLES_COEFFICIENT.get(boots);
		if (livingEntity.isShiftKeyDown() || coefficient == null || !TravellersModifiersManager.isModifierActive(livingEntity, boots, TravellersModifiersManager.SLIMY_SOLES_MODIFIER))
			return;
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(livingEntity);
		if (attachment == null)
			return;
		SlimySolesAttachment slimySolesAttachment = attachment.getSlimySoles();
		if (calculateFallDamage(event) > 0 || slimySolesAttachment.forceBounce) {
			event.setCanceled(true);
			slimySolesAttachment.bounceVelocity = -livingEntity.getDeltaMovement().y() * Math.sqrt(coefficient);
			slimySolesAttachment.doubleJumpBoostVelocity = slimySolesAttachment.bounceVelocity;
			slimySolesAttachment.hasBounced = false;
		}
	}

	// [VanillaCopy] LivingEntity#calculateFallDamage; 1.21 subtracts its safe fall distance attribute, which jump boost raises there
	private static int calculateFallDamage(LivingFallEvent event) {
		LivingEntity livingEntity = event.getEntity();
		if (livingEntity.getType().is(EntityTypeTags.FALL_DAMAGE_IMMUNE))
			return 0;
		MobEffectInstance jumpBoost = livingEntity.getEffect(MobEffects.JUMP);
		float jumpBoostSafeFallDistance = jumpBoost == null ? 0.0F : (float) (jumpBoost.getAmplifier() + 1);
		return Mth.ceil((event.getDistance() - 3.0F - jumpBoostSafeFallDistance) * event.getDamageMultiplier());
	}

	/**
	 * Jumping ends a Slimy Soles bounce. Backport of 4.8 TravellersGearEvents#cancelSlimySolesJump.
	 */
	@SubscribeEvent
	public static void cancelSlimySolesJump(LivingEvent.LivingJumpEvent event) {
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(event.getEntity());
		SlimySolesAttachment slimySolesAttachment = attachment == null ? null : attachment.getSlimySolesIfPresent();
		if (slimySolesAttachment == null)
			return;
		slimySolesAttachment.bounceVelocity = 0;
		slimySolesAttachment.forceBounce = false;
	}

	/**
	 * After a double jump the entity may fall {@link TravellersGearLogic#DOUBLE_JUMP_SAFE_FALL_DISTANCE} blocks further
	 * without damage until it lands. 4.8 adds a modifier to 1.21's safe fall distance attribute; 1.20.1 has none, so the
	 * fall distance shrinks instead, before the other fall listeners (Slimy Soles) see it.
	 */
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void applyDoubleJumpSafeFallDistance(LivingFallEvent event) {
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(event.getEntity());
		if (attachment != null && attachment.hasDoubleJumpSafeFallDistance)
			event.setDistance(Math.max(0.0F, event.getDistance() - TravellersGearLogic.DOUBLE_JUMP_SAFE_FALL_DISTANCE));
	}

	/**
	 * Arrow Magnetism: an arrow shot by the wearer of a vest with the modifier that hits a block within its first ten
	 * seconds goes straight back into the shooter's inventory (players) or vanishes (other shooters), unless it could
	 * not have been picked up.
	 */
	@SubscribeEvent
	public static void magnetizeArrows(ProjectileImpactEvent event) {
		Projectile projectile = event.getProjectile();
		Entity entity = projectile.getOwner();
		if (!(entity instanceof LivingEntity livingEntity) || event.getRayTraceResult().getType() != HitResult.Type.BLOCK || projectile.tickCount >= 200)
			return;

		if (!TravellersModifiersManager.isModifierActive(livingEntity, TravellersModifiersManager.ARROW_MAGNETISM_MODIFIER)
			|| !(projectile instanceof AbstractArrow arrow) || projectile.level().isClientSide())
			return;

		if (!(livingEntity instanceof Player player)) {
			projectile.discard();
			return;
		}
		AbstractArrow.Pickup pickup = arrow.pickup;
		// 1.21 Player#hasInfiniteMaterials and AbstractArrow#getPickupItemStackOrigin; getPickupItem is public through the access transformer
		if (!player.getAbilities().instabuild && pickup == AbstractArrow.Pickup.ALLOWED) {
			ItemHandlerHelper.giveItemToPlayer(player, arrow.getPickupItem());
			player.getInventory().setChanged();
		}
		if (pickup == AbstractArrow.Pickup.ALLOWED || pickup == AbstractArrow.Pickup.CREATIVE_ONLY && player.isCreative())
			projectile.discard();
	}

	/**
	 * Perfect Dodge: a projectile hitting the wearer of a vest with the modifier misses with the modifier's probability
	 * (30% with the stock definition), with a sound and a puff of particles where it would have hit. The client always
	 * lets the projectile pass, the server decides whether it hits.
	 */
	@SubscribeEvent
	public static void performPerfectDodge(ProjectileImpactEvent event) {
		HitResult rayResult = event.getRayTraceResult();
		if (!(rayResult instanceof EntityHitResult entityHitResult) || !(entityHitResult.getEntity() instanceof LivingEntity livingEntity))
			return;
		ItemStack chest = livingEntity.getItemBySlot(EquipmentSlot.CHEST);
		if (!TravellersArmorItem.isTravellersGear(chest) || !TravellersModifiersManager.isModifierActive(livingEntity, chest, TravellersModifiersManager.PERFECT_DODGE_MODIFIER))
			return;
		Float probability = TravellersGearData.PERFECT_DODGE_PROBABILITY.get(chest);
		if (probability == null)
			return;
		Level level = livingEntity.level();
		if (level.isClientSide()) {
			event.setCanceled(true); // always cancel on the client side because the game sends a damage packet when it hits the player
			return;
		}
		if (probability <= level.random.nextFloat())
			return;
		Entity projectile = event.getEntity();
		Vec3 hitPosition = projectile.position().add(projectile.getDeltaMovement());
		level.playSound(null, hitPosition.x(), hitPosition.y(), hitPosition.z(), TFSounds.PERFECT_DODGE.get(), livingEntity.getSoundSource(), 1.5F, livingEntity.getVoicePitch());
		event.setCanceled(true);
		ParticlePacket particlePacket = new ParticlePacket();
		for (int particleNumber = 0; particleNumber < 20; particleNumber++) {
			Vec3 particleVelocity = new Vec3(
				(level.random.nextDouble() - 0.5),
				(level.random.nextDouble() - 0.5),
				(level.random.nextDouble() - 0.5)
			);
			ParticleOptions type = TFParticleType.PERFECT_DODGE.get();
			particlePacket.queueParticle(type, false, hitPosition, particleVelocity);
		}
		TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity), particlePacket);
	}

	@SubscribeEvent
	public static void performStealth(TickEvent.PlayerTickEvent event) {
		if (event.phase == TickEvent.Phase.END && !event.player.level().isClientSide()) {
			TravellersGearLogic.travellersStealth(event.player, player -> player.addEffect(new MobEffectInstance(MobEffects.INVISIBILITY, 2, 0, false, false, false)));
		}
	}

	@SubscribeEvent
	public static void applyVestHaste(LivingEvent.LivingTickEvent event) {
		LivingEntity livingEntity = event.getEntity();
		if (!livingEntity.level().isClientSide())
			TravellersGearLogic.travellersVestHaste(livingEntity);
	}

	@SubscribeEvent
	public static void resetWingStateWhenTakenOff(LivingEquipmentChangeEvent event) {
		if (event.getSlot() == EquipmentSlot.LEGS && !event.getEntity().level().isClientSide()
			&& TravellersArmorItem.hasPart(event.getFrom(), TravellersArmorItem.Part.WINGS) && !TravellersArmorItem.hasPart(event.getTo(), TravellersArmorItem.Part.WINGS))
			TravellersGearLogic.resetWingState(event.getEntity());
	}

	@SubscribeEvent
	public static void syncWingStateToNewTracker(PlayerEvent.StartTracking event) {
		if (event.getEntity() instanceof ServerPlayer player && event.getTarget() instanceof LivingEntity livingEntity && TravellersGearLogic.isWearingWings(livingEntity))
			TravellersGearLogic.syncWingState(livingEntity, player);
	}

	@SubscribeEvent
	public static void syncWingStateAfterDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
		// the client gets a fresh player entity in the new dimension, while the server keeps the old one and its state
		if (event.getEntity() instanceof ServerPlayer player && TravellersGearLogic.isWearingWings(player))
			TravellersGearLogic.syncWingState(player, player);
	}

	/**
	 * A broken piece of Traveller's Gear gives no attribute modifiers (armor, toughness and whatever a modifier adds)
	 * until it is repaired. 4.8 moves the stack's attribute component into STORED_BROKEN_ATTRIBUTES and back; 1.20.1
	 * computes item attribute modifiers on every query, so clearing them while broken is enough and needs no stored copy.
	 */
	@SubscribeEvent
	public static void deactivateBrokenTravellersGear(ItemAttributeModifierEvent event) {
		if (TravellersArmorItem.isTravellersArmorAndBroken(event.getItemStack()))
			event.clearModifiers();
	}

	@SubscribeEvent
	public static void invalidateModifierCache(AddReloadListenerEvent event) {
		event.addListener(TravellersModifiersManager.CacheInvalidationReloadListener.INSTANCE);
	}

	/**
	 * A single piece of Traveller's Gear in the grindstone loses every inserted modifier (the built-in abilities stay);
	 * the grindstone refuses gear without one, and two pieces at once. Backport of 4.8
	 * TravellersGearEvents#removeModifiersFromTravellersGear. 4.8 ignores whatever sits in the other slot and the
	 * grindstone then consumes it with the gear; here the other slot has to be empty.
	 */
	@SubscribeEvent
	public static void removeModifiersFromTravellersGear(GrindstoneEvent.OnPlaceItem event) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null)
			return;
		ItemStack top = event.getTopItem();
		ItemStack bottom = event.getBottomItem();
		boolean topIsGear = TravellersArmorItem.isTravellersGear(top);
		boolean bottomIsGear = TravellersArmorItem.isTravellersGear(bottom);
		if (!topIsGear && !bottomIsGear)
			return; // Delegate to vanilla logic
		if (topIsGear && bottomIsGear || !(topIsGear ? bottom : top).isEmpty()) {
			event.setCanceled(true);
			return;
		}

		RegistryAccess access = server.registryAccess();
		ItemStack inputStack = topIsGear ? top : bottom;
		List<Holder.Reference<TravellersModifier>> modifiers = TravellersModifiersManager.findAllInsertableModifiers(access, inputStack);
		if (modifiers.isEmpty()) {
			event.setCanceled(true);
			return;
		}

		ItemStack unmodifiedStack = inputStack.copy();
		modifiers.forEach(modifier -> ((InsertableTravellersModifier) modifier.value()).removeModifier(unmodifiedStack));
		event.setOutput(unmodifiedStack);
	}

	/**
	 * Fires twilightforest:add_modifier for every modifier the crafted piece of gear has that the piece in the grid did
	 * not. Backport of 4.8 TravellersGearEvents#fireCraftingModifierTrigger.
	 */
	@SubscribeEvent
	public static void fireCraftingModifierTrigger(PlayerEvent.ItemCraftedEvent event) {
		if (event.getEntity() instanceof ServerPlayer player && TravellersArmorItem.isTravellersGear(event.getCrafting())) {
			ItemStack compareStack = ItemStack.EMPTY;
			for (int i = 0; i < event.getInventory().getContainerSize(); i++) {
				if (event.getInventory().getItem(i).is(event.getCrafting().getItem())) compareStack = event.getInventory().getItem(i);
			}

			if (!compareStack.isEmpty()) {
				List<Holder.Reference<TravellersModifier>> oldMods = TravellersModifiersManager.findAllInsertableModifiers(player, compareStack);
				TravellersModifiersManager.findAllInsertableModifiers(player, event.getCrafting()).stream()
					.filter(modifier -> !oldMods.contains(modifier))
					.forEach(modifier -> TFAdvancements.ADD_MODIFIER.trigger(player, modifier.key().location()));
			}
		}
	}

	/**
	 * Two pieces of Traveller's Gear cannot be combined in an anvil; they repair only with Tanned Leather. isRepairable
	 * only stops the repair crafting recipe and the grindstone, vanilla's anvil still merges two damaged copies of the
	 * same item.
	 */
	@SubscribeEvent
	public static void cancelCombiningTravellersGear(AnvilUpdateEvent event) {
		if (TravellersArmorItem.isTravellersGear(event.getLeft()) && TravellersArmorItem.isTravellersGear(event.getRight())) {
			event.setCanceled(true);
			// Forge 47 returns from AnvilMenu#createResult on cancel without clearing the result slot. A stale renamed copy could otherwise be taken and would consume the right-hand piece.
			if (event.getPlayer() != null && event.getPlayer().containerMenu instanceof AnvilMenu menu)
				menu.getSlot(menu.getResultSlot()).set(ItemStack.EMPTY);
		}
	}
}
