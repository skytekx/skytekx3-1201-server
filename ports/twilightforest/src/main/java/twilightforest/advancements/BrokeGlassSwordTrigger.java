package twilightforest.advancements;

import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import twilightforest.TwilightForestMod;

/**
 * Fired by {@link twilightforest.item.GlassSwordItem} when a player actually shatters a Glass Sword.
 * Replaces the old item_durability_changed criterion, which only matched an exact durability delta of -1
 * and therefore never fired when Unbreaking (or another mod) changed how much damage the sword took.
 */
public class BrokeGlassSwordTrigger extends SimpleCriterionTrigger<BrokeGlassSwordTrigger.Instance> {

	public static final ResourceLocation ID = TwilightForestMod.prefix("broke_glass_sword");

	@Override
	public ResourceLocation getId() {
		return ID;
	}

	@Override
	protected Instance createInstance(JsonObject json, ContextAwarePredicate player, DeserializationContext ctx) {
		return new Instance(player);
	}

	public void trigger(ServerPlayer player) {
		this.trigger(player, (instance) -> true);
	}

	public static class Instance extends AbstractCriterionTriggerInstance {
		public Instance(ContextAwarePredicate player) {
			super(BrokeGlassSwordTrigger.ID, player);
		}

		public static Instance brokeSword() {
			return new Instance(ContextAwarePredicate.ANY);
		}
	}
}
