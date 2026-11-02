package baguchi.spiced_twilight.util;

import baguchi.spiced_twilight.SpicedTwilight;
import twilightforest.entity.boss.Lich;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

/**
 * Upstream skips the minion conversion while the 4.8 Lich is invisible after a teleport
 * ({@code Lich#getTeleportInvisibility}). The SkyTekx fork only gains that method with its Lich rework, so it is looked
 * up once at runtime: until then there is no teleport invisibility and the check always passes.
 */
public final class LichCompat {
    private static final MethodHandle GET_TELEPORT_INVISIBILITY = find();

    private LichCompat() {
    }

    private static MethodHandle find() {
        try {
            return MethodHandles.publicLookup().findVirtual(Lich.class, "getTeleportInvisibility", MethodType.methodType(int.class));
        } catch (NoSuchMethodException | IllegalAccessException e) {
            SpicedTwilight.LOGGER.debug("Lich#getTeleportInvisibility not present, skipping that check");
            return null;
        }
    }

    public static int getTeleportInvisibility(Lich lich) {
        if (GET_TELEPORT_INVISIBILITY == null) {
            return 0;
        }
        try {
            return (int) GET_TELEPORT_INVISIBILITY.invokeExact(lich);
        } catch (Throwable e) {
            return 0;
        }
    }
}
