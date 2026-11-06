package de.ellpeck.actuallyadditions.mod.inventory;

import de.ellpeck.actuallyadditions.mod.items.ItemTag;
import de.ellpeck.actuallyadditions.mod.network.gui.IButtonReactor;
import de.ellpeck.actuallyadditions.mod.network.gui.IStringReactor;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

// SkyTekx: the menu of the Item Tag screen of upstream 6f567725, new menu id
// actuallyadditions:item_tag_container. Main registers it but never opens it, and its button
// handler only clears the tag, so the tag the screen sends was lost. Here the screen sends the tag
// as text through PacketHandler.GUI_STRING_TO_CONTAINER_HANDLER, which drops a text longer than
// ItemTag.MAX_TAG_LENGTH, and the menu sets it after ResourceLocation.tryParse when the item tag
// exists. Button 0 clears the tag. The menu is bound to the hotbar slot and the stack it opened
// with, as the other item menus
public class ItemTagContainer extends AbstractContainerMenu implements IButtonReactor, IStringReactor {
    private final Inventory inventory;
    private final int selectedSlot;
    private final ItemStack tagStack;

    public ItemTagContainer(int windowId, Inventory playerInventory) {
        super(ActuallyContainers.ITEM_TAG_CONTAINER.get(), windowId);
        this.inventory = playerInventory;
        this.selectedSlot = playerInventory.selected;
        this.tagStack = playerInventory.getSelected();
    }

    public static ItemTagContainer fromNetwork(int windowId, Inventory inv, FriendlyByteBuf data) {
        return new ItemTagContainer(windowId, inv);
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return this.inventory.selected == this.selectedSlot && this.inventory.getSelected() == this.tagStack && this.tagStack.getItem() instanceof ItemTag;
    }

    @Override
    public void onButtonPressed(int buttonID, Player player) {
        if (buttonID == 0) {
            ItemTag.clearTag(this.tagStack);
        }
    }

    @Override
    public void onTextReceived(String text, int textID, Player player) {
        if (textID != 0 || text.length() > ItemTag.MAX_TAG_LENGTH) {
            return;
        }
        ResourceLocation location = ResourceLocation.tryParse(text);
        if (location != null && BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, location)).isPresent()) {
            ItemTag.setTag(this.tagStack, location);
        }
    }
}
