package ianm1647.expandeddelight.common.world;

import ianm1647.expandeddelight.ExpandedDelight;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

/**
 * The keys of the configured features. Upstream builds their JSON with datagen bootstrap methods, this port
 * keeps the converted JSON in src/generated/resources and needs the keys only for the sapling.
 */
public class EDConfiguredFeatures {
    private static final String MODID = ExpandedDelight.MODID;

    public static final ResourceKey<ConfiguredFeature<?, ?>> SALT_ORE = key("salt_ore");

    public static final ResourceKey<ConfiguredFeature<?, ?>> CINNAMON_TREE = key("cinnamon_tree");

    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WILD_ASPARAGUS = key("patch_wild_asparagus");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WILD_SWEET_POTATO = key("patch_wild_sweet_potato");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WILD_CHILI_PEPPER = key("patch_wild_chili_pepper");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_WILD_PEANUT = key("patch_wild_peanuts");
    public static final ResourceKey<ConfiguredFeature<?, ?>> PATCH_CRANBERRIES = key("patch_cranberries");

    private static ResourceKey<ConfiguredFeature<?, ?>> key(String path) {
        return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(MODID, path));
    }
}
