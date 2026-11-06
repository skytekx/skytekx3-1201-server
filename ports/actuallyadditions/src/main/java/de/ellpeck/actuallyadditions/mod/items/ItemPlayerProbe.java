/*
 * This file ("ItemPlayerProbe.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityPlayerInterface;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class ItemPlayerProbe extends ItemBase {

    public ItemPlayerProbe() {
        super(ActuallyItems.defaultProps().stacksTo(1));
    }

    // SkyTekx: the probe stores the player with putUUID under UUID, and the checks look for that key as
    // in d2d0afa6. The base looked for UUIDMost, which putUUID never writes, so a probe never bound a
    // Player Interface. A probe with the long pair UUIDMost and UUIDLeast still works, and clearing
    // removes both forms
    @Nullable
    private static UUID getProbedPlayer(CompoundTag compound) {
        if (compound.hasUUID("UUID")) {
            return compound.getUUID("UUID");
        }
        if (compound.contains("UUIDMost", Tag.TAG_LONG) && compound.contains("UUIDLeast", Tag.TAG_LONG)) {
            return new UUID(compound.getLong("UUIDMost"), compound.getLong("UUIDLeast"));
        }
        return null;
    }

    // TODO: [port] might be the wrong event
    @Override
    public void inventoryTick(ItemStack stack, Level world, Entity entity, int itemSlot, boolean isSelected) {
        if (!world.isClientSide) {
            CompoundTag compound = stack.getOrCreateTag();
            UUID id = getProbedPlayer(compound);
            if (id != null) {
                Player player = world.getPlayerByUUID(id);
                if (player != null) {
                    if (player.isShiftKeyDown()) {
                        ItemPhantomConnector.clearStorage(stack, "UUID", "UUIDLeast", "UUIDMost", "Name");
                        ((Player) entity).displayClientMessage(Component.translatable("tooltip." + ActuallyAdditions.MODID + ".playerProbe.disconnect.1"), false);
                        player.displayClientMessage(Component.translatable("tooltip." + ActuallyAdditions.MODID + ".playerProbe.notice"), false);
                        //TheAchievements.GET_UNPROBED.get(player);
                    }
                } else {
                    ItemPhantomConnector.clearStorage(stack, "UUID", "UUIDLeast", "UUIDMost", "Name");
                    ((Player) entity).displayClientMessage(Component.translatable("tooltip." + ActuallyAdditions.MODID + ".playerProbe.disconnect.2"), false);
                }
            }
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.FAIL;
        }

        // SkyTekx: the stack of the context (upstream e08ef13a)
        ItemStack stack = context.getItemInHand();
        BlockEntity tile = context.getLevel().getBlockEntity(context.getClickedPos());
        if (tile instanceof TileEntityPlayerInterface face) {
            CompoundTag compound = stack.getOrCreateTag();
            UUID id = getProbedPlayer(compound);
            if (id != null) {
                if (!context.getLevel().isClientSide) {
                    face.connectedPlayer = id;
                    face.playerName = compound.getString("Name");
                    face.setChanged();
                    face.sendUpdate();

                    ItemPhantomConnector.clearStorage(stack, "UUID", "UUIDLeast", "UUIDMost", "Name");
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.FAIL;
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack aStack, Player player, LivingEntity entity, InteractionHand hand) {
        if (!player.level().isClientSide) {
            ItemStack stack = player.getMainHandItem();
            if (StackUtil.isValid(stack) && stack.getItem() == this) {
                if (entity instanceof Player playerHit) {

	                if (!playerHit.isShiftKeyDown()) {
                        CompoundTag compound = stack.getOrCreateTag();
                        compound.putString("Name", playerHit.getName().getString());
                        compound.putUUID("UUID", playerHit.getUUID());
                        return InteractionResult.SUCCESS;
                    }
                }
            }
        }
        return InteractionResult.FAIL;
    }

    @OnlyIn(Dist.CLIENT)
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level playerIn, List<Component> tooltip, TooltipFlag advanced) {
        if (stack.getOrCreateTag().contains("Name")) {
            String name = stack.getOrCreateTag().getString("Name");
            tooltip.add(Component.translatable("tooltip." + ActuallyAdditions.MODID + ".playerProbe.probing").append(": " + name));
        }
    }
}
