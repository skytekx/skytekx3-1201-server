package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import twilightforest.init.TFBlocks;
import twilightforest.util.LandmarkUtil;

public class UndergroundPlantFeature extends Feature<BlockStateConfiguration> {
	private final boolean spawnInStructure;

	public UndergroundPlantFeature(Codec<BlockStateConfiguration> config) {
		this(config, false);
	}

	/**
	 * @param spawnInStructure whether the plants may also grow inside the bounding box of the nearest landmark (Troll
	 *                         Cave vegetation). Otherwise they are kept out of landmark structures, as in 4.8 (#2532).
	 */
	public UndergroundPlantFeature(Codec<BlockStateConfiguration> config, boolean spawnInStructure) {
		super(config);
		this.spawnInStructure = spawnInStructure;
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockStateConfiguration> ctx) {
		WorldGenLevel world = ctx.level();
		BlockPos pos = ctx.origin();
		RandomSource random = ctx.random();

		int copyX = pos.getX();
		int copyZ = pos.getZ();

		for (; pos.getY() > world.getMinBuildHeight(); pos = pos.below()) {
			if (world.isEmptyBlock(pos) && random.nextInt(6) > 0) {
				if (ctx.config().state.canSurvive(ctx.level(), pos) && (this.spawnInStructure || !isInsideLandmark(world, pos))) {
					if (ctx.config().state.is(TFBlocks.TROLLVIDR.get()) && random.nextInt(10) == 0) {
						world.setBlock(pos, TFBlocks.UNRIPE_TROLLBER.get().defaultBlockState(), 16 | 2);
					} else {
						world.setBlock(pos, ctx.config().state, 16 | 2);
					}
				}
			} else {
				pos = new BlockPos(
						copyX + random.nextInt(4) - random.nextInt(4),
						pos.getY(),
						copyZ + random.nextInt(4) - random.nextInt(4)
				);
			}
		}
		return true;
	}

	// Only looked up once a plant could actually be placed, as the landmark lookup is not free
	private static boolean isInsideLandmark(WorldGenLevel world, BlockPos pos) {
		return LandmarkUtil.locateNearestLandmarkStart(world, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()))
				.map(start -> start.getBoundingBox().isInside(pos))
				.orElse(false);
	}
}
