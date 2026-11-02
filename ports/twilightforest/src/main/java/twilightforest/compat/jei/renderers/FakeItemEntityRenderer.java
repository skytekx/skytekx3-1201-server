package twilightforest.compat.jei.renderers;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import twilightforest.TwilightForestMod;
import twilightforest.client.EntityRenderingUtil;
import twilightforest.compat.jei.FakeItemEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//the drawing is shared with the EMI plugin through EntityRenderingUtil, like 4.8 does
@SuppressWarnings("deprecation")
public class FakeItemEntityRenderer implements IIngredientRenderer<FakeItemEntity> {

	private final float bobOffs;
	private final int size;

	public FakeItemEntityRenderer(int size) {
		this.bobOffs = RandomSource.create().nextFloat() * (float) Math.PI * 2.0F;
		this.size = size;
	}

	@Override
	public int getWidth() {
		return this.size;
	}

	@Override
	public int getHeight() {
		return this.size;
	}

	@Override
	public void render(GuiGraphics graphics, @Nullable FakeItemEntity item) {
		Level level = Minecraft.getInstance().level;
		if (item != null && level != null) {
			try {
				EntityRenderingUtil.renderItemEntity(graphics, item.stack(), level, this.bobOffs);
			} catch (Exception e) {
				TwilightForestMod.LOGGER.error("Error drawing item in JEI!", e);
			}
		}
	}

	@Override
	public List<Component> getTooltip(FakeItemEntity item, TooltipFlag flag) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(item.stack().getItem().getDescription());
		if (flag.isAdvanced()) {
			tooltip.add(Component.literal(Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(item.stack().getItem())).toString()).withStyle(ChatFormatting.DARK_GRAY));
		}
		return tooltip;
	}
}
