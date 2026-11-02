package twilightforest.item.travellers_gear;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.network.TFPacketHandler;
import twilightforest.network.TravellersWingsStatePacket;

/**
 * Server-side logic of the Traveller's Gear. Backport of the core part of 4.8's TravellersGearLogic: the wing animation
 * state machine. The modifier logic (double jump, sidestep, gradual glide, auto repair and so on) comes with the
 * travellers-* modifier features.
 */
public class TravellersGearLogic {

	public static boolean isWearingWings(LivingEntity livingEntity) {
		return TravellersArmorItem.hasPart(livingEntity.getItemBySlot(EquipmentSlot.LEGS), TravellersArmorItem.Part.WINGS);
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
			boolean inLiquid = livingEntity.isInWaterOrBubble() || livingEntity.isInLava(); // 1.21 Entity#isInLiquid
			if (livingEntity.isPassenger()) {
				newState = TravellersWingsAttachment.WingState.RIDE;
			} else if (livingEntity.isSwimming()) {
				newState = TravellersWingsAttachment.WingState.SWIM;
			} else if (!livingEntity.onGround() && !inLiquid && livingEntity.fallDistance < 2.3F && (!(livingEntity instanceof Player p) || !p.getAbilities().flying)) {
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
