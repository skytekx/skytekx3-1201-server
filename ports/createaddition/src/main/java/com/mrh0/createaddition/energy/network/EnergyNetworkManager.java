package com.mrh0.createaddition.energy.network;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.level.LevelAccessor;

/**
 * Replaced by {@link WireGraph}, which keeps the energy networks of a dimension. Nothing creates,
 * fills or ticks instances any more.
 *
 * SkyTekx: kept as an empty class so that code linked against Create Crafts & Additions 1.3.3 still
 * resolves {@link #instances}. AllTheLeaks 1.1.1 (UntrackedIssue001) reads that field, in a fix
 * that its version range keeps off for this build.
 */
@Deprecated
public class EnergyNetworkManager {
	public static Map<LevelAccessor, EnergyNetworkManager> instances = new WeakHashMap<>();

	private EnergyNetworkManager() {}
}
