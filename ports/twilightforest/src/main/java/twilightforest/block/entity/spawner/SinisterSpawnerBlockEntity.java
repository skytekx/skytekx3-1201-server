package twilightforest.block.entity.spawner;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;
import twilightforest.init.TFBlockEntities;

import java.util.List;
import java.util.Objects;

public class SinisterSpawnerBlockEntity extends BlockEntity {
	private final SinisterSpawnerLogic spawner = new SinisterSpawnerLogic() {
		@Override
		public BlockEntity getSpawnerBlockEntity() {
			return SinisterSpawnerBlockEntity.this;
		}
	};
	@Nullable private ResourceLocation lootTable = null;

	public SinisterSpawnerBlockEntity(BlockPos pos, BlockState blockState) {
		super(TFBlockEntities.SINISTER_SPAWNER.get(), pos, blockState);
	}

	@Override
	public void load(CompoundTag tag) {
		super.load(tag);
		this.spawner.load(this.level, this.worldPosition, tag);

		if (tag.contains("LootTable")) {
			this.lootTable = ResourceLocation.tryParse(tag.getString("LootTable"));
		} else {
			this.lootTable = null;
		}
	}

	@Override
	protected void saveAdditional(CompoundTag tag) {
		super.saveAdditional(tag);
		this.spawner.save(tag);

		if (this.lootTable != null) {
			tag.putString("LootTable", this.lootTable.toString());
		}
	}

	public static void clientTick(Level level, BlockPos pos, BlockState state, SinisterSpawnerBlockEntity blockEntity) {
		blockEntity.spawner.clientTick(level, pos);
	}

	public static void serverTick(Level level, BlockPos pos, BlockState state, SinisterSpawnerBlockEntity blockEntity) {
		blockEntity.spawner.serverTick((ServerLevel) level, pos);
	}

	@Override
	public ClientboundBlockEntityDataPacket getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	@Override
	public CompoundTag getUpdateTag() {
		CompoundTag compoundtag = this.saveWithoutMetadata();
		compoundtag.remove("SpawnPotentials");
		return compoundtag;
	}

	@Override
	public boolean triggerEvent(int id, int type) {
		return this.spawner.onEventTriggered(this.level, id) || super.triggerEvent(id, type);
	}

	@Override
	public boolean onlyOpCanSetNbt() {
		return true;
	}

	// 4.8 gets this from the Spawner interface, which 1.20.1 does not have; SinisterSpawnerBlock routes spawn eggs here
	public void setEntityId(EntityType<?> type, RandomSource random) {
		this.spawner.setEntityId(type, this.level, random, this.worldPosition);
		this.setChanged();
	}

	public SinisterSpawnerLogic getSpawner() {
		return this.spawner;
	}

	public boolean setParticles(List<ParticleOptions> particles, boolean sendUpdate) {
		return this.getSpawner().setParticles(particles, sendUpdate);
	}

	public boolean addParticle(ParticleOptions particle, boolean sendUpdate) {
		return this.getSpawner().addParticle(particle, sendUpdate);
	}

	public boolean removeParticle(ParticleOptions particle, boolean sendUpdate) {
		return this.getSpawner().removeParticle(particle, sendUpdate);
	}

	public void sendChanges() {
		if (this.level != null) {
			this.level.sendBlockUpdated(this.getBlockPos(), this.getBlockState(), this.getBlockState(), 3);
		}
	}

	@Nullable
	public ResourceLocation getLootTableId() {
		return this.lootTable;
	}

	@Nullable
	public LootTable getLootTable() {
		if (this.level == null || this.lootTable == null) return null;
		MinecraftServer server = this.level.getServer();
		if (server == null) return null;
		return server.getLootData().getLootTable(this.lootTable);
	}

	public boolean setLootTable(@Nullable ResourceLocation lootTable) {
		boolean updated = !Objects.equals(this.lootTable, lootTable);
		this.lootTable = lootTable;
		if (updated) this.setChanged();
		return updated;
	}

	// The displayed mob can be larger than the block, so do not cull it at the block's own bounds
	@Override
	public AABB getRenderBoundingBox() {
		BlockPos pos = this.getBlockPos();
		return new AABB(pos.getX() - 1.0, pos.getY() - 1.0, pos.getZ() - 1.0, pos.getX() + 2.0, pos.getY() + 2.0, pos.getZ() + 2.0);
	}
}
