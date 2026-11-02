package twilightforest.client.renderer.entity.newmodels;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.newmodels.NewHydraHeadModel;
import twilightforest.client.renderer.entity.HydraHeadRenderer;
import twilightforest.client.renderer.entity.TFPartRenderer;
import twilightforest.entity.boss.Hydra;
import twilightforest.entity.boss.HydraHead;
import twilightforest.entity.boss.HydraHeadContainer;

import org.jetbrains.annotations.Nullable;

public class NewHydraHeadRenderer extends TFPartRenderer<HydraHead, NewHydraHeadModel> {

	private static final ResourceLocation textureLoc = TwilightForestMod.getModelTexture("hydra4.png");


	public NewHydraHeadRenderer(EntityRendererProvider.Context manager) {
		super(manager, new NewHydraHeadModel(manager.bakeLayer(TFModelLayers.HYDRA_HEAD)));
	}

	@Override
	public void render(HydraHead entity, float yaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int light) {
		// get the HydraHeadContainer that we're taking about
		HydraHeadContainer headCon = getHeadObject(entity);

		if (headCon != null) {
			// see whether we want to render these
			if (entity.isActive()) {
				stack.mulPose(Axis.YP.rotationDegrees(-180));
				super.render(entity, yaw, partialTicks, stack, buffer, light);
			}

		} else {
			super.render(entity, yaw, partialTicks, stack, buffer, light);
		}
	}

	@Override
	protected boolean shouldShowName(HydraHead head) {
		return HydraHeadRenderer.shouldShowHeadName(head);
	}

	@Override
	protected void renderNameTag(HydraHead head, Component component, PoseStack stack, MultiBufferSource source, int light) {
		HydraHeadRenderer.renderHeadNameTag(this.entityRenderDispatcher, this.getFont(), head, component, stack, source, light);
	}

	@Nullable
	public static HydraHeadContainer getHeadObject(HydraHead entity) {
		Hydra hydra = entity.getParent();

		if (hydra != null) {
			for (int i = 0; i < hydra.numHeads; i++) {
				if (hydra.hc[i].headEntity == entity) {
					return hydra.hc[i];
				}
			}
		}
		return null;
	}

	@Override
	public ResourceLocation getTextureLocation(HydraHead entity) {
		return textureLoc;
	}
}
