package ianm1647.expandeddelight.client.event;

import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.client.gui.JuicerScreen;
import ianm1647.expandeddelight.client.gui.JuicerTooltip;
import ianm1647.expandeddelight.client.recipebook.RecipeCategories;
import ianm1647.expandeddelight.client.renderer.CinnamonBoatRenderer;
import ianm1647.expandeddelight.common.entity.CinnamonBoat;
import ianm1647.expandeddelight.common.item.JuicerTooltipComponent;
import ianm1647.expandeddelight.common.registry.EDBlockEntityTypes;
import ianm1647.expandeddelight.common.registry.EDEntityTypes;
import ianm1647.expandeddelight.common.registry.EDMenuTypes;
import ianm1647.expandeddelight.common.registry.EDWoodTypes;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.client.model.BoatModel;
import net.minecraft.client.model.ChestBoatModel;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.HangingSignRenderer;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.client.event.RegisterRecipeBookCategoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client registrations. Only the client loads this class: the annotation limits it to Dist.CLIENT, and no
 * common class names it.
 */
@Mod.EventBusSubscriber(
        modid = ExpandedDelight.MODID,
        bus = Mod.EventBusSubscriber.Bus.MOD,
        value = {Dist.CLIENT}
)
public class ClientSetupEvents {
    public ClientSetupEvents() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            // NeoForge 1.21.1 has RegisterMenuScreensEvent and registers wood type materials itself
            MenuScreens.register(EDMenuTypes.JUICER.get(), JuicerScreen::new);
            Sheets.addWoodType(EDWoodTypes.CINNAMON);
        });
    }

    @SubscribeEvent
    public static void registerRecipeBookCategories(RegisterRecipeBookCategoriesEvent event) {
        RecipeCategories.init(event);
    }

    @SubscribeEvent
    public static void registerCustomTooltipRenderers(RegisterClientTooltipComponentFactoriesEvent event) {
        event.register(JuicerTooltipComponent.class, JuicerTooltip::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        LayerDefinition boatLayerDefinition = BoatModel.createBodyModel();
        LayerDefinition chestBoatLayerDefinition = ChestBoatModel.createBodyModel();
        for (CinnamonBoat.Type type : CinnamonBoat.Type.values()) {
            event.registerLayerDefinition(CinnamonBoatRenderer.boatTextureLocation(type), () -> boatLayerDefinition);
            event.registerLayerDefinition(CinnamonBoatRenderer.chestBoatTextureLocation(type), () -> chestBoatLayerDefinition);
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(EDEntityTypes.CINNAMON_BOAT.get(), context -> new CinnamonBoatRenderer(context, false));
        event.registerEntityRenderer(EDEntityTypes.CINNAMON_CHEST_BOAT.get(), context -> new CinnamonBoatRenderer(context, true));

        event.registerBlockEntityRenderer(EDBlockEntityTypes.CINNAMON_SIGN.get(), SignRenderer::new);
        event.registerBlockEntityRenderer(EDBlockEntityTypes.CINNAMON_HANGING_SIGN.get(), HangingSignRenderer::new);
    }
}
