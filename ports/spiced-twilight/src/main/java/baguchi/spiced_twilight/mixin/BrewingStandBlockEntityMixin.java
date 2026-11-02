package baguchi.spiced_twilight.mixin;

import baguchi.spiced_twilight.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BrewingStandBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(BrewingStandBlockEntity.class)
public abstract class BrewingStandBlockEntityMixin extends BaseContainerBlockEntity {

    protected BrewingStandBlockEntityMixin(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Inject(method = "serverTick", at = @At("HEAD"))
    private static void spicedTwilight$fireBeetlePowderFuel(Level level, BlockPos pos, BlockState state, BrewingStandBlockEntity blockEntity, CallbackInfo ci) {
        ItemStack itemstack = blockEntity.getItem(4);
        if (blockEntity.fuel <= BrewingStandBlockEntity.FUEL_USES - 5 && itemstack.is(ModItems.FIRE_BEETLE_POWDER.get())) {
            blockEntity.fuel = Mth.clamp(blockEntity.fuel + 5, 0, BrewingStandBlockEntity.FUEL_USES);
            itemstack.shrink(1);
            setChanged(level, pos, state);
        }
    }

    @Inject(method = "canPlaceItem", at = @At("HEAD"), cancellable = true)
    private void spicedTwilight$fireBeetlePowderFuelSlot(int index, ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (index == 4
                && stack.is(ModItems.FIRE_BEETLE_POWDER.get())) {
            cir.setReturnValue(true);
        }
    }
}
