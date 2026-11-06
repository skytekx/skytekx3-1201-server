package de.ellpeck.actuallyadditions.mod.inventory;

import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotDeletion;
import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotFilter;
import de.ellpeck.actuallyadditions.mod.inventory.slot.SlotImmovable;
import de.ellpeck.actuallyadditions.mod.items.Sack;
import de.ellpeck.actuallyadditions.mod.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.mod.tile.FilterSettings;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

// SkyTekx: the menu of the Void Sack of main (87601c1e, 521cf822, bc20d82f, 6ab2a067), new menu id
// actuallyadditions:void_sack_container. Its deletion slot destroys what the filter lets through.
// The filter and the pickup toggle live in the stack's NBT keys Filter and AutoInsert, where main
// has the data components FILTER_SETTINGS and AUTO_INSERT, and every change goes into the stack at
// once, as the drill does since its menu fix. The base opened the Traveler's Sack menu for the Void
// Sack as well, without a deletion slot
public class VoidSackContainer extends AbstractContainerMenu implements IButtonReactor {
    public final FilterSettings filter;

    private final ItemStackHandlerAA bagInventory;
    private final Inventory inventory;
    public boolean autoInsert;
    // The hotbar slot and the sack stack of the moment the menu opened, as in SackContainer
    private final int selectedSlot;
    private final ItemStack sackStack;

    public static VoidSackContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        boolean autoInsert = data.readBoolean();
        int filterOptions = data.readInt();
        return new VoidSackContainer(windowId, inv, autoInsert, new FilterSettings(Sack.FILTER_SLOTS, filterOptions));
    }

    public VoidSackContainer(int windowId, Inventory playerInventory, boolean autoInsert, FilterSettings filterIn) {
        super(ActuallyContainers.VOID_SACK_CONTAINER.get(), windowId);

        this.filter = filterIn;
        this.inventory = playerInventory;
        this.bagInventory = new ItemStackHandlerAA(1);
        this.autoInsert = autoInsert;
        this.selectedSlot = playerInventory.selected;
        this.sackStack = playerInventory.getSelected();

        // 6ab2a067: the filter slots sit 18 pixels apart, on the slots of the texture
        for (int c = 0; c < 4; c++) {
            this.addSlot(new SlotFilter(this.filter, c, 98 + c * 18, 17));
        }

        this.addSlot(new SlotDeletion(this.bagInventory, 0, 13, 18) {
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return VoidSackContainer.this.filter.check(stack);
            }
        });

        // Player Inventory
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 9; col++) {
                this.addSlot(new Slot(this.inventory, col + row * 9 + 9, 8 + col * 18, 47 + row * 18));
            }
        }

        // Player Hotbar
        for (int i = 0; i < 9; i++) {
            if (i == this.inventory.selected) {
                this.addSlot(new SlotImmovable(this.inventory, i, 8 + i * 18, 105));
            } else {
                this.addSlot(new Slot(this.inventory, i, 8 + i * 18, 105));
            }
        }
    }

    @Override
    public void clicked(int slotId, int dragType, @Nonnull ClickType clickTypeIn, @Nonnull Player player) {
        if (SlotFilter.checkFilter(this, slotId, player)) {
            this.saveToStack(player);
        } else if (clickTypeIn != ClickType.SWAP || dragType != this.inventory.selected) {
            super.clicked(slotId, dragType, clickTypeIn, player);
        }
    }

    @Override
    public void removed(@Nonnull Player player) {
        this.saveToStack(player);
        super.removed(player);
    }

    // Button 0 toggles the pickup, the others are the filter buttons with the offset 1
    @Override
    public void onButtonPressed(int buttonID, Player player) {
        if (buttonID == 0) {
            this.autoInsert = !this.autoInsert;
            this.saveToStack(player);
        } else if (this.filter.onButtonPressed(buttonID - 1)) {
            this.saveToStack(player);
        }
    }

    // Only the server writes the stack, the client gets it with the slot sync
    private void saveToStack(Player player) {
        if (!player.level().isClientSide && isVoidSack(this.sackStack)) {
            Sack.setAutoInsert(this.sackStack, this.autoInsert);
            Sack.writeVoidFilter(this.sackStack, this.filter);
        }
    }

    private static boolean isVoidSack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof Sack sack && sack.isVoid;
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return ItemStack.EMPTY; //TODO old behavior was shift clicking moved stacks around the normal inventory
    }

    // SkyTekx: valid while the selected hotbar slot still holds the sack it was opened with
    @Override
    public boolean stillValid(@Nonnull Player player) {
        return this.inventory.selected == this.selectedSlot && this.inventory.getSelected() == this.sackStack && isVoidSack(this.sackStack);
    }
}
