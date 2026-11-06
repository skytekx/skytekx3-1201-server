package de.ellpeck.actuallyadditions.api;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public final class ActuallyTags {
    public static final void init() {
        // lol
        Items.touch();
        Blocks.touch();
    }
    public static class Items {
        public static void touch() {
            // load the stupid tags
        }
        public static final TagKey<Item> DRILLS = tag("drills");
        public static final TagKey<Item> COFFEE_BEANS = tag("coffee_beans");
        public static final TagKey<Item> TINY_COALS = tag("tiny_coals");
        public static final TagKey<Item> HOLDS_ITEMS = ItemTags.create(new ResourceLocation("forge", "holds_items"));
        public static final TagKey<Item> CRYSTALS = tag("crystals");
        public static final TagKey<Item> CRYSTAL_BLOCKS = tag("crystal_blocks");
        public static final TagKey<Item> SEEDS_RICE = forge("seeds/rice");
        public static final TagKey<Item> SEEDS_COFFEE = forge("seeds/coffee");
        public static final TagKey<Item> SEEDS_CANOLA = forge("seeds/canola");
        public static final TagKey<Item> SEEDS_FLAX = forge("seeds/flax");
        public static final TagKey<Item> CROPS_RICE = forge("crops/rice");
        public static final TagKey<Item> CROPS_COFFEE = forge("crops/coffee");
        public static final TagKey<Item> CROPS_CANOLA = forge("crops/canola");
        public static final TagKey<Item> CROPS_FLAX = forge("crops/flax");
        public static final TagKey<Item> GEMS_BLACK_QUARTZ = forge("gems/black_quartz");
        public static final TagKey<Item> ORES_BLACK_QUARTZ = forge("ores/black_quartz");
        public static final TagKey<Item> STORAGE_BLOCKS_BLACK_QUARTZ = forge("storage_blocks/black_quartz");
        public static final TagKey<Item> STORAGE_BLOCKS_RESTONIA_CRYSTAL = forge("storage_blocks/restonia_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_PALIS_CRYSTAL = forge("storage_blocks/palis_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_DIAMATINE_CRYSTAL = forge("storage_blocks/diamatine_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_VOID_CRYSTAL = forge("storage_blocks/void_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_EMERADIC_CRYSTAL = forge("storage_blocks/emeradic_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_ENORI_CRYSTAL = forge("storage_blocks/enori_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_EMPOWERED_RESTONIA_CRYSTAL = forge("storage_blocks/empowered_restonia_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_EMPOWERED_PALIS_CRYSTAL = forge("storage_blocks/empowered_palis_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_EMPOWERED_DIAMATINE_CRYSTAL = forge("storage_blocks/empowered_diamatine_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_EMPOWERED_VOID_CRYSTAL = forge("storage_blocks/empowered_void_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_EMPOWERED_EMERADIC_CRYSTAL = forge("storage_blocks/empowered_emeradic_crystal");
        public static final TagKey<Item> STORAGE_BLOCKS_EMPOWERED_ENORI_CRYSTAL = forge("storage_blocks/empowered_enori_crystal");
        // SkyTekx: main names these c:bucket/<fluid>. Forge 47 has no common bucket tag, so they stay in the mod's namespace
        public static final TagKey<Item> BUCKET_CANOLA_OIL = tag("buckets/canola_oil");
        public static final TagKey<Item> BUCKET_REFINED_CANOLA_OIL = tag("buckets/refined_canola_oil");
        public static final TagKey<Item> BUCKET_CRYSTALLIZED_OIL = tag("buckets/crystallized_oil");
        public static final TagKey<Item> BUCKET_EMPOWERED_OIL = tag("buckets/empowered_oil");
        public static final TagKey<Item> STONE_ORE_REPLACEABLES = tag("stone_ore_replaceables");
        public static final TagKey<Item> DEEPSLATE_ORE_REPLACEABLES = tag("deepslate_ore_replaceables");
        public static final TagKey<Item> LAMPS = tag("lamps");
        // SkyTekx: the Curios charm slot of b5cb3434, filled with the Crafting Table On A Stick
        public static final TagKey<Item> CURIOS_CHARM = ItemTags.create(new ResourceLocation("curios", "charm"));
        // SkyTekx: the tool tags that pack mods keep next to minecraft:pickaxes and friends, which Forge
        // 47 itself does not define (Farmer's Delight 1.3.4 and Immersive Engineering 10.2.0 ship them),
        // and the paxel tag of Mekanism Tools and Mystical Agradditions. The AIOTs join them
        public static final TagKey<Item> TOOLS_PICKAXES = forge("tools/pickaxes");
        public static final TagKey<Item> TOOLS_AXES = forge("tools/axes");
        public static final TagKey<Item> TOOLS_SHOVELS = forge("tools/shovels");
        public static final TagKey<Item> TOOLS_SWORDS = forge("tools/swords");
        public static final TagKey<Item> TOOLS_PAXELS = forge("tools/paxels");

        private static TagKey<Item> tag(String name) {
            return TagKey.create(Registries.ITEM, new ResourceLocation(ActuallyAdditions.MODID, name));
        }

        private static TagKey<Item> forge(String name) {
            return ItemTags.create(new ResourceLocation("forge", name));
        }
    }

    public static class Blocks {
        public static void touch() {
            // load the stupid tags
        }

        public static final TagKey<Block> WORM_CAN_POP = tag("worm_drop");
        public static final TagKey<Block> MINEABLE_WITH_DRILL = tag("mineable/drill");
        public static final TagKey<Block> MINEABLE_WITH_AIO = tag("mineable/aio");
        public static final TagKey<Block> NEEDS_BLACK_QUARTZ_TOOL = tag("needs_black_quartz_tool");
        public static final TagKey<Block> NEEDS_RESTONIA_TOOL = tag("needs_restonia_tool");
        public static final TagKey<Block> NEEDS_PALIS_TOOL = tag("needs_palis_tool");
        public static final TagKey<Block> NEEDS_DIAMATINE_TOOL = tag("needs_diamatine_tool");
        public static final TagKey<Block> NEEDS_VOID_TOOL = tag("needs_void_tool");
        public static final TagKey<Block> NEEDS_EMERADIC_TOOL = tag("needs_emeradic_tool");
        public static final TagKey<Block> NEEDS_ENORI_TOOL = tag("needs_enori_tool");
        public static final TagKey<Block> ORES_BLACK_QUARTZ = forge("ores/black_quartz");
        public static final TagKey<Block> STORAGE_BLOCKS_BLACK_QUARTZ = forge("storage_blocks/black_quartz");
        public static final TagKey<Block> STORAGE_BLOCKS_RESTONIA_CRYSTAL = forge("storage_blocks/restonia_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_PALIS_CRYSTAL = forge("storage_blocks/palis_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_DIAMATINE_CRYSTAL = forge("storage_blocks/diamatine_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_VOID_CRYSTAL = forge("storage_blocks/void_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_EMERADIC_CRYSTAL = forge("storage_blocks/emeradic_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_ENORI_CRYSTAL = forge("storage_blocks/enori_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_EMPOWERED_RESTONIA_CRYSTAL = forge("storage_blocks/empowered_restonia_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_EMPOWERED_PALIS_CRYSTAL = forge("storage_blocks/empowered_palis_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_EMPOWERED_DIAMATINE_CRYSTAL = forge("storage_blocks/empowered_diamatine_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_EMPOWERED_VOID_CRYSTAL = forge("storage_blocks/empowered_void_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_EMPOWERED_EMERADIC_CRYSTAL = forge("storage_blocks/empowered_emeradic_crystal");
        public static final TagKey<Block> STORAGE_BLOCKS_EMPOWERED_ENORI_CRYSTAL = forge("storage_blocks/empowered_enori_crystal");

        private static TagKey<Block> tag(String name) {
            return TagKey.create(Registries.BLOCK, new ResourceLocation(ActuallyAdditions.MODID, name));
        }

        private static TagKey<Block> forge(String name) {
            return BlockTags.create(new ResourceLocation("forge", name));
        }
    }
}
