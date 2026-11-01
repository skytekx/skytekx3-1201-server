package twilightforest.compat.jei.renderers;

import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.Nullable;
import twilightforest.compat.jei.FakeEntityType;
import twilightforest.compat.jei.JEICompat;

import java.util.Objects;

public class EntityHelper implements IIngredientHelper<FakeEntityType> {

	@Override
	public IIngredientType<FakeEntityType> getIngredientType() {
		return JEICompat.ENTITY_TYPE;
	}

	@Override
	public String getDisplayName(FakeEntityType type) {
		return type.type().getDescription().getString();
	}

	@Override
	public String getUniqueId(FakeEntityType type, UidContext context) {
		return Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(type.type())).toString();
	}

	@Override
	public ResourceLocation getResourceLocation(FakeEntityType type) {
		return Objects.requireNonNull(ForgeRegistries.ENTITY_TYPES.getKey(type.type()));
	}

	@Override
	public FakeEntityType copyIngredient(FakeEntityType type) {
		return type;
	}

	@Override
	public String getErrorInfo(@Nullable FakeEntityType type) {
		if (type == null) {
			return "null";
		}
		ResourceLocation name = ForgeRegistries.ENTITY_TYPES.getKey(type.type());
		if (name == null) {
			return "unnamed sadface :(";
		}
		return name.toString();
	}
}