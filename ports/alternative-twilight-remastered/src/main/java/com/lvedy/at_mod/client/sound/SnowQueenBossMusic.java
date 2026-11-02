package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModSounds;
import twilightforest.entity.boss.SnowQueen;

public final class SnowQueenBossMusic {
    private SnowQueenBossMusic() {}

    public static void register() {
        BossMusicTracker<SnowQueen> tracker = new BossMusicTracker<>(
                "snow_queen",
                SnowQueen.class,
                () -> ModSounds.SNOW_QUEEN_MUSIC.get(),
                () -> Config.SNOW_QUEEN_MUSIC_ENABLED.get(),
                () -> Config.SNOW_QUEEN_MUSIC_DETECTION_RADIUS.get(),
                () -> Config.SNOW_QUEEN_MUSIC_VOLUME.get()
        );
        BossMusicManager.register(tracker);
    }
}
