package de.ellpeck.actuallyadditions.data;

import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.world.entity.EntityType;
import net.minecraftforge.common.data.ExistingFileHelper;
import top.theillusivec4.curios.api.CuriosDataProvider;

import java.util.concurrent.CompletableFuture;

// SkyTekx: the Curios data of b5cb3434, the charm slot for players, which the Crafting Table On A
// Stick fills (data/actuallyadditions/curios/entities/player.json, the slot format of Curios 5).
// ActuallyAdditionsData adds it only when Curios is loaded, as in the dev runs
public class Curios extends CuriosDataProvider {
    public Curios(PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        super(ActuallyAdditions.MODID, output, fileHelper, registries);
    }

    public static void addTo(DataGenerator generator, PackOutput output, ExistingFileHelper fileHelper, CompletableFuture<HolderLookup.Provider> registries) {
        generator.addProvider(true, new Curios(output, fileHelper, registries));
    }

    @Override
    public void generate(HolderLookup.Provider registries, ExistingFileHelper fileHelper) {
        this.createEntities("player").addEntities(EntityType.PLAYER).addSlots("charm");
    }
}
