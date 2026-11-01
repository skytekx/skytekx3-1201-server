package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import twilightforest.block.entity.ReactorDebrisBlockEntity;
import twilightforest.init.TFBlockEntities;

/**
 * Debris left by an exploding Carminite Reactor. Like the 1.7 version it shows up as a randomly sized chunk of
 * netherrack, bedrock, obsidian or nether portal; the size and the per-face textures live in the block entity and
 * are drawn by its renderer (the block itself has no rendered model).
 */
public class ReactorDebrisBlock extends BaseEntityBlock {

	public ReactorDebrisBlock(Properties properties) {
		super(properties);
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		if (level.getBlockEntity(pos) instanceof ReactorDebrisBlockEntity reactorDebrisBlockEntity)
			return reactorDebrisBlockEntity.shape;
		return ReactorDebrisBlockEntity.calculateVoxelShape();
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		return Shapes.block();
	}

	@Override
	public void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
		if (!level.isClientSide() && level.getBlockEntity(pos) instanceof ReactorDebrisBlockEntity blockEntity) {  //  blockEntity should always be
			blockEntity.randomizeDimensions();
			blockEntity.randomizeTextures();
		}
	}

	/**
	 * The block entity now removes the debris after 3 seconds. This scheduled tick only still fires for debris that
	 * was placed before the block entity existed and saved with a pending tick.
	 */
	@Override
	public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (state.is(this))
			level.destroyBlock(pos, false);
	}

	// Fork-only savegame shim: 4.3 debris was a plain block, and debris from before the scheduled-tick fix (#1948) never
	// went away. Loaded without a block entity it would now be invisible but still solid, so random ticks clear any
	// debris that has no block entity. Debris with one (everything placed from now on) is left to its block entity.
	@Override
	public boolean isRandomlyTicking(BlockState state) {
		return true;
	}

	@Override
	public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
		if (level.getChunkAt(pos).getBlockEntity(pos, LevelChunk.EntityCreationType.CHECK) == null)
			level.destroyBlock(pos, false);
	}

	@Nullable
	@Override
	public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
		return createTickerHelper(type, TFBlockEntities.REACTOR_DEBRIS.get(), ReactorDebrisBlockEntity::tick);
	}

	@Nullable
	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new ReactorDebrisBlockEntity(pos, state);
	}

	@Override
	public boolean addLandingEffects(BlockState state1, ServerLevel level, BlockPos pos, BlockState state2, LivingEntity entity, int numberOfParticles) {
		return true;  // Actually disables landing particle effects after fall.
	}
}
