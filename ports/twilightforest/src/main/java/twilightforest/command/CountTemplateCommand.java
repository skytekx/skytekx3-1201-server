package twilightforest.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceKeyArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;

import java.util.Comparator;
import java.util.List;

/**
 * /tf structure_util count_template &lt;structure&gt;: lists how often each structure template is used by the template pieces of
 * the structure under the executor.
 */
public class CountTemplateCommand {
	public static LiteralArgumentBuilder<CommandSourceStack> register() {
		return Commands.literal("count_template").requires(cs -> cs.hasPermission(Commands.LEVEL_GAMEMASTERS))
			.then(Commands.argument("filter_structure", ResourceKeyArgument.key(Registries.STRUCTURE)).executes(CountTemplateCommand::countTemplates));
	}

	private static int countTemplates(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
		Holder.Reference<Structure> structure = ResourceKeyArgument.getStructure(context, "filter_structure");

		if (!structure.isBound()) return 0;

		CommandSourceStack source = context.getSource();
		ServerLevel level = source.getLevel();
		BlockPos commandPos = BlockPos.containing(source.getPosition());

		StructureStart structureAt = level.structureManager().getStructureAt(commandPos, structure.value());

		Object2IntMap<ResourceLocation> templateCounts = new Object2IntOpenHashMap<>();

		List<StructurePiece> structurePieces = structureAt.getPieces();
		for (StructurePiece piece : structurePieces) {
			if (piece instanceof TemplateStructurePiece templatePiece) {
				ResourceLocation resourceLocation = templatePiece.makeTemplateLocation();
				templateCounts.put(resourceLocation, templateCounts.getOrDefault(resourceLocation, 0) + 1);
			}
		}

		Comparator<Object2IntMap.Entry<ResourceLocation>> byKey = Comparator.comparing(Object2IntMap.Entry::getKey);
		Comparator<Object2IntMap.Entry<ResourceLocation>> byCount = Comparator.comparingInt(Object2IntMap.Entry::getIntValue);
		for (Object2IntMap.Entry<ResourceLocation> countedTemplate : templateCounts.object2IntEntrySet().stream().sorted(byKey).sorted(byCount).toList()) {
			MutableComponent text = Component.literal(countedTemplate.getKey() + "    " + countedTemplate.getIntValue());
			source.sendSystemMessage(text);
		}

		return templateCounts.size();
	}
}
