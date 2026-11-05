package com.enderio.conduits.common.menu;

import com.enderio.api.conduit.ConduitType;
import com.enderio.api.conduit.SlotType;
import com.enderio.base.common.menu.AbstractFilterMenu;
import com.enderio.base.common.menu.FilterAccess;
import com.enderio.base.common.menu.FilterTarget;
import com.enderio.conduits.common.conduit.ConduitBundle;
import com.enderio.conduits.common.conduit.SlotData;
import com.enderio.conduits.common.conduit.block.ConduitBlockEntity;
import com.enderio.conduits.common.conduit.connection.DynamicConnectionState;
import com.enderio.conduits.common.init.EIOConduitTypes;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.IItemHandlerModifiable;

import java.util.Optional;

/**
 * A filter in a slot of a conduit connection, opened from the conduit screen, after InventoryFilterAccess of Ender IO 8.x
 * (04e69b8e6).
 * <p>
 * The menu edits a copy. On every broadcastChanges and on close, a changed copy goes back into the conduit slot through the
 * item handler of the conduit, which stores it with DynamicConnectionState.withItem, pushes the new state to the node so the
 * ticker filters with it, marks the block entity for saving and syncs the bundle. A copy is only written while the slot still
 * holds the very stack that this menu found or wrote last. When another player takes the filter out, replaces it or edits it in
 * a second filter menu, when the connection or the conduit goes or the bundle is broken, the slot holds something else, the menu
 * writes nothing and closes, so no filter is duplicated or lost.
 * <p>
 * Server side only. The client menu uses {@link FilterAccess.Remote}.
 */
public class ConduitFilterAccess implements FilterAccess {

    private final ConduitBlockEntity conduit;
    private final ConduitType<?> type;
    private final Direction direction;
    private final SlotType slotType;

    private final ItemStack working;
    private ItemStack inConduit;
    private ItemStack saved;

    private ConduitFilterAccess(ConduitBlockEntity conduit, ConduitType<?> type, Direction direction, SlotType slotType, ItemStack inConduit) {
        this.conduit = conduit;
        this.type = type;
        this.direction = direction;
        this.slotType = slotType;
        this.inConduit = inConduit;
        this.working = inConduit.copy();
        this.saved = inConduit.copy();
    }

    /**
     * @return the access to the filter in this slot, or empty when the conduit has no such connection slot or the slot holds no
     * filter with a menu
     */
    public static Optional<ConduitFilterAccess> of(ConduitBlockEntity conduit, ConduitType<?> type, Direction direction, SlotType slotType) {
        if (slotType == SlotType.UPGRADE_EXTRACT || !slotType.isAvailableFor(type.getMenuData())) {
            return Optional.empty();
        }

        ItemStack stack = getSlotStack(conduit, type, direction, slotType);
        if (stack.isEmpty() || !AbstractFilterMenu.hasMenu(stack)) {
            return Optional.empty();
        }

        return Optional.of(new ConduitFilterAccess(conduit, type, direction, slotType, stack));
    }

    @Override
    public FilterTarget target() {
        return new FilterTarget.Conduit(conduit.getBlockPos(), EIOConduitTypes.REGISTRY.get().getKey(type), direction, slotType);
    }

    @Override
    public ItemStack getStack() {
        return working;
    }

    @Override
    public void writeExtraData(FriendlyByteBuf buf) {
        target().write(buf);
        buf.writeItem(working);
    }

    @Override
    public boolean stillValid(Player player) {
        return conduit.stillValid(player) && isUnchanged();
    }

    @Override
    public void saveChanges() {
        if (!isUnchanged() || ItemStack.matches(working, saved)) {
            return;
        }

        ConduitBundle bundle = conduit.getBundle();
        ItemStack copy = working.copy();
        int slot = new SlotData(direction, bundle.getTypeIndex(type), slotType).slotIndex();
        if (!(conduit.getConduitItemHandler() instanceof IItemHandlerModifiable itemHandler)) {
            return;
        }
        itemHandler.setStackInSlot(slot, copy);

        // The handler stores the stack it gets. If it refused, the slot keeps the old stack and the menu closes.
        if (getSlotStack(conduit, type, direction, slotType) == copy) {
            inConduit = copy;
            saved = copy.copy();
        }
    }

    @Override
    public boolean goBack(ServerPlayer player) {
        if (!stillValid(player)) {
            return false;
        }

        conduit.openMenu(player, direction, type);
        return true;
    }

    /**
     * @return whether the conduit slot still holds the stack that this menu found or wrote last
     */
    private boolean isUnchanged() {
        return !conduit.isRemoved() && getSlotStack(conduit, type, direction, slotType) == inConduit;
    }

    private static ItemStack getSlotStack(ConduitBlockEntity conduit, ConduitType<?> type, Direction direction, SlotType slotType) {
        ConduitBundle bundle = conduit.getBundle();
        if (!bundle.hasType(type)) {
            return ItemStack.EMPTY;
        }

        if (bundle.getConnectionState(direction, type) instanceof DynamicConnectionState dyn) {
            return dyn.getItem(slotType);
        }

        return ItemStack.EMPTY;
    }
}
