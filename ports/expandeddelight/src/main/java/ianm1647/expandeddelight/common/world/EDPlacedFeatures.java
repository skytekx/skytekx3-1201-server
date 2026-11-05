package ianm1647.expandeddelight.common.world;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.common.registry.EDBlocks;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.placement.HeightRangePlacement;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

public class EDPlacedFeatures {
    private static String MODID = ExpandedDelight.MODID;

    public static ResourceKey<PlacedFeature> SALT_ORE;

    public static ResourceKey<PlacedFeature> CINNAMON_TREE;

    public static ResourceKey<PlacedFeature> PATCH_WILD_ASPARAGUS;
    public static ResourceKey<PlacedFeature> PATCH_WILD_SWEET_POTATO;
    public static ResourceKey<PlacedFeature> PATCH_WILD_CHILI_PEPPER;
    public static ResourceKey<PlacedFeature> PATCH_WILD_PEANUT;
    public static ResourceKey<PlacedFeature> PATCH_CRANBERRIES;

    public static void bootstrap(BootstrapContext<PlacedFeature> context) {
        var configuredFeatures = context.lookup(Registries.CONFIGURED_FEATURE);

        context.register(SALT_ORE, new PlacedFeature(configuredFeatures.getOrThrow(EDConfiguredFeatures.SALT_ORE),
                OrePlacement.commonOrePlacement(6, HeightRangePlacement.uniform(VerticalAnchor.absolute(-20), VerticalAnchor.absolute(60)))));

        context.register(CINNAMON_TREE, new PlacedFeature(configuredFeatures.getOrThrow(EDConfiguredFeatures.CINNAMON_TREE),
                VegetationPlacements.treePlacement(PlacementUtils.countExtra(3, 0.1f, 2),
                        EDBlocks.CINNAMON_SAPLING.get())));
    }

    static {
        SALT_ORE = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, "salt_ore"));

        CINNAMON_TREE = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, "cinnamon_tree"));

        PATCH_WILD_ASPARAGUS = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, "patch_wild_asparagus"));
        PATCH_WILD_SWEET_POTATO = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, "patch_wild_sweet_potato"));
        PATCH_WILD_CHILI_PEPPER = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, "patch_wild_chili_pepper"));
        PATCH_WILD_PEANUT = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, "patch_wild_peanuts"));
        PATCH_CRANBERRIES = ResourceKey.create(Registries.PLACED_FEATURE, ResourceLocation.fromNamespaceAndPath(MODID, "patch_cranberries"));

    }
}
