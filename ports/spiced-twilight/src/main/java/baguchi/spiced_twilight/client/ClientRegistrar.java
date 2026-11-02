package baguchi.spiced_twilight.client;

import baguchi.spiced_twilight.SpicedTwilight;
import baguchi.spiced_twilight.api.ISpicedArmor;
import baguchi.spiced_twilight.client.render.MazeSlimeBeetleRenderer;
import baguchi.spiced_twilight.client.render.layer.CustomTFArmorLayer;
import baguchi.spiced_twilight.entity.ModEntities;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.SlimeBeetleModel;
import twilightforest.entity.monster.Kobold;
import twilightforest.init.TFEntities;

@Mod.EventBusSubscriber(modid = SpicedTwilight.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistrar {

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.MAZE_SLIME_BALL.get(), r -> new ThrownItemRenderer<>(r, 1.0F, false));
        // always the default slime beetle model: the maze texture is laid out for it, not for the Jappa pack model
        event.registerEntityRenderer(ModEntities.MAZE_SLIME_BEETLE.get(), m -> new MazeSlimeBeetleRenderer(m, new SlimeBeetleModel(m.bakeLayer(TFModelLayers.NEW_SLIME_BEETLE)), 0.6F));
    }

    @SubscribeEvent
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void onAddLayers(EntityRenderersEvent.AddLayers event) {
        LivingEntityRenderer<Kobold, ?> renderer = event.getRenderer(TFEntities.KOBOLD.get());
        // the kobold models get ISpicedArmor from KoboldModelMixin / NewKoboldModelMixin
        if (renderer != null && renderer.getModel() instanceof ISpicedArmor<?>) {
            ((LivingEntityRenderer) renderer).addLayer(new CustomTFArmorLayer(renderer, event.getContext()));
        }
    }
}
