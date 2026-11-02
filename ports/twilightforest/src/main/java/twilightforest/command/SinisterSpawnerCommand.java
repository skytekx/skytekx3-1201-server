package twilightforest.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ParticleArgument;
import net.minecraft.commands.arguments.ResourceArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.commands.LootCommand;
import net.minecraft.world.level.storage.loot.LootDataType;
import twilightforest.block.entity.spawner.SinisterSpawnerBlockEntity;

public class SinisterSpawnerCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> register(CommandBuildContext buildContext) {
		return Commands.literal("sinister_spawner")
			.requires(cs -> cs.hasPermission(2))
			.then(Commands.literal("add_particle").then(Commands.argument("particle", ParticleArgument.particle(buildContext)).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(SinisterSpawnerCommand::addParticle))))
			.then(Commands.literal("remove_particle").then(Commands.argument("particle", ParticleArgument.particle(buildContext)).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(SinisterSpawnerCommand::removeParticle))))
			// 1.20.1 loot tables are not a registry, so the loot table is a plain id suggested like /loot does
			.then(Commands.literal("set_loot").then(Commands.argument("loot", ResourceLocationArgument.id()).suggests(LootCommand.SUGGEST_LOOT_TABLE).then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(SinisterSpawnerCommand::setLootTable))))
			.then(Commands.literal("clear_loot").then(Commands.argument("pos", BlockPosArgument.blockPos()).executes(SinisterSpawnerCommand::clearLootTable)))
			;
	}

	private static int addParticle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ParticleOptions options = ParticleArgument.getParticle(context, "particle");
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

		if (context.getSource().getLevel().getBlockEntity(pos) instanceof SinisterSpawnerBlockEntity entity) {
			if (entity.addParticle(options, true))
				return 1;
		}

		return 0;
	}

	private static int removeParticle(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		ParticleOptions options = ParticleArgument.getParticle(context, "particle");
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

		if (context.getSource().getLevel().getBlockEntity(pos) instanceof SinisterSpawnerBlockEntity entity) {
			if (entity.removeParticle(options, true))
				return 1;
		}

		return 0;
	}

	private static int setLootTable(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");
		ResourceLocation loot = ResourceLocationArgument.getId(context, "loot");

		// Like 4.8's ResourceOrIdArgument, reject a loot table id that does not exist
		if (context.getSource().getServer().getLootData().getElementOptional(LootDataType.TABLE, loot).isEmpty())
			throw ResourceArgument.ERROR_UNKNOWN_RESOURCE.create(loot, new ResourceLocation("loot_table"));

		if (context.getSource().getLevel().getBlockEntity(pos) instanceof SinisterSpawnerBlockEntity entity)
			if (entity.setLootTable(loot))
				return 1;

		return 0;
	}

	private static int clearLootTable(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		BlockPos pos = BlockPosArgument.getLoadedBlockPos(context, "pos");

		if (context.getSource().getLevel().getBlockEntity(pos) instanceof SinisterSpawnerBlockEntity entity)
			if (entity.setLootTable(null))
				return 1;

		return 0;
	}
}
