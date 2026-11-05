package com.enderio.conduits.common.redstone;

/**
 * A redstone filter with settings that a player can change, for the configured and unconfigured tooltip lines of
 * Ender IO 8.x (0c79259d9, RedstoneFilter.isConfigured).
 */
public interface ConfigurableRedstoneFilter {

    /**
     * @return whether the settings differ from the defaults of a new filter
     */
    boolean isConfigured();
}
