package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.data.tags.BlockTagGenerator;

/**
 * The berry bushes that grow in the Dark Tower's nether rooms: they live on netherrack-like blocks and die on nylium.
 */
public class DarkTowerBerryBushBlock extends BerryBushBlock {

	public DarkTowerBerryBushBlock(ResourceLocation berryTable, Properties properties) {
		super(berryTable, properties);
	}

	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (!level.isAreaLoaded(pos, 1)) return;
		if (this.shouldDie(level, pos))
			level.destroyBlock(pos, true);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		if (this.shouldDie(level, currentPos))
			level.scheduleTick(currentPos, this, 1);

		return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
	}

	@Override
	public boolean canBePlacedAt(BlockState state) {
		return state.is(BlockTagGenerator.DARK_TOWER_BERRY_BUSHES_SURVIVE);
	}

	@Override
	protected boolean canGrowAt(BlockState state, LevelReader level, BlockPos pos) {
		return super.canGrowAt(state, level, pos) && state.getValue(SNOW_LAYERS) == 0;
	}

	protected boolean shouldDie(LevelAccessor level, BlockPos pos) {
		return level.getBlockState(pos.below()).is(BlockTagGenerator.DARK_TOWER_BERRY_BUSHES_DIE);
	}
}
