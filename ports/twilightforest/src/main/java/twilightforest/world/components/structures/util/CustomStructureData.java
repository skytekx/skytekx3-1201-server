package twilightforest.world.components.structures.util;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import twilightforest.world.components.structures.start.TFStructureStart;
import twilightforest.world.components.structures.type.KnightStrongholdStructure;

public interface CustomStructureData {
    default TFStructureStart forDeserialization(Structure structure, ChunkPos chunkPos, int references, PiecesContainer pieces, CompoundTag nbt) {
        TFStructureStart start;
        // Knight Stronghold starts persist their clipped start height ("knight_y"). Starts saved before that tag
        // existed are still loaded as KnightStructureStart, which then falls back to the legacy height.
        if (nbt.contains(KnightStrongholdStructure.KnightStructureStart.KNIGHT_Y_TAG) || structure instanceof KnightStrongholdStructure)
            start = new KnightStrongholdStructure.KnightStructureStart(structure, chunkPos, references, pieces);
        else
            start = new TFStructureStart(structure, chunkPos, references, pieces);
        start.load(nbt);
        return start;
    }
}
