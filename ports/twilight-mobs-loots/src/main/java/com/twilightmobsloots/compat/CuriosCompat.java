package com.twilightmobsloots.compat;

import com.twilightmobsloots.MobTrophyItem;
import com.twilightmobsloots.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.registries.RegistryObject;

/**
 * Curios compatibility (Curios 5.x for 1.20.1): registers every mob trophy as a curio.
 *
 * <p>Curios is an <b>optional</b> dependency, so every Curios class reference is isolated in
 * {@link CuriosTrophies} and only reached once Curios is known to be loaded. Which slots accept a trophy
 * is decided by the item tags {@code data/curios/tags/items/head.json} and {@code trophy.json} (the
 * {@code trophy} slot itself is declared in {@code data/curios/curios/slots/trophy.json}).
 *
 * <p>Upstream also registers Twilight Forest's nine boss trophies here. The 1.20.1 Twilight Forest fork
 * already gives them a curio capability ({@code twilightforest.item.CurioItem}) and a Curios head renderer,
 * so this port leaves them alone.
 */
public final class CuriosCompat {

    public static final String CURIOS_MOD_ID = "curios";

    private CuriosCompat() {
    }

    /** Whether Curios is loaded (optional dependency switch). */
    public static boolean isLoaded() {
        return ModList.get().isLoaded(CURIOS_MOD_ID);
    }

    public static void init(IEventBus modEventBus) {
        if (isLoaded()) {
            modEventBus.addListener(CuriosCompat::onCommonSetup);
        }
    }

    private static void onCommonSetup(FMLCommonSetupEvent event) {
        // Curios' curio registry is a plain map: register on the main thread.
        event.enqueueWork(CuriosCompat::registerTrophiesAsCurios);
    }

    /** Plain decorative curios: every {@code ICurioItem} method keeps its default. */
    private static void registerTrophiesAsCurios() {
        for (RegistryObject<MobTrophyItem> holder : ModItems.TROPHIES) {
            CuriosTrophies.register(holder.get());
        }
    }

    /** Holds the Curios class references; only loaded when Curios is present. */
    private static final class CuriosTrophies {
        private static final top.theillusivec4.curios.api.type.capability.ICurioItem DECORATIVE =
                new top.theillusivec4.curios.api.type.capability.ICurioItem() {
                };

        private static void register(net.minecraft.world.item.Item item) {
            top.theillusivec4.curios.api.CuriosApi.registerCurio(item, DECORATIVE);
        }
    }
}
