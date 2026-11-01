package twilightforest.advancements;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.registries.ForgeRegistries;
import twilightforest.TwilightForestMod;

import org.jetbrains.annotations.Nullable;

public class DrinkFromFlaskTrigger extends SimpleCriterionTrigger<DrinkFromFlaskTrigger.Instance> {

	public static final ResourceLocation ID = TwilightForestMod.prefix("drink_from_flask");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext ctx) {
		Potion potion = null;
		if (json.has("potion")) {
			ResourceLocation resourcelocation = new ResourceLocation(GsonHelper.getAsString(json, "potion"));
			potion = ForgeRegistries.POTIONS.getValue(resourcelocation);
		}
		if(json.has("doses") && GsonHelper.getAsInt(json, "doses") > 4) throw new JsonSyntaxException("DrinkFromFlaskTrigger: can't have more than 4 doses.");
		MinMaxBounds.Ints doses = MinMaxBounds.Ints.fromJson(json.get("doses"));
		MinMaxBounds.Ints seconds = MinMaxBounds.Ints.fromJson(json.get("seconds"));
		return new Instance(player, doses, seconds, potion);
	}

	/**
	 * @param doses   how many doses of {@code potion} the player has drunk in a row
	 * @param seconds whole seconds since the first of those doses
	 */
	public void trigger(ServerPlayer player, int doses, int seconds, Potion potion) {
		this.trigger(player, (instance) -> instance.matches(doses, seconds, potion));
	}

	public static class Instance extends AbstractCriterionTriggerInstance {

		private final MinMaxBounds.Ints doses;
		private final MinMaxBounds.Ints seconds;
		@Nullable
		private final Potion potion;

		public Instance(ContextAwarePredicate player, MinMaxBounds.Ints doses, MinMaxBounds.Ints seconds, @Nullable Potion potion) {
			super(ID, player);
			this.doses = doses;
			this.seconds = seconds;
			this.potion = potion;
		}

		public static Instance drankPotion(int doses, MinMaxBounds.Ints seconds, Potion potion) {
			return new Instance(ContextAwarePredicate.ANY, MinMaxBounds.Ints.exactly(doses), seconds, potion);
		}

		public static Instance drankPotion(MinMaxBounds.Ints doses, MinMaxBounds.Ints seconds, Potion potion) {
			return new Instance(ContextAwarePredicate.ANY, doses, seconds, potion);
		}

		public boolean matches(int doses, int seconds, Potion potion) {
			return this.doses.matches(doses) && this.seconds.matches(seconds) && this.potion != null && this.potion == potion;
		}

		@Override
		public JsonObject serializeToJson(SerializationContext ctx) {
			JsonObject object = super.serializeToJson(ctx);
			object.add("doses", this.doses.serializeToJson());
			if (!this.seconds.isAny()) {
				object.add("seconds", this.seconds.serializeToJson());
			}
			if (this.potion != null) {
				object.addProperty("potion", ForgeRegistries.POTIONS.getKey(this.potion).toString());
			}
			return object;
		}
	}
}
