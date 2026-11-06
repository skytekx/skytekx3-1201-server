package de.ellpeck.actuallyadditions.mod.network.packet;

import de.ellpeck.actuallyadditions.mod.items.ItemCrafterOnAStick;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;

/**
 * SkyTekx: the hotkeys of b5cb3434. Main sends them as their own payload, here they are the
 * handler PacketHandler.HOTKEY_HANDLER of the mod's SimpleChannel, which reads the ordinal of a
 * {@link HotKey} from the NBT key Hotkey.
 */
public final class HotkeyPacket {

    public static final String HOTKEY_KEY = "Hotkey";

    public enum HotKey {
        OPEN_CRAFTING_STICK
    }

    private HotkeyPacket() {
    }

    public static CompoundTag create(HotKey hotKey) {
        CompoundTag compound = new CompoundTag();
        compound.putByte(HOTKEY_KEY, (byte) hotKey.ordinal());
        return compound;
    }

    /**
     * @return false for an unknown hotkey
     */
    public static boolean handle(CompoundTag compound, ServerPlayer player) {
        int ordinal = compound.getByte(HOTKEY_KEY);
        if (ordinal < 0 || ordinal >= HotKey.values().length) {
            return false;
        }
        switch (HotKey.values()[ordinal]) {
            case OPEN_CRAFTING_STICK -> {
                if (ItemCrafterOnAStick.hasCrafterOnAStick(player)) {
                    ItemCrafterOnAStick.openCraftingMenu(player);
                }
            }
        }
        return true;
    }
}
