package ianm1647.expandeddelight.common.item;

import ianm1647.expandeddelight.common.registry.EDItems;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * Cranberries: eaten, or planted on a water source like a lily pad.
 * <p>
 * Upstream places the plant with setBlock above any water source it hits, without asking whether the block
 * there may be replaced. This port hands the placement to the cranberry_plant item, a vanilla
 * PlaceOnWaterBlockItem, which checks the spot, uses the cranberries in hand and lets Bukkit's
 * BlockPlaceEvent and protection plugins see the placement on the Arclight server.
 */
public class LilypadCropItem extends Item {
    private final Block block;

    public LilypadCropItem(Block block1, Properties properties) {
        super(properties);
        block = block1;
    }

    public Block getBlock() {
        return this.block;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult hitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (hitresult.getType() == HitResult.Type.BLOCK && level.getFluidState(hitresult.getBlockPos()).is(FluidTags.WATER) && !player.isUnderWater()) {
            InteractionResultHolder<ItemStack> placed = EDItems.CRANBERRY_PLANT.get().use(level, player, hand);
            if (placed.getResult().consumesAction()) {
                return placed;
            }
        }

        FoodProperties foodproperties = itemstack.getFoodProperties(player);
        if (foodproperties != null) {
            if (player.canEat(foodproperties.canAlwaysEat())) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        }
        return InteractionResultHolder.pass(itemstack);
    }
}
