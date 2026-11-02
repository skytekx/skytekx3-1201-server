package baguchi.spiced_twilight.api;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.Entity;

public interface ISpicedArmor<T extends Entity> {
    void translateToHead(T entity, ModelPart var1, PoseStack var2);

    void translateToChest(T entity, ModelPart var1, PoseStack var2);

    void translateToLeg(T entity, ModelPart var1, PoseStack var2);

    void translateToChestPat(T entity, ModelPart var1, PoseStack var2);

    default Iterable<ModelPart> rightHandArmors() {
        return ImmutableList.of();
    }

    default Iterable<ModelPart> leftHandArmors() {
        return ImmutableList.of();
    }

    default Iterable<ModelPart> rightLegPartArmors() {
        return ImmutableList.of();
    }

    default Iterable<ModelPart> leftLegPartArmors() {
        return ImmutableList.of();
    }

    default Iterable<ModelPart> bodyPartArmors() {
        return ImmutableList.of();
    }

    default Iterable<ModelPart> headPartArmors() {
        return ImmutableList.of();
    }
}
