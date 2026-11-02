package twilightforest.compat.jei.renderers;

import mezz.jei.api.ingredients.IIngredientRenderer;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.TooltipFlag;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import twilightforest.client.EntityRenderingUtil;
import twilightforest.compat.jei.FakeEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

//yoinked from TCon, entity melting rendering looks so good and I couldnt be bothered to write my own system
//the drawing is shared with the EMI plugin through EntityRenderingUtil, like 4.8 does
@SuppressWarnings("deprecation")
public class EntityRenderer implements IIngredientRenderer<FakeEntityType> {

	private final int size;

	public EntityRenderer(int size) {
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
	public void render(GuiGraphics graphics, @Nullable FakeEntityType fakeType) {
		if (fakeType != null) {
			EntityRenderingUtil.renderEntity(graphics, fakeType.type(), this.size);
		}
	}

	@Override
	public List<Component> getTooltip(FakeEntityType type, TooltipFlag flag) {
		List<Component> tooltip = new ArrayList<>();
		tooltip.add(type.type().getDescription());
		if (flag.isAdvanced()) {
			tooltip.add(Component.literal(Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(type.type())).toString()).withStyle(ChatFormatting.DARK_GRAY));
		}
		return tooltip;
	}
}
