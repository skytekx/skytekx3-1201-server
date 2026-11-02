package com.lvedy.at_mod.client.aurora;

import com.lvedy.at_mod.AlternativeTwilight;
import com.lvedy.at_mod.config.Config;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.boss.KnightPhantom;

import java.util.ArrayList;
import java.util.List;

/**
 * 每 tick 在客户端扫描玩家附近的 KnightPhantom，把"当前所有可见幻影骑士的位置列表"
 * 传给 {@link PhantomAuroraState}；State 在第一次接收到非空列表时锁定几何中心，之后
 * 锚点不再随骑士移动而变化。
 *
 * <p>暮色源码 {@code KnightPhantomSpawnerBlockEntity} 一次召唤 6 只骑士，围绕 spawner
 * 半径 4 格均匀分布，所以召唤瞬间几何中心 ≈ spawner 坐标——这就是我们要的"召唤点"。</p>
 */
@Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID, value = Dist.CLIENT)
public final class PhantomAuroraTracker {
    private PhantomAuroraTracker() {}

    // 1.20.1: TickEvent.PlayerTickEvent (END phase, client side) replaces NeoForge's PlayerTickEvent.Post
    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !event.side.isClient()) return;
        if (!(event.player instanceof LocalPlayer player)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != player) return;

        PhantomAuroraState state = PhantomAuroraState.getInstance();

        if (!Config.PHANTOM_AURORA_ENABLED.get()) {
            state.update(false, null);
            return;
        }

        Level level = player.level();
        double radius = Config.PHANTOM_AURORA_DETECTION_RADIUS.get();
        AABB box = player.getBoundingBox().inflate(radius);
        double radiusSq = radius * radius;

        List<Vec3> positions = new ArrayList<>();
        for (KnightPhantom kp : level.getEntitiesOfClass(KnightPhantom.class, box)) {
            if (!kp.isAlive()) continue;
            double dx = kp.getX() - player.getX();
            double dy = kp.getY() - player.getY();
            double dz = kp.getZ() - player.getZ();
            if (dx * dx + dy * dy + dz * dz <= radiusSq) {
                positions.add(new Vec3(kp.getX(), kp.getY(), kp.getZ()));
            }
        }

        boolean nearby = !positions.isEmpty();
        state.update(nearby, positions);
    }

    @SubscribeEvent
    public static void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        AlternativeTwilight.LOGGER.debug("[aurora] logout - resetting state");
        PhantomAuroraState.getInstance().reset();
    }
}
