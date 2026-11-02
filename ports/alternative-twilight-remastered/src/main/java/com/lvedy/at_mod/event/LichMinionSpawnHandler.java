package com.lvedy.at_mod.event;

import com.lvedy.at_mod.AlternativeTwilight;
import com.lvedy.at_mod.config.Config;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import twilightforest.entity.boss.Lich;
import twilightforest.entity.monster.LichMinion;

@Mod.EventBusSubscriber(modid = AlternativeTwilight.MODID)
public class LichMinionSpawnHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) return;
        if (!(event.getEntity() instanceof LichMinion minion)) return;

        Lich master = minion.master;
        if (master == null) return;

        // 只在二阶段应用属性
        if (master.getPhase() != 2) return;

        var maxHealthAttr = minion.getAttribute(Attributes.MAX_HEALTH);
        if (maxHealthAttr != null) {
            maxHealthAttr.setBaseValue(Config.LICH_MINION_MAX_HEALTH.get());
            minion.setHealth(Config.LICH_MINION_MAX_HEALTH.get().floatValue());
        }

        var armorAttr = minion.getAttribute(Attributes.ARMOR);
        if (armorAttr != null) {
            armorAttr.setBaseValue(Config.LICH_MINION_ARMOR.get());
        }

        var armorToughnessAttr = minion.getAttribute(Attributes.ARMOR_TOUGHNESS);
        if (armorToughnessAttr != null) {
            armorToughnessAttr.setBaseValue(Config.LICH_MINION_ARMOR_TOUGHNESS.get());
        }
    }
}
