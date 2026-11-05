package com.blakebr0.mysticalautomation.compat;

import com.blakebr0.cucumber.util.Tooltip;
import com.blakebr0.mysticalagriculture.api.crafting.IAwakeningRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IEnchanterRecipe;
import com.blakebr0.mysticalagriculture.api.crafting.IInfusionRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class MysticalCompat {
    public static final class Items {
        public static final RegistryObject<Item> INFERIUM_ESSENCE = item("mysticalagriculture:inferium_essence");
        public static final RegistryObject<Item> PRUDENTIUM_ESSENCE = item("mysticalagriculture:prudentium_essence");
        public static final RegistryObject<Item> TERTIUM_ESSENCE = item("mysticalagriculture:tertium_essence");
        public static final RegistryObject<Item> IMPERIUM_ESSENCE = item("mysticalagriculture:imperium_essence");
        public static final RegistryObject<Item> SUPREMIUM_ESSENCE = item("mysticalagriculture:supremium_essence");

        public static final RegistryObject<Item> INFUSION_CRYSTAL = item("mysticalagriculture:infusion_crystal");
        public static final RegistryObject<Item> MASTER_INFUSION_CRYSTAL = item("mysticalagriculture:master_infusion_crystal");

        public static final RegistryObject<Item> FERTILIZED_ESSENCE = item("mysticalagriculture:fertilized_essence");

        public static final RegistryObject<Item> INSANIUM_ESSENCE = item("mysticalagradditions:insanium_essence");

        private static RegistryObject<Item> item(String id) {
            return RegistryObject.create(new ResourceLocation(id), ForgeRegistries.ITEMS);
        }
    }

    public static final class RecipeTypes {
        public static final RegistryObject<RecipeType<IInfusionRecipe>> INFUSION = recipeType("mysticalagriculture:infusion");
        public static final RegistryObject<RecipeType<IAwakeningRecipe>> AWAKENING = recipeType("mysticalagriculture:awakening");
        public static final RegistryObject<RecipeType<IEnchanterRecipe>> ENCHANTER = recipeType("mysticalagriculture:enchanter");

        // SkyTekx port: a typed lookup by id, the Forge form of upstream's DeferredHolder.create
        private static <T extends RecipeType<?>> RegistryObject<T> recipeType(String id) {
            return RegistryObject.create(new ResourceLocation(id), Registries.RECIPE_TYPE, "mysticalautomation");
        }
    }

    public static final class Tooltips {
        // SkyTekx port: Mystical Agriculture 8 has these three tooltips, Mystical Agriculture 7 does not, so the keys
        // and their translations live in this mod's lang files
        public static final Tooltip CHANCE = new Tooltip("tooltip.mysticalautomation.chance");
        public static final Tooltip MACHINE_AREA = new Tooltip("tooltip.mysticalagriculture.machine_area");
        public static final Tooltip MACHINE_SPEED = new Tooltip("tooltip.mysticalagriculture.machine_speed");
        public static final Tooltip MACHINE_FUEL_RATE = new Tooltip("tooltip.mysticalagriculture.machine_fuel_rate");
        public static final Tooltip MACHINE_FUEL_CAPACITY = new Tooltip("tooltip.mysticalagriculture.machine_fuel_capacity");
        public static final Tooltip MACHINE_FUEL_USAGE = new Tooltip("tooltip.mysticalautomation.machine_fuel_usage");
        public static final Tooltip MACHINE_SCAN_FUEL_USAGE = new Tooltip("tooltip.mysticalautomation.machine_scan_fuel_usage");
    }

    // SkyTekx port: ItemStack#is(Holder) of 1.21 for a Forge RegistryObject, false while the item does not exist
    public static boolean is(ItemStack stack, RegistryObject<Item> item) {
        return item.isPresent() && stack.is(item.get());
    }

    public static boolean isEssence(ItemStack stack) {
        return is(stack, Items.INFERIUM_ESSENCE)
                || is(stack, Items.PRUDENTIUM_ESSENCE)
                || is(stack, Items.TERTIUM_ESSENCE)
                || is(stack, Items.IMPERIUM_ESSENCE)
                || is(stack, Items.SUPREMIUM_ESSENCE)
                || is(stack, Items.INSANIUM_ESSENCE);
    }

    public static boolean isInfusionCrystal(ItemStack stack) {
        return is(stack, Items.INFUSION_CRYSTAL) || is(stack, Items.MASTER_INFUSION_CRYSTAL);
    }
}
