package com.enderio.base.common.item.filter;

import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.menu.LimitedItemFilterMenu;
import com.enderio.core.common.capability.DamageFilterMode;
import com.enderio.core.common.capability.LimitedItemFilterCapability;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * The limited item filter of Ender IO 8.x (LimitedItemFilterItem, 1a85694c2) as a 6.x filter item.
 */
public class LimitedItemFilter extends Item {

    public LimitedItemFilter(Properties pProperties) {
        super(pProperties);
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
                if (capability == EIOCapabilities.FILTER) {
                    return LazyOptional.of(() -> new LimitedItemFilterCapability(stack)).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        // The menu edits the main hand stack, so a filter used from the off hand must not open it.
        if (pPlayer instanceof ServerPlayer serverPlayer && pUsedHand == InteractionHand.MAIN_HAND) {
            openMenu(serverPlayer);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        readFilter(stack).ifPresent(filter -> {
            if (filter.getEntries().stream().allMatch(ItemStack::isEmpty) && !filter.isNbt()
                && filter.getDamageMode() == DamageFilterMode.IGNORE) {
                tooltip.add(FilterTooltips.unconfiguredHint());
            }

            tooltip.add(FilterTooltips.nbtMode(filter.isNbt()));
            tooltip.add(FilterTooltips.damageMode(filter.getDamageMode()));
        });
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (FilterTooltips.isAdditionalTooltipHidden(stack)) {
            return Optional.empty();
        }

        // The item counts show the stock levels.
        return readFilter(stack).flatMap(filter -> FilterTooltips.itemList(filter.getEntries()));
    }

    /**
     * Reads the filter of a copy, because the capability writes the empty entry list into a stack that has none,
     * and a tooltip must not change the stack it shows.
     */
    private static Optional<LimitedItemFilterCapability> readFilter(ItemStack stack) {
        return stack.copy().getCapability(EIOCapabilities.FILTER).resolve()
            .filter(LimitedItemFilterCapability.class::isInstance)
            .map(LimitedItemFilterCapability.class::cast);
    }

    private static void openMenu(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return player.getMainHandItem().getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
                return new LimitedItemFilterMenu(pContainerId, pInventory, player.getMainHandItem());
            }
        });
    }
}
