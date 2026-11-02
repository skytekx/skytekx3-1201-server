package com.twilightmobsloots.client;

import com.twilightmobsloots.MobTrophyItem;
import com.twilightmobsloots.ModItems;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.items.IItemHandlerModifiable;
import net.minecraftforge.registries.RegistryObject;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.client.CuriosRendererRegistry;

/**
 * Client-side Curios hooks. Only loaded when Curios is present ({@code CuriosCompat.isLoaded()}).
 *
 * <p>Upstream also registers {@link TrophyCurioRenderer} for Twilight Forest's nine boss trophies. The
 * 1.20.1 fork already registers its own equivalent {@code CurioHeadRenderer} for them, so only this mod's
 * trophies are registered here.
 */
final class CuriosClientCompat {

    private CuriosClientCompat() {
    }

    /** Trophies in a Curios slot (head or trophy) render as 3D heads. */
    static void registerRenderers() {
        for (RegistryObject<MobTrophyItem> holder : ModItems.TROPHIES) {
            CuriosRendererRegistry.register(holder.get(), TrophyCurioRenderer::new);
        }
    }

    /** Whether any trophy (this mod's or Twilight Forest's) is equipped in any Curios slot. */
    static boolean hasTrophyInCurios(LivingEntity entity) {
        return CuriosApi.getCuriosInventory(entity)
                .map(handler -> {
                    IItemHandlerModifiable stacks = handler.getEquippedCurios();
                    for (int i = 0; i < stacks.getSlots(); i++) {
                        if (ClientSetup.isTrophy(stacks.getStackInSlot(i))) {
                            return true;
                        }
                    }
                    return false;
                }).orElse(false);
    }
}
