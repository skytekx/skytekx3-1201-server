package twilightforest.init;

import com.google.common.base.Suppliers;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.network.TFPacketHandler;
import twilightforest.network.UpdateGamerulePacket;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * [4.8 TFGameRules] Game rules are registered lazily and then on the main thread during common setup, because
 * {@link GameRules#register} writes to an unsynchronized map and mod constructors run in parallel (4.8 e448c154c,
 * 4a9808926).
 */
public class TFGameRules {
	private static final List<Supplier<?>> GAME_RULES = new ArrayList<>();

	public static final Supplier<GameRules.Key<GameRules.BooleanValue>> ENFORCED_PROGRESSION_RULE = register("tfEnforcedProgression",
		GameRules.Category.UPDATES, // Putting it in UPDATES since other world stuff is here
		GameRules.BooleanValue.create(true, (server, enforced) ->
			// sends a packet to every player online when this changes so weather effects update accordingly
			TFPacketHandler.CHANNEL.send(PacketDistributor.ALL.noArg(), new UpdateGamerulePacket(enforced.get()))
		)
	);

	public static final Supplier<GameRules.Key<GameRules.IntegerValue>> RULE_PLAYERS_TF_PORTAL_DEFAULT_DELAY = register("playersTfPortalDefaultDelay",
		GameRules.Category.PLAYER,
		GameRules.IntegerValue.create(60)
	);

	public static final Supplier<GameRules.Key<GameRules.IntegerValue>> RULE_PLAYERS_TF_PORTAL_CREATIVE_DELAY = register("playersTfPortalCreativeDelay",
		GameRules.Category.PLAYER,
		GameRules.IntegerValue.create(1)
	);

	private static <T extends GameRules.Value<T>> Supplier<GameRules.Key<T>> register(String name, GameRules.Category category, GameRules.Type<T> type) {
		Supplier<GameRules.Key<T>> supplier = Suppliers.memoize(() -> GameRules.register(name, category, type));
		GAME_RULES.add(supplier);
		return supplier;
	}

	public static void register(FMLCommonSetupEvent event) {
		// Register our game rules out of parallel mod loading
		event.enqueueWork(() -> GAME_RULES.forEach(Supplier::get));
	}
}
