package com.enderio.base.common.item.filter;

import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.base.common.menu.ItemFilterMenu;
import com.enderio.core.common.capability.ItemFilterCapability;
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
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemFilter extends Item {

    private final int size;
    private final boolean canMatchNbt;
    private final boolean canFilterByDamage;

    public ItemFilter(Properties pProperties, int size) {
        this(pProperties, size, true, false);
    }

    /**
     * The flags follow EnderItemFilterItem.Type of Ender IO 8.x: the big filter has no NBT match,
     * the advanced filters have a damage mode. The basic filter keeps its 6.x NBT toggle.
     */
    public ItemFilter(Properties pProperties, int size, boolean canMatchNbt, boolean canFilterByDamage) {
        super(pProperties);
        this.size = size;
        this.canMatchNbt = canMatchNbt;
        this.canFilterByDamage = canFilterByDamage;
    }

    @Override
    public @Nullable ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundTag nbt) {
        return new ICapabilityProvider() {
            @Override
            public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> capability, @Nullable Direction direction) {
                if (capability == EIOCapabilities.FILTER) {
                    return LazyOptional.of(() -> new ItemFilterCapability(stack, size, canMatchNbt, canFilterByDamage)).cast();
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

    private static void openMenu(ServerPlayer player) {
        player.openMenu(new MenuProvider() {
            @Override
            public Component getDisplayName() {
                return player.getMainHandItem().getHoverName();
            }

            @Override
            public AbstractContainerMenu createMenu(int pContainerId, Inventory pInventory, Player pPlayer) {
                return new ItemFilterMenu(pContainerId, pInventory, player.getMainHandItem());
            }
        });
    }
}
