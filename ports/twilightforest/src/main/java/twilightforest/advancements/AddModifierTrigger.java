package twilightforest.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SerializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;

/**
 * Fires when a player crafts a new modifier into a piece of Traveller's Gear. The optional {@code modifier} condition
 * names the Traveller's modifier id that has to be added. Backport of 4.8's AddModifierTrigger.
 */
public class AddModifierTrigger extends SimpleCriterionTrigger<AddModifierTrigger.TriggerInstance> {
	public static final ResourceLocation ID = TwilightForestMod.prefix("add_modifier");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	protected TriggerInstance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext ctx) {
		ResourceLocation modifier = json.has("modifier") ? new ResourceLocation(GsonHelper.getAsString(json, "modifier")) : null;
		return new TriggerInstance(player, modifier);
	}

	public void trigger(ServerPlayer player, ResourceLocation modifier) {
		this.trigger(player, (instance) -> instance.test(modifier));
	}

	public static class TriggerInstance extends AbstractCriterionTriggerInstance {
		@Nullable
		private final ResourceLocation modifier;

		public TriggerInstance(ContextAwarePredicate player, @Nullable ResourceLocation modifier) {
			super(ID, player);
			this.modifier = modifier;
		}

		public static TriggerInstance addedAnyModifier() {
			return new TriggerInstance(ContextAwarePredicate.ANY, null);
		}

		public static TriggerInstance addedModifier(ResourceLocation modifier) {
			return new TriggerInstance(ContextAwarePredicate.ANY, modifier);
		}

		public boolean test(ResourceLocation modifier) {
			return this.modifier == null || this.modifier.equals(modifier);
		}

		@Override
		public JsonObject serializeToJson(SerializationContext ctx) {
			JsonObject object = super.serializeToJson(ctx);
			if (this.modifier != null) {
				object.addProperty("modifier", this.modifier.toString());
			}
			return object;
		}
	}
}
