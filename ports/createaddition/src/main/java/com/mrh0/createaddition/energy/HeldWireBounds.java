package com.mrh0.createaddition.energy;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the wire the local player is placing runs from its start node to the player, so the
 * render box of that node has to reach the player too. Upstream 126ed38 asks the player inside
 * WireNodeRenderer#getRenderBoundingBox on NeoForge. On Forge 1.20.1 the block entity owns the
 * render box, and block entities also load on a dedicated server, so the client tick handler
 * ({@code ClientEventHandler}) stores the start and the player's box here and the block entity
 * only reads them. This class uses no client classes and stays empty on a server.
 */
public final class HeldWireBounds {
	@Nullable
	private static BlockPos start;
	@Nullable
	private static AABB playerBox;

	private HeldWireBounds() {}

	public static void set(@Nullable BlockPos start, @Nullable AABB playerBox) {
		HeldWireBounds.start = start == null || playerBox == null ? null : start.immutable();
		HeldWireBounds.playerBox = HeldWireBounds.start == null ? null : playerBox;
	}

	public static void clear() {
		set(null, null);
	}

	// The box of the player placing a wire from this position, or null.
	@Nullable
	public static AABB forStart(BlockPos pos) {
		BlockPos held = start;
		return held != null && held.equals(pos) ? playerBox : null;
	}
}
