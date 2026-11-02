package twilightforest.loot;

import com.google.common.collect.Sets;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import net.minecraft.util.GsonHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.capabilities.CapabilityList;
import twilightforest.capabilities.multiplayer.MultiplayerInclusivityCapability;
import twilightforest.init.TFLoot;

import java.util.Optional;
import java.util.Set;

/**
 * Adds {@code extra_count_per_player} items for every player beyond the first that took part in the fight against the dropping
 * entity, when the {@code multiplayerFightAdjuster} config adds loot. Backported from TF 4.8 (#1987).
 */
public class MultiplayerBasedAdditionLootFunction extends LootItemConditionalFunction {

	private final NumberProvider value;

	public MultiplayerBasedAdditionLootFunction(LootItemCondition[] predicates, NumberProvider value) {
		super(predicates);
		this.value = value;
	}

	public static MultiplayerBasedAdditionLootFunction.Builder addForAllParticipatingPlayers(NumberProvider additionPerPlayer) {
		return new MultiplayerBasedAdditionLootFunction.Builder(additionPerPlayer);
	}

	/**
	 * The fight data of the entity being looted, if it is one the multiplayer fight adjuster tracks.
	 */
	static Optional<MultiplayerInclusivityCapability> getFightData(LootContext context) {
		Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
		return entity != null ? entity.getCapability(CapabilityList.MULTIPLAYER_FIGHT).resolve() : Optional.empty();
	}

	@Override
	public LootItemFunctionType getType() {
		return TFLoot.MULTIPLAYER_MULTIPLIER.get();
	}

	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return Sets.union(super.getReferencedContextParams(), this.value.getReferencedContextParams());
	}

	@Override
	protected ItemStack run(ItemStack stack, LootContext context) {
		if (TFConfig.multiplayerFightAdjuster.adjustsLootRolls()) {
			Optional<MultiplayerInclusivityCapability> data = getFightData(context);
			if (data.isPresent()) {
				int qualifiedPlayers = data.get().getQualifiedPlayerCount();
				if (qualifiedPlayers > 1) {
					int participatingPlayers = qualifiedPlayers - 1;
					// 4.8 lets a negative roll take items away, so the Minoshroom's single axe (-2 to 1 per player) could drop to a
					// count of 0 and vanish in group fights. This function only ever adds.
					int extraItems = Math.max(0, this.value.getInt(context)) * participatingPlayers;
					stack.setCount(Mth.clamp(stack.getCount() + extraItems, 0, stack.getMaxStackSize()));
					TwilightForestMod.LOGGER.debug("{} extra players participated in a fight against {}, dropping {} extra {} for a total of {}.", participatingPlayers, context.getParam(LootContextParams.THIS_ENTITY).getType().getDescription().getString(), extraItems, stack.getItem().getDescription().getString(), stack.getCount());
				}
			}
		}
		return stack;
	}

	public static class Builder extends LootItemConditionalFunction.Builder<MultiplayerBasedAdditionLootFunction.Builder> {
		private final NumberProvider count;

		public Builder(NumberProvider additionPerPlayer) {
			this.count = additionPerPlayer;
		}

		@Override
		protected MultiplayerBasedAdditionLootFunction.Builder getThis() {
			return this;
		}

		@Override
		public LootItemFunction build() {
			return new MultiplayerBasedAdditionLootFunction(this.getConditions(), this.count);
		}
	}

	public static class Serializer extends LootItemConditionalFunction.Serializer<MultiplayerBasedAdditionLootFunction> {

		@Override
		public void serialize(JsonObject json, MultiplayerBasedAdditionLootFunction function, JsonSerializationContext context) {
			super.serialize(json, function, context);
			json.add("extra_count_per_player", context.serialize(function.value));
		}

		@Override
		public MultiplayerBasedAdditionLootFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
			return new MultiplayerBasedAdditionLootFunction(conditions, GsonHelper.getAsObject(json, "extra_count_per_player", context, NumberProvider.class));
		}
	}
}
