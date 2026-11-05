package com.enderio.conduits.common.conduit.type.item;

import com.enderio.api.conduit.ColoredRedstoneProvider;
import com.enderio.api.conduit.ConduitGraph;
import com.enderio.api.conduit.ConduitType;
import com.enderio.api.conduit.ticker.CapabilityAwareConduitTicker;
import com.enderio.api.filter.ItemStackFilter;
import com.enderio.api.filter.LimitingItemStackFilter;
import com.enderio.conduits.common.capability.ExtractionSpeedUpgrade;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

import java.util.Comparator;
import java.util.List;

public class ItemConduitTicker extends CapabilityAwareConduitTicker<ItemConduitData, IItemHandler> {

    public static ItemConduitTicker INSTANCE = new ItemConduitTicker();

    @Override
    protected void tickCapabilityGraph(
        ServerLevel level,
        ConduitType<ItemConduitData> type,
        List<CapabilityConnection> inserts,
        List<CapabilityConnection> extracts,
        ConduitGraph<ItemConduitData> graph,
        ColoredRedstoneProvider coloredRedstoneProvider) {

        inserts.sort(Comparator.comparingInt((CapabilityConnection conn) -> conn.data.compute(conn.direction).getPriority()).reversed());

        toNextExtract:
        for (CapabilityConnection extract: extracts) {
            IItemHandler extractHandler = extract.capability;
            int extracted = 0;

            int speed = 4;
            if (extract.upgrade instanceof ExtractionSpeedUpgrade speedUpgrade) {
                speed *= (int) Math.pow(2, speedUpgrade.tier());
            }

            nextItem:
            for (int i = 0; i < extractHandler.getSlots(); i++) {

                ItemStack extractedItem = extractHandler.extractItem(i, speed - extracted, true);
                if (extractedItem.isEmpty()) {
                    continue;
                }

                // Ensure we cap the stack to its max size, even if the parent handler doesn't respect it.
                int extractedItemMaxStack = extractedItem.getMaxStackSize();
                if (extractedItem.getCount() > extractedItemMaxStack) {
                    extractedItem.setCount(extractedItemMaxStack);
                }

                if (extract.extractFilter instanceof ItemStackFilter itemFilter) {
                    if (!itemFilter.test(extractedItem)) {
                        continue;
                    }

                    // SkyTekx: a limited item filter leaves its stock level in the source (8.x 292bd84ed).
                    if (itemFilter instanceof LimitingItemStackFilter limitingFilter) {
                        int limit = limitingFilter.getTransferLimit(extractedItem, extractHandler, false);
                        if (limit <= 0) {
                            continue;
                        }

                        if (extractedItem.getCount() > limit) {
                            extractedItem.setCount(limit);
                        }
                    }
                }

                ItemConduitData.ItemSidedData sidedExtractData = extract.data.castTo(ItemConduitData.class).compute(extract.direction);
                if (sidedExtractData.isRoundRobin) {
                    if (inserts.size() <= sidedExtractData.rotatingIndex) {
                        sidedExtractData.rotatingIndex = 0;
                    }
                } else {
                    sidedExtractData.rotatingIndex = 0;
                }

                for (int j = sidedExtractData.rotatingIndex; j < sidedExtractData.rotatingIndex + inserts.size(); j++) {
                    int insertIndex = j % inserts.size();
                    CapabilityConnection insert = inserts.get(insertIndex);

                    if (!sidedExtractData.isSelfFeed
                        && extract.direction == insert.direction
                        && extract.data == insert.data) {
                        continue;
                    }

                    int insertCount = extractedItem.getCount();
                    if (insert.insertFilter instanceof ItemStackFilter itemFilter) {
                        if (!itemFilter.test(extractedItem)) {
                            continue;
                        }

                        // SkyTekx: a limited item filter fills the target only up to its stock level (8.x 1a85694c2, 292bd84ed).
                        if (itemFilter instanceof LimitingItemStackFilter limitingFilter) {
                            int limit = limitingFilter.getTransferLimit(extractedItem, insert.capability, true);
                            if (limit <= 0) {
                                continue;
                            }

                            insertCount = Math.min(insertCount, limit);
                        }
                    }

                    // Copy the stack to ensure extractedItem isn't modified by item handler implementations (Create Chute for example)
                    ItemStack notInserted = ItemHandlerHelper.insertItem(insert.capability, extractedItem.copyWithCount(insertCount), false);
                    int successfullyInserted = insertCount - notInserted.getCount();

                    if (successfullyInserted > 0) {
                        extracted += successfullyInserted;
                        extractHandler.extractItem(i, successfullyInserted, false);
                        if (extracted >= speed || isEmpty(extractHandler, i + 1)) {
                            if (sidedExtractData.isRoundRobin) {
                                sidedExtractData.rotatingIndex = insertIndex + 1;
                            }
                            continue toNextExtract;
                        } else {
                            continue nextItem;
                        }
                    }
                }
            }
        }
    }

    private boolean isEmpty(IItemHandler itemHandler, int afterIndex) {
        for (var i = afterIndex; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    protected Capability<IItemHandler> getCapability() {
        return ForgeCapabilities.ITEM_HANDLER;
    }

    @Override
    public int getTickRate() {
        return 20;
    }
}
