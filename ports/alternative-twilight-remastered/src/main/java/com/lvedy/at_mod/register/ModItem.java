package com.lvedy.at_mod.register;

import com.lvedy.at_mod.special.ModItems.SpearItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Tiers;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import twilightforest.util.TwilightItemTier;

import static com.lvedy.at_mod.AlternativeTwilight.MODID;

// 1.20.1 backport: 4.8's TFToolMaterials is TwilightItemTier in the TF fork; the attribute triple
// (damage, attack speed, reach) moves from Item.Properties#attributes into the SpearItem constructor.
public class ModItem {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> WOODEN_SPEAR = ITEMS.register(
            "spear/wooden_spear", () -> new SpearItem(Tiers.WOOD, new Item.Properties(), 2, -2.4, 1, 2F));

    public static final RegistryObject<Item> STONE_SPEAR = ITEMS.register(
            "spear/stone_spear", () -> new SpearItem(Tiers.STONE, new Item.Properties(), 3, -2.8, 1.1, 1.8F));

    public static final RegistryObject<Item> COPPER_SPEAR = ITEMS.register(
            "spear/copper_spear", () -> new SpearItem(ModTiers.COPPER, new Item.Properties(), 3, -2.8, 1.1, 1.7F));

    public static final RegistryObject<Item> IRON_SPEAR = ITEMS.register(
            "spear/iron_spear", () -> new SpearItem(Tiers.IRON, new Item.Properties(), 4, -3, 1.2, 1.6F));

    public static final RegistryObject<Item> GOLDEN_SPEAR = ITEMS.register(
            "spear/golden_spear", () -> new SpearItem(ModTiers.GOLDEN_SPEAR, new Item.Properties(), 2, -2.2, 1.0, 2.1F));

    public static final RegistryObject<Item> DIAMOND_SPEAR = ITEMS.register(
            "spear/diamond_spear", () -> new SpearItem(Tiers.DIAMOND, new Item.Properties(), 5, -2.8, 1.2, 1.75F));

    public static final RegistryObject<Item> NETHERITE_SPEAR = ITEMS.register(
            "spear/netherite_spear", () -> new SpearItem(Tiers.NETHERITE, new Item.Properties(), 6, -2.8, 1.3, 1.8F));

    public static final RegistryObject<Item> IRONWOOD_SPEAR = ITEMS.register(
            "spear/ironwood_spear", () -> new SpearItem(TwilightItemTier.IRONWOOD, new Item.Properties(), 4, -2.4, 1.1, 2F));

    public static final RegistryObject<Item> KNIGHT_SPEAR = ITEMS.register(
            "spear/knight_spear", () -> new SpearItem(TwilightItemTier.KNIGHTMETAL, new Item.Properties(), 6, -2.4, 1.3, 2.1F));

    public static void register(IEventBus bus) {
        ITEMS.register(bus);
    }
}
