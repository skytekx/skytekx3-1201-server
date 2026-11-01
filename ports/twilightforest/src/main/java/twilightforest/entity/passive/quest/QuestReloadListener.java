package twilightforest.entity.passive.quest;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.OnDatapackSyncEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.network.SyncQuestsPacket;
import twilightforest.network.TFPacketHandler;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Loads {@code twilight/quests/*.json} from data packs. Only {@code questing_ram} is read for now. The mod's own file is
 * applied first, so the same file under any other namespace (a data pack, KubeJS or another mod) overrides it.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID)
public class QuestReloadListener extends SimpleJsonResourceReloadListener {

	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

	public QuestReloadListener() {
		super(GSON, "twilight/quests");
	}

	@Override
	protected void apply(Map<ResourceLocation, JsonElement> object, ResourceManager resourceManager, ProfilerFiller profiler) {
		List<Map.Entry<ResourceLocation, JsonElement>> entries = new ArrayList<>(object.entrySet());
		entries.sort(Map.Entry.<ResourceLocation, JsonElement>comparingByKey(Comparator.comparing(id -> !TwilightForestMod.ID.equals(id.getNamespace())))
			.thenComparing(Map.Entry.comparingByKey()));

		QuestingRamContext ram = null;
		for (var entry : entries) {
			if (entry.getKey().getPath().equals("questing_ram")) {
				DataResult<QuestingRamContext> result = QuestingRamContext.CODEC.parse(JsonOps.INSTANCE, entry.getValue());
				if (result.result().isPresent()) {
					ram = result.result().get();
					TwilightForestMod.LOGGER.debug("Questing Ram quest set by mod {}", entry.getKey().getNamespace());
				} else {
					TwilightForestMod.LOGGER.error("Couldn't parse Questing Ram quest {}: {}", entry.getKey(), result.error().map(DataResult.PartialResult::message).orElse("unknown error"));
				}
			}
		}

		if (ram == null) {
			TwilightForestMod.LOGGER.error("Questing Ram quest file not found. Defaulting to fallback");
			ram = QuestingRamContext.FALLBACK;
		}
		QuestingRamCurrentContext.setServerContext(ram);
	}

	@SubscribeEvent
	public static void addReloadListener(AddReloadListenerEvent event) {
		event.addListener(new QuestReloadListener());
	}

	// Clients need the quest for the crosshair indicator. Sent on join and after every /reload.
	@SubscribeEvent
	public static void syncQuests(OnDatapackSyncEvent event) {
		SyncQuestsPacket packet = new SyncQuestsPacket(QuestingRamCurrentContext.getServerContext());
		List<ServerPlayer> players = event.getPlayer() != null ? List.of(event.getPlayer()) : event.getPlayerList().getPlayers();
		for (ServerPlayer player : players) {
			if (!(player instanceof FakePlayer)) {
				TFPacketHandler.CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
			}
		}
	}
}
