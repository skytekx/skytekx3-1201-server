package com.enderio.conduits.client.model.conduit.facades;

import com.enderio.conduits.common.conduit.block.ConduitBlockEntity;
import com.enderio.conduits.common.conduit.facades.FacadeUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.LongSet;
import net.minecraft.client.Minecraft;
import net.minecraft.core.SectionPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

/**
 * Whether the local player sees the facades (Ender IO 7.1.1, 7de602e53). Only the local player's held items count (3c8717b0c), so other
 * players with a Yeta wrench in view do not change what this client renders. A change rebuilds the sections that hold facades, so the
 * conduit model leaves the facades out and the ghost pass of {@link com.enderio.conduits.client.ConduitFacadeRendering} draws them.
 */
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public final class ClientFacadeVisibility {

    private static volatile boolean facadesVisible = true;

    private ClientFacadeVisibility() {
    }

    public static boolean areFacadesVisible() {
        return facadesVisible;
    }

    private static void setFacadesVisible(boolean visible) {
        if (visible != facadesVisible) {
            facadesVisible = visible;
            RenderSystem.recordRenderCall(ClientFacadeVisibility::rebuildFacadeSections);
        }
    }

    private static void rebuildFacadeSections() {
        var minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return;
        }

        @Nullable Long2ObjectMap<LongSet> sections = ConduitBlockEntity.CHUNK_FACADES.get(minecraft.level.dimension());
        if (sections == null) {
            return;
        }

        for (long section : sections.keySet()) {
            minecraft.levelRenderer.setSectionDirty(SectionPos.x(section), SectionPos.y(section), SectionPos.z(section));
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.START) {
            return;
        }

        var player = Minecraft.getInstance().player;
        if (player == null || Minecraft.getInstance().level == null) {
            return;
        }

        setFacadesVisible(FacadeUtil.areFacadesVisible(player));
    }

    @SubscribeEvent
    public static void onLoggingOut(ClientPlayerNetworkEvent.LoggingOut event) {
        ConduitBlockEntity.clearClientFacades();
        facadesVisible = true;
    }
}
