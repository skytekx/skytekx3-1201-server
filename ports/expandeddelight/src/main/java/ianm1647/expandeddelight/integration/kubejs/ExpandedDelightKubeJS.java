package ianm1647.expandeddelight.integration.kubejs;

import dev.latvian.mods.kubejs.KubeJSPlugin;
import dev.latvian.mods.kubejs.recipe.schema.RegisterRecipeSchemasEvent;
import ianm1647.expandeddelight.ExpandedDelight;
import ianm1647.expandeddelight.integration.kubejs.recipe.JuicingRecipeJS;

/**
 * KubeJS loads this plugin through kubejs.plugins.txt, so nothing else names it. This is the KubeJS 6
 * (2001) form of upstream's KubeJS 7 plugin.
 */
public class ExpandedDelightKubeJS extends KubeJSPlugin {
    @Override
    public void registerRecipeSchemas(RegisterRecipeSchemasEvent event) {
        event.namespace(ExpandedDelight.MODID).register("juicing", JuicingRecipeJS.SCHEMA);
    }
}
