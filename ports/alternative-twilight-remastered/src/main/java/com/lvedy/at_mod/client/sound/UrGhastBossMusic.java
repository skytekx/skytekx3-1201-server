package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModSounds;
import twilightforest.entity.boss.UrGhast;

public final class UrGhastBossMusic {
    private UrGhastBossMusic() {}

    public static void register() {
        BossMusicTracker<UrGhast> tracker = new BossMusicTracker<>(
                "ur_ghast",
                UrGhast.class,
                () -> ModSounds.UR_GHAST_MUSIC.get(),
                () -> Config.UR_GHAST_MUSIC_ENABLED.get(),
                () -> Config.UR_GHAST_MUSIC_DETECTION_RADIUS.get(),
                () -> Config.UR_GHAST_MUSIC_VOLUME.get()
        );
        BossMusicManager.register(tracker);
    }
}
