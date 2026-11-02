package twilightforest.capabilities.portal;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import twilightforest.TwilightForestMod;
import twilightforest.block.TFPortalBlock;

/**
 * Per-player state of the Twilight portal warm-up. It is not saved, like 4.8's attachment and 1.21's PortalProcessor.
 * <p>
 * Two timers are kept. {@link #getPortalTimer()} is 4.8's TFPortalAttachment: it rises while the player stands in the
 * portal and falls after they leave, and the client draws the portal overlay from it. The transfer timer is the
 * Twilight portal part of 1.21's PortalProcessor, which 1.20.1 does not have: the server only sends the player through
 * once they have stood in the portal for {@link TFPortalBlock#getPortalTransitionTime} ticks.
 */
public class TFPortalCapability {

	public static final ResourceLocation ID = TwilightForestMod.prefix("tf_portal_cooldown");
	/**
	 * Ticks until the portal overlay is fully visible.
	 */
	public static final int MAX_TICKS = 60;

	// [4.8 TFPortalAttachment]
	private boolean isInsidePortal = false;
	private int portalTimer = 0;

	// [1.21 PortalProcessor] server only
	private boolean insidePortalThisTick = false;
	private int portalTime = 0;

	public void setInPortal(boolean inPortal) {
		this.isInsidePortal = inPortal;
	}

	public boolean isInsidePortal() {
		return this.isInsidePortal;
	}

	public int getPortalTimer() {
		return this.portalTimer;
	}

	/**
	 * [1.21 Entity#setAsInsidePortal] Called on the server for every tick the player stands in an active portal. While
	 * the player is on portal cooldown (they just came through a portal) standing in it only renews the cooldown, so they
	 * have to step out before the warm-up starts again.
	 */
	public void setAsInsidePortal(Player player) {
		if (player.isOnPortalCooldown()) {
			player.setPortalCooldown();
		} else {
			this.insidePortalThisTick = true;
		}
	}

	/**
	 * [4.8 TFPortalAttachment#tick] Called at the end of every player tick on both sides.
	 */
	public void tick(Player player) {
		if (this.isInsidePortal()) {
			this.portalTimer = Math.min(this.portalTimer + 1, MAX_TICKS);

			if (!player.isInWall()) {
				BlockPos pos = player.blockPosition();
				if (!(player.level().getBlockState(pos).getBlock() instanceof TFPortalBlock) && !(player.level().getBlockState(pos.below()).getBlock() instanceof TFPortalBlock)) {
					this.isInsidePortal = false;
				}
			}
		} else if (this.getPortalTimer() > 0) this.portalTimer -= 2;
	}

	/**
	 * [1.21 PortalProcessor#processPortalTeleportation and Entity#handlePortal] Called on the server after {@link #tick}.
	 * The timer counts up while the player stands in the portal and decays by 4 per tick once they leave it. When it
	 * reaches the transition time the player gets a portal cooldown and is sent through.
	 */
	public void processPortalTeleportation(ServerLevel level, Player player) {
		if (!this.insidePortalThisTick) {
			this.portalTime = Math.max(this.portalTime - 4, 0);
		} else {
			this.insidePortalThisTick = false;
			// [1.21 Entity#canUsePortal(false)]
			if (!player.isPassenger() && player.isAlive() && this.portalTime++ >= TFPortalBlock.getPortalTransitionTime(level, player)) {
				player.setPortalCooldown();
				TFPortalBlock.attemptSendEntity(player, false, true);
			}
		}
	}
}
