package com.enderio.machines.data.tag;

import com.enderio.EnderIO;
import com.enderio.machines.common.tag.MachineTags;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.CompletableFuture;

/**
 * The reagent tags of the Vat recipes (6bc3ec343), with forge tags for the c: tags of 8.x and 1.20.1 items for the
 * 1.20.5 and 1.21 ones.
 */
public class MachineItemTagsProvider extends IntrinsicHolderTagsProvider<Item> {

    public MachineItemTagsProvider(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> provider, @Nullable ExistingFileHelper existingFileHelper) {
        super(packOutput, Registries.ITEM, provider, item -> item.builtInRegistryHolder().key(), EnderIO.MODID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // 8.x points these at the NeoForge 1.21.1 tags c:seeds and c:crops. Forge 47's forge:seeds and forge:crops
        // lack some of their vanilla members, so the items that only the c: tags hold are listed here. Without sugar
        // cane the 0.5 factor of the vat_reagent data map for #enderio:crops could never apply.
        tag(MachineTags.Items.SEEDS).addTag(Tags.Items.SEEDS).add(Items.TORCHFLOWER_SEEDS);
        tag(MachineTags.Items.CROPS).addTag(Tags.Items.CROPS).add(Items.CACTUS, Items.COCOA_BEANS, Items.MELON, Items.PUMPKIN, Items.SUGAR_CANE);

        tag(MachineTags.Items.CLOUD_COLD).add(Items.SNOW, Items.SNOW_BLOCK, Items.SNOWBALL, Items.ICE, Items.PACKED_ICE, Items.BLUE_ICE);
        tag(MachineTags.Items.EXPLOSIVES).add(Items.TNT, Items.FIREWORK_STAR, Items.FIREWORK_ROCKET, Items.FIRE_CHARGE, Items.GUNPOWDER);
        tag(MachineTags.Items.NATURAL_LIGHTS).add(Items.GLOWSTONE_DUST, Items.GLOWSTONE, Items.SEA_LANTERN, Items.SEA_PICKLE, Items.GLOW_LICHEN,
            Items.GLOW_BERRIES, Items.GLOW_INK_SAC);
        tag(MachineTags.Items.SUNFLOWER).add(Items.SUNFLOWER, Items.TORCHFLOWER);

        // 8.x uses #minecraft:meat, a tag of 1.20.5.
        tag(MachineTags.Items.MEAT).add(Items.BEEF, Items.COOKED_BEEF, Items.PORKCHOP, Items.COOKED_PORKCHOP, Items.CHICKEN, Items.COOKED_CHICKEN,
            Items.MUTTON, Items.COOKED_MUTTON, Items.RABBIT, Items.COOKED_RABBIT, Items.ROTTEN_FLESH);

        tag(MachineTags.Items.PRISMARINE).add(Items.PRISMARINE_SHARD);
        tag(MachineTags.Items.BLAZE_POWDER).add(Items.BLAZE_POWDER);
        tag(MachineTags.Items.LIGHTNING_ROD).add(Items.LIGHTNING_ROD);

        // SkyTekx: 8.x fills this tag with the wind charge of 1.21. Phantom membrane keeps concentrated cloud seed
        // craftable on 1.20.1. A datapack can change the tag.
        tag(MachineTags.Items.WIND_CHARGES).add(Items.PHANTOM_MEMBRANE);
    }
}
