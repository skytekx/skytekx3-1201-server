/*
 * This file ("ContainerFireworkBox.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory;

import de.ellpeck.actuallyadditions.mod.tile.TileEntityFireworkBox;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;

public class ContainerFireworkBox extends AbstractContainerMenu {

    // SkyTekx: the box this menu belongs to, as on upstream main. The base menu knew no box, so its
    // stillValid returned true and the screen stayed open at any distance. The client looks the box
    // up from the position that BlockContainerBase sends with the menu, and may find none
    @Nullable
    public final TileEntityFireworkBox fireworkbox;

    public static ContainerFireworkBox fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        BlockEntity tile = data.readableBytes() > 0 ? inv.player.level().getBlockEntity(data.readBlockPos()) : null;
        return new ContainerFireworkBox(windowId, inv, tile instanceof TileEntityFireworkBox box ? box : null);
    }

    public ContainerFireworkBox(int windowId, Inventory inventory, @Nullable TileEntityFireworkBox tile) {
        super(ActuallyContainers.FIREWORK_BOX_CONTAINER.get(), windowId);
        this.fireworkbox = tile;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player playerIn) {
        return this.fireworkbox != null && this.fireworkbox.canPlayerUse(playerIn);
    }
}
