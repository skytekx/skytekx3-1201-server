package twilightforest.block.entity.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.ForgeEventFactory;
import twilightforest.entity.boss.Lich;
import twilightforest.init.TFBlockEntities;
import twilightforest.init.TFEntities;
import twilightforest.init.TFParticleType;

public class LichSpawnerBlockEntity extends BossSpawnerBlockEntity<Lich> {

	public LichSpawnerBlockEntity(BlockPos pos, BlockState state) {
		super(TFBlockEntities.LICH_SPAWNER.get(), TFEntities.LICH.get(), pos, state);
	}

	@Override
	public boolean anyPlayerInRange() {
		Player closestPlayer = this.getLevel().getNearestPlayer(this.getBlockPos().getX() + 0.5D, this.getBlockPos().getY() + 0.5D, this.getBlockPos().getZ() + 0.5D, this.getRange(), false);
		return closestPlayer != null && closestPlayer.getY() > this.getBlockPos().getY() - 4;
	}

	/**
	 * Spawns the Lich standing on the floor below a spawner that floats above it (4.8 3e5ad54dc), with its home one block
	 * above that floor spot so the boss chest lands where it stood (3c7c7576d). A spawner in a 4.3 tower floats one block
	 * over the floor, so the Lich's home there is still the spawner position.
	 */
	@Override
	protected boolean spawnMyBoss(ServerLevelAccessor accessor) {
		Lich myCreature = this.makeMyCreature();

		BlockPos.MutableBlockPos mutableBlockPos = this.getBlockPos().mutable();
		while (true) {
			if (accessor.getMinBuildHeight() >= mutableBlockPos.getY()) break;
			if (accessor.getBlockState(mutableBlockPos.below()).isAir()) {
				mutableBlockPos.move(Direction.DOWN);
			} else break;
		}

		mutableBlockPos.move(Direction.UP);

		myCreature.moveTo(mutableBlockPos, accessor.getLevel().getRandom().nextFloat() * 360F, 0.0F);
		ForgeEventFactory.onFinalizeSpawn(myCreature, accessor, accessor.getCurrentDifficultyAt(mutableBlockPos), MobSpawnType.SPAWNER, null, null);
		myCreature.setAttackCooldown(40);
		myCreature.setExtinguishTimer();

		// set creature's home to this
		myCreature.setRestrictionPoint(GlobalPos.of(myCreature.level().dimension(), mutableBlockPos.immutable()));

		// spawn it
		return accessor.addFreshEntity(myCreature);
	}

	@Override
	public ParticleOptions getSpawnerParticle() {
		return TFParticleType.OMINOUS_FLAME.get();
	}
}
