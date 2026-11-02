package com.lvedy.at_mod.client.sound;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.function.BooleanSupplier;
import java.util.function.DoubleSupplier;
import java.util.function.Supplier;

/**
 * 单个 Boss-音乐绑定的状态容器。
 * <p>
 * 一个 tracker 对应"一种生物 → 一首音乐"。所有 tracker 通过
 * {@link BossMusicManager#register(BossMusicTracker)} 注册后由其统一驱动。
 *
 * @param <E> 触发音乐的生物类型
 */
public class BossMusicTracker<E extends LivingEntity> {

    private final String id;
    private final Class<E> entityClass;
    private final Supplier<SoundEvent> soundEvent;
    private final BooleanSupplier enabled;
    private final DoubleSupplier detectionRadius;
    private final DoubleSupplier volume;

    private BossMusicSoundInstance current;

    public BossMusicTracker(String id,
                            Class<E> entityClass,
                            Supplier<SoundEvent> soundEvent,
                            BooleanSupplier enabled,
                            DoubleSupplier detectionRadius,
                            DoubleSupplier volume) {
        this.id = id;
        this.entityClass = entityClass;
        this.soundEvent = soundEvent;
        this.enabled = enabled;
        this.detectionRadius = detectionRadius;
        this.volume = volume;
    }

    public String id() {
        return id;
    }

    /**
     * 子类可覆写以做更细的过滤（例如只对 Boss 战阶段播放、忽略驯服后的实体等）。
     */
    protected boolean matchesEntity(E entity, LocalPlayer player) {
        return entity.isAlive();
    }

    /**
     * 由 {@link BossMusicManager} 在每次检测节奏触发时调用。
     */
    public void update(Minecraft mc, LocalPlayer player) {
        if (!enabled.getAsBoolean()) {
            stop(mc);
            return;
        }
        if (isNearby(player)) {
            startIfNeeded(mc, player);
        } else {
            stop(mc);
        }
    }

    public void stop(Minecraft mc) {
        if (current == null) return;
        current.requestStop();
        mc.getSoundManager().stop(current);
        current = null;
    }

    private boolean isNearby(LocalPlayer player) {
        Level level = player.level();
        double radius = detectionRadius.getAsDouble();
        AABB box = player.getBoundingBox().inflate(radius);
        double radiusSq = radius * radius;
        for (E e : level.getEntitiesOfClass(entityClass, box)) {
            if (matchesEntity(e, player) && distanceToSqr(e, player) <= radiusSq) {
                return true;
            }
        }
        return false;
    }

    private static double distanceToSqr(Entity a, Entity b) {
        double dx = a.getX() - b.getX();
        double dy = a.getY() - b.getY();
        double dz = a.getZ() - b.getZ();
        return dx * dx + dy * dy + dz * dz;
    }

    private void startIfNeeded(Minecraft mc, LocalPlayer player) {
        if (current != null && mc.getSoundManager().isActive(current)) return;
        SoundEvent event = soundEvent.get();
        if (event == null) return;
        // 1.20.1 backport: the jar ships no music (see NOTICE). Only play when a resource pack defines the
        // sound event in sounds.json; otherwise the sound engine would log "unknown sound" every check.
        if (mc.getSoundManager().getSoundEvent(event.getLocation()) == null) return;
        current = new BossMusicSoundInstance(event, player, (float) volume.getAsDouble());
        mc.getSoundManager().play(current);
    }
}
