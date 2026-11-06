/*
 * This file ("CommonEvents.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.event;

import de.ellpeck.actuallyadditions.api.ActuallyTags;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.BlockLaserRelay;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.config.values.ConfigBoolValues;
import de.ellpeck.actuallyadditions.mod.data.PlayerData;
import de.ellpeck.actuallyadditions.mod.data.WorldData;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import de.ellpeck.actuallyadditions.mod.items.ItemTag;
import de.ellpeck.actuallyadditions.mod.items.Sack;
import de.ellpeck.actuallyadditions.mod.network.PacketHelperServer;
import de.ellpeck.actuallyadditions.mod.sack.SackData;
import de.ellpeck.actuallyadditions.mod.sack.SackManager;
import de.ellpeck.actuallyadditions.mod.util.ItemStackHandlerAA;
import de.ellpeck.actuallyadditions.mod.util.ItemUtil;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Locale;
import java.util.Optional;

public class CommonEvents {

    // SkyTekx: the workaround of 99e16633 in place of the empty spawner break listener. A sneaking
    // player with an item in hand skips the block, so a crouching compass click never reached the
    // laser relay and could not lower the priority of an item relay
    @SubscribeEvent
    public void onBlockRightClick(PlayerInteractEvent.RightClickBlock event) { //Workaround, cant sneak right click a block with an item normally.
        if (event.getLevel().isClientSide)
            return;
        if( (event.getLevel().getBlockState(event.getHitVec().getBlockPos()).getBlock() instanceof BlockLaserRelay) && (event.getItemStack().is(CommonConfig.Other.relayConfigureItem))) {
            event.setUseItem(Event.Result.DENY);
            event.setUseBlock(Event.Result.ALLOW);
        }

    }

    // SkyTekx: the sack pickup of main (edccaec7, 6389668b, 669e8437, 521cf822, bc20d82f). A sack with
    // AutoInsert in a slot other than the selected one takes the items its filter lets through, the
    // Traveler's Sack into its SackData with that data's filter, the Void Sack into nothing with the
    // filter in its own NBT. The base read the 1.12 contents and filter from the stack's NBT, wrote
    // the items back there, where the sack menu never looked, and checked a filter that no menu saved.
    // Forge 47 calls Player.take for an ALLOW result (ItemEntity.playerTouch), and its packet plays
    // the pickup sound on the client, so the sound of 669e8437 is not needed here. Arclight overwrites
    // playerTouch and posts the Bukkit PlayerPickupItemEvent and EntityPickupItemEvent only after this
    // event, and only for what the inventory can still hold, so a plugin that cancels them does not
    // stop a sack
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent event) {
        if (event.isCanceled() || event.getResult() == Event.Result.ALLOW) {
            return;
        }

        Player player = event.getEntity();
        ItemEntity item = event.getItem();
        if (item != null && item.isAlive() && !item.hasPickUpDelay()) {
            ItemStack stack = item.getItem();
            if (!stack.isEmpty()) {
                for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                    if (i != player.getInventory().selected) {

                        ItemStack invStack = player.getInventory().getItem(i);
                        if (!invStack.isEmpty() && invStack.getItem() instanceof Sack sack && Sack.isAutoInsert(invStack)) {
                            boolean changed = false;

                            if (sack.isVoid) {
                                if (Sack.readVoidFilter(invStack).check(stack)) {
                                    stack.setCount(0);
                                    changed = true;
                                }
                            } else {
                                Optional<SackData> optData = SackManager.get().getData(invStack);
                                if (optData.isEmpty()) {
                                    continue;
                                }

                                // SkyTekx: a local deviation from main. The slots of the sack
                                // menu refuse forge:holds_items, so the pickup skips that tag as
                                // well. Nothing in the pack fills the tag yet, so it only holds
                                // what a datapack puts there
                                ItemStackHandlerAA inv = optData.get().getSpecialHandler();
                                if (!stack.is(ActuallyTags.Items.HOLDS_ITEMS) && optData.get().getFilter().check(stack)) {
                                    for (int j = 0; j < inv.getSlots(); j++) {
                                        ItemStack bagStack = inv.getStackInSlot(j);
                                        if (!bagStack.isEmpty()) {
                                            if (ItemUtil.canBeStacked(bagStack, stack)) {
                                                int maxTransfer = Math.min(stack.getCount(), stack.getMaxStackSize() - bagStack.getCount());
                                                if (maxTransfer > 0) {
                                                    inv.setStackInSlot(j, StackUtil.grow(bagStack, maxTransfer));
                                                    stack.shrink(maxTransfer);
                                                    changed = true;
                                                }
                                            }
                                        } else {
                                            inv.setStackInSlot(j, stack.copy());
                                            stack.setCount(0);
                                            changed = true;
                                        }

                                        if (stack.isEmpty()) {
                                            break;
                                        }
                                    }
                                }
                            }

                            if (changed) {
                                event.setResult(Event.Result.ALLOW);
                            }
                        }
                    }

                    if (stack.isEmpty()) {
                        break;
                    }
                }
            }

            item.setItem(stack);
        }
    }

    //TODO Checking Achievements? yeet?
    /*public static void checkAchievements(ItemStack gotten, PlayerEntity player, InitAchievements.Type type){
        if(gotten != null && player != null){
            for(TheAchievements ach : TheAchievements.values()){
                if(ach.type == type){
                    if(ItemUtil.contains(ach.itemsToBeGotten, gotten, true)){
                        ach.get(player);
                    }
                }
            }
        }
    }*/

    //TODO this isnt how this should be done im pretty sure...
    @SubscribeEvent
    public void onEntityDropEvent(LivingDropsEvent event) {
        if (event.getEntity().level() != null && !event.getEntity().level().isClientSide && event.getSource().getEntity() instanceof Player) {
            //Drop Cobwebs from Spiders
            if (ConfigBoolValues.DO_SPIDER_DROPS.isEnabled() && event.getEntity() instanceof Spider) {
                if (event.getEntity().level().random.nextInt(20) <= event.getLootingLevel() * 2) {
                    event.getDrops().add(new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(Blocks.COBWEB, event.getEntity().level().random.nextInt(2 + event.getLootingLevel()) + 1)));
                }
            }
        }
    }

    @SubscribeEvent
    public void onLogInEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if (!event.getEntity().level().isClientSide && event.getEntity() instanceof ServerPlayer player) {
	        PacketHelperServer.syncPlayerData(player, true);
            ActuallyAdditions.LOGGER.info("Sending Player Data to player " + player.getName() + " with UUID " + player.getUUID() + ".");
        }
    }

    //TODO im pretty sure this can be done with normal advancements...
    @SubscribeEvent
    public void onCraftedEvent(PlayerEvent.ItemCraftedEvent event) {
        //checkAchievements(event.crafting, event.player, InitAchievements.Type.CRAFTING);

        if (CommonConfig.Other.GIVE_BOOKLET_ON_FIRST_CRAFT.get()) {
            if (!event.getEntity().level().isClientSide && StackUtil.isValid(event.getCrafting()) && event.getCrafting().getItem() != ActuallyItems.ITEM_BOOKLET.get()) {

                String name = ForgeRegistries.ITEMS.getKey(event.getCrafting().getItem()).toString();
                if (name != null && name.toLowerCase(Locale.ROOT).contains(ActuallyAdditions.MODID)) {
                    PlayerData.PlayerSave save = PlayerData.getDataFromPlayer(event.getEntity());
                    if (save != null && !save.bookGottenAlready) {
                        save.bookGottenAlready = true;
                        WorldData.get(event.getEntity().getCommandSenderWorld()).setDirty();

                        ItemEntity entityItem = new ItemEntity(event.getEntity().level(), event.getEntity().getX(), event.getEntity().getY(), event.getEntity().getZ(), new ItemStack(ActuallyItems.ITEM_BOOKLET.get()));
                        entityItem.setPickUpDelay(0);
                        event.getEntity().level().addFreshEntity(entityItem);
                    }
                }
            }
        }
    }

    //TODO im pretty sure this can be done with normal advancements...
    @SubscribeEvent
    public void onSmeltedEvent(PlayerEvent.ItemSmeltedEvent event) {
        //checkAchievements(event.smelting, event.player, InitAchievements.Type.SMELTING);
    }

    //TODO im pretty sure this can be done with normal advancements...
    @SubscribeEvent
    public void onPickupEvent(EntityItemPickupEvent event) {
        //checkAchievements(event.getItem().getItem(), event.getEntityPlayer(), InitAchievements.Type.PICK_UP);
    }

    // SkyTekx: renaming an Item Tag in an anvil sets its tag at no cost (6f567725, main parses the
    // name with ResourceLocation.tryParse). A name that is not the id of an existing item tag
    // cancels the event, and AnvilMixin clears the result slot before, so the anvil shows no
    // result. AnvilMixin lets the player take a result of cost 0. A local deviation from main: the
    // handler only acts on an empty right slot. The event keeps the material cost 0, and
    // AnvilMenu.onTake empties the whole right slot for that cost, so main deletes any stack that
    // sits there. With an item on the right, vanilla gives an Item Tag no result
    @SubscribeEvent
    public void onAnvilEvent(AnvilUpdateEvent event) {
        if (!event.getRight().isEmpty()) {
            return;
        }
        if (!event.getLeft().isEmpty() && event.getLeft().getItem() instanceof ItemTag && event.getName() != null && !event.getName().isEmpty()) {
            event.setCost(0);
            ResourceLocation tagLocation = event.getName().length() <= ItemTag.MAX_TAG_LENGTH ? ResourceLocation.tryParse(event.getName()) : null;
            if (tagLocation != null && BuiltInRegistries.ITEM.getTag(TagKey.create(Registries.ITEM, tagLocation)).isPresent()) {
                ItemStack stack = event.getLeft().copy();
                ItemTag.setTag(stack, tagLocation);
                event.setOutput(stack);
            } else {
                event.setCanceled(true);
            }
        }
    }
}
