package baguchi.spiced_twilight.client.render.layer;

import baguchi.spiced_twilight.api.ISpicedArmor;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.ModelManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.DyeableLeatherItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraftforge.client.ForgeHooksClient;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;
import java.util.Map;

/**
 * Renders worn armor on models that are not humanoid-shaped by copying the vanilla armor model parts onto the
 * entity model's own parts ({@link ISpicedArmor}).
 * <p>
 * Port from My mod. Bagus Lib. 1.20.1 backport: armor textures, dye and trims come from the 1.20.1 armor API
 * ({@code ArmorMaterial#getName}, {@link DyeableLeatherItem} with an untinted {@code overlay} layer, NBT trims)
 * instead of the 1.21 armor material layers and data components.
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class CustomTFArmorLayer<T extends LivingEntity, M extends EntityModel<T> & ISpicedArmor<T>> extends RenderLayer<T, M> {
    private static final Map<String, ResourceLocation> ARMOR_LOCATION_CACHE = Maps.newHashMap();
    private final HumanoidModel defaultBipedModel;
    private final HumanoidModel innerModel;
    private final RenderLayerParent<T, M> renderer;
    private final TextureAtlas armorTrimAtlas;

    public CustomTFArmorLayer(RenderLayerParent<T, M> render, EntityRendererProvider.Context context) {
        super(render);
        this.defaultBipedModel = new HumanoidModel(context.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR));
        this.innerModel = new HumanoidModel(context.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR));
        this.renderer = render;
        this.armorTrimAtlas = context.getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    public CustomTFArmorLayer(RenderLayerParent<T, M> render, EntityModelSet modelSet, ModelManager modelManager) {
        super(render);
        this.defaultBipedModel = new HumanoidModel(modelSet.bakeLayer(ModelLayers.ARMOR_STAND_OUTER_ARMOR));
        this.innerModel = new HumanoidModel(modelSet.bakeLayer(ModelLayers.ARMOR_STAND_INNER_ARMOR));
        this.renderer = render;
        this.armorTrimAtlas = modelManager.getAtlas(Sheets.ARMOR_TRIMS_SHEET);
    }

    /**
     * [VanillaCopy] Forge's HumanoidArmorLayer#getArmorResource
     */
    public static ResourceLocation getArmorResource(Entity entity, ItemStack stack, EquipmentSlot slot, @Nullable String type) {
        ArmorItem item = (ArmorItem) stack.getItem();
        String texture = item.getMaterial().getName();
        String domain = "minecraft";
        int idx = texture.indexOf(':');
        if (idx != -1) {
            domain = texture.substring(0, idx);
            texture = texture.substring(idx + 1);
        }
        String s1 = String.format(Locale.ROOT, "%s:textures/models/armor/%s_layer_%d%s.png", domain, texture, (usesInnerModel(slot) ? 2 : 1), type == null ? "" : String.format(Locale.ROOT, "_%s", type));

        s1 = ForgeHooksClient.getArmorTexture(entity, stack, s1, slot, type);
        return ARMOR_LOCATION_CACHE.computeIfAbsent(s1, ResourceLocation::new);
    }

    @Override
    public void render(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        this.renderSlot(matrixStackIn, bufferIn, packedLightIn, entity, EquipmentSlot.HEAD);
        this.renderSlot(matrixStackIn, bufferIn, packedLightIn, entity, EquipmentSlot.CHEST);
        this.renderSlot(matrixStackIn, bufferIn, packedLightIn, entity, EquipmentSlot.LEGS);
        this.renderSlot(matrixStackIn, bufferIn, packedLightIn, entity, EquipmentSlot.FEET);
    }

    private void renderSlot(PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, T entity, EquipmentSlot slot) {
        matrixStackIn.pushPose();
        ItemStack item = entity.getItemBySlot(slot);
        if (item.getItem() instanceof ArmorItem armoritem) {
            boolean fits = slot == EquipmentSlot.HEAD ? item.canEquip(EquipmentSlot.HEAD, entity) : armoritem.getEquipmentSlot() == slot;
            if (fits) {
                HumanoidModel a = slot == EquipmentSlot.LEGS ? this.innerModel : this.defaultBipedModel;
                a = this.getArmorModelHook(entity, item, slot, a);
                this.setModelSlotVisible(a, slot);
                boolean glint = item.hasFoil();
                if (armoritem instanceof DyeableLeatherItem dyeable) {
                    int i = dyeable.getColor(item);
                    float r = (float) (i >> 16 & 255) / 255.0F;
                    float g = (float) (i >> 8 & 255) / 255.0F;
                    float b = (float) (i & 255) / 255.0F;
                    this.renderPiece(slot, item, entity, matrixStackIn, bufferIn, packedLightIn, glint, a, r, g, b, getArmorResource(entity, item, slot, null));
                    this.renderPiece(slot, item, entity, matrixStackIn, bufferIn, packedLightIn, glint, a, 1.0F, 1.0F, 1.0F, getArmorResource(entity, item, slot, "overlay"));
                } else {
                    this.renderPiece(slot, item, entity, matrixStackIn, bufferIn, packedLightIn, glint, a, 1.0F, 1.0F, 1.0F, getArmorResource(entity, item, slot, null));
                }
            }
        }
        matrixStackIn.popPose();
    }

    private void renderPiece(EquipmentSlot slot, ItemStack item, T entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean glintIn, HumanoidModel modelIn, float r, float g, float b, ResourceLocation armorResource) {
        switch (slot) {
            case HEAD -> this.renderHelmet(item, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, modelIn, r, g, b, armorResource);
            case CHEST -> this.renderChestplate(item, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, modelIn, r, g, b, armorResource);
            case LEGS -> this.renderLeg(item, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, modelIn, r, g, b, armorResource);
            case FEET -> this.renderBoot(item, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, modelIn, r, g, b, armorResource);
            default -> {
            }
        }
    }

    private static boolean usesInnerModel(EquipmentSlot p_117129_) {
        return p_117129_ == EquipmentSlot.LEGS;
    }

    private void renderTrim(ModelPart part, ArmorMaterial material, PoseStack poseStack, MultiBufferSource buffer, int packedLight, ArmorTrim trim, boolean glint, boolean innerTexture) {
        TextureAtlasSprite textureatlassprite = this.armorTrimAtlas.getSprite(innerTexture ? trim.innerTexture(material) : trim.outerTexture(material));
        VertexConsumer vertexconsumer = textureatlassprite.wrap(ItemRenderer.getFoilBufferDirect(buffer, Sheets.armorTrimsSheet(), true, glint));
        part.render(poseStack, vertexconsumer, packedLight, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderTrim(ModelPart part, ItemStack item, T entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean glintIn, EquipmentSlot equipmentSlot) {
        if (item.getItem() instanceof ArmorItem armorItem) {
            ArmorTrim.getTrim(entity.level().registryAccess(), item).ifPresent(armortrim ->
                    this.renderTrim(part, armorItem.getMaterial(), matrixStackIn, bufferIn, packedLightIn, armortrim, glintIn, usesInnerModel(equipmentSlot)));
        }
    }

    private void renderLeg(ItemStack legItem, T entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean glintIn, HumanoidModel modelIn, float r, float g, float b, ResourceLocation armorResource) {
        VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutoutNoCull(armorResource), false, glintIn);
        this.renderer.getModel().copyPropertiesTo(modelIn);
        modelIn.body.xRot = 0.0F;
        modelIn.body.yRot = 0.0F;
        modelIn.body.zRot = 0.0F;
        modelIn.body.x = 0.0F;
        modelIn.body.y = 0.0F;
        modelIn.body.z = 0.0F;
        modelIn.rightLeg.x = 0.0F;
        modelIn.rightLeg.xRot = 0.0F;
        modelIn.rightLeg.yRot = 0.0F;
        modelIn.rightLeg.zRot = 0.0F;
        modelIn.leftLeg.x = 0.0F;
        modelIn.leftLeg.xRot = 0.0F;
        modelIn.leftLeg.yRot = 0.0F;
        modelIn.leftLeg.zRot = 0.0F;
        modelIn.leftLeg.y = 0.0F;
        modelIn.rightLeg.y = 0.0F;
        modelIn.leftLeg.z = 0.0F;
        modelIn.rightLeg.z = 0.0F;
        this.renderer.getModel().rightLegPartArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToLeg(entity, part, matrixStackIn);
            modelIn.rightLeg.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.rightLeg, legItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.LEGS);
            matrixStackIn.popPose();
        });
        this.renderer.getModel().leftLegPartArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToLeg(entity, part, matrixStackIn);
            modelIn.leftLeg.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.leftLeg, legItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.LEGS);
            matrixStackIn.popPose();
        });
        this.renderer.getModel().bodyPartArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToChest(entity, part, matrixStackIn);
            modelIn.body.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.body, legItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.LEGS);
            matrixStackIn.popPose();
        });
    }

    private void renderBoot(ItemStack feetItem, T entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean glintIn, HumanoidModel modelIn, float r, float g, float b, ResourceLocation armorResource) {
        VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutoutNoCull(armorResource), false, glintIn);
        this.renderer.getModel().copyPropertiesTo(modelIn);
        modelIn.rightLeg.x = 0.0F;
        modelIn.rightLeg.xRot = 0.0F;
        modelIn.rightLeg.yRot = 0.0F;
        modelIn.rightLeg.zRot = 0.0F;
        modelIn.leftLeg.x = 0.0F;
        modelIn.leftLeg.xRot = 0.0F;
        modelIn.leftLeg.yRot = 0.0F;
        modelIn.leftLeg.zRot = 0.0F;
        modelIn.leftLeg.y = 0.0F;
        modelIn.rightLeg.y = 0.0F;
        modelIn.leftLeg.z = 0.0F;
        modelIn.rightLeg.z = 0.0F;
        this.renderer.getModel().rightLegPartArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToLeg(entity, part, matrixStackIn);
            modelIn.rightLeg.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.rightLeg, feetItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.FEET);
            matrixStackIn.popPose();
        });
        this.renderer.getModel().leftLegPartArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToLeg(entity, part, matrixStackIn);
            modelIn.leftLeg.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.leftLeg, feetItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.FEET);
            matrixStackIn.popPose();
        });
    }

    private void renderChestplate(ItemStack chestItem, T entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean glintIn, HumanoidModel modelIn, float r, float g, float b, ResourceLocation armorResource) {
        VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutoutNoCull(armorResource), false, glintIn);
        this.renderer.getModel().copyPropertiesTo(modelIn);
        modelIn.body.xRot = 0.0F;
        modelIn.body.yRot = 0.0F;
        modelIn.body.zRot = 0.0F;
        modelIn.body.x = 0.0F;
        modelIn.body.y = 0.0F;
        modelIn.body.z = 0.0F;
        modelIn.rightArm.x = 0.0F;
        modelIn.rightArm.y = 0.0F;
        modelIn.rightArm.z = 0.0F;
        modelIn.rightArm.xRot = 0.0F;
        modelIn.rightArm.yRot = 0.0F;
        modelIn.rightArm.zRot = 0.0F;
        modelIn.leftArm.x = 0.0F;
        modelIn.leftArm.y = 0.0F;
        modelIn.leftArm.z = 0.0F;
        modelIn.leftArm.xRot = 0.0F;
        modelIn.leftArm.yRot = 0.0F;
        modelIn.leftArm.zRot = 0.0F;
        this.renderer.getModel().rightHandArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToChestPat(entity, part, matrixStackIn);
            modelIn.rightArm.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.rightArm, chestItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.CHEST);
            matrixStackIn.popPose();
        });
        this.renderer.getModel().leftHandArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToChestPat(entity, part, matrixStackIn);
            modelIn.leftArm.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.leftArm, chestItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.CHEST);
            matrixStackIn.popPose();
        });
        this.renderer.getModel().bodyPartArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToChest(entity, part, matrixStackIn);
            modelIn.body.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.body, chestItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.CHEST);
            matrixStackIn.popPose();
        });
    }

    private void renderHelmet(ItemStack headItem, T entity, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, boolean glintIn, HumanoidModel modelIn, float r, float g, float b, ResourceLocation armorResource) {
        VertexConsumer ivertexbuilder = ItemRenderer.getFoilBuffer(bufferIn, RenderType.entityCutoutNoCull(armorResource), false, glintIn);
        this.renderer.getModel().copyPropertiesTo(modelIn);
        modelIn.head.xRot = 0.0F;
        modelIn.head.yRot = 0.0F;
        modelIn.head.zRot = 0.0F;
        modelIn.hat.xRot = 0.0F;
        modelIn.hat.yRot = 0.0F;
        modelIn.hat.zRot = 0.0F;
        modelIn.head.x = 0.0F;
        modelIn.head.y = 0.0F;
        modelIn.head.z = 0.0F;
        modelIn.hat.x = 0.0F;
        modelIn.hat.y = 0.0F;
        modelIn.hat.z = 0.0F;
        this.renderer.getModel().headPartArmors().forEach((part) -> {
            matrixStackIn.pushPose();
            this.renderer.getModel().translateToHead(entity, part, matrixStackIn);
            modelIn.head.render(matrixStackIn, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, r, g, b, 1.0F);
            this.renderTrim(modelIn.head, headItem, entity, matrixStackIn, bufferIn, packedLightIn, glintIn, EquipmentSlot.HEAD);
            matrixStackIn.popPose();
        });
    }

    protected void setModelSlotVisible(HumanoidModel p_188359_1_, EquipmentSlot slotIn) {
        this.setModelVisible(p_188359_1_);
        switch (slotIn) {
            case HEAD:
                p_188359_1_.head.visible = true;
                p_188359_1_.hat.visible = true;
                break;
            case CHEST:
                p_188359_1_.body.visible = true;
                p_188359_1_.rightArm.visible = true;
                p_188359_1_.leftArm.visible = true;
                break;
            case LEGS:
                p_188359_1_.body.visible = true;
                p_188359_1_.rightLeg.visible = true;
                p_188359_1_.leftLeg.visible = true;
                break;
            case FEET:
                p_188359_1_.rightLeg.visible = true;
                p_188359_1_.leftLeg.visible = true;
        }

    }

    protected void setModelVisible(HumanoidModel model) {
        model.setAllVisible(false);
    }

    protected HumanoidModel<?> getArmorModelHook(LivingEntity entity, ItemStack itemStack, EquipmentSlot slot, HumanoidModel model) {
        Model basicModel = ForgeHooksClient.getArmorModel(entity, itemStack, slot, model);
        return basicModel instanceof HumanoidModel ? (HumanoidModel) basicModel : model;
    }
}
