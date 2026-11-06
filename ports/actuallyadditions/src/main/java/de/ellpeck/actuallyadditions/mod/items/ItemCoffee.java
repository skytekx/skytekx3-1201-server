/*
 * This file ("ItemCoffee.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.api.ActuallyAdditionsAPI;
import de.ellpeck.actuallyadditions.mod.crafting.ActuallyRecipes;
import de.ellpeck.actuallyadditions.mod.crafting.CoffeeIngredientRecipe;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.StringUtil;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

// SkyTekx: the cup of coffee of upstream main on the 1.20.1 item API. The base registered the cup as
// a plain item, so it could not be drunk, and kept this class unused
public class ItemCoffee extends ItemBase {
    private static final FoodProperties FOOD = new FoodProperties.Builder().nutrition(8).saturationMod(5.0F).alwaysEat().build();

    public ItemCoffee() {
        // Three sips per cup, and no repair in a crafting grid or an anvil (upstream e6156bfc, #1512)
        super(ActuallyItems.defaultProps().food(FOOD).durability(3).setNoRepair());
    }

    /**
     * The ingredient for a stack. The server reads the list of the API, which only the server fills,
     * and a client falls back to the recipes it got from the server.
     */
    @Nullable
    public static CoffeeIngredientRecipe getIngredientRecipeFromStack(ItemStack stack) {
        for (CoffeeIngredientRecipe recipe : ActuallyAdditionsAPI.COFFEE_MACHINE_INGREDIENTS) {
            if (recipe.getIngredient().test(stack)) {
                return recipe;
            }
        }
        return null;
    }

    @Nullable
    public static CoffeeIngredientRecipe getIngredientRecipeFromStack(@Nullable Level level, ItemStack stack) {
        if (level != null && level.isClientSide) {
            for (CoffeeIngredientRecipe recipe : level.getRecipeManager().getAllRecipesFor(ActuallyRecipes.Types.COFFEE_INGREDIENT.get())) {
                if (recipe.getIngredient().test(stack)) {
                    return recipe;
                }
            }
            return null;
        }
        return getIngredientRecipeFromStack(stack);
    }

    // The cup stores the durations in seconds
    public static void applyPotionEffectsFromStack(ItemStack stack, LivingEntity player) {
        MobEffectInstance[] effects = ActuallyAdditionsAPI.methodHandler.getEffectsFromStack(stack);
        if (effects != null && effects.length > 0) {
            for (MobEffectInstance effect : effects) {
                player.addEffect(new MobEffectInstance(effect.getEffect(), effect.getDuration() * 20, effect.getAmplifier()));
            }
        }
    }

    // SkyTekx: upstream 0d27a4a3 (#1422) reads the effects from the copy, because eating empties the
    // stack in hand, and gives back an empty cup after the last sip (c2b2cc4d)
    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        ItemStack theStack = stack.copy();
        super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide) {
            applyPotionEffectsFromStack(theStack, livingEntity);
        }
        theStack.setDamageValue(theStack.getDamageValue() + 1);
        if (theStack.getMaxDamage() - theStack.getDamageValue() <= 0) {
            return new ItemStack(ActuallyItems.EMPTY_CUP.get());
        } else {
            return theStack;
        }
    }

    @Override
    public UseAnim getUseAnimation(ItemStack pStack) {
        return UseAnim.DRINK;
    }

    // SkyTekx: upstream d4cc7b07. The cup holds seconds, and the 1.20.1 formatter takes ticks
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag advanced) {
        MobEffectInstance[] effects = ActuallyAdditionsAPI.methodHandler.getEffectsFromStack(stack);
        if (effects != null) {
            for (MobEffectInstance effect : effects) {
                tooltip.add(Component.translatable(effect.getDescriptionId())
                        .append(" " + (effect.getAmplifier() + 1) + ", " + StringUtil.formatTickDuration(effect.getDuration() * 20))
                        .withStyle(ChatFormatting.GRAY));
            }
        } else {
            tooltip.add(Component.translatable("tooltip.actuallyadditions.coffeeCup.noEffect").withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public boolean isEnchantable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment) {
        return false;
    }
}
