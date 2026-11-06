/*
 * This file ("FilterSettingsGui.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.inventory.gui;

import de.ellpeck.actuallyadditions.mod.tile.FilterSettings;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// SkyTekx: the filter buttons of main (85725a0f, 79c8bcfb, adc276e8, 87601c1e, 6389668b, f46de7d9,
// bc5dc448). Each button toggles the client copy of the settings and hands its id, idOffset plus
// the ordinal of FilterSettings.Buttons, to clickConsumer, which sends it to the server. The base
// built three buttons without an action and set all three labels on the whitelist button
@OnlyIn(Dist.CLIENT)
public class FilterSettingsGui {

    // The width the 1.12 code wrapped these tooltips at (listFormattedStringToWidth(..., 200)),
    // main leaves them unwrapped with a TODO
    private static final int TOOLTIP_WIDTH = 200;

    private final FilterSettings theSettings;

    public Button whitelistButton;
    public Button modButton;
    public Button damageButton;
    public Button nbtButton;

    public FilterSettingsGui(FilterSettings settings, int x, int y, boolean vertical, Consumer<AbstractButton> buttonConsumer, Consumer<Integer> clickConsumer, int idOffset) {
        this.theSettings = settings;

        this.whitelistButton = Button.builder(Component.literal("WH"), $ -> {
                    this.theSettings.isWhitelist = !this.theSettings.isWhitelist;
                    clickConsumer.accept(idOffset + FilterSettings.Buttons.WHITELIST.ordinal());
                })
                .bounds(x, y, 16, 12).build();
        buttonConsumer.accept(this.whitelistButton);

        if (vertical) {
            y += 14;
        } else {
            x += 18;
        }

        this.modButton = Button.builder(Component.literal("MO"), $ -> {
                    this.theSettings.respectMod = !this.theSettings.respectMod;
                    clickConsumer.accept(idOffset + FilterSettings.Buttons.MOD.ordinal());
                })
                .bounds(x, y, 16, 12).build();
        buttonConsumer.accept(this.modButton);

        if (vertical) {
            y += 14;
        } else {
            x += 18;
        }

        this.damageButton = Button.builder(Component.literal("DM"), $ -> {
                    this.theSettings.matchDamage = !this.theSettings.matchDamage;
                    clickConsumer.accept(idOffset + FilterSettings.Buttons.DAMAGE.ordinal());
                })
                .bounds(x, y, 16, 12).build();
        buttonConsumer.accept(this.damageButton);

        if (vertical) {
            y += 14;
        } else {
            x += 18;
        }

        // f46de7d9: the NBT button sends idOffset + 3, 1.20.4 sent the id of the damage button
        this.nbtButton = Button.builder(Component.literal("DA"), $ -> {
                    this.theSettings.matchNBT = !this.theSettings.matchNBT;
                    clickConsumer.accept(idOffset + FilterSettings.Buttons.NBT.ordinal());
                })
                .bounds(x, y, 16, 12).build();
        buttonConsumer.accept(this.nbtButton);

        this.tick();
    }

    public void tick() {
        this.whitelistButton.setMessage(Component.literal("WH").withStyle(this.theSettings.isWhitelist
            ? ChatFormatting.DARK_GREEN
            : ChatFormatting.RED));
        this.modButton.setMessage(Component.literal("MO").withStyle(this.theSettings.respectMod
            ? ChatFormatting.DARK_GREEN
            : ChatFormatting.RED));
        this.damageButton.setMessage(Component.literal("DM").withStyle(this.theSettings.matchDamage
            ? ChatFormatting.DARK_GREEN
            : ChatFormatting.RED));
        this.nbtButton.setMessage(Component.literal("DA").withStyle(this.theSettings.matchNBT
            ? ChatFormatting.DARK_GREEN
            : ChatFormatting.RED));
    }

    public void drawHover(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        Minecraft mc = Minecraft.getInstance();
        List<Component> list = new ArrayList<>();
        if (this.whitelistButton.isMouseOver(mouseX, mouseY)) {
            list.add((this.theSettings.isWhitelist
                ? Component.translatable("info.actuallyadditions.gui.whitelist")
                : Component.translatable("info.actuallyadditions.gui.blacklist")).withStyle(ChatFormatting.BOLD));
            list.add(Component.translatable("info.actuallyadditions.gui.whitelistInfo"));
        } else if (this.modButton.isMouseOver(mouseX, mouseY)) {
            list.add((this.theSettings.respectMod
                ? Component.translatable("info.actuallyadditions.gui.respectMod")
                : Component.translatable("info.actuallyadditions.gui.ignoreMod")).withStyle(ChatFormatting.BOLD));
            list.add(Component.translatable("info.actuallyadditions.gui.respectModInfo"));
            list.add(Component.translatable("info.actuallyadditions.gui.respectModInfo2"));
        } else if (this.damageButton.isMouseOver(mouseX, mouseY)) {
            list.add((this.theSettings.matchDamage
                ? Component.translatable("info.actuallyadditions.gui.respectDamage")
                : Component.translatable("info.actuallyadditions.gui.ignoreDamage")).withStyle(ChatFormatting.BOLD));
        } else if (this.nbtButton.isMouseOver(mouseX, mouseY)) {
            list.add((this.theSettings.matchNBT
                ? Component.translatable("info.actuallyadditions.gui.respectNBT")
                : Component.translatable("info.actuallyadditions.gui.ignoreNBT")).withStyle(ChatFormatting.BOLD));
        }
        if (!list.isEmpty()) {
            guiGraphics.renderTooltip(mc.font, wrap(mc.font, list), mouseX, mouseY);
        }
    }

    /**
     * Splits each line of a tooltip at {@link #TOOLTIP_WIDTH} pixels.
     */
    public static List<FormattedCharSequence> wrap(Font font, List<Component> lines) {
        List<FormattedCharSequence> wrapped = new ArrayList<>();
        for (Component line : lines) {
            wrapped.addAll(font.split(line, TOOLTIP_WIDTH));
        }
        return wrapped;
    }
}
