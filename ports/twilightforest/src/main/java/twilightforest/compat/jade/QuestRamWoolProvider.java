package twilightforest.compat.jade;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.Vec2;
import snownee.jade.api.EntityAccessor;
import snownee.jade.api.IEntityComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;
import twilightforest.TwilightForestMod;
import twilightforest.entity.passive.QuestRam;
import twilightforest.entity.passive.quest.ram.QuestingRamContext;
import twilightforest.entity.passive.quest.ram.QuestingRamCurrentContext;
import twilightforest.util.ColorUtil;

import java.util.Map;

public enum QuestRamWoolProvider implements IEntityComponentProvider {

	INSTANCE;

	@Override
	public void appendTooltip(ITooltip tooltip, EntityAccessor entityAccessor, IPluginConfig pluginConfig) {
		if (entityAccessor.getEntity() instanceof QuestRam ram) {
			// show what the (data pack driven) quest still asks for, in the order the ram displays its colours
			QuestingRamContext quest = QuestingRamCurrentContext.getClientContext();
			int getRenderedWools = 0;
			for (Map.Entry<DyeColor, Block> entry : ColorUtil.WOOL_TO_DYE_IN_RAM_ORDER.entrySet()) {
				if (!ram.isColorPresent(entry.getKey())) {
					ItemStack stack = quest.getDisplayStack(entry.getKey(), entry.getValue());
					if (getRenderedWools % 8 == 0) {
						tooltip.add(IElementHelper.get().smallItem(stack).size(new Vec2(4.0F, 4.0F)));
					} else {
						tooltip.append(IElementHelper.get().smallItem(stack).size(new Vec2(4.0F, 4.0F)));
					}
					tooltip.append(Component.literal(" "));
					getRenderedWools++;
				}
			}
		}
	}

	@Override
	public ResourceLocation getUid() {
		return TwilightForestMod.prefix("quest_ram_wool");
	}
}
