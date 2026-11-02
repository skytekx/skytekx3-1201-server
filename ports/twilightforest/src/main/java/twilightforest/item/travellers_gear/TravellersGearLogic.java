package twilightforest.item.travellers_gear;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.server.ServerLifecycleHooks;
import twilightforest.TwilightForestMod;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.components.item.TravellersGearData;
import twilightforest.init.TFItems;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.network.ParticlePacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.network.TravellersWingsStatePacket;

/**
 * Logic of the Traveller's Gear and its modifiers. Backport of 4.8's TravellersGearLogic: the wing animation state
 * machine and the Traveller's Wings modifiers (High Jump, Gradual Glide, Sidestep, Double Jump). The other pieces'
 * modifiers come with their travellers-* features.
 */
public class TravellersGearLogic {

	/**
	 * How far below its usual safe fall distance an entity may land after a double jump without taking damage, 4.8's
	 * {@code travellers_gear.boots_double_jump_safe_fall_distance} modifier (+2 on 1.21's safe fall distance).
	 */
	public static final float DOUBLE_JUMP_SAFE_FALL_DISTANCE = 2.0F;

	public static boolean isWearingWings(LivingEntity livingEntity) {
		return TravellersArmorItem.hasPart(livingEntity.getItemBySlot(EquipmentSlot.LEGS), TravellersArmorItem.Part.WINGS);
	}

	// 1.21 Entity#isInLiquid
	public static boolean isInLiquid(LivingEntity livingEntity) {
		return livingEntity.isInWaterOrBubble() || livingEntity.isInLava();
	}

	public static void travellersWingsSidestepCooldownSound(Player player) {
		ItemStack leggingsStack = player.getItemBySlot(EquipmentSlot.LEGS);
		Long cooldown = TravellersGearData.SIDESTEP_COOLDOWN.get(leggingsStack);
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(player);
		if (cooldown == null || attachment == null)
			return;
		long dt = player.level().getGameTime() - attachment.lastSidestepTime;
		if (TravellersModifiersManager.isModifierActive(player, leggingsStack, TravellersModifiersManager.SIDESTEP_MODIFIER) && dt > cooldown && attachment.shouldPlaySideStepCooldownSound) {
			// 4.8 notifies on both sides, so the player who stepped heard the sound twice (once from the server); the client plays it alone here
			if (player.level().isClientSide())
				player.playNotifySound(TFSounds.SIDE_STEP_CHARGED.get(), player.getSoundSource(), 1F, player.getVoicePitch());
			attachment.shouldPlaySideStepCooldownSound = false;
		}
	}

	public static void travellersWingsGradualGlide(LivingEntity livingEntity) {
		ItemStack leggingsStack = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
		Float multiplier = TravellersGearData.GRADUALLY_GLIDING_MULTIPLIER.get(leggingsStack);
		Vec3 deltaMovement = livingEntity.getDeltaMovement();
		if (!TravellersModifiersManager.isModifierActive(livingEntity, leggingsStack, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER) || multiplier == null || deltaMovement.y() >= 0 || livingEntity.isFallFlying())
			return;

		if (livingEntity instanceof Player player) {
			TravellersWingsAttachment attachment = TravellersWingsAttachment.get(player);
			if (attachment == null || !attachment.isGraduallyGliding)
				return;
		}

		double newDeltaMovementY = deltaMovement.y() * multiplier;
		livingEntity.setDeltaMovement(
			deltaMovement.x(),
			newDeltaMovementY,  // works similar to minecraft air resistance
			deltaMovement.z()
		);

		// use mv ^ 2 / 2 / mg = h; 1.21's Entity#getGravity is Forge's entity gravity attribute here
		AttributeInstance gravityAttribute = livingEntity.getAttribute(ForgeMod.ENTITY_GRAVITY.get());
		double gravity = livingEntity.isNoGravity() || gravityAttribute == null ? 0 : gravityAttribute.getValue();
		if (gravity > 0)
			livingEntity.fallDistance = (float) (Math.pow(newDeltaMovementY, 2) / 2 / gravity);
	}

	public static void travellersWingsHighJump(LivingEntity livingEntity) {
		ItemStack leggingsStack = livingEntity.getItemBySlot(EquipmentSlot.LEGS);
		Integer amplifier = TravellersGearData.HIGH_JUMP_AMPLIFIER.get(leggingsStack);
		if (TravellersModifiersManager.isModifierActive(livingEntity, leggingsStack, TravellersModifiersManager.HIGH_JUMP_ABILITY) && amplifier != null)
			livingEntity.addEffect(new MobEffectInstance(MobEffects.JUMP, 2, amplifier, false, false, false));
	}

	public static boolean tryPerformSidestep(Player player, boolean isLeftSidestep) {
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(player);
		if (attachment == null)
			return false;
		long lastSidestepTime = attachment.lastSidestepTime;
		ItemStack leggingsStack = player.getItemBySlot(EquipmentSlot.LEGS);
		Long cooldown = TravellersGearData.SIDESTEP_COOLDOWN.get(leggingsStack);
		long currentTime = player.level().getGameTime();
		if (TravellersModifiersManager.isModifierActive(player, leggingsStack, TravellersModifiersManager.SIDESTEP_MODIFIER) && cooldown != null && currentTime - lastSidestepTime > cooldown && !player.isFallFlying() && player.onGround() && !player.isCrouching()) {
			performSidestep(player, attachment, isLeftSidestep);
			attachment.lastSidestepTime = currentTime;
			attachment.shouldPlaySideStepCooldownSound = true;
			return true;
		}
		return false;
	}

	private static void performSidestep(Player player, TravellersWingsAttachment attachment, boolean isLeftSidestep) {
		float angle = player.getYRot();
		double rot = isLeftSidestep ? -Math.PI / 2 : Math.PI / 2;
		Vec3 dashDirection = new Vec3(-Math.sin(Math.toRadians(angle) + rot), 0, Math.cos(Math.toRadians(angle) + rot)).scale(1.6);  // 5 blocks
		player.push(dashDirection.x(), dashDirection.y(), dashDirection.z());
		player.playSound(TFSounds.SIDE_STEP.get(), 1.0F, player.getVoicePitch());

		TravellersWingsAttachment.WingState newState = TravellersWingsAttachment.WingState.SIDESTEP;
		attachment.state = newState;
		attachment.sidestepLeft = isLeftSidestep;
		attachment.sidestepTimer = 0;

		if (player.level() instanceof ServerLevel) {
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new TravellersWingsStatePacket(player.getId(), newState, isLeftSidestep, attachment.doubleJumpTimer, attachment.sidestepTimer));
		}
	}

	public static boolean performDoubleJump(Player player) {
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(player);
		if (attachment == null || !attachment.hasDoubleJump || player.isFallFlying() || player.onClimbable() || player.onGround() || player.isSwimming() || player.getAbilities().flying || isInLiquid(player) || player.isPassenger())
			return false;
		player.jumpFromGround();
		player.resetFallDistance();
		float pitchShift = 0.1F;
		player.playSound(TFSounds.DOUBLE_JUMP.get(), 1.5F, (player.getVoicePitch() - 1) * (1 + pitchShift) + (1 - pitchShift * 0.2F));
		attachment.hasDoubleJump = false;
		attachment.doubleJumpValidator.count = 0;
		// Increase safe fall distance so the player can land up to 2 blocks below their starting height after performing a double jump at peak height without taking fall damage
		attachment.hasDoubleJumpSafeFallDistance = true;

		boolean wearsWings = player.getItemBySlot(EquipmentSlot.LEGS).is(TFItems.TRAVELLERS_WINGS.get());
		if (wearsWings) {
			attachment.state = TravellersWingsAttachment.WingState.DOUBLE_JUMP;
			attachment.doubleJumpTimer = 0;
		}

		if (player.level() instanceof ServerLevel serverLevel && wearsWings) {
			ParticlePacket particlePacket = new ParticlePacket();
			Vec3 deltaMovement = player.getDeltaMovement();
			for (int particleNumber = 0; particleNumber < 10; particleNumber++) {
				Vec3 particleVelocity = new Vec3(
					(serverLevel.random.nextDouble() - 0.5),
					serverLevel.random.nextDouble() + 1,
					(serverLevel.random.nextDouble() - 0.5)
				);
				ParticleOptions type = TFParticleType.DOUBLE_JUMP.get();
				Vec3 wingsPosition = player.position().add(Math.sin(Math.toRadians(player.yBodyRot)) / 3, 1.2, -Math.cos(Math.toRadians(player.yBodyRot)) / 3);
				particlePacket.queueParticle(type, false, wingsPosition, particleVelocity.multiply(0.25, -0.5, 0.25).add(deltaMovement));
			}
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), particlePacket);
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> player), new TravellersWingsStatePacket(player.getId(), TravellersWingsAttachment.WingState.DOUBLE_JUMP, attachment.sidestepLeft, attachment.doubleJumpTimer, attachment.sidestepTimer));
		}
		return true;
	}

	/**
	 * Counts a double jump or sidestep request the server had to reject. From the third one in a row the player is put
	 * back where the server sees them, from the sixth one they are kicked; 45 ticks without a rejection start the count
	 * over. Only on dedicated servers, as in 4.8.
	 */
	private static void validateMovement(ServerPlayer serverPlayer, TravellersWingsAttachment.MovementValidator validator, String movementType) {
		MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
		if (server == null || !server.isDedicatedServer())
			return;
		int count = validator.count;
		int lastTick = validator.lastCheck;
		int currentTick = serverPlayer.tickCount;
		int diff = currentTick - lastTick;
		TwilightForestMod.LOGGER.debug("{} {} check: count={}, lastTick={}, currentTick={}, diff={}",
			serverPlayer.getName().getString(), movementType, count, lastTick, currentTick, diff);

		if (diff >= 45 && !serverPlayer.isFallFlying()) {
			count = -1;
		}

		validator.lastCheck = currentTick;

		if (count >= 5) {
			serverPlayer.connection.disconnect(Component.translatable("multiplayer.disconnect.flying"));
			return;
		}

		validator.count = count + 1;

		if (count > 1) {
			TwilightForestMod.LOGGER.warn("{} illegal {}", serverPlayer.getName().getString(), movementType);
			// 4.8 moves the player and sends a raw position packet with teleport id 0; teleport() does both and makes the server wait for the client to accept it
			serverPlayer.connection.teleport(serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), serverPlayer.getYRot(), serverPlayer.getXRot());
		}
	}

	public static void handleSidestepAbuse(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			TravellersWingsAttachment attachment = TravellersWingsAttachment.get(serverPlayer);
			if (attachment != null)
				validateMovement(serverPlayer, attachment.sidestepValidator, "sidestep");
		}
	}

	public static void handleDoubleJumpAbuse(Player player) {
		if (player instanceof ServerPlayer serverPlayer) {
			TravellersWingsAttachment attachment = TravellersWingsAttachment.get(serverPlayer);
			if (attachment != null)
				validateMovement(serverPlayer, attachment.doubleJumpValidator, "double jump");
		}
	}

	/**
	 * Picks the wing animation of the entity for this tick and sends it to everyone tracking the entity when it changes.
	 * The double jump and sidestep animations stay locked for their whole duration once started.
	 */
	public static void determineWingState(LivingEntity livingEntity) {
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(livingEntity);
		if (attachment == null)
			return;
		TravellersWingsAttachment.WingState newState = TravellersWingsAttachment.WingState.IDLE;

		boolean isLocked = false;
		if (attachment.state == TravellersWingsAttachment.WingState.DOUBLE_JUMP) {
			attachment.doubleJumpTimer++;
			if (attachment.doubleJumpTimer < TravellersWingsAttachment.DOUBLE_JUMP_DURATION) {
				isLocked = true;
				newState = TravellersWingsAttachment.WingState.DOUBLE_JUMP;
			}
		} else if (attachment.state == TravellersWingsAttachment.WingState.SIDESTEP) {
			attachment.sidestepTimer++;
			if (attachment.sidestepTimer < TravellersWingsAttachment.SIDESTEP_DURATION) {
				isLocked = true;
				newState = attachment.state;
			}
		} else {
			attachment.doubleJumpTimer = 0;
			attachment.sidestepTimer = 0;
		}

		if (!isLocked) {
			if (livingEntity.isPassenger()) {
				newState = TravellersWingsAttachment.WingState.RIDE;
			} else if (livingEntity.isSwimming()) {
				newState = TravellersWingsAttachment.WingState.SWIM;
			} else if (!livingEntity.onGround() && !isInLiquid(livingEntity) && livingEntity.fallDistance < 2.3F && (!(livingEntity instanceof Player p) || !p.getAbilities().flying)) {
				newState = TravellersWingsAttachment.WingState.FALL_SLOW;
			} else if (livingEntity.getDeltaMovement().y < 0 && livingEntity.fallDistance > 2.3F) {
				newState = TravellersWingsAttachment.WingState.FALL_FAST;
			} else if (livingEntity.isSprinting()) {
				newState = TravellersWingsAttachment.WingState.SPRINT;
			} else if (livingEntity.walkAnimation.speed() > 0.1) {
				newState = TravellersWingsAttachment.WingState.WALK;
			}
		}

		if (newState != attachment.state) {
			attachment.state = newState;
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity), createWingStatePacket(livingEntity, attachment));
		}
	}

	/**
	 * Puts the wings of an entity that took them off back to idle, so the server and every tracking client agree on
	 * the state the next time wings are worn. 4.8 keeps running the state machine for entities without wings instead.
	 */
	public static void resetWingState(LivingEntity livingEntity) {
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(livingEntity);
		if (attachment == null || attachment.state == TravellersWingsAttachment.WingState.IDLE)
			return;
		attachment.state = TravellersWingsAttachment.WingState.IDLE;
		attachment.doubleJumpTimer = 0;
		attachment.sidestepTimer = 0;
		TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> livingEntity), createWingStatePacket(livingEntity, attachment));
	}

	/**
	 * Sends the current wing state of {@code livingEntity} to a single player, for players that start tracking it while
	 * its wings are mid-animation. 4.8 only syncs on state changes.
	 */
	public static void syncWingState(LivingEntity livingEntity, ServerPlayer player) {
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(livingEntity);
		if (attachment != null && attachment.state != TravellersWingsAttachment.WingState.IDLE)
			TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), createWingStatePacket(livingEntity, attachment));
	}

	private static TravellersWingsStatePacket createWingStatePacket(LivingEntity livingEntity, TravellersWingsAttachment attachment) {
		return new TravellersWingsStatePacket(livingEntity.getId(), attachment.state, attachment.sidestepLeft, attachment.doubleJumpTimer, attachment.sidestepTimer);
	}
}
