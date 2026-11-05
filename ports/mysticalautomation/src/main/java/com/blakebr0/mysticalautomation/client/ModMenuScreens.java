package com.blakebr0.mysticalautomation.client;

import com.blakebr0.mysticalautomation.client.screen.AwakeningAltarnatorScreen;
import com.blakebr0.mysticalautomation.client.screen.CrafterScreen;
import com.blakebr0.mysticalautomation.client.screen.EnchanternatorScreen;
import com.blakebr0.mysticalautomation.client.screen.FarmerScreen;
import com.blakebr0.mysticalautomation.client.screen.FertilizerScreen;
import com.blakebr0.mysticalautomation.client.screen.InfuserScreen;
import com.blakebr0.mysticalautomation.client.screen.InfusionAltarnatorScreen;
import com.blakebr0.mysticalautomation.init.ModMenuTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

// SkyTekx port: Forge 1.20.1 has no RegisterMenuScreensEvent, the screens register through MenuScreens in client setup
public final class ModMenuScreens {
    @SubscribeEvent
    public void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            MenuScreens.register(ModMenuTypes.INFUSER.get(), InfuserScreen::new);
            MenuScreens.register(ModMenuTypes.CRAFTER.get(), CrafterScreen::new);
            MenuScreens.register(ModMenuTypes.FARMER.get(), FarmerScreen::new);
            MenuScreens.register(ModMenuTypes.FERTILIZER.get(), FertilizerScreen::new);
            MenuScreens.register(ModMenuTypes.ENCHANTERNATOR.get(), EnchanternatorScreen::new);
            MenuScreens.register(ModMenuTypes.INFUSION_ALTARNATOR.get(), InfusionAltarnatorScreen::new);
            MenuScreens.register(ModMenuTypes.AWAKENING_ALTARNATOR.get(), AwakeningAltarnatorScreen::new);
        });
    }
}
