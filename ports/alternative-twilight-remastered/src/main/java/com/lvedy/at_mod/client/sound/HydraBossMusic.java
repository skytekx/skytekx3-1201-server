package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModSounds;
import twilightforest.entity.boss.Hydra;

public final class HydraBossMusic {
    private HydraBossMusic() {}

    public static void register() {
        BossMusicTracker<Hydra> tracker = new BossMusicTracker<>(
                "hydra",
                Hydra.class,
                () -> ModSounds.HYDRA_MUSIC.get(),
                () -> Config.HYDRA_MUSIC_ENABLED.get(),
                () -> Config.HYDRA_MUSIC_DETECTION_RADIUS.get(),
                () -> Config.HYDRA_MUSIC_VOLUME.get()
        );
        BossMusicManager.register(tracker);
    }
}
