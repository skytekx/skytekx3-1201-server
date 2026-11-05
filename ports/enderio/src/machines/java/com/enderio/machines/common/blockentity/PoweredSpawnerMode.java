package com.enderio.machines.common.blockentity;

import com.enderio.EnderIO;
import com.enderio.api.misc.IIcon;
import com.enderio.api.misc.Vector2i;
import com.enderio.machines.common.lang.MachineLang;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * SkyTekx: the powered spawner mode of 8.x (a90e5c512).
 * Spawn mode spawns the bound mob, capture mode fills empty soul vials with it.
 * Also provides the icons of the mode button, like {@link AlloySmelterMode}.
 */
public enum PoweredSpawnerMode implements IIcon {
    SPAWN("spawn", MachineLang.POWERED_SPAWNER_MODE_SPAWN),
    CAPTURE("capture", MachineLang.POWERED_SPAWNER_MODE_CAPTURE);

    private static final Vector2i SIZE = new Vector2i(16, 16);

    private final String name;
    private final ResourceLocation texture;
    private final Component tooltip;

    PoweredSpawnerMode(String name, Component tooltip) {
        this.name = name;
        this.texture = EnderIO.loc("textures/gui/icons/powered_spawner_mode/" + name + ".png");
        this.tooltip = tooltip;
    }

    /**
     * The name that the block entity saves under the NBT key {@code SpawnerMode}.
     */
    public String getSerializedName() {
        return name;
    }

    /**
     * @return the mode with this saved name, or null for an unknown name.
     */
    @Nullable
    public static PoweredSpawnerMode byName(String name) {
        for (PoweredSpawnerMode mode : values()) {
            if (mode.name.equals(name)) {
                return mode;
            }
        }

        return null;
    }

    @Override
    public ResourceLocation getTextureLocation() {
        return texture;
    }

    @Override
    public Vector2i getIconSize() {
        return SIZE;
    }

    @Override
    public Vector2i getTexturePosition() {
        return Vector2i.ZERO;
    }

    @Override
    public Vector2i getTextureSize() {
        return SIZE;
    }

    @Override
    public Component getTooltip() {
        return tooltip;
    }
}
