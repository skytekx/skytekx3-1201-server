/*
 * This file ("WorldUtil.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.util;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditionsClient;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.tile.FilterSettings;
import de.ellpeck.actuallyadditions.mod.util.compat.SlotlessableItemHandlerWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.level.BlockEvent.BreakEvent;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.fml.DistExecutor;

import java.util.ArrayList;

public final class WorldUtil {

    public static boolean doItemInteraction(SlotlessableItemHandlerWrapper extractWrapper, SlotlessableItemHandlerWrapper insertWrapper, int maxExtract) {
        return doItemInteraction(extractWrapper, insertWrapper, maxExtract, null);
    }

    public static boolean doItemInteraction(SlotlessableItemHandlerWrapper extractWrapper, SlotlessableItemHandlerWrapper insertWrapper, int maxExtract, FilterSettings filter) {
        return doItemInteraction(extractWrapper, insertWrapper, maxExtract, 0, Integer.MAX_VALUE, 0, Integer.MAX_VALUE, filter);
    }

    public static boolean doItemInteraction(SlotlessableItemHandlerWrapper extractWrapper, SlotlessableItemHandlerWrapper insertWrapper, int maxExtract, int extractSlotStart, int extractSlotEnd, int insertSlotStart, int insertSlotEnd, FilterSettings filter) {
        ItemStack theoreticalExtract = extractItem(extractWrapper, maxExtract, true, extractSlotStart, extractSlotEnd, filter);
        if (StackUtil.isValid(theoreticalExtract)) {
            ItemStack remaining = StackUtil.insertItem(insertWrapper, theoreticalExtract, false, insertSlotStart, insertSlotEnd);
            if (!ItemStack.matches(remaining, theoreticalExtract)) {
                int toExtract = theoreticalExtract.getCount() - remaining.getCount();
                extractItem(extractWrapper, toExtract, false, extractSlotStart, extractSlotEnd, filter);
                return true;
            }
        }
        return false;
    }

    public static ItemStack extractItem(SlotlessableItemHandlerWrapper extractWrapper, int maxExtract, boolean simulate, int slotStart, int slotEnd, FilterSettings filter) {
        ItemStack extracted = ItemStack.EMPTY;

        if (ActuallyAdditions.commonCapsLoaded) {
/*            Object handler = extractWrapper.getSlotlessHandler();
            if (handler instanceof ISlotlessItemHandler) {
                ISlotlessItemHandler slotless = (ISlotlessItemHandler) handler;

                if (filter == null || !filter.needsCheck()) {
                    extracted = slotless.extractItem(maxExtract, simulate);
                    return extracted;
                } else {
                    ItemStack would = slotless.extractItem(maxExtract, true);
                    if (filter.check(would)) {
                        if (simulate) {
                            extracted = would;
                        } else {
                            extracted = slotless.extractItem(maxExtract, false);
                        }
                    }
                    //Leave the possibility to fall back to vanilla when there is a filter
                }
            }*/
        }

        // SkyTekx: the extraction of 5a1eabea. The base had it commented out, so doItemInteraction never
        // moved an item and the Hopping Item Interface neither pulled nor pushed
        if (!StackUtil.isValid(extracted)) {
            IItemHandler handler = extractWrapper.getNormalHandler().resolve().orElse(null);
            if (handler != null) {
                for (int i = Math.max(0, slotStart); i < Math.min(slotEnd, handler.getSlots()); i++) {
                    if (filter == null || !filter.needsCheck() || filter.check(handler.getStackInSlot(i))) {
                        extracted = handler.extractItem(i, maxExtract, simulate);

                        if (StackUtil.isValid(extracted)) {
                            break;
                        }
                    }
                }
            }
        }

        return extracted;
    }

    public static void doEnergyInteraction(BlockEntity tileFrom, BlockEntity tileTo, Direction sideTo, int maxTransfer) {
        if (maxTransfer > 0) {
            Direction opp = sideTo == null
                ? null
                : sideTo.getOpposite();
            LazyOptional<IEnergyStorage> handlerFrom = tileFrom.getCapability(ForgeCapabilities.ENERGY, sideTo);
            LazyOptional<IEnergyStorage> handlerTo = tileTo.getCapability(ForgeCapabilities.ENERGY, opp);
            handlerFrom.ifPresent((from) -> {
                handlerTo.ifPresent((to) -> {
                    int drain = from.extractEnergy(maxTransfer, true);
                    if (drain > 0) {
                        int filled = to.receiveEnergy(drain, false);
                        from.extractEnergy(filled, false);
                    }
                });
            });
        }
    }

    public static void doFluidInteraction(BlockEntity tileFrom, BlockEntity tileTo, Direction sideTo, int maxTransfer) {
        if (maxTransfer > 0) {
            LazyOptional<IFluidHandler> optionalFrom = tileFrom.getCapability(ForgeCapabilities.FLUID_HANDLER, sideTo);
            LazyOptional<IFluidHandler> optionalTo = tileTo.getCapability(ForgeCapabilities.FLUID_HANDLER, sideTo.getOpposite());
            optionalFrom.ifPresent((from) -> {
                optionalTo.ifPresent((to) -> {
                    FluidStack drain = from.drain(maxTransfer, IFluidHandler.FluidAction.SIMULATE);
                    if (!drain.isEmpty()) {
                        int filled = to.fill(drain.copy(), IFluidHandler.FluidAction.EXECUTE);
                        from.drain(filled, IFluidHandler.FluidAction.EXECUTE);
                    }
                });
            });
        }
    }

    /**
     * Checks if a given Block with a given Meta is present in given Positions
     *
     * @param positions The Positions, an array of {x, y, z} arrays containing Positions
     * @param block     The Block
     * @param level     The World
     * @return Is every block present?
     */
    public static boolean hasBlocksInPlacesGiven(BlockPos[] positions, Block block, Level level) {
        for (BlockPos pos : positions) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() == block)) {
                return false;
            }
        }
        return true;
    }

    public static ItemStack useItemAtSide(Direction side, Level level, BlockPos pos, ItemStack stack) {
        return useItemAtSide(side, level, pos, stack, pos);
    }

    // SkyTekx: the named fake player of AAFakePlayers stands at the machine, which for the Phantom Placer
    // is not the block it places next to. Forge 47 gives every FakePlayer its own net handler, so the
    // NetHandlerSpaghettiServer of 1.12 is no longer needed (main comments it out too)
    public static ItemStack useItemAtSide(Direction side, Level level, BlockPos pos, ItemStack stack, BlockPos machine) {
        if (level instanceof ServerLevel serverLevel && StackUtil.isValid(stack) && pos != null) {
            BlockPos offsetPos = pos.relative(side);
            // SkyTekx: the replace check runs in the try with the fake player in its context, so a modded
            // canBeReplaced that reads the player of the context cannot crash the tick of the machine
            try (AAFakePlayers.Use use = AAFakePlayers.use(serverLevel, machine)) {
                FakePlayer fake = use.player();

                BlockState state = level.getBlockState(offsetPos);
                // SkyTekx: the replace check of a117dd53, the base hard coded false
                boolean replaceable = state.canBeReplaced(new BlockPlaceContext(level, fake, InteractionHand.MAIN_HAND, stack,
                        new BlockHitResult(new Vec3(0, 0, 0), side, pos, false)
                ));

                //Redstone
                // SkyTekx: the wire goes through the place event like every other block, and a refused
                // wire keeps the redstone in the machine
                if (replaceable && stack.getItem() == Items.REDSTONE) {
                    if (AAFakePlayers.placeBlock(serverLevel, machine, offsetPos, Blocks.REDSTONE_WIRE.defaultBlockState(), 2, side.getOpposite(), stack)) {
                        return StackUtil.shrink(stack, 1);
                    }
                    return stack;
                }

                //Plants
/*                if (replaceable && stack.getItem() instanceof IPlantable) { //TODO
                    if (((IPlantable) stack.getItem()).getPlant(world, offsetPos).getBlock().canPlaceBlockAt(world, offsetPos)) {
                        if (world.setBlock(offsetPos, ((IPlantable) stack.getItem()).getPlant(world, offsetPos), 2)) {
                            return StackUtil.shrink(stack, 1);
                        }
                    }
                }*/

                //Everything else
                use.hold(stack.copy());
                BlockHitResult ray = new BlockHitResult(new Vec3(0.5, 0.5, 0.5), side.getOpposite(), offsetPos, true);
                fake.gameMode.useItemOn(fake, level, fake.getMainHandItem(), InteractionHand.MAIN_HAND, ray);
                // SkyTekx: a copy goes back and the use gives the hands of the fake player back, so the
                // slot of the machine never holds the stack the fake player holds
                return fake.getItemInHand(InteractionHand.MAIN_HAND).copy();
            } catch (Exception e) {
                ActuallyAdditions.LOGGER.error("Something that places Blocks at " + offsetPos.getX() + ", " + offsetPos.getY() + ", " + offsetPos.getZ() + " in World " + level.dimension() + " threw an Exception! Don't let that happen again!", e);
            }
        }
        return stack;
    }

    public static boolean dropItemAtSide(Direction side, Level level, BlockPos pos, ItemStack stack) {
        BlockPos coords = pos.relative(side);
        if (level.hasChunkAt(coords)) {
            ItemEntity item = new ItemEntity(level, coords.getX() + 0.5, coords.getY() + 0.5, coords.getZ() + 0.5, stack);
            item.setDeltaMovement(0,0,0);

            return level.addFreshEntity(item);
        }
        return false;
    }

    public static Direction getDirectionBySidesInOrder(int side) {
        switch (side) {
            case 0:
                return Direction.UP;
            case 1:
                return Direction.DOWN;
            case 2:
                return Direction.NORTH;
            case 3:
                return Direction.EAST;
            case 4:
                return Direction.SOUTH;
            default:
                return Direction.WEST;
        }
    }

    public static Direction getDirectionByPistonRotation(BlockState state) {
        return state.getValue(BlockStateProperties.FACING);
    }

    public static ArrayList<BlockState> getStatesAround(Level level, BlockPos pos) {
        ArrayList<BlockState> blocks = new ArrayList<>();
        blocks.add(level.getBlockState(pos.relative(Direction.NORTH)));
        blocks.add(level.getBlockState(pos.relative(Direction.EAST)));
        blocks.add(level.getBlockState(pos.relative(Direction.SOUTH)));
        blocks.add(level.getBlockState(pos.relative(Direction.WEST)));
        return blocks;
    }

    public static void setHandItemWithoutAnnoyingSound(Player player, InteractionHand hand, ItemStack stack) {
        if (hand == InteractionHand.MAIN_HAND) {
            player.getInventory().items.set(player.getInventory().selected, stack);
        } else if (hand == InteractionHand.OFF_HAND) {
            player.getInventory().offhand.set(0, stack);
        }
    }

    //I think something is up with this, but I'm not entirely certain what.
    // SkyTekx: the drop chance of 17d8dcce and 902e8064. The base never answered anything but 0, so the
    // Long-Range Breaker and the Vertical Digger mined nothing. The named fake player of AAFakePlayers
    // stands at the machine and holds a Netherite AIOT for the harvest check, as the [Minecraft] player
    // of main does, and gets its hands back afterwards. On Forge 47 BlockState#canHarvestBlock posts
    // the HarvestCheck that EventHooks.doPlayerHarvestCheck posts on main. AAFakePlayers#postBreak takes
    // back the capture context that Arclight pushes for the break event
    public static float fireFakeHarvestEventsForDropChance(BlockEntity caller, Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            try (AAFakePlayers.Use use = AAFakePlayers.use(serverLevel, caller.getBlockPos())) {
                FakePlayer fake = use.player();
                use.hold(new ItemStack(ActuallyItems.NETHERITE_AIOT.get()));
                BlockState state = level.getBlockState(pos);
                if (AAFakePlayers.postBreak(new BreakEvent(level, pos, state, fake))) {
                    return state.canHarvestBlock(level, pos, fake) ? 1F : 0F;
                }
            }
        }
        return 0F;
    }

    // SkyTekx: a local change. The Leaf-Eating Generator and the Heat Collector remove blocks without a
    // tool or drops, and upstream main posts no event for that. They ask here first, so protection mods
    // and, on Arclight, Bukkit plugins see the break of the named fake player at the machine and can
    // refuse it. Answers true when the block may go
    public static boolean fireFakeBreakEvent(BlockEntity caller, Level level, BlockPos pos) {
        if (level instanceof ServerLevel serverLevel) {
            return AAFakePlayers.mayBreak(serverLevel, caller.getBlockPos(), pos);
        }
        return false;
    }

    /**
     * Tries to break a block as if this player had broken it.  This is a complex operation.
     *
     * @param stack  The player's current held stack, main hand.
     * @param level  The player's world.
     * @param player The player that is breaking this block.
     * @param pos    The pos to break.
     * @return If the break was successful.
     */
    public static boolean breakExtraBlock(ItemStack stack, Level level, Player player, BlockPos pos) {
        return breakExtraBlock(stack, level, player, pos, true);
    }

    // SkyTekx: isExtra is false for the block the player mined. ServerPlayerGameMode#destroyBlock already
    // posted its break event before Item#onBlockStartBreak handed the break to the drill, so a second one
    // made protection and logging plugins see that block broken twice. Its experience is worked out as
    // the BreakEvent constructor does, with the fortune or silk touch the drill holds by now. The extra
    // blocks of a drill in creative post their break event too, the base removed them without one.
    // bb06b24a adds the call to Block#playerWillDestroy that 1.21 moved out of onDestroyedByPlayer. On
    // Forge 47 IForgeBlock#onDestroyedByPlayer still makes that call, so only its air check comes over
    public static boolean breakExtraBlock(ItemStack stack, Level level, Player player, BlockPos pos, boolean isExtra) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();

        if (state.isAir()) {
            return false;
        }

        if (player.isCreative()) {
            if (isExtra && !level.isClientSide && player instanceof ServerPlayer serverPlayer) {
                if (ForgeHooks.onBlockBreakEvent(level, serverPlayer.gameMode.getGameModeForPlayer(), serverPlayer, pos) == -1) {
                    return false;
                }
            }
            if (block.onDestroyedByPlayer(state, level, pos, player, false, state.getFluidState())) {
                block.destroy(level, pos, state);
            }

            // send update to client
            if (!level.isClientSide) {
                //((ServerPlayerEntity) player).connection.send(new SPacketBlockChange(world, pos)); //TODO dunno what this is
            }
            return true;
        }

        // callback to the tool the player uses. Called on both sides. This damages the tool n stuff.
        stack.mineBlock(level, state, pos, player);

        // server sided handling
        if (!level.isClientSide) {
            // send the blockbreak event
            int xp;
            if (isExtra) {
                xp = ForgeHooks.onBlockBreakEvent(level, ((ServerPlayer) player).gameMode.getGameModeForPlayer(), (ServerPlayer) player, pos);
                if (xp == -1) {
                    return false;
                }
            } else {
                xp = ForgeHooks.isCorrectToolForDrops(state, player)
                    ? state.getExpDrop(level, level.random, pos, stack.getEnchantmentLevel(Enchantments.BLOCK_FORTUNE), stack.getEnchantmentLevel(Enchantments.SILK_TOUCH))
                    : 0;
            }

            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (block.onDestroyedByPlayer(state, level, pos, player, true, state.getFluidState())) { // boolean is if block can be harvested, checked above
                block.destroy(level, pos, state);
                block.playerDestroy(level, player, pos, state, blockEntity, stack);
                block.popExperience(((ServerLevel) level), pos, xp);
            }

            // always send block update to client
            //((ServerPlayerEntity) player).connection.send(new SPacketBlockChange(world, pos)); //TODO how
            return true;
        }
        // client sided handling
        else {
            // clientside we do a "this block has been clicked on long enough to be broken" call. This should not send any new packets
            // the code above, executed on the server, sends a block-updates that give us the correct state of the block we destroy.

            // following code can be found in PlayerControllerMP.onPlayerDestroyBlock
            level.levelEvent(2001, pos, Block.getId(state));
            if (block.onDestroyedByPlayer(state, level, pos, player, true, state.getFluidState())) {
                block.destroy(level, pos, state);
            }
            // callback to the tool
            stack.mineBlock(level, state, pos, player);

            // send an update to the server, so we get an update back
            // SkyTekx: through DistExecutor, so that this common class only names the client class
            // in a lambda that runs on the physical client
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> ActuallyAdditionsClient.sendBreakPacket(pos));
            return true;
        }
    }
}
