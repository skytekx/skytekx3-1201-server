package ianm1647.expandeddelight.common.item;

import net.minecraft.world.item.Item;
import vectorwing.farmersdelight.common.item.ConsumableItem;

public class JellyItem extends ConsumableItem {
    public JellyItem(Item.Properties properties) {
        super(properties);
    }

    public JellyItem(Item.Properties properties, boolean hasFoodEffectTooltip) {
        super(properties, hasFoodEffectTooltip);
    }

    public JellyItem(Item.Properties properties, boolean hasPotionEffectTooltip, boolean hasCustomTooltip) {
        super(properties, hasPotionEffectTooltip, hasCustomTooltip);
    }
}
