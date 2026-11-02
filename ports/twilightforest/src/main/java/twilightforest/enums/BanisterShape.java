package twilightforest.enums;

import net.minecraft.util.StringRepresentable;

import java.util.Locale;

public enum BanisterShape implements StringRepresentable {
    SHORT,
    TALL,
    CONNECTED;
    // TODO Re-add these -- Their model issues have been resolved; now they just need voxel shapes and integration into cycling via axe.
    //  Adding them changes the banister blockstate property values, so keep them out of the enum until that work happens.
    // TILT_RIGHT,
    // TILT_LEFT;

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }
}
