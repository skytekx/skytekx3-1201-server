package twilightforest.events;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TwilightForestMod;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.item.travellers_gear.TravellersGearLogic;

/**
 * Common events of the Traveller's Gear core. Backport of the core part of 4.8's TravellersGearEvents (the wing state
 * update, the broken-state attribute switch and the anvil combining block); the modifier listeners come with the
 * travellers-* modifier features.
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
