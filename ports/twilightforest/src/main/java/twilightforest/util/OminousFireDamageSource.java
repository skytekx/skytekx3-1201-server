package twilightforest.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.network.chat.Component;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;

/**
 * Re-wraps the attacks of a zombie risen from a player killed by ominous fire, so deaths name the player it used to be.
 */
public class OminousFireDamageSource extends DamageSource {
	public OminousFireDamageSource(DamageSource wrappedSource) {
		super(wrappedSource.typeHolder(), wrappedSource.getDirectEntity(), wrappedSource.getEntity(), wrappedSource.getSourcePosition());
	}

	@Override
	public Component getLocalizedDeathMessage(LivingEntity living) {
		if (living.getKillCredit() instanceof Zombie zombie) {
			GameProfile profile = EntityUtil.getZombifiedPlayer(zombie);
			if (profile != null) {
				if (living instanceof Player player && player.getGameProfile().getName().equals(profile.getName())) {
					return Component.translatable("death.attack.twilightforest.ominousFire.zombified_player.self", living.getDisplayName());
				}
				return Component.translatable("death.attack.twilightforest.ominousFire.zombified_player", living.getDisplayName(), profile.getName());
			}
		}
		return super.getLocalizedDeathMessage(living);
	}
}
