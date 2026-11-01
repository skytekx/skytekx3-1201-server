package twilightforest.world.components.feature;

import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.VineBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import twilightforest.init.TFBiomes;
import twilightforest.init.TFBlocks;

/**
 * Hangs long vines through the whole Enchanted Forest chunk (TF 4.8, #2275): every air block from y -10 up to the
 * tallest neighbouring motion-blocking column gets a vine 1 in 7 times when it has a horizontal face to cling to.
 * Vines clinging only to logs and leaves are a further 8 times rarer, and rainbow oak leaves are never used.
 * Placed once per chunk at TOP_LAYER_MODIFICATION, after the trees.
 */
public class EnchantedForestVinesFeature extends Feature<NoneFeatureConfiguration> {
	private static final int RARITY = 7;
	private static final int EXTRA_RARITY_ON_TREES = 8;
	private static final int MIN_Y = -10;

	public EnchantedForestVinesFeature(Codec<NoneFeatureConfiguration> codec) {
		super(codec);
	}

	@Override
	public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
		WorldGenLevel world = context.level();
		RandomSource random = context.random();
		BlockPos origin = context.origin();

		// All column heights are read before any vine is placed, as upstream does
		int[] maxYs = new int[16 * 16];
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				maxYs[dx * 16 + dz] = getColumnMaxY(world, origin.getX() + dx, origin.getZ() + dz);
			}
		}

		BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
		for (int dx = 0; dx < 16; dx++) {
			for (int dz = 0; dz < 16; dz++) {
				int maxY = maxYs[dx * 16 + dz];
				for (int y = MIN_Y; y <= maxY; y++) {
					this.setVine(world, random, pos.set(origin.getX() + dx, y, origin.getZ() + dz));
				}
			}
		}

		return true;
	}

	private static int getColumnMaxY(WorldGenLevel world, int x, int z) {
		int height1 = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x - 1, z);
		int height2 = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x + 1, z);
		int height3 = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z - 1);
		int height4 = world.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z + 1);

		return Math.max(Math.max(height1, height2), Math.max(height3, height4));
	}

	private void setVine(WorldGenLevel world, RandomSource random, BlockPos pos) {
		if (random.nextInt(RARITY) > 0 || !world.getBlockState(pos).isAir() || !isSuitableBiome(world, pos))
			return;

		BlockState state = Blocks.VINE.defaultBlockState();
		boolean empty = true;
		boolean isTree = true;

		for (Direction dir : Direction.values()) {
			BlockPos relativePos = pos.relative(dir);
			if (dir != Direction.DOWN && VineBlock.isAcceptableNeighbour(world, relativePos, dir) && !world.getBlockState(relativePos).is(TFBlocks.RAINBOW_OAK_LEAVES.get())) {
				if (!isTree(world.getBlockState(relativePos)))
					isTree = false;

				state = state.setValue(VineBlock.getPropertyForFace(dir), true);
				if (dir.getAxis() != Direction.Axis.Y)
					empty = false;
			}
		}
		if (isTree && random.nextInt(EXTRA_RARITY_ON_TREES) > 0)
			return;

		if (!empty)
			this.setBlock(world, pos, state);
	}

	private static boolean isSuitableBiome(WorldGenLevel world, BlockPos pos) {
		return world.getBiome(pos).is(TFBiomes.ENCHANTED_FOREST);
	}

	private static boolean isTree(BlockState state) {
		return state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES);
	}
}
