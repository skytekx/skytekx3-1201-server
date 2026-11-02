package com.lvedy.at_mod.register;

import com.lvedy.at_mod.AlternativeTwilight;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

// 1.20.1 backport: the sound events stay registered, but the backport jar ships no music files (see NOTICE);
// a resource pack can define at_mod:music.<boss> in assets/at_mod/sounds.json to supply the tracks.
public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, AlternativeTwilight.MODID);

    public static final RegistryObject<SoundEvent> NAGA_MUSIC = music("naga");
    public static final RegistryObject<SoundEvent> LICH_MUSIC = music("lich");
    public static final RegistryObject<SoundEvent> HYDRA_MUSIC = music("hydra");
    public static final RegistryObject<SoundEvent> UR_GHAST_MUSIC = music("ur_ghast");
    public static final RegistryObject<SoundEvent> KNIGHT_PHANTOM_MUSIC = music("knight_phantom");
    public static final RegistryObject<SoundEvent> SNOW_QUEEN_MUSIC = music("snow_queen");
    public static final RegistryObject<SoundEvent> MINOSHROOM_MUSIC = music("minoshroom");
    public static final RegistryObject<SoundEvent> ALPHA_YETI_MUSIC = music("alpha_yeti");

    private static RegistryObject<SoundEvent> music(String boss) {
        String name = "music." + boss;
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(AlternativeTwilight.prefix(name)));
    }

    public static void register(IEventBus bus) {
        SOUND_EVENTS.register(bus);
    }
}
