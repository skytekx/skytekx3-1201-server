/*
 * This file ("ItemCrafterOnAStick.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.util.Util;
import de.ellpeck.actuallyadditions.mod.util.compat.CuriosCompat;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;

public class ItemCrafterOnAStick extends ItemBase {
    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

    public ItemCrafterOnAStick() {
        super(ActuallyItems.defaultNonStacking());
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (!world.isClientSide) {
            openCraftingMenu(player);
        }
        return new InteractionResultHolder<>(InteractionResult.SUCCESS, player.getItemInHand(hand));
    }

    // SkyTekx: c4d81aea. The base opened the crafting menu with ContainerLevelAccess.NULL, which runs
    // nothing it is handed, so the grid never showed a result and the items in it were deleted when
    // the menu closed. The menu stays open without a crafting table at the player's position, as on
    // main
    public static void openCraftingMenu(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((windowId, playerInventory, playerEntity) -> new CraftingMenu(windowId, playerInventory, ContainerLevelAccess.create(player.level(), player.blockPosition())) {
            @Override
            public boolean stillValid(@Nonnull Player playerIn) {
                return true;
            }
        }, CONTAINER_TITLE));
    }

    // SkyTekx: b5cb3434, for the key bind. The crafter counts in either hand, in a Curios slot when
    // Curios is loaded (CuriosCompat, the charm slot by default) and anywhere in the 36 slots of the
    // main inventory
    public static boolean hasCrafterOnAStick(@Nonnull Player player) {
        if (isCrafterOnAStick(player.getMainHandItem()) || isCrafterOnAStick(player.getOffhandItem())) {
            return true;
        }

        if (Util.curiosLoaded && CuriosCompat.hasCurio(player, ItemCrafterOnAStick::isCrafterOnAStick)) {
            return true;
        }

        Inventory inventory = player.getInventory();
        for (int i = 0; i < inventory.items.size(); i++) {
            if (isCrafterOnAStick(inventory.items.get(i))) {
                return true;
            }
        }

        return false;
    }

    public static boolean isCrafterOnAStick(@Nonnull ItemStack stack) {
        return stack.getItem() instanceof ItemCrafterOnAStick;
    }
}
