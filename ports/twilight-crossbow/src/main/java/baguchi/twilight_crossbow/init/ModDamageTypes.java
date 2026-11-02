package baguchi.twilight_crossbow.init;

import baguchi.twilight_crossbow.TwilightCrossBow;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> ICE_FIREWORK = create("ice_firework");

    public static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, TwilightCrossBow.prefix(name));
    }
}
