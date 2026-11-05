package ianm1647.expandeddelight;

import net.minecraftforge.common.ForgeConfigSpec;

public class EDConfig {
    public static final ForgeConfigSpec COMMON_CONFIG;
    public static final String CATEGORY_RECIPE_BOOK = "recipe_book";
    public static final ForgeConfigSpec.BooleanValue ENABLE_RECIPE_BOOK_JUICER;

    public EDConfig() {
    }

    static {
        ForgeConfigSpec.Builder COMMON_BUILDER = new ForgeConfigSpec.Builder();
        COMMON_BUILDER.comment("Recipe book").push(CATEGORY_RECIPE_BOOK);
        ENABLE_RECIPE_BOOK_JUICER = COMMON_BUILDER.comment("Should the Juicer have a Recipe Book available on its interface?").define("enableRecipeBookJuicer", true);
        COMMON_BUILDER.pop();
        COMMON_CONFIG = COMMON_BUILDER.build();
    }
}
