package ianm1647.expandeddelight.common.block;

import ianm1647.expandeddelight.common.registry.EDBlocks;
import ianm1647.expandeddelight.common.registry.EDItems;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
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
    public ItemInteractionResult useItemOn(ItemStack heldStack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        ItemStack mainHand = player.getMainHandItem();
        if (mainHand.is(Items.MILK_BUCKET)) {
            if(!player.isCreative()) {
                mainHand.shrink(1);
                player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
            }
            level.setBlock(pos, EDBlocks.MILK_CASK.get().defaultBlockState(), 2);
            level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }
        if (mainHand.is(EDItems.GOAT_MILK_BUCKET.get())) {
            if(!player.isCreative()) {
                mainHand.shrink(1);
                player.setItemInHand(hand, Items.BUCKET.getDefaultInstance());
            }
            level.setBlock(pos, EDBlocks.GOAT_MILK_CASK.get().defaultBlockState(), 2);
            level.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            return ItemInteractionResult.SUCCESS;
        }
        return ItemInteractionResult.FAIL;
    }
}
