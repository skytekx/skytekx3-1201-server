package de.ellpeck.actuallyadditions.mod.inventory.gui;

import de.ellpeck.actuallyadditions.mod.inventory.ItemTagContainer;
import de.ellpeck.actuallyadditions.mod.items.ItemTag;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.StringWidget;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nonnull;

// SkyTekx: the Item Tag screen of upstream 6f567725. Enter sends a tag that exists on the client
// to the server, which checks it again, and Enter on an empty box clears the tag. Main sends the
// text in a button packet that its menu ignores, and it keeps the last valid state when the text
// turns into a valid id of a tag that does not exist
@OnlyIn(Dist.CLIENT)
public class ItemTagScreen extends AAScreen<ItemTagContainer> {
    private EditBox tagBox;
    private StringWidget testText;
    private boolean validTag = false;

    public ItemTagScreen(ItemTagContainer container, Inventory inventory, Component title) {
        super(container, inventory, title);
    }

    @Override
    protected void init() {
        super.init();

        this.testText = new StringWidget(this.leftPos + 7, this.topPos + 27, 162, 20, Component.literal(""), this.font);
        this.addRenderableWidget(this.testText);

        this.tagBox = new EditBox(this.font, this.leftPos + 7, this.topPos + 7, 162, 20, Component.literal("Tag"));
        this.tagBox.setMaxLength(128);
        this.tagBox.setCanLoseFocus(false);
        this.setInitialFocus(this.tagBox);
        this.tagBox.setResponder(this::textChanged);
        this.tagBox.setValue(this.minecraft == null || this.minecraft.player == null ? "" : ItemTag.getTagLocation(this.minecraft.player.getInventory().getSelected()).map(ResourceLocation::toString).orElse(""));

        this.addRenderableWidget(this.tagBox);
    }

    @Override
    protected void renderBg(@Nonnull GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {

    }

    private void textChanged(String text) {
        ResourceLocation location = text.isEmpty() ? null : ResourceLocation.tryParse(text);
        this.validTag = location != null && BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, location)).isPresent();

        if (location == null) {
            this.tagBox.setTextColor(0xFF0000);
            this.testText.setMessage(Component.literal(""));
        } else {
            this.tagBox.setTextColor(0x00FF00);
            this.testText.setMessage(Component.literal(this.validTag ? "Valid Tag" : "Invalid Tag"));
            this.testText.setColor(this.validTag ? 0x00FF00 : 0xFF0000);
        }
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            this.minecraft.player.closeContainer();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            if (this.validTag) {
                PacketHelperClient.sendContainerStringPacket(this.tagBox.getValue(), 0);
                this.minecraft.player.closeContainer();
                return true;
            }
            if (this.tagBox.getValue().isEmpty()) {
                PacketHelperClient.sendContainerButtonPacket(0);
                this.minecraft.player.closeContainer();
                return true;
            }
        }

        return this.tagBox.keyPressed(keyCode, scanCode, modifiers) || this.tagBox.canConsumeInput() || super.keyPressed(keyCode, scanCode, modifiers);
    }
}
