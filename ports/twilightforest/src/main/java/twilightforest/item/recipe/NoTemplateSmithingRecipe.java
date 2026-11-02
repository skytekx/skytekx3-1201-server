package twilightforest.item.recipe;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.SmithingRecipe;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeHooks;
import twilightforest.init.TFRecipes;

import java.util.stream.Stream;

/**
 * A smithing recipe with an empty template slot that copies the base item and merges {@link #additionalData} into
 * its tag. TF 4.8 attaches data components instead; on 1.20.1 the payload is a stack NBT compound, serialized in
 * the recipe JSON as an SNBT string like Forge's own NBT ingredients so tag types survive the round trip.
 */
public class NoTemplateSmithingRecipe implements SmithingRecipe {

	private final ResourceLocation id;
	private final Ingredient base;
	private final Ingredient addition;
	private final CompoundTag additionalData;

	public NoTemplateSmithingRecipe(ResourceLocation id, Ingredient base, Ingredient addition, CompoundTag additionalData) {
		this.id = id;
		this.base = base;
		this.addition = addition;
		this.additionalData = additionalData;
	}

	/**
	 * Used to check if a recipe matches current crafting inventory
	 */
	@Override
	public boolean matches(Container container, Level level) {
		if (!container.getItem(0).isEmpty() || !this.base.test(container.getItem(1)) || !this.addition.test(container.getItem(2))) return false;
		CompoundTag tag = container.getItem(1).getTag();
		if (tag != null) {
			// don't apply the same data twice (#2008)
			for (String key : this.additionalData.getAllKeys()) {
				if (!key.equals(ItemStack.TAG_DAMAGE) && tag.contains(key)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public ItemStack assemble(Container container, RegistryAccess access) {
		// copy() also copies the tag, so the merge never writes into the input stack
		return this.applyData(container.getItem(1).copy());
	}

	@Override
	public ItemStack getResultItem(RegistryAccess access) {
		return this.applyData(new ItemStack(Items.IRON_CHESTPLATE));
	}

	private ItemStack applyData(ItemStack stack) {
		if (!this.additionalData.isEmpty()) {
			stack.getOrCreateTag().merge(this.additionalData.copy());
		}
		return stack;
	}

	@Override
	public boolean isTemplateIngredient(ItemStack stack) {
		return stack.isEmpty();
	}

	@Override
	public boolean isBaseIngredient(ItemStack stack) {
		return this.base.test(stack);
	}

	@Override
	public boolean isAdditionIngredient(ItemStack stack) {
		return this.addition.test(stack);
	}

	public Ingredient getBase() {
		return this.base;
	}

	public Ingredient getAddition() {
		return this.addition;
	}

	public CompoundTag getAdditionalData() {
		return this.additionalData;
	}

	@Override
	public ResourceLocation getId() {
		return this.id;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.NO_TEMPLATE_SMITHING_SERIALIZER.get();
	}

	@Override
	public boolean isIncomplete() {
		return Stream.of(this.base, this.addition).anyMatch(ForgeHooks::hasNoElements);
	}

	public static class Serializer implements RecipeSerializer<NoTemplateSmithingRecipe> {

		@Override
		public NoTemplateSmithingRecipe fromJson(ResourceLocation id, JsonObject json) {
			Ingredient base = Ingredient.fromJson(GsonHelper.getNonNull(json, "base"));
			Ingredient addition = Ingredient.fromJson(GsonHelper.getNonNull(json, "addition"));
			return new NoTemplateSmithingRecipe(id, base, addition, parseData(json));
		}

		private static CompoundTag parseData(JsonObject json) {
			if (!json.has("additional_data")) return new CompoundTag();
			JsonElement element = json.get("additional_data");
			try {
				// same rules as Forge's CraftingHelper for "nbt": an SNBT string, or a JSON object read as SNBT
				return TagParser.parseTag(element.isJsonObject() ? element.toString() : GsonHelper.convertToString(element, "additional_data"));
			} catch (CommandSyntaxException e) {
				throw new JsonSyntaxException("Invalid additional_data: " + e.getMessage());
			}
		}

		@Override
		public NoTemplateSmithingRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
			Ingredient base = Ingredient.fromNetwork(buf);
			Ingredient addition = Ingredient.fromNetwork(buf);
			CompoundTag data = buf.readNbt();
			return new NoTemplateSmithingRecipe(id, base, addition, data == null ? new CompoundTag() : data);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buf, NoTemplateSmithingRecipe recipe) {
			recipe.base.toNetwork(buf);
			recipe.addition.toNetwork(buf);
			buf.writeNbt(recipe.additionalData);
		}
	}
}
