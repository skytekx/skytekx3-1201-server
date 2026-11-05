package com.enderio.machines.common.io;

import com.enderio.api.io.IOMode;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import org.slf4j.Logger;

public class TransferUtil {

    private static final Logger LOGGER = LogUtils.getLogger();

    // region Items

    public static void distributeItems(IOMode mode, IItemHandler selfItemHandler, IItemHandler otherItemHandler) {
        distributeItems(mode.canPush(), mode.canPull(), selfItemHandler, otherItemHandler);
    }

    public static void distributeItems(boolean canPush, boolean canPull, IItemHandler selfItemHandler, IItemHandler otherItemHandler) {
        if (canPush) {
            moveItems(selfItemHandler, otherItemHandler);
        }

        if (canPull) {
            moveItems(otherItemHandler, selfItemHandler);
        }
    }

    // Moves one item per call. The target only ever receives what really left the source,
    // so a source whose real extraction differs from its simulation cannot duplicate items.
    private static void moveItems(IItemHandler from, IItemHandler to) {
        for (int i = 0; i < from.getSlots(); i++) {
            ItemStack simulated = from.extractItem(i, 1, true);
            if (simulated.isEmpty()) {
                continue;
            }

            for (int j = 0; j < to.getSlots(); j++) {
                // Find a slot that takes the item before anything leaves the source.
                if (!to.insertItem(j, simulated, true).isEmpty()) {
                    continue;
                }

                ItemStack extracted = from.extractItem(i, 1, false);
                if (extracted.isEmpty()) {
                    return;
                }

                ItemStack remainder = to.insertItem(j, extracted, false);
                if (!remainder.isEmpty()) {
                    // The target refused what its simulation accepted. Try its other slots, then give it back to the source.
                    remainder = ItemHandlerHelper.insertItem(to, remainder, false);
                    if (!remainder.isEmpty()) {
                        remainder = from.insertItem(i, remainder, false);
                    }
                    if (!remainder.isEmpty()) {
                        remainder = ItemHandlerHelper.insertItem(from, remainder, false);
                    }
                    if (!remainder.isEmpty()) {
                        LOGGER.warn("Item transfer lost {}: the target refused it after its simulation accepted it, and the source did not take it back", remainder);
                    }
                }
                return;
            }
        }
    }

    // endregion

    // region Fluids

    public static final int DEFAULT_FLUID_DRAIN = 100;

    public static void distributeFluids(IOMode mode, IFluidHandler selfItemHandler, IFluidHandler otherItemHandler) {
        distributeFluids(mode.canPush(), mode.canPull(), selfItemHandler, otherItemHandler, DEFAULT_FLUID_DRAIN);
    }

    public static void distributeFluids(IOMode mode, IFluidHandler selfItemHandler, IFluidHandler otherItemHandler, int maxDrain) {
        distributeFluids(mode.canPush(), mode.canPull(), selfItemHandler, otherItemHandler, maxDrain);
    }

    public static void distributeFluids(boolean canPush, boolean canPull, IFluidHandler selfItemHandler, IFluidHandler otherItemHandler) {
        distributeFluids(canPush, canPull, selfItemHandler, otherItemHandler, DEFAULT_FLUID_DRAIN);
    }

    public static void distributeFluids(boolean canPush, boolean canPull, IFluidHandler selfItemHandler, IFluidHandler otherItemHandler, int maxDrain) {
        // TODO: Do we want to imitate old behaviour where if we have no fluid, we pull by default?

        if (canPush) {
            int filled = 0;
            for (int i = 0; i < selfItemHandler.getTanks(); i++) {
                filled += FluidUtil.tryFluidTransfer(otherItemHandler, selfItemHandler, new FluidStack(selfItemHandler.getFluidInTank(i), maxDrain), true).getAmount();
            }
            if (filled > 0) {
                return;
            }
        }

        if (canPull) {
            for (int i = 0; i < selfItemHandler.getTanks(); i++) {
                // An empty tank takes any fluid, a filled tank only more of its own.
                if (selfItemHandler.getFluidInTank(i).isEmpty()) {
                    FluidUtil.tryFluidTransfer(selfItemHandler, otherItemHandler, maxDrain, true);
                } else {
                    FluidUtil.tryFluidTransfer(selfItemHandler, otherItemHandler, new FluidStack(selfItemHandler.getFluidInTank(i), maxDrain), true);
                }
            }
        }
    }

    // endregion
}
