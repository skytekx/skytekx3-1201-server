package twilightforest.events;

import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.inventory.AnvilMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.GrindstoneEvent;
import net.minecraftforge.event.ItemAttributeModifierEvent;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.TFAdvancements;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.item.travellers_gear.TravellersGearLogic;
import twilightforest.item.travellers_gear.modifiers.InsertableTravellersModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.List;

/**
 * Common events of the Traveller's Gear core and its modifier framework. Backport of the core part of 4.8's
 * TravellersGearEvents (the wing state update, the broken-state attribute switch, the anvil combining block, stripping
 * modifiers at the grindstone, the add_modifier criterion and the modifier cache reload); the listeners that make the
 * single modifiers work come with the travellers-* modifier features.
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
