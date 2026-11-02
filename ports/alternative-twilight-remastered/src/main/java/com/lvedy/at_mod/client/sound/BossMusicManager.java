package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.AlternativeTwilight;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

/**
 * 通用 Boss 背景音乐管理器。
 * <p>
 * 模块只需实例化一个 {@link BossMusicTracker} 并调用 {@link #register(BossMusicTracker)}，
 * 本类会按统一节奏驱动所有 tracker 的检测和启停。
 */
@Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID, value = Dist.CLIENT)
public class BossMusicManager {
    private static final List<BossMusicTracker<?>> TRACKERS = new ArrayList<>();
    private static final int CHECK_INTERVAL_TICKS = 10;
    private static int checkCooldown = 0;

    public static synchronized void register(BossMusicTracker<?> tracker) {
        for (BossMusicTracker<?> existing : TRACKERS) {
            if (existing.id().equals(tracker.id())) return;
        }
        TRACKERS.add(tracker);
    }

    // 1.20.1: TickEvent.PlayerTickEvent (END phase, client side) replaces NeoForge's PlayerTickEvent.Post
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isClient()) return;
        if (!(event.player instanceof LocalPlayer player)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) return;

        if (checkCooldown > 0) {
            checkCooldown--;
            return;
        }
        checkCooldown = CHECK_INTERVAL_TICKS;

        for (BossMusicTracker<?> tracker : TRACKERS) {
            tracker.update(mc, player);
        }
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        Minecraft mc = Minecraft.getInstance();
        for (BossMusicTracker<?> tracker : TRACKERS) {
            tracker.stop(mc);
        }
    }
}
