/*
 * This file ("ItemMagnetRing.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.items.base.ItemEnergy;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

public class ItemMagnetRing extends ItemEnergy {

    // SkyTekx: a local change. Every ring stack in the inventory looked for items in its 11 block box
    // in every tick. The rings of a player now look every SCAN_INTERVAL ticks, staggered by the entity
    // id so that the players of a server do not all scan in the same tick, and only the first ring
    // that is switched on scans. An item is paid by the first switched on ring that has the energy for
    // it, so a second ring still takes over when the first one runs dry, as it did when each ring
    // scanned on its own
    public static final int SCAN_INTERVAL = 4;
    public static final int RANGE = 5;
    public static final int ENERGY_PER_ITEM = 50;

    public ItemMagnetRing() {
        super(200000, 1000);
    }

    // SkyTekx: the annotation of c10862ce
    @Override
    public boolean isFoil(@Nonnull ItemStack stack) {
        return !ItemUtil.isEnabled(stack);
    }

    // The ring is on while its NBT key IsEnabled is false or missing (ItemUtil#changeEnabled). This
    // reads it without adding a tag to the other rings
    private static boolean isSwitchedOn(ItemStack stack) {
        return stack.getTag() == null || !stack.getTag().getBoolean("IsEnabled");
    }

    private static List<ItemStack> switchedOnRings(Player player) {
        List<ItemStack> rings = new ArrayList<>(1);
        Inventory inventory = player.getInventory();
        for (List<ItemStack> compartment : List.of(inventory.items, inventory.armor, inventory.offhand)) {
            for (ItemStack candidate : compartment) {
                if (candidate.getItem() instanceof ItemMagnetRing && isSwitchedOn(candidate)) {
                    rings.add(candidate);
                }
            }
        }
        return rings;
    }

    @Override
    public void inventoryTick(@Nonnull ItemStack stack, @Nonnull Level world, @Nonnull Entity entity, int itemSlot, boolean isSelected) {
        if (entity instanceof Player player && !world.isClientSide && isSwitchedOn(stack)) {
	        if (player.isCreative() || player.isSpectator()) {
                return;
            }
            if ((world.getGameTime() + player.getId()) % SCAN_INTERVAL != 0) {
                return;
            }
            if (!entity.isShiftKeyDown()) {
                List<ItemStack> rings = switchedOnRings(player);
                if (rings.isEmpty() || rings.get(0) != stack) {
                    return;
                }
                //Get all the Items in the area
                int range = RANGE;
                List<ItemEntity> items = world.getEntitiesOfClass(ItemEntity.class, new AABB(entity.getX() - range, entity.getY() - range, entity.getZ() - range, entity.getX() + range, entity.getY() + range, entity.getZ() + range));
                if (!items.isEmpty()) {
                    for (ItemEntity item : items) {
                        if (item.getPersistentData().getBoolean("PreventRemoteMovement")) {
                            continue;
                        }
                        if (item.isAlive() && !item.hasPickUpDelay()) { // TODO config
                            int energyForItem = ENERGY_PER_ITEM * item.getItem().getCount();

                            ItemStack payer = null;
                            for (ItemStack ring : rings) {
                                if (this.getEnergyStored(ring) >= energyForItem) {
                                    payer = ring;
                                    break;
                                }
                            }

                            if (payer != null) {
                                ItemStack oldItem = item.getItem().copy();

                                // SkyTekx: the pickup of the player, with the Forge pickup event and,
                                // on Arclight, the Bukkit PlayerPickupItemEvent and
                                // EntityPickupItemEvent of its ItemEntityMixin
                                item.playerTouch(player);

                                if (!player.isCreative()) {
                                    if (!item.isAlive() || !ItemStack.matches(item.getItem(), oldItem)) {
                                        // SkyTekx: the internal extraction of 3ed5e9cc, which main swapped
                                        // for extractEnergy because the internal one finds no storage on
                                        // 1.21. On 1.20.1 it works and is not capped at the 1000 per tick
                                        // of the ring, so a stack of 64 still costs 3200
                                        this.extractEnergyInternal(payer, energyForItem, false);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, @Nonnull Player player, @Nonnull InteractionHand hand) {
        if (!worldIn.isClientSide && player.isShiftKeyDown()) {
            ItemUtil.changeEnabled(player, hand);
            return InteractionResultHolder.success(player.getItemInHand(hand));
        }

        return super.use(worldIn, player, hand);
    }
}
