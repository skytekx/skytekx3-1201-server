package twilightforest.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.Input;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ProjectileWeaponItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.ComputeFovModifierEvent;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RenderArmEvent;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.TFConfig;
import twilightforest.TwilightForestMod;
import twilightforest.client.model.armor.TFArmorModel;
import twilightforest.components.entity.TravellersGogglesZoomAttachment;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.components.item.TravellersGearData;
import twilightforest.data.tags.ItemTagGenerator;
import twilightforest.init.TFSounds;
import twilightforest.init.custom.TravellersModifiersManager;
import twilightforest.item.travellers_gear.TravellersArmorItem;
import twilightforest.item.travellers_gear.TravellersGearLogic;
import twilightforest.network.GogglesZoomPacket;
import twilightforest.network.GradualGlidePacket;
import twilightforest.network.PerformDoubleJumpPacket;
import twilightforest.network.PerformSidestepPacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.util.ArmorUtil;

import java.util.UUID;

/**
 * Client events of the Traveller's Gear core and the modifiers ported so far. Backport of 4.8's TravellersClientEvents:
 * the glove overlay, the Traveller's Goggles' Zoom and Red Thread Vision, the Traveller's Vest's Stealth, the
 * Traveller's Wings modifiers (Double Jump, Sidestep, Agile Ranger, Gradual Glide) and the Traveller's Boots' Straight
 * Ahead; the key and movement handlers of the other pieces' modifiers come with their travellers-* features.
 */
@Mod.EventBusSubscriber(modid = TwilightForestMod.ID, value = Dist.CLIENT)
public class TravellersClientEvents {
	private static final ResourceLocation GLOVE_TEXTURE = TwilightForestMod.prefix("textures/models/armor/travellers_layer_1.png");

	/**
	 * Whether goggles with Red Thread Vision show the red thread, toggled with its key. 4.8 keeps it in the
	 * {@code travellers_goggles_red_thread_vision} attachment of the client player, which only ever changes on the client
	 * and so starts over whenever the client player is recreated; here it lives for the whole game session.
	 */
	private static boolean redThreadVision = true;

	private static boolean isZoomKeyHeld(Player player) {
		return TFKeyBinds.ZOOM_KEY.isDown() && !player.isScoping();
	}

	public static boolean isRedThreadVisionEnabled() {
		return redThreadVision;
	}

	/**
	 * Whether Shift is held, for the modifier descriptions in the Traveller's Gear tooltip (4.8 TooltipFlag#hasShiftDown).
	 */
	public static boolean hasShiftDown() {
		return Screen.hasShiftDown();
	}

	/**
	 * The modifier cache is static and the server only clears it when it reloads its data. A client that joins another
	 * server, whose data packs define the modifiers differently, must not keep the definitions of the last one.
	 */
	@SubscribeEvent
	public static void clearModifierCacheOnLogin(ClientPlayerNetworkEvent.LoggingIn event) {
		TravellersModifiersManager.clearCache();
	}

	@SubscribeEvent
	public static void clearModifierCacheOnLogout(ClientPlayerNetworkEvent.LoggingOut event) {
		TravellersModifiersManager.clearCache();
	}

	/**
	 * Hides sneaking players with Stealth at once, before the server's Invisibility effect reaches the client. 4.8 does
	 * this on RenderFrameEvent.Pre, the start of the render tick here.
	 */
	@SubscribeEvent
	public static void handleStealth(TickEvent.RenderTickEvent event) {
		if (event.phase != TickEvent.Phase.START || Minecraft.getInstance().level == null)
			return;
		for (Entity entity : Minecraft.getInstance().level.entitiesForRendering()) {
			if (entity instanceof Player player)
				TravellersGearLogic.travellersStealth(player, player1 -> player1.setInvisible(true));  // call it on client to make player invisible instantly
		}
	}

	/**
	 * The movement input of the local player, in the order 4.8 registers its MovementInputUpdateEvent listeners. Forge
	 * fires the event before LocalPlayer#aiStep slows down a player using an item, as in 4.8.
	 */
	@SubscribeEvent
	public static void handleMovementInput(MovementInputUpdateEvent event) {
		if (!(event.getEntity() instanceof LocalPlayer localPlayer))
			return;
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(localPlayer);
		if (attachment == null)
			return;
		handleAgileRanger(localPlayer, event.getInput());
		handleStraightAhead(localPlayer);
		speedUpControlledWhileSneaking(localPlayer, attachment);
		handleSidestep(localPlayer, attachment);
	}

	private static void handleAgileRanger(LocalPlayer localPlayer, Input input) {
		ItemStack leggingsStack = localPlayer.getItemBySlot(EquipmentSlot.LEGS);
		Float agileRangerModifier = TravellersGearData.AGILE_RANGER_MODIFIER.get(leggingsStack);
		if (!TravellersModifiersManager.isModifierActive(localPlayer, leggingsStack, TravellersModifiersManager.AGILE_RANGER_MODIFIER) || agileRangerModifier == null)
			return;
		ItemStack stack = localPlayer.getUseItem();
		boolean isLegalItem = (stack.getItem() instanceof ProjectileWeaponItem || stack.is(ItemTagGenerator.TRAVELLERS_AGILE_RANGER_WHITELISTED)) && !stack.is(ItemTagGenerator.TRAVELLERS_AGILE_RANGER_BLACKLISTED);
		if (localPlayer.isUsingItem() && !localPlayer.isPassenger() && isLegalItem) {
			input.leftImpulse *= agileRangerModifier;
			input.forwardImpulse *= agileRangerModifier;
		}
	}

	/**
	 * Straight Ahead speeds the local player up only while they walk forward, and not their strafing.
	 */
	private static void handleStraightAhead(LocalPlayer localPlayer) {
		ItemStack bootsStack = localPlayer.getItemBySlot(EquipmentSlot.FEET);
		Double multiplier = TravellersGearData.STRAIGHT_AHEAD_MULTIPLIER.get(bootsStack);
		AttributeInstance attributeInstance = localPlayer.getAttributes().getInstance(Attributes.MOVEMENT_SPEED);
		if (attributeInstance == null)
			return;

		Input input = localPlayer.input;
		if (!TravellersModifiersManager.isModifierActive(localPlayer, bootsStack, TravellersModifiersManager.STRAIGHT_AHEAD_MODIFIER) || multiplier == null || input.forwardImpulse <= 0)
			multiplier = 1D;
		// 4.8 re-adds the modifier every tick (addOrUpdateTransientModifier); replacing it only when it changes gives the same speed
		AttributeModifier current = attributeInstance.getModifier(TravellersGearLogic.STRAIGHT_AHEAD_MODIFIER_UUID);
		if (current == null || current.getAmount() != multiplier - 1 || current.getOperation() != AttributeModifier.Operation.MULTIPLY_TOTAL) {
			attributeInstance.removeModifier(TravellersGearLogic.STRAIGHT_AHEAD_MODIFIER_UUID);
			attributeInstance.addTransientModifier(TravellersGearLogic.createStraightAheadModifier(multiplier));
		}
		input.leftImpulse /= multiplier;
	}

	private static void speedUpControlledWhileSneaking(LocalPlayer localPlayer, TravellersWingsAttachment attachment) {
		if (!attachment.isGraduallyGliding || !localPlayer.isShiftKeyDown())
			return;
		localPlayer.input.forwardImpulse /= 0.2F;
		localPlayer.input.leftImpulse /= 0.2F;
	}

	/**
	 * Double-tapping left or right on the ground dashes to that side.
	 */
	private static void handleSidestep(LocalPlayer localPlayer, TravellersWingsAttachment attachment) {
		if (!localPlayer.onGround())
			return;

		Input input = localPlayer.input;
		boolean lastImpulseZero = attachment.lastHorizontalImpulse == 0;
		boolean sameImpulseDirection = Math.signum(attachment.lastNonZeroHorizontalImpulse) == Math.signum(input.leftImpulse);
		int currentTime = localPlayer.tickCount;
		int lastWalkingTime = attachment.lastHorizontalWalkingTime;
		boolean hasDoubleTapped = currentTime - lastWalkingTime < 4;

		if (lastImpulseZero && sameImpulseDirection && hasDoubleTapped && input.leftImpulse != 0) {
			boolean isLeftSidestep = input.leftImpulse > 0;
			if (TravellersGearLogic.tryPerformSidestep(localPlayer, isLeftSidestep)) {
				TFPacketHandler.CHANNEL.sendToServer(new PerformSidestepPacket(isLeftSidestep));
			}
		}

		attachment.lastHorizontalImpulse = input.leftImpulse;
		if (input.leftImpulse != 0) {
			attachment.lastHorizontalWalkingTime = currentTime;
			attachment.lastNonZeroHorizontalImpulse = input.leftImpulse;
		}
	}

	/**
	 * Pressing jump in mid-air performs the Double Jump, unless a player that may fly double-tapped jump to start flying.
	 */
	@SubscribeEvent
	public static void handleDoubleJump(InputEvent.Key event) {
		Minecraft minecraft = Minecraft.getInstance();
		LocalPlayer localPlayer = minecraft.player;
		if (localPlayer == null || ignoreKeyEvent(event, minecraft.options.keyJump))
			return;
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(localPlayer);
		if (attachment == null)
			return;
		int lastJumpKeyPressTime = attachment.lastJumpKeyPressTime;
		boolean pressedKey = event.getAction() == InputConstants.PRESS;
		if (pressedKey)
			attachment.lastJumpKeyPressTime = localPlayer.tickCount;
		boolean avoidCreativeFly = localPlayer.getAbilities().mayfly && localPlayer.tickCount - lastJumpKeyPressTime <= 6;
		if (pressedKey && !avoidCreativeFly && TravellersModifiersManager.isModifierActive(localPlayer, TravellersModifiersManager.DOUBLE_JUMP_MODIFIER)) {
			if (TravellersGearLogic.performDoubleJump(localPlayer)) {
				TFPacketHandler.CHANNEL.sendToServer(new PerformDoubleJumpPacket());
			}
		}
	}

	private static boolean ignoreKeyEvent(InputEvent.Key event, KeyMapping key) {
		return !key.matches(event.getKey(), event.getScanCode()) || event.getAction() != InputConstants.PRESS || Minecraft.getInstance().screen != null;
	}

	/**
	 * Decides every frame whether the local player glides gradually: while falling, with the sneak key held or released
	 * depending on the client config, and tells the server when that changes. 4.8 runs this on RenderFrameEvent.Pre and
	 * does not ask for the modifier, so a player without it bounced packets with the server while falling and sneaked
	 * through the air at gliding speed; here only players with an active Gradual Glide modifier glide.
	 */
	@SubscribeEvent
	public static void updateGradualGlideState(TickEvent.RenderTickEvent event) {
		if (event.phase != TickEvent.Phase.START)
			return;
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null)
			return;
		TravellersWingsAttachment attachment = TravellersWingsAttachment.get(player);
		if (attachment == null)
			return;
		boolean wasGraduallyGliding = attachment.isGraduallyGliding;
		boolean shiftHeld = player.isShiftKeyDown();
		boolean isGraduallyGliding = TFConfig.CLIENT_CONFIG.manualTravellersWingsGradualGlide.get() == shiftHeld && player.getDeltaMovement().y() < 0 && !player.onGround()
			&& TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.GRADUAL_GLIDE_MODIFIER);
		if (isGraduallyGliding == wasGraduallyGliding)
			return;

		attachment.isGraduallyGliding = isGraduallyGliding;
		TFPacketHandler.CHANNEL.sendToServer(new GradualGlidePacket(isGraduallyGliding, player.getUUID()));
	}

	/**
	 * Straight Ahead does not widen the field of view the way other speed boosts do: the view is computed as if the
	 * player moved without it. 4.8 takes the modifier off around AbstractClientPlayer#getFieldOfViewModifier with a
	 * coremod (GetFieldOfViewModifierTransformer); Forge's event receives the result, so the modifier's share of the
	 * speed term ((speed / walking speed + 1) / 2) is divided out instead, before any other listener changes the view.
	 */
	@SubscribeEvent(priority = EventPriority.HIGHEST)
	public static void nullifyStraightAheadFov(ComputeFovModifierEvent event) {
		AttributeInstance attributeInstance = event.getPlayer().getAttribute(Attributes.MOVEMENT_SPEED);
		AttributeModifier modifier = attributeInstance == null ? null : attributeInstance.getModifier(TravellersGearLogic.STRAIGHT_AHEAD_MODIFIER_UUID);
		float walkingSpeed = event.getPlayer().getAbilities().getWalkingSpeed();
		if (modifier == null || modifier.getAmount() == 0 || modifier.getAmount() <= -1 || walkingSpeed == 0)
			return;

		float speed = (float) attributeInstance.getValue();
		float speedWithoutStraightAhead = (float) (speed / (1 + modifier.getAmount()));
		float speedTerm = speed / walkingSpeed + 1.0F;
		float speedTermWithoutStraightAhead = speedWithoutStraightAhead / walkingSpeed + 1.0F;
		float ratio = speedTermWithoutStraightAhead / speedTerm;
		if (Float.isFinite(ratio))
			event.setNewFovModifier(event.getNewFovModifier() * ratio);
	}

	/**
	 * Applies the Gradual Glide state the server sent for a player, see {@link GradualGlidePacket}.
	 */
	public static void setGraduallyGliding(UUID playerUUID, boolean isGraduallyGliding) {
		ClientLevel level = Minecraft.getInstance().level;
		if (level == null)
			return;
		Player player = level.getPlayerByUUID(playerUUID);
		TravellersWingsAttachment attachment = player == null ? null : TravellersWingsAttachment.get(player);
		if (attachment != null)
			attachment.isGraduallyGliding = isGraduallyGliding;
	}

	/**
	 * Zoom: while the zoom key is held with goggles that have the ability, the field of view shrinks by the ability's
	 * factor. A change of the zoom state plays the zoom sound and is sent to the server, which passes it on to the other
	 * players.
	 */
	@SubscribeEvent
	public static void updateZoomState(ComputeFovModifierEvent event) {
		LocalPlayer player = Minecraft.getInstance().player;
		if (player == null) return;
		boolean wasUsingZoom = TravellersGogglesZoomAttachment.isUsingZoom(player);
		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		Float zoomModifier = TravellersArmorItem.isTravellersGear(headStack) ? TravellersGearData.ZOOM_ABILITY_MODIFIER.get(headStack) : null;
		boolean isUsingZoom = zoomModifier != null && isZoomKeyHeld(player) && TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ZOOM_ABILITY);
		if (isUsingZoom)
			event.setNewFovModifier(event.getNewFovModifier() * zoomModifier);
		if (isUsingZoom == wasUsingZoom)
			return;

		TravellersGogglesZoomAttachment.setUsingZoom(player, isUsingZoom);
		player.playSound(isUsingZoom ? TFSounds.GOGGLES_ZOOM_IN.get() : TFSounds.GOGGLES_ZOOM_OUT.get(), 1.0F, 1.0F);
		TFPacketHandler.CHANNEL.sendToServer(new GogglesZoomPacket(isUsingZoom, player.getUUID()));
	}

	/**
	 * Slows the mouse down while zooming with the goggles, like a spyglass. 4.8 listens to NeoForge's
	 * CalculatePlayerTurnEvent; Forge 47 has none, so {@code META-INF/asm/zoom.js} calls this from MouseHandler#turnPlayer
	 * with the mouse sensitivity option, and the returned value takes its place.
	 */
	@SuppressWarnings("unused")
	public static double modifyMouseSensitivity(double mouseSensitivity) {
		Minecraft minecraft = Minecraft.getInstance();
		Player player = minecraft.player;
		// a sensitivity of 0 would turn the camera by NaN below
		if (minecraft.options.smoothCamera || player == null || mouseSensitivity <= 0.0D)
			return mouseSensitivity;

		ItemStack headStack = player.getItemBySlot(EquipmentSlot.HEAD);
		Float zoomModifier = TravellersArmorItem.isTravellersGear(headStack) ? TravellersGearData.ZOOM_ABILITY_MODIFIER.get(headStack) : null;
		// 4.8 does not check that the ability works here, so broken goggles slowed the mouse down without zooming
		if (zoomModifier == null || !isZoomKeyHeld(player) || !TravellersModifiersManager.isModifierActive(player, headStack, TravellersModifiersManager.ZOOM_ABILITY))
			return mouseSensitivity;

		// vanilla math for turning is (m * 0.6 + 0.2)³ * 8; where m is the mouse sensitivity
		// vanilla spyglasses avoid using the "* 8" part, so we probably want to as well
		// the mod value to reverse that was borrowed from IE since they also have zoom functionality
		// we can then divide by our zoom modifier (and add 0.05 to slow it down slightly) to set the sensitivity to a reasonable value when zooming
		double mod = 0.5D - 1 / (6 * mouseSensitivity);
		double fovMod = zoomModifier + 0.05F;
		return mod * mouseSensitivity / fovMod;
	}

	/**
	 * Red Thread Vision: its key turns the glowing red thread of goggles with the modifier on and off.
	 */
	@SubscribeEvent
	public static void toggleRedThreadVision(InputEvent.Key event) {
		if (!TFKeyBinds.RED_THREAD_VISION_KEY.consumeClick())
			return;

		Player player = Minecraft.getInstance().player;
		if (player == null || !TravellersModifiersManager.isModifierActive(player, TravellersModifiersManager.RED_THREAD_VISION_MODIFIER))
			return;

		redThreadVision = !redThreadVision;
	}

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
