package twilightforest.compat.emi.recipes;

import dev.emi.emi.EmiUtil;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.render.EmiTooltipComponents;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import dev.emi.emi.recipe.EmiSmithingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.SmithingRecipe;

import java.util.Objects;
import java.util.Random;

/**
 * Smithing recipes without a template, like the Emperor's Cloth removal (twilightforest:no_template_smithing).
 */
public class EmiNoSmithingTemplateRecipe extends EmiSmithingRecipe {
	private final SmithingRecipe recipe;
	private final int uniq;

	public EmiNoSmithingTemplateRecipe(EmiIngredient input, EmiIngredient addition, EmiStack output, SmithingRecipe recipe) {
		super(EmiStack.EMPTY, input, addition, output, recipe.getId());
		this.uniq = EmiUtil.RANDOM.nextInt();
		this.recipe = recipe;
	}

	@Override
	public boolean supportsRecipeTree() {
		return false;
	}

	@Override
	public void addWidgets(WidgetHolder widgets) {
		widgets.addTexture(EmiTexture.EMPTY_ARROW, 62, 1);
		widgets.addSlot(EmiStack.EMPTY, 0, 0);
		widgets.addGeneratedSlot((r) -> this.getStack(r, 0), this.uniq, 18, 0).appendTooltip(() -> EmiTooltipComponents.getIngredientTooltipComponent(this.input.getEmiStacks()));
		widgets.addGeneratedSlot((r) -> this.getStack(r, 1), this.uniq, 36, 0).appendTooltip(() -> EmiTooltipComponents.getIngredientTooltipComponent(this.addition.getEmiStacks()));
		widgets.addGeneratedSlot((r) -> this.getStack(r, 2), this.uniq, 94, 0).recipeContext(this);
	}

	private EmiStack getStack(Random r, int i) {
		EmiStack input = this.input.getEmiStacks().get(r.nextInt(this.input.getEmiStacks().size()));
		EmiStack addition = this.addition.getEmiStacks().get(r.nextInt(this.addition.getEmiStacks().size()));
		// 1.20.1 smithing recipes read a 3 slot container (template, base, addition) instead of a SmithingRecipeInput
		SimpleContainer inv = new SimpleContainer(ItemStack.EMPTY, input.getItemStack(), addition.getItemStack());
		Minecraft client = Minecraft.getInstance();
		return new EmiStack[]{input, addition, EmiStack.of(this.recipe.assemble(inv, Objects.requireNonNull(client.level).registryAccess()))}[i];
	}
}
