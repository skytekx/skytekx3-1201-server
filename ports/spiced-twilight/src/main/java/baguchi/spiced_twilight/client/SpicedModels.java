package baguchi.spiced_twilight.client;

import baguchi.spiced_twilight.api.HelmetCrabHideAccess;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import twilightforest.entity.monster.HelmetCrab;
import twilightforest.entity.monster.Kobold;

/**
 * Model logic shared by the mixins on the default and the Jappa-pack models of the fork. Upstream (4.8) has one model
 * class per mob with both layer definitions; the 1.20.1 fork keeps the Jappa models as separate classes.
 */
public final class SpicedModels {
    // part names of both helmet crab models: 4.8 / fork default (helmet_base > helmet, right_horn_1, left_horn_1) and Jappa (helmet > horns)
    private static final String[] HELMET_PARTS = {"helmet", "horns", "right_horn_1", "left_horn_1"};

    private SpicedModels() {
    }

    /**
     * Upstream HelmetCrabModelMixin#setAnim: while hiding only the helmet (shifted down by a pixel) is drawn.
     */
    public static void setupHelmetCrab(HierarchicalModel<HelmetCrab> model, HelmetCrab entity) {
        boolean hiding = ((HelmetCrabHideAccess) entity).spicedTwilight$isHiding();
        model.root().getAllParts().forEach(part -> part.skipDraw = hiding);
        for (String name : HELMET_PARTS) {
            model.getAnyDescendantWithName(name).ifPresent(part -> {
                part.resetPose();
                if (hiding) {
                    // backport: also keep the horn tips (children of the horn parts) visible
                    part.getAllParts().forEach(child -> child.skipDraw = false);
                    part.z += 1F;
                }
            });
        }
    }

    /**
     * Upstream KoboldModelMixin#setupAnim: lift the baby head so it sits on the halved body.
     * <p>
     * Backport: upstream calls {@code head.resetPose()}, which also wipes the head yaw/pitch set just before; only the
     * position is reset here so kobolds keep looking around.
     */
    public static void setupKoboldHead(ModelPart head, Kobold entity) {
        head.x = head.getInitialPose().x;
        head.y = head.getInitialPose().y;
        head.z = head.getInitialPose().z;
        if (entity.isBaby()) {
            head.y -= 4.0F;
        }
    }

    public static void translateToHead(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(0.8F, 0.8F, 0.8F);
    }

    public static void translateToChest(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(0.85F, 0.75F, 0.85F);
        poseStack.translate((4 / 16F), -(4 / 16F), (2 / 16F));
    }

    public static void translateToChestPat(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(0.75F, 0.6F, 0.75F);
    }

    public static void translateToLeg(ModelPart modelPart, PoseStack poseStack) {
        modelPart.translateAndRotate(poseStack);
        poseStack.scale(0.75F, 0.6F, 0.75F);
    }
}
