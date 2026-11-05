package com.enderio.base.client.gui.widget;

import com.enderio.EnderIO;
import com.enderio.base.common.lang.EIOLang;
import com.enderio.core.EnderCore;
import com.enderio.core.common.capability.DamageFilterMode;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipPositioner;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A 6.x style cycle button for the damage mode of an item filter. A left click selects the next mode, a right click
 * the previous one. It stands in for the DamageFilterModePickerWidget of Ender IO 8.x and draws the 8.x mode icons.
 */
public class DamageFilterModeButton extends AbstractWidget {

    private static final ResourceLocation BUTTON_TEXTURE = EnderCore.loc("textures/gui/checkbox.png");
    private static final Map<DamageFilterMode, ResourceLocation> ICONS = new EnumMap<>(DamageFilterMode.class);

    static {
        for (DamageFilterMode mode : DamageFilterMode.values()) {
            ICONS.put(mode, EnderIO.loc("textures/gui/icons/damage_filter_mode/" + mode.getSerializedName() + ".png"));
        }
    }

    private final Supplier<DamageFilterMode> getter;
    private final Consumer<DamageFilterMode> setter;

    private int lastMouseButton = InputConstants.MOUSE_BUTTON_LEFT;

    @Nullable
    private DamageFilterMode tooltipMode;

    public DamageFilterModeButton(int x, int y, Supplier<DamageFilterMode> getter, Consumer<DamageFilterMode> setter) {
        super(x, y, 16, 16, EIOLang.DAMAGE_FILTER);
        this.getter = getter;
        this.setter = setter;
    }

    @Override
    protected boolean isValidClickButton(int button) {
        return button == InputConstants.MOUSE_BUTTON_LEFT || button == InputConstants.MOUSE_BUTTON_RIGHT;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        lastMouseButton = button;
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void onClick(double mouseX, double mouseY) {
        setter.accept(getter.get().cycle(lastMouseButton != InputConstants.MOUSE_BUTTON_RIGHT));
    }

    @Override
    public void renderWidget(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        guiGraphics.blit(BUTTON_TEXTURE, getX(), getY(), width, height, isHovered ? 28 : 0, 0, 14, 14, 256, 256);

        DamageFilterMode mode = getter.get();
        RenderSystem.enableBlend();
        guiGraphics.blit(ICONS.get(mode), getX(), getY(), width, height, 0, 0, 32, 32, 32, 32);
        RenderSystem.disableBlend();

        if (tooltipMode != mode) {
            tooltipMode = mode;
            setTooltip(Tooltip.create(EIOLang.DAMAGE_FILTER.copy()
                .append("\n")
                .append(Component.translatable(mode.getTranslationKey()).withStyle(ChatFormatting.GRAY))));
        }
    }

    @Override
    protected ClientTooltipPositioner createTooltipPositioner() {
        return DefaultTooltipPositioner.INSTANCE;
    }

    @Override
    public void updateWidgetNarration(NarrationElementOutput narrationElementOutput) {
        defaultButtonNarrationText(narrationElementOutput);
    }
}
