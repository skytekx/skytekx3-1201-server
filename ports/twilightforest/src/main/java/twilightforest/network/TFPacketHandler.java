package twilightforest.network;

import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;
import twilightforest.TwilightForestMod;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

public class TFPacketHandler {
	// Bump this number every time theres a breaking change, to ensure people dont mess things up when joining on the wrong version
	// SkyTekx backport: the "-skytekx.N" suffix keeps stock TF 4.3 clients (protocol "2") out at the handshake instead of letting
	// them fail to decode the changed packets later. Bump N (2-skytekx.2, ...) whenever a backported packet changes its wire format.
	// .1: UpdateTFMultipartPacket is keyed by part entity id and always carries the part's synched data.
	// .2: SpawnCharmPacket is registered after ParticlePacket, which shifts the discriminator of every later packet.
	// .3: SyncUncraftingTableConfigPacket carries disableIngredientSwitching after allowShapeless (4.8 f5ef5a835).
	// .4: UpdateDeathTimePacket syncs a dying boss's deathTime to players that start tracking it (4.8 5b63f08d4).
	// .5: AddTFBossBarPacket and UpdateTFBossBarStylePacket carry the RGB boss bars (4.8 15ab0d12a).
	private static final String PROTOCOL_VERSION = "2-skytekx.5";
	public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
			TwilightForestMod.prefix("channel"),
			() -> PROTOCOL_VERSION,
			PROTOCOL_VERSION::equals,
			PROTOCOL_VERSION::equals
	);

	@SuppressWarnings("UnusedAssignment")
	public static void init() {
		int id = 0;
		CHANNEL.registerMessage(id++, AreaProtectionPacket.class, AreaProtectionPacket::encode, AreaProtectionPacket::new, AreaProtectionPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, CreateMovingCicadaSoundPacket.class, CreateMovingCicadaSoundPacket::encode, CreateMovingCicadaSoundPacket::new, CreateMovingCicadaSoundPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, EnforceProgressionStatusPacket.class, EnforceProgressionStatusPacket::encode, EnforceProgressionStatusPacket::new, EnforceProgressionStatusPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, MagicMapPacket.class, MagicMapPacket::encode, MagicMapPacket::new, MagicMapPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, MazeMapPacket.class, MazeMapPacket::encode, MazeMapPacket::new, MazeMapPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, MissingAdvancementToastPacket.class, MissingAdvancementToastPacket::encode, MissingAdvancementToastPacket::new, MissingAdvancementToastPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, ParticlePacket.class, ParticlePacket::encode, ParticlePacket::new, ParticlePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SpawnCharmPacket.class, SpawnCharmPacket::encode, SpawnCharmPacket::new, SpawnCharmPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, SpawnFallenLeafFromPacket.class, SpawnFallenLeafFromPacket::encode, SpawnFallenLeafFromPacket::new, SpawnFallenLeafFromPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, StructureProtectionClearPacket.class, StructureProtectionClearPacket::encode, StructureProtectionClearPacket::new, StructureProtectionClearPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, StructureProtectionPacket.class, StructureProtectionPacket::encode, StructureProtectionPacket::new, StructureProtectionPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, SyncUncraftingTableConfigPacket.class, SyncUncraftingTableConfigPacket::encode, SyncUncraftingTableConfigPacket::new, SyncUncraftingTableConfigPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, ThrowPlayerPacket.class, ThrowPlayerPacket::encode, ThrowPlayerPacket::new, ThrowPlayerPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UncraftingGuiPacket.class, UncraftingGuiPacket::encode, UncraftingGuiPacket::new, UncraftingGuiPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateFeatherFanFallPacket.class, UpdateFeatherFanFallPacket::encode, UpdateFeatherFanFallPacket::new, UpdateFeatherFanFallPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateGamerulePacket.class, UpdateGamerulePacket::encode, UpdateGamerulePacket::new, UpdateGamerulePacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateShieldPacket.class, UpdateShieldPacket::encode, UpdateShieldPacket::new, UpdateShieldPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateTFMultipartPacket.class, UpdateTFMultipartPacket::encode, UpdateTFMultipartPacket::new, UpdateTFMultipartPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, UpdateThrownPacket.class, UpdateThrownPacket::encode, UpdateThrownPacket::new, UpdateThrownPacket.Handler::onMessage);
		CHANNEL.registerMessage(id++, UpdateDeathTimePacket.class, UpdateDeathTimePacket::encode, UpdateDeathTimePacket::new, UpdateDeathTimePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, TFBossBarPacket.AddTFBossBarPacket.class, TFBossBarPacket.AddTFBossBarPacket::encode, TFBossBarPacket.AddTFBossBarPacket::new, TFBossBarPacket.AddTFBossBarPacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
		CHANNEL.registerMessage(id++, TFBossBarPacket.UpdateTFBossBarStylePacket.class, TFBossBarPacket.UpdateTFBossBarStylePacket::encode, TFBossBarPacket.UpdateTFBossBarStylePacket::new, TFBossBarPacket.UpdateTFBossBarStylePacket.Handler::onMessage, Optional.of(NetworkDirection.PLAY_TO_CLIENT));
	}
}
