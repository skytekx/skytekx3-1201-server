package twilightforest.client;

import com.google.common.base.Suppliers;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.Material;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SkullBlockEntity;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.client.model.data.ModelData;
import net.minecraftforge.registries.RegistryObject;
import org.apache.commons.lang3.StringUtils;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.block.AbstractSkullCandleBlock;
import twilightforest.block.AbstractTrophyBlock;
import twilightforest.block.JarBlock;
import twilightforest.block.KeepsakeCasketBlock;
import twilightforest.block.MasonJarBlock;
import twilightforest.block.SkullChestBlock;
import twilightforest.block.TFChestBlock;
import twilightforest.block.TFTrappedChestBlock;
import twilightforest.block.entity.JarBlockEntity;
import twilightforest.block.entity.KeepsakeCasketBlockEntity;
import twilightforest.block.entity.MasonJarBlockEntity;
import twilightforest.block.entity.SkullChestBlockEntity;
import twilightforest.block.entity.TFTrappedChestBlockEntity;
import twilightforest.block.entity.TwilightChestEntity;
import twilightforest.client.model.TFModelLayers;
import twilightforest.client.model.entity.KnightmetalShieldModel;
import twilightforest.client.model.tileentity.GenericTrophyModel;
import twilightforest.client.renderer.tileentity.JarRenderer;
import twilightforest.client.renderer.tileentity.SkullCandleTileEntityRenderer;
import twilightforest.client.renderer.tileentity.SkullChestRenderer;
import twilightforest.client.renderer.tileentity.TrophyTileEntityRenderer;
import twilightforest.enums.BossVariant;
import twilightforest.init.TFBlocks;
import twilightforest.init.TFItemDisplayContexts;
import twilightforest.item.KeepsakeCasketItem;
import twilightforest.item.KnightmetalShieldItem;
import twilightforest.item.MysticCrownItem;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ISTER extends BlockEntityWithoutLevelRenderer {
	public static final Supplier<ISTER> INSTANCE = Suppliers.memoize(ISTER::new);
	public static final IClientItemExtensions CLIENT_ITEM_EXTENSION = Util.make(() -> new IClientItemExtensions() {
		@Override
		public BlockEntityWithoutLevelRenderer getCustomRenderer() {
			return INSTANCE.get();
		}
	});

	private final SkullChestBlockEntity skullChest = new SkullChestBlockEntity(BlockPos.ZERO, TFBlocks.SKULL_CHEST.get().defaultBlockState());
	private final KeepsakeCasketBlockEntity keepsakeCasket = new KeepsakeCasketBlockEntity(BlockPos.ZERO, TFBlocks.KEEPSAKE_CASKET.get().defaultBlockState());
	private final Map<Block, TwilightChestEntity> chestEntities = Util.make(new HashMap<>(), map -> {
		makeInstance(map, TFBlocks.TWILIGHT_OAK_CHEST);
		makeInstance(map, TFBlocks.CANOPY_CHEST);
		makeInstance(map, TFBlocks.MANGROVE_CHEST);
		makeInstance(map, TFBlocks.DARK_CHEST);
		makeInstance(map, TFBlocks.TIME_CHEST);
		makeInstance(map, TFBlocks.TRANSFORMATION_CHEST);
		makeInstance(map, TFBlocks.MINING_CHEST);
		makeInstance(map, TFBlocks.SORTING_CHEST);
	});
	private final Map<Block, TFTrappedChestBlockEntity> trappedChestEntities = Util.make(new HashMap<>(), map -> {
		makeTrappedInstance(map, TFBlocks.TWILIGHT_OAK_TRAPPED_CHEST);
		makeTrappedInstance(map, TFBlocks.CANOPY_TRAPPED_CHEST);
		makeTrappedInstance(map, TFBlocks.MANGROVE_TRAPPED_CHEST);
		makeTrappedInstance(map, TFBlocks.DARK_TRAPPED_CHEST);
		makeTrappedInstance(map, TFBlocks.TIME_TRAPPED_CHEST);
		makeTrappedInstance(map, TFBlocks.TRANSFORMATION_TRAPPED_CHEST);
		makeTrappedInstance(map, TFBlocks.MINING_TRAPPED_CHEST);
		makeTrappedInstance(map, TFBlocks.SORTING_TRAPPED_CHEST);
	});
	private static final ResourceLocation LICH_TEXTURE = TwilightForestMod.getModelTexture("twilightlich64.png");

	private KnightmetalShieldModel shield = new KnightmetalShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.KNIGHTMETAL_SHIELD));
	// The Mystic Crown is the Lich's own crown ("hat" layer of the Lich model)
	private ModelPart crown = Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.LICH).getChild("hat");
	private Map<BossVariant, GenericTrophyModel> trophies = TrophyTileEntityRenderer.createTrophyRenderers(Minecraft.getInstance().getEntityModels());
	private Map<SkullBlock.Type, SkullModelBase> skulls = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels());
	// Dummy block entities used to look up the renderer for EntityBlock items (critters etc.), created once per block instead of every frame
	private final Map<Block, BlockEntity> itemBlockEntities = new HashMap<>();

	// Use the cached INSTANCE.get instead
	private ISTER() {
		super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
	}

	@Override
	public void onResourceManagerReload(ResourceManager manager) {
		this.shield = new KnightmetalShieldModel(Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.KNIGHTMETAL_SHIELD));
		this.crown = Minecraft.getInstance().getEntityModels().bakeLayer(TFModelLayers.LICH).getChild("hat");
		this.trophies = TrophyTileEntityRenderer.createTrophyRenderers(Minecraft.getInstance().getEntityModels());
		this.skulls = SkullBlockRenderer.createSkullRenderers(Minecraft.getInstance().getEntityModels());

		TwilightForestMod.LOGGER.debug("Reloaded ISTER!");
	}

	@Override
	public void renderByItem(ItemStack stack, ItemDisplayContext camera, PoseStack ms, MultiBufferSource buffers, int light, int overlay) {
		Item item = stack.getItem();
		if (item instanceof BlockItem blockItem) {
			Block block = blockItem.getBlock();
			if (block instanceof AbstractTrophyBlock trophyBlock) {
				BossVariant variant = trophyBlock.getVariant();
				GenericTrophyModel trophy = this.trophies.get(variant);

				if (camera == ItemDisplayContext.GUI) {
					ModelResourceLocation back = new ModelResourceLocation(TwilightForestMod.prefix(((AbstractTrophyBlock) block).getVariant().getTrophyType().getModelName()), "inventory");
					BakedModel modelBack = Minecraft.getInstance().getItemRenderer().getItemModelShaper().getModelManager().getModel(back);

					Lighting.setupForFlatItems();
					MultiBufferSource.BufferSource bufferSource = Minecraft.getInstance().renderBuffers().bufferSource();
					ms.pushPose();
					Lighting.setupForFlatItems();
					ms.translate(0.5F, 0.5F, -1.5F);
					Minecraft.getInstance().getItemRenderer().render(TrophyTileEntityRenderer.stack, ItemDisplayContext.GUI, false, ms, bufferSource, 15728880, OverlayTexture.NO_OVERLAY, modelBack.applyTransform(camera, ms, false));
					ms.popPose();
					bufferSource.endBatch();
					Lighting.setupFor3DItems();

					ms.pushPose();
					ms.translate(0.5F, 0.5F, 0.5F);
					if (trophyBlock.getVariant() == BossVariant.HYDRA || trophyBlock.getVariant() == BossVariant.QUEST_RAM)
						ms.scale(0.9F, 0.9F, 0.9F);
					ms.mulPose(Axis.XP.rotationDegrees(30));
					ms.mulPose(Axis.YN.rotationDegrees(TFConfig.CLIENT_CONFIG.rotateTrophyHeadsGui.get() && !Minecraft.getInstance().isPaused() ? TFClientEvents.rotationTicker : -45));
					ms.translate(-0.5F, -0.5F, -0.5F);
					ms.translate(0.0F, 0.25F, 0.0F);
					if (trophyBlock.getVariant() == BossVariant.UR_GHAST) ms.translate(0.0F, 0.5F, 0.0F);
					if (trophyBlock.getVariant() == BossVariant.ALPHA_YETI) ms.translate(0.0F, -0.15F, 0.0F);
					TrophyTileEntityRenderer.render(null, 180.0F, trophy, variant, !Minecraft.getInstance().isPaused() ? TFClientEvents.time + Minecraft.getInstance().getDeltaFrameTime() : 0, ms, buffers, light, camera);
					ms.popPose();
				} else {
					TrophyTileEntityRenderer.render(null, 180.0F, trophy, variant, !Minecraft.getInstance().isPaused() ? TFClientEvents.time + Minecraft.getInstance().getDeltaFrameTime() : 0, ms, buffers, light, camera);
				}

			} else if (block instanceof KeepsakeCasketBlock) {
				//the item shows the casket's damage
				int damage = KeepsakeCasketItem.getCasketDamage(stack);
				if (Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(this.keepsakeCasket) instanceof SkullChestRenderer<?> renderer) {
					renderer.renderCasket(0.0F, ms, buffers, light, overlay, renderer.getTextureLocation(damage), Direction.NORTH);
				}
			} else if (block instanceof SkullChestBlock) {
				if (Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(this.skullChest) instanceof SkullChestRenderer<?> renderer) {
					renderer.renderCasket(0.0F, ms, buffers, light, overlay, renderer.getTextureLocation(0), Direction.NORTH);
				}
			} else if (block instanceof TFChestBlock) {
				Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(this.chestEntities.get(block), ms, buffers, light, overlay);
			} else if (block instanceof TFTrappedChestBlock) {
				Minecraft.getInstance().getBlockEntityRenderDispatcher().renderItem(this.trappedChestEntities.get(block), ms, buffers, light, overlay);
			} else if (block instanceof AbstractSkullCandleBlock candleBlock) {
				GameProfile gameprofile = null;
				if (stack.hasTag()) {
					CompoundTag compoundtag = stack.getTag();
					if (compoundtag.contains("SkullOwner", 10)) {
						gameprofile = NbtUtils.readGameProfile(compoundtag.getCompound("SkullOwner"));
					} else if (compoundtag.contains("SkullOwner", 8) && !StringUtils.isBlank(compoundtag.getString("SkullOwner"))) {
						gameprofile = new GameProfile(null, compoundtag.getString("SkullOwner"));
						compoundtag.remove("SkullOwner");
						SkullBlockEntity.updateGameprofile(gameprofile, (p_172560_) ->
								compoundtag.put("SkullOwner", NbtUtils.writeGameProfile(new CompoundTag(), p_172560_)));
					}
				}

				SkullBlock.Type type = candleBlock.getType();
				SkullModelBase base = this.skulls.get(type);
				RenderType renderType = SkullCandleTileEntityRenderer.getRenderType(type, gameprofile);
				SkullCandleTileEntityRenderer.renderSkull(null, 180.0F, 0.0F, ms, buffers, light, base, renderType);

				//we put the candle
				ms.translate(0.0F, 0.5F, 0.0F);
				CompoundTag tag = stack.getTagElement("BlockEntityTag");
				if (tag != null && tag.contains("CandleColor") && tag.contains("CandleAmount")) {
					if (tag.getInt("CandleAmount") <= 0) tag.putInt("CandleAmount", 1);
					Minecraft.getInstance().getBlockRenderer().renderSingleBlock(
							AbstractSkullCandleBlock.candleColorToCandle(AbstractSkullCandleBlock.CandleColors.colorFromInt(tag.getInt("CandleColor")))
									.defaultBlockState().setValue(CandleBlock.CANDLES, tag.getInt("CandleAmount")), ms, buffers, light, OverlayTexture.NO_OVERLAY, ModelData.EMPTY, RenderType.cutout());
				}
			} else if (block instanceof JarBlock jarBlock) {
				Minecraft minecraft = Minecraft.getInstance();
				JarRenderer.renderJarModel(block.defaultBlockState(), minecraft.getBlockRenderer(), ms, buffers, light, overlay);
				Item jarLid = JarBlockEntity.getLid(stack);
				Item lid = jarLid == null || !JarRenderer.LIDS.containsKey(jarLid) ? jarBlock.getDefaultLid() : jarLid;
				BakedModel lidModel = JarRenderer.LIDS.get(lid);
				if (lidModel != null) JarRenderer.renderModel(lidModel, block.defaultBlockState(), minecraft.getBlockRenderer(), ms, buffers, light, overlay);

				if (jarBlock instanceof MasonJarBlock) {
					ItemStack contents = MasonJarBlockEntity.getContents(stack);
					if (!contents.isEmpty()) {
						MultiBufferSource.BufferSource bufferSource = minecraft.renderBuffers().bufferSource();
						ms.pushPose();
						ms.translate(0.5D, 0.4375D, 0.5D);
						ms.scale(0.5F, 0.5F, 0.5F);
						minecraft.getItemRenderer().render(contents, TFItemDisplayContexts.JARRED, false, ms, bufferSource, light, OverlayTexture.NO_OVERLAY, minecraft.getItemRenderer().getModel(contents, null, null, 1));
						ms.popPose();
						bufferSource.endBatch();
					}
				}
			} else {
				if (block instanceof EntityBlock be) {
					BlockEntity blockEntity = this.itemBlockEntities.computeIfAbsent(block, b -> be.newBlockEntity(BlockPos.ZERO, b.defaultBlockState()));
					if (blockEntity != null) {
						BlockEntityRenderer<BlockEntity> renderer = Minecraft.getInstance().getBlockEntityRenderDispatcher().getRenderer(blockEntity);
						if (renderer != null) {
							renderer.render(null, 0, ms, buffers, light, overlay);
						}
					}
				}
			}
		} else if (item instanceof KnightmetalShieldItem) {
			ms.pushPose();
			ms.scale(1.0F, -1.0F, -1.0F);
			Material material = new Material(Sheets.SHIELD_SHEET, new ResourceLocation(TwilightForestMod.ID, "model/knightmetal_shield"));
			VertexConsumer vertexconsumer = material.sprite().wrap(ItemRenderer.getFoilBufferDirect(buffers, this.shield.renderType(material.atlasLocation()), true, stack.hasFoil()));
			this.shield.renderToBuffer(ms, vertexconsumer, light, overlay, 1.0F, 1.0F, 1.0F, 1.0F);
			ms.popPose();
		} else if (item instanceof MysticCrownItem) {
			this.crown.xRot = 0.0F;
			this.crown.yRot = 0.0F;
			ms.pushPose();
			ms.scale(1.0F, -1.0F, -1.0F);
			if (camera == ItemDisplayContext.GUI) {
				ms.translate(0.5F, -0.1F, 0.0F);
				ms.last().normal().rotate(Mth.PI * 0.5F, 0.0F, 1.0F, 0.0F);
				ms.mulPose(Axis.XP.rotationDegrees(30));
				ms.mulPose(Axis.YP.rotationDegrees(45));
			} else {
				ms.translate(0.5F, 0.0F, -0.5F);
				ms.scale(1.05F, 1.05F, 1.05F);
			}
			this.crown.render(ms, buffers.getBuffer(RenderType.entityCutoutNoCull(LICH_TEXTURE)), light, overlay);
			ms.popPose();
		}
	}

	public static void makeInstance(Map<Block, TwilightChestEntity> map, RegistryObject<? extends ChestBlock> registryObject) {
		ChestBlock block = registryObject.get();
		map.put(block, new TwilightChestEntity(BlockPos.ZERO, block.defaultBlockState()));
	}

	public static void makeTrappedInstance(Map<Block, TFTrappedChestBlockEntity> map, RegistryObject<? extends ChestBlock> registryObject) {
		ChestBlock block = registryObject.get();
		map.put(block, new TFTrappedChestBlockEntity(BlockPos.ZERO, block.defaultBlockState()));
	}
}
