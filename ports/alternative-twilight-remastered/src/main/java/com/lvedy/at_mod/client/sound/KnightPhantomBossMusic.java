package com.lvedy.at_mod.client.sound;

import com.lvedy.at_mod.config.Config;
import com.lvedy.at_mod.register.ModSounds;
import twilightforest.entity.boss.KnightPhantom;

public final class KnightPhantomBossMusic {
    private KnightPhantomBossMusic() {}

    public static void register() {
        BossMusicTracker<KnightPhantom> tracker = new BossMusicTracker<>(
                "knight_phantom",
                KnightPhantom.class,
                () -> ModSounds.KNIGHT_PHANTOM_MUSIC.get(),
                () -> Config.KNIGHT_PHANTOM_MUSIC_ENABLED.get(),
                () -> Config.KNIGHT_PHANTOM_MUSIC_DETECTION_RADIUS.get(),
                () -> Config.KNIGHT_PHANTOM_MUSIC_VOLUME.get()
        );
        BossMusicManager.register(tracker);
    }
}
