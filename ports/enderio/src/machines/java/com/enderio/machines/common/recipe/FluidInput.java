package com.enderio.machines.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSyntaxException;
import com.mojang.datafixers.util.Either;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * SkyTekx: a fluid ingredient with an amount, the stand-in for the NeoForge {@code SizedFluidIngredient} that the 8.x
 * Vat recipes use. Forge 47 has no fluid ingredient. It matches one fluid or every fluid of a tag:
 * <pre>{@code
 * {"fluid": "enderio:hootch", "amount": 1000}
 * {"tag": "minecraft:water", "amount": 1000}
 * }</pre>
 */
public record FluidInput(Either<Fluid, TagKey<Fluid>> fluid, int amount) {

    public FluidInput {
        Objects.requireNonNull(fluid);
        if (amount <= 0) {
            throw new IllegalArgumentException("A fluid input needs a positive amount, got " + amount);
        }
    }

    public static FluidInput of(Fluid fluid, int amount) {
        return new FluidInput(Either.left(fluid), amount);
    }

    public static FluidInput of(TagKey<Fluid> tag, int amount) {
        return new FluidInput(Either.right(tag), amount);
    }

    /**
     * Whether the stack holds a matching fluid. The amount is not checked, see {@link #testWithAmount(FluidStack)}.
     */
    public boolean test(FluidStack stack) {
        if (stack.isEmpty()) {
            return false;
        }

        return fluid.map(f -> stack.getFluid().isSame(f), tag -> stack.getFluid().is(tag));
    }

    /**
     * Whether the stack holds a matching fluid and at least the amount of this input.
     */
    public boolean testWithAmount(FluidStack stack) {
        return test(stack) && stack.getAmount() >= amount;
    }

    /**
     * The matching fluids for display, such as in JEI. A tag lists its source fluids only, unless it holds no source.
     */
    public List<FluidStack> getDisplayStacks() {
        List<Fluid> fluids = new ArrayList<>();
        fluid.ifLeft(fluids::add).ifRight(tag -> ForgeRegistries.FLUIDS.tags().getTag(tag).forEach(fluids::add));

        List<FluidStack> sources = new ArrayList<>();
        List<FluidStack> all = new ArrayList<>();
        for (Fluid f : fluids) {
            if (f == Fluids.EMPTY) {
                continue;
            }

            FluidStack stack = new FluidStack(f, amount);
            all.add(stack);
            if (f.isSource(f.defaultFluidState())) {
                sources.add(stack);
            }
        }

        return sources.isEmpty() ? all : sources;
    }

    // region Serialization

    public static FluidInput fromJson(JsonObject json) {
        int amount = GsonHelper.getAsInt(json, "amount");
        if (amount <= 0) {
            throw new JsonSyntaxException("The fluid amount must be positive, got " + amount);
        }

        if (json.has("fluid") && json.has("tag")) {
            throw new JsonParseException("A fluid input takes either 'fluid' or 'tag', not both");
        }

        if (json.has("tag")) {
            ResourceLocation tag = new ResourceLocation(GsonHelper.getAsString(json, "tag"));
            return of(TagKey.create(Registries.FLUID, tag), amount);
        }

        ResourceLocation id = new ResourceLocation(GsonHelper.getAsString(json, "fluid"));
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new JsonSyntaxException("Unknown fluid '" + id + "'");
        }

        return of(fluid, amount);
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        fluid.ifLeft(f -> json.addProperty("fluid", Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(f)).toString()))
            .ifRight(tag -> json.addProperty("tag", tag.location().toString()));
        json.addProperty("amount", amount);
        return json;
    }

    public void toNetwork(FriendlyByteBuf buf) {
        buf.writeEither(fluid, (b, f) -> b.writeResourceLocation(Objects.requireNonNull(ForgeRegistries.FLUIDS.getKey(f))),
            (b, tag) -> b.writeResourceLocation(tag.location()));
        buf.writeVarInt(amount);
    }

    public static FluidInput fromNetwork(FriendlyByteBuf buf) {
        Either<Fluid, TagKey<Fluid>> fluid = buf.readEither(FluidInput::readFluid, b -> TagKey.create(Registries.FLUID, b.readResourceLocation()));
        return new FluidInput(fluid, buf.readVarInt());
    }

    private static Fluid readFluid(FriendlyByteBuf buf) {
        ResourceLocation id = buf.readResourceLocation();
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(id);
        if (fluid == null || fluid == Fluids.EMPTY) {
            throw new IllegalArgumentException("Unknown fluid '" + id + "' in a fluid input");
        }

        return fluid;
    }

    // endregion
}
