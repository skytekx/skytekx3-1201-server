/*
 * This file ("ItemWorm.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.entity.EntityWorm;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.util.LootTableUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.ToolActions;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.eventbus.api.Event;

import java.util.List;

public class Worm extends ItemBase {

    public Worm() {
        super();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockPos pos = context.getClickedPos();
        // SkyTekx: the stack of the context and a nullable player (upstream acb52994, #1489)
        ItemStack stack = context.getItemInHand();
        Player player = context.getPlayer();
        Level level = context.getLevel();
        BlockState state = level.getBlockState(pos);

        if (level.isClientSide || !EntityWorm.canWormify(level, context.getClickedPos(), state))
            return super.useOn(context);

        List<EntityWorm> worms = level.getEntitiesOfClass(EntityWorm.class, new AABB(pos.getX() - 1, pos.getY(), pos.getZ() - 1, pos.getX() + 2, pos.getY() + 1, pos.getZ() + 2));
        if (!worms.isEmpty())
            return super.useOn(context);

        EntityWorm worm = new EntityWorm(ActuallyAdditions.ENTITY_WORM.get(), level);
        worm.setPos(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        worm.setCustomName(stack.getHoverName());
        level.addFreshEntity(worm);
        if (player == null || !player.getAbilities().instabuild)
            stack.shrink(1);

        return InteractionResult.SUCCESS;
    }

    public static void onHoe(BlockEvent.BlockToolModificationEvent event) {
        if (event.getToolAction() == ToolActions.HOE_TILL) {
            // SkyTekx: no worms when a machine tills, as in b26c47dd, since the Farmer now tills
            // through the hoe item and so posts this event
            if (event.getPlayer() instanceof FakePlayer) {
                return;
            }
            // SkyTekx: the level of the event, as on main, since a tilling without a player has none
            if (!(event.getLevel() instanceof Level level)) {
                return;
            }

            // SkyTekx: a simulated tilling, which a mod may run to ask whether a block can be tilled,
            // changes no block and so drops no worms
            if (level.isClientSide || event.isSimulated() || !CommonConfig.Other.WORMS.get() || event.getResult() == Event.Result.DENY)
                return;

            BlockPos pos = event.getContext().getClickedPos();
            if (level.isEmptyBlock(pos.above())) {
                BlockState state = level.getBlockState(pos);
                // SkyTekx: the blocks of the block tag actuallyadditions:worm_drop (upstream e37019a5)
                // drop the loot table actuallyadditions:worm_drop (159fd67c, 02ca59fe). Its 5 percent
                // chance of one or two worms is the base's old roll. The luck of a missing player is 0
                // (acb52994, #1489)
                if (state.is(ActuallyTags.Blocks.WORM_CAN_POP) && level instanceof ServerLevel serverLevel) {
                    Player player = event.getPlayer();
                    float luck = player != null ? player.getLuck() : 0;

                    List<ItemStack> lootWormItems = LootTableUtil.getLootFromTable(level, LootTableUtil.WORM_DROP)
                            .getRandomItems(new LootParams.Builder(serverLevel)
                                    .withLuck(luck)
                                    .create(LootContextParamSets.EMPTY));

                    for (ItemStack item : lootWormItems) {
                        level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, item));
                    }
                }
            }
        }
    }
}
