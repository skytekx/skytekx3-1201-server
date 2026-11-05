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

package plus.dragons.createintegratedfarming.common.ranching.roost;

import com.simibubi.create.content.kinetics.belt.behaviour.DirectBeltInputBehaviour;
import com.simibubi.create.foundation.block.IBE;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import com.simibubi.create.foundation.item.ItemHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import java.util.function.Supplier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public abstract class BirdRoostBlock<T extends Animal, B extends AnimalRoostBlockEntity> extends RoostBlock
        implements IBE<B>, RoostCapturable {
    // SkyTekx: a supplier, since the BlockEntry of Registrate for 1.20.1 is no Holder
    protected final Supplier<? extends Block> empty;

    protected BirdRoostBlock(Properties properties, Supplier<? extends Block> empty) {
        super(properties);
        this.empty = empty;
    }

    protected abstract Class<T> getBirdClass();

    protected abstract EntityType<T> getBirdType();

    protected @Nullable T createBird(Level level) {
        return getBirdType().create(level);
    }

    protected abstract SoundEvent getEggSound();

    protected abstract SoundEvent getHurtSound();

    protected abstract boolean isBirdJockey(T bird);

    protected boolean canCapture(T bird) {
        return !bird.isBaby() && !isBirdJockey(bird) && !bird.isPassenger() && !bird.isVehicle();
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        return onBlockEntityUse(level, pos, roost -> {
            for (int slot = 0; slot < roost.outputHandler.getSlots(); slot++) {
                ItemStack stack = roost.outputHandler.extractItem(slot, 64, false);
                if (stack.isEmpty())
                    continue;
                player.getInventory().placeItemBackInInventory(stack);
                level.playSound(
                        player, pos, getEggSound(), SoundSource.BLOCKS,
                        1.0F, (level.random.nextFloat() - level.random.nextFloat()) * 0.2F + 1.0F);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
            InteractionHand hand, BlockHitResult hitResult) {
        if (stack.is(Items.LEAD)) {
            if (!level.isClientSide) {
                T bird = createBird(level);
                if (bird == null)
                    return InteractionResult.FAIL;
                bird.setAge(0);
                bird.setPos(pos.getCenter());
                bird.setLeashedTo(player, true);
                level.addFreshEntity(bird);
                level.setBlockAndUpdate(pos, empty.get().withPropertiesOf(state));
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return onBlockEntityUse(level, pos, roost -> {
            if (roost != null && roost.feedItem(stack, false)) {
                if (!player.getAbilities().instabuild)
                    stack.shrink(1);
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
            return InteractionResult.PASS;
        });
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        super.updateEntityAfterFallOn(level, entity);
        if (!(entity instanceof ItemEntity itemEntity) || !entity.isAlive() || entity.level().isClientSide)
            return;
        DirectBeltInputBehaviour input = BlockEntityBehaviour.get(
                level, entity.blockPosition(), DirectBeltInputBehaviour.TYPE);
        if (input == null)
            return;
        ItemStack remainder = input.handleInsertion(itemEntity.getItem(), Direction.UP, false);
        itemEntity.setItem(remainder);
        if (remainder.isEmpty())
            itemEntity.discard();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        IBE.onRemove(state, level, pos, newState);
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos) {
        return ItemHelper.calcRedstoneFromBlockEntity(this, level, pos);
    }

    @Override
    public InteractionResult captureBlock(
            Level level, BlockState state, BlockPos pos, ItemStack stack, Player player, Entity entity) {
        if (!getBirdClass().isInstance(entity))
            return InteractionResult.PASS;
        T bird = getBirdClass().cast(entity);
        if (!canCapture(bird))
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            level.setBlockAndUpdate(pos, withPropertiesOf(state));
            bird.playSound(getHurtSound());
            bird.discard();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    public InteractionResult captureItem(
            Level level, ItemStack stack, InteractionHand hand, Player player, Entity entity) {
        if (!getBirdClass().isInstance(entity))
            return InteractionResult.PASS;
        T bird = getBirdClass().cast(entity);
        if (!canCapture(bird))
            return InteractionResult.PASS;
        if (!level.isClientSide) {
            ItemStack filledRoost = new ItemStack(this);
            if (player.getAbilities().instabuild) {
                player.getInventory().placeItemBackInInventory(filledRoost);
            } else if (stack.getCount() == 1) {
                player.setItemInHand(hand, filledRoost);
            } else {
                player.getInventory().placeItemBackInInventory(filledRoost);
                stack.shrink(1);
            }
            bird.playSound(getHurtSound());
            bird.discard();
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}
