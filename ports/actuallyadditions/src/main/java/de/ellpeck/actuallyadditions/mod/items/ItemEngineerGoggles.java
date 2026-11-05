/*
 * This file ("ItemInfraredGoggles.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.api.misc.IGoggles;
import de.ellpeck.actuallyadditions.mod.items.base.ItemArmorAA;
import de.ellpeck.actuallyadditions.mod.material.ArmorMaterials;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class ItemEngineerGoggles extends ItemArmorAA implements IGoggles {

    private final boolean displayMobs;

    // SkyTekx: the client tick that made mobs glow moved to ClientEvents (upstream 844efe42), so
    // this common item no longer registers itself on the Forge bus or names Minecraft
    public ItemEngineerGoggles(boolean displayMobs) {
        super(ArmorMaterials.GOGGLES, Type.HELMET, ActuallyItems.defaultProps().setNoRepair().durability(0));
        this.displayMobs = displayMobs;
    }

    public static boolean isWearing(Player player) {
        ItemStack face = player.getInventory().armor.get(3);
        return StackUtil.isValid(face) && face.getItem() instanceof IGoggles;
    }

    @Override
    public boolean displaySpectralMobs() {
        return this.displayMobs;
    }
}
