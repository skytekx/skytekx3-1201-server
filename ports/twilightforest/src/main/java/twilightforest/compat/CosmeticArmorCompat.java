package twilightforest.compat;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import twilightforest.TwilightForestMod;
import twilightforest.events.CharmEvents;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Cosmetic Armor Reworked drops the cosmetic armor slots on death in its own LivingDropsEvent handler.
 * When a Charm of Keeping saved the player's armor, keep the cosmetic armor too by cancelling its CosArmorDeathDrops event.
 * <p>
 * The event class is looked up reflectively so Twilight Forest neither needs Cosmetic Armor Reworked at compile time
 * nor loads any of its classes when it isn't installed.
 */
public class CosmeticArmorCompat {

	private static final String DEATH_DROPS_EVENT = "lain.mods.cos.api.event.CosArmorDeathDrops";

	public static void init() {
		try {
			register(Class.forName(DEATH_DROPS_EVENT).asSubclass(Event.class));
		} catch (ReflectiveOperationException | ClassCastException | LinkageError e) {
			TwilightForestMod.LOGGER.warn("Could not hook into Cosmetic Armor Reworked, cosmetic armor will drop on death even when a Charm of Keeping is used", e);
		}
	}

	private static <T extends Event> void register(Class<T> eventClass) throws ReflectiveOperationException {
		MethodHandle getPlayer = MethodHandles.publicLookup().findVirtual(eventClass, "getEntityPlayer", MethodType.methodType(Player.class));
		MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, eventClass, event -> keepCosmeticArmor(event, getPlayer));
	}

	private static void keepCosmeticArmor(Event event, MethodHandle getPlayer) {
		Player player;
		try {
			player = (Player) getPlayer.invoke(event);
		} catch (Throwable e) {
			TwilightForestMod.LOGGER.debug("Could not read the player from a Cosmetic Armor Reworked death drop event", e);
			return;
		}

		if (player != null && !player.level().isClientSide() && event.isCancelable()) {
			CompoundTag playerData = CharmEvents.getPlayerData(player);
			//same rule as the Curios compat: only a charm of keeping keeps the extra armor slots, not kept_on_death items or a lone casket
			if (playerData.contains(CharmEvents.CONSUMED_CHARM_TAG) && playerData.contains(CharmEvents.CHARM_INV_TAG)) {
				event.setCanceled(true);
			}
		}
	}
}
