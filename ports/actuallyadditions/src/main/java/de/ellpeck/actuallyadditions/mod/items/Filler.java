/*
 * This file ("Filler.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import de.ellpeck.actuallyadditions.mod.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

// SkyTekx: named Filler as on upstream 87b02617. The registry id stays handheld_filler
public class Filler extends ItemEnergy {

    // SkyTekx: the orientation properties that a saved block keeps. Every other property goes back to
    // the block's default, see normalize
    private static final List<Property<?>> KEPT_PROPERTIES = List.of(
        BlockStateProperties.FACING,
        BlockStateProperties.HORIZONTAL_FACING,
        BlockStateProperties.FACING_HOPPER,
        BlockStateProperties.AXIS,
        BlockStateProperties.HORIZONTAL_AXIS,
        BlockStateProperties.HALF,
        BlockStateProperties.ATTACH_FACE,
        BlockStateProperties.ROTATION_16
    );

    public Filler() {
        super(500000, 1000);
    }

    // SkyTekx: finds the slot of the block's item without taking it yet, so a place event that a
    // plugin cancels costs nothing. Answers -1 when the player carries none
    private static int findFittingItem(BlockState state, Player player) {
        Block block = state.getBlock();
        ItemStack stack = new ItemStack(block, 1);

        if (StackUtil.isValid(stack)) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack slot = player.getInventory().getItem(i);
                if (StackUtil.isValid(slot) && ItemStack.isSameItem(slot, stack)) {
                    return i;
                }
            }
        }

        return -1;
    }

    private static void removeItem(Player player, int index) {
        ItemStack slot = player.getInventory().getItem(index);
        slot.shrink(1);
        if (!StackUtil.isValid(slot)) {
            player.getInventory().setItem(index, ItemStack.EMPTY);
        }
    }

    // SkyTekx: the filler places the saved block for one item each, so the saved state keeps only the
    // block's orientation. It used to keep every property, and a double slab, eight snow layers, four
    // sea pickles, a full composter or mature wheat came out of the filler for one item and dropped
    // their whole loot when broken. A double slab becomes a bottom slab and the block is never
    // waterlogged. Upstream main keeps the full state, which never mattered on the base, where the
    // filler could not fill
    private static BlockState normalize(BlockState state) {
        BlockState result = state.getBlock().defaultBlockState();
        for (Property<?> property : KEPT_PROPERTIES) {
            result = copyProperty(state, result, property);
        }

        if (state.hasProperty(BlockStateProperties.SLAB_TYPE) && result.hasProperty(BlockStateProperties.SLAB_TYPE)) {
            SlabType type = state.getValue(BlockStateProperties.SLAB_TYPE);
            result = result.setValue(BlockStateProperties.SLAB_TYPE, type == SlabType.DOUBLE ? SlabType.BOTTOM : type);
        }
        if (result.hasProperty(BlockStateProperties.WATERLOGGED)) {
            result = result.setValue(BlockStateProperties.WATERLOGGED, false);
        }

        return result;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState from, BlockState to, Property<T> property) {
        return from.hasProperty(property) && to.hasProperty(property)
            ? to.setValue(property, from.getValue(property))
            : to;
    }

    private static void saveData(BlockState state, ItemStack wand) {
        wand.getOrCreateTag().put("state", NbtUtils.writeBlockState(normalize(state)));
    }

    // SkyTekx: reads without adding an empty tag, since the tooltip and the tick call it on every stack.
    // The state read back is normalized as well, so a filler saved before the fix keeps no multiplied
    // state, and air, which a block of a removed mod reads as, counts as no block
    private static Optional<BlockState> loadData(ItemStack stack) {
        CompoundTag compound = stack.getTag();
        if (compound != null && compound.contains("state")) {
            BlockState state = NbtUtils.readBlockState(BuiltInRegistries.BLOCK.asLookup(), compound.getCompound("state"));
            if (!state.isAir()) {
                return Optional.of(normalize(state));
            }
        }

        return Optional.empty();
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getPlayer() == null) {
            return InteractionResult.PASS;
        }

        // SkyTekx: the stack of the context (upstream e08ef13a)
        ItemStack stack = context.getItemInHand();
        if (!context.getLevel().isClientSide && context.getPlayer().getUseItemRemainingTicks() <= 0) {
            if (context.getPlayer().isCrouching()) {
                BlockState state = context.getLevel().getBlockState(context.getClickedPos());
                saveData(state, stack);
                return InteractionResult.SUCCESS;
            } else if (loadData(stack).isPresent()) {
                CompoundTag compound = stack.getOrCreateTag();

                if (compound.getInt("CurrX") == 0 && compound.getInt("CurrY") == 0 && compound.getInt("CurrZ") == 0) {
                    compound.putInt("FirstX", context.getClickedPos().getX());
                    compound.putInt("FirstY", context.getClickedPos().getY());
                    compound.putInt("FirstZ", context.getClickedPos().getZ());

                    context.getPlayer().startUsingItem(context.getHand());
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return super.useOn(context);
    }

    @Override
    public void releaseUsing(ItemStack stack, Level world, LivingEntity entity, int timeLeft) {
        if (!world.isClientSide) {
            boolean clear = true;
            if (entity instanceof Player player) {
                // SkyTekx: the second corner is the block the player looks at within reach (upstream
                // d1438dd6 and 2fbf2984). The base's stub always answered 0, 0, 0. A miss is a
                // BlockHitResult too, so the type is checked as well
                HitResult result = player.pick(Util.getReachDistance(player), 1f, false);
                if (result instanceof BlockHitResult && result.getType() == HitResult.Type.BLOCK) {
                    CompoundTag compound = stack.getOrCreateTag();

                    BlockPos pos = ((BlockHitResult) result).getBlockPos();
                    compound.putInt("SecondX", pos.getX());
                    compound.putInt("SecondY", pos.getY());
                    compound.putInt("SecondZ", pos.getZ());

                    clear = false;
                }
            }

            if (clear) {
                ItemPhantomConnector.clearStorage(stack, "FirstX", "FirstY", "FirstZ");
            }
        }

        super.releaseUsing(stack, world, entity, timeLeft);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        super.inventoryTick(stack, world, entity, itemSlot, isSelected);


        if (!world.isClientSide) {
            boolean shouldClear = false;

            if (isSelected) {
                if (entity instanceof Player player && stack.hasTag()) {
	                boolean creative = player.isCreative();

                    CompoundTag compound = stack.getOrCreateTag();

                    BlockPos firstPos = new BlockPos(compound.getInt("FirstX"), compound.getInt("FirstY"), compound.getInt("FirstZ"));
                    BlockPos secondPos = new BlockPos(compound.getInt("SecondX"), compound.getInt("SecondY"), compound.getInt("SecondZ"));

                    if (!BlockPos.ZERO.equals(firstPos) && !BlockPos.ZERO.equals(secondPos)) {
                        int energyUse = 1500;

                        Optional<BlockState> data = loadData(stack);
                        if (data.isPresent() && (creative || this.getEnergyStored(stack) >= energyUse)) {
                            BlockState replaceState = data.get(); // not the best way to do this.
                            int lowestX = Math.min(firstPos.getX(), secondPos.getX());
                            int lowestY = Math.min(firstPos.getY(), secondPos.getY());
                            int lowestZ = Math.min(firstPos.getZ(), secondPos.getZ());

                            int currX = compound.getInt("CurrX");
                            int currY = compound.getInt("CurrY");
                            int currZ = compound.getInt("CurrZ");

                            BlockPos pos = new BlockPos(lowestX + currX, lowestY + currY, lowestZ + currZ);
                            BlockState state = world.getBlockState(pos);

                            // SkyTekx: a position that spawn protection, the world border or adventure
                            // mode keeps from the player is skipped, and every block goes through the
                            // Forge place event, which Arclight hands to Bukkit as a BlockPlaceEvent.
                            // A cancelled place is undone, costs no item and no energy and ends the job.
                            // Upstream main sets the blocks without either check
                            if (state.canBeReplaced() && replaceState.canSurvive(world, pos)
                                && world.mayInteract(player, pos) && player.mayUseItemAt(pos, Direction.UP, stack)) {
                                int fittingSlot = creative ? -1 : findFittingItem(replaceState, player);
                                if (creative || fittingSlot >= 0) {
                                    BlockSnapshot snapshot = BlockSnapshot.create(world.dimension(), world, pos);
                                    world.setBlock(pos, replaceState, 2);

                                    if (ForgeEventFactory.onBlockPlace(player, snapshot, Direction.UP)) {
                                        snapshot.restore(true, false);
                                        shouldClear = true;
                                    } else {
                                        if (!creative) {
                                            removeItem(player, fittingSlot);
                                        }

                                        SoundType sound = replaceState.getBlock().getSoundType(replaceState, world, pos, player);
                                        world.playSound(null, pos, sound.getPlaceSound(), SoundSource.BLOCKS, sound.getVolume() / 2F + .5F, sound.getPitch() * 0.8F);

                                        if (!creative) {
                                            this.extractEnergyInternal(stack, energyUse, false);
                                        }
                                    }
                                } else {
                                    shouldClear = true;
                                }
                            }

                            int distX = Math.abs(secondPos.getX() - firstPos.getX());
                            int distY = Math.abs(secondPos.getY() - firstPos.getY());
                            int distZ = Math.abs(secondPos.getZ() - firstPos.getZ());

                            currX++;
                            if (currX > distX) {
                                currX = 0;
                                currY++;
                                if (currY > distY) {
                                    currY = 0;
                                    currZ++;
                                    if (currZ > distZ) {
                                        shouldClear = true;
                                    }
                                }
                            }

                            if (!shouldClear) {
                                compound.putInt("CurrX", currX);
                                compound.putInt("CurrY", currY);
                                compound.putInt("CurrZ", currZ);
                            }
                        } else {
                            shouldClear = true;
                        }
                    }
                }
            } else {
                shouldClear = true;
            }

            if (shouldClear) {
                ItemPhantomConnector.clearStorage(stack, "FirstX", "FirstY", "FirstZ", "SecondX", "SecondY", "SecondZ", "CurrX", "CurrY", "CurrZ");
            }
        }
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        MutableComponent display = loadData(stack)
            .map(state -> state.getBlock().getName())
            .orElse(Component.translatable("tooltip.actuallyadditions.item_filling_wand.selected_block.none"));

        tooltip.add(Component.translatable("tooltip.actuallyadditions.item_filling_wand.selected_block", display.getString()));
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return Integer.MAX_VALUE;
    }
}
