package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModSounds;
import twilightforest.entity.boss.Naga;

/**
 * Naga 背景音乐接线：把 {@link Config} 中的开关 / 半径 / 音量绑定到通用
 * {@link BossMusicTracker}，并在客户端启动阶段调用 {@link #register()} 一次。
 */
public final class NagaBossMusic {
    private NagaBossMusic() {}

    public static void register() {
        BossMusicTracker<Naga> tracker = new BossMusicTracker<>(
                "naga",
                Naga.class,
                () -> ModSounds.NAGA_MUSIC.get(),
                () -> Config.MUSIC_ENABLED.get(),
                () -> Config.MUSIC_DETECTION_RADIUS.get(),
                () -> Config.MUSIC_VOLUME.get()
        );
        BossMusicManager.register(tracker);
    }
}
