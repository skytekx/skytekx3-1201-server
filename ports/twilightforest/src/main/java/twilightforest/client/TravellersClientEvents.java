package twilightforest.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.armor.TFArmorModel;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.util.ArmorUtil;

/**
 * Client events of the Traveller's Gear core. Backport of the core part of 4.8's TravellersClientEvents; the key and
 * movement handlers of the modifiers come with the travellers-* modifier features.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT)
public class TravellersClientEvents {
	private static final ResourceLocation GLOVE_TEXTURE = TwilightForestMod.prefix("textures/models/armor/travellers_layer_1.png");

	/**
	 * Draws the Traveller's Gloves over the first-person arm, the same way PlayerRenderer#renderHand draws the sleeve.
	 */
	@SubscribeEvent
	public static void renderGlovesInFirstPerson(RenderArmEvent event) {
		if (!TFConfig.CLIENT_CONFIG.firstPersonGloveOverlay.get())
			return;

		AbstractClientPlayer player = event.getPlayer();
		ItemStack chestStack = player.getItemBySlot(EquipmentSlot.CHEST);
		if (!TravellersArmorItem.hasGloves(chestStack) || ArmorUtil.hasEmperorsCloth(chestStack))
			return;

		EntityRenderer<? super AbstractClientPlayer> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
		if (!(renderer instanceof PlayerRenderer playerRenderer) || !(IClientItemExtensions.of(chestStack).getHumanoidArmorModel(player, chestStack, EquipmentSlot.CHEST, playerRenderer.getModel()) instanceof TFArmorModel model))
			return;

		ModelPart armPart = event.getArm() == HumanoidArm.RIGHT ? model.rightArm : model.leftArm;
		model.attackTime = 0.0F;
		model.crouching = false;
		model.swimAmount = 0.0F;
		model.setupAnim(player, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F);
		armPart.visible = true;
		armPart.xRot = 0.0F;
		armPart.render(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.armorCutoutNoCull(GLOVE_TEXTURE)), event.getPackedLight(), OverlayTexture.NO_OVERLAY);
	}
}
