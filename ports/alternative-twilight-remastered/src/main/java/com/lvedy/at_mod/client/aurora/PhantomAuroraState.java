package com.lvedy.at_mod.client.aurora;

import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

/**
 * 客户端单例，维护极光特效的"锚点 + 强度 + 时间"。
 *
 * <p><b>锚点策略（已锁定型）</b>：第一次进入触发条件时，把当前可见的所有幻影骑士
 * 位置取几何中心作为锚点；之后无论骑士飞到哪，锚点都不再变化。完全淡出后才解锁，
 * 这样下一波 boss 战可以重新选锚点。
 *
 * <p>对应的暮色源码事实：{@code KnightPhantomSpawnerBlockEntity.spawnMyBoss} 把 6 只骑士
 * 围绕 spawner 4 格半径均匀分布，所以"召唤瞬间所有可见骑士的几何中心 ≈ spawner 位置"。
 */
@OnlyIn(Dist.CLIENT)
public final class PhantomAuroraState {
    private static final PhantomAuroraState INSTANCE = new PhantomAuroraState();

    public static PhantomAuroraState getInstance() {
        return INSTANCE;
    }

    private Vec3 lockedAnchor;        // null 表示尚未锁定
    private float intensity;          // 真正推给 shader 的强度（带缓动）
    private float targetIntensity;
    private float timeSeconds;        // 自激活以来的秒数，驱动 fbm 流动
    private boolean active;

    private PhantomAuroraState() {}

    /**
     * 由 {@link PhantomAuroraTracker} 每 tick 调一次。
     *
     * @param nearby            玩家附近是否仍存在幻影骑士
     * @param visiblePositions  当前帧检测到的所有幻影骑士的位置（可空）
     */
    public void update(boolean nearby, List<Vec3> visiblePositions) {
        if (nearby) {
            targetIntensity = 1.0f;
            // 第一次激活：用几何中心锁定锚点。锁定后再也不动。
            if (lockedAnchor == null && visiblePositions != null && !visiblePositions.isEmpty()) {
                double sx = 0, sy = 0, sz = 0;
                for (Vec3 p : visiblePositions) {
                    sx += p.x;
                    sy += p.y;
                    sz += p.z;
                }
                int n = visiblePositions.size();
                lockedAnchor = new Vec3(sx / n, sy / n, sz / n);
            }
        } else {
            targetIntensity = 0.0f;
        }

        // 强度缓动：每 tick 朝目标走 8% （≈淡入/淡出 0.6 秒）
        float diff = targetIntensity - intensity;
        intensity += diff * 0.08f;
        if (Math.abs(diff) < 1e-3f) intensity = targetIntensity;

        active = intensity > 1e-3f;
        if (active) {
            timeSeconds += 1.0f / 20.0f; // 每 tick = 0.05s
        }

        // 完全淡出后才解锁锚点：下一波 boss 战会重新锁定一个新位置
        if (!nearby && intensity <= 1e-3f) {
            lockedAnchor = null;
            timeSeconds = 0.0f;
        }
    }

    public float getIntensity() {
        return intensity;
    }

    /** 锚点未锁定时返回 (0,0,0)；外部应配合 isActive() 一起判断。 */
    public Vec3 getAnchor() {
        return lockedAnchor != null ? lockedAnchor : Vec3.ZERO;
    }

    public float getTimeSeconds(float partialTick) {
        return timeSeconds + (active ? partialTick / 20.0f : 0f);
    }

    public boolean isActive() {
        return active && lockedAnchor != null;
    }

    /** 退出世界 / 切维度时调用，避免残留状态。 */
    public void reset() {
        lockedAnchor = null;
        intensity = 0.0f;
        targetIntensity = 0.0f;
        timeSeconds = 0.0f;
        active = false;
    }
}
