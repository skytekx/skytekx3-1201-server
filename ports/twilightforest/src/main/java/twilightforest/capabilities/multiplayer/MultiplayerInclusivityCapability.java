package twilightforest.capabilities.multiplayer;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.INBTSerializable;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.advancements.TFAdvancements;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Every player that took part in a fight against an entity in {@code #twilightforest:multiplayer_inclusive_entities}.
 * Backported from TF 4.8's MultiplayerInclusivityAttachment (#1987), as a Forge capability that only those entity types get.
 * <p>
 * 4.8 keeps ServerPlayer references in memory only. This keeps player UUIDs and saves them with the entity, so a player who dies
 * and respawns mid-fight (a new ServerPlayer on 1.20.1) is still counted once, and the group survives the boss's chunk being
 * unloaded while everyone runs back from their spawn point.
 */
public class MultiplayerInclusivityCapability implements INBTSerializable<CompoundTag> {

	public static final ResourceLocation ID = TwilightForestMod.prefix("multiplayer_fight");

	private final Set<UUID> qualifiedPlayers = new LinkedHashSet<>();

	public Set<UUID> getQualifiedPlayers() {
		return Collections.unmodifiableSet(this.qualifiedPlayers);
	}

	public int getQualifiedPlayerCount() {
		return this.qualifiedPlayers.size();
	}

	public void maybeAddQualifiedPlayer(@Nullable Entity entity) {
		// FakePlayers (deployers, turrets and the like) are not part of the group
		if (entity instanceof ServerPlayer player && !(player instanceof FakePlayer)) {
			this.qualifiedPlayers.add(player.getUUID());
		}
	}

	/**
	 * Credits every qualified player that is online with the {@code twilightforest:hurt_boss} trigger.
	 */
	public void grantGroupAdvancement(LivingEntity boss) {
		MinecraftServer server = boss.getServer();
		if (server == null) return;
		for (UUID uuid : this.qualifiedPlayers) {
			ServerPlayer player = server.getPlayerList().getPlayer(uuid);
			if (player != null) {
				TFAdvancements.HURT_BOSS.trigger(player, boss);
			}
		}
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		ListTag players = new ListTag();
		for (UUID uuid : this.qualifiedPlayers) {
			players.add(NbtUtils.createUUID(uuid));
		}
		tag.put("QualifiedPlayers", players);
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		this.qualifiedPlayers.clear();
		for (Tag player : tag.getList("QualifiedPlayers", Tag.TAG_INT_ARRAY)) {
			this.qualifiedPlayers.add(NbtUtils.loadUUID(player));
		}
	}
}
