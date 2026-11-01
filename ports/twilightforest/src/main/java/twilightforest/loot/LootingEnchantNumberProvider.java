package twilightforest.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSyntaxException;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParam;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.providers.number.LootNumberProviderType;
import net.minecraft.world.level.storage.loot.providers.number.NumberProvider;
import net.minecraftforge.registries.ForgeRegistries;
import twilightforest.init.TFLoot;

import java.util.Objects;
import java.util.Set;

/**
 * Adds the killer's level of {@code enchantment} (Looting unless set otherwise) to {@code base_value}, so a pool can roll more
 * often when the killing blow is struck with it. Backported from TF 4.8 (f67973125, d0b95a8bf), where the Knight Phantoms use it.
 * <p>
 * Looting is read through {@link LootContext#getLootingModifier()}, like the vanilla looting functions on Forge, so the
 * LootingLevelEvent still applies. The {@code enchantment} field is the 4.8 one; on 1.20.1 it may be left out and defaults to
 * {@code minecraft:looting}.
 */
public record LootingEnchantNumberProvider(Enchantment enchantment, NumberProvider baseValue) implements NumberProvider {

	@Override
	public LootNumberProviderType getType() {
		return TFLoot.LOOTING_ROLLS.get();
	}

	public static LootingEnchantNumberProvider applyLootingLevelTo(NumberProvider baseValue) {
		return new LootingEnchantNumberProvider(Enchantments.MOB_LOOTING, baseValue);
	}

	@Override
	public float getFloat(LootContext context) {
		if (context.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof LivingEntity living) {
			int level = this.enchantment == Enchantments.MOB_LOOTING ? context.getLootingModifier() : EnchantmentHelper.getEnchantmentLevel(this.enchantment, living);
			return level + this.baseValue.getFloat(context);
		}
		return this.baseValue.getFloat(context);
	}

	/**
	 * Get the parameters used by this object.
	 */
	@Override
	public Set<LootContextParam<?>> getReferencedContextParams() {
		return this.baseValue.getReferencedContextParams();
	}

	public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootingEnchantNumberProvider> {

		@Override
		public void serialize(JsonObject json, LootingEnchantNumberProvider provider, JsonSerializationContext context) {
			json.add("base_value", context.serialize(provider.baseValue()));
			json.addProperty("enchantment", Objects.requireNonNull(ForgeRegistries.ENCHANTMENTS.getKey(provider.enchantment())).toString());
		}

		@Override
		public LootingEnchantNumberProvider deserialize(JsonObject json, JsonDeserializationContext context) {
			Enchantment enchantment = Enchantments.MOB_LOOTING;
			if (json.has("enchantment")) {
				ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "enchantment"));
				enchantment = ForgeRegistries.ENCHANTMENTS.getValue(id);
				if (enchantment == null) {
					throw new JsonSyntaxException("Unknown enchantment '" + id + "'");
				}
			}
			return new LootingEnchantNumberProvider(enchantment, GsonHelper.getAsObject(json, "base_value", context, NumberProvider.class));
		}
	}
}
