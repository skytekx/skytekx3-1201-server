/*
 * This file ("ItemWaterBowl.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nonnull;

public class ItemWaterBowl extends ItemBase {

    // SkyTekx: registered on the Forge bus as on upstream 9f852348. The base left the registration
    // commented out, so an empty bowl never picked up water
    public ItemWaterBowl() {
        super(ActuallyItems.defaultProps().stacksTo(1));
        MinecraftForge.EVENT_BUS.register(this);
    }

    // SkyTekx: the pickup of upstream 9f852348 and 3191a63d (#1499, PR #1500). The trace stops at
    // source blocks only, and the water leaves through BucketPickup, so a waterlogged block keeps its
    // block and loses its water instead of turning into air. The base traced with a stub that always
    // answered the bottom face of 0, 0, 0
    @SubscribeEvent
    public void onPlayerInteractEvent(PlayerInteractEvent.RightClickItem event) {
        if (CommonConfig.Other.WATER_BOWL.get() && event.getItemStack().is(Items.BOWL)) {
            Player player = event.getEntity();
            Level level = event.getLevel();
            BlockHitResult trace = getPlayerPOVHitResult(
                    level, player, ClipContext.Fluid.SOURCE_ONLY //Using pick will also return flowing water
            );
            if (trace.getType() != HitResult.Type.BLOCK) {
                return;
            }

            ItemStack bowl = event.getItemStack();
            BlockPos pos = trace.getBlockPos();

            if (level.mayInteract(player, pos) && player.mayUseItemAt(pos.relative(trace.getDirection()), trace.getDirection(), bowl)) {
                BlockState blockState = level.getBlockState(pos);
                FluidState fluidState = blockState.getFluidState();

                if (fluidState.isSourceOfType(Fluids.WATER)) {
                    // SkyTekx: the source goes through the Forge break event first, which Arclight hands
                    // to Bukkit as a BlockBreakEvent, so claim plugins can keep the water in place. On a
                    // refusal the server sends the inventory again, since the client already showed the
                    // filled bowl
                    if (!mayTakeBlock(player, level, pos)) {
                        player.containerMenu.sendAllDataToRemote();
                        return;
                    }

                    if (blockState.getBlock() instanceof BucketPickup pickup) {
                        ItemStack fillResult = pickup.pickupBlock(level, pos, blockState);
                        if (!fillResult.isEmpty()) {
                            player.awardStat(Stats.ITEM_USED.get(bowl.getItem()));
                            level.gameEvent(player, GameEvent.FLUID_PICKUP, pos);
                        }

                        pickup.getPickupSound(blockState).ifPresent(soundEvent -> player.playSound(soundEvent, 1.0F, 1.0F));
                    }

                    ItemStack waterBowl = new ItemStack(ActuallyItems.WATER_BOWL.get());
                    ItemStack inHand = ItemUtils.createFilledResult(bowl, player, waterBowl);
                    player.setItemInHand(event.getHand(), inHand);
                    // SkyTekx: the bowl is used up, so the click ends here with a swing
                    event.setCanceled(true);
                    event.setCancellationResult(InteractionResult.sidedSuccess(level.isClientSide));
                }
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(@Nonnull Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        // SkyTekx: the reach of upstream ecb8d46f, 2fbf2984 and 3191a63d instead of the base's stub trace
        HitResult trace = player.pick(Util.getReachDistance(player), 1.0F, false);
        InteractionResultHolder<ItemStack> result = ForgeEventFactory.onBucketUse(player, world, stack, trace);
        if (result != null) {
            return result;
        }

        if (trace == null) {
            return InteractionResultHolder.pass(stack);
        } else if (trace.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(stack);
        } else {
            BlockHitResult blockTrace = (BlockHitResult) trace;
            BlockPos pos = blockTrace.getBlockPos();

            if (!world.mayInteract(player, pos)) {
                return InteractionResultHolder.fail(stack);
            } else {
                BlockPos pos1 = world.getBlockState(pos).canBeReplaced() && blockTrace.getDirection() == Direction.UP
                    ? pos
                    : pos.relative(blockTrace.getDirection());

                if (!player.mayUseItemAt(pos1, blockTrace.getDirection(), stack)) {
                    return InteractionResultHolder.fail(stack);
                } else if (this.tryPlaceContainedLiquid(player, world, pos1, false)) {
                    return !player.isCreative()
                        ? InteractionResultHolder.success(new ItemStack(Items.BOWL))
                        : InteractionResultHolder.success(stack);
                } else {
                    // SkyTekx: the client may already show the empty bowl when a plugin refused the
                    // water on the server, so the server sends the inventory again
                    if (!world.isClientSide) {
                        player.containerMenu.sendAllDataToRemote();
                    }
                    return InteractionResultHolder.fail(stack);
                }
            }
        }
    }

    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isClientSide) {
            if (CommonConfig.Other.WATER_BOWL_LOSS.get()) {
                // SkyTekx: the spilled water obeys spawn protection and adventure mode like a placed
                // bowl, see the checks in use
                if (world.getGameTime() % 10 == 0 && world.random.nextFloat() >= 0.5F) {
                    int lastX = 0;
                    int lastY = 0;

                    if (stack.hasTag()) {
                        CompoundTag compound = stack.getOrCreateTag();
                        lastX = compound.getInt("lastX");
                        lastY = compound.getInt("lastY");
                    }

                    boolean change = false;
                    if (lastX != 0 && lastX != (int) entity.getX() || lastY != 0 && lastY != (int) entity.getY()) {
                        if (!entity.isShiftKeyDown()) {
                            if (entity instanceof Player player) {
                                BlockPos pos = player.blockPosition();
                                if (world.mayInteract(player, pos) && player.mayUseItemAt(pos, Direction.UP, stack)
                                    && this.tryPlaceContainedLiquid(player, world, pos, true)) {
                                    this.checkReplace(player, stack, new ItemStack(Items.BOWL), itemSlot);
                                }
                            }
                        }
                        change = true;
                    }

                    if (change || lastX == 0 || lastY == 0) {
                        CompoundTag compound = stack.getOrCreateTag();
                        compound.putInt("lastX", (int) entity.getX());
                        compound.putInt("lastY", (int) entity.getY());
                    }
                }
            }
        }
    }

    // SkyTekx: the tick passes the slot index within its compartment, so a bowl ticked from an armor
    // slot above 0 would ask the one slot offhand list for that index and throw. The index is checked
    private void checkReplace(Player player, ItemStack old, ItemStack stack, int slot) {
        if (player.getInventory().getItem(slot) == old) {
            player.getInventory().setItem(slot, stack);
        } else if (slot < player.getInventory().offhand.size() && player.getInventory().offhand.get(slot) == old) {
            player.getInventory().offhand.set(slot, stack);
        }
    }

    // SkyTekx: posts the Forge break event for a block the bowl takes or washes away. Answers true on
    // the client, which predicts the change, and for a player that is no server player
    private static boolean mayTakeBlock(Player player, Level world, BlockPos pos) {
        if (!world.isClientSide && player instanceof ServerPlayer serverPlayer) {
            return ForgeHooks.onBlockBreakEvent(world, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos) != -1;
        }

        return true;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return !ItemStack.isSameItem(oldStack, newStack);
    }

    public boolean tryPlaceContainedLiquid(Player player, Level world, BlockPos pos, boolean finite) {
        BlockState state = world.getBlockState(pos);
        boolean nonSolid = !state.isSolid();
        boolean replaceable = state.canBeReplaced();

        if (!world.isEmptyBlock(pos) && !nonSolid && !replaceable) {
            return false;
        } else {
            if (world.dimensionType().ultraWarm()) {
                world.playSound(player, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.5F, 2.6F + (world.random.nextFloat() - world.random.nextFloat()) * 0.8F);

                for (int k = 0; k < 8; k++) {
                    world.addParticle(ParticleTypes.LARGE_SMOKE, pos.getX() + Math.random(), pos.getY() + Math.random(), pos.getZ() + Math.random(), 0.0D, 0.0D, 0.0D);
                }
            } else {
                // SkyTekx: the block that the water washes away goes through the Forge break event and
                // the water through the Forge place event, which Arclight hands to Bukkit, so claim
                // plugins see both. A refused break keeps the block and the water in the bowl. A
                // refused place takes the water back, after the block it washed away has dropped as
                // the break event allowed. Upstream main posts neither event
                if (!world.isClientSide && (nonSolid || replaceable) && world.getFluidState(pos).isEmpty()) {
                    if (!state.isAir() && !mayTakeBlock(player, world, pos)) {
                        return false;
                    }
                    world.destroyBlock(pos, true);
                }

                BlockSnapshot snapshot = world.isClientSide ? null : BlockSnapshot.create(world.dimension(), world, pos);
                BlockState placeState = Blocks.WATER.defaultBlockState();
                world.setBlock(pos, placeState, 3);

                if (snapshot != null && ForgeEventFactory.onBlockPlace(player, snapshot, Direction.UP)) {
                    snapshot.restore(true, false);
                    return false;
                }

                world.playSound(player, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
            }

            return true;
        }
    }
}
