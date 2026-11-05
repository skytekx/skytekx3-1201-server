package com.enderio.base.common.menu;

import com.enderio.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.core.common.capability.EntityFilterCapability;
import com.enderio.core.common.capability.FluidFilterCapability;
import com.enderio.core.common.capability.ItemFilterCapability;
import com.enderio.core.common.capability.LimitedItemFilterCapability;
import io.netty.buffer.Unpooled;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkHooks;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

/**
 * The common part of the item, fluid, entity and limited item filter menus, after AbstractFilterMenu of Ender IO 8.x
 * (04e69b8e6, 7fe86947b). The menus edit the stack of a {@link FilterAccess}: the main hand as in 6.x, or a filter in a
 * conduit slot that the conduit screen opened.
 */
public abstract class AbstractFilterMenu extends AbstractContainerMenu {

    /**
     * The menu button of the back arrow. Ids from {@link ItemFilterMenu#DAMAGE_MODE_BUTTON_ID} on are the damage modes.
     */
    public static final int BACK_BUTTON_ID = 0;

    /**
     * NetworkHooks.openScreen refuses more than 32600 bytes of extra data, after it has closed the open menu. Bigger filters
     * are not opened from a conduit at all.
     */
    private static final int MAX_EXTRA_DATA_BYTES = 32000;

    protected final FilterAccess access;
    protected final ItemStack stack;

    protected AbstractFilterMenu(@Nullable MenuType<?> menuType, int containerId, FilterAccess access) {
        super(menuType, containerId);
        this.access = access;
        this.stack = access.getStack();
    }

    public FilterAccess getAccess() {
        return access;
    }

    /**
     * @return the stack the menu edits
     */
    public ItemStack getFilterStack() {
        return stack;
    }

    /**
     * @return true when the filter lives somewhere else than the main hand, so the back arrow returns to that menu
     */
    public boolean hasParentMenu() {
        return !(access.target() instanceof FilterTarget.Hand);
    }

    @Override
    public ItemStack quickMoveStack(Player pPlayer, int pIndex) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return access.stillValid(pPlayer);
    }

    @Override
    public void broadcastChanges() {
        access.saveChanges();
        super.broadcastChanges();
    }

    @Override
    public void removed(Player player) {
        if (player instanceof ServerPlayer) {
            access.saveChanges();
        }
        super.removed(player);
    }

    @Override
    public boolean clickMenuButton(Player player, int id) {
        if (id == BACK_BUTTON_ID) {
            if (player instanceof ServerPlayer serverPlayer && !access.goBack(serverPlayer)) {
                serverPlayer.closeContainer();
            }
            return true;
        }

        return super.clickMenuButton(player, id);
    }

    public void addInventorySlots(int xPos, int yPos, Inventory inventory) {

        // The slot that holds the opened filter is locked, so the stack the menu edits cannot be moved or duplicated.

        // Hotbar
        for (int x = 0; x < 9; x++) {
            Slot ref = new Slot(inventory, x, xPos + x * 18, yPos + 58) {

                @Override
                public boolean mayPickup(Player player) {
                    return !this.getItem().equals(AbstractFilterMenu.this.stack);
                }
            };
            this.addSlot(ref);
        }

        // Inventory
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 9; x++) {
                Slot ref = new Slot(inventory, x + y * 9 + 9, xPos + x * 18, yPos + y * 18) {

                    @Override
                    public boolean mayPickup(Player player) {
                        return !this.getItem().equals(AbstractFilterMenu.this.stack);
                    }
                };
                this.addSlot(ref);
            }
        }
    }

    // region Opening

    /**
     * @return whether the stack holds a filter that one of these menus edits
     */
    public static boolean hasMenu(ItemStack stack) {
        return getFilter(stack)
            .filter(filter -> filter instanceof ItemFilterCapability || filter instanceof LimitedItemFilterCapability
                || filter instanceof FluidFilterCapability || filter instanceof EntityFilterCapability)
            .isPresent();
    }

    /**
     * The menu for the filter of the access, chosen by its capability like the use method of each filter item.
     */
    @Nullable
    public static AbstractFilterMenu create(int containerId, Inventory inventory, FilterAccess access) {
        Optional<ResourceFilter> filter = getFilter(access.getStack());
        if (filter.isEmpty()) {
            return null;
        }

        if (filter.get() instanceof LimitedItemFilterCapability) {
            return new LimitedItemFilterMenu(containerId, inventory, access);
        }
        if (filter.get() instanceof ItemFilterCapability) {
            return new ItemFilterMenu(containerId, inventory, access);
        }
        if (filter.get() instanceof FluidFilterCapability) {
            return new FluidFilterMenu(containerId, inventory, access);
        }
        if (filter.get() instanceof EntityFilterCapability) {
            return new EntityFilterMenu(containerId, inventory, access);
        }
        return null;
    }

    /**
     * Open the menu of a filter that is not in the main hand. The client menu gets the target and a copy of the stack.
     * @return false when the stack holds no filter with a menu, or is too big to send
     */
    public static boolean open(ServerPlayer player, FilterAccess access) {
        if (!hasMenu(access.getStack())) {
            return false;
        }

        FriendlyByteBuf extraData = new FriendlyByteBuf(Unpooled.buffer());
        try {
            access.writeExtraData(extraData);
            if (extraData.readableBytes() > MAX_EXTRA_DATA_BYTES) {
                return false;
            }
        } finally {
            extraData.release();
        }

        // The item and limited filters show the name of their stack, the others none, as their use methods do.
        Optional<ResourceFilter> filter = getFilter(access.getStack());
        Component title = filter.isPresent() && (filter.get() instanceof ItemFilterCapability || filter.get() instanceof LimitedItemFilterCapability)
            ? access.getStack().getHoverName()
            : Component.literal("");

        NetworkHooks.openScreen(player, new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return title;
            }

            @Override
            public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player menuPlayer) {
                return AbstractFilterMenu.create(containerId, inventory, access);
            }
        }, access::writeExtraData);
        return true;
    }

    private static Optional<ResourceFilter> getFilter(ItemStack stack) {
        if (stack.isEmpty()) {
            return Optional.empty();
        }
        return stack.getCapability(EIOCapabilities.FILTER).resolve();
    }

    // endregion
}
