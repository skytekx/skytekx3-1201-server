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
}
