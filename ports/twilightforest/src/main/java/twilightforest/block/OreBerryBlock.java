package twilightforest.block;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFDamageTypes;

/**
 * Underground bush that grows metal (or essence) berries. It only survives on {@link BlockTagGenerator#OREBERRY_BUSHES_SURVIVE}
 * in darkness, unless it survives in light, and it pricks every non-item entity that touches it.
 */
public class OreBerryBlock extends TFBushBlock {
	// Slightly smaller than a full block, so an entity that walks into or stands on a grown bush overlaps its block and gets pricked
	protected static final VoxelShape ENTITY_COLLISION_SHAPE = Block.box(0.001D, 0.0D, 0.001D, 15.999D, 15.999D, 15.999D);
	protected final boolean surviveInLight;

	public OreBerryBlock(boolean surviveInLight, ResourceLocation berryTable, Properties properties) {
		super(berryTable, properties);
		this.surviveInLight = surviveInLight;
	}

	@Override
	public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
		if (!(entity instanceof ItemEntity)) {
			entity.hurt(TFDamageTypes.getDamageSource(level, TFDamageTypes.OREBERRY), 1.0F);
		}
		super.entityInside(state, level, pos, entity);
	}

	@Override
	public VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
		VoxelShape shape = super.getCollisionShape(state, level, pos, context);
		// Use the ENTITY_COLLISION_SHAPE for entities when the bush is fully grown; keep the normal shape for non-entity checks.
		if (state.getValue(AGE) >= MAX_AGE - 1 && !context.equals(CollisionContext.empty())) {
			return ENTITY_COLLISION_SHAPE;
		}
		return shape;
	}

	@Override
	public @Nullable BlockPathTypes getBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @Nullable Mob mob) {
		return BlockPathTypes.DANGER_OTHER;
	}

	@Override
	public boolean canBePlacedAt(BlockState state) {
		return state.is(BlockTagGenerator.OREBERRY_BUSHES_SURVIVE);
	}

	@Override
	public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
		return super.canSurvive(state, level, pos) && (this.surviveInLight || level.getRawBrightness(pos, 0) < 13);
	}

	@Override
	protected boolean canGrowAt(BlockState state, LevelReader level, BlockPos pos) {
		return super.canGrowAt(state, level, pos) && (this.surviveInLight || level.getRawBrightness(pos, 0) < 10);
	}

	@Override
	public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
		return false;
	}
}
