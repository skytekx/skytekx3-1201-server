package twilightforest.world.components.feature.trees.growers;

import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFConfiguredFeatures;

/**
 * A single canopy sapling grows a canopy tree; four of them in a 2x2 square grow a Mega Canopy.
 */
public class CanopyTreeGrower extends AbstractMegaTreeGrower {

	@Nullable
	@Override
	protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean makeBees) {
		return TFConfiguredFeatures.CANOPY_TREE;
	}

	@Nullable
	@Override
	protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource random) {
		return TFConfiguredFeatures.MEGA_CANOPY_TREE;
	}
}
