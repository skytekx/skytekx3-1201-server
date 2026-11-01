package twilightforest.client.renderer.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

/**
 * Armor renderer with an inner (leggings) and an outer (everything else) layer, both baked once and cached.
 */
public class TFSimpleArmorRenderer extends TFArmorRenderer {
	private final Function<ModelPart, ? extends HumanoidModel<?>> createModelInstance;
	private final ModelLayerLocation innerArmorModel;
	private final ModelLayerLocation outerArmorModel;

	public TFSimpleArmorRenderer(Function<ModelPart, ? extends HumanoidModel<?>> createModelInstance, ModelLayerLocation innerLayerLocation, ModelLayerLocation outerLayerLocation) {
		this.createModelInstance = createModelInstance;
		this.innerArmorModel = innerLayerLocation;
		this.outerArmorModel = outerLayerLocation;
	}

	@Override
	public @NotNull HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> model) {
		return this.createModelInstance.apply(this.getModelPart(slot == EquipmentSlot.LEGS ? this.innerArmorModel : this.outerArmorModel));
	}
}
