package ianm1647.expandeddelight.common.block;

import ianm1647.expandeddelight.common.registry.EDBlocks;
import ianm1647.expandeddelight.common.registry.EDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class CaskBlock extends Block {

    public CaskBlock(Properties properties) {
        super(properties);
    }

    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        // upstream checks the main hand whatever hand the event is for, this port checks the hand in use
        ItemStack heldStack = player.getItemInHand(hand);
        if (heldStack.is(Items.MILK_BUCKET)) {
            fill(level, pos, player, hand, heldStack, EDBlocks.MILK_CASK.get());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (heldStack.is(EDItems.GOAT_MILK_BUCKET.get())) {
            fill(level, pos, player, hand, heldStack, EDBlocks.GOAT_MILK_CASK.get());
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    private static void fill(Level level, BlockPos pos, Player player, InteractionHand hand, ItemStack heldStack, Block milkCask) {
        if (!player.isCreative()) {
            heldStack.shrink(1);
            player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
        }
        level.setBlock(pos, milkCask.defaultBlockState(), 2);
        level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
    }
}
