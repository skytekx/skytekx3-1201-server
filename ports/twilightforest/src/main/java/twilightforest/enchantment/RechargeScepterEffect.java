package twilightforest.enchantment;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import twilightforest.init.TFEnchantments;
import twilightforest.init.TFItems;
import twilightforest.item.recipe.ScepterRepairRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * The Renewal effect: an empty scepter takes one Exanimate Essence, or one set of its scepter_repair ingredients, from
 * the player's inventory and recharges itself.
 */
public final class RechargeScepterEffect {

	private RechargeScepterEffect() {
	}

	/**
	 * Called from each scepter's inventoryTick. 4.8 runs the effect every tick while the scepter is held (the enchantment's
	 * tick effect) and every second from anywhere else in the inventory (inventoryTick); this does the same.
	 */
	public static void inventoryTick(ItemStack stack, Level level, Entity entity, boolean isSelected) {
		if (level instanceof ServerLevel serverLevel && stack.getEnchantmentLevel(TFEnchantments.RENEWAL.get()) > 0) {
			boolean held = isSelected || (entity instanceof LivingEntity living && living.getOffhandItem() == stack);
			if (held || entity.tickCount % 20 == 0) {
				applyRecharge(serverLevel, stack, entity);
			}
		}
	}

	public static void applyRecharge(ServerLevel level, ItemStack item, Entity entity) {
		if (entity instanceof Player player && item.getDamageValue() == item.getMaxDamage()) {
			List<ScepterRepairRecipe> recipes = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream().filter(recipe -> recipe instanceof ScepterRepairRecipe).map(ScepterRepairRecipe.class::cast).toList();
			List<Integer> slotsToConsume = new ArrayList<>();
			for (ScepterRepairRecipe recipe : recipes) {
				if (item.is(recipe.getScepter())) {
					List<Ingredient> ingredientCopy = new ArrayList<>(recipe.getIngredients());
					scepterItemsCheck:
					for (int i = 0; i < player.getInventory().items.size(); i++) {
						ItemStack stack = player.getInventory().items.get(i);
						if (stack.isEmpty()) continue;
						if (stack.is(TFItems.EXANIMATE_ESSENCE.get())) {
							stack.shrink(1);
							item.setDamageValue(0);
							return;
						}
						for (Ingredient ingredient : recipe.getIngredients()) {
							if (ingredientCopy.contains(ingredient) && ingredient.test(stack)) {
								ingredientCopy.remove(ingredient);
								slotsToConsume.add(i);
								if (ingredientCopy.isEmpty()) break scepterItemsCheck;
							}
						}
					}

					if (slotsToConsume.size() == recipe.getIngredients().size()) {
						for (int slot : slotsToConsume) {
							ItemStack stack = player.getInventory().items.get(slot);
							stack.shrink(1);
							if (stack.hasCraftingRemainingItem()) {
								if (!player.getInventory().add(stack.getCraftingRemainingItem())) {
									player.drop(stack.getCraftingRemainingItem(), false);
								}
							}
						}
						item.setDamageValue(item.getDamageValue() - recipe.getRepairDurability());
					}
				}
			}
		}
	}
}
