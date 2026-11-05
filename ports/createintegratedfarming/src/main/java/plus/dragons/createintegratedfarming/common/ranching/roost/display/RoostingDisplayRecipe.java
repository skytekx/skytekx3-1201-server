/*
 * Copyright (C) 2025  DragonsPlus
 * SPDX-License-Identifier: LGPL-3.0-or-later
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package plus.dragons.createintegratedfarming.common.ranching.roost.display;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import plus.dragons.createintegratedfarming.common.network.BufCodec;

public record RoostingDisplayRecipe(
        ResourceLocation id,
        ResourceLocation representativeBlock,
        List<ResourceLocation> equivalentBlocks,
        IntRange productionTime,
        List<ItemFeedDisplay> itemFeeds,
        List<FluidFeedDisplay> fluidFeeds,
        List<OutputDisplay> outputs,
        LootDisplayStatus lootStatus) {

    private static final int MAX_BLOCKS = 64;
    private static final int MAX_FEEDS = 4096;
    private static final int MAX_OUTPUTS = 4096;
    private static final BufCodec<ResourceLocation> RESOURCE_LOCATION = BufCodec.ofMember(
            (id, buffer) -> buffer.writeResourceLocation(id), FriendlyByteBuf::readResourceLocation);

    public static final BufCodec<RoostingDisplayRecipe> STREAM_CODEC = BufCodec.ofMember(
            RoostingDisplayRecipe::encode,
            RoostingDisplayRecipe::decode);
    public RoostingDisplayRecipe {
        equivalentBlocks = List.copyOf(equivalentBlocks);
        itemFeeds = List.copyOf(itemFeeds);
        fluidFeeds = List.copyOf(fluidFeeds);
        outputs = List.copyOf(outputs);
    }

    private void encode(FriendlyByteBuf buffer) {
        buffer.writeResourceLocation(id);
        buffer.writeResourceLocation(representativeBlock);
        writeList(buffer, equivalentBlocks, RESOURCE_LOCATION);
        IntRange.STREAM_CODEC.encode(buffer, productionTime);
        writeList(buffer, itemFeeds, ItemFeedDisplay.STREAM_CODEC);
        writeList(buffer, fluidFeeds, FluidFeedDisplay.STREAM_CODEC);
        writeList(buffer, outputs, OutputDisplay.STREAM_CODEC);
        buffer.writeEnum(lootStatus);
    }

    private static RoostingDisplayRecipe decode(FriendlyByteBuf buffer) {
        return new RoostingDisplayRecipe(
                buffer.readResourceLocation(),
                buffer.readResourceLocation(),
                readList(buffer, RESOURCE_LOCATION, MAX_BLOCKS),
                IntRange.STREAM_CODEC.decode(buffer),
                readList(buffer, ItemFeedDisplay.STREAM_CODEC, MAX_FEEDS),
                readList(buffer, FluidFeedDisplay.STREAM_CODEC, MAX_FEEDS),
                readList(buffer, OutputDisplay.STREAM_CODEC, MAX_OUTPUTS),
                buffer.readEnum(LootDisplayStatus.class));
    }

    private static <T> void writeList(
            FriendlyByteBuf buffer, List<T> values, BufCodec<T> codec) {
        buffer.writeVarInt(values.size());
        values.forEach(value -> codec.encode(buffer, value));
    }

    private static <T> List<T> readList(
            FriendlyByteBuf buffer,
            BufCodec<T> codec,
            int maximumSize) {
        int size = buffer.readVarInt();
        if (size < 0 || size > maximumSize)
            throw new IllegalArgumentException("Invalid roosting display list size: " + size);
        var result = new ArrayList<T>(size);
        for (int i = 0; i < size; i++)
            result.add(codec.decode(buffer));
        return result;
    }

    public record IntRange(int minimum, int maximum) {
        public static final BufCodec<IntRange> STREAM_CODEC = BufCodec.ofMember(
                IntRange::encode,
                IntRange::decode);

        public IntRange {
            if (minimum < 0 || maximum < minimum)
                throw new IllegalArgumentException("Invalid integer range: " + minimum + ".." + maximum);
        }

        public static IntRange exact(int value) {
            return new IntRange(value, value);
        }

        public boolean isExact() {
            return minimum == maximum;
        }

        public IntRange add(int value) {
            return new IntRange(Math.addExact(minimum, value), Math.addExact(maximum, value));
        }

        public IntRange multiply(int value) {
            return new IntRange(Math.multiplyExact(minimum, value), Math.multiplyExact(maximum, value));
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeVarInt(minimum);
            buffer.writeVarInt(maximum);
        }

        private static IntRange decode(FriendlyByteBuf buffer) {
            return new IntRange(buffer.readVarInt(), buffer.readVarInt());
        }
    }

    public record ItemFeedDisplay(
            ItemStack ingredient, IntRange progress, IntRange cooldown, ItemStack remainder) {

        public static final BufCodec<ItemFeedDisplay> STREAM_CODEC = BufCodec.ofMember(
                ItemFeedDisplay::encode,
                ItemFeedDisplay::decode);
        public ItemFeedDisplay {
            ingredient = ingredient.copy();
            remainder = remainder.copy();
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeItem(ingredient);
            IntRange.STREAM_CODEC.encode(buffer, progress);
            IntRange.STREAM_CODEC.encode(buffer, cooldown);
            buffer.writeItem(remainder);
        }

        private static ItemFeedDisplay decode(FriendlyByteBuf buffer) {
            return new ItemFeedDisplay(
                    buffer.readItem(),
                    IntRange.STREAM_CODEC.decode(buffer),
                    IntRange.STREAM_CODEC.decode(buffer),
                    buffer.readItem());
        }
    }

    public record FluidFeedDisplay(FluidStack ingredient, IntRange progress, IntRange cooldown) {

        public static final BufCodec<FluidFeedDisplay> STREAM_CODEC = BufCodec.ofMember(
                FluidFeedDisplay::encode,
                FluidFeedDisplay::decode);
        public FluidFeedDisplay {
            ingredient = ingredient.copy();
        }

        private void encode(FriendlyByteBuf buffer) {
            ingredient.writeToPacket(buffer);
            IntRange.STREAM_CODEC.encode(buffer, progress);
            IntRange.STREAM_CODEC.encode(buffer, cooldown);
        }

        private static FluidFeedDisplay decode(FriendlyByteBuf buffer) {
            return new FluidFeedDisplay(
                    FluidStack.readFromPacket(buffer),
                    IntRange.STREAM_CODEC.decode(buffer),
                    IntRange.STREAM_CODEC.decode(buffer));
        }
    }

    public record OutputDisplay(ItemStack ingredient, IntRange count, boolean conditional) {

        public static final BufCodec<OutputDisplay> STREAM_CODEC = BufCodec.ofMember(
                OutputDisplay::encode,
                OutputDisplay::decode);
        public OutputDisplay {
            ingredient = ingredient.copy();
        }

        private void encode(FriendlyByteBuf buffer) {
            buffer.writeItem(ingredient);
            IntRange.STREAM_CODEC.encode(buffer, count);
            buffer.writeBoolean(conditional);
        }

        private static OutputDisplay decode(FriendlyByteBuf buffer) {
            return new OutputDisplay(
                    buffer.readItem(),
                    IntRange.STREAM_CODEC.decode(buffer),
                    buffer.readBoolean());
        }
    }

    public enum LootDisplayStatus {
        EXACT,
        CONDITIONAL,
        COMPLEX,
        MISSING;

        public LootDisplayStatus merge(LootDisplayStatus other) {
            if (this == COMPLEX || other == COMPLEX)
                return COMPLEX;
            if (this == MISSING || other == MISSING)
                return MISSING;
            if (this == CONDITIONAL || other == CONDITIONAL)
                return CONDITIONAL;
            return EXACT;
        }
    }
}
