package twilightforest.util;

import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;

// For making rectangular grids that are approximately-evenly spaced (floating-point -> integer rounding), even if re-sampled for a different chunk or general region
// Making a hexagonal pattern will require using two of these
// Positions are lazily generated, meaning no excess of positions are produced if terminated early
public class RectangleLatticeIterator<T> implements Iterator<T>, Iterable<T> {
    private final int yLevel, latticeStartX, latticeStartZ, latticeCountX, latticeCountZ;
    private final float xSpacing, zSpacing, xOffset, zOffset;
    private final TernaryIntegerFunction<T> converter;

    private int latticeX = 0, latticeZ = 0;

    public static RectangleLatticeIterator<BlockPos.MutableBlockPos> boundedGrid(BoundingBox chunkBounds, int yLevel, float xSpacing, float zSpacing, float xOffset, float zOffset) {
        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();
        return new RectangleLatticeIterator<>(chunkBounds.minX(), chunkBounds.minZ(), chunkBounds.maxX(), chunkBounds.maxZ(), yLevel, xSpacing, zSpacing, xOffset, zOffset, mutableBlockPos::set);
    }

    public RectangleLatticeIterator(int minX, int minZ, int maxX, int maxZ, int yLevel, float xSpacing, float zSpacing, float xOffset, float zOffset, TernaryIntegerFunction<T> converter) {
        if (!(xSpacing > 0) || !(zSpacing > 0))
            throw new IllegalArgumentException("Lattice spacing must be positive, got " + xSpacing + " x " + zSpacing);

        this.yLevel = yLevel;

        this.xSpacing = xSpacing;
        this.zSpacing = zSpacing;
        this.xOffset = xOffset;
        this.zOffset = zOffset;

        // TF 4.8 (65859725a) fixed the 4.3 index math, which skipped or doubled lattice points at chunk borders, but its version
        // yields each point one lattice step early, often in the neighbouring chunk. Instead, count exactly the lattice points
        // whose block coordinate lies inside [min, max], so every point is generated once and inside the chunk being decorated
        this.latticeStartX = firstLatticeIndex(minX, this.xSpacing, this.xOffset);
        this.latticeStartZ = firstLatticeIndex(minZ, this.zSpacing, this.zOffset);
        this.latticeCountX = Math.max(0, firstLatticeIndex(maxX + 1, this.xSpacing, this.xOffset) - this.latticeStartX);
        this.latticeCountZ = Math.max(0, firstLatticeIndex(maxZ + 1, this.zSpacing, this.zOffset) - this.latticeStartZ);

        this.converter = converter;
    }

    @Override
    public T next() {
        T ret = this.converter.apply(this.generateX(), this.yLevel, this.generateZ());

        // March downwards, then rightwards as each column is completed
        if (this.latticeZ + 1 < this.latticeCountZ) {
            this.latticeZ++;
        } else {
            this.latticeZ = 0;
            this.latticeX++;
        }

        return ret;
    }

    private int generateX() {
        return toBlockCoordinate(this.latticeStartX + this.latticeX, this.xSpacing, this.xOffset);
    }

    private int generateZ() {
        return toBlockCoordinate(this.latticeStartZ + this.latticeZ, this.zSpacing, this.zOffset);
    }

    @Override
    public boolean hasNext() {
        return this.latticeCountZ > 0 && this.latticeX < this.latticeCountX;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return this;
    }

    // Flooring instead of 4.3/4.8's (int) cast keeps the mapping strictly increasing across zero, so no two lattice points share a block
    private static int toBlockCoordinate(int latticeIndex, float latticeSpacing, float offset) {
        return Mth.floor(offset + latticeIndex * latticeSpacing);
    }

    /**
     * @return The lowest lattice index whose block coordinate is at or after the given block coordinate
     */
    private static int firstLatticeIndex(int blockCoordinate, float latticeSpacing, float offset) {
        int index = Mth.floor((blockCoordinate - offset) / latticeSpacing);

        // The estimate can be one off from float rounding, settle it against the exact coordinate function
        while (toBlockCoordinate(index, latticeSpacing, offset) < blockCoordinate) index++;
        while (toBlockCoordinate(index - 1, latticeSpacing, offset) >= blockCoordinate) index--;

        return index;
    }

    @FunctionalInterface
    public interface TernaryIntegerFunction<T> {
        T apply(int x, int y, int z);
    }

    public record TriangularLatticeConfig(float spacing, float xOffset, float zOffset, float xSpacing, float zSpacing) {
        private static final Codec<TriangularLatticeConfig> VERBOSE_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(1, 32).fieldOf("spacing").forGetter(TriangularLatticeConfig::spacing),
                Codec.floatRange(1, 32).fieldOf("x_offset").forGetter(TriangularLatticeConfig::xOffset),
                Codec.floatRange(1, 32).fieldOf("z_offset").forGetter(TriangularLatticeConfig::zOffset),
                Codec.floatRange(1, 32).fieldOf("x_spacing").forGetter(TriangularLatticeConfig::xSpacing),
                Codec.floatRange(1, 32).fieldOf("z_spacing").forGetter(TriangularLatticeConfig::zSpacing)
        ).apply(instance, TriangularLatticeConfig::new));

        private static final Codec<TriangularLatticeConfig> OFFSET_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(1, 32).fieldOf("spacing").forGetter(TriangularLatticeConfig::spacing),
                Codec.floatRange(1, 32).fieldOf("x_offset").forGetter(TriangularLatticeConfig::xOffset),
                Codec.floatRange(1, 32).fieldOf("z_offset").forGetter(TriangularLatticeConfig::zOffset)
        ).apply(instance, TriangularLatticeConfig::new));

        private static final Codec<TriangularLatticeConfig> SPACING_CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.floatRange(1, 32).fieldOf("spacing").forGetter(TriangularLatticeConfig::spacing)
        ).apply(instance, TriangularLatticeConfig::new));

        public static final TriangularLatticeConfig DEFAULT = new TriangularLatticeConfig(3.5f);

        public static final Codec<TriangularLatticeConfig> CODEC = withAlternative(VERBOSE_CODEC, withAlternative(OFFSET_CODEC, withAlternative(SPACING_CODEC, Codec.unit(DEFAULT))));

        // Stand-in for Codec.withAlternative() (DFU 7+, MC 1.20.5+): decodes with the primary codec, falls back to the alternative, always encodes with the primary
        private static <T> Codec<T> withAlternative(Codec<T> primary, Codec<T> alternative) {
            return Codec.either(primary, alternative).xmap(either -> either.map(t -> t, t -> t), Either::left);
        }

        public TriangularLatticeConfig(float spacing) {
            this(spacing, Mth.cos(Mth.PI / 6f) * spacing, Mth.sin(Mth.PI / 6f) * spacing);
        }

        public TriangularLatticeConfig(float spacing, float xOffset, float zOffset) {
            this(spacing, xOffset, zOffset, xOffset * 2f, spacing);
        }

        public static TriangularLatticeConfig fromNBT(CompoundTag tag) {
            float spacing = tag.getFloat("spacing");
            if (spacing <= 0.0000001) spacing = 3.5f;

            float xOffset = tag.contains("x_offset", 5) ? tag.getFloat("x_offset") : Mth.cos(Mth.PI / 6f) * spacing;
            float zOffset = tag.contains("z_offset", 5) ? tag.getFloat("z_offset") : Mth.sin(Mth.PI / 6f) * spacing;

            if (tag.contains("x_spacing", 5) || tag.contains("z_spacing", 5)) {
                return new TriangularLatticeConfig(spacing, xOffset, zOffset, tag.getFloat("x_spacing"), tag.getFloat("z_spacing"));
            } else {
                return new TriangularLatticeConfig(spacing, xOffset, zOffset);
            }
        }

        /**
         * Two interleaved rectangular lattices, the second shifted by half a cell, form a triangular lattice: the densest even
         * spacing for filling a structure with stalactites, stalagmites, chests and spawners.
         */
        public ZippedIterator<BlockPos.MutableBlockPos> boundedGrid(BoundingBox chunkBounds, int yLevel) {
            return new ZippedIterator<>(RectangleLatticeIterator.boundedGrid(chunkBounds, yLevel, this.xSpacing, this.zSpacing, 0, 0), RectangleLatticeIterator.boundedGrid(chunkBounds, yLevel, this.xSpacing, this.zSpacing, this.xOffset, this.zOffset));
        }
    }
}
