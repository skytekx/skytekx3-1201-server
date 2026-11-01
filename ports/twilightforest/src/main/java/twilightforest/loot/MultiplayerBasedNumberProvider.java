package twilightforest.loot;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import twilightforest.TFConfig;
import twilightforest.capabilities.multiplayer.MultiplayerInclusivityCapability;
import twilightforest.init.TFLoot;

import java.util.Optional;
import java.util.Set;

/**
 * Rolls {@code default_rolls}, plus {@code per_player_rolls} (negative results count as 0) for every player beyond the first that
 * took part in the fight against the dropping entity, when the {@code multiplayerFightAdjuster} config adds loot.
 * Backported from TF 4.8 (#1987).
 */
public record MultiplayerBasedNumberProvider(NumberProvider rollsPerPlayer, NumberProvider defaultRolls) implements NumberProvider {

	@Override
	public LootNumberProviderType getType() {
		return TFLoot.MULTIPLAYER_ROLLS.get();
	}

	public static MultiplayerBasedNumberProvider rollsForPlayers(NumberProvider rollsPerPlayer, NumberProvider defaultRolls) {
		return new MultiplayerBasedNumberProvider(rollsPerPlayer, defaultRolls);
	}

	@Override
	public float getFloat(LootContext context) {
		if (TFConfig.multiplayerFightAdjuster.adjustsLootRolls()) {
			Optional<MultiplayerInclusivityCapability> data = MultiplayerBasedAdditionLootFunction.getFightData(context);
			if (data.isPresent()) {
				int qualifiedPlayers = data.get().getQualifiedPlayerCount();
				float total = this.defaultRolls.getFloat(context);
				for (int i = 0; i < qualifiedPlayers - 1; i++) {
					total += Math.max(0, this.rollsPerPlayer.getFloat(context));
				}
				return total;
			}
		}
		return this.defaultRolls.getFloat(context);
	}

	/**
	 * Get the parameters used by this object.
	 */
	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Sets.union(this.rollsPerPlayer.getReferencedContextParams(), this.defaultRolls.getReferencedContextParams());
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<MultiplayerBasedNumberProvider> {

		@Override
		public void serialize(JsonObject json, MultiplayerBasedNumberProvider provider, JsonSerializationContext context) {
			json.add("per_player_rolls", context.serialize(provider.rollsPerPlayer()));
			json.add("default_rolls", context.serialize(provider.defaultRolls()));
		}

		@Override
		public MultiplayerBasedNumberProvider deserialize(JsonObject json, JsonDeserializationContext context) {
			return new MultiplayerBasedNumberProvider(
				GsonHelper.getAsObject(json, "per_player_rolls", context, NumberProvider.class),
				GsonHelper.getAsObject(json, "default_rolls", context, NumberProvider.class));
		}
	}
}
