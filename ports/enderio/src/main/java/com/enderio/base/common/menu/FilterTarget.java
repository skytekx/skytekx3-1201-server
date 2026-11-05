package com.enderio.base.common.menu;

import com.enderio.api.conduit.SlotType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Where the stack of an open filter menu lives, as written into the menu buffer. Ender IO 8.x opens filter menus from the
 * conduit screen (04e69b8e6, ServerboundOpenConduitFilterMenu) with its AbstractFilterMenu.FilterAccess. 6.x filter menus only
 * knew the main hand.
 */
public sealed interface FilterTarget permits FilterTarget.Hand, FilterTarget.Conduit {

    FilterTarget HAND = new Hand();

    void write(FriendlyByteBuf buf);

    /**
     * Read a target. A menu opened with the vanilla openMenu has no buffer at all (Forge passes null) or an empty one, which
     * means the main hand, as every 6.x filter menu did.
     */
    static FilterTarget read(@Nullable FriendlyByteBuf buf) {
        if (buf == null || !buf.isReadable()) {
            return HAND;
        }

        return switch (buf.readByte()) {
            case Conduit.ID -> new Conduit(buf.readBlockPos(), buf.readResourceLocation(), buf.readEnum(Direction.class), buf.readEnum(SlotType.class));
            default -> HAND;
        };
    }

    /**
     * The filter in the main hand of the player, the 6.x behaviour.
     */
    record Hand() implements FilterTarget {
        static final byte ID = 0;

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeByte(ID);
        }
    }

    /**
     * The filter in a slot of a conduit connection.
     *
     * @param pos         the position of the conduit bundle
     * @param conduitType the registry id of the conduit type
     * @param direction   the side of the connection
     * @param slotType    the filter slot of the connection
     */
    record Conduit(BlockPos pos, ResourceLocation conduitType, Direction direction, SlotType slotType) implements FilterTarget {
        static final byte ID = 1;

        @Override
        public void write(FriendlyByteBuf buf) {
            buf.writeByte(ID);
            buf.writeBlockPos(pos);
            buf.writeResourceLocation(conduitType);
            buf.writeEnum(direction);
            buf.writeEnum(slotType);
        }
    }
}
