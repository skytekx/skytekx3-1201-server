package de.ellpeck.actuallyadditions.mod.inventory.slot;

import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;

import javax.annotation.Nullable;

/**
 * SkyTekx: the armor slot of upstream 556fa487 on the 1.20.1 API, a copy of the armor slots of
 * vanilla's InventoryMenu. A slot takes what the player could wear there (ItemStack.canEquip, which
 * asks Mob.getEquipmentSlotForItem unless the item decides itself), plays the equip sound and keeps
 * armor with the Curse of Binding on a player who is not in creative mode
 */
public class ArmorSlot extends Slot {
    private final Player owner;
    private final EquipmentSlot slot;

    public ArmorSlot(Inventory inventory, EquipmentSlot slot, int slotIndex, int x, int y) {
        super(inventory, slotIndex, x, y);
        this.owner = inventory.player;
        this.slot = slot;
    }

    @Nullable
    public static ResourceLocation emptyIcon(EquipmentSlot slot) {
        return switch (slot) {
            case HEAD -> InventoryMenu.EMPTY_ARMOR_SLOT_HELMET;
            case CHEST -> InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE;
            case LEGS -> InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS;
            case FEET -> InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS;
            case OFFHAND -> InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD;
            default -> null;
        };
    }

    @Override
    public void setByPlayer(ItemStack stack) {
        this.owner.onEquipItem(this.slot, this.getItem(), stack);
        super.setByPlayer(stack);
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return !stack.isEmpty() && stack.canEquip(this.slot, this.owner);
    }

    @Override
    public boolean mayPickup(Player player) {
        ItemStack itemstack = this.getItem();
        return !itemstack.isEmpty() && !player.isCreative() && EnchantmentHelper.hasBindingCurse(itemstack)
            ? false
            : super.mayPickup(player);
    }

    @Nullable
    @Override
    public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
        ResourceLocation icon = emptyIcon(this.slot);
        return icon != null ? Pair.of(InventoryMenu.BLOCK_ATLAS, icon) : super.getNoItemIcon();
    }

    /**
     * SkyTekx: the off hand slot of upstream c1d10d12 (#1456), which takes any item like the off
     * hand slot of the player inventory
     */
    public static class OffHand extends Slot {
        private final Player owner;

        public OffHand(Inventory inventory, int x, int y) {
            super(inventory, 40, x, y);
            this.owner = inventory.player;
        }

        @Override
        public void setByPlayer(ItemStack stack) {
            this.owner.onEquipItem(EquipmentSlot.OFFHAND, this.getItem(), stack);
            super.setByPlayer(stack);
        }

        @Override
        public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
            return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
        }
    }
}
