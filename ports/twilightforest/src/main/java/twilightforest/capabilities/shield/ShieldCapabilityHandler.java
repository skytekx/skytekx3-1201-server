package twilightforest.capabilities.shield;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import twilightforest.init.TFParticleType;
import twilightforest.init.TFSounds;
import twilightforest.init.TFStats;
import twilightforest.network.ParticlePacket;
import twilightforest.network.TFPacketHandler;
import twilightforest.network.UpdateShieldPacket;

public class ShieldCapabilityHandler implements IShieldCapability {
	/**
	 * Upper bound for both shield counts (4.8 e47e4546e), so commands cannot stack an absurd number of shields.
	 */
	public static final int MAX_SHIELDS = 115;

	private int temporaryShields;
	private int permanentShields;
	private LivingEntity host;
	private int timer;

	@Override
	public void setEntity(LivingEntity entity) {
		this.host = entity;
	}

	@Override
	public void update() {
		// Temporary shields only count down on the server, and not at all for invulnerable (creative or spectator) players.
		// Breaking is paced by the host's hurt i-frames (see CapabilityEvents), so an expiry is no longer held back after a hit.
		if (!this.host.level().isClientSide() && this.temporaryShieldsLeft() > 0 && !(this.host instanceof Player player && player.getAbilities().invulnerable)) {
			if (this.timer <= 0) {
				this.breakShield(true);
			} else {
				this.timer--;
			}
		}
	}

	@Override
	public int shieldsLeft() {
		return this.temporaryShields + this.permanentShields;
	}

	@Override
	public int temporaryShieldsLeft() {
		return this.temporaryShields;
	}

	@Override
	public int permanentShieldsLeft() {
		return this.permanentShields;
	}

	@Override
	public void breakShield(boolean expired) {
		// Temp shields should break first before permanent ones. Reset time each time a temp shield is busted.
		if (this.temporaryShields > 0) {
			this.temporaryShields--;
			this.resetTimer();
		} else if (this.permanentShields > 0) {
			this.permanentShields--;
		}

		if (this.host instanceof ServerPlayer player && !expired)
			player.awardStat(TFStats.TF_SHIELDS_BROKEN.get());
		this.sendUpdatePacket();
		this.host.level().playSound(null, this.host.blockPosition(), expired ? TFSounds.SHIELD_EXPIRE.get() : TFSounds.SHIELD_BREAK.get(), SoundSource.PLAYERS, 1.0F, (this.host.getRandom().nextFloat() - this.host.getRandom().nextFloat()) * 0.3F + 1.0F);
	}

	/**
	 * Sends a burst of shield shards from the side of the entity that the damage came from, or a ring around it when
	 * the damage has no position. Server side only.
	 */
	public static void addShieldBreakParticles(DamageSource src, LivingEntity entity) {
		ParticlePacket particlePacket = new ParticlePacket();

		Vec3 pos = src.getSourcePosition();
		if (src.getDirectEntity() instanceof LivingEntity living) pos = living.getEyePosition(); // If entity is not null, transfer the position to the eye level
		if (src.getEntity() instanceof TraceableEntity traceable && traceable.getOwner() instanceof LivingEntity living) pos = living.getEyePosition(); // If entity is not null, transfer the position to the eye level

		if (pos != null) {
			Vec3 entityPos = entity.position().add(0.0D, entity.getBbHeight() * 0.65D, 0.0D);
			Vec3 offset = pos.subtract(entityPos).multiply(1.0D, 0.0D, 1.0D).normalize();
			pos = entityPos.add(offset.scale(0.55D));

			double sizeRange = 0.85D; // Change this value to change size of the plane in which the particle may be placed

			for (int j = 0; j < 16; ++j) {
				double horizontal = entity.getRandom().nextDouble() - 0.5D;
				double x = sizeRange * offset.z * horizontal;
				double y = sizeRange * (entity.getRandom().nextDouble() - 0.5D);
				double z = sizeRange * offset.x * -horizontal;
				particlePacket.queueParticle(TFParticleType.SHIELD_BREAK.get(), false, pos.x + x, pos.y + y, pos.z + z, x * 0.5D, y * 0.5D, z * 0.5D);
			}
		} else {
			pos = entity.position().add(0.0D, entity.getBbHeight() * 0.65D, 0.0D);
			for (int j = 0; j < 16; ++j) {
				double x = (entity.getRandom().nextDouble() - 0.5D);
				double y = (entity.getRandom().nextDouble() - 0.5D) * 0.25D;
				double z = (entity.getRandom().nextDouble() - 0.5D);
				particlePacket.queueParticle(TFParticleType.SHIELD_BREAK.get(), false, pos.x + x, pos.y + y, pos.z + z, x * 0.33D, y * 0.33D, z * 0.33D);
			}
		}

		TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> entity), particlePacket);
	}

	@Override
	public void replenishShields() {
		this.setShields(5, true);
		this.host.level().playSound(null, this.host.blockPosition(), TFSounds.SHIELD_ADD.get(), SoundSource.PLAYERS, 1.0F, (this.host.getRandom().nextFloat() - this.host.getRandom().nextFloat()) * 0.2F + 1.0F);
	}

	@Override
	public void setShields(int amount, boolean temp) {
		if (temp) {
			this.temporaryShields = Mth.clamp(amount, 0, MAX_SHIELDS);
			this.resetTimer();
		} else {
			this.permanentShields = Mth.clamp(amount, 0, MAX_SHIELDS);
		}

		this.sendUpdatePacket();
	}

	@Override
	public void addShields(int amount, boolean temp) {
		if (temp) {
			if (this.temporaryShields <= 0)
				this.resetTimer(); // Since we add new shields to the stack instead of setting them, no timer reset is needed, unless they start from 0 shields.
			this.temporaryShields = Mth.clamp(this.temporaryShields + amount, 0, MAX_SHIELDS);
		} else {
			this.permanentShields = Mth.clamp(this.permanentShields + amount, 0, MAX_SHIELDS);
		}

		this.sendUpdatePacket();
	}

	void initShields(int temporary, int permanent) {
		this.temporaryShields = Math.max(temporary, 0);
		this.permanentShields = Math.max(permanent, 0);
		this.resetTimer();
	}

	private void resetTimer() {
		this.timer = 240;
	}

	/**
	 * Syncs the shield count of any shielded entity, not only players, to everyone tracking it (4.8 327db8d34), so the
	 * shield layer of a mob also disappears when its last shield breaks.
	 */
	private void sendUpdatePacket() {
		if (!this.host.level().isClientSide())
			TFPacketHandler.CHANNEL.send(PacketDistributor.TRACKING_ENTITY_AND_SELF.with(() -> this.host), new UpdateShieldPacket(this.host, this));
	}

	@Override
	public CompoundTag serializeNBT() {
		CompoundTag tag = new CompoundTag();
		tag.putInt("tempshields", this.temporaryShieldsLeft());
		tag.putInt("permshields", this.permanentShieldsLeft());
		return tag;
	}

	@Override
	public void deserializeNBT(CompoundTag tag) {
		this.initShields(tag.getInt("tempshields"), tag.getInt("permshields"));
	}
}
