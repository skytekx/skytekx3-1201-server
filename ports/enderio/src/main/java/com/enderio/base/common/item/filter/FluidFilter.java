package com.enderio.base.common.item.filter;

import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.menu.FluidFilterMenu;
import com.enderio.core.common.capability.FluidFilterCapability;
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
import net.minecraftforge.fluids.FluidStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class FluidFilter extends Item {

    private final int size;

    public FluidFilter(Properties pProperties, int size) {
        super(pProperties);
        this.size = size;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
                if (capability == EIOCapabilities.FILTER) {
                    return LazyOptional.of(() -> new FluidFilterCapability(stack, size)).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer) {
            openMenu(serverPlayer);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        readFilter(stack).ifPresent(filter -> {
            if (filter.getEntries().stream().allMatch(FluidStack::isEmpty) && !filter.isInvert() && !filter.isNbt()) {
                tooltip.add(FilterTooltips.unconfiguredHint());
            }

            tooltip.add(FilterTooltips.listMode(filter.isInvert()));
            tooltip.add(FilterTooltips.nbtMode(filter.isNbt()));
        });
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (FilterTooltips.isAdditionalTooltipHidden(stack)) {
            return Optional.empty();
        }

        return readFilter(stack).flatMap(filter -> FilterTooltips.fluidList(filter.getEntries()));
    }

    /**
     * Reads the filter of a copy, because the capability writes the empty entry list into a stack that has none,
     * and a tooltip must not change the stack it shows.
     */
    private static Optional<FluidFilterCapability> readFilter(ItemStack stack) {
        return stack.copy().getCapability(EIOCapabilities.FILTER).resolve()
            .filter(FluidFilterCapability.class::isInstance)
            .map(FluidFilterCapability.class::cast);
    }

    private static void openMenu(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
                return new FluidFilterMenu(pContainerId, pInventory, player.getMainHandItem());
            }
        });
    }
}
