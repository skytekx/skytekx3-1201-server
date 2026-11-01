package twilightforest.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ThrowableProjectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DecoratedPotBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.pattern.BlockInWorld;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;
import org.jetbrains.annotations.Nullable;
import twilightforest.data.tags.BlockTagGenerator;
import twilightforest.init.TFDamageTypes;
import twilightforest.init.TFEnchantments;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.util.WorldUtil;

public class ChainBlock extends ThrowableProjectile implements IEntityAdditionalSpawnData {

	private static final int MAX_SMASH = 12;
	private static final int MAX_STUCK_TICKS = 100;
	private static final int MAX_CHAIN = 16;

	private static final EntityDataAccessor<Boolean> HAND = SynchedEntityData.defineId(ChainBlock.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> IS_FOIL = SynchedEntityData.defineId(ChainBlock.class, EntityDataSerializers.BOOLEAN);
	private static final EntityDataAccessor<Boolean> IS_RETURNING = SynchedEntityData.defineId(ChainBlock.class, EntityDataSerializers.BOOLEAN);
	private boolean canSmashBlocks;
	private boolean hitEntity = false;
	private int stuckTime;
	@Nullable
	private ItemStack stack;
	private int blocksSmashed = 0;
	private double velX;
	private double velY;
	private double velZ;

	public ChainBlock(EntityType<? extends ChainBlock> type, Level world) {
		super(type, world);
	}

	public ChainBlock(EntityType<? extends ChainBlock> type, Level world, LivingEntity thrower, InteractionHand hand, ItemStack stack) {
		super(type, thrower, world);
		this.canSmashBlocks = EnchantmentHelper.getTagEnchantmentLevel(TFEnchantments.DESTRUCTION.get(), stack) > 0 && !thrower.hasEffect(MobEffects.DIG_SLOWDOWN);
		this.stack = stack;
		this.setHand(hand);
		this.shootFromRotation(thrower, thrower.getXRot(), thrower.getYRot(), 0.0F, 1.5F, 1.0F);
		this.getEntityData().set(IS_FOIL, stack.hasFoil());
	}

	@Override
	public AABB getBoundingBoxForCulling() {
		// Keep rendering the block and its chain while the thrower is on screen, even if the block itself is not
		if (this.getOwner() != null) {
			AABB dis = super.getBoundingBoxForCulling();
			AABB owner = this.getOwner().getBoundingBoxForCulling();
			return dis.minmax(owner);
		}
		return super.getBoundingBoxForCulling();
	}

	private void setHand(InteractionHand hand) {
		this.getEntityData().set(HAND, hand == InteractionHand.MAIN_HAND);
	}

	public InteractionHand getHand() {
		return this.getEntityData().get(HAND) ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
	}

	public boolean isFoil() {
		return this.getEntityData().get(IS_FOIL);
	}

	public void setIsReturning(boolean returning) {
		this.getEntityData().set(IS_RETURNING, returning);
	}

	public boolean isReturning() {
		return this.getEntityData().get(IS_RETURNING);
	}

	@Override
	public boolean canChangeDimensions() {
		return false;
	}

	@Override
	public void shoot(double x, double y, double z, float speed, float accuracy) {
		super.shoot(x, y, z, speed, accuracy);

		// save velocity
		this.velX = this.getDeltaMovement().x();
		this.velY = this.getDeltaMovement().y();
		this.velZ = this.getDeltaMovement().z();
	}

	@Override
	protected void updateRotation() {
		Vec3 vec3 = this.getDeltaMovement();
		if (this.isReturning() && this.getOwner() instanceof LivingEntity living) {
			// Use this vec3 when returning. It's supposed to be pulled, so turning to face you doesn't make sense
			vec3 = vec3.normalize().scale(-1.0D).lerp(this.getEyePosition().subtract(living.getEyePosition()).normalize(), 0.5);
		}
		double d0 = vec3.horizontalDistance();
		this.setXRot(lerpRotation(this.xRotO, (float) (Mth.atan2(vec3.y, d0) * 180.0F / (float) Math.PI)));
		this.setYRot(lerpRotation(this.yRotO, (float) (Mth.atan2(vec3.x, vec3.z) * 180.0F / (float) Math.PI)));
	}

	@Override
	protected float getGravity() {
		return 0.05F;
	}

	@Override
	protected void onHitEntity(EntityHitResult result) {
		super.onHitEntity(result);
		// only hit living things & inside the world border
		Level level = this.level();
		if (!level.isClientSide() && result.getEntity() != this.getOwner() && level.getWorldBorder().isWithinBounds(result.getEntity().blockPosition())) {
			float damage = 0.0F;
			if (result.getEntity() instanceof LivingEntity living) {
				damage = 10 + this.getDamageBonus(living);
			} else if (result.getEntity() instanceof PartEntity<?> part && part.getParent() instanceof LivingEntity living) {
				damage = 10 + this.getDamageBonus(living);
			}

			//properly disable shields
			if (result.getEntity() instanceof Player player && player.isUsingItem() && player.getUseItem().canPerformAction(ToolActions.SHIELD_BLOCK)) {
				player.getUseItem().hurtAndBreak(5, player, event -> event.broadcastBreakEvent(player.getUsedItemHand()));
				player.disableShield(true);
			}

			if (damage > 0.0F) {
				if (result.getEntity().hurt(TFDamageTypes.getIndirectEntityDamageSource(level, TFDamageTypes.SPIKED, this, this.getOwner()), damage)) {
					this.playSound(TFSounds.BLOCK_AND_CHAIN_HIT.get(), 1.0f, this.random.nextFloat());
					// age when we hit a monster so that we go back to the player faster
					this.hitEntity = true;
					this.setIsReturning(true);
					this.tickCount += 60;
					if (this.stack != null && this.getOwner() instanceof LivingEntity living) {
						this.stack.hurtAndBreak(1, living, user -> user.broadcastBreakEvent(this.getHand()));
					}
				}
			}
		}
	}

	private float getDamageBonus(LivingEntity target) {
		return this.stack != null ? EnchantmentHelper.getDamageBonus(this.stack, target.getMobType()) : 0.0F;
	}

	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		BlockPos pos = result.getBlockPos();
		if (this.level() instanceof ServerLevel level && this.stack != null) {
			boolean restrictedPlaceMode = this.getOwner() instanceof ServerPlayer player && player.gameMode.getGameModeForPlayer().isBlockPlacingRestricted();
			BlockState state = level.getBlockState(pos);
			if (!state.isAir() && !this.smashDecoratedPot(level, pos, state, restrictedPlaceMode)) {
				// bounce off anything we may not break, and off everything once we have smashed enough
				if (!canBreakBlockAt(level, pos, state, this.stack, restrictedPlaceMode) || this.blocksSmashed >= MAX_SMASH) {
					this.bounce(result.getDirection());
				}

				if (this.canSmashBlocks) {
					// demolish some blocks
					AABB aabb = this.getBoundingBox().inflate(0.25D);
					//I don't know why, and I can't find out why, but we have to subtract the coordinates by 0.5 here.
					//Otherwise, it will sometimes not break blocks when hitting west, and always break too many when hitting east.
					this.affectBlocksInAABB(level, aabb.move(result.getLocation().subtract(aabb.getCenter()).add(-0.5D, 0D, 0D)));
				}
			}
		}
	}

	public void bounce(Direction direction) {
		if (!this.isReturning() && !this.hitEntity) {
			this.playSound(TFSounds.BLOCK_AND_CHAIN_COLLIDE.get(), 0.125F, this.random.nextFloat());
			this.gameEvent(GameEvent.HIT_GROUND);
		}

		this.setIsReturning(true);

		// riccochet
		double bounce = 0.6;
		this.velX *= bounce;
		this.velY *= bounce;
		this.velZ *= bounce;


		switch (direction) {
			case DOWN:
				if (this.velY > 0) {
					this.velY *= -bounce;
				}
				break;
			case UP:
				if (this.velY < 0) {
					this.velY *= -bounce;
				}
				break;
			case NORTH:
				if (this.velZ > 0) {
					this.velZ *= -bounce;
				}
				break;
			case SOUTH:
				if (this.velZ < 0) {
					this.velZ *= -bounce;
				}
				break;
			case WEST:
				if (this.velX > 0) {
					this.velX *= -bounce;
				}
				break;
			case EAST:
				if (this.velX < 0) {
					this.velX *= -bounce;
				}
				break;
		}
	}

	public static boolean canBreakBlockAt(Level level, BlockPos pos, BlockState state, ItemStack stack, boolean restrictedPlaceMode) {
		return level.getWorldBorder().isWithinBounds(pos) && stack.isCorrectToolForDrops(state) && !state.is(BlockTagGenerator.BLOCK_AND_CHAIN_NEVER_BREAKS)
				&& (!restrictedPlaceMode || stack.hasAdventureModeBreakTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK), new BlockInWorld(level, pos, false)));
	}

	/**
	 * 4.8 shatters decorated pots through vanilla's projectile handling (chain_block is in #minecraft:impact_projectiles, #2108).
	 * 1.20.1 pots ignore projectiles, so this mirrors 1.20.3's DecoratedPotBlock.onProjectileHit for the block and chain.
	 * The 1.20.1 pot loot table drops the sherds because the block and chain is in #minecraft:breaks_decorated_pots.
	 */
	private boolean smashDecoratedPot(ServerLevel level, BlockPos pos, BlockState state, boolean restrictedPlaceMode) {
		if (this.stack == null) return false;
		if (state.getBlock() instanceof DecoratedPotBlock && state.hasProperty(BlockStateProperties.CRACKED)
				&& this.mayInteract(level, pos) && level.getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
				&& (!restrictedPlaceMode || this.stack.hasAdventureModeBreakTagForBlock(level.registryAccess().registryOrThrow(Registries.BLOCK), new BlockInWorld(level, pos, false)))) {
			BlockState cracked = state.setValue(BlockStateProperties.CRACKED, true);
			level.setBlock(pos, cracked, Block.UPDATE_INVISIBLE);
			Block.dropResources(cracked, level, pos, level.getBlockEntity(pos), this, this.stack);
			level.destroyBlock(pos, false, this);
			return true;
		}
		return false;
	}

	private void affectBlocksInAABB(ServerLevel level, AABB box) {
		if (this.stack != null && this.getOwner() instanceof ServerPlayer player) {
			boolean creative = player.getAbilities().instabuild;
			boolean restrictedPlaceMode = player.gameMode.getGameModeForPlayer().isBlockPlacingRestricted();

			for (BlockPos pos : WorldUtil.getAllInBB(box)) {
				if (this.blocksSmashed >= MAX_SMASH) {
					break;
				}

				BlockState state = level.getBlockState(pos);
				Block block = state.getBlock();

				if (!state.isAir() && canBreakBlockAt(level, pos, state, this.stack, restrictedPlaceMode) && block.canEntityDestroy(state, level, pos, this)) {
					if (!MinecraftForge.EVENT_BUS.post(new BlockEvent.BreakEvent(level, pos, state, player))) {
						if (ForgeEventFactory.doPlayerHarvestCheck(player, state, !state.requiresCorrectToolForDrops() || player.getItemInHand(this.getHand()).isCorrectToolForDrops(state))) {
							level.destroyBlock(pos, false);
							if (!creative) block.playerDestroy(level, player, pos, state, level.getBlockEntity(pos), player.getItemInHand(this.getHand()));
							this.blocksSmashed++;
						}
					}
				}
			}
		}
	}

	@Override
	public void tick() {
		super.tick();

		if (!this.level().isClientSide()) {
			if (this.getOwner() == null) {
				this.discard();
			} else {
				// return if we stopped moving, e.g. wedged between blocks
				if (!this.isReturning()) {
					if (this.xOld != this.getX() || this.zOld != this.getZ()) {
						double d0 = Math.abs(this.getX() - this.xOld);
						double d1 = Math.abs(this.getZ() - this.zOld);
						if (d0 < 0.003F && d1 < 0.003F) {
							this.stuckTime++;
						}
					}

					if (this.stuckTime >= MAX_STUCK_TICKS) {
						this.setIsReturning(true);
					}
				}

				double distToPlayer = this.distanceTo(this.getOwner());
				// return if far enough away
				if (!this.isReturning() && distToPlayer > MAX_CHAIN) {
					this.setIsReturning(true);
				}

				if (this.isReturning()) {
					// despawn if close enough
					if (distToPlayer < 2F) {
						if (this.stack != null && this.getOwner() instanceof LivingEntity living && this.blocksSmashed > 0) {
							this.stack.hurtAndBreak(Math.min(this.blocksSmashed, 3), living, user -> user.broadcastBreakEvent(this.getHand()));
						}
						this.discard();
					}

					LivingEntity returnTo = (LivingEntity) this.getOwner();

					Vec3 back = new Vec3(returnTo.getX(), returnTo.getY() + returnTo.getEyeHeight(), returnTo.getZ()).subtract(this.position()).normalize();
					float age = Math.min(this.tickCount * 0.03F, 1.0F);

					// separate the return velocity from the normal bouncy velocity
					this.setDeltaMovement(new Vec3(
							this.velX * (1.0 - age) + (back.x() * 2F * age),
							this.velY * (1.0 - age) + (back.y() * 2F * age) - this.getGravity(),
							this.velZ * (1.0 - age) + (back.z() * 2F * age)
					));
				}
			}
		}
	}

	@Override
	protected void defineSynchedData() {
		this.getEntityData().define(HAND, true);
		this.getEntityData().define(IS_FOIL, false);
		this.getEntityData().define(IS_RETURNING, false);
	}

	@Override
	public void remove(RemovalReason reason) {
		super.remove(reason);
		LivingEntity thrower = (LivingEntity) this.getOwner();
		if (thrower != null && thrower.getUseItem().is(TFItems.BLOCK_AND_CHAIN.get())) {
			thrower.stopUsingItem();
		}
	}

	@Override
	protected void readAdditionalSaveData(CompoundTag pCompound) {
		super.readAdditionalSaveData(pCompound);
		if (pCompound.contains("BlockAndChainStack", 10)) {
			this.stack = ItemStack.of(pCompound.getCompound("BlockAndChainStack"));
		}
		this.setIsReturning(pCompound.getBoolean("IsReturning"));
	}

	@Override
	protected void addAdditionalSaveData(CompoundTag pCompound) {
		super.addAdditionalSaveData(pCompound);
		if (this.stack != null) {
			pCompound.put("BlockAndChainStack", this.stack.save(new CompoundTag()));
		}
		pCompound.putBoolean("IsReturning", this.isReturning());
	}

	@Override
	public void writeSpawnData(FriendlyByteBuf buffer) {
		buffer.writeInt(this.getOwner() != null ? this.getOwner().getId() : -1);
		buffer.writeBoolean(this.getHand() == InteractionHand.MAIN_HAND);
	}

	@Override
	public void readSpawnData(FriendlyByteBuf buf) {
		Entity e = this.level().getEntity(buf.readInt());
		if (e instanceof LivingEntity) {
			this.setOwner(e);
		}
		this.setHand(buf.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND);
	}
}
