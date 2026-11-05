package com.github.mekanismextramodules.mixin;

import com.github.mekanismextramodules.service.ChaosCompatService;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * SkyTekx port: Forge 1.20.1 runs on SRG names, so the two Minecraft call targets are remapped through
 * the refmap ({@code remap = true} on {@code @At}), while {@code serverTick} stays a Draconic Evolution
 * method name. Checked against Draconic Evolution 3.1.2.621, whose {@code LaserBeamPhase.serverTick}
 * calls {@code Player.setHealth(F)} and {@code Player.die(DamageSource)} once each.
 */
@Mixin(targets = "com.brandon3055.draconicevolution.entity.guardian.control.LaserBeamPhase", remap = false)
public abstract class LaserBeamPhaseMixin {
    @Redirect(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V", remap = true)
    )
    private void mekanismExtraModules$guardSetHealth(Player player, float health) {
        if (!ChaosCompatService.protectDirectLaserHealthWrite(player)) {
            player.setHealth(health);
        }
    }

    @Redirect(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;die(Lnet/minecraft/world/damagesource/DamageSource;)V", remap = true)
    )
    private void mekanismExtraModules$guardDie(Player player, DamageSource source) {
        if (!ChaosCompatService.protectChaosLaser(player, source)) {
            player.die(source);
        }
    }
}
