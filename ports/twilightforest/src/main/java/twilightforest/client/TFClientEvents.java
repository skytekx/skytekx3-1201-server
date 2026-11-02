package twilightforest.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.WrittenBookItem;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.*;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import twilightforest.FoliageColorHandler;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.block.GiantBlock;
import twilightforest.block.MiniatureStructureBlock;
import twilightforest.block.entity.GrowingBeanstalkBlockEntity;
import twilightforest.client.model.block.ReactorDebrisModel;
import twilightforest.client.model.block.carpet.RoyalRagsModelLoader;
import twilightforest.client.model.block.connected.ConnectedTextureModelLoader;
import twilightforest.client.model.block.forcefield.ForceFieldModelLoader;
import twilightforest.client.model.block.giantblock.GiantBlockModelLoader;
import twilightforest.client.model.block.leaves.BakedLeavesModel;
import twilightforest.client.model.block.patch.PatchModelLoader;
import twilightforest.client.model.item.TrollsteinnModel;
import twilightforest.client.renderer.TFSkyRenderer;
import twilightforest.client.renderer.entity.MagicPaintingRenderer;
import twilightforest.client.renderer.entity.ShieldLayer;
import twilightforest.client.renderer.tileentity.JarRenderer;
import twilightforest.client.renderer.tooltip.PotionFlaskTooltipComponent;
import twilightforest.compat.curios.CuriosCompat;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.entity.boss.bar.ClientTFBossBar;
import twilightforest.events.HostileMountEvents;
import twilightforest.init.TFItems;
import twilightforest.init.TFSounds;
import twilightforest.item.*;
import twilightforest.network.TFPacketHandler;
import twilightforest.network.WipeOreMeterPacket;
import twilightforest.util.ArmorUtil;
import twilightforest.world.registration.TFGenerationSettings;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT)
public class TFClientEvents {

	@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD)
	public static class ModBusEvents {
		@SubscribeEvent
		public static void registerLoaders(ModelEvent.RegisterGeometryLoaders event) {
			event.register("patch", PatchModelLoader.INSTANCE);
			event.register("giant_block", GiantBlockModelLoader.INSTANCE);
			event.register("force_field", ForceFieldModelLoader.INSTANCE);
			event.register("connected_texture_block", ConnectedTextureModelLoader.INSTANCE);
			event.register("royal_rags", RoyalRagsModelLoader.INSTANCE);
		}

		@SubscribeEvent
		public static void modelBake(ModelEvent.ModifyBakingResult event) {
			TFItems.addItemModelProperties();

			List<Map.Entry<ResourceLocation, BakedModel>> models =  event.getModels().entrySet().stream()
					.filter(entry -> entry.getKey().getNamespace().equals(TwilightForestMod.ID) && ((entry.getKey().getPath().contains("leaves") && !entry.getKey().getPath().contains("dark")) || entry.getKey().getPath().contains("_bush"))).toList();

			models.forEach(entry -> event.getModels().put(entry.getKey(), new BakedLeavesModel(entry.getValue())));

			// the Trollsteinn item shows its lit texture while in light
			ModelResourceLocation trollsteinn = new ModelResourceLocation(TwilightForestMod.prefix("trollsteinn"), "inventory");
			BakedModel trollsteinnModel = event.getModels().get(trollsteinn);
			if (trollsteinnModel != null) {
				event.getModels().put(trollsteinn, new TrollsteinnModel(trollsteinnModel));
			}

			// reactor debris has no rendered block model; the wrapper only picks its break particle texture
			BakedModel defaultReactorDebrisModel = event.getModels().get(ModelResourceLocation.vanilla("netherrack", ""));
			if (defaultReactorDebrisModel != null) {
				event.getModels().put(new ModelResourceLocation(TwilightForestMod.prefix("reactor_debris"), ""), new ReactorDebrisModel(defaultReactorDebrisModel));
			}
		}

		@SubscribeEvent
		public static void registerModels(ModelEvent.RegisterAdditional event) {
			event.register(ShieldLayer.LOC);
			event.register(new ModelResourceLocation(TwilightForestMod.prefix("trophy"), "inventory"));
			event.register(new ModelResourceLocation(TwilightForestMod.prefix("trophy_minor"), "inventory"));
			event.register(new ModelResourceLocation(TwilightForestMod.prefix("trophy_quest"), "inventory"));
			event.register(TrollsteinnModel.LIT_TROLLSTEINN);

			event.register(TwilightForestMod.prefix("block/casket_obsidian"));
			event.register(TwilightForestMod.prefix("block/casket_stone"));
			event.register(TwilightForestMod.prefix("block/casket_basalt"));

			for (JarRenderer.LidResource lid : JarRenderer.LID_LOCATION_LIST.get()) {
				event.register(lid.modelLocation());
			}
		}

		@SubscribeEvent
		public static void cacheJarLids(ModelEvent.BakingCompleted event) {
			JarRenderer.LIDS.clear();
			for (JarRenderer.LidResource lid : JarRenderer.LID_LOCATION_LIST.get()) {
				BakedModel model = event.getModels().get(lid.modelLocation());
				if (model != null) JarRenderer.LIDS.put(lid.lid(), model);
			}
		}

		@SubscribeEvent
		public static void registerTooltipComponents(RegisterClientTooltipComponentFactoriesEvent event) {
			event.register(BrittleFlaskItem.Tooltip.class, PotionFlaskTooltipComponent::new);
		}

		@SubscribeEvent
		public static void registerDimEffects(RegisterDimensionSpecialEffectsEvent event) {
			TFSkyRenderer.createStars();
			event.register(TwilightForestMod.prefix("renderer"), new TwilightForestRenderInfo(128.0F, false, DimensionSpecialEffects.SkyType.NONE, false, false));
		}
	}

	/**
	 * Drops the cached foliage colour handlers when anything leaves a client level, which covers leaving a world and
	 * switching dimensions or servers, so handlers resolved against an old biome registry are not reused (#2361).
	 */
	@SubscribeEvent
	public static void clearFoliageColorCache(EntityLeaveLevelEvent event) {
		if (event.getLevel().isClientSide()) {
			FoliageColorHandler.clearCache();
		}
	}

	/**
	 * Stop the game from rendering the mount health for unfriendly creatures
	 */
	@SubscribeEvent
	public static void preOverlay(RenderGuiOverlayEvent.Pre event) {
		if (event.getOverlay().id() == VanillaGuiOverlay.MOUNT_HEALTH.id()) {
			if (HostileMountEvents.isRidingUnfriendly(Minecraft.getInstance().player)) {
				event.setCanceled(true);
			}
		}
	}

	/**
	 * Render the aurora
	 */
	@SubscribeEvent
	public static void renderWorldLast(RenderLevelStageEvent event) {
		if (Minecraft.getInstance().level == null) return;

		if (event.getStage() == RenderLevelStageEvent.Stage.AFTER_WEATHER && (aurora > 0 || lastAurora > 0) && TFShaders.AURORA != null) {
			BufferBuilder buffer = Tesselator.getInstance().getBuilder();
			buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);

			final double scale = 2048F * (Minecraft.getInstance().gameRenderer.getRenderDistance() / 32F);
			Vec3 pos = event.getCamera().getPosition();
			double y = 256D - pos.y();
			buffer.vertex(-scale, y, scale).color(1F, 1F, 1F, 1F).endVertex();
			buffer.vertex(-scale, y, -scale).color(1F, 1F, 1F, 1F).endVertex();
			buffer.vertex(scale, y, -scale).color(1F, 1F, 1F, 1F).endVertex();
			buffer.vertex(scale, y, scale).color(1F, 1F, 1F, 1F).endVertex();

			RenderSystem.enableBlend();
			RenderSystem.enableDepthTest();
			RenderSystem.setShaderColor(1F, 1F, 1F, (Mth.lerp(event.getPartialTick(), lastAurora, aurora)) / 60F * 0.5F);
			TFShaders.AURORA.invokeThenEndTesselator(
					Minecraft.getInstance().level == null ? 0 : Mth.abs((int) Minecraft.getInstance().level.getBiomeManager().biomeZoomSeed),
					(float) pos.x(), (float) pos.y(), (float) pos.z()
			);
			RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
			RenderSystem.disableDepthTest();
			RenderSystem.disableBlend();
		}
	}

	/**
	 * On the tick, we kill the vignette
	 */
	@SubscribeEvent
	public static void renderTick(TickEvent.RenderTickEvent event) {
		if (event.phase == TickEvent.Phase.START) {
			Minecraft minecraft = Minecraft.getInstance();

			// only fire if we're in the twilight forest
			if (minecraft.level != null && TFGenerationSettings.DIMENSION_KEY.equals(minecraft.level.dimension())) {
				// vignette
				if (minecraft.gui != null) {
					minecraft.gui.vignetteBrightness = 0.0F;
				}
			}

			if (minecraft.player != null && HostileMountEvents.isRidingUnfriendly(minecraft.player)) {
				if (minecraft.gui != null) {
					minecraft.gui.setOverlayMessage(Component.empty(), false);
				}
			}
		}
	}

	public static int time = 0;
	private static int rotationTickerI = 0;
	private static int sineTickerI = 0;
	public static float rotationTicker = 0;
	public static float sineTicker = 0;
	public static final float PI = (float) Math.PI;
	private static final int SINE_TICKER_BOUND = (int) ((PI * 200.0F) - 1.0F);
	private static float intensity = 0.0F;

	private static int aurora = 0;
	private static int lastAurora = 0;

	// [4.8 HolderMatcher] compares reference holders by registry key, so a holder cached from an earlier RegistryAccess (e.g. before switching servers) still matches
	private static <T> boolean holdersMatch(Holder<T> a, Holder<T> b) {
		if (a == b) return true;
		if (a.kind() == Holder.Kind.DIRECT || b.kind() == Holder.Kind.DIRECT) {
			return a.value() == b.value();
		}
		return b.unwrapKey().map(a::is).orElseGet(() -> a.unwrapKey().map(b::is).orElse(false));
	}

	@SubscribeEvent
	public static void clientTick(TickEvent.ClientTickEvent event) {
		if (event.phase != TickEvent.Phase.END) return;
		Minecraft mc = Minecraft.getInstance();
		float partial = mc.getFrameTime();

		if (!mc.isPaused()) {
			time++;

			rotationTickerI = (rotationTickerI >= 359 ? 0 : rotationTickerI + 1);
			sineTickerI = (sineTickerI >= SINE_TICKER_BOUND ? 0 : sineTickerI + 1);

			rotationTicker = rotationTickerI + partial;
			sineTicker = sineTicker + partial;

			lastAurora = aurora;
			if (Minecraft.getInstance().level != null && Minecraft.getInstance().cameraEntity != null && !TFConfig.getValidAuroraBiomes(Minecraft.getInstance().level.registryAccess()).isEmpty()) {
				RegistryAccess access = Minecraft.getInstance().level.registryAccess();
				Holder<Biome> biome = Minecraft.getInstance().level.getBiome(Minecraft.getInstance().cameraEntity.blockPosition());
				if (TFConfig.getValidAuroraBiomes(access).stream().anyMatch(valid -> holdersMatch(valid, biome)))
					aurora++;
				else
					aurora--;
				aurora = Mth.clamp(aurora, 0, 60);
			} else {
				aurora = 0;
			}
		}

		if (!mc.isPaused()) {
			BugModelAnimationHelper.animate();

			// Magic painting lightning layers fade out from the last tick a sky flash was active
			if (mc.level != null && mc.level.getSkyFlashTime() > 0) {
				MagicPaintingRenderer.lastLightning = mc.level.getGameTime();
			}

			if (TFConfig.CLIENT_CONFIG.firstPersonEffects.get() && mc.level != null && mc.player != null) {
				HashSet<ChunkPos> chunksInRange = new HashSet<>();
				for (int x = -16; x <= 16; x += 16) {
					for (int z = -16; z <= 16; z += 16) {
						chunksInRange.add(new ChunkPos((int) (mc.player.getX() + x) >> 4, (int) (mc.player.getZ() + z) >> 4));
					}
				}
				for (ChunkPos pos : chunksInRange) {
					if (mc.level.getChunk(pos.x, pos.z, ChunkStatus.FULL, false) != null) {
						List<BlockEntity> beanstalksInChunk = mc.level.getChunk(pos.x, pos.z).getBlockEntities().values().stream()
								.filter(blockEntity -> blockEntity instanceof GrowingBeanstalkBlockEntity beanstalkBlock && beanstalkBlock.isBeanstalkRumbling())
								.toList();
						if (!beanstalksInChunk.isEmpty()) {
							BlockEntity beanstalk = beanstalksInChunk.get(0);
							Player player = mc.player;
							intensity = (float) (1.0F - mc.player.distanceToSqr(Vec3.atCenterOf(beanstalk.getBlockPos())) / Math.pow(16, 2));
							if (intensity > 0) {
								player.moveTo(player.getX(), player.getY(), player.getZ(),
										player.getYRot() + (player.getRandom().nextFloat() * 2.0F - 1.0F) * intensity,
										player.getXRot() + (player.getRandom().nextFloat() * 2.0F - 1.0F) * intensity);
								intensity = 0.0F;
								break;
							}
						}
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void camera(ViewportEvent.ComputeCameraAngles event) {
		if (TFConfig.CLIENT_CONFIG.firstPersonEffects.get() && !Minecraft.getInstance().isPaused() && intensity > 0 && Minecraft.getInstance().player != null) {
			event.setYaw((float) Mth.lerp(event.getPartialTick(), event.getYaw(), event.getYaw() + (Minecraft.getInstance().player.getRandom().nextFloat() * 2F - 1F) * intensity));
			event.setPitch((float) Mth.lerp(event.getPartialTick(), event.getPitch(), event.getPitch() + (Minecraft.getInstance().player.getRandom().nextFloat() * 2F - 1F) * intensity));
			event.setRoll((float) Mth.lerp(event.getPartialTick(), event.getRoll(), event.getRoll() + (Minecraft.getInstance().player.getRandom().nextFloat() * 2F - 1F) * intensity));
			intensity = 0F;
		}
	}

	private static final MutableComponent WIP_TEXT_0 = Component.translatable("misc.twilightforest.wip0").setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
	private static final MutableComponent WIP_TEXT_1 = Component.translatable("misc.twilightforest.wip1").setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
	private static final MutableComponent NYI_TEXT = Component.translatable("misc.twilightforest.nyi").setStyle(Style.EMPTY.withColor(ChatFormatting.RED));
	private static final MutableComponent EMPERORS_CLOTH_TOOLTIP = Component.translatable("item.twilightforest.emperors_cloth.desc").withStyle(ChatFormatting.GRAY);

	@SubscribeEvent
	public static void tooltipEvent(ItemTooltipEvent event) {
		ItemStack item = event.getItemStack();

		if (ArmorUtil.hasEmperorsCloth(item)) {
			event.getToolTip().add(Math.min(1, event.getToolTip().size()), EMPERORS_CLOTH_TOOLTIP);
		}

		if (!item.is(ItemTagGenerator.WIP) && !item.is(ItemTagGenerator.NYI)) return;

		if (item.is(ItemTagGenerator.WIP)) {
			event.getToolTip().add(WIP_TEXT_0);
			event.getToolTip().add(WIP_TEXT_1);
		} else {
			event.getToolTip().add(NYI_TEXT);
		}
	}

	/**
	 * Left clicking air with an Ore Meter wipes its scan results and targeted block (4.8 EntityEvents#wipeOreMeterOnLeftClick).
	 * The event only fires on the client, so the server is told through {@link WipeOreMeterPacket}.
	 */
	@SubscribeEvent
	public static void wipeOreMeterOnLeftClick(PlayerInteractEvent.LeftClickEmpty event) {
		ItemStack stack = event.getItemStack();
		if (stack.is(TFItems.ORE_METER.get()) && OreMeterItem.hasScanDataOrFilter(stack)) {
			TFPacketHandler.CHANNEL.sendToServer(new WipeOreMeterPacket(event.getHand()));
			OreMeterItem.wipe(stack);
			event.getLevel().playSound(event.getEntity(), event.getEntity().blockPosition(), TFSounds.ORE_METER_CLEAR.get(), SoundSource.PLAYERS, 1.25F, event.getLevel().getRandom().nextFloat() * 0.2F + 0.6F);
		}
	}

	/**
	 * Zooms in the FOV while using a bow, just like vanilla does in the AbstractClientPlayer's getFieldOfViewModifier() method (1.18.2)
	 */
	@SubscribeEvent
	public static void updateBowFOV(ComputeFovModifierEvent event) {
		Player player = event.getPlayer();
		if (player.isUsingItem()) {
			Item useItem = player.getUseItem().getItem();
			if (useItem instanceof TripleBowItem || useItem instanceof EnderBowItem || useItem instanceof IceBowItem || useItem instanceof SeekerBowItem) {
				float f = player.getTicksUsingItem() / 20.0F;
				f = f > 1.0F ? 1.0F : f * f;
				event.setNewFovModifier((float) Mth.lerp(Minecraft.getInstance().options.fovEffectScale().get(), 1.0F, (event.getFovModifier() * (1.0F - f * 0.15F))));
			}
		}
	}

	@SubscribeEvent
	public static void unrenderHeadWithTrophies(RenderLivingEvent.Pre<?, ?> event) {
		ItemStack stack = event.getEntity().getItemBySlot(EquipmentSlot.HEAD);
		boolean visible = !(stack.getItem() instanceof TrophyItem) && !areCuriosEquipped(event.getEntity());
		boolean isPlayer = event.getEntity() instanceof Player;
		// Renderer models are shared by every entity of a type, so the head has to be made visible again as well, or one trophy wearer leaves all of them headless (#2147).
		// PlayerRenderer resets part visibility itself before this event, so keep whatever it (or mods like Better Combat, which hide the head in first person) decided (#2267).
		if (event.getRenderer().getModel() instanceof HeadedModel headedModel) {
			headedModel.getHead().visible = visible && (!isPlayer || headedModel.getHead().visible);
			if (event.getRenderer().getModel() instanceof HumanoidModel<?> humanoidModel) {
				humanoidModel.hat.visible = visible && (!isPlayer || humanoidModel.hat.visible);
			}
		}
	}

	private static boolean areCuriosEquipped(LivingEntity entity) {
		if (ModList.get().isLoaded("curios")) {
			return CuriosCompat.isTrophyCurioEquipped(entity);
		}
		return false;
	}

	@SubscribeEvent
	public static void translateBookAuthor(ItemTooltipEvent event) {
		ItemStack stack = event.getItemStack();
		if (stack.getItem() instanceof WrittenBookItem && stack.hasTag()) {
			CompoundTag tag = stack.getOrCreateTag();
			if (tag.contains(TwilightForestMod.ID + ":book")) {
				List<Component> components = event.getToolTip();
				for (Component component : components) {
					if (component.toString().contains("book.byAuthor")) {
						components.set(components.indexOf(component), (Component.translatable("book.byAuthor", Component.translatable(TwilightForestMod.ID + ".book.author"))).withStyle(component.getStyle()));
					}
				}
			}
		}
	}

	@SubscribeEvent
	public static void onRenderBlockHighlightEvent(RenderHighlightEvent.Block event) {
		BlockPos pos = event.getTarget().getBlockPos();
		BlockState state = event.getCamera().getEntity().level().getBlockState(pos);

		if (state.getBlock() instanceof MiniatureStructureBlock) {
			event.setCanceled(true);
			return;
		}

		LocalPlayer player = Minecraft.getInstance().player;
		if (player != null && (player.getMainHandItem().getItem() instanceof GiantPickItem || (player.getMainHandItem().getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof GiantBlock))) {
			event.setCanceled(true);
			if (!state.isAir() && player.level().getWorldBorder().isWithinBounds(pos)) {
				BlockPos offsetPos = new BlockPos(pos.getX() & ~0b11, pos.getY() & ~0b11, pos.getZ() & ~0b11);
				renderGiantHitOutline(event.getPoseStack(), event.getMultiBufferSource().getBuffer(RenderType.lines()), Vec3.atLowerCornerOf(offsetPos).subtract(event.getCamera().getPosition()));
			}
		}
	}

	/**
	 * Draws our RGB boss bars, which the vanilla overlay can only draw in its seven colours.
	 */
	@SubscribeEvent
	public static void renderCustomBossBars(CustomizeGuiOverlayEvent.BossEventProgress event) {
		if (event.getBossEvent() instanceof ClientTFBossBar bossEvent) {
			event.setCanceled(true);
			bossEvent.renderBossBar(event.getGuiGraphics(), event.getX(), event.getY());
		}
	}

	private static final VoxelShape GIANT_BLOCK = Shapes.box(0.0D, 0.0D, 0.0D, 4.0D, 4.0D, 4.0D);

	// xyz is the giant block's corner relative to the camera, computed in double precision:
	// casting the absolute coordinates to float first made the outline jitter far from the origin
	private static void renderGiantHitOutline(PoseStack poseStack, VertexConsumer consumer, Vec3 xyz) {
		PoseStack.Pose last = poseStack.last();
		GIANT_BLOCK.forAllEdges((x, y, z, x1, y1, z1) -> {
			float xSize = (float)(x1 - x);
			float ySize = (float)(y1 - y);
			float zSize = (float)(z1 - z);
			float sqrt = Mth.sqrt(xSize * xSize + ySize * ySize + zSize * zSize);
			xSize /= sqrt;
			ySize /= sqrt;
			zSize /= sqrt;
			consumer.vertex(last.pose(), (float)(x + xyz.x()), (float)(y + xyz.y()), (float)(z + xyz.z())).color(0.0F, 0.0F, 0.0F, 0.45F).normal(last.normal(), xSize, ySize, zSize).endVertex();
			consumer.vertex(last.pose(), (float)(x1 + xyz.x()), (float)(y1 + xyz.y()), (float)(z1 + xyz.z())).color(0.0F, 0.0F, 0.0F, 0.45F).normal(last.normal(), xSize, ySize, zSize).endVertex();
		});
	}
}
