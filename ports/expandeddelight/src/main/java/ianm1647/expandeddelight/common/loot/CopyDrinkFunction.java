package ianm1647.expandeddelight.common.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import ianm1647.expandeddelight.common.block.entity.JuicerBlockEntity;
import ianm1647.expandeddelight.common.registry.EDLootFunctions;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Copies the juicer's stored drink and its container into the dropped juicer item as BlockEntityTag,
 * written like Farmer's Delight's CopyMealFunction (MIT).
 */
@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class CopyDrinkFunction extends LootItemConditionalFunction {
    private CopyDrinkFunction(LootItemCondition[] conditions) {
        super(conditions);
    }

    public static LootItemConditionalFunction.Builder<?> builder() {
        return simpleBuilder(CopyDrinkFunction::new);
    }

    @Override
    protected ItemStack run(ItemStack stack, LootContext context) {
        if (context.getParamOrNull(LootContextParams.BLOCK_ENTITY) instanceof JuicerBlockEntity juicer) {
            CompoundTag tag = juicer.writeDrink(new CompoundTag());
            if (!tag.isEmpty()) {
                stack.addTagElement("BlockEntityTag", tag);
            }
        }
        return stack;
    }

    @Override
    public LootItemFunctionType getType() {
        return EDLootFunctions.COPY_DRINK.get();
    }

    public static class Serializer extends LootItemConditionalFunction.Serializer<CopyDrinkFunction> {
        @Override
        public CopyDrinkFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
            return new CopyDrinkFunction(conditions);
        }
    }
}
