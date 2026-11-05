package ianm1647.expandeddelight.common.item;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.MilkBucketItem;
import net.minecraft.world.level.Level;

/**
 * The goat milk bucket. Upstream registers a plain MilkBucketItem, which on NeoForge 1.21.1 cures every
 * effect that milk cures. Forge 1.20.1 cures only effects whose curative items include the drunk item,
 * and by default that is the vanilla milk bucket alone, so this item cures as a milk bucket would.
 */
public class GoatMilkBucketItem extends MilkBucketItem {
    public GoatMilkBucketItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide) {
            entity.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
        }

        if (entity instanceof ServerPlayer serverPlayer) {
            CriteriaTriggers.CONSUME_ITEM.trigger(serverPlayer, stack);
            serverPlayer.awardStat(Stats.ITEM_USED.get(this));
        }

        if (entity instanceof Player player && !player.getAbilities().instabuild) {
            stack.shrink(1);
        }

        return stack.isEmpty() ? new ItemStack(Items.BUCKET) : stack;
    }
}
