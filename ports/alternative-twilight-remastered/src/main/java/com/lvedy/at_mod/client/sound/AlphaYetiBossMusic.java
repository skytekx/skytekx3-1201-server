package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModSounds;
import twilightforest.entity.boss.AlphaYeti;

public final class AlphaYetiBossMusic {
    private AlphaYetiBossMusic() {}

    public static void register() {
        BossMusicTracker<AlphaYeti> tracker = new BossMusicTracker<>(
                "alpha_yeti",
                AlphaYeti.class,
                () -> ModSounds.ALPHA_YETI_MUSIC.get(),
                () -> Config.ALPHA_YETI_MUSIC_ENABLED.get(),
                () -> Config.ALPHA_YETI_MUSIC_DETECTION_RADIUS.get(),
                () -> Config.ALPHA_YETI_MUSIC_VOLUME.get()
        );
        BossMusicManager.register(tracker);
    }
}
