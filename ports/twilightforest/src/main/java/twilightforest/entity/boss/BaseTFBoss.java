package twilightforest.entity.boss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.fluids.FluidType;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFConfig;
import twilightforest.advancements.TFAdvancements;
import twilightforest.entity.EnforcedHomePoint;
import twilightforest.entity.boss.bar.ServerTFBossBar;
import twilightforest.loot.TFLootTables;
import twilightforest.network.TFPacketHandler;
import twilightforest.network.UpdateDeathTimePacket;
import twilightforest.util.EntityUtil;
import twilightforest.util.LandmarkUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Common base of the Twilight Forest bosses (backported from TF 4.8). It owns the logic 4.3 copied into every boss: the home point,
 * the peaceful despawn back into a boss spawner, the loot chest buffer, marking the home structure as conquered and the boss bar.
 * <p>
 * The boss bar is a {@link ServerTFBossBar}, so each boss picks an RGB colour in {@link #getBossBarColor()}.
 * <p>
 * Death animations play on the client in {@link #tickDeathAnimation()}, driven by the vanilla {@link #deathTime} counter, which
 * {@link UpdateDeathTimePacket} syncs to players that start tracking a boss that is already dying.
 * <p>
 * The NBT keys are the ones 4.3 already used ("HomePos" and the "Items" death inventory), so existing bosses load unchanged.
 */
public abstract class BaseTFBoss extends Monster implements IBossLootBuffer, EnforcedHomePoint {
	private static final EntityDataAccessor<Optional<GlobalPos>> HOME_POINT = SynchedEntityData.defineId(BaseTFBoss.class, EntityDataSerializers.OPTIONAL_GLOBAL_POS);

	private final ServerTFBossBar bossEvent;
	private final NonNullList<ItemStack> dyingInventory = NonNullList.withSize(IBossLootBuffer.CONTAINER_SIZE, ItemStack.EMPTY);
	// Players credited with the hurt_boss trigger when this boss dies. Like the per-boss lists 4.3 kept, it is not saved.
	private final List<ServerPlayer> hurtBy = new ArrayList<>();
	private boolean lootAlreadyPlaced; // 4.3 boss saved mid-death: its chest was already placed and filled in die()

	protected BaseTFBoss(EntityType<? extends Monster> type, Level level) {
		super(type, level);
		this.bossEvent = this.createBossBar();
	}

	public abstract ResourceKey<Structure> getHomeStructure();

	public abstract Block getDeathContainer(RandomSource random);

	public abstract Block getBossSpawner();

	/**
	 * Whether this boss buffers its loot, deposits it into a chest and marks its home structure as conquered when it dies.
	 * The doMobLoot gamerule is checked by {@link IBossLootBuffer} itself, so a fight with mob loot disabled still counts as a
	 * conquest.
	 */
	protected boolean shouldSpawnLoot() {
		return true;
	}

	protected boolean shouldCreateSpawner() {
		return true;
	}

	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
		this.getEntityData().define(HOME_POINT, Optional.empty());
	}

	@Override
	public void startSeenByPlayer(ServerPlayer player) {
		super.startSeenByPlayer(player);
		this.getBossBar().addPlayer(player);
		this.syncDeathTime(player);
	}

	/**
	 * Death animations run on the client off {@link #deathTime}, so a player that starts tracking a boss mid-animation needs the
	 * server's timer.
	 */
	protected void syncDeathTime(ServerPlayer player) {
		if (this.deathTime > 0) {
			TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new UpdateDeathTimePacket(this.getId(), this.deathTime));
		}
	}

	@Override
	public void stopSeenByPlayer(ServerPlayer player) {
		super.stopSeenByPlayer(player);
		this.getBossBar().removePlayer(player);
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		this.saveHomePointToNbt(compound);
		this.addDeathItemsSaveData(compound);
		super.addAdditionalSaveData(compound);
	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		// ContainerHelper.saveAllItems always writes "Items" (even when empty), so only a dying boss saved by 4.3's Hydra, Alpha Yeti,
		// Snow Queen, Minoshroom or Plateau boss lacks it. Those placed and filled their chest in die(); depositing the empty buffer
		// into that chest on removal would wipe the loot the player has not collected yet.
		this.lootAlreadyPlaced = this.isDeadOrDying() && !compound.contains("Items", Tag.TAG_LIST);
		this.readDeathItemsSaveData(compound);
		this.loadHomePointFromNbt(compound);
	}

	@Override
	public void lavaHurt() {
		if (!this.fireImmune()) {
			this.setSecondsOnFire(5);
			if (this.hurt(this.damageSources().lava(), 4.0F)) {
				this.playSound(SoundEvents.GENERIC_BURN, 0.4F, 2.0F + this.getRandom().nextFloat() * 0.4F);
				EntityUtil.killLavaAround(this);
			}
		}
	}

	@Override
	protected void actuallyHurt(DamageSource source, float amount) {
		float healthBefore = this.getHealth();
		super.actuallyHurt(source, amount);
		// only credit damage that went through (not shield-blocked or cancelled). actuallyHurt runs before die(), so the player
		// landing the killing blow is credited as well
		if (this.getHealth() < healthBefore && source.getEntity() instanceof ServerPlayer player && !this.hurtBy.contains(player)) {
			this.hurtBy.add(player);
		}
	}

	protected List<ServerPlayer> getPlayersWhoHurtMe() {
		return this.hurtBy;
	}

	@Override
	public void die(DamageSource cause) {
		super.die(cause);
		if (this.shouldSpawnLoot() && this.level() instanceof ServerLevel server) this.postmortem(server, cause);
	}

	// mark the boss structure as conquered, separate method, so it can be overridden
	protected void postmortem(ServerLevel serverLevel, DamageSource cause) {
		this.getBossBar().setProgress(0.0F);
		IBossLootBuffer.saveDropsIntoBoss(this, TFLootTables.createLootParams(this, true, cause).create(LootContextParamSets.ENTITY), serverLevel);
		LandmarkUtil.markStructureConquered(serverLevel, this, this.getHomeStructure(), true);
		for (ServerPlayer player : this.getPlayersWhoHurtMe()) {
			TFAdvancements.HURT_BOSS.trigger(player, this);
		}
	}

	@Override
	public void remove(RemovalReason reason) {
		if (this.level() instanceof ServerLevel serverLevel) this.postRemoval(serverLevel, reason);
		super.remove(reason);
	}

	// drop loot into a chest after removal, separate method, so it can be overridden
	protected void postRemoval(ServerLevel serverLevel, RemovalReason reason) {
		if (reason.equals(RemovalReason.KILLED) && this.shouldSpawnLoot() && !this.lootAlreadyPlaced) {
			BlockState chest = this.getDeathContainer(this.getRandom()).defaultBlockState();
			if (chest.hasProperty(ChestBlock.FACING)) {
				chest = chest.setValue(ChestBlock.FACING, Direction.Plane.HORIZONTAL.getRandomDirection(this.level().getRandom()));
			}
			IBossLootBuffer.depositDropsIntoChest(this, chest, EntityUtil.bossChestLocation(this), serverLevel);
		}
	}

	@Override
	public void checkDespawn() {
		if (this.level().getDifficulty() == Difficulty.PEACEFUL) {
			if (this.shouldCreateSpawner() && this.isRestrictionPointValid(this.level().dimension()) && this.level().isLoaded(Objects.requireNonNull(this.getRestrictionPoint()).pos())) {
				this.placeSpawner(this.getRestrictionPoint().pos());
			}
			this.discard();
		} else {
			super.checkDespawn();
		}
	}

	//Separate method, cuz Lich needs it
	public void placeSpawner(BlockPos pos) {
		this.level().setBlockAndUpdate(pos, this.getBossSpawner().defaultBlockState());
	}

	@Override
	protected boolean shouldDespawnInPeaceful() {
		return true;
	}

	@Override
	protected boolean shouldDropLoot() {
		return !TFConfig.COMMON_CONFIG.bossDropChests.get();
	}

	@Override
	public boolean removeWhenFarAway(double distance) {
		return false;
	}

	@Override
	protected boolean canRide(Entity entity) {
		return false;
	}

	@Override
	public boolean isPushedByFluid(FluidType type) {
		return false;
	}

	@Override
	protected float getWaterSlowDown() {
		return 1.0F;
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public @Nullable GlobalPos getRestrictionPoint() {
		return this.getEntityData().get(HOME_POINT).orElse(null);
	}

	@Override
	public void setRestrictionPoint(@Nullable GlobalPos pos) {
		this.getEntityData().set(HOME_POINT, Optional.ofNullable(pos));
	}

	@Override
	public NonNullList<ItemStack> getItemStacks() {
		return this.dyingInventory;
	}

	@Override
	protected void tickDeath() {
		this.deathTime++;
		if (!this.isRemoved()) {
			if (!this.level().isClientSide()) {
				if (this.isDeathAnimationFinished()) {
					this.level().broadcastEntityEvent(this, (byte) 60); // makePoofParticles()
					this.remove(RemovalReason.KILLED);
				} else this.tickBossBar();
			} else this.tickDeathAnimation();
		}
	}

	public boolean isDeathAnimationFinished() {
		return this.deathTime >= 20;
	}

	/**
	 * Called every tick on the client while the boss is dying. Only client-side effects (particles, local sounds) belong here,
	 * and no client-only class may be referenced, as this class is loaded on dedicated servers.
	 */
	public void tickDeathAnimation() {

	}

	public ServerTFBossBar getBossBar() {
		return this.bossEvent;
	}

	@Override
	protected void customServerAiStep() {
		super.customServerAiStep();
		if (!this.level().isClientSide()) this.tickBossBar();
	}

	protected void tickBossBar() {
		this.getBossBar().setProgress(this.getHealth() / this.getMaxHealth());
	}

	protected ServerTFBossBar createBossBar() {
		return new ServerTFBossBar(this.getBossBarTitle(), this.getBossBarColor(), this.getBossBarOverlay());
	}

	public Component getBossBarTitle() {
		return this.getDisplayName();
	}

	/**
	 * The RGB colour of the boss bar. A boss that changes it mid-fight sends the new style with
	 * {@link ServerTFBossBar#updateStyle}.
	 */
	public abstract int getBossBarColor();

	public BossEvent.BossBarOverlay getBossBarOverlay() {
		return BossEvent.BossBarOverlay.PROGRESS;
	}

	@Override
	public void setCustomName(@Nullable Component name) {
		super.setCustomName(name);
		this.getBossBar().setName(this.getBossBarTitle());
	}
}
