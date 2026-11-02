package twilightforest.components.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.capabilities.CapabilityList;

/**
 * Animation state of the Traveller's Wings worn by a living entity. The server decides the state every tick (see
 * {@link twilightforest.item.travellers_gear.TravellersGearLogic#determineWingState}) and syncs every change to the
 * entity's trackers through {@link twilightforest.network.TravellersWingsStatePacket}; the client model reads it.
 * <p>
 * Backport of 4.8's {@code twilightforest:travellers_wings} data attachment. 1.20.1 has no attachments, so it lives in
 * a non-serialized capability ({@link CapabilityList#TRAVELLERS_WINGS}) on every living entity, matching the unsaved
 * attachment. The client-only render bookkeeping (4.8's {@code travellers_wings_anim} attachment) hangs off the same
 * object and is only created when the client first draws the wings.
 * <p>
 * The same object also holds the state of the Traveller's Wings modifiers, which 4.8 keeps in a dozen separate
 * attachments ({@code has_double_jump}, {@code double_jump_validator}, {@code is_gradually_gliding},
 * {@code last_horizontal_impulse} and so on). None of it has to survive a reload, so none of it is saved.
 */
public class TravellersWingsAttachment {
	public static final ResourceLocation ID = TwilightForestMod.prefix("travellers_wings");
	public static final int DOUBLE_JUMP_DURATION = 15;
	public static final int SIDESTEP_DURATION = 11;

	public WingState state = WingState.IDLE;
	public boolean sidestepLeft;
	public int doubleJumpTimer;
	public int sidestepTimer;
	public long lastSidestepTime;
	public boolean shouldPlaySideStepCooldownSound;
	@Nullable
	private TravellersWingsAnimAttachment anim;

	/**
	 * 4.8 {@code has_double_jump}: whether the entity may still jump in mid-air, refilled on the ground, in liquids and
	 * on climbable blocks.
	 */
	public boolean hasDoubleJump;
	/**
	 * Set by a double jump and cleared once the Double Jump refills; while set, falls count
	 * {@link twilightforest.item.travellers_gear.TravellersGearLogic#DOUBLE_JUMP_SAFE_FALL_DISTANCE} blocks shorter.
	 * Stands in for 4.8's {@code travellers_gear.boots_double_jump_safe_fall_distance} modifier on 1.21's safe fall
	 * distance attribute.
	 */
	public boolean hasDoubleJumpSafeFallDistance;
	/**
	 * 4.8 {@code is_gradually_gliding}: the local player decides it from the sneak key, the server and the other clients
	 * learn it from {@link twilightforest.network.GradualGlidePacket}.
	 */
	public boolean isGraduallyGliding;
	/**
	 * 4.8 {@code double_jump_validator} and {@code double_jump_validator_last_check}.
	 */
	public final MovementValidator doubleJumpValidator = new MovementValidator();
	/**
	 * 4.8 {@code sidestep_validator} and {@code sidestep_validator_last_check}.
	 */
	public final MovementValidator sidestepValidator = new MovementValidator();

	// Input bookkeeping of the local player: 4.8 last_jump_key_press_time, last_horizontal_impulse,
	// last_non_horizontal_impulse and last_horizontal_walking_time (in ticks of the player)
	public int lastJumpKeyPressTime;
	public float lastHorizontalImpulse;
	public float lastNonZeroHorizontalImpulse;
	public int lastHorizontalWalkingTime;

	public TravellersWingsAnimAttachment getAnim() {
		if (this.anim == null) this.anim = new TravellersWingsAnimAttachment();
		return this.anim;
	}

	@Nullable
	public static TravellersWingsAttachment get(LivingEntity entity) {
		return entity.getCapability(CapabilityList.TRAVELLERS_WINGS).resolve().orElse(null);
	}

	public enum WingState {
		IDLE,
		WALK,
		SPRINT,
		FALL_SLOW,
		FALL_FAST,
		SWIM,
		RIDE,
		DOUBLE_JUMP,
		SIDESTEP
	}

	/**
	 * Counts the rejected double jump or sidestep requests of a player, so the server can pull back and finally kick a
	 * client that keeps sending them (see {@link twilightforest.item.travellers_gear.TravellersGearLogic#handleDoubleJumpAbuse}).
	 */
	public static final class MovementValidator {
		public int count;
		public int lastCheck;
	}
}
