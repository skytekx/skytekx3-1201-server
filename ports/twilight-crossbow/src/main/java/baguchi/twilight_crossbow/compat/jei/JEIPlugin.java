package baguchi.twilight_crossbow.compat.jei;

import baguchi.twilight_crossbow.TwilightCrossBow;
import baguchi.twilight_crossbow.recipe.RebuildSmithingRecipe;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IVanillaCategoryExtensionRegistration;
import net.minecraft.resources.ResourceLocation;

@JeiPlugin
public class JEIPlugin implements IModPlugin {
    public static final ResourceLocation PLUGIN_ID = TwilightCrossBow.prefix("jei_plugin");

    @Override
    public void registerVanillaCategoryExtensions(IVanillaCategoryExtensionRegistration registration) {
        registration.getSmithingCategory().addExtension(RebuildSmithingRecipe.class, new ReBuildSmithingExtension());
    }

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_ID;
    }
}
