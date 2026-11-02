package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFItems;

/**
 * Purple fire placed with Exanimate Essence. It never spreads or burns out, hurts everything that is not undead, and its
 * kills are turned undead (see {@link twilightforest.init.TFDataMaps#OMINOUS_FIRE} and EntityEvents#ominousFireConversion).
 */
public class OminousFireBlock extends BaseFireBlock {

	public OminousFireBlock(Properties properties) {
		super(properties, 1.0F);
	}

	@Override
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
		return this.canSurvive(state, level, currentPos) ? this.defaultBlockState() : Blocks.AIR.defaultBlockState();
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return level.getBlockState(pos.below()).isFaceSturdy(level, pos.below(), Direction.UP) && level.getFluidState(pos).isEmpty();
	}

	@Override
	protected boolean canBurn(BlockState state) {
		return true;
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		return new ItemStack(TFItems.EXANIMATE_ESSENCE.get());
	}

	// replaces the vanilla fire behaviour: nothing is set alight, and everything except undead mobs takes ominous fire damage
	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (!(entity instanceof LivingEntity living && living.getMobType() == MobType.UNDEAD)) {
			entity.hurt(TFDamageTypes.getDamageSource(level, TFDamageTypes.OMINOUS_FIRE), 1.0F);
		}
	}
}
