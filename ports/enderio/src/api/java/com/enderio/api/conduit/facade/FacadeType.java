package com.enderio.api.conduit.facade;

import net.minecraft.util.StringRepresentable;

/**
 * Additional options for a conduit facade, ported from Ender IO 7.1 (78e627263).
 * The type comes from the facade item, so it is stored with the facade item in the conduit bundle.
 */
public enum FacadeType implements StringRepresentable {
    BASIC("basic", false, false),
    HARDENED("hardened", false, true),
    TRANSPARENT("transparent", true, false),
    TRANSPARENT_HARDENED("transparent_hardened", true, true);

    private final String serializedName;
    private final boolean doesHideConduits;
    private final boolean isBlastResistant;

    FacadeType(String serializedName, boolean doesHideConduits, boolean isBlastResistant) {
        this.serializedName = serializedName;
        this.doesHideConduits = doesHideConduits;
        this.isBlastResistant = isBlastResistant;
    }

    /**
     * @return whether the conduits are not rendered behind the facade, so a facade painted with a translucent block hides them.
     */
    public boolean doesHideConduits() {
        return doesHideConduits;
    }

    /**
     * @return whether the conduit bundle resists explosions like the explosion resistant blocks of Ender IO.
     */
    public boolean isBlastResistant() {
        return isBlastResistant;
    }

    @Override
    public String getSerializedName() {
        return serializedName;
    }
}
