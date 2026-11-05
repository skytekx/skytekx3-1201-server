package com.github.mekanismextramodules.mixin;

import com.github.mekanismextramodules.service.ChaosCompatService;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "com.brandon3055.draconicevolution.entity.guardian.control.LaserBeamPhase", remap = false)
public abstract class LaserBeamPhaseMixin {
    @Redirect(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;setHealth(F)V")
    )
    private void mekanismExtraModules$guardSetHealth(Player player, float health) {
        if (!ChaosCompatService.protectDirectLaserHealthWrite(player)) {
            player.setHealth(health);
        }
    }

    @Redirect(
            method = "serverTick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;die(Lnet/minecraft/world/damagesource/DamageSource;)V")
    )
    private void mekanismExtraModules$guardDie(Player player, DamageSource source) {
        if (!ChaosCompatService.protectChaosLaser(player, source)) {
            player.die(source);
        }
    }
}
