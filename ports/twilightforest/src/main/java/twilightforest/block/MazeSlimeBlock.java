package twilightforest.block;

import net.minecraft.world.level.block.SlimeBlock;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFBlocks;

/**
 * A slime block that only sticks to other maze slime blocks and to #twilightforest:mazestone when pushed by a piston.
 */
public class MazeSlimeBlock extends SlimeBlock {
	public MazeSlimeBlock(Properties properties) {
		super(properties);
	}

	@Override
	public boolean canStickTo(BlockState state, BlockState other) {
		return other.is(TFBlocks.MAZE_SLIME_BLOCK.get()) || other.is(BlockTagGenerator.MAZESTONE);
	}

	@Override
	public boolean isStickyBlock(BlockState state) {
		return true;
	}
}
