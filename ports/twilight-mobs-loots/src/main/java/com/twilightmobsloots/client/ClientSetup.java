package com.twilightmobsloots.client;

import com.twilightmobsloots.MobTrophyItem;
import com.twilightmobsloots.ModBlockEntities;
import com.twilightmobsloots.compat.CuriosCompat;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

/**
 * Client-only setup (called from the mod constructor on the client dist only):
 * <ul>
 *     <li>registers the trophy block entity renderer {@link MobTrophyRenderer} (floor and wall heads);</li>
 *     <li>the trophy items render through {@link MobTrophyISTER} (see {@link TrophyItemClientExtensions});</li>
 *     <li>registers the flat GUI icon model;</li>
 *     <li>hides the player's own head while a trophy is worn, so the 3D head does not clip with it;</li>
 *     <li>Curios (optional): registers the Curios slot renderer for the trophies.</li>
 * </ul>
 */
public class ClientSetup {

    public static void init(IEventBus modEventBus) {
        modEventBus.addListener(ClientSetup::onRegisterRenderers);
        modEventBus.addListener(ClientSetup::onRegisterAdditionalModels);
        modEventBus.addListener(ClientSetup::onRegisterReloadListeners);
        modEventBus.addListener(ClientSetup::onClientSetup);
        MinecraftForge.EVENT_BUS.addListener(ClientSetup::onRenderPlayer);
    }

    /**
     * Hides the player's head and hat while a trophy is worn: in the vanilla head slot (this mod's trophies
     * or Twilight Forest's) or in a Curios slot. The player renderer resets visibility every frame.
     */
    private static void onRenderPlayer(RenderPlayerEvent.Pre event) {
        if (isTrophy(event.getEntity().getItemBySlot(EquipmentSlot.HEAD))
                || (CuriosCompat.isLoaded() && CuriosClientCompat.hasTrophyInCurios(event.getEntity()))) {
            PlayerModel<?> model = event.getRenderer().getModel();
            model.head.visible = false;
            model.hat.visible = false;
        }
    }

    /** Whether the stack is a trophy: one of this mod's, or a Twilight Forest {@code twilightforest:*_trophy}. */
    static boolean isTrophy(ItemStack stack) {
        Item item = stack.getItem();
        if (item instanceof MobTrophyItem) {
            return true;
        }
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key.getNamespace().equals("twilightforest") && key.getPath().endsWith("_trophy");
    }

    private static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.MOB_TROPHY.get(), MobTrophyRenderer::new);
    }

    private static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(MobTrophyISTER.SILVER_ICON_MODEL);
    }

    /** Re-bake the item renderer's heads after a resource reload (model layers or the Jappa pack changed). */
    private static void onRegisterReloadListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener((ResourceManagerReloadListener) manager -> TrophyItemClientExtensions.invalidate());
    }

    private static void onClientSetup(FMLClientSetupEvent event) {
        if (CuriosCompat.isLoaded()) {
            event.enqueueWork(CuriosClientCompat::registerRenderers);
        }
    }
}
