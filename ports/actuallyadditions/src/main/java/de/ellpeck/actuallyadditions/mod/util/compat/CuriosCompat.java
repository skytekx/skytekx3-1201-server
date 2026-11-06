package de.ellpeck.actuallyadditions.mod.util.compat;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;

import java.util.function.Predicate;

/**
 * SkyTekx: every call of the mod into the Curios API (b5cb3434). Main calls CuriosApi from the
 * common class ItemCrafterOnAStick. Callers check Util.curiosLoaded first, so this class only
 * loads when Curios is there, and a server without Curios never resolves a Curios class.
 */
public final class CuriosCompat {

    private CuriosCompat() {
    }

    /**
     * Whether a Curios slot of the entity holds a stack that matches the predicate. On Curios 5
     * for 1.20.1 the curios inventory is a LazyOptional, on the 1.21 line an Optional.
     */
    public static boolean hasCurio(LivingEntity entity, Predicate<ItemStack> predicate) {
        return CuriosApi.getCuriosInventory(entity).resolve()
                .flatMap(inventory -> inventory.findFirstCurio(predicate))
                .isPresent();
    }
}
