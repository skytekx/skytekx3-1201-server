/*
 * This file ("ItemBag.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.items;

import de.ellpeck.actuallyadditions.mod.inventory.SackContainer;
import de.ellpeck.actuallyadditions.mod.inventory.VoidSackContainer;
import de.ellpeck.actuallyadditions.mod.items.base.ItemBase;
import de.ellpeck.actuallyadditions.mod.sack.SackData;
import de.ellpeck.actuallyadditions.mod.sack.SackManager;
import de.ellpeck.actuallyadditions.mod.tile.FilterSettings;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nonnull;
import java.util.UUID;

// SkyTekx: the sacks of main. The Traveler's Sack keeps its inventory and filter in SackData under
// the stack's NBT key UUID, the Void Sack its filter in the stack's NBT key Filter, and both keep
// the pickup toggle in the stack's NBT key AutoInsert, the key of the base (main's data components
// AUTO_INSERT and FILTER_SETTINGS, 669e8437, 521cf822, bc20d82f)
public class Sack extends ItemBase {
    public static final String AUTO_INSERT_KEY = "AutoInsert";
    public static final String FILTER_KEY = "Filter";
    public static final int FILTER_SLOTS = 4;

    public final boolean isVoid;

    public Sack(boolean isVoid) {
        super(ActuallyItems.defaultProps().stacksTo(1));
        this.isVoid = isVoid;
    }

    // SkyTekx: main comments out the old emptying into a clicked inventory (TODO in Sack.useOn), as
    // it read the contents from the stack's NBT, where the sacks of the UUID system keep nothing. The
    // base still returned SUCCESS on every block entity, so the sack did nothing there and did not
    // open
    @Nonnull
    @Override
    public InteractionResult useOn(UseOnContext context) {
        return InteractionResult.PASS;
    }

    @Nonnull
    @Override
    public InteractionResultHolder<ItemStack> use(Level world, Player player, @Nonnull InteractionHand hand) {
        ItemStack sackStack = player.getItemInHand(hand);
        if (!world.isClientSide && hand == InteractionHand.MAIN_HAND && sackStack.getItem() instanceof Sack && player instanceof ServerPlayer serverPlayer) {
            boolean autoInsert = isAutoInsert(sackStack);
            if (!this.isVoid) {
                SackData data = getData(sackStack);
                if (data == null)
                    return InteractionResultHolder.fail(sackStack);

                UUID uuid = data.getUuid();
                data.updateAccessRecords(player.getName().getString(), System.currentTimeMillis());

                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inv, entity) ->
                        new SackContainer(id, inv, data.getSpecialHandler(), autoInsert, data.getFilter()), sackStack.getHoverName()),
                    buffer -> {
                        buffer.writeUUID(uuid);
                        buffer.writeBoolean(autoInsert);
                        buffer.writeInt(data.getFilter().getPackedSettings());
                    });
            } else {
                FilterSettings filter = readVoidFilter(sackStack);
                NetworkHooks.openScreen(serverPlayer, new SimpleMenuProvider((id, inv, entity) ->
                        new VoidSackContainer(id, inv, autoInsert, filter), sackStack.getHoverName()),
                    buffer -> {
                        buffer.writeBoolean(autoInsert);
                        buffer.writeInt(filter.getPackedSettings());
                    });
            }
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    public static boolean isAutoInsert(ItemStack stack) {
        CompoundTag tag = stack.getTag();
        return tag != null && tag.getBoolean(AUTO_INSERT_KEY);
    }

    public static void setAutoInsert(ItemStack stack, boolean autoInsert) {
        stack.getOrCreateTag().putBoolean(AUTO_INSERT_KEY, autoInsert);
    }

    /**
     * The filter of a Void Sack, from the stack's NBT key Filter. A stack without it has an empty
     * filter that lets everything through, as on main.
     */
    public static FilterSettings readVoidFilter(ItemStack stack) {
        FilterSettings filter = new FilterSettings(FILTER_SLOTS, false, false, false, false);
        CompoundTag tag = stack.getTag();
        if (tag != null && tag.contains(FILTER_KEY)) {
            filter.readFromNBT(tag, FILTER_KEY);
        }
        return filter;
    }

    public static void writeVoidFilter(ItemStack stack, FilterSettings filter) {
        filter.writeToNBT(stack.getOrCreateTag(), FILTER_KEY);
    }

    public static SackData getData(ItemStack stack) {
        if (!(stack.getItem() instanceof Sack))
            return null;
        UUID uuid;
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains("UUID")) {
            uuid = UUID.randomUUID();
            tag.putUUID("UUID", uuid);
        } else
            uuid = tag.getUUID("UUID");
        return SackManager.get().getOrCreateSack(uuid);
    }
}
