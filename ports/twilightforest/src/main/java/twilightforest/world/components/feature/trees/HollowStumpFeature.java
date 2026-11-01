package twilightforest.world.components.feature.trees;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import twilightforest.util.FeatureLogic;
import twilightforest.util.FeaturePlacers;
import twilightforest.util.FeatureUtil;
import twilightforest.util.RootPlacer;
import twilightforest.util.VoxelBresenhamIterator;
import twilightforest.world.components.feature.config.TFTreeFeatureConfig;

import java.util.function.BiConsumer;

/**
 * A stump from a hollow tree
 *
 * @author Ben
 */
public class HollowStumpFeature extends HollowTreeFeature {

	public HollowStumpFeature(Codec<TFTreeFeatureConfig> config) {
		super(config);
	}

	@Override
	public boolean generate(WorldGenLevel world, RandomSource random, BlockPos pos, BiConsumer<BlockPos, BlockState> trunkPlacer, BiConsumer<BlockPos, BlockState> leavesPlacer, BiConsumer<BlockPos, BlockState> decorationPlacer, TFTreeFeatureConfig config) {
		int radius = random.nextInt(2) + 2;

		if (!FeatureUtil.isAreaSuitable(world, pos.offset(-radius, 0, -radius), 2 * radius, 6, 2 * radius)) {
			return false;
		}

		// TF 4.8 (bf74ddf65): the trunk goes first, the roots then trace through its logs (#minecraft:logs is in tree_roots_skip)
		this.buildTrunk(world, trunkPlacer, decorationPlacer, random, pos, radius, 6, config);

		// 3-5 roots at the bottom
		buildBranchRing(world, trunkPlacer, leavesPlacer, random, pos, radius, 3, 2, 6, 0.75D, 3, 5, 3, false, config);

		// several more taproots
		buildBranchRing(world, trunkPlacer, leavesPlacer, random, pos, radius, 1, 2, 8, 0.9D, 3, 5, 3, false, config);

		return true;
	}

	@Override
	protected void buildTrunk(LevelAccessor world, BiConsumer<BlockPos, BlockState> trunkPlacer, BiConsumer<BlockPos, BlockState> decoPlacer, RandomSource random, BlockPos pos, int diameter, int maxheight, TFTreeFeatureConfig config) {
		int hollow = diameter >> 1;

		// go down 4 squares and fill in extra trunk as needed, in case we're on uneven terrain
		for (int dx = -diameter; dx <= diameter; dx++) {
			for (int dz = -diameter; dz <= diameter; dz++) {
				for (int dy = -4; dy < 0; dy++) {
					// determine how far we are from the center.
					int ax = Math.abs(dx);
					int az = Math.abs(dz);
					int dist = (int) (Math.max(ax, az) + (Math.min(ax, az) * 0.5));

					if (dist <= diameter) {
						BlockPos dPos = pos.offset(dx, dy, dz);
						if (FeatureLogic.hasEmptyNeighborExceptBelow(world, dPos)) {
							// The exposed base replaces the ground it sticks out of (4.8), the hollow core gets branch wood.
							// Deviation: blocks in minecraft:features_cannot_replace (chests, spawners, bedrock...) are kept.
							if (!world.isStateAtPosition(dPos, state -> state.is(BlockTags.FEATURES_CANNOT_REPLACE))) {
								BlockStateProvider trunkProvider = dist > hollow ? config.trunkProvider : config.branchProvider;
								trunkPlacer.accept(dPos, trunkProvider.getState(random, dPos));
							}
						} else {
							FeaturePlacers.placeIfValidRootPos(world, decoPlacer, random, dPos, config.rootsProvider);
						}
					}
				}
			}
		}

		// build the trunk upwards
		for (int dx = -diameter; dx <= diameter; dx++) {
			for (int dz = -diameter; dz <= diameter; dz++) {
				int height = 2 + random.nextInt(3) + random.nextInt(2);

				for (int dy = 0; dy <= height; dy++) {
					// determine how far we are from the center.
					int ax = Math.abs(dx);
					int az = Math.abs(dz);
					int dist = Math.max(ax, az) + (Math.min(ax, az) >> 1);

					// make a trunk!
					if (dist <= diameter && dist > hollow) {
						FeaturePlacers.placeIfValidTreePos(world, trunkPlacer, random, pos.offset(dx, dy, dz), config.trunkProvider);
					}
				}
			}
		}
	}

	/**
	 * Two blocks thick roots (4.8 bf74ddf65, made safe by d0e183242): the lower block is validated as well through
	 * the RootPlacer's root penetrability of 2. Only the stump gets these; 4.3's giant hollow oak keeps its own roots.
	 */
	@Override
	protected void makeRoot(LevelAccessor world, RandomSource random, BlockPos pos, int diameter, int branchHeight, double length, double angle, double tilt, TFTreeFeatureConfig config) {
		BlockPos src = FeatureLogic.translate(pos.above(branchHeight), diameter, angle, 0.5);
		BlockPos dest = FeatureLogic.translate(src, length, angle, tilt);

		FeaturePlacers.traceExposedRoot(world, new RootPlacer((checkedPos, state) -> {
			world.setBlock(checkedPos, state, Block.UPDATE_ALL);
			world.setBlock(checkedPos.below(), state, Block.UPDATE_ALL);
		}, 2), random, config.branchProvider, config.rootsProvider, new VoxelBresenhamIterator(src, dest));
	}
}
