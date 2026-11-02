package com.twilightmobsloots;

import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.StandingAndWallBlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

/**
 * Mob trophy item, modelled on Twilight Forest's {@code TrophyItem}: a {@link StandingAndWallBlockItem}
 * (use on a block to place it on the floor or a wall) that is also {@link Equipable}, so using it in the
 * air puts it on the head like a helmet.
 */
public class MobTrophyItem extends StandingAndWallBlockItem implements Equipable {

    private final MobHead head;

    public MobTrophyItem(MobHead head, Item.Properties properties, Block floorBlock, Block wallBlock) {
        super(floorBlock, wallBlock, properties, Direction.DOWN);
        this.head = head;
    }

    public MobHead getMobHead() {
        return this.head;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    /** Using the trophy in the air wears it on the head, like armor. */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return this.swapWithEquipmentSlot(this, level, player, hand);
    }

    /** 1.20.1 Forge replacement for NeoForge's RegisterClientExtensionsEvent; only called on the client. */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(com.twilightmobsloots.client.TrophyItemClientExtensions.INSTANCE);
    }
}
