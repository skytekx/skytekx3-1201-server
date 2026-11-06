package de.ellpeck.actuallyadditions.mod.blocks.base;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.items.ItemHandlerHelper;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Supplier;

public class AACrops extends CropBlock {
    Supplier<Item> itemSupplier;

    // SkyTekx: upstream 38d6404b. The patches of the world generation place a ripe crop with this flag,
    // so a block update next to it no longer pops it off its grass. A crop of an older world has no
    // value saved and loads with the default false, the old behaviour
    public static final BooleanProperty PERSISTENT = BooleanProperty.create("persistent");

    public AACrops(Properties p_i48421_1_, Supplier<Item> seedSupplier) {
        super(p_i48421_1_);
        this.itemSupplier = seedSupplier;

        // SkyTekx: any() is the state with persistent true, the first value of a BooleanProperty
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0).setValue(PERSISTENT, false));
    }

    @Nonnull
    @Override
    protected ItemLike getBaseSeedId() {
        return this.itemSupplier.get();
    }

    @Override
    public boolean canSurvive(@Nonnull BlockState state, @Nonnull LevelReader level, @Nonnull BlockPos pos) {
        if (!(level instanceof WorldGenRegion) && state.getValue(PERSISTENT) && !level.getBlockState(pos.below()).isAir()) {
            return true;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, PERSISTENT);
    }

    // SkyTekx: upstream 506d481d (changelog 1.3.11), the right click harvest of 1.12. A ripe crop gives its
    // drops minus one seed to the player and starts again at age 0. Upstream's useItemOn passes the click
    // on to the held item after the harvest, here it is consumed, as the sweet berry bush does, so bone
    // meal in the hand is not spent on the replanted crop
    @Nonnull
    @Override
    public InteractionResult use(@Nonnull BlockState state, @Nonnull Level world, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull InteractionHand hand, @Nonnull BlockHitResult hit) {
        if (this.getAge(state) < 7) {
            return InteractionResult.PASS;
        }

        if (!world.isClientSide) {
            List<ItemStack> drops = Block.getDrops(state, (ServerLevel) world, pos, null, player, player.getItemInHand(hand));
            boolean deductedSeedSize = false;
            for (ItemStack drop : drops) {
                if (!drop.isEmpty()) {
                    if (drop.getItem() == this.itemSupplier.get() && !deductedSeedSize) {
                        drop.shrink(1);
                        deductedSeedSize = true;
                    }
                    if (!drop.isEmpty()) {
                        ItemHandlerHelper.giveItemToPlayer(player, drop);
                    }
                }
            }

            world.setBlockAndUpdate(pos, this.defaultBlockState().setValue(AGE, 0));
        }

        return InteractionResult.sidedSuccess(world.isClientSide);
    }
}
