package twilightforest.asmhooks;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.custom.TravellersModifiersManager;

/**
 * Block hooks of the Traveller's Boots Unrestrained modifier, called from {@code META-INF/asm/travellers_boots.js}.
 * Backport of the Unrestrained part of 4.8's BlockHooks.
 */
@SuppressWarnings("unused")
public final class BlockHooks {

	private BlockHooks() {
	}

	/**
	 * Unrestrained: ice, slime and other slippery blocks give the wearer the friction of an ordinary block.
	 * <p>
	 * Injection Point (before every return, 4.8 UnrestrainedFrictionTransformer):<br/>
	 * {@code net.minecraftforge.common.extensions.IForgeBlock#getFriction(BlockState, LevelReader, BlockPos, Entity)}
	 */
	public static float resetBlockFrictionWithUnrestrained(float o, @Nullable Entity entity) {
		return TravellersModifiersManager.isModifierActive(entity, TravellersModifiersManager.UNRESTRAINED_MODIFIER) ? 0.6F : o;
	}

	/**
	 * Unrestrained: walking over a slime block does not slow the wearer down.
	 * <p>
	 * Injection Point (after {@code entity.isSteppingCarefully()}, 4.8 SlimeBlockMomentumTransformer):<br/>
	 * {@code net.minecraft.world.level.block.SlimeBlock#stepOn(Level, BlockPos, BlockState, Entity)}
	 */
	public static boolean resetSlimeMomentumWithUnrestrained(boolean o, Entity entity) {
		if (TravellersModifiersManager.isModifierActive(entity, TravellersModifiersManager.UNRESTRAINED_MODIFIER)) {
			return true; //dont return false here as the original check is looking that an entity is NOT stepping carefully
		}
		return o;
	}

	/**
	 * Unrestrained: a slime block does not bounce the wearer back up from a slow landing, which would make them jitter.
	 * <p>
	 * Injection Point (before the first {@code entity.getDeltaMovement()}, 4.8 SlimeBlockBounceUpTransformer):<br/>
	 * {@code net.minecraft.world.level.block.SlimeBlock#bounceUp(Entity)}
	 */
	public static void stopBouncing(Entity entity) {
		if (TravellersModifiersManager.isModifierActive(entity, TravellersModifiersManager.UNRESTRAINED_MODIFIER) && entity.getDeltaMovement().y() > -0.08)
			entity.setDeltaMovement(new Vec3(entity.getDeltaMovement().x, Math.max(0, entity.getDeltaMovement().y), entity.getDeltaMovement().z));
	}
}
