package twilightforest.compat.emi.recipes;

import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.recipe.EmiGrindstoneRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import twilightforest.TwilightForestMod;
import twilightforest.compat.DefaultModifiedTravellersGearGetter;

import java.util.List;
import java.util.Objects;
import java.util.Random;

/**
 * Shows that the grindstone strips the modifiers off a piece of Traveller's Gear. Backport of 4.8's
 * EmiTravellersGearGrindstoneRecipe.
 */
public class EmiTravellersGearGrindstoneRecipe extends EmiGrindstoneRecipe {
	protected final ItemStack modifiedStack;

	public EmiTravellersGearGrindstoneRecipe(ItemStack modifiedStack) {
		super(modifiedStack.getItem(), TwilightForestMod.prefix("/" + modifiedStack.getItem().getDescriptionId()));
		this.modifiedStack = modifiedStack;
	}

	@Override
	protected EmiStack getItem(Random random, int item) {
		List<ItemStack> items = List.of(this.modifiedStack, ItemStack.EMPTY, DefaultModifiedTravellersGearGetter.getDemodifiedStack(this.modifiedStack));
		return EmiStack.of(items.get(item));
	}

	public static void register(EmiRegistry registry) {
		DefaultModifiedTravellersGearGetter.getDefaultModifiedTravellersGear(Objects.requireNonNull(Minecraft.getInstance().level).registryAccess()).forEach(stack ->
			registry.addRecipe(new EmiTravellersGearGrindstoneRecipe(stack))
		);
	}
}
