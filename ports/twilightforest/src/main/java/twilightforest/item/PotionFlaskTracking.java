package twilightforest.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.ForgeRegistries;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.TFAdvancements;

/**
 * Per-player potion flask tracking for the "Full Mettle Alchemist" challenge. It records which potion a player last drank from a flask,
 * how many doses of it they drank in a row, and when the first of those doses was taken.
 * <p>
 * Backport of 4.8's {@code PotionFlaskTrackingAttachment}. 1.20.1 has no data attachments, so the state lives in the player's persistent
 * (ForgeData) NBT. Like the 4.8 attachment, it is saved with the player but not copied over on respawn.
 */
public final class PotionFlaskTracking {

	private static final String KEY = TwilightForestMod.prefix("flask_doses").toString();
	private static final String LAST_USED_POTION = "last_used_potion";
	private static final String DOSES_DRANK = "doses_drank";
	private static final String LAST_TIME_STARTED = "last_game_time_started";

	private PotionFlaskTracking() {
	}

	/**
	 * Counts one dose of {@code potion} towards the player's streak and fires the drink_from_flask trigger with the dose count and the
	 * whole seconds elapsed since the streak began. Drinking a different potion restarts the streak.
	 */
	public static void trackDrink(ServerPlayer player, Potion potion) {
		ResourceLocation potionId = ForgeRegistries.POTIONS.getKey(potion);
		if (potionId == null) return;

		CompoundTag tag = player.getPersistentData().getCompound(KEY);
		long gameTime = player.level().getGameTime();
		int doses;
		long timeStarted;
		//reset advancement window if potion changed. Otherwise, continue incrementing the doses
		if (!tag.contains(LAST_USED_POTION) || !tag.getString(LAST_USED_POTION).equals(potionId.toString())) {
			doses = 1;
			timeStarted = gameTime;
		} else {
			doses = tag.getInt(DOSES_DRANK) + 1;
			timeStarted = tag.getLong(LAST_TIME_STARTED);
		}

		tag.putString(LAST_USED_POTION, potionId.toString());
		tag.putInt(DOSES_DRANK, doses);
		tag.putLong(LAST_TIME_STARTED, timeStarted);
		player.getPersistentData().put(KEY, tag);

		if (player.isAlive()) {
			TFAdvancements.DRINK_FROM_FLASK.trigger(player, doses, Mth.floor((float) (gameTime - timeStarted) / 20L), potion);
		}
	}

	/**
	 * Clears the player's streak, so a flask advancement that was just earned starts from zero next time.
	 */
	public static void resetDoses(Player player) {
		player.getPersistentData().remove(KEY);
	}
}
