package twilightforest.client.renderer.armor;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.ForgeHooksClient;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.armor.TFArmorModel;
import twilightforest.client.model.armor.TravellersWingsModel;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;

/**
 * Armor models of the Traveller's Gear. Each slot has its own layer: goggles, vest and gloves (with a slim-arm variant
 * so the gloves fit Alex-style skins, 4.8 b23c59d1e), belt and wings, boots. Parts the worn piece does not have are
 * skipped, so the chest layer draws the vest body and/or the gloves and the leg layer the belt and/or the wings.
 * Backport of 4.8's TravellersArmorItem.ArmorRender.
 */
public final class TravellersArmorRenderer extends TFArmorRenderer {
	public static final TravellersArmorRenderer INSTANCE = new TravellersArmorRenderer();

	private TravellersArmorRenderer() {
	}

	@Override
	public HumanoidModel<?> getHumanoidArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> model) {
		ModelPart root = switch (slot) {
			case HEAD -> this.getModelPart(TFModelLayers.TRAVELLERS_ARMOR_HELMET);
			case CHEST -> {
				ModelPart chestLayer = this.getModelPart(isModelSlim(living) ? TFModelLayers.TRAVELLERS_ARMOR_CHEST_GLOVES_SLIM : TFModelLayers.TRAVELLERS_ARMOR_CHEST_GLOVES);
				chestLayer.getAllParts().forEach(part -> part.skipDraw = true);
				boolean hasChestplate = TravellersArmorItem.hasPart(stack, TravellersArmorItem.Part.CHESTPLATE);
				boolean hasGloves = TravellersArmorItem.hasGloves(stack);
				chestLayer.getChild("body").skipDraw = !hasChestplate;
				chestLayer.getChild("left_arm").skipDraw = !hasGloves;
				chestLayer.getChild("right_arm").skipDraw = !hasGloves;

				yield chestLayer;
			}
			case LEGS -> {
				ModelPart leggingsLayer = this.getModelPart(TFModelLayers.TRAVELLERS_ARMOR_LEGGINGS);
				leggingsLayer.getAllParts().forEach(part -> part.skipDraw = true);
				boolean hasWings = TravellersArmorItem.hasPart(stack, TravellersArmorItem.Part.WINGS);
				// wings with the Swap Hotbar modifier carry a belt as well
				boolean hasBelt = TravellersArmorItem.hasPart(stack, TravellersArmorItem.Part.BELT) || TravellersModifiersManager.hasTravellersModifier(living.level().registryAccess(), stack, TravellersModifiersManager.SWAP_HOTBAR_MODIFIER);

				TravellersWingsModel.skipBelt(leggingsLayer, !hasBelt);
				TravellersWingsModel.skipWings(leggingsLayer, !hasWings);

				yield leggingsLayer;
			}
			case FEET -> this.getModelPart(TFModelLayers.TRAVELLERS_ARMOR_BOOTS);
			default -> throw new IllegalArgumentException("Unexpected slot: " + slot + ": " + stack + ". Please report to https://github.com/TeamTwilight/twilightforest/issues");
		};

		if (slot == EquipmentSlot.LEGS)
			return new TravellersWingsModel(root);
		return new TFArmorModel(root);
	}

	/**
	 * [VanillaCopy] of the default, plus 4.8's NeoForge-only setupModelAnimations step: the wings are animated after
	 * Forge has copied the wearer's pose onto the leg layer.
	 */
	@Override
	public Model getGenericArmorModel(LivingEntity living, ItemStack stack, EquipmentSlot slot, HumanoidModel<?> original) {
		HumanoidModel<?> replacement = this.getHumanoidArmorModel(living, stack, slot, original);
		ForgeHooksClient.copyModelProperties(original, replacement);
		if (replacement instanceof TravellersWingsModel wingsModel)
			wingsModel.setupWingAnimations(living, living.tickCount + Minecraft.getInstance().getFrameTime());
		return replacement;
	}

	private static boolean isModelSlim(LivingEntity entity) {
		return entity instanceof AbstractClientPlayer player && "slim".equals(player.getModelName());
	}
}
