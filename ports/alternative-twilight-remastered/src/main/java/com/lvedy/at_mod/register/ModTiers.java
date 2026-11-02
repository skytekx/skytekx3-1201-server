package com.lvedy.at_mod.register;

import net.minecraft.world.item.Items;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;

// 1.20.1 backport: NeoForge's SimpleTier(incorrectBlocksTag, ...) becomes Forge's ForgeTier(level, ..., needsTag, ...).
public class ModTiers {
    public static final Tier COPPER = new ForgeTier(
            Tiers.STONE.getLevel(), // Copper uses stone mining level
            200, // Durability
            5.0f, // Mining speed
            1.5f, // Attack damage bonus
            14, // Enchantability
            Tiers.STONE.getTag(),
            () -> Ingredient.of(Items.COPPER_INGOT) // Repair ingredient
    );

    // 金长矛的专属等级（耐久度+100）
    public static final Tier GOLDEN_SPEAR = new ForgeTier(
            Tiers.GOLD.getLevel(),
            Tiers.GOLD.getUses() + 100, // 32 + 100 = 132
            Tiers.GOLD.getSpeed(),
            Tiers.GOLD.getAttackDamageBonus(),
            Tiers.GOLD.getEnchantmentValue(),
            Tiers.GOLD.getTag(),
            Tiers.GOLD::getRepairIngredient
    );
}
