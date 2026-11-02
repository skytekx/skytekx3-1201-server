package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.BlockStateConfiguration;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import twilightforest.init.TFBlocks;
import twilightforest.util.LandmarkUtil;

import java.util.Optional;

/**
 * Walks down from the origin and places the configured plant in empty spots it can survive in, wandering a few blocks
 * sideways on the way. Stops after {@code maxCount} plants and, unless {@code spawnInStructure}, skips spots inside the
 * nearest landmark.
 */
public class UndergroundPlantFeature extends Feature<BlockStateConfiguration> {
	private final int maxCount;
	private final boolean spawnInStructure;

	public UndergroundPlantFeature(Codec<BlockStateConfiguration> config, int maxCount) {
		this(config, maxCount, false);
	}

	public UndergroundPlantFeature(Codec<BlockStateConfiguration> config) {
		this(config, Integer.MAX_VALUE, false);
	}

	public UndergroundPlantFeature(Codec<BlockStateConfiguration> config, boolean spawnInStructure) {
		this(config, Integer.MAX_VALUE, spawnInStructure);
	}

	public UndergroundPlantFeature(Codec<BlockStateConfiguration> config, int maxCount, boolean spawnInStructure) {
		super(config);
		this.maxCount = maxCount;
		this.spawnInStructure = spawnInStructure;
	}

	@Override
	public boolean place(FeaturePlaceContext<BlockStateConfiguration> ctx) {
		WorldGenLevel world = ctx.level();
		BlockPos origin = ctx.origin();
		RandomSource random = ctx.random();

		int x = origin.getX();
		int z = origin.getZ();
		int placed = 0;

		for (int y = origin.getY(); y > world.getMinBuildHeight(); y--) {
			if (placed >= this.maxCount)
				break;

			BlockPos pos = new BlockPos(x, y, z);
			if (!world.isEmptyBlock(pos) || random.nextInt(6) == 0) {
				x = origin.getX() + random.nextInt(4) - random.nextInt(4);
				z = origin.getZ() + random.nextInt(4) - random.nextInt(4);
				continue;
			}

			BlockState state = ctx.config().state;
			if (state.is(TFBlocks.TROLLVIDR.get()) && random.nextInt(10) == 0)
				state = TFBlocks.UNRIPE_TROLLBER.get().defaultBlockState();
			// 4.8 looks the landmark up before the survival check; it consumes no randomness, so only looking it up when needed places the same plants
			if (state.canSurvive(world, pos) && (this.spawnInStructure || !isInsideLandmark(world, pos))) {
				world.setBlock(pos, state, Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_CLIENTS);
				placed++;
			}
		}
		return placed > 0;
	}

	private static boolean isInsideLandmark(WorldGenLevel world, BlockPos pos) {
		Optional<StructureStart> structureStart = LandmarkUtil.locateNearestLandmarkStart(world, SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
		return structureStart.isPresent() && structureStart.get().getBoundingBox().isInside(pos);
	}
}
