package twilightforest.util;

import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.DynamicOps;
import com.mojang.serialization.codecs.PrimitiveCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.floats.Float2ObjectAVLTreeMap;
import it.unimi.dsi.fastutil.floats.Float2ObjectSortedMap;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.Vec3i;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.material.MapColor;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

public final class Codecs {
    public static final Codec<BlockPos> STRING_POS = Codec.STRING.comapFlatMap(Codecs::parseString2BlockPos, Vec3i::toShortString);
    public static final Codec<Direction> ONLY_HORIZONTAL = Direction.CODEC.comapFlatMap(direction -> direction.getAxis() != Direction.Axis.Y ? DataResult.success(direction) : DataResult.error(() -> "Horizontal direction only!", direction), Function.identity());
    public static final Codec<Float> FLOAT_STRING = Codec.STRING.comapFlatMap(Codecs::parseString2Float, f -> Float.toString(f));
    public static final Codec<Character> CHARACTER_CODEC = new CharacterCodec();

    /**
     * TF 4.8's MapColor codec, {@code {"id": <map colour id>, "color": <rgb>}}. 1.20.1 cannot create a MapColor without
     * overwriting the vanilla colour table, and a map pixel only stores the id anyway, so the id picks the vanilla MapColor
     * and {@code color} is written for reference but not used when reading.
     */
    public static final Codec<MapColor> COLOR_CODEC = RecordCodecBuilder.<Pair<Integer, Integer>>create(instance -> instance.group(
            Codec.INT.fieldOf("id").forGetter(Pair::getFirst),
            Codec.INT.fieldOf("color").forGetter(Pair::getSecond)
    ).apply(instance, Pair::of)).comapFlatMap(Codecs::validateMapColor, color -> Pair.of(color.id, color.col));

    public static final Codec<Climate.ParameterList<Holder<Biome>>> CLIMATE_SYSTEM = ExtraCodecs.nonEmptyList(RecordCodecBuilder.<Pair<Climate.ParameterPoint, Holder<Biome>>>create((instance) -> instance.group(Climate.ParameterPoint.CODEC.fieldOf("parameters").forGetter(Pair::getFirst), Biome.CODEC.fieldOf("biome").forGetter(Pair::getSecond)).apply(instance, Pair::of)).listOf()).xmap(Climate.ParameterList::new, Climate.ParameterList::values);

    public static <T> Codec<Float2ObjectSortedMap<T>> floatTreeCodec(Codec<T> elementCodec) {
        return Codec
                .compoundList(Codecs.FLOAT_STRING, elementCodec)
                .xmap(floatEList -> floatEList.stream().collect(Float2ObjectAVLTreeMap::new, (map, pair) -> map.put(pair.getFirst(), pair.getSecond()), Float2ObjectAVLTreeMap::putAll), map -> map.entrySet().stream().map(entry -> new Pair<>(entry.getKey(), entry.getValue())).toList());
    }

    private static DataResult<BlockPos> parseString2BlockPos(String string) {
        try {
            return Util.fixedSize(Arrays.stream(string.split(" *, *")).mapToInt(Integer::parseInt), 3).map(arr -> new BlockPos(arr[0], arr[1], arr[2]));
        } catch (Throwable e) {
            return DataResult.error(e::getMessage);
        }
    }

    private static DataResult<Float> parseString2Float(String string) {
        try {
            return DataResult.success(Float.valueOf(string));
        } catch (Throwable e) {
            return DataResult.error(e::getMessage);
        }
    }

    private static DataResult<MapColor> validateMapColor(Pair<Integer, Integer> idAndColor) {
        int id = idAndColor.getFirst();
        return id > 0 && id <= 63 && MapColor.byId(id) != MapColor.NONE ? DataResult.success(MapColor.byId(id)) : DataResult.error(() -> "Provided MapColor is not a valid MapColor: " + id);
    }

    public static <E> DataResult<Pair<E, E>> arrayToPair(List<E> list) {
        return Util.fixedSize(list, 2).map(l -> Pair.of(l.get(0), l.get(1)));
    }

    // A single character, written as a one-character string. Reading takes the first character of the string and rejects an empty one
    private static class CharacterCodec implements PrimitiveCodec<Character> {
        @Override
        public <T> DataResult<Character> read(DynamicOps<T> ops, T input) {
            return ops.getStringValue(input).flatMap(s -> s.isEmpty() ? DataResult.error(() -> "Expected character value, received empty string.") : DataResult.success(s.charAt(0)));
        }

        @Override
        public <T> T write(DynamicOps<T> ops, Character value) {
            return ops.createString(value.toString());
        }
    }

    private Codecs() {}
}
