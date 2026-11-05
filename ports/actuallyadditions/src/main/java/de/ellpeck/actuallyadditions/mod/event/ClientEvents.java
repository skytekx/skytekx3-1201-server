/*
 * This file ("ClientEvents.java") is part of the Actually Additions mod for Minecraft.
 * It is created and owned by Ellpeck and distributed
 * under the Actually Additions License to be found at
 * http://ellpeck.de/actaddlicense
 * View the source code at https://github.com/Ellpeck/ActuallyAdditions
 *
 * © 2015-2017 Ellpeck
 */

package de.ellpeck.actuallyadditions.mod.event;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import de.ellpeck.actuallyadditions.api.misc.IGoggles;
import de.ellpeck.actuallyadditions.mod.ActuallyAdditions;
import de.ellpeck.actuallyadditions.mod.blocks.BlockAtomicReconstructor;
import de.ellpeck.actuallyadditions.mod.blocks.BlockLaserRelay;
import de.ellpeck.actuallyadditions.mod.blocks.BlockLavaFactoryController;
import de.ellpeck.actuallyadditions.mod.blocks.BlockPhantom;
import de.ellpeck.actuallyadditions.mod.blocks.BlockPlayerInterface;
import de.ellpeck.actuallyadditions.mod.blocks.BlockVerticalDigger;
import de.ellpeck.actuallyadditions.mod.blocks.IHudDisplay;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.BlockHuds;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.IBlockHud;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.LaserRelayHud;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.LavaFactoryControllerHud;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.MinerHud;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.PhantomHud;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.PlayerInterfaceHud;
import de.ellpeck.actuallyadditions.mod.blocks.blockhuds.ReconstructorHud;
import de.ellpeck.actuallyadditions.mod.config.ClientConfig;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.data.WorldData;
import de.ellpeck.actuallyadditions.mod.inventory.gui.EnergyDisplay;
import de.ellpeck.actuallyadditions.mod.tile.IEnergyDisplay;
import de.ellpeck.actuallyadditions.mod.tile.TileEntityBase;
import de.ellpeck.actuallyadditions.mod.util.StackUtil;
import io.netty.util.internal.ConcurrentSet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.gui.overlay.GuiOverlayManager;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.List;
import java.util.Set;

@OnlyIn(Dist.CLIENT)
public class ClientEvents {

    private static final Component ADVANCED_INFO_TEXT_PRE = Component.literal("  -").withStyle(ChatFormatting.DARK_GRAY);
    private static final Component ADVANCED_INFO_HEADER_PRE = Component.literal("  -").withStyle(ChatFormatting.GRAY);

    private static EnergyDisplay energyDisplay;

    // SkyTekx: the overlays that upstream 668f7ffe moved into client only IBlockHud classes. The
    // blocks only implement the marker IHudDisplay, so the client registers them here
    public ClientEvents() {
        BlockHuds.register(BlockAtomicReconstructor.class, new ReconstructorHud());
        BlockHuds.register(BlockLaserRelay.class, new LaserRelayHud());
        BlockHuds.register(BlockLavaFactoryController.class, new LavaFactoryControllerHud());
        BlockHuds.register(BlockPhantom.class, new PhantomHud());
        BlockHuds.register(BlockPlayerInterface.class, new PlayerInterfaceHud());
        BlockHuds.register(BlockVerticalDigger.class, new MinerHud());
    }

    // TODO: [port] the fuck?
    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            Minecraft mc = Minecraft.getInstance();

            if (mc.level == null) {
                WorldData.clear();
            }

            if (mc.player != null) {
                this.renderEngineerEffect(mc.player);
            }
        }
    }

    // SkyTekx: the client tick of ItemEngineerGoggles, moved here by upstream 844efe42. The base
    // registered each goggles item on the Forge bus from its common constructor
    private final Set<Entity> cachedGlowingEntities = new ConcurrentSet<>();

    /**
     * Renders a special visual effect for entities around the player if the player is wearing goggles that allow them to see spectral mobs.
     *
     * @param player The player wearing the goggles.
     */
    private void renderEngineerEffect(Player player) {
        ItemStack face = player.getInventory().armor.get(3);
        if (!face.isEmpty() && face.getItem() instanceof IGoggles goggles && goggles.displaySpectralMobs()) {
            double range = 8;
            AABB aabb = new AABB(player.getX() - range, player.getY() - range, player.getZ() - range, player.getX() + range, player.getY() + range, player.getZ() + range);
            List<Entity> entities = player.level().getEntitiesOfClass(Entity.class, aabb);
            if (entities != null && !entities.isEmpty()) {
                this.cachedGlowingEntities.addAll(entities);
            }

            if (!this.cachedGlowingEntities.isEmpty()) {
                for (Entity entity : this.cachedGlowingEntities) {
                    if (!entity.isAlive() || entity.distanceToSqr(player.getX(), player.getY(), player.getZ()) > range * range) {
                        entity.setGlowingTag(false);

                        this.cachedGlowingEntities.remove(entity);
                    } else {
                        entity.setGlowingTag(true);
                    }
                }
            }

            return;
        }

        if (!this.cachedGlowingEntities.isEmpty()) {
            for (Entity entity : this.cachedGlowingEntities) {
                if (entity.isAlive()) {
                    entity.setGlowingTag(false);
                }
            }
            this.cachedGlowingEntities.clear();
        }
    }

    @SubscribeEvent
    public void onTooltipEvent(ItemTooltipEvent event) {
        // TODO: [port] ADD BACK AS NEEDED

        //        ItemStack stack = event.getItemStack();
        //        if (StackUtil.isValid(stack)) {
        //            //Be da bland
        //            if (ConfigBoolValues.MOST_BLAND_PERSON_EVER.isEnabled()) {
        //                ResourceLocation regName = stack.getItem().getRegistryName();
        //                if (regName != null) {
        //                    if (regName.toString().toLowerCase(Locale.ROOT).contains(ActuallyAdditions.MODID)) {
        //                        if (event.getToolTip().size() > 0) {
        //                            event.getToolTip().set(0, TextFormatting.RESET + TextFormatting.WHITE.toString() + event.getToolTip().get(0));
        //                        }
        //                    }
        //                }
        //            }
        //
        //            if (ItemWingsOfTheBats.THE_BAT_BAT.equalsIgnoreCase(stack.getDisplayName()) && stack.getItem() instanceof SwordItem) {
        //                event.getToolTip().set(0, TextFormatting.GOLD + event.getToolTip().get(0));
        //                event.getToolTip().add(1, TextFormatting.RED.toString() + TextFormatting.ITALIC + "That's a really bat pun");
        //            }
        //        }
        //
        //        //Advanced Item Info
        //        if (event.getFlags().isAdvanced() && StackUtil.isValid(event.getItemStack())) {
        //            if (ConfigBoolValues.CTRL_EXTRA_INFO.isEnabled()) {
        //                if (Screen.hasControlDown()) {
        //                    event.getToolTip().add(Help.Trans("tooltip.", ".extraInfo.desc").mergeStyle(TextFormatting.DARK_GRAY).mergeStyle(TextFormatting.ITALIC).append(new StringTextComponent(":")));
        //
        //                    // TODO: [port] come back to this and see if we can re-add it
        //                    //OreDict Names
        //                    //                    int[] oreIDs = OreDictionary.getOreIDs(event.getItemStack());
        //                    //                    event.getToolTip().add(ADVANCED_INFO_HEADER_PRE + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".oredictName.desc") + ":");
        //                    //                    if (oreIDs.length > 0) {
        //                    //                        for (int oreID : oreIDs) {
        //                    //                            event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + OreDictionary.getOreName(oreID));
        //                    //                        }
        //                    //                    } else {
        //                    //                        event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".noOredictNameAvail.desc"));
        //                    //                    }
        //
        //                    //Code Name
        //                    event.getToolTip().add(ADVANCED_INFO_HEADER_PRE + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".codeName.desc") + ":");
        //                    event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + Item.REGISTRY.getNameForObject(event.getItemStack().getItem()));
        //
        //                    //Base Item's Unlocalized Name
        //                    String baseName = event.getItemStack().getItem().getTranslationKey();
        //                    if (baseName != null) {
        //                        event.getToolTip().add(ADVANCED_INFO_HEADER_PRE + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".baseUnlocName.desc") + ":");
        //                        event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + baseName);
        //                    }
        //
        //                    //Metadata
        //                    int meta = event.getItemStack().getItemDamage();
        //                    int max = event.getItemStack().getMaxDamage();
        //                    event.getToolTip().add(ADVANCED_INFO_HEADER_PRE + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".meta.desc") + ":");
        //                    event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + meta + (max > 0
        //                        ? "/" + max
        //                        : ""));
        //
        //                    //Unlocalized Name
        //                    String metaName = event.getItemStack().getItem().getTranslationKey(event.getItemStack());
        //                    if (metaName != null && baseName != null && !metaName.equals(baseName)) {
        //                        event.getToolTip().add(ADVANCED_INFO_HEADER_PRE + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".unlocName.desc") + ":");
        //                        event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + metaName);
        //                    }
        //
        //                    //NBT
        //                    CompoundNBT compound = event.getItemStack().getTagCompound();
        //                    if (compound != null && !compound.isEmpty()) {
        //                        event.getToolTip().add(ADVANCED_INFO_HEADER_PRE + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".nbt.desc") + ":");
        //                        if (Screen.hasShiftDown()) {
        //                            int limit = ConfigIntValues.CTRL_INFO_NBT_CHAR_LIMIT.getValue();
        //                            String compoundStrg = compound.toString();
        //                            int compoundStrgLength = compoundStrg.length();
        //
        //                            String compoundDisplay;
        //                            if (limit > 0 && compoundStrgLength > limit) {
        //                                compoundDisplay = compoundStrg.substring(0, limit) + TextFormatting.GRAY + " (" + (compoundStrgLength - limit) + " more characters...)";
        //                            } else {
        //                                compoundDisplay = compoundStrg;
        //                            }
        //                            event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + compoundDisplay);
        //                        } else {
        //                            event.getToolTip().add(ADVANCED_INFO_TEXT_PRE + TextFormatting.ITALIC + "[" + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".pressShift.desc") + "]");
        //                        }
        //                    }
        //
        //                    //Disabling Info
        //                    event.getToolTip().add(TextFormatting.ITALIC + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".disablingInfo.desc"));
        //
        //                } else {
        //                    if (ConfigBoolValues.CTRL_INFO_FOR_EXTRA_INFO.isEnabled()) {
        //                        event.getToolTip().add(TextFormatting.DARK_GRAY + "" + TextFormatting.ITALIC + StringUtil.localize("tooltip." + ActuallyAdditions.MODID + ".ctrlForMoreInfo.desc"));
        //                    }
        //                }
        //            }
        //        }
    }

    @SubscribeEvent
    public void onGameOverlay(RenderGuiOverlayEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        if (event.getOverlay() == GuiOverlayManager.findOverlay(new ResourceLocation("crosshair")) && minecraft.screen == null) { //ALL
            GuiGraphics guiGraphics = event.getGuiGraphics();
            Player player = minecraft.player;
            if (player == null) {
                return;
            }

            HitResult posHit = minecraft.hitResult;
            Font font = minecraft.font;
            ItemStack stack = player.getMainHandItem();

            if (StackUtil.isValid(stack)) {
                if (stack.getItem() instanceof IHudDisplay) {
                    IBlockHud hud = BlockHuds.get(stack.getItem());
                    if (hud != null) {
                        hud.displayHud(guiGraphics, minecraft, player, stack, posHit, event.getWindow());
                    }
                }
            }

            if (posHit != null && posHit.getType() == HitResult.Type.BLOCK) {
                BlockHitResult rayCast = (BlockHitResult) posHit;
                Block blockHit = minecraft.level.getBlockState(rayCast.getBlockPos()).getBlock();
                BlockEntity tileHit = minecraft.level.getBlockEntity(rayCast.getBlockPos());

                if (blockHit instanceof IHudDisplay) {
                    IBlockHud hud = BlockHuds.get(blockHit);
                    if (hud != null) {
                        hud.displayHud(guiGraphics, minecraft, player, stack, posHit, event.getWindow());
                    }
                }

                if (tileHit instanceof TileEntityBase base) {
	                if (base.isRedstoneToggle()) {
                        // SkyTekx: Components instead of I18n strings (upstream 3a767e18)
                        Component component = Component.translatable("info." + ActuallyAdditions.MODID + ".redstoneMode").append(": ")
                                .append(Component.translatable("info." + ActuallyAdditions.MODID + ".redstoneMode." + (base.isPulseMode
                            ? "pulse"
                            : "deactivation")).withStyle(ChatFormatting.DARK_RED));
                        guiGraphics.drawString(font, component, (int) (event.getWindow().getGuiScaledWidth() / 2f + 5), (int) (event.getWindow().getGuiScaledHeight() / 2f + 5), 0xFFFFFF);

                        Component expl;
                        if (!stack.isEmpty() && stack.getItem() == CommonConfig.Other.redstoneConfigureItem) {
                            expl = Component.translatable("info." + ActuallyAdditions.MODID + ".redstoneMode.validItem").withStyle(ChatFormatting.GREEN);
                        } else {
                            expl = Component.translatable("info." + ActuallyAdditions.MODID + ".redstoneMode.invalidItem", Component.translatable(CommonConfig.Other.redstoneConfigureItem.asItem().getDescriptionId()).getString()).withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC);
                        }
                        guiGraphics.drawString(font, expl, (int) (event.getWindow().getGuiScaledWidth() / 2f + 5), (int) (event.getWindow().getGuiScaledHeight() / 2f + 15), 0xFFFFFF);
                    }
                }

                // SkyTekx: upstream 12527cd1, e21713e3 and 9c6859cd, read on each frame
                if (tileHit instanceof IEnergyDisplay display && !ClientConfig.HIDE_ENERGY_OVERLAY.get()) {
	                if (!display.needsHoldShift() || player.isShiftKeyDown()) {
                        if (energyDisplay == null) {
                            energyDisplay = new EnergyDisplay(0, 0, null);
                        }
                        energyDisplay.setData(2, event.getWindow().getGuiScaledHeight() - 96, display.getEnergyStorage(), true, true);

                        PoseStack matrices = guiGraphics.pose();
                        matrices.pushPose();
//                        GlStateManager._color4f(1F, 1F, 1F, 1F);
                        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
                        energyDisplay.draw(guiGraphics);
                        matrices.popPose();
                    }
                }
            }
        }
    }

}
