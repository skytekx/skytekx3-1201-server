package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFSounds;

import java.util.function.BooleanSupplier;

/**
 * Base of the firefly, cicada and mason jars. The jar and its lid are drawn by
 * {@link twilightforest.client.renderer.tileentity.JarRenderer}, so the block itself renders nothing
 * ({@link BaseEntityBlock} defaults to an invisible render shape). Clicking the top of the jar with a registered log
 * swaps the lid; clicking it with empty hands makes it wobble.
 */
public class JarBlock extends BaseEntityBlock implements SimpleWaterloggedBlock {
	private static final VoxelShape JAR = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 14.0D, 13.0D);
	private static final VoxelShape LID = Block.box(4.0D, 14.0D, 4.0D, 12.0D, 16.0D, 12.0D);
	private static final VoxelShape AABB = Shapes.or(JAR, LID);
	private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

	public JarBlock(BlockBehaviour.Properties properties) {
		super(properties);
		this.registerDefaultState(this.getStateDefinition().any().setValue(WATERLOGGED, false));
	}

	@Override
	@Deprecated
	public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
		return AABB;
	}

	@Override
	@Deprecated
	public FluidState getFluidState(BlockState state) {
		return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		BlockState superState = super.getStateForPlacement(context);
		return superState == null ? null : superState.setValue(WATERLOGGED, context.getLevel().getFluidState(context.getClickedPos()).is(Fluids.WATER));
	}

	@Override
	@Deprecated
	public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor accessor, BlockPos currentPos, BlockPos facingPos) {
		if (state.getValue(WATERLOGGED)) accessor.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(accessor));
		return super.updateShape(state, facing, facingState, accessor, currentPos, facingPos);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		builder.add(WATERLOGGED);
	}

	public Item getDefaultLid() {
		return TFBlocks.TWILIGHT_OAK_LOG.get().asItem();
	}

	@Override
	public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter level, BlockPos pos, Player player) {
		return level.getBlockEntity(pos) instanceof JarBlockEntity jarBlockEntity
				? jarBlockEntity.getJarAsItem()
				: super.getCloneItemStack(state, target, level, pos, player);
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new JarBlockEntity(pos, state);
	}

	/**
	 * 1.20.1 has a single {@code use} hook, called once per hand. It runs {@link #useItemOn} first and falls back to
	 * {@link #useWithoutItem} for the main hand, the order 1.20.5+ uses for its two hooks.
	 */
	@Override
	@Deprecated
	public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		InteractionResult result = this.useItemOn(player.getItemInHand(hand), state, level, pos, player, hand, hitResult);
		if (result != null) return result;
		return hand == InteractionHand.MAIN_HAND ? this.useWithoutItem(state, level, pos, player, hitResult) : InteractionResult.PASS;
	}

	/**
	 * @return the result, or {@code null} to fall through to {@link #useWithoutItem}
	 */
	@Nullable
	protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
		if (level.getBlockEntity(pos) instanceof JarBlockEntity jarBlockEntity && hitResult.getLocation().y >= pos.getY() + (14.0D / 16.0D)) {
			Item lid = stack.getItem();
			BooleanSupplier check = JarBlockEntity.REGISTERED_LOG_LIDS.get(lid);
			if (lid != jarBlockEntity.lid && check != null && check.getAsBoolean()) {
				jarBlockEntity.lid = lid;
				if (level instanceof ServerLevel serverLevel) {
					serverLevel.playSound(null, pos, TFSounds.JAR_LID_SWAP.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
					jarBlockEntity.wobble(JarBlockEntity.WobbleStyle.POSITIVE);
					jarBlockEntity.setChanged();
					// Unlike 4.8, also send the new lid to the other players watching the jar
					serverLevel.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
				}
				return InteractionResult.sidedSuccess(level.isClientSide());
			}
		}
		return null;
	}

	protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
		if (level.getBlockEntity(pos) instanceof JarBlockEntity blockEntity && player.getItemInHand(InteractionHand.MAIN_HAND).isEmpty() && player.getItemInHand(InteractionHand.OFF_HAND).isEmpty()) {
			if (level instanceof ServerLevel serverLevel) {
				serverLevel.playSound(null, pos, TFSounds.JAR_WIGGLE.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
				blockEntity.wobble(JarBlockEntity.WobbleStyle.NEGATIVE);
			}
			return InteractionResult.sidedSuccess(level.isClientSide());
		} else {
			return InteractionResult.PASS;
		}
	}
}
