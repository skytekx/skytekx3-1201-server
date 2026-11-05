package com.mrh0.createaddition.recipe.charging;

import com.google.gson.JsonObject;
import com.mrh0.createaddition.CreateAddition;
import com.mrh0.createaddition.compat.jei.ChargingAssemblySubCategory;
import com.mrh0.createaddition.index.CABlocks;
import com.mrh0.createaddition.index.CARecipes;
import com.mrh0.createaddition.util.Util;
import com.simibubi.create.compat.jei.category.sequencedAssembly.SequencedAssemblySubCategory;
import com.simibubi.create.content.processing.recipe.ProcessingRecipe;
import com.simibubi.create.content.processing.recipe.ProcessingRecipeBuilder.ProcessingRecipeParams;
import com.simibubi.create.content.processing.sequenced.IAssemblyRecipe;
import com.simibubi.create.foundation.recipe.IRecipeTypeInfo;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.items.wrapper.RecipeWrapper;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A Create processing recipe, so that Create's Sequenced Assembly accepts it as a step
 * (upstream 715895e and b070742). {@link ChargingRecipeSerializer} reads the legacy keys
 * input, result and maxChargeRate as well as Create's ingredients, results and max_charge_rate.
 */
public class ChargingRecipe extends ProcessingRecipe<RecipeWrapper> implements IAssemblyRecipe {

	public static final IRecipeTypeInfo TYPE_INFO = new IRecipeTypeInfo() {
		@Override
		public ResourceLocation getId() {
			return CARecipes.CHARGING.getId();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends RecipeSerializer<?>> T getSerializer() {
			return (T) CARecipes.CHARGING.get();
		}

		@SuppressWarnings("unchecked")
		@Override
		public <T extends RecipeType<?>> T getType() {
			return (T) CARecipes.CHARGING_TYPE.get();
		}
	};

	static final String ENERGY_KEY = "energy";
	static final String MAX_CHARGE_RATE_KEY = "max_charge_rate";
	static final String LEGACY_MAX_CHARGE_RATE_KEY = "maxChargeRate";

	public int energy;
	// Without a max charge rate only the Tesla Coil's own rate limits a recipe, as in the base.
	public int maxChargeRate = Integer.MAX_VALUE;

	public ChargingRecipe(ProcessingRecipeParams params) {
		super(TYPE_INFO, params);
	}

	@Override
	public boolean matches(RecipeWrapper wrapper, Level world) {
		if(wrapper == null || ingredients.isEmpty())
			return false;
		return ingredients.get(0).test(wrapper.getItem(0));
	}

	@Override
	protected int getMaxInputCount() {
		return 1;
	}

	@Override
	protected int getMaxOutputCount() {
		return 1;
	}

	public Ingredient getIngredient() {
		return ingredients.isEmpty() ? Ingredient.EMPTY : ingredients.get(0);
	}

	public ItemStack getResultItem() {
		return results.isEmpty() ? ItemStack.EMPTY : results.get(0).getStack();
	}

	public int getEnergy() {
		return energy;
	}

	public int getMaxChargeRate() {
		return maxChargeRate;
	}

	@Override
	public void readAdditional(JsonObject json) {
		energy = GsonHelper.getAsInt(json, ENERGY_KEY);
		if(json.has(MAX_CHARGE_RATE_KEY))
			maxChargeRate = GsonHelper.getAsInt(json, MAX_CHARGE_RATE_KEY);
		else
			maxChargeRate = GsonHelper.getAsInt(json, LEGACY_MAX_CHARGE_RATE_KEY, Integer.MAX_VALUE);
	}

	@Override
	public void writeAdditional(JsonObject json) {
		json.addProperty(ENERGY_KEY, energy);
		json.addProperty(MAX_CHARGE_RATE_KEY, maxChargeRate);
	}

	@Override
	public void readAdditional(FriendlyByteBuf buffer) {
		energy = buffer.readInt();
		maxChargeRate = buffer.readInt();
	}

	@Override
	public void writeAdditional(FriendlyByteBuf buffer) {
		buffer.writeInt(energy);
		buffer.writeInt(maxChargeRate);
	}

	@Override
	@OnlyIn(Dist.CLIENT)
	public Component getDescriptionForAssembly() {
		return Component.translatable(CreateAddition.MODID + ".recipe.charging.sequence", Util.format(energy));
	}

	@Override
	public void addRequiredMachines(Set<ItemLike> list) {
		list.add(CABlocks.TESLA_COIL.get());
	}

	@Override
	public void addAssemblyIngredients(List<Ingredient> list) {}

	// A lazy supplier, so that the JEI class only loads when JEI asks for it on the client.
	@Override
	public Supplier<Supplier<SequencedAssemblySubCategory>> getJEISubCategory() {
		return () -> ChargingAssemblySubCategory::new;
	}
}
