package ianm1647.expandeddelight.common.world.tree;

import ianm1647.expandeddelight.common.world.EDConfiguredFeatures;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

import javax.annotation.Nullable;

/**
 * Minecraft 1.21.1 describes tree growers as TreeGrower records, 1.20.1 as AbstractTreeGrower subclasses.
 */
public class EDTreeGrowers {

    public static class Cinnamon extends AbstractTreeGrower {
        @Nullable
        @Override
        protected ResourceKey<ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource random, boolean hasFlowers) {
            return EDConfiguredFeatures.CINNAMON_TREE;
        }
    }
}
