package de.ellpeck.actuallyadditions.mod.gen;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.ActuallyBlocks;
import de.ellpeck.actuallyadditions.mod.blocks.base.AACrops;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstapContext;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.RandomPatchConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.SimpleBlockConfiguration;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.placement.BlockPredicateFilter;
import net.minecraft.world.level.levelgen.structure.templatesystem.RuleTest;
import net.minecraft.world.level.levelgen.structure.templatesystem.TagMatchTest;
import net.minecraft.world.level.material.Fluids;

import java.util.List;

// SkyTekx: the configured features of main (upstream 02422d65, ec4b79ff, 16cd3a69, 2ffe78d6, 38d6404b),
// written by the data generator into data/actuallyadditions/worldgen/configured_feature/
public class ActuallyConfiguredFeatures {
	public static final ResourceKey<ConfiguredFeature<?, ?>> ORE_BLACK_QUARTZ = createKey("ore_black_quartz");
	public static final ResourceKey<ConfiguredFeature<?, ?>> CANOLA_PATCH = createKey("canola_patch");
	public static final ResourceKey<ConfiguredFeature<?, ?>> FLAX_PATCH = createKey("flax_patch");
	public static final ResourceKey<ConfiguredFeature<?, ?>> COFFEE_PATCH = createKey("coffee_patch");
	public static final ResourceKey<ConfiguredFeature<?, ?>> RICE_PATCH = createKey("rice_patch");

	public static void bootstrap(BootstapContext<ConfiguredFeature<?, ?>> context) {
		// SkyTekx: there is no deepslate variant of the ore, so both targets place the same block
		RuleTest stoneRuleTest = new TagMatchTest(BlockTags.STONE_ORE_REPLACEABLES);
		RuleTest deepslateRuleTest = new TagMatchTest(BlockTags.DEEPSLATE_ORE_REPLACEABLES);
		List<OreConfiguration.TargetBlockState> list = List.of(
				OreConfiguration.target(stoneRuleTest, ActuallyBlocks.BLACK_QUARTZ_ORE.get().defaultBlockState()),
				OreConfiguration.target(deepslateRuleTest, ActuallyBlocks.BLACK_QUARTZ_ORE.get().defaultBlockState())
		);
		FeatureUtils.register(context, ORE_BLACK_QUARTZ, Feature.ORE, new OreConfiguration(list, 6));
		FeatureUtils.register(context, CANOLA_PATCH, Feature.RANDOM_PATCH, plantPatch(ActuallyBlocks.CANOLA.get(), 10));
		FeatureUtils.register(context, FLAX_PATCH, Feature.RANDOM_PATCH, plantPatch(ActuallyBlocks.FLAX.get(), 8));
		FeatureUtils.register(context, COFFEE_PATCH, Feature.RANDOM_PATCH, plantPatch(ActuallyBlocks.COFFEE.get(), 6));
		FeatureUtils.register(context, RICE_PATCH, Feature.RANDOM_PATCH, waterPlantPatch(ActuallyBlocks.RICE.get(), 20));
	}

	// SkyTekx: a ripe crop with persistent set (38d6404b), so the patch survives block updates on its grass
	private static RandomPatchConfiguration plantPatch(Block crop, int tries) {
		BlockStateProvider stateProvider = BlockStateProvider.simple(crop.defaultBlockState().setValue(AACrops.AGE, AACrops.MAX_AGE).setValue(AACrops.PERSISTENT, true));
		return FeatureUtils.simpleRandomPatchConfiguration(
				tries, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(stateProvider))
		);
	}

	// SkyTekx: upstream 2ffe78d6, rice only grows where the block below touches water on one side
	private static RandomPatchConfiguration waterPlantPatch(Block crop, int tries) {
		BlockStateProvider stateProvider = BlockStateProvider.simple(crop.defaultBlockState().setValue(AACrops.AGE, AACrops.MAX_AGE).setValue(AACrops.PERSISTENT, true));
		return new RandomPatchConfiguration(tries, 8, 0, PlacementUtils.inlinePlaced(Feature.SIMPLE_BLOCK, new SimpleBlockConfiguration(stateProvider),
				BlockPredicateFilter.forPredicate(BlockPredicate.allOf(
						BlockPredicate.ONLY_IN_AIR_PREDICATE,
						BlockPredicate.anyOf(
								BlockPredicate.matchesFluids(new BlockPos(1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER),
								BlockPredicate.matchesFluids(new BlockPos(-1, -1, 0), Fluids.WATER, Fluids.FLOWING_WATER),
								BlockPredicate.matchesFluids(new BlockPos(0, -1, 1), Fluids.WATER, Fluids.FLOWING_WATER),
								BlockPredicate.matchesFluids(new BlockPos(0, -1, -1), Fluids.WATER, Fluids.FLOWING_WATER)
						)
				)))
		);
	}

	private static ResourceKey<ConfiguredFeature<?, ?>> createKey(String name) {
		return ResourceKey.create(Registries.CONFIGURED_FEATURE, new ResourceLocation(ActuallyAdditions.MODID, name));
	}
}
