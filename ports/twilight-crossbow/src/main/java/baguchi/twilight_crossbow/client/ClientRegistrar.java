package baguchi.twilight_crossbow.client;

import baguchi.twilight_crossbow.TwilightCrossBow;
import baguchi.twilight_crossbow.init.ModEntities;
import baguchi.twilight_crossbow.init.ModItems;
import net.minecraft.client.renderer.entity.FireworkEntityRenderer;
import net.minecraft.client.renderer.item.ClampedItemPropertyFunction;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client setup. Upstream registers the item properties in ModelEvent.ModifyBakingResult and has an empty
 * TwilightCrossBowClient (config screen only, no configs); both are folded into this one subscriber.
 */
@Mod.EventBusSubscriber(modid = TwilightCrossBow.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ClientRegistrar {

    @SubscribeEvent
    public static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            registerCrossbowProperties(ModItems.TRIPLE_CROSSBOW.get());
            registerCrossbowProperties(ModItems.ICE_CROSSBOW.get());
            registerCrossbowProperties(ModItems.SEEKER_CROSSBOW.get());
            registerCrossbowProperties(ModItems.ENDER_CROSSBOW.get());
        });
    }

    /**
     * The vanilla 1.20.1 crossbow model predicates (net.minecraft.client.renderer.item.ItemProperties).
     */
    private static void registerCrossbowProperties(Item crossbow) {
        ItemProperties.register(crossbow, new ResourceLocation("pull"), (ClampedItemPropertyFunction) (stack, level, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(stack)
                        ? 0.0F
                        : (float) (stack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float) CrossbowItem.getChargeDuration(stack);
            }
        });
        ItemProperties.register(crossbow, new ResourceLocation("pulling"), (ClampedItemPropertyFunction) (stack, level, entity, seed) -> entity != null
                && entity.isUsingItem()
                && entity.getUseItem() == stack
                && !CrossbowItem.isCharged(stack)
                ? 1.0F
                : 0.0F
        );
        ItemProperties.register(crossbow, new ResourceLocation("charged"), (ClampedItemPropertyFunction) (stack, level, entity, seed) -> CrossbowItem.isCharged(stack) ? 1.0F : 0.0F);
        ItemProperties.register(crossbow, new ResourceLocation("firework"), (ClampedItemPropertyFunction) (stack, level, entity, seed) ->
                CrossbowItem.isCharged(stack) && CrossbowItem.containsChargedProjectile(stack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F);
    }

    @SubscribeEvent
    public static void registerEntityRenders(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.ICE_FIREWORK.get(), FireworkEntityRenderer::new);
        event.registerEntityRenderer(ModEntities.SEEKER_FIREWORK.get(), FireworkEntityRenderer::new);
    }
}
