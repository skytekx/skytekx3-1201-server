package de.ellpeck.actuallyadditions.mod.lootmodifier;

import com.google.common.base.Supplier;
import com.google.common.base.Suppliers;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import de.ellpeck.actuallyadditions.mod.config.CommonConfig;
import de.ellpeck.actuallyadditions.mod.items.ActuallyItems;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.ambient.Bat;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;

// SkyTekx: upstream b9435301. A bat that a player kills drops Bat Wings, more often and more of them with
// looting, while doBatDrops is on. The killer is KILLER_ENTITY on 1.20.1 (main: ATTACKING_ENTITY), and the
// looting level comes from Forge, which also asks the LootingLevelEvent
public class BatLootModifier extends LootModifier {
	public static final Supplier<Codec<BatLootModifier>> CODEC = Suppliers.memoize(() ->
			RecordCodecBuilder.create(inst -> codecStart(inst).apply(inst, BatLootModifier::new)));

	public BatLootModifier(LootItemCondition[] conditionsIn) {
		super(conditionsIn);
	}

	@Override
	protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
		RandomSource random = context.getRandom();
		if (CommonConfig.Other.DO_BAT_DROPS.get() &&
				context.hasParam(LootContextParams.KILLER_ENTITY) &&
				context.hasParam(LootContextParams.DAMAGE_SOURCE) &&
				context.hasParam(LootContextParams.THIS_ENTITY) &&
				context.getParam(LootContextParams.THIS_ENTITY) instanceof Bat) {
			int looting = context.getLootingModifier();
			if (random.nextInt(15) <= looting * 2) {
				generatedLoot.add(new ItemStack(ActuallyItems.BATS_WING.get(), random.nextInt(2 + looting) + 1));
			}
		}
		return generatedLoot;
	}

	@Override
	public Codec<? extends IGlobalLootModifier> codec() {
		return ActuallyLootModifiers.BAT_LOOT.get();
	}
}
