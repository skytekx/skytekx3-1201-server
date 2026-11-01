package twilightforest.events;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.block.KeepsakeCasketBlock;
import twilightforest.block.entity.KeepsakeCasketBlockEntity;
import twilightforest.compat.curios.CuriosCompat;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.enums.BlockLoggingEnum;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.init.TFStats;
import twilightforest.network.SpawnCharmPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.TFItemStackUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Charm of Life, Charm of Keeping and Keepsake Casket handling.
 * <p>
 * All state that has to survive between the death and the respawn of a player (kept items, the charm that was used,
 * the damage of the consumed casket and whether the casket broke) lives in the player's persisted NBT
 * ({@link #getPlayerData(Player)}), which Forge copies onto the respawned player. Nothing is kept in static fields,
 * so several players dying at once can no longer swap or lose each other's charm state.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class CharmEvents {

	public static final String CHARM_INV_TAG = "TFCharmInventory";
	public static final String CASKET_DAMAGE_TAG = "CasketDamage";
	//stores if the casket was too damaged to hold the player's items, so we can tell them on respawn
	public static final String CASKET_BROKEN_TAG = "CasketBroken";
	//stores the charm of keeping that was used, so the effect can be played on respawn
	public static final String CONSUMED_CHARM_TAG = "CharmStack";

	// Check for charm of life first to stop a player from dying
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void applyCharmOfLife(LivingDeathEvent event) {
		LivingEntity living = event.getEntity();

		//ensure our player is real and in survival before attempting anything
		if (event.isCanceled() || living.level().isClientSide() || !(living instanceof Player player) || living instanceof FakePlayer ||
				player.isCreative() || player.isSpectator()) return;

		if (handleCharmOfLife(player)) event.setCanceled(true); // Executes if the player had charms
	}

	// Then check if the player should keep any items through death.
	// This runs at a lower priority so mods that cancel the death (like PlayerRevive) can do so before we take any items.
	@SubscribeEvent(priority = EventPriority.HIGH)
	public static void applyKeepingAndCasket(LivingDeathEvent event) {
		LivingEntity living = event.getEntity();

		//ensure our player is real and in survival before attempting anything
		if (event.isCanceled() || living.level().isClientSide() || !(living instanceof Player player) || living instanceof FakePlayer ||
				player.isCreative() || player.isSpectator()) return;

		if (!living.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
			// Did the player recover? No? Let's give them their stuff based on the keeping charms
			handleCharmOfKeeping(player);

			// Then let's store the rest of their stuff in the casket
			stockKeepsakeCasket(player);
		}
	}

	@SubscribeEvent
	public static void returnItemsOnRespawn(PlayerEvent.PlayerRespawnEvent event) {
		if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) return;
		if (!event.isEndConquered()) {
			if (getPlayerData(serverPlayer).contains(CASKET_BROKEN_TAG)) {
				serverPlayer.sendSystemMessage(Component.translatable("block.twilightforest.casket.broken").withStyle(ChatFormatting.RED));
				getPlayerData(serverPlayer).remove(CASKET_BROKEN_TAG);
			}
			returnStoredItems(serverPlayer);
		}
	}

	private static boolean handleCharmOfLife(Player player) {
		boolean charm2 = TFItemStackUtils.consumeInventoryItem(player, TFItems.CHARM_OF_LIFE_2.get(), getPlayerData(player), false) || hasCharmCurio(TFItems.CHARM_OF_LIFE_2.get(), player, false);
		boolean charm1 = !charm2 && (TFItemStackUtils.consumeInventoryItem(player, TFItems.CHARM_OF_LIFE_1.get(), getPlayerData(player), false) || hasCharmCurio(TFItems.CHARM_OF_LIFE_1.get(), player, false));

		if (charm2 || charm1) {
			if (charm1) {
				player.setHealth(8);
				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100, 0));
			}

			if (charm2) {
				player.setHealth(player.getMaxHealth());

				player.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 600, 3));
				player.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0));
				player.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 600, 0));
			}

			if (player instanceof ServerPlayer serverPlayer) {
				sendCharmEffect(serverPlayer, new ItemStack(charm1 ? TFItems.CHARM_OF_LIFE_1.get() : TFItems.CHARM_OF_LIFE_2.get()), TFSounds.CHARM_LIFE.getKey());
				serverPlayer.awardStat(TFStats.LIFE_CHARMS_ACTIVATED.get());
			}

			return true;
		}

		return false;
	}

	private static void handleCharmOfKeeping(Player player) {
		//create a fake inventory to organize our kept inventory in
		Inventory keepInventory = new Inventory(player);
		ListTag tagList = new ListTag();

		//tier 3 keeps our entire inventory, tier 2 our hotbar, tier 1 our selected item. All of them keep armor and offhand.
		if (!applyCharm(TFItems.CHARM_OF_KEEPING_3.get(), keepInventory, player, 0, player.getInventory().items.size())) {
			if (!applyCharm(TFItems.CHARM_OF_KEEPING_2.get(), keepInventory, player, 0, Inventory.getSelectionSize())) {
				int i = player.getInventory().selected;
				if (Inventory.isHotbarSlot(i)) {
					applyCharm(TFItems.CHARM_OF_KEEPING_1.get(), keepInventory, player, i, i + 1);
				}
			}
		}

		//keep all items in the kept_on_death tag. This allows modpacks to support other items to keep on death
		for (int i = 0; i < player.getInventory().items.size(); i++) {
			ItemStack stack = player.getInventory().items.get(i);
			if (stack.is(ItemTagGenerator.KEPT_ON_DEATH)) {
				keepInventory.items.set(i, stack.copy());
				player.getInventory().items.set(i, ItemStack.EMPTY);
			}
		}

		for (int i = 0; i < player.getInventory().armor.size(); i++) {
			ItemStack armor = player.getInventory().armor.get(i);
			if (armor.is(ItemTagGenerator.KEPT_ON_DEATH)) {
				keepInventory.armor.set(i, armor.copy());
				player.getInventory().armor.set(i, ItemStack.EMPTY);
			}
		}

		if (player.getInventory().offhand.get(0).is(ItemTagGenerator.KEPT_ON_DEATH)) {
			keepInventory.offhand.set(0, player.getInventory().offhand.get(0).copy());
			player.getInventory().offhand.set(0, ItemStack.EMPTY);
		}

		//take our fake inventory and save it to the persistent player data.
		//by saving it there we can guarantee we will always get all of our items back, even if the player logs out and back in.
		if (!keepInventory.isEmpty()) {
			keepInventory.save(tagList);
			getPlayerData(player).put(CHARM_INV_TAG, tagList);
		}
	}

	/**
	 * Tries to use the given charm of keeping on the inventory slots [fromSlot, toSlot) plus armor and offhand.
	 * The charm is only consumed if any of the slots it would protect actually hold an item.
	 *
	 * @return true if the charm was found and consumed
	 */
	private static boolean applyCharm(Item charm, Inventory keptInventory, Player player, int fromSlot, int toSlot) {
		//views on the real slots, so moving items out of them empties the player's inventory
		List<ItemStack> inventorySlots = player.getInventory().items.subList(fromSlot, toSlot);
		List<ItemStack> mergedCheck = new ArrayList<>(inventorySlots);
		//merge armor and offhand into check slots since theyll always be kept by a charm
		mergedCheck.addAll(player.getInventory().armor);
		mergedCheck.addAll(player.getInventory().offhand);
		//first, check all affected slots to make sure they arent empty.
		//filter out the charm so it doesnt count towards keeping items if its the only thing we are holding
		if (mergedCheck.stream().filter(stack -> !stack.is(charm)).allMatch(ItemStack::isEmpty)) return false;

		//do we even have a charm? No? Then stop operation
		if (!TFItemStackUtils.consumeInventoryItem(player, charm, getPlayerData(player), true) && !hasCharmCurio(charm, player, true)) return false;

		//unless we keep everything, leave one keepsake casket behind so it can hold the items we didn't keep
		boolean keptACasket = keepWholeListAndCheckCasket(keptInventory.items.subList(fromSlot, toSlot), inventorySlots, charm == TFItems.CHARM_OF_KEEPING_3.get());
		keptACasket = keepWholeListAndCheckCasket(keptInventory.armor, player.getInventory().armor, keptACasket);
		keepWholeListAndCheckCasket(keptInventory.offhand, player.getInventory().offhand, keptACasket);

		return true;
	}

	private static void stockKeepsakeCasket(Player player) {
		//make sure we are still actually holding onto items before trying to place a casket
		if (player.getInventory().hasAnyMatching(stack -> !stack.isEmpty() && !stack.is(TFBlocks.KEEPSAKE_CASKET.get().asItem()))) {
			boolean casketConsumed = TFItemStackUtils.consumeInventoryItem(player, TFBlocks.KEEPSAKE_CASKET.get().asItem(), getPlayerData(player), false);

			//read the damage of the casket we just consumed and clear it right away, so a failed placement can't leak it into the next death
			int damage = getPlayerData(player).contains(CASKET_DAMAGE_TAG) ? getPlayerData(player).getInt(CASKET_DAMAGE_TAG) : 0;
			getPlayerData(player).remove(CASKET_DAMAGE_TAG);

			if (!casketConsumed)
				return;

			Level level = player.level();
			BlockPos.MutableBlockPos pos = player.blockPosition().mutable();

			if (pos.getY() < level.dimensionType().minY() + 2) {
				pos.setY(level.dimensionType().minY() + 2);
			} else {
				int logicalHeight = player.level().dimensionType().logicalHeight();

				if (pos.getY() > logicalHeight) {
					pos.setY(logicalHeight - 1);
				}
			}

			pos.move(0, -1, 0);

			do {
				pos.move(0, 1, 0);
			} while (!level.getBlockState(pos).canBeReplaced());

			BlockPos immutablePos = pos.immutable();
			FluidState fluidState = level.getFluidState(immutablePos);

			damage = Math.max(0, Math.min(2, damage));
			if (player.getRandom().nextFloat() <= 0.15F) {
				if (damage >= 2) {
					//the casket is too damaged to hold anything: it breaks, and the remaining items drop as usual
					getPlayerData(player).putBoolean(CASKET_BROKEN_TAG, true);
					TwilightForestMod.LOGGER.debug("{}'s Casket damage value was too high, alerting the player and dropping extra items", player.getName().getString());
					return;
				} else {
					damage = damage + 1;
					TwilightForestMod.LOGGER.debug("{}'s Casket was randomly damaged, applying new damage", player.getName().getString());
				}
			}

			BlockState setState = TFBlocks.KEEPSAKE_CASKET.get().defaultBlockState()
					.setValue(BlockLoggingEnum.MULTILOGGED, BlockLoggingEnum.getFromFluid(fluidState.getType()))
					.setValue(KeepsakeCasketBlock.BREAKAGE, damage)
					.setValue(KeepsakeCasketBlock.FACING, Direction.from2DDataValue(level.getRandom().nextInt(3)));

			if (!level.setBlockAndUpdate(immutablePos, setState)) {
				TwilightForestMod.LOGGER.error("Could not place Keepsake Casket at {}", pos);
				return;
			}

			if (!(level.getBlockEntity(immutablePos) instanceof KeepsakeCasketBlockEntity casket)) {
				TwilightForestMod.LOGGER.error("Failed to set Keepsake Casket data at {}", pos);
				return;
			}

			if (TFConfig.COMMON_CONFIG.casketUUIDLocking.get()) {
				//make it so only the player who died can open the chest if our config allows us
				casket.playeruuid = player.getGameProfile().getId();
			} else {
				casket.playeruuid = null;
			}

			//some names are way too long for the casket so we'll cut them down
			String modifiedName = player.getName().getString().substring(0, Math.min(12, player.getName().getString().length()));
			casket.name = player.getName().getString();
			casket.casketname = modifiedName;
			casket.setCustomName(Component.literal(modifiedName + "'s " + (level.getRandom().nextInt(1000) == 0 ? "Costco Casket" : casket.getDisplayName().getString())));

			int casketCapacity = casket.getContainerSize();
			List<ItemStack> list = new ArrayList<>(casketCapacity);
			NonNullList<ItemStack> filler = NonNullList.withSize(4, ItemStack.EMPTY);

			// lets add our inventory exactly how it was on us
			list.addAll(TFItemStackUtils.sortArmorForCasket(player));
			player.getInventory().armor.clear();
			list.addAll(filler);
			list.addAll(player.getInventory().offhand);
			player.getInventory().offhand.clear();
			list.addAll(TFItemStackUtils.sortInvForCasket(player));
			player.getInventory().items.clear();

			casket.setItems(NonNullList.of(ItemStack.EMPTY, list.toArray(new ItemStack[casketCapacity])));
		} else {
			//inventory is empty minus the casket: put the casket into the kept inventory
			for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
				if (player.getInventory().getItem(i).is(TFBlocks.KEEPSAKE_CASKET.get().asItem())) {
					Inventory tmp = new Inventory(player);
					tmp.load(getPlayerData(player).getList(CHARM_INV_TAG, 10));
					tmp.add(player.getInventory().getItem(i).copy());
					player.getInventory().setItem(i, ItemStack.EMPTY);
					getPlayerData(player).put(CHARM_INV_TAG, tmp.save(new ListTag()));
				}
			}
		}
	}

	/**
	 * Maybe we kept some stuff for the player!
	 */
	private static void returnStoredItems(ServerPlayer player) {

		TwilightForestMod.LOGGER.debug("Player {} ({}) respawned and received items held in storage", player.getName().getString(), player.getUUID());

		//check if our tag is in the persistent player data. If so, copy that inventory over to our own. Cloud storage at its finest!
		CompoundTag playerData = getPlayerData(player);
		if (playerData.contains(CHARM_INV_TAG)) {
			ListTag tagList = playerData.getList(CHARM_INV_TAG, 10);
			TFItemStackUtils.loadNoClear(tagList, player.getInventory());
			playerData.remove(CHARM_INV_TAG);
		}

		// spawn effect thingers
		if (playerData.contains(CONSUMED_CHARM_TAG)) {
			ItemStack stack = ItemStack.of(playerData.getCompound(CONSUMED_CHARM_TAG));

			sendCharmEffect(player, stack, TFSounds.CHARM_KEEP.getKey());
			player.awardStat(TFStats.KEEPING_CHARMS_ACTIVATED.get());
			playerData.remove(CONSUMED_CHARM_TAG);
		}
	}

	//the effect is played on the client of the player that used the charm, either as our orbiting charm or as a totem pop
	private static void sendCharmEffect(ServerPlayer player, ItemStack charm, @Nullable ResourceKey<SoundEvent> sound) {
		if (sound != null) {
			TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new SpawnCharmPacket(charm, sound));
		}
	}

	public static CompoundTag getPlayerData(Player player) {
		if (!player.getPersistentData().contains(Player.PERSISTED_NBT_TAG)) {
			player.getPersistentData().put(Player.PERSISTED_NBT_TAG, new CompoundTag());
		}
		return player.getPersistentData().getCompound(Player.PERSISTED_NBT_TAG);
	}

	/**
	 * Transfers a list of items to another.
	 * Unless skipCasketCheck is set, the first keepsake casket found stays in transferFrom (one item of it, if stacked)
	 * so it can still be placed to hold the items that weren't kept.
	 *
	 * @return true if a casket was left behind, or if skipCasketCheck was set
	 */
	private static boolean keepWholeListAndCheckCasket(List<ItemStack> transferTo, List<ItemStack> transferFrom, boolean skipCasketCheck) {
		boolean keptCasket = false;
		for (int i = 0; i < transferFrom.size(); i++) {
			ItemStack item = transferFrom.get(i).copy();
			if (skipCasketCheck || (!item.is(TFBlocks.KEEPSAKE_CASKET.get().asItem()) || keptCasket)) {
				transferTo.set(i, item);
				transferFrom.set(i, ItemStack.EMPTY);
			} else {
				keptCasket = true;
				if (item.getCount() > 1) {
					item.shrink(1);
					transferTo.set(i, item);
					transferFrom.set(i, item.copyWithCount(1));
				}
			}
		}
		return keptCasket || skipCasketCheck;
	}

	private static boolean hasCharmCurio(Item item, Player player, boolean saveItemToTag) {
		if (ModList.get().isLoaded("curios")) {
			return CuriosCompat.findAndConsumeCurio(item, player, saveItemToTag);
		}

		return false;
	}
}
