package com.twilightmobsloots.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

/**
 * Item client extension shared by every trophy item: routes item rendering (GUI, hand, ground, head) to one
 * {@link MobTrophyISTER}. Created lazily on first use and dropped on resource reload so the head models are
 * re-baked from the current entity model set (see {@link ClientSetup}).
 */
public final class TrophyItemClientExtensions implements IClientItemExtensions {

    public static final TrophyItemClientExtensions INSTANCE = new TrophyItemClientExtensions();

    private static MobTrophyISTER renderer;

    private TrophyItemClientExtensions() {
    }

    @Override
    public BlockEntityWithoutLevelRenderer getCustomRenderer() {
        if (renderer == null) {
            renderer = new MobTrophyISTER(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());
        }
        return renderer;
    }

    /** Forget the baked renderer; the next render re-creates it. */
    static void invalidate() {
        renderer = null;
    }
}
