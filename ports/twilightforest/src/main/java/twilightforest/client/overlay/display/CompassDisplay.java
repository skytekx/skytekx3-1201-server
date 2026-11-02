package twilightforest.client.overlay.display;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.core.GlobalPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CompassItem;
import net.minecraft.world.item.ItemStack;

/**
 * The compass widget: the wearer's block position, or a lodestone compass's target and the distance to it while the
 * wearer is in its dimension. Backport of 4.8's CompassDisplay.
 */
public class CompassDisplay extends SimpleTextDisplay {

	@Override
	public Component getText(ItemStack item, Minecraft minecraft, Gui gui, Player player) {
		// 4.8 reads the lodestone_tracker component; 1.20.1 keeps the target in the compass's NBT
		CompoundTag tag = item.getTag();
		if (tag != null && CompassItem.isLodestoneCompass(item) && tag.getBoolean(CompassItem.TAG_LODESTONE_TRACKED)) {
			GlobalPos target = CompassItem.getLodestonePosition(tag);
			if (target != null && target.dimension().equals(player.level().dimension())) {
				return Component.translatable("travellers_gear.modifier.twilightforest.item_display.compass.lodestone", target.pos().toShortString(), player.blockPosition().distManhattan(target.pos()));
			}
		}

		return Component.literal(player.blockPosition().toShortString());
	}
}
