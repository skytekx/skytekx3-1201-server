package com.mrh0.createaddition.item;

import com.mrh0.createaddition.config.Config;
import com.mrh0.createaddition.index.CAEffects;
import com.mrh0.createaddition.index.CASounds;

import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.energy.IEnergyStorage;

/**
 * Held in either hand, the amulet now and then charges the FE item in the other hand (upstream
 * ElectrumAmuletItem of 5c2b9ea, release 1.6.0). It replaces the Luck and Speed below four hearts
 * of the 1.20.1 base. The class keeps its 1.20.1 name.
 */
public class ElectrumAmulet extends Item {
    private static final int CHARGE_CHANCE = 300;

    public ElectrumAmulet(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public Rarity getRarity(ItemStack pStack) {
        return Rarity.UNCOMMON;
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int pSlotId, boolean pIsSelected) {
        if (!(entity instanceof Player player)) return;
        if (level.isClientSide) return;
        if (!Config.AMULET_EFFECT_ENABLED.get()) return;

        ItemStack mainHand = player.getMainHandItem();
        ItemStack offHand = player.getOffhandItem();

        ItemStack toCharge;
        if (mainHand == stack) toCharge = offHand;
        else if (offHand == stack) toCharge = mainHand;
        else return;

        IEnergyStorage es = toCharge.getCapability(ForgeCapabilities.ENERGY).orElse(null);
        boolean canCharge = es != null && es.receiveEnergy(1, true) > 0;

        if (canCharge && level.random.nextInt(CHARGE_CHANCE) == 0) {
            // Average rate times the chance, in long so a large rate cannot overflow.
            es.receiveEnergy((int) Math.min(Integer.MAX_VALUE, (long) CHARGE_CHANCE * Config.ELECTRUM_AMULET_CHARGE_RATE.get()), false);
            player.addEffect(new MobEffectInstance(CAEffects.SHOCKING.get(), 40, 0, false, true, true));
            if (Config.AUDIO_ENABLED.get())
                level.playSound(null, player.blockPosition(), CASounds.LITTLE_ZAP.get(), SoundSource.PLAYERS, 0.4f, 1f);
        }
    }
}
