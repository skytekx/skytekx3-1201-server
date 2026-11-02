package com.lvedy.at_mod.special.ModItems;

import com.lvedy.at_mod.AlternativeTwilight;
import com.lvedy.at_mod.config.Config;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

// 1.20.1 backport: NeoForge's LivingIncomingDamageEvent -> Forge's LivingAttackEvent (both fire at the start of LivingEntity#hurt)
@Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID)
public class SpearEvents {

    @SubscribeEvent
    public static void onPlayerHurt(LivingAttackEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (player.level().isClientSide()) return;
        if (!(player.getMainHandItem().getItem() instanceof SpearItem)) return;

        int ticks = Config.SPEAR_HIT_PROTECTION_TICKS.get();
        if (ticks <= 0) return;
        long until = player.level().getGameTime() + ticks;
        long current = player.getPersistentData().getLong(SpearItem.PROTECT_UNTIL_KEY);
        if (until > current) {
            player.getPersistentData().putLong(SpearItem.PROTECT_UNTIL_KEY, until);
        }
    }
}
