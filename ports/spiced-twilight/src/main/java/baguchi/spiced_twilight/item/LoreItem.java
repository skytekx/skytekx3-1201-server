package baguchi.spiced_twilight.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * 1.20.1 stand-in for the 1.21 {@code minecraft:lore} data component that upstream sets on its items:
 * a fixed tooltip line in the vanilla lore style.
 */
public class LoreItem extends Item {
    static final Style LORE_STYLE = Style.EMPTY.withColor(ChatFormatting.DARK_PURPLE).withItalic(true);

    private final String loreKey;

    public LoreItem(Properties properties, String loreKey) {
        super(properties);
        this.loreKey = loreKey;
    }

    static void appendLore(List<Component> tooltip, String loreKey) {
        tooltip.add(Component.translatable(loreKey).withStyle(LORE_STYLE));
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        appendLore(tooltip, this.loreKey);
    }
}
