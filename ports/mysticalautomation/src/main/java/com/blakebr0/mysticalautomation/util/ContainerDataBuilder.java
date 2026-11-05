package com.blakebr0.mysticalautomation.util;

import net.minecraft.world.inventory.ContainerData;

import java.util.ArrayList;
import java.util.List;
import java.util.function.IntConsumer;
import java.util.function.IntSupplier;

/**
 * SkyTekx port: the 1.20.1 form of Cucumber 8's {@code com.blakebr0.cucumber.util.ContainerDataBuilder}
 * (Cucumber, MIT License, Copyright (c) 2018 BlakeBr0), which Cucumber 7 lacks.
 * <p>
 * Minecraft 1.20.1 sends container data slots as shorts ({@code ClientboundContainerSetDataPacket}), and Forge 47
 * has no NeoForge style payload for larger values. Energy, capacity and fuel values of the machines exceed 32767,
 * so every synced int takes two data slots here: the low and the high 16 bits. Menus read a value back with
 * {@link #get(ContainerData, int)} and size their client side {@code SimpleContainerData} with {@link #slots(int)}.
 */
public class ContainerDataBuilder {
    private final List<DataSlotGetterSetter> data;

    private ContainerDataBuilder() {
        this.data = new ArrayList<>();
    }

    public static ContainerDataBuilder builder() {
        return new ContainerDataBuilder();
    }

    /**
     * @param values the number of synced int values
     * @return the number of data slots that these values take
     */
    public static int slots(int values) {
        return values * 2;
    }

    /**
     * Reads the int value with the given index from data built by this class
     * @param data the container data (the tile's on the server, a SimpleContainerData on the client)
     * @param index the index of the synced value, in the order of the {@link #sync(IntSupplier, IntConsumer)} calls
     * @return the value
     */
    public static int get(ContainerData data, int index) {
        return (data.get(index * 2) & 0xFFFF) | (data.get(index * 2 + 1) << 16);
    }

    public ContainerDataBuilder sync(IntSupplier getter, IntConsumer setter) {
        this.data.add(new DataSlotGetterSetter(getter, setter));
        return this;
    }

    public ContainerData build() {
        return new ContainerData() {
            private final List<DataSlotGetterSetter> data = ContainerDataBuilder.this.data;
            private final int[] halves = new int[this.data.size() * 2];

            @Override
            public int get(int index) {
                var value = this.data.get(index / 2).getter.getAsInt();
                return index % 2 == 0 ? value & 0xFFFF : (value >>> 16) & 0xFFFF;
            }

            @Override
            public void set(int index, int value) {
                this.halves[index] = value;

                var low = this.halves[index - index % 2] & 0xFFFF;
                var high = this.halves[index - index % 2 + 1] << 16;

                this.data.get(index / 2).setter.accept(low | high);
            }

            @Override
            public int getCount() {
                return this.data.size() * 2;
            }
        };
    }

    record DataSlotGetterSetter(IntSupplier getter, IntConsumer setter) { }
}
