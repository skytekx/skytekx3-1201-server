package twilightforest.compat;

import net.minecraft.core.RegistryAccess;
import net.minecraft.world.item.ItemStack;
import twilightforest.init.TFItems;
import twilightforest.init.custom.TravellersModifiersManager;

import java.util.List;

/**
 * Example pieces of Traveller's Gear with three modifiers each, for the recipe viewers' grindstone displays. Backport
 * of 4.8's compat/common/DefaultModifiedTravellersGearGetter.
 */
public final class DefaultModifiedTravellersGearGetter {

	private DefaultModifiedTravellersGearGetter() {
	}

	public static List<ItemStack> getDefaultModifiedTravellersGear(RegistryAccess registries) {
		return List.of(
			getDefaultGoggles(registries),
			getDefaultVest(registries),
			getDefaultWings(registries),
			getDefaultBoots(registries)
		);
	}

	public static ItemStack getDemodifiedStack(ItemStack modifiedStack) {
		return new ItemStack(modifiedStack.getItem(), modifiedStack.getCount());
	}

	private static ItemStack getDefaultGoggles(RegistryAccess registries) {
		ItemStack goggles = new ItemStack(TFItems.TRAVELLERS_GOGGLES.get());
		TravellersModifiersManager.addModifier(registries, goggles, TravellersModifiersManager.AUTO_REPAIR_MODIFIER);
		TravellersModifiersManager.addModifier(registries, goggles, TravellersModifiersManager.RED_THREAD_VISION_MODIFIER);
		TravellersModifiersManager.addModifier(registries, goggles, TravellersModifiersManager.ALL_NIGHT_GOGGLES_MODIFIER);
		return goggles;
	}

	private static ItemStack getDefaultVest(RegistryAccess registries) {
		ItemStack vest = new ItemStack(TFItems.TRAVELLERS_VEST.get());
		TravellersModifiersManager.addModifier(registries, vest, TravellersModifiersManager.AUTO_REPAIR_MODIFIER);
		TravellersModifiersManager.addModifier(registries, vest, TravellersModifiersManager.PERFECT_DODGE_MODIFIER);
		TravellersModifiersManager.addModifier(registries, vest, TravellersModifiersManager.STEALTH_MODIFIER);
		return vest;
	}

	private static ItemStack getDefaultWings(RegistryAccess registries) {
		ItemStack wings = new ItemStack(TFItems.TRAVELLERS_WINGS.get());
		TravellersModifiersManager.addModifier(registries, wings, TravellersModifiersManager.AUTO_REPAIR_MODIFIER);
		TravellersModifiersManager.addModifier(registries, wings, TravellersModifiersManager.AGILE_RANGER_MODIFIER);
		TravellersModifiersManager.addModifier(registries, wings, TravellersModifiersManager.SIDESTEP_MODIFIER);
		return wings;
	}

	private static ItemStack getDefaultBoots(RegistryAccess registries) {
		ItemStack boots = new ItemStack(TFItems.TRAVELLERS_BOOTS.get());
		TravellersModifiersManager.addModifier(registries, boots, TravellersModifiersManager.AUTO_REPAIR_MODIFIER);
		TravellersModifiersManager.addModifier(registries, boots, TravellersModifiersManager.STRAIGHT_AHEAD_MODIFIER);
		TravellersModifiersManager.addModifier(registries, boots, TravellersModifiersManager.WATER_WALK_MODIFIER);
		return boots;
	}
}
