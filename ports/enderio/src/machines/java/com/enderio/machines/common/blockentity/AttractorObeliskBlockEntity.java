package com.enderio.machines.common.blockentity;

import com.enderio.api.UseOnly;
import com.enderio.api.capacitor.CapacitorModifier;
import com.enderio.api.capacitor.LinearScalable;
import com.enderio.api.capacitor.QuadraticScalable;
import com.enderio.api.filter.EntityFilter;
import com.enderio.api.io.energy.EnergyIOMode;
import com.enderio.base.common.init.EIOCapabilities;
import com.enderio.machines.common.blockentity.base.ObeliskBlockEntity;
import com.enderio.machines.common.config.MachinesConfig;
import com.enderio.machines.common.io.item.MachineInventoryLayout;
import com.enderio.machines.common.menu.AttractorObeliskMenu;
import com.enderio.machines.common.obelisk.IObeliskManagerCapability;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.fml.LogicalSide;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

/**
 * SkyTekx: the Attractor Obelisk of 8.x (461cf0511, 65fe4d639, ef97d54da, d061a0bb6) on the 6.x obelisk base.
 * Each server tick it pulls the filtered mobs in its area toward itself: pathfinding mobs walk to it, slimes and
 * phantoms target a fake player at the obelisk, ghasts (and the wither, if bosses are allowed) are pushed directly.
 */
public class AttractorObeliskBlockEntity extends ObeliskBlockEntity<AttractorObeliskBlockEntity> {

    private static final QuadraticScalable ENERGY_CAPACITY = new QuadraticScalable(CapacitorModifier.ENERGY_CAPACITY, MachinesConfig.COMMON.ENERGY.ATTRACTOR_CAPACITY);
    private static final LinearScalable ENERGY_USAGE = new LinearScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.ENERGY.ATTRACTOR_USAGE);
    private static final LinearScalable RANGE = new LinearScalable(CapacitorModifier.ENERGY_USE, MachinesConfig.COMMON.ATTRACTOR_RANGE);

    private final Vec3 targetPos;

    @UseOnly(LogicalSide.SERVER)
    @Nullable
    private FakePlayer fakePlayer;

    private boolean chunkUnloaded;

    public AttractorObeliskBlockEntity(BlockEntityType<?> type, BlockPos worldPosition, BlockState blockState) {
        super(EnergyIOMode.Input, ENERGY_CAPACITY, ENERGY_USAGE, type, worldPosition, blockState);
        // 0 means not set, the first tick with a capacitor grows it to the max range (8.x DEFAULT_RANGE).
        range = 0;
        targetPos = Vec3.atCenterOf(worldPosition);
    }

    /**
     * The fake player that slimes and phantoms target. It is created on first use from the server tick, never from
     * setLevel or onLoad: creating it during a chunk load froze world loading upstream (d061a0bb6), and a block entity
     * that changes itself from setLevel crashed next to Flux Networks 7.2.1.15 (ef97d54da). 6.x machines have no
     * owner, so the profile comes from the position, which also makes FakePlayerFactory hand the same player back after
     * a chunk reload.
     */
    @UseOnly(LogicalSide.SERVER)
    private FakePlayer getFakePlayer(ServerLevel serverLevel) {
        if (fakePlayer == null) {
            UUID id = UUID.nameUUIDFromBytes(("enderio:attractor:" + worldPosition).getBytes(StandardCharsets.UTF_8));
            fakePlayer = FakePlayerFactory.get(serverLevel, new GameProfile(id, "[EnderIO Attractor]"));
            fakePlayer.setPos(targetPos.x, targetPos.y, targetPos.z);
        }

        return fakePlayer;
    }

    // The attractor needs no manager, it looks for mobs itself.
    @Override
    protected @Nullable IObeliskManagerCapability<AttractorObeliskBlockEntity> getObeliskManager(ServerLevel level) {
        return null;
    }

    @Override
    public MachineInventoryLayout createInventoryLayout() {
        return MachineInventoryLayout.builder()
            .inputSlot((integer, itemStack) -> itemStack.getCapability(EIOCapabilities.FILTER).orElse(null) instanceof EntityFilter)
            .slotAccess(FILTER)
            .capacitor()
            .build();
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int pContainerId, Inventory pPlayerInventory, Player pPlayer) {
        return new AttractorObeliskMenu(this, pPlayerInventory, pContainerId);
    }

    @Override
    public boolean requiresFilter() {
        return true;
    }

    @Override
    public int getMaxRange() {
        return getCapacitorScaledMaxRange(RANGE);
    }

    @Override
    protected boolean rangeFollowsMaxRange() {
        return true;
    }

    @Override
    public int getPerTickEnergyCost() {
        return getRangeScaledEnergyCost();
    }

    @Override
    public String getColor() {
        return MachinesConfig.CLIENT.BLOCKS.ATTRACTOR_RANGE_COLOR.get();
    }

    @Override
    public void serverTick() {
        super.serverTick();

        if (isActive()) {
            doAttract();
        }
    }

    private void doAttract() {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        AABB aabb = getAABB();
        if (aabb == null) {
            return;
        }

        EntityFilter filter = getEntityFilter();
        if (filter == null) {
            return;
        }

        List<Mob> filteredEntities = serverLevel.getEntities(EntityTypeTest.forClass(Mob.class), aabb, filter::test);
        for (Mob mob : filteredEntities) {
            if (!MachinesConfig.COMMON.ATTRACTOR_PULL_BOSSES.get() && mob.getType().is(Tags.EntityTypes.BOSSES)) {
                // ignore
            } else if (mob instanceof WitherBoss) {
                mob.goalSelector.disableControlFlag(Goal.Flag.TARGET);
                mob.goalSelector.disableControlFlag(Goal.Flag.LOOK);
                mob.setTarget(null);
                directPull(mob, 2.25f);
            } else if (mob instanceof PathfinderMob) {
                attractMob(mob);
            } else if (useTarget(mob)) {
                mob.setTarget(getFakePlayer(serverLevel));
            } else if (mob instanceof Ghast) {
                directPull(mob, 1);
            }
        }
    }

    private boolean useTarget(Mob mob) {
        return mob instanceof Slime || mob instanceof Phantom;
    }

    private void attractMob(Mob mob) {
        mob.goalSelector.enableControlFlag(Goal.Flag.MOVE);
        Vec3 moveOffset = targetPos.subtract(mob.getX(), mob.getY(), mob.getZ());
        // keep them 1 block away
        moveOffset = moveOffset.subtract(moveOffset.normalize());
        mob.getNavigation().moveTo(mob.getX() + moveOffset.x, mob.getY() + moveOffset.y, mob.getZ() + moveOffset.z, 1);
    }

    private void directPull(LivingEntity mob, float speed) {
        Vec3 dir = targetPos.subtract(new Vec3(mob.xo, mob.yo, mob.zo)).normalize();
        dir = dir.scale(speed);
        AABB aabb = mob.getBoundingBox();
        aabb = aabb.move(dir);
        if (level != null && level.noCollision(mob, aabb)) {
            mob.setDeltaMovement(dir);
        }
    }

    @Override
    public void onChunkUnloaded() {
        chunkUnloaded = true;
        super.onChunkUnloaded();
    }

    /**
     * FakePlayerFactory owns the fake player, so it is not discarded here (upstream discards its own instance): a reload
     * of the chunk gets the same player back. When the block itself goes away, the mobs in range that still target the
     * player let go of it instead of chasing the spot of a removed obelisk.
     */
    @Override
    public void setRemoved() {
        if (fakePlayer != null && !chunkUnloaded && level instanceof ServerLevel serverLevel) {
            AABB aabb = getAABB();
            if (aabb != null) {
                FakePlayer target = fakePlayer;
                for (Mob mob : serverLevel.getEntities(EntityTypeTest.forClass(Mob.class), aabb, mob -> mob.getTarget() == target)) {
                    mob.setTarget(null);
                }
            }
        }

        fakePlayer = null;
        super.setRemoved();
    }
}
