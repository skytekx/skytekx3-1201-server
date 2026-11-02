package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.ParticleUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFParticleType;

public class MagicLeavesBlock extends TFLeavesBlock {

	public MagicLeavesBlock(BlockBehaviour.Properties props) {
		super(props);
	}

	@Override
	public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
		if (state.getBlock() == TFBlocks.TRANSFORMATION_LEAVES.get()) {
			// runes drift down from the underside of the leaves
			ParticleUtils.spawnParticlesOnBlockFace(level, pos, TFParticleType.LEAF_RUNE.get(), ConstantInt.of(1), Direction.DOWN, () -> Vec3.ZERO, 0.5F);
		}
	}
}
