package com.enderio.base.common.menu;

import com.enderio.api.UseOnly;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

/**
 * How a filter menu reaches the stack it edits, after AbstractFilterMenu.FilterAccess of Ender IO 8.x (04e69b8e6). The main
 * hand edits its stack in place, as 6.x always did. A filter in a conduit is edited as a copy, which the server writes back
 * into the conduit slot (see the conduits module).
 */
public interface FilterAccess {

    FilterTarget target();

    /**
     * @return the stack that the menu reads and changes
     */
    ItemStack getStack();

    boolean stillValid(Player player);

    /**
     * Write the changes of the stack back to where it lives. The menu calls this on the server with every broadcastChanges and
     * when it closes.
     */
    @UseOnly(LogicalSide.SERVER)
    default void saveChanges() {
    }

    /**
     * Reopen the menu the filter was opened from.
     * @return false when there is none
     */
    @UseOnly(LogicalSide.SERVER)
    default boolean goBack(ServerPlayer player) {
        return false;
    }

    /**
     * Data for the client menu after the target, see {@link #fromNetwork(Inventory, FriendlyByteBuf)}.
     */
    default void writeExtraData(FriendlyByteBuf buf) {
        target().write(buf);
    }

    static FilterAccess hand(ItemStack mainHandStack) {
        return new Hand(mainHandStack);
    }

    /**
     * The client side access of a menu, from the buffer that the server wrote with {@link #writeExtraData(FriendlyByteBuf)}.
     * A filter outside the main hand comes with a copy of its stack, so the client menu matches the menu type the server chose.
     */
    @UseOnly(LogicalSide.CLIENT)
    static FilterAccess fromNetwork(Inventory inventory, @Nullable FriendlyByteBuf buf) {
        FilterTarget target = FilterTarget.read(buf);
        if (target instanceof FilterTarget.Hand || buf == null) {
            return hand(inventory.player.getMainHandItem());
        }

        return new Remote(target, buf.readItem());
    }

    /**
     * The filter in the main hand, edited in place. The menu stays open while the main hand holds this very stack.
     */
    record Hand(ItemStack stack) implements FilterAccess {
        @Override
        public FilterTarget target() {
            return FilterTarget.HAND;
        }

        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public boolean stillValid(Player player) {
            return player.getMainHandItem().equals(stack);
        }
    }

    /**
     * The client copy of a filter that lives elsewhere. The server decides when the menu closes.
     */
    record Remote(FilterTarget target, ItemStack stack) implements FilterAccess {
        @Override
        public ItemStack getStack() {
            return stack;
        }

        @Override
        public boolean stillValid(Player player) {
            return true;
        }
    }
}
