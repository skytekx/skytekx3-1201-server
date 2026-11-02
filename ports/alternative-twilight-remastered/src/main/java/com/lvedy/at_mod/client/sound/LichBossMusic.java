package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModSounds;
import twilightforest.entity.boss.Lich;

/**
 * Lich 背景音乐接线：把 {@link Config} 中的开关 / 半径 / 音量绑定到通用
 * {@link BossMusicTracker}，并在客户端启动阶段调用 {@link #register()} 一次。
 */
public final class LichBossMusic {
    private LichBossMusic() {}

    public static void register() {
        BossMusicTracker<Lich> tracker = new BossMusicTracker<>(
                "lich",
                Lich.class,
                () -> ModSounds.LICH_MUSIC.get(),
                () -> Config.LICH_MUSIC_ENABLED.get(),
                () -> Config.LICH_MUSIC_DETECTION_RADIUS.get(),
                () -> Config.LICH_MUSIC_VOLUME.get()
        );
        BossMusicManager.register(tracker);
    }
}
