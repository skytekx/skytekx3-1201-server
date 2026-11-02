package com.lvedy.at_mod.client.sound;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * 通用 Boss 背景音乐 SoundInstance：循环播放，跟随玩家位置。
 * 由 {@link BossMusicTracker} 创建和控制。
 */
public class BossMusicSoundInstance extends AbstractTickableSoundInstance {
    private final LocalPlayer player;
    private boolean stopRequested = false;

    public BossMusicSoundInstance(SoundEvent event, LocalPlayer player, float volume) {
        super(event, SoundSource.MUSIC, RandomSource.create());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = volume;
        this.relative = true;
        this.x = 0.0D;
        this.y = 0.0D;
        this.z = 0.0D;
    }

    public void requestStop() {
        this.stopRequested = true;
    }

    @Override
    public void tick() {
        if (this.stopRequested || this.player == null || !this.player.isAlive()) {
            this.stop();
            return;
        }
        this.x = this.player.getX();
        this.y = this.player.getY();
        this.z = this.player.getZ();
    }
}
