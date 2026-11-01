package twilightforest.item.recipe;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingBookCategory;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.RecipeMatcher;
import twilightforest.init.TFRecipes;

import java.util.ArrayList;
import java.util.List;

/**
 * Recharges a scepter by a fixed number of charges. A recipe with one repair ingredient takes any number of it, one
 * {@link #getRepairDurability()} per item (but no more items than the scepter still needs); a recipe with several takes
 * exactly one set of them.
 */
public class ScepterRepairRecipe extends CustomRecipe {

	private final Item scepter;
	private final NonNullList<Ingredient> repairItems;
	private final int durability;

	public ScepterRepairRecipe(ResourceLocation id, Item scepter, NonNullList<Ingredient> repairItems, int repairDurability, CraftingBookCategory category) {
		super(id, category);
		this.scepter = scepter;
		this.repairItems = repairItems;
		this.durability = repairDurability;
	}

	@Override
	public boolean matches(CraftingContainer input, Level level) {
		ItemStack scepter = null;
		if (this.repairItems.size() == 1) {
			int ingredients = 0;
			for (int i = 0; i < input.getContainerSize(); ++i) {
				ItemStack stackInQuestion = input.getItem(i);
				if (!stackInQuestion.isEmpty()) {
					if (stackInQuestion.is(this.scepter) && stackInQuestion.getDamageValue() > 0) {
						if (scepter != null) return false;
						scepter = stackInQuestion;
					} else if (this.repairItems.get(0).test(stackInQuestion)) {
						ingredients++;
					} else {
						return false;
					}
				}
			}
			int duraRes = ingredients * this.getRepairDurability();
			return scepter != null && (ingredients > 0 && (scepter.getDamageValue() + this.getRepairDurability() - duraRes) > 0);
		} else {
			List<ItemStack> others = new ArrayList<>();
			for (int i = 0; i < input.getContainerSize(); ++i) {
				ItemStack stackInQuestion = input.getItem(i);
				if (!stackInQuestion.isEmpty()) {
					if (stackInQuestion.is(this.scepter) && stackInQuestion.getDamageValue() > 0) {
						if (scepter != null) return false;
						scepter = stackInQuestion;
					} else {
						others.add(stackInQuestion);
					}
				}
			}
			// 4.8 checks the ingredient count and then StackedContents#canCraft; RecipeMatcher does both and also respects NBT ingredients (the potions)
			return scepter != null && RecipeMatcher.findMatches(others, this.repairItems) != null;
		}
	}

	@Override
	public ItemStack assemble(CraftingContainer input, RegistryAccess access) {
		ItemStack scepter = null;
		int ingredients = 0;
		for (int i = 0; i < input.getContainerSize(); ++i) {
			ItemStack stackInQuestion = input.getItem(i);
			if (!stackInQuestion.isEmpty()) {
				if (stackInQuestion.is(this.scepter) && stackInQuestion.getDamageValue() > 0) {
					scepter = stackInQuestion;
				} else if (this.repairItems.size() == 1 && this.repairItems.get(0).test(stackInQuestion)) {
					ingredients++;
				}
			}
		}

		if (scepter != null) {
			ItemStack copy = scepter.copyWithCount(1);
			if (ingredients > 0) {
				copy.setDamageValue(scepter.getDamageValue() - (this.getRepairDurability() * ingredients));
			} else {
				copy.setDamageValue(scepter.getDamageValue() - this.durability);
			}
			return copy;
		}
		return ItemStack.EMPTY;
	}

	public Item getScepter() {
		return this.scepter;
	}

	public List<Ingredient> getRepairItems() {
		return this.repairItems;
	}

	public int getRepairDurability() {
		return this.durability;
	}

	@Override
	public NonNullList<Ingredient> getIngredients() {
		NonNullList<Ingredient> ingredients = NonNullList.create();
		ingredients.addAll(this.repairItems);
		return ingredients;
	}

	@Override
	public boolean canCraftInDimensions(int width, int height) {
		return this.repairItems.size() + 1 <= width * height;
	}

	@Override
	public RecipeSerializer<?> getSerializer() {
		return TFRecipes.SCEPTER_REPAIR_RECIPE.get();
	}

	public static class Serializer implements RecipeSerializer<ScepterRepairRecipe> {

		@Override
		public ScepterRepairRecipe fromJson(ResourceLocation id, JsonObject json) {
			Item scepter = GsonHelper.getAsItem(json, "scepter");
			NonNullList<Ingredient> repairItems = NonNullList.create();
			for (JsonElement element : GsonHelper.getAsJsonArray(json, "repair_ingredients")) {
				repairItems.add(Ingredient.fromJson(element, false));
			}
			int durability = GsonHelper.getAsInt(json, "durability");
			CraftingBookCategory category = CraftingBookCategory.CODEC.byName(GsonHelper.getAsString(json, "category", null), CraftingBookCategory.MISC);
			return new ScepterRepairRecipe(id, scepter, repairItems, durability, category);
		}

		@Override
		public ScepterRepairRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
			CraftingBookCategory category = buffer.readEnum(CraftingBookCategory.class);
			Item scepter = buffer.readById(BuiltInRegistries.ITEM);
			NonNullList<Ingredient> repairItems = buffer.readCollection(NonNullList::createWithCapacity, Ingredient::fromNetwork);
			int durability = buffer.readVarInt();
			return new ScepterRepairRecipe(id, scepter == null ? Items.AIR : scepter, repairItems, durability, category);
		}

		@Override
		public void toNetwork(FriendlyByteBuf buffer, ScepterRepairRecipe recipe) {
			buffer.writeEnum(recipe.category());
			buffer.writeId(BuiltInRegistries.ITEM, recipe.scepter);
			buffer.writeCollection(recipe.repairItems, (buf, ingredient) -> ingredient.toNetwork(buf));
			buffer.writeVarInt(recipe.durability);
		}

		/**
		 * Writes the recipe JSON the way 4.8's codec does (the category is left out when it is the default).
		 */
		public static void toJson(JsonObject json, Item scepter, List<Ingredient> repairItems, int durability, CraftingBookCategory category) {
			if (category != CraftingBookCategory.MISC) json.addProperty("category", category.getSerializedName());
			json.addProperty("durability", durability);
			JsonArray ingredients = new JsonArray();
			repairItems.forEach(ingredient -> ingredients.add(ingredient.toJson()));
			json.add("repair_ingredients", ingredients);
			json.addProperty("scepter", BuiltInRegistries.ITEM.getKey(scepter).toString());
		}
	}
}
