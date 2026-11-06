package de.ellpeck.actuallyadditions.devtest;

import com.mojang.brigadier.Command;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.AccessibilityOnboardingScreen;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ServerboundCustomPayloadPacket;
import net.minecraft.network.protocol.game.ServerboundPlayerAbilitiesPacket;
import net.minecraft.network.protocol.game.ServerboundSetCarriedItemPacket;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientChatReceivedEvent;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent;
import net.minecraftforge.client.event.RegisterClientCommandsEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import io.netty.buffer.Unpooled;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

/**
 * SkyTekx: the client driver of the server safety tests. It runs as an operator on a dedicated
 * server and logs every step and every chat line with the prefix AADEVTEST, so that the client
 * log and the server log of one run can be read side by side.
 * <p>
 * Start it with {@code /aadevtest run} in game, or put a file aadevtest.properties into the game
 * directory with {@code connect=<host:port>} (the title screen connects there) and
 * {@code autorun=true} (the tests start after the login and the client stops at the end).
 * {@code skip=<block id>,...} leaves blocks out of the block exercise.
 */
@OnlyIn(Dist.CLIENT)
public final class AADevTestClient {

    static final Logger LOG = LogManager.getLogger("AADevTest");
    /** The SimpleChannel of Actually Additions and the index of PacketClientToServer on it. */
    private static final ResourceLocation CHANNEL = new ResourceLocation("actuallyadditions", "main");
    private static final int SERVERBOUND_DISCRIMINATOR = 1;
    /** The DATA_HANDLERS indices of the base, which the backport keeps. */
    private static final int LASER = 0, TILE_ENTITY = 1, BUTTON_TO_TILE = 2, STRING_TO_TILE = 3, NUMBER_TO_TILE = 4,
        SYNC_PLAYER_DATA = 5, BUTTON_TO_CONTAINER = 6, LASER_PARTICLE = 7, PLAYER_DATA_TO_SERVER = 8;

    private final Properties props = new Properties();
    private final Deque<Step> steps = new ArrayDeque<>();
    private int wait;
    private boolean connectTried;
    private boolean stopAtEnd;

    private BlockPos origin;
    private BlockPos nearCrusher;
    private BlockPos farCrusher;
    private BlockPos solidifier;
    private BlockPos fireworkBox;
    private BlockPos testSpot;

    private record Step(String name, int delay, Runnable action) {
    }

    static void init() {
        MinecraftForge.EVENT_BUS.register(new AADevTestClient());
    }

    private AADevTestClient() {
        Path file = FMLPaths.GAMEDIR.get().resolve("aadevtest.properties");
        if (Files.isRegularFile(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                this.props.load(reader);
            } catch (IOException e) {
                LOG.warn("AADEVTEST could not read {}", file, e);
            }
        }
    }

    @SubscribeEvent
    public void onScreenInit(ScreenEvent.Init.Post event) {
        String connect = this.props.getProperty("connect");
        // A fresh game directory shows the accessibility onboarding screen before the title screen
        if (connect != null && !this.connectTried && (event.getScreen() instanceof TitleScreen || event.getScreen() instanceof AccessibilityOnboardingScreen)) {
            this.connectTried = true;
            LOG.info("AADEVTEST connecting to {}", connect);
            ConnectScreen.startConnecting(event.getScreen(), Minecraft.getInstance(), ServerAddress.parseString(connect), new ServerData("aadevtest", connect, false), false);
        }
    }

    @SubscribeEvent
    public void onLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        LOG.info("AADEVTEST logged in as {} ({})", event.getPlayer().getName().getString(), event.getPlayer().getUUID());
        if (Boolean.parseBoolean(this.props.getProperty("autorun"))) {
            this.stopAtEnd = true;
            this.steps.clear();
            this.step("wait for the world", 100, () -> {
            });
            this.scheduleAll();
        }
    }

    @SubscribeEvent
    public void onLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        if (!this.steps.isEmpty()) {
            LOG.error("AADEVTEST ABORTED: disconnected with {} steps left", this.steps.size());
            this.steps.clear();
            if (this.stopAtEnd) {
                Minecraft.getInstance().stop();
            }
        }
    }

    @SubscribeEvent
    public void onChat(ClientChatReceivedEvent event) {
        LOG.info("AADEVTEST CHAT {}", event.getMessage().getString());
    }

    @SubscribeEvent
    public void onClientCommands(RegisterClientCommandsEvent event) {
        event.getDispatcher().register(Commands.literal("aadevtest").then(Commands.literal("run").executes(context -> {
            this.steps.clear();
            this.scheduleAll();
            return Command.SINGLE_SUCCESS;
        })));
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || Minecraft.getInstance().player == null) {
            return;
        }
        if (this.wait > 0) {
            this.wait--;
            return;
        }
        Step step = this.steps.poll();
        if (step != null) {
            LOG.info("AADEVTEST STEP {}", step.name());
            try {
                step.action().run();
            } catch (Throwable t) {
                LOG.error("AADEVTEST step {} threw", step.name(), t);
            }
            this.wait = step.delay();
        }
    }

    private void step(String name, int delay, Runnable action) {
        this.steps.add(new Step(name, delay, action));
    }

    private void scheduleAll() {
        this.scheduleSetup();
        this.schedulePacketTests();
        this.scheduleMenuTests();
        this.scheduleBlockAndItemTests();
        this.step("done", 40, () -> LOG.info("AADEVTEST DONE"));
        if (this.stopAtEnd) {
            this.step("stop", 0, () -> Minecraft.getInstance().stop());
        }
    }

    // ---- setup -------------------------------------------------------------------------------

    private void scheduleSetup() {
        this.step("setup", 20, () -> {
            LocalPlayer player = player();
            this.origin = player.blockPosition().above(8);
            this.nearCrusher = this.origin.offset(2, 1, 0);
            this.farCrusher = this.origin.offset(20, 1, 0);
            this.solidifier = this.origin.offset(-2, 1, 0);
            this.fireworkBox = this.origin.offset(0, 1, 2);
            this.testSpot = this.origin.offset(0, 0, -2);
            this.command("gamemode creative");
            this.command("gamerule sendCommandFeedback true");
            // Experience, so that the XP Solidifier reaches the array access that a button id of -1 breaks
            this.command("xp add @s 100 levels");
            this.command("tp @s " + this.origin.getX() + ".5 " + this.origin.getY() + " " + this.origin.getZ() + ".5 0 0");
            this.command("setblock " + pos(this.nearCrusher) + " actuallyadditions:crusher");
            this.command("setblock " + pos(this.farCrusher) + " actuallyadditions:crusher");
            this.command("setblock " + pos(this.solidifier) + " actuallyadditions:xp_solidifier");
            this.command("setblock " + pos(this.fireworkBox) + " actuallyadditions:firework_box");
            this.command("execute in minecraft:the_nether run forceload add " + this.nearCrusher.getX() + " " + this.nearCrusher.getZ());
            this.command("execute in minecraft:the_nether run setblock " + pos(this.nearCrusher) + " actuallyadditions:crusher");
            this.command("summon minecraft:pig " + pos(this.origin.offset(0, 1, -4)) + " {NoAI:1b,NoGravity:1b,Invulnerable:1b}");
        });
        this.step("fly", 20, () -> {
            LocalPlayer player = player();
            player.getAbilities().flying = true;
            connection().send(new ServerboundPlayerAbilitiesPacket(player.getAbilities()));
            this.backToOrigin();
        });
    }

    // ---- packet-hardening ----------------------------------------------------------------------

    private void schedulePacketTests() {
        this.step("packets", 40, () -> {
            LocalPlayer player = player();
            String dimension = player.level().dimension().location().toString();
            int me = player.getId();
            List<Pig> pigs = player.level().getEntitiesOfClass(Pig.class, new AABB(this.origin).inflate(8));
            int pig = pigs.isEmpty() ? me + 1000 : pigs.get(0).getId();
            // The server runs this command before the packets below, which arrive after it
            this.backToOrigin();
            LOG.info("AADEVTEST packets: me={} pig={} dimension={} near={} far={}", me, pig, dimension, this.nearCrusher, this.farCrusher);

            // The one valid press: toggles the auto split of the near crusher
            this.sendServerbound("control: valid press on the near crusher", BUTTON_TO_TILE, gui(this.nearCrusher, dimension, me, 0));
            // Expected: one WARN each, no effect
            this.sendServerbound("press on a crusher 20 blocks away", BUTTON_TO_TILE, gui(this.farCrusher, dimension, me, 0));
            this.sendServerbound("press at an unloaded position", BUTTON_TO_TILE, gui(new BlockPos(29999000, 64, 29999000), dimension, me, 0));
            this.sendServerbound("press on the nether crusher", BUTTON_TO_TILE, gui(this.nearCrusher, "minecraft:the_nether", me, 0));
            this.sendServerbound("press as the pig", BUTTON_TO_TILE, gui(this.nearCrusher, dimension, pig, 0));
            this.sendServerbound("press with an unknown WorldID", BUTTON_TO_TILE, gui(this.nearCrusher, "skytekx3:nowhere", me, 0));
            this.sendServerbound("press with an invalid WorldID", BUTTON_TO_TILE, gui(this.nearCrusher, "Not A Valid:ID!", me, 0));
            for (int index : new int[]{LASER, TILE_ENTITY, SYNC_PLAYER_DATA, LASER_PARTICLE}) {
                CompoundTag data = gui(this.nearCrusher, dimension, me, 0);
                data.put("Data", new CompoundTag());
                this.sendServerbound("clientbound handler index " + index, index, data);
            }
            this.sendServerbound("handler index 99", 99, new CompoundTag());
            this.sendServerbound("handler index -1", -1, new CompoundTag());
            CompoundTag foreign = new CompoundTag();
            foreign.putUUID("UUID", UUID.randomUUID());
            foreign.putString("World", dimension);
            foreign.putInt("Type", 0);
            foreign.put("Bookmarks", new ListTag());
            this.sendServerbound("player data of another UUID", PLAYER_DATA_TO_SERVER, foreign);
            CompoundTag number = gui(this.fireworkBox, dimension, me, 0);
            number.putInt("NumberID", 1);
            number.putDouble("Number", Double.NaN);
            this.sendServerbound("NaN to the firework box", NUMBER_TO_TILE, number);
            this.sendServerbound("button -1 on the XP solidifier (throws in the reactor)", BUTTON_TO_TILE, gui(this.solidifier, dimension, me, -1));
            FriendlyByteBuf garbage = new FriendlyByteBuf(Unpooled.buffer());
            garbage.writeByte(SERVERBOUND_DISCRIMINATOR);
            garbage.writeByte(10);
            garbage.writeShort(3);
            LOG.info("AADEVTEST SEND unreadable packet");
            connection().send(new ServerboundCustomPayloadPacket(CHANNEL, garbage));
            // Expected: ignored without a WARN, the player has no menu open
            this.sendServerbound("button to container without a menu", BUTTON_TO_CONTAINER, gui(this.nearCrusher, dimension, me, 0));
            this.sendServerbound("string to a crusher (no string reactor)", STRING_TO_TILE, gui(this.nearCrusher, dimension, me, 0));
        });
        this.step("packet results", 40, () -> {
            this.command("data get block " + pos(this.nearCrusher) + " IsAutoSplit");
            this.command("data get block " + pos(this.farCrusher) + " IsAutoSplit");
            this.command("execute in minecraft:the_nether run data get block " + pos(this.nearCrusher) + " IsAutoSplit");
        });
        // Finite numbers out of the slider ranges. Without the clamps of the firework box a type
        // chance of -1 makes Weight.of throw at the next shot, and a Value Play of 2e9 overflows
        // the bound of Mth.nextInt, both a ticking block entity crash. Expected: no WARN, Play 5,
        // TypeChance0 0, and the box fires (its energy drops below 1000)
        this.step("firework box: out of range numbers on a powered box", 130, () -> {
            String dimension = player().level().dimension().location().toString();
            int me = player().getId();
            this.backToOrigin();
            CompoundTag chance = gui(this.fireworkBox, dimension, me, 0);
            chance.putInt("NumberID", 6);
            chance.putDouble("Number", -1.0D);
            this.sendServerbound("type chance -1 to the firework box", NUMBER_TO_TILE, chance);
            CompoundTag play = gui(this.fireworkBox, dimension, me, 0);
            play.putInt("NumberID", 0);
            play.putDouble("Number", 2.0E9D);
            this.sendServerbound("value play 2e9 to the firework box", NUMBER_TO_TILE, play);
            // Energy for two shots: the box fires every 100 ticks while it holds 500 CF
            this.command("data merge block " + pos(this.fireworkBox) + " {Energy:1000}");
        });
        this.step("firework box: after the next shot", 20, () -> {
            this.command("data get block " + pos(this.fireworkBox) + " Play");
            this.command("data get block " + pos(this.fireworkBox) + " TypeChance0");
            this.command("data get block " + pos(this.fireworkBox) + " Energy");
        });
        // The WARN limit of PacketHandler: 30 more dropped packets within the minute of the ones
        // above. Expected: WARN lines up to 20 for this sender in the window, then one line that
        // the rest is suppressed, and the server keeps running
        this.step("packet flood: 30 packets with an unknown handler index", 20, () -> {
            for (int i = 0; i < 30; i++) {
                this.sendServerbound("flood " + i, 99, new CompoundTag());
            }
        });
    }

    private static CompoundTag gui(BlockPos pos, String world, int playerId, int button) {
        CompoundTag compound = new CompoundTag();
        compound.putInt("X", pos.getX());
        compound.putInt("Y", pos.getY());
        compound.putInt("Z", pos.getZ());
        compound.putString("WorldID", world);
        compound.putInt("PlayerID", playerId);
        compound.putInt("ButtonID", button);
        return compound;
    }

    /**
     * Sends a PacketClientToServer as raw bytes, so that any handler index can be named.
     */
    private void sendServerbound(String what, int handlerIndex, CompoundTag data) {
        LOG.info("AADEVTEST SEND {} (handler index {})", what, handlerIndex);
        FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
        buffer.writeByte(SERVERBOUND_DISCRIMINATOR);
        buffer.writeNbt(data);
        buffer.writeInt(handlerIndex);
        connection().send(new ServerboundCustomPayloadPacket(CHANNEL, buffer));
    }

    // ---- menu-open-and-validity ----------------------------------------------------------------

    /**
     * The drill upgrade dupe and the filter: a menu that belongs to the selected hotbar stack,
     * with a selected slot switched by packet while it is open.
     */
    private void scheduleMenuTests() {
        this.step("drill: give two drills and two upgrades", 10, () -> {
            this.command("clear @s");
            this.command("item replace entity @s hotbar.0 with actuallyadditions:drill_light_blue");
            this.command("item replace entity @s hotbar.1 with actuallyadditions:drill_light_blue");
            this.command("item replace entity @s hotbar.2 with actuallyadditions:drill_upgrade_speed");
            this.command("item replace entity @s hotbar.3 with actuallyadditions:drill_upgrade_three_by_three");
            player().getInventory().selected = 0;
        });
        this.step("drill: open the first drill", 15, this::openSelectedItemMenu);
        this.step("drill: put both upgrades in", 10, () -> {
            this.logMenu("drill first open");
            // Menu slots: 0 to 4 the upgrades, 5 to 31 the inventory, 32 to 40 the hotbar
            this.click(34, ClickType.QUICK_MOVE);
            this.click(35, ClickType.QUICK_MOVE);
        });
        this.step("drill: close", 10, () -> player().closeContainer());
        this.step("drill: open again", 15, this::openSelectedItemMenu);
        this.step("drill: take one upgrade out", 10, () -> {
            this.logMenu("drill second open");
            this.click(0, ClickType.QUICK_MOVE);
        });
        this.step("drill: switch the selected slot by packet", 20, () -> {
            LOG.info("AADEVTEST SEND carried item slot 1 while the drill menu is open");
            connection().send(new ServerboundSetCarriedItemPacket(1));
        });
        this.step("drill: result", 20, () -> {
            this.logMenu("drill after the slot switch");
            player().getInventory().selected = 1;
            connection().send(new ServerboundSetCarriedItemPacket(1));
            this.command("data get entity @s Inventory");
        });
        this.step("filter: give a filter", 10, () -> {
            this.command("clear @s");
            this.command("item replace entity @s hotbar.0 with actuallyadditions:filter");
            this.command("item replace entity @s hotbar.1 with actuallyadditions:filter");
            player().getInventory().selected = 0;
            connection().send(new ServerboundSetCarriedItemPacket(0));
        });
        this.step("filter: open", 15, this::openSelectedItemMenu);
        this.step("filter: switch the selected slot by packet", 20, () -> {
            this.logMenu("filter open");
            LOG.info("AADEVTEST SEND carried item slot 1 while the filter menu is open");
            connection().send(new ServerboundSetCarriedItemPacket(1));
        });
        this.step("filter: result", 10, () -> {
            this.logMenu("filter after the slot switch");
            player().getInventory().selected = 1;
            connection().send(new ServerboundSetCarriedItemPacket(1));
            this.command("clear @s");
        });
        // The machine screens that must close at more than 8 blocks: see the block exercise
    }

    /**
     * Waits until the client has been teleported away (the server may lag), then logs whether the
     * server closed the screen.
     */
    private void checkClosedAway(ResourceLocation id, int tries) {
        boolean away = player().getX() < this.origin.getX() - 5;
        if (Minecraft.getInstance().screen != null && !away && tries < 40) {
            this.steps.addFirst(new Step("block " + id + ": wait for the teleport", 1, () -> this.checkClosedAway(id, tries + 1)));
            return;
        }
        if (Minecraft.getInstance().screen != null) {
            // Give the server up to 40 more ticks to notice the distance
            if (tries < 40) {
                this.steps.addFirst(new Step("block " + id + ": wait for the server", 1, () -> this.checkClosedAway(id, tries + 1)));
                return;
            }
            LOG.info("AADEVTEST MENU {} still open {} blocks away: screen {}", id, Math.round(this.origin.getX() + 0.5 - player().getX()), Minecraft.getInstance().screen.getClass().getSimpleName());
            player().closeContainer();
        } else if (away) {
            LOG.info("AADEVTEST MENU {} closed by the server {} blocks away", id, Math.round(this.origin.getX() + 0.5 - player().getX()));
        }
        this.backToOrigin();
    }

    private void openSelectedItemMenu() {
        Minecraft.getInstance().options.keyShift.setDown(true);
        this.steps.addFirst(new Step("use the selected item while sneaking", 10, () -> {
            Minecraft.getInstance().gameMode.useItem(player(), InteractionHand.MAIN_HAND);
            this.steps.addFirst(new Step("stop sneaking", 5, () -> Minecraft.getInstance().options.keyShift.setDown(false)));
        }));
    }

    private void click(int slot, ClickType type) {
        LocalPlayer player = player();
        LOG.info("AADEVTEST CLICK slot {} {} in menu {}", slot, type, player.containerMenu.containerId);
        Minecraft.getInstance().gameMode.handleInventoryMouseClick(player.containerMenu.containerId, slot, 0, type, player);
    }

    private void logMenu(String what) {
        Minecraft mc = Minecraft.getInstance();
        LOG.info("AADEVTEST MENU {}: screen {}, menu {}", what, mc.screen == null ? "none" : mc.screen.getClass().getSimpleName(), player().containerMenu.getClass().getSimpleName());
    }

    // ---- dedicated-server-client-split ---------------------------------------------------------

    /**
     * Places, right clicks, wrenches and breaks every block of Actually Additions and uses every
     * item, so that the dedicated server runs their server code at least once.
     */
    private void scheduleBlockAndItemTests() {
        List<ResourceLocation> blocks = new ArrayList<>();
        for (Map.Entry<net.minecraft.resources.ResourceKey<Block>, Block> entry : ForgeRegistries.BLOCKS.getEntries()) {
            if (entry.getKey().location().getNamespace().equals("actuallyadditions")) {
                blocks.add(entry.getKey().location());
            }
        }
        List<ResourceLocation> items = new ArrayList<>();
        for (Map.Entry<net.minecraft.resources.ResourceKey<Item>, Item> entry : ForgeRegistries.ITEMS.getEntries()) {
            if (entry.getKey().location().getNamespace().equals("actuallyadditions") && !(entry.getValue() instanceof BlockItem)) {
                items.add(entry.getKey().location());
            }
        }
        blocks.sort(null);
        items.sort(null);
        this.step("blocks and items: " + blocks.size() + " blocks, " + items.size() + " items", 5, () -> {
            this.command("clear @s");
            this.command("item replace entity @s hotbar.0 with actuallyadditions:laser_wrench");
        });
        List<String> skip = List.of(this.props.getProperty("skip", "").split(","));
        for (ResourceLocation id : blocks) {
            if (skip.contains(id.toString())) {
                this.step("block " + id + ": skipped (aadevtest.properties)", 0, () -> LOG.warn("AADEVTEST SKIP {}", id));
                continue;
            }
            this.step("block " + id + ": place", 4, () -> {
                this.backToOrigin();
                this.command("setblock " + pos(this.testSpot) + " air");
                this.command("setblock " + pos(this.testSpot) + " " + id);
            });
            this.step("block " + id + ": right click", 4, () -> {
                player().getInventory().selected = 1;
                this.useOn(this.testSpot);
            });
            // menu-open-and-validity: an open machine screen must close once the player is more
            // than 8 blocks away. The server ignores block clicks until the client confirms a
            // teleport, so the player goes back a few ticks before the next click
            this.step("block " + id + ": screen", 10, () -> {
                if (Minecraft.getInstance().screen != null) {
                    this.logMenu(id + " opened");
                    this.command("tp @s " + (this.origin.getX() - 10) + ".5 " + this.origin.getY() + " " + this.origin.getZ() + ".5");
                }
            });
            this.step("block " + id + ": screen 10 blocks away", 5, () -> this.checkClosedAway(id, 0));
            this.step("block " + id + ": wrench", 4, () -> {
                player().getInventory().selected = 0;
                this.useOn(this.testSpot);
            });
            this.step("block " + id + ": break", 3, () -> {
                this.logAndCloseScreen(id + " with the wrench");
                Minecraft.getInstance().gameMode.startDestroyBlock(this.testSpot, Direction.UP);
            });
        }
        for (ResourceLocation id : items) {
            this.step("item " + id + ": give", 3, () -> {
                this.backToOrigin();
                this.command("setblock " + pos(this.testSpot) + " minecraft:stone");
                this.command("item replace entity @s hotbar.2 with " + id);
                player().getInventory().selected = 2;
            });
            this.step("item " + id + ": use", 4, () -> {
                Minecraft.getInstance().gameMode.useItem(player(), InteractionHand.MAIN_HAND);
            });
            this.step("item " + id + ": use on a block", 4, () -> {
                Minecraft.getInstance().gameMode.releaseUsingItem(player());
                this.logAndCloseScreen(id.toString());
                this.useOn(this.testSpot);
            });
            this.step("item " + id + ": clean up", 2, () -> {
                Minecraft.getInstance().gameMode.releaseUsingItem(player());
                this.logAndCloseScreen(id + " on a block");
                this.command("item replace entity @s hotbar.2 with minecraft:air");
            });
        }
    }

    private void backToOrigin() {
        this.command("tp @s " + this.origin.getX() + ".5 " + this.origin.getY() + " " + this.origin.getZ() + ".5 0 0");
    }

    private void useOn(BlockPos pos) {
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        Minecraft.getInstance().gameMode.useItemOn(player(), InteractionHand.MAIN_HAND, hit);
    }

    private void logAndCloseScreen(String what) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.screen != null) {
            LOG.info("AADEVTEST SCREEN {} opened {} (menu {})", what, mc.screen.getClass().getSimpleName(), player().containerMenu.getClass().getSimpleName());
            player().closeContainer();
        }
    }

    // ---- helpers -------------------------------------------------------------------------------

    private void command(String command) {
        LOG.info("AADEVTEST CMD /{}", command);
        connection().sendCommand(command);
    }

    private static String pos(BlockPos pos) {
        return pos.getX() + " " + pos.getY() + " " + pos.getZ();
    }

    private static LocalPlayer player() {
        return Minecraft.getInstance().player;
    }

    private static net.minecraft.client.multiplayer.ClientPacketListener connection() {
        return Minecraft.getInstance().getConnection();
    }
}
