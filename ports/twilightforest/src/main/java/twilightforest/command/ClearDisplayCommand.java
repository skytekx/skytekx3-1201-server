package twilightforest.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.MinecraftServer;
import twilightforest.util.DisplayUtil;

import java.util.List;

/**
 * /tf structure_util clear_display: removes every display entity spawned by the other structure_util commands.
 */
public class ClearDisplayCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("clear_display").requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.executes(ClearDisplayCommand::clearDisplayPieces);
	}

	private static int clearDisplayPieces(CommandContext<CommandSourceStack> context) {
		MinecraftServer server = context.getSource().getServer();

		// Extremely lazy way of clearing the entities without having to access the level entities with a scope, etc
		List<String> commands = List.of(
			"kill @e[type=minecraft:text_display,tag=" + DisplayUtil.TAG + "]",
			"kill @e[type=minecraft:block_display,tag=" + DisplayUtil.TAG + "]"
		);
		for (String command : commands) {
			server.getCommands().performCommand(server.getCommands().getDispatcher().parse(command, context.getSource()), command);
		}

		return 0;
	}
}
