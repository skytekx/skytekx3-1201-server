package twilightforest.capabilities;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import twilightforest.capabilities.fan.FeatherFanCapabilityHandler;
import twilightforest.capabilities.fan.FeatherFanFallCapability;
import twilightforest.capabilities.giant_pick.GiantPickMineCapability;
import twilightforest.capabilities.giant_pick.GiantPickMineCapabilityHandler;
import twilightforest.capabilities.multiplayer.MultiplayerInclusivityCapability;
import twilightforest.capabilities.portal.TFPortalCapability;
import twilightforest.capabilities.shield.IShieldCapability;
import twilightforest.capabilities.shield.ShieldCapabilityHandler;
import twilightforest.capabilities.teleporter_cache.TeleporterCacheCapability;
import twilightforest.capabilities.teleporter_cache.TeleporterCacheCapabilityHandler;
import twilightforest.capabilities.thrown.YetiThrowCapability;
import twilightforest.capabilities.thrown.YetiThrowCapabilityHandler;
import twilightforest.components.entity.TravellersWingsAttachment;
import twilightforest.data.tags.EntityTagGenerator;

import javax.annotation.Nonnull;

public class CapabilityList {

	public static final Capability<TeleporterCacheCapability> TELEPORTER_CACHE = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<IShieldCapability> SHIELDS = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<FeatherFanFallCapability> FEATHER_FAN_FALLING = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<YetiThrowCapability> YETI_THROWN = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<GiantPickMineCapability> GIANT_PICK_MINE = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<MultiplayerInclusivityCapability> MULTIPLAYER_FIGHT = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<TFPortalCapability> TF_PORTAL = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<TravellersWingsAttachment> TRAVELLERS_WINGS = CapabilityManager.get(new CapabilityToken<>() {});

	public static void registerCapabilities(RegisterCapabilitiesEvent event) {
		event.register(TeleporterCacheCapability.class);
		event.register(IShieldCapability.class);
		event.register(FeatherFanFallCapability.class);
		event.register(YetiThrowCapability.class);
		event.register(GiantPickMineCapability.class);
		event.register(MultiplayerInclusivityCapability.class);
		event.register(TFPortalCapability.class);
		event.register(TravellersWingsAttachment.class);
	}

	public static void attachLevelCapability(AttachCapabilitiesEvent<Level> e) {
		if (e.getObject().dimension().equals(Level.OVERWORLD)) {
			e.addCapability(TeleporterCacheCapability.ID, new ICapabilitySerializable<CompoundTag>() {

				final LazyOptional<TeleporterCacheCapability> inst = LazyOptional.of(TeleporterCacheCapabilityHandler::new);

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
					return TELEPORTER_CACHE.orEmpty(capability, inst.cast());
				}

				@Override
				public CompoundTag serializeNBT() {
					return inst.orElseThrow(NullPointerException::new).serializeNBT();
				}

				@Override
				public void deserializeNBT(CompoundTag nbt) {
					inst.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
				}
			});
		}
	}

	public static void attachEntityCapability(AttachCapabilitiesEvent<Entity> e) {
		if (e.getObject() instanceof LivingEntity living) {
			e.addCapability(IShieldCapability.ID, new ICapabilitySerializable<CompoundTag>() {

				final LazyOptional<IShieldCapability> inst = LazyOptional.of(() -> {
					ShieldCapabilityHandler i = new ShieldCapabilityHandler();
					i.setEntity(living);
					return i;
				});

				@Nonnull
				@Override
				public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, Direction facing) {
					return SHIELDS.orEmpty(capability, inst.cast());
				}

				@Override
				public CompoundTag serializeNBT() {
					return inst.orElseThrow(NullPointerException::new).serializeNBT();
				}

				@Override
				public void deserializeNBT(CompoundTag nbt) {
					inst.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
				}
			});

			if (e.getObject() instanceof Player player) {
				e.addCapability(FeatherFanFallCapability.ID, new ICapabilitySerializable<CompoundTag>() {

					private final LazyOptional<FeatherFanFallCapability> inst = LazyOptional.of(() -> {
						FeatherFanCapabilityHandler cap = new FeatherFanCapabilityHandler();
						cap.setEntity(player);
						return cap;
					});

					@Override
					public CompoundTag serializeNBT() {
						return inst.orElseThrow(NullPointerException::new).serializeNBT();
					}

					@Override
					public void deserializeNBT(CompoundTag nbt) {
						inst.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
					}

					@Override
					public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
						return FEATHER_FAN_FALLING.orEmpty(cap, inst.cast());
					}
				});

				// not serializable: the portal warm-up starts over after a relog, like 4.8's unsaved attachment
				e.addCapability(TFPortalCapability.ID, new ICapabilityProvider() {

					private final LazyOptional<TFPortalCapability> inst = LazyOptional.of(TFPortalCapability::new);

					@Override
					public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
						return TF_PORTAL.orEmpty(cap, inst.cast());
					}
				});
			}

			e.addCapability(YetiThrowCapability.ID, new ICapabilitySerializable<CompoundTag>() {

				private final LazyOptional<YetiThrowCapability> inst = LazyOptional.of(() -> {
					YetiThrowCapabilityHandler cap = new YetiThrowCapabilityHandler();
					cap.setEntity(living);
					return cap;
				});

				@Override
				public CompoundTag serializeNBT() {
					return inst.orElseThrow(NullPointerException::new).serializeNBT();
				}

				@Override
				public void deserializeNBT(CompoundTag nbt) {
					inst.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
				}

				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return YETI_THROWN.orEmpty(cap, inst.cast());
				}
			});

			e.addCapability(GiantPickMineCapability.ID, new ICapabilitySerializable<CompoundTag>() {

				private final LazyOptional<GiantPickMineCapability> inst = LazyOptional.of(GiantPickMineCapabilityHandler::new);

				@Override
				public CompoundTag serializeNBT() {
					return inst.orElseThrow(NullPointerException::new).serializeNBT();
				}

				@Override
				public void deserializeNBT(CompoundTag nbt) {
					inst.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
				}

				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return GIANT_PICK_MINE.orEmpty(cap, inst.cast());
				}
			});

			// not serializable: like 4.8's unsaved travellers_wings attachment, the wing animation state starts over after a reload
			e.addCapability(TravellersWingsAttachment.ID, new ICapabilityProvider() {

				private final LazyOptional<TravellersWingsAttachment> inst = LazyOptional.of(TravellersWingsAttachment::new);

				@Override
				public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
					return TRAVELLERS_WINGS.orEmpty(cap, inst.cast());
				}
			});

			// Only the entity types tagged when the entity is created get it, so a /reload that changes the tag only affects new entities
			if (living.getType().is(EntityTagGenerator.MULTIPLAYER_INCLUSIVE_ENTITIES)) {
				e.addCapability(MultiplayerInclusivityCapability.ID, new ICapabilitySerializable<CompoundTag>() {

					private final LazyOptional<MultiplayerInclusivityCapability> inst = LazyOptional.of(MultiplayerInclusivityCapability::new);

					@Override
					public CompoundTag serializeNBT() {
						return inst.orElseThrow(NullPointerException::new).serializeNBT();
					}

					@Override
					public void deserializeNBT(CompoundTag nbt) {
						inst.orElseThrow(NullPointerException::new).deserializeNBT(nbt);
					}

					@Override
					public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
						return MULTIPLAYER_FIGHT.orEmpty(cap, inst.cast());
					}
				});
			}
		}
	}
}
