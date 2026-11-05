package com.enderio.conduits.common.conduit.facades;

import com.enderio.base.common.tag.EIOTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Ender IO 7.1.1 (7de602e53): whether a player sees the facades. A player who holds an item of #enderio:hide_facades, the Yeta wrench
 * or the conduit probe, sees and reaches the conduits behind them. This reads only the held items, so the server, which has no client
 * state, gives the same answer as the client.
 */
public final class FacadeUtil {

    private FacadeUtil() {
    }

    public static boolean areFacadesVisible(@Nullable Player player) {
        if (player == null) {
            return true;
        }

        return areFacadesVisible(player.getMainHandItem()) && areFacadesVisible(player.getOffhandItem());
    }

    public static boolean areFacadesVisible(ItemStack stack) {
        return !stack.is(EIOTags.Items.HIDE_FACADES);
    }
}
