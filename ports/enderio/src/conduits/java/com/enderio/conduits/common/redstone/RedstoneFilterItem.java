package com.enderio.conduits.common.redstone;

import com.enderio.api.filter.ResourceFilter;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.item.filter.FilterTooltips;
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
import net.minecraft.world.inventory.MenuType;
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
import java.util.function.Function;
import java.util.function.Supplier;

public class RedstoneFilterItem extends Item {

    @Nullable
    private final Supplier<MenuType<?>> menu;

    private final Function<ItemStack, ResourceFilter> filterFactory;

    public RedstoneFilterItem(Properties pProperties, Function<ItemStack, ResourceFilter> filterFactory, @Nullable Supplier<MenuType<?>> menu) {
        super(pProperties);
        this.menu = menu;
        this.filterFactory = filterFactory;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
                if (capability == EIOCapabilities.FILTER) {
                    return LazyOptional.of(() -> filterFactory.apply(stack)).cast();
                }
                return LazyOptional.empty();
            }
        };
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level pLevel, Player pPlayer, InteractionHand pUsedHand) {
        if (pPlayer instanceof ServerPlayer serverPlayer && menu != null) {
            openMenu(serverPlayer);
        }
        return super.use(pLevel, pPlayer, pUsedHand);
    }

    /**
     * 0c79259d9: a filter with a screen tells whether it is configured, or how to configure it.
     */
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);

        // The filter getters create the tag, so read a copy and leave the shown stack as it is.
        if (filterFactory.apply(stack.copy()) instanceof ConfigurableRedstoneFilter filter) {
            if (filter.isConfigured()) {
                tooltip.add(FilterTooltips.configured());
            } else if (menu != null) {
                tooltip.add(FilterTooltips.unconfiguredHint());
            }
        }
    }

    private void openMenu(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return Component.literal("");
            }

            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
                return menu.get().create(pContainerId, pInventory);
            }
        });
    }
}
