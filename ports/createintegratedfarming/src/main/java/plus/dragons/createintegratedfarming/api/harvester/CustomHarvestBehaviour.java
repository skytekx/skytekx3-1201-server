/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createintegratedfarming.api.harvester;

import com.simibubi.create.AllTags;
import com.simibubi.create.api.behaviour.movement.MovementBehaviour;
import com.simibubi.create.api.registry.SimpleRegistry;
import com.simibubi.create.content.contraptions.actors.harvester.HarvesterMovementBehaviour;
import com.simibubi.create.content.contraptions.behaviour.MovementContext;
import com.simibubi.create.foundation.utility.BlockHelper;
import com.simibubi.create.infrastructure.config.AllConfigs;
import java.util.List;
import java.util.function.Consumer;
import javax.annotation.Nullable;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.level.BlockEvent;
import plus.dragons.createdragonsplus.util.CodeReference;
import plus.dragons.createintegratedfarming.integration.ModIntegration;

/**
 * CustomHarvestBehaviour provides full control for a block's interaction with {@link HarvesterMovementBehaviour}.
 * <p>
 * For specifically disable the harvest behaviour, add the block to {@link AllTags.AllBlockTags#NON_HARVESTABLE} instead.
 */
@FunctionalInterface
public interface CustomHarvestBehaviour {
    SimpleRegistry<Block, CustomHarvestBehaviour> REGISTRY = SimpleRegistry.create();

    /**
     * Harvest the block and collect the drops.
     * 
     * @param behaviour the actual {@link HarvesterMovementBehaviour} of the harvester,
     *                  usually used for calling {@link MovementBehaviour#dropItem(MovementContext, ItemStack)}.
     * @param context   the {@link MovementContext} of the harvester.
     * @param pos       the {@link BlockPos} of the block to be harvested.
     * @param state     the {@link BlockState} of the block to be harvested.
     */
    void harvest(HarvesterMovementBehaviour behaviour, MovementContext context, BlockPos pos, BlockState state);

    /**
     * Harvest a block through an area harvester. Implementations must opt in explicitly because a generic fallback can
     * corrupt multi-block or otherwise structured crops.
     *
     * @return {@code true} when this invocation harvested a block
     */
    default boolean harvestInArea(AreaHarvestContext context, BlockPos pos, BlockState state) {
        return false;
    }

    /**
     * Shortcut for {@code harvesterReplants} config.
     */
    static boolean replant() {
        return AllConfigs.server().kinetics.harvesterReplants.get();
    }

    /**
     * Shortcut for {@code harvestPartiallyGrown} config.
     */
    static boolean partial() {
        return AllConfigs.server().kinetics.harvestPartiallyGrown.get();
    }

    /**
     * Helper method for retrieving the correct harvest tool item from the context.
     * <p>
     * Use this method for keeping consistency with {@link ModIntegration#CREATE_ENCHANTABLE_MACHINERY}.
     */
    static ItemStack getHarvestTool(MovementContext context) {
        return getHarvestTool(context, ItemStack.EMPTY);
    }

    /**
     * Helper method for retrieving the correct harvest tool item from the context.
     * <p>
     * Use this method for keeping consistency with {@link ModIntegration#CREATE_ENCHANTABLE_MACHINERY}.
     */
    static ItemStack getHarvestTool(MovementContext context, ItemStack original) {
        if (ModIntegration.CREATE_ENCHANTABLE_MACHINERY.enabled()) {
            if (original.isEmpty())
                original = new ItemStack(Items.NETHERITE_PICKAXE);
            if (context.blockEntityData == null)
                return original;
            // SkyTekx: 1.20.1 stores enchantments as the ItemStack NBT list "Enchantments", which the
            // block entity data of Create Enchantable Machinery carries in the same form
            var tag = context.blockEntityData.getList("Enchantments", Tag.TAG_COMPOUND);
            if (tag.isEmpty())
                return original;
            original.getOrCreateTag().put("Enchantments", tag.copy());
            return original;
        }
        return original;
    }

    /**
     * Helper method for harvest a block and transform it into a specific state.
     * <p>
     * Derived from {@link BlockHelper#destroyBlockAs(Level, BlockPos, Player, ItemStack, float, Consumer)},
     * supports specifying the result state, useful for harvesting high crops.
     * <p>
     * SkyTekx: the Forge 1.20.1 form follows Create 6.0.8's destroyBlockAs, which drops the experience of
     * the {@link BlockEvent.BreakEvent} instead of NeoForge's BlockDropsEvent.
     */
    @CodeReference(value = BlockHelper.class, targets = "destroyBlockAs", source = "create", license = "mit")
    static void harvestBlock(Level level, BlockPos pos, BlockState newState, @Nullable Player player, ItemStack usedTool, float effectChance, Consumer<ItemStack> droppedItemCallback) {
        BlockState state = level.getBlockState(pos);

        if (level.random.nextFloat() < effectChance)
            level.levelEvent(2001, pos, Block.getId(state));
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;

        if (player != null) {
            BlockEvent.BreakEvent event = new BlockEvent.BreakEvent(level, pos, state, player);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled())
                return;
            if (event.getExpToDrop() > 0 && level instanceof ServerLevel serverLevel)
                state.getBlock().popExperience(serverLevel, pos, event.getExpToDrop());

            usedTool.mineBlock(level, state, pos, player);
            player.awardStat(Stats.BLOCK_MINED.get(state.getBlock()));
        }

        if (level instanceof ServerLevel serverLevel &&
                level.getGameRules().getBoolean(GameRules.RULE_DOBLOCKDROPS) &&
                !level.restoringBlockSnapshots &&
                (player == null || !player.isCreative())) {
            List<ItemStack> drops = Block.getDrops(state, serverLevel, pos, blockEntity, player, usedTool);
            for (ItemStack itemStack : drops)
                droppedItemCallback.accept(itemStack);

            state.spawnAfterBreak((ServerLevel) level, pos, ItemStack.EMPTY, true);
        }

        level.setBlockAndUpdate(pos, newState);
    }
}
