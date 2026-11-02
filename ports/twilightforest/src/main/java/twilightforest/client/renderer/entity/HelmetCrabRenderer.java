package twilightforest.client.renderer.entity;

import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.entity.monster.HelmetCrab;

public class HelmetCrabRenderer extends MobRenderer<HelmetCrab, HierarchicalModel<HelmetCrab>> {

	public static final ResourceLocation TEXTURE = TwilightForestMod.getModelTexture("helmetcrab.png");
	public static final ResourceLocation BLUE_TEXTURE = TwilightForestMod.getModelTexture("helmetcrabblue.png");

	public HelmetCrabRenderer(EntityRendererProvider.Context context, HierarchicalModel<HelmetCrab> model) {
		super(context, model, 0.625F);
	}

	// the models roll the crab out from under its helmet themselves, so skip the vanilla death flip
	@Override
	protected float getFlipDegrees(HelmetCrab entity) {
		return 0.0F;
	}

	@Override
	public ResourceLocation getTextureLocation(HelmetCrab entity) {
		return entity.isBlue() ? BLUE_TEXTURE : TEXTURE;
	}
}
