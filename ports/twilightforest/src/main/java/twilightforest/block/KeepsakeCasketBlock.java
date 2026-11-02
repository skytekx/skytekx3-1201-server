package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.KeepsakeCasketBlockEntity;
import twilightforest.block.entity.SkullChestBlockEntity;
import twilightforest.enums.BlockLoggingEnum;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.item.KeepsakeCasketItem;

public class KeepsakeCasketBlock extends SkullChestBlock {

	public static final IntegerProperty BREAKAGE = IntegerProperty.create("damage", 0, 2);

	public KeepsakeCasketBlock(Properties properties) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(FACING, Direction.NORTH).setValue(BREAKAGE, 0));
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new KeepsakeCasketBlockEntity(pos, state);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return level.isClientSide() ? createTickerHelper(type, TFBlockEntities.KEEPSAKE_CASKET.get(), SkullChestBlockEntity::lidAnimateTick) : null;
	}

	@Override
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
		if (state.getValue(BlockLoggingEnum.MULTILOGGED).getBlock() == Blocks.AIR || state.getValue(BlockLoggingEnum.MULTILOGGED).getFluid() != Fluids.EMPTY) {
			ItemStack stack = player.getItemInHand(hand);
			//a Charm of Keeping III repairs one level of damage, anything else opens the casket
			if (stack.is(TFItems.CHARM_OF_KEEPING_3.get()) && state.getValue(BREAKAGE) > 0) {
				if (!player.getAbilities().instabuild) stack.shrink(1);
				level.setBlockAndUpdate(pos, state.setValue(BREAKAGE, state.getValue(BREAKAGE) - 1));
				level.playSound(null, pos, TFSounds.CASKET_REPAIR.get(), SoundSource.BLOCKS, 0.5F, level.getRandom().nextFloat() * 0.1F + 0.9F);
				return InteractionResult.sidedSuccess(level.isClientSide());
			}
			return this.openMenu(state, level, pos, player);
		}
		return InteractionResult.PASS;
	}

	@Override
	protected void modifyDrop(BlockState state, ItemStack stack) {
		KeepsakeCasketItem.setCasketDamage(stack, state.getValue(BREAKAGE));
	}

	@Override
	public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
		level.setBlock(pos, state.setValue(BREAKAGE, KeepsakeCasketItem.getCasketDamage(stack)), Block.UPDATE_CLIENTS);

		super.setPlacedBy(level, pos, state, placer, stack);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(BREAKAGE);
	}

	@Override
	public ItemStack getCloneItemStack(BlockGetter level, BlockPos pos, BlockState state) {
		ItemStack stack = super.getCloneItemStack(level, pos, state);
		KeepsakeCasketItem.setCasketDamage(stack, state.getValue(BREAKAGE));
		return stack;
	}
}
