package com.lvedy.at_mod.register;

import com.lvedy.at_mod.AlternativeTwilight;
import com.lvedy.at_mod.config.Config;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, AlternativeTwilight.MODID);

    // 1.20.1 backport: attribute modifiers are keyed by UUID (name-based UUIDs of the upstream ids
    // at_mod:effect.blood_lust.attack / .speed), and the per-level amount curve becomes
    // getAttributeModifierValue, which reads the config every time the effect is (re)applied.
    public static final RegistryObject<MobEffect> BLOOD_LUST =
            EFFECTS.register("blood_lust", () -> new ConfigScaledEffect(MobEffectCategory.BENEFICIAL, 0xB30000)
                    .addAttributeModifier(Attributes.ATTACK_DAMAGE,
                            "dc16dd99-c400-3331-b1aa-fa16b87c13ec",
                            0.0D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED,
                            "92a41c6a-2a77-33b1-96f8-a9b287885092",
                            0.0D,
                            AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> BLOOD_LUST_CURSE =
            EFFECTS.register("blood_lust_curse", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x4A0000) {});

    /**
     * Blood Lust: +attackPerLevel / +speedPerLevel (multiplied total) per amplifier level, read from the config.
     */
    private static class ConfigScaledEffect extends MobEffect {
        protected ConfigScaledEffect(MobEffectCategory category, int color) {
            super(category, color);
        }

        @Override
        public double getAttributeModifierValue(int amplifier, AttributeModifier modifier) {
            Supplier<Double> perLevel = "92a41c6a-2a77-33b1-96f8-a9b287885092".equals(modifier.getId().toString())
                    ? Config.BLOOD_LUST_SPEED_PER_LEVEL
                    : Config.BLOOD_LUST_ATTACK_PER_LEVEL;
            return readConfig(perLevel) * (amplifier + 1);
        }
    }

    /**
     * 配置尚未加载时（例如注册阶段）读取 Config 会抛 IllegalStateException，此时返回 0。
     */
    private static double readConfig(Supplier<Double> supplier) {
        try {
            return supplier.get();
        } catch (IllegalStateException notLoadedYet) {
            return 0.0D;
        }
    }

    public static void register(IEventBus bus) {
        EFFECTS.register(bus);
    }
}
