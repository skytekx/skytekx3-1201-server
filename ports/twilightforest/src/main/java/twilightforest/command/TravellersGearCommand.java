package twilightforest.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.modifiers.InsertableTravellersModifier;
import twilightforest.item.travellers_gear.modifiers.TravellersModifiable;
import twilightforest.item.travellers_gear.modifiers.TravellersModifier;

import java.util.Optional;
import java.util.function.Function;

/**
 * {@code /tf travellers_gear add_modifier|remove_modifier <modifier>}: adds a Traveller's modifier to, or removes one
 * from, the piece of gear in the main hand. Backport of 4.8's TravellersGearCommand (d3b7f702a).
 */
public class TravellersGearCommand {

	private static final DynamicCommandExceptionType ERROR_INVALID_MODIFIER = new DynamicCommandExceptionType(modifier -> Component.translatable("commands.tffeature.invalid_modifier", modifier));
	private static final SimpleCommandExceptionType ERROR_NOT_RUN_BY_PLAYER = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.not_player"));
	private static final SimpleCommandExceptionType ERROR_NOT_HOLDING_GEAR = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.not_travellers_gear"));
	private static final SimpleCommandExceptionType ERROR_TOO_MANY_MODIFIERS = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.too_many_modifiers"));
	private static final Function<Component, SimpleCommandExceptionType> ERROR_NO_MODIFIER = component -> new SimpleCommandExceptionType(Component.translatable("commands.tffeature.no_modifier", component));
	private static final Function<Component, SimpleCommandExceptionType> ERROR_HAS_MODIFIER = component -> new SimpleCommandExceptionType(Component.translatable("commands.tffeature.has_modifier", component));
	private static final Function<Component, SimpleCommandExceptionType> ERROR_WRONG_SLOT = component -> new SimpleCommandExceptionType(Component.translatable("commands.tffeature.wrong_modifier_slot", component));
	private static final SimpleCommandExceptionType ERROR_ABILITY = new SimpleCommandExceptionType(Component.translatable("commands.tffeature.ability_modifier"));

	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("travellers_gear").requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(Commands.literal("add_modifier")
				.then(Commands.argument("modifier", ResourceKeyArgument.key(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY))
					.executes(context -> addModifier(context.getSource(), resolveKey(context)))))
			.then(Commands.literal("remove_modifier")
				.then(Commands.argument("modifier", ResourceKeyArgument.key(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY))
					.executes(context -> removeModifier(context.getSource(), resolveKey(context)))));
	}

	// [VanillaCopy] ResourceKeyArgument#resolveKey, which is private in 1.20.1
	private static Holder.Reference<TravellersModifier> resolveKey(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ResourceKey<?> key = context.getArgument("modifier", ResourceKey.class);
		Optional<ResourceKey<TravellersModifier>> modifierKey = key.cast(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY);
		Optional<Holder.Reference<TravellersModifier>> modifier = modifierKey.flatMap(resourceKey -> context.getSource().registryAccess().registry(TravellersModifiersManager.TRAVELLERS_MODIFIERS_KEY).flatMap(registry -> getHolder(registry, resourceKey)));
		return modifier.orElseThrow(() -> ERROR_INVALID_MODIFIER.create(key.location()));
	}

	private static Optional<Holder.Reference<TravellersModifier>> getHolder(Registry<TravellersModifier> registry, ResourceKey<TravellersModifier> key) {
		return registry.getHolder(key);
	}

	private static int addModifier(CommandSourceStack source, Holder.Reference<TravellersModifier> modifier) throws CommandSyntaxException {
		Context ctx = validate(source, modifier);
		if (TravellersModifiersManager.countInsertableModifiers(source.registryAccess(), ctx.stack()) >= ctx.item().getModifierSlots()) throw ERROR_TOO_MANY_MODIFIERS.create();
		if (TravellersModifiersManager.hasTravellersModifier(source.registryAccess(), ctx.stack(), modifier.key())) throw ERROR_HAS_MODIFIER.apply(ctx.modKey()).create();
		if (!modifier.value().group().test(LivingEntity.getEquipmentSlotForItem(ctx.stack()))) throw ERROR_WRONG_SLOT.apply(ctx.modKey()).create();

		TravellersModifiersManager.addModifier(source.registryAccess(), ctx.stack(), modifier.key());
		source.sendSuccess(() -> Component.translatable("commands.tffeature.added_modifier", ctx.modKey(), ctx.stack().getHoverName()), true);
		return Command.SINGLE_SUCCESS;
	}

	private static int removeModifier(CommandSourceStack source, Holder.Reference<TravellersModifier> modifier) throws CommandSyntaxException {
		Context ctx = validate(source, modifier);
		if (!TravellersModifiersManager.hasTravellersModifier(source.registryAccess(), ctx.stack(), modifier.key())) throw ERROR_NO_MODIFIER.apply(ctx.modKey()).create();

		((InsertableTravellersModifier) modifier.value()).removeModifier(ctx.stack());
		source.sendSuccess(() -> Component.translatable("commands.tffeature.removed_modifier", ctx.modKey(), ctx.stack().getHoverName()), true);
		return Command.SINGLE_SUCCESS;
	}

	private static Context validate(CommandSourceStack source, Holder.Reference<TravellersModifier> modifier) throws CommandSyntaxException {
		if (!(source.getEntity() instanceof Player player) || player instanceof FakePlayer) throw ERROR_NOT_RUN_BY_PLAYER.create();
		if (!(player.getMainHandItem().getItem() instanceof TravellersModifiable armor)) throw ERROR_NOT_HOLDING_GEAR.create();
		if (modifier.value().isAbility()) throw ERROR_ABILITY.create();
		Component modKey = TravellersModifiersManager.getModifierTooltipComponent(modifier);
		return new Context(player, player.getMainHandItem(), armor, modKey);
	}

	private record Context(Player player, ItemStack stack, TravellersModifiable item, Component modKey) {}
}
