package de.ellpeck.actuallyadditions.mod.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.BlockSnapshot;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.event.ForgeEventFactory;
import net.minecraftforge.event.level.BlockEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.UUID;

/**
 * SkyTekx: a local change, upstream uses the shared [Minecraft] fake player of Forge everywhere. The
 * machines of the mod break, place and convert blocks as one named fake player, [ActuallyAdditions]
 * with a fixed UUID, so protection mods and, on Arclight, Bukkit plugins can tell them apart from
 * other mods and trust or refuse them. Forge caches one such player per level and profile, gives it
 * its own FakePlayerNetHandler and drops it when the level unloads (FakePlayerFactory#unloadLevel),
 * so nothing here keeps a reference to it. Every use stands it at the machine with empty hands and
 * gives back its hands and position when it ends. A block that one machine places can pulse another
 * machine, whose use then runs inside the first one and must not empty the hand the first one still
 * reads
 */
public final class AAFakePlayers {

    public static final GameProfile PROFILE = new GameProfile(UUID.fromString("5bc9ca5e-449d-4e9f-9707-7d8d34e2eecd"), "[ActuallyAdditions]");

    // Arclight turns every posted BreakEvent into a Bukkit BlockBreakEvent and pushes a capture context
    // onto ArclightCaptures.blockBreakEventStack (BlockBreakEventDispatcher#onBreakBlock). Only the break
    // of a real player (ServerPlayerGameMode#destroyBlock) pops it again. A context left behind by a
    // machine catches every later Block#popResource drop of the server, which Arclight overwrites to
    // fill the top context instead of spawning the item, until the next block a player mines clears the
    // stack and loses them. The helpers below take back the contexts their own event pushed. Without
    // Arclight the class is missing and this stays null
    private static final Field ARCLIGHT_BREAK_CONTEXTS = findArclightBreakContexts();

    private AAFakePlayers() {
    }

    public static FakePlayer get(ServerLevel level) {
        return FakePlayerFactory.get(level, PROFILE);
    }

    /**
     * Stands the fake player in the middle of the machine at that position with empty hands. Closing
     * the use gives back the hands and the position it had before
     */
    public static Use use(ServerLevel level, BlockPos machine) {
        return new Use(get(level), machine);
    }

    public static final class Use implements AutoCloseable {

        private final FakePlayer player;
        private final int selected;
        private final ItemStack mainBefore;
        private final ItemStack offBefore;
        private final double xBefore;
        private final double yBefore;
        private final double zBefore;

        private Use(FakePlayer player, BlockPos machine) {
            this.player = player;
            this.selected = player.getInventory().selected;
            this.mainBefore = player.getInventory().items.get(this.selected);
            this.offBefore = player.getInventory().offhand.get(0);
            this.xBefore = player.getX();
            this.yBefore = player.getY();
            this.zBefore = player.getZ();
            player.setPos(machine.getX() + 0.5, machine.getY() + 0.5, machine.getZ() + 0.5);
            player.getInventory().items.set(this.selected, ItemStack.EMPTY);
            player.getInventory().offhand.set(0, ItemStack.EMPTY);
        }

        public FakePlayer player() {
            return this.player;
        }

        public void hold(ItemStack stack) {
            this.player.getInventory().items.set(this.selected, stack);
        }

        @Override
        public void close() {
            this.player.getInventory().items.set(this.selected, this.mainBefore);
            this.player.getInventory().offhand.set(0, this.offBefore);
            this.player.setPos(this.xBefore, this.yBefore, this.zBefore);
        }
    }

    /**
     * Posts the break event and takes back what Arclight captured for it. Answers true when the block
     * may go
     */
    public static boolean postBreak(BlockEvent.BreakEvent event) {
        List<?> contexts = arclightBreakContexts();
        int before = contexts == null ? 0 : contexts.size();
        boolean canceled;
        try {
            canceled = MinecraftForge.EVENT_BUS.post(event);
        } finally {
            if (contexts != null) {
                while (contexts.size() > before) {
                    contexts.remove(contexts.size() - 1);
                }
            }
        }
        return !canceled;
    }

    /**
     * Asks whether the machine at that position may break the block at pos, as the fake player with an
     * empty hand
     */
    public static boolean mayBreak(ServerLevel level, BlockPos machine, BlockPos pos) {
        try (Use use = use(level, machine)) {
            return postBreak(new BlockEvent.BreakEvent(level, pos, level.getBlockState(pos), use.player()));
        }
    }

    /**
     * Sets the block as the machine's fake player and posts the place event of Forge, which Arclight
     * hands to Bukkit as a BlockPlaceEvent (BlockPlaceEventDispatcher#onBlockPlace, once
     * ForgeEventFactory#onBlockPlace has captured the face). The event comes after the change, as at
     * ForgeHooks#onPlaceItemIntoWorld, because a player's place event reads the new block from the
     * level. A refused placement puts the snapshot back. Answers true when the new block stays
     *
     * @param face   The face of the block that the machine works on, as the clicked face of a player
     * @param placed The item the fake player holds while the event runs, for the plugins that log it
     */
    public static boolean placeBlock(ServerLevel level, BlockPos machine, BlockPos pos, BlockState state, int flags, Direction face, ItemStack placed) {
        try (Use use = use(level, machine)) {
            use.hold(placed.copy());
            BlockSnapshot snapshot = BlockSnapshot.create(level.dimension(), level, pos, flags);
            if (!level.setBlock(pos, state, flags)) {
                return false;
            }
            if (ForgeEventFactory.onBlockPlace(use.player(), snapshot, face)) {
                level.restoringBlockSnapshots = true;
                try {
                    snapshot.restore(true, false);
                } finally {
                    level.restoringBlockSnapshots = false;
                }
                return false;
            }
            return true;
        }
    }

    private static Field findArclightBreakContexts() {
        try {
            Class<?> captures = Class.forName("io.izzel.arclight.common.mod.util.ArclightCaptures", false, AAFakePlayers.class.getClassLoader());
            Field field = captures.getField("blockBreakEventStack");
            if (Modifier.isStatic(field.getModifiers()) && List.class.isAssignableFrom(field.getType())) {
                return field;
            }
        } catch (ReflectiveOperationException | LinkageError | SecurityException ignored) {
        }
        return null;
    }

    private static List<?> arclightBreakContexts() {
        if (ARCLIGHT_BREAK_CONTEXTS != null) {
            try {
                return (List<?>) ARCLIGHT_BREAK_CONTEXTS.get(null);
            } catch (IllegalAccessException | ClassCastException ignored) {
            }
        }
        return null;
    }
}
