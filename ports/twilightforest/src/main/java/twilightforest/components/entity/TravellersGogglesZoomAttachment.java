package twilightforest.components.entity;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import twilightforest.TwilightForestMod;
import twilightforest.capabilities.CapabilityList;

/**
 * Whether a player is looking through the zoom of their Traveller's Goggles. The client of the player decides it every
 * tick and tells the server through {@link twilightforest.network.GogglesZoomPacket}, which passes it on to the players
 * tracking them; the armor renderer then flips the goggles down over the eyes
 * ({@link twilightforest.item.travellers_gear.TravellersArmorItem#getArmorTexture}).
 * <p>
 * Backport of 4.8's {@code twilightforest:is_using_goggles_zoom_modifier} data attachment. 1.20.1 has no attachments,
 * so it is a capability on every player ({@link CapabilityList#TRAVELLERS_GOGGLES_ZOOM}). Unlike 4.8's attachment it is
 * not saved: the zoom only lasts while the key is held, and a client always starts out not zooming.
 */
public class TravellersGogglesZoomAttachment {
	public static final ResourceLocation ID = TwilightForestMod.prefix("is_using_goggles_zoom_modifier");

	public boolean usingZoom;

	public static boolean isUsingZoom(Entity entity) {
		return entity.getCapability(CapabilityList.TRAVELLERS_GOGGLES_ZOOM).map(attachment -> attachment.usingZoom).orElse(false);
	}

	public static void setUsingZoom(Entity entity, boolean usingZoom) {
		entity.getCapability(CapabilityList.TRAVELLERS_GOGGLES_ZOOM).ifPresent(attachment -> attachment.usingZoom = usingZoom);
	}
}
