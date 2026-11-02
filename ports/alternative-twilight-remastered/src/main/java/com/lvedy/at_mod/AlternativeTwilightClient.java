package com.lvedy.at_mod;

import com.lvedy.at_mod.client.sound.AlphaYetiBossMusic;
import com.lvedy.at_mod.client.sound.HydraBossMusic;
import com.lvedy.at_mod.client.sound.KnightPhantomBossMusic;
import com.lvedy.at_mod.client.sound.LichBossMusic;
import com.lvedy.at_mod.client.sound.MinoshroomBossMusic;
import com.lvedy.at_mod.client.sound.NagaBossMusic;
import com.lvedy.at_mod.client.sound.SnowQueenBossMusic;
import com.lvedy.at_mod.client.sound.UrGhastBossMusic;
import com.lvedy.at_mod.register.ModEntityTypes;
import com.lvedy.at_mod.special.ModEntity.client.NagaVenomRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
// 1.20.1 backport: the spear arm pose moved to SpearItem#initializeClient (Forge's IClientItemExtensions hook), and
// NeoForge's generic ConfigurationScreen has no Forge 47 counterpart, so there is no in-game config screen.
@Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AlternativeTwilightClient {

    @SubscribeEvent
    public static void registerEntityRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntityTypes.NAGA_VENOM.get(), NagaVenomRenderer::new);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 注册 Boss 背景音乐
        event.enqueueWork(NagaBossMusic::register);
        event.enqueueWork(LichBossMusic::register);
        event.enqueueWork(HydraBossMusic::register);
        event.enqueueWork(UrGhastBossMusic::register);
        event.enqueueWork(KnightPhantomBossMusic::register);
        event.enqueueWork(SnowQueenBossMusic::register);
        event.enqueueWork(MinoshroomBossMusic::register);
        event.enqueueWork(AlphaYetiBossMusic::register);
    }
}
