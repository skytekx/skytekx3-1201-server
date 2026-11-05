package de.ellpeck.actuallyadditions.mod.blocks.blockhuds;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * SkyTekx: the client side registry of the look at overlays. Upstream 668f7ffe lets each block
 * return its IBlockHud through IHudDisplay.getHud(), so the common block class holds an instance
 * of a client class. Here ClientEvents registers the overlays by block or item class on the
 * physical client, and the common classes only carry the marker IHudDisplay.
 */
@OnlyIn(Dist.CLIENT)
public final class BlockHuds {

    private static final Map<Class<?>, IBlockHud> HUDS = new LinkedHashMap<>();
    private static final Map<Class<?>, Optional<IBlockHud>> CACHE = new HashMap<>();

    private BlockHuds() {
    }

    /**
     * Registers the overlay of a block or item class and of its subclasses.
     */
    public static void register(Class<?> holder, IBlockHud hud) {
        HUDS.put(holder, hud);
        CACHE.clear();
    }

    /**
     * @return the overlay registered for the class of the given block or item, or null
     */
    @Nullable
    public static IBlockHud get(Object holder) {
        return CACHE.computeIfAbsent(holder.getClass(), BlockHuds::find).orElse(null);
    }

    private static Optional<IBlockHud> find(Class<?> type) {
        for (Map.Entry<Class<?>, IBlockHud> entry : HUDS.entrySet()) {
            if (entry.getKey().isAssignableFrom(type)) {
                return Optional.of(entry.getValue());
            }
        }
        return Optional.empty();
    }
}
