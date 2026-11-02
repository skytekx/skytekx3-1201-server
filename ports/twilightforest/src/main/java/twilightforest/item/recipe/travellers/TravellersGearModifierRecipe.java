package twilightforest.item.recipe.travellers;

import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.TravellersModifiable;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.Arrays;
import java.util.List;
import java.util.stream.StreamSupport;

/**
 * Adds a Traveller's modifier to the piece of gear in the grid, keeping everything else the piece has. The recipe
 * matches while the piece has a free modifier slot and lacks the modifier; the result is computed from the modifier
 * definition, so a data pack that deletes the modifier also disables its recipes. Backport of 4.8's
 * TravellersGearModifierRecipe (485d7e0a7, 51781e15f, 3f51e8b9a).
 */
public abstract class TravellersGearModifierRecipe extends CustomRecipe {
	protected final ResourceKey<TravellersModifier> travellersModifierKey;

	public TravellersGearModifierRecipe(ResourceLocation id, ResourceKey<TravellersModifier> travellersModifier) {
		super(id, CraftingBookCategory.EQUIPMENT);
		this.travellersModifierKey = travellersModifier;
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		ItemStack stack = getModifiableArmor(input.getItems());
		if (stack == null)
			return false;
		int slots = 0;
		if (stack.getItem() instanceof TravellersModifiable travellersModifiableItem)
			slots = travellersModifiableItem.getModifierSlots();
		RegistryAccess registries = level.registryAccess();
		return TravellersModifiersManager.countInsertableModifiers(registries, stack) < slots
			&& !TravellersModifiersManager.hasTravellersModifier(registries, stack, this.travellersModifierKey)
			&& TravellersModifiersManager.getModifierDataComponentProviders(registries, toIngredients(input), this.travellersModifierKey) <= 1;
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess registries) {
		ItemStack travellerArmorStack = getModifiableArmor(input.getItems());
		if (travellerArmorStack == null)
			return ItemStack.EMPTY;  // Should never happen

		ItemStack stack = travellerArmorStack.copyWithCount(1);
		return this.applyModifier(registries, stack, toIngredients(input));
	}

	public ItemStack applyModifier(RegistryAccess registries, ItemStack stack, List<Ingredient> inputs) {
		if (TravellersModifiersManager.transferModifier(registries, stack, inputs, this.travellersModifierKey))
			return stack;
		boolean modifierAdded = TravellersModifiersManager.addModifier(registries, stack, this.travellersModifierKey);
		return modifierAdded ? stack : ItemStack.EMPTY;
	}

	public abstract boolean isShapeless();

	public abstract int getWidth();

	public abstract int getHeight();

	/**
	 * The crafting grid as 4.8 hands it to the modifiers: one ingredient per slot, holding the actual stack.
	 */
	protected static List<Ingredient> toIngredients(CraftingContainer input) {
		return input.getItems().stream().map(stack -> Ingredient.of(stack)).toList();
	}

	protected static @Nullable ItemStack getModifiableArmor(Iterable<ItemStack> items) {
		return StreamSupport.stream(items.spliterator(), false)
			.filter(stack -> stack.getItem() instanceof TravellersModifiable modifiable && modifiable.getModifierSlots() > 0).findFirst().orElse(null);
	}

	public static ItemStack getModifiableArmorFromIngredients(Iterable<Ingredient> ingredients) {
		return StreamSupport.stream(ingredients.spliterator(), false)
			.flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
			.filter(stack -> stack.getItem() instanceof TravellersModifiable).findFirst().orElseThrow();
	}

	/**
	 * 4.8 TravellersGearModifierRecipe#getId: {@code add_modifier_to_travellers_gear/<piece>/<modifier>_modifier}, with a
	 * {@code _rotated} suffix for the second Auto-Repair layout. 1.20.1 recipes take their id from the file, so datagen
	 * names the files with it.
	 */
	public static ResourceLocation computeId(ResourceKey<TravellersModifier> travellersModifierKey, Iterable<Ingredient> ingredients, boolean isRotated) {
		return travellersModifierKey.location()
			.withPrefix(StringUtils.substringAfterLast(getModifiableArmorFromIngredients(ingredients).getDescriptionId(), '.') + "/")
			.withPrefix("add_modifier_to_travellers_gear/")
			.withSuffix("_modifier")
			.withSuffix(isRotated ? "_rotated" : "");
	}

	public ResourceKey<TravellersModifier> getTravellersModifierKey() {
		return this.travellersModifierKey;
	}
}
