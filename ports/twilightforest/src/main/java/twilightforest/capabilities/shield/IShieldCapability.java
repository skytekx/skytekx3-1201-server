package twilightforest.capabilities.shield;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.util.INBTSerializable;
import twilightforest.TwilightForestMod;

public interface IShieldCapability extends INBTSerializable<CompoundTag> {

	ResourceLocation ID = TwilightForestMod.prefix("cap_shield");

	void setEntity(LivingEntity entity);

	void update();

	int shieldsLeft();

	int temporaryShieldsLeft();

	int permanentShieldsLeft();

	/**
	 * Breaks one shield, temporary ones first.
	 *
	 * @param expired true when a temporary shield ran out of time instead of absorbing a hit. Expired shields play the
	 *                expire sound and do not count towards the shields broken statistic.
	 */
	void breakShield(boolean expired);

	default void breakShield() {
		this.breakShield(false);
	}

	void replenishShields();

	void setShields(int amount, boolean temp);

    void addShields(int amount, boolean temp);
}
