package com.twilightmobsloots.block;

import com.twilightmobsloots.MobHead;
import com.twilightmobsloots.block.entity.MobTrophyBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.BlockHitResult;
import twilightforest.init.TFSounds;

import java.util.EnumMap;
import java.util.function.Supplier;

/**
 * Base class of the mob trophy blocks, modelled on Twilight Forest's {@code AbstractTrophyBlock}:
 * <ul>
 *     <li>a {@link BaseEntityBlock} whose head is drawn by the block entity renderer;</li>
 *     <li>{@link Equipable} with the head slot;</li>
 *     <li>using a placed trophy plays the mob's sound and spawns its particles
 *         ({@link #use} -> {@link #playSound} + {@link #createParticle});</li>
 *     <li>the block has no visible geometry (the blockstate points at {@code minecraft:block/skull}, used
 *         only for break particles); everything is drawn by {@code MobTrophyRenderer}.</li>
 * </ul>
 */
public abstract class AbstractMobTrophyBlock extends BaseEntityBlock implements Equipable {

    private final MobHead head;

    protected AbstractMobTrophyBlock(MobHead head, BlockBehaviour.Properties properties) {
        super(properties);
        this.head = head;
    }

    /** Which mob this trophy belongs to. */
    public MobHead getMobHead() {
        return this.head;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    /**
     * Using a placed trophy plays the mob's sound and particles (the facing comes from the placement, there
     * is no in-place rotation). 1.20.1 {@code use} replaces 1.21 {@code useWithoutItem}; both run for any
     * held item unless the player sneaks.
     */
    @Override
    @SuppressWarnings("deprecation")
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand,
                                 BlockHitResult hitResult) {
        playSound(level, pos);
        createParticle(level, pos);
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    /** Plays the mob's ambient sound (server side; the client hears the broadcast). */
    public void playSound(Level level, BlockPos pos) {
        if (level.isClientSide()) {
            return;
        }
        Supplier<SoundEvent> sound = SOUNDS.get(this.head);
        if (sound != null) {
            level.playSound(null, pos, sound.get(), SoundSource.BLOCKS,
                    1.0F, 0.9F + level.getRandom().nextFloat() * 0.1F);
        }
    }

    /** Spawns ten of the mob's particles above the trophy (server side, sent to nearby players). */
    public void createParticle(Level level, BlockPos pos) {
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        ParticleOptions particle = PARTICLES.get(this.head);
        RandomSource random = level.getRandom();
        for (int i = 0; i < 10; i++) {
            double x = pos.getX() + random.nextDouble();
            double y = pos.getY() + 0.25 + random.nextFloat() * 0.5;
            double z = pos.getZ() + random.nextDouble();
            serverLevel.sendParticles(particle, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ===== mob -> sound / particle (data driven: a new MobHead constant only needs an entry here) =====

    /**
     * Mob -> sound played on use (no entry = silent). Kept as registry suppliers and resolved on use, so
     * this class can initialise during block registration regardless of registry order.
     */
    private static final EnumMap<MobHead, Supplier<SoundEvent>> SOUNDS = new EnumMap<>(MobHead.class);
    /** Mob -> particle spawned on use. */
    private static final EnumMap<MobHead, ParticleOptions> PARTICLES = new EnumMap<>(MobHead.class);

    static {
        // Sounds
        SOUNDS.put(MobHead.REDCAP, TFSounds.REDCAP_AMBIENT);
        SOUNDS.put(MobHead.REDCAP_SAPPER, TFSounds.REDCAP_AMBIENT);
        SOUNDS.put(MobHead.KOBOLD, TFSounds.KOBOLD_AMBIENT);
        SOUNDS.put(MobHead.BLOCKCHAIN_GOBLIN, TFSounds.BLOCKCHAIN_GOBLIN_AMBIENT);
        SOUNDS.put(MobHead.LOWER_GOBLIN_KNIGHT, TFSounds.GOBLIN_KNIGHT_AMBIENT);
        SOUNDS.put(MobHead.UPPER_GOBLIN_KNIGHT, TFSounds.GOBLIN_KNIGHT_AMBIENT);
        SOUNDS.put(MobHead.LOYAL_ZOMBIE, TFSounds.LOYAL_ZOMBIE_AMBIENT);
        // RISING_ZOMBIE: no sound
        SOUNDS.put(MobHead.LICH_MINION, TFSounds.MINION_AMBIENT);
        SOUNDS.put(MobHead.MINOTAUR, TFSounds.MINOTAUR_AMBIENT);
        SOUNDS.put(MobHead.MAZE_SLIME, TFSounds.MAZE_SLIME_SQUISH);
        SOUNDS.put(MobHead.PINCH_BEETLE, TFSounds.PINCH_BEETLE_AMBIENT);
        SOUNDS.put(MobHead.SLIME_BEETLE, TFSounds.SLIME_BEETLE_AMBIENT);
        SOUNDS.put(MobHead.HELMET_CRAB, TFSounds.HELMET_CRAB_AMBIENT);
        // HARBINGER_CUBE / ROVING_CUBE / ADHERENT: no sound
        SOUNDS.put(MobHead.DEATH_TOME, TFSounds.DEATH_TOME_AMBIENT);
        SOUNDS.put(MobHead.CARMINITE_BROODLING, TFSounds.CARMINITE_BROODLING_AMBIENT);
        SOUNDS.put(MobHead.CARMINITE_GHASTGUARD, TFSounds.CARMINITE_GHASTGUARD_AMBIENT);
        SOUNDS.put(MobHead.CARMINITE_GHASTLING, TFSounds.CARMINITE_GHASTLING_AMBIENT);
        SOUNDS.put(MobHead.CARMINITE_GOLEM, TFSounds.CARMINITE_GOLEM_STEP);
        SOUNDS.put(MobHead.TOWERWOOD_BORER, TFSounds.TOWERWOOD_BORER_AMBIENT);
        SOUNDS.put(MobHead.SKELETON_DRUID, TFSounds.SKELETON_DRUID_AMBIENT);
        SOUNDS.put(MobHead.TROLL, TFSounds.TROLL_AMBIENT);
        SOUNDS.put(MobHead.HEDGE_SPIDER, TFSounds.HEDGE_SPIDER_AMBIENT);
        SOUNDS.put(MobHead.KING_SPIDER, TFSounds.KING_SPIDER_AMBIENT);
        SOUNDS.put(MobHead.SWARM_SPIDER, TFSounds.SWARM_SPIDER_AMBIENT);
        SOUNDS.put(MobHead.FIRE_BEETLE, TFSounds.FIRE_BEETLE_AMBIENT);
        SOUNDS.put(MobHead.MOSQUITO_SWARM, TFSounds.MOSQUITO);
        SOUNDS.put(MobHead.TINY_BIRD, TFSounds.TINY_BIRD_CHIRP);
        SOUNDS.put(MobHead.WINTER_WOLF, TFSounds.WINTER_WOLF_AMBIENT);
        SOUNDS.put(MobHead.HOSTILE_WOLF, TFSounds.HOSTILE_WOLF_AMBIENT);
        SOUNDS.put(MobHead.MIST_WOLF, TFSounds.MIST_WOLF_AMBIENT);
        SOUNDS.put(MobHead.STABLE_ICE_CORE, TFSounds.ICE_CORE_AMBIENT);
        SOUNDS.put(MobHead.UNSTABLE_ICE_CORE, TFSounds.ICE_CORE_AMBIENT);
        SOUNDS.put(MobHead.ICE_CRYSTAL, TFSounds.ICE_CORE_AMBIENT);
        SOUNDS.put(MobHead.WRAITH, TFSounds.WRAITH_AMBIENT);
        SOUNDS.put(MobHead.YETI, TFSounds.YETI_GROWL);
        SOUNDS.put(MobHead.RAVEN, TFSounds.RAVEN_CAW);
        SOUNDS.put(MobHead.BIGHORN_SHEEP, TFSounds.BIGHORN_SHEEP_AMBIENT);
        SOUNDS.put(MobHead.BOAR, TFSounds.BOAR_AMBIENT);
        SOUNDS.put(MobHead.DEER, TFSounds.DEER_AMBIENT);
        SOUNDS.put(MobHead.DWARF_RABBIT, TFSounds.DWARF_RABBIT_AMBIENT);
        SOUNDS.put(MobHead.SQUIRREL, TFSounds.SQUIRREL_AMBIENT);
        SOUNDS.put(MobHead.PENGUIN, TFSounds.PENGUIN_AMBIENT);

        // Particles
        PARTICLES.put(MobHead.REDCAP, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.REDCAP_SAPPER, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.KOBOLD, ParticleTypes.ANGRY_VILLAGER);
        PARTICLES.put(MobHead.BLOCKCHAIN_GOBLIN, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.LOWER_GOBLIN_KNIGHT, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.UPPER_GOBLIN_KNIGHT, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.LOYAL_ZOMBIE, ParticleTypes.POOF);
        PARTICLES.put(MobHead.RISING_ZOMBIE, ParticleTypes.POOF);
        PARTICLES.put(MobHead.LICH_MINION, ParticleTypes.PORTAL);
        PARTICLES.put(MobHead.MINOTAUR, ParticleTypes.CRIT);
        PARTICLES.put(MobHead.MAZE_SLIME, ParticleTypes.ITEM_SLIME);
        PARTICLES.put(MobHead.PINCH_BEETLE, ParticleTypes.POOF);
        PARTICLES.put(MobHead.SLIME_BEETLE, ParticleTypes.ITEM_SLIME);
        PARTICLES.put(MobHead.HELMET_CRAB, ParticleTypes.POOF);
        PARTICLES.put(MobHead.HARBINGER_CUBE, ParticleTypes.PORTAL);
        PARTICLES.put(MobHead.ROVING_CUBE, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.ADHERENT, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.DEATH_TOME, ParticleTypes.ENCHANT);
        PARTICLES.put(MobHead.CARMINITE_BROODLING, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.CARMINITE_GHASTGUARD, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.CARMINITE_GHASTLING, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.CARMINITE_GOLEM, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.TOWERWOOD_BORER, ParticleTypes.POOF);
        PARTICLES.put(MobHead.SKELETON_DRUID, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.TROLL, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.HEDGE_SPIDER, ParticleTypes.POOF);
        PARTICLES.put(MobHead.KING_SPIDER, ParticleTypes.POOF);
        PARTICLES.put(MobHead.SWARM_SPIDER, ParticleTypes.POOF);
        PARTICLES.put(MobHead.FIRE_BEETLE, ParticleTypes.FLAME);
        PARTICLES.put(MobHead.MOSQUITO_SWARM, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.TINY_BIRD, ParticleTypes.ASH);
        PARTICLES.put(MobHead.WINTER_WOLF, ParticleTypes.SNOWFLAKE);
        PARTICLES.put(MobHead.HOSTILE_WOLF, ParticleTypes.POOF);
        PARTICLES.put(MobHead.MIST_WOLF, ParticleTypes.SNOWFLAKE);
        PARTICLES.put(MobHead.STABLE_ICE_CORE, ParticleTypes.SNOWFLAKE);
        PARTICLES.put(MobHead.UNSTABLE_ICE_CORE, ParticleTypes.SNOWFLAKE);
        PARTICLES.put(MobHead.ICE_CRYSTAL, ParticleTypes.SNOWFLAKE);
        PARTICLES.put(MobHead.WRAITH, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.YETI, ParticleTypes.SNOWFLAKE);
        PARTICLES.put(MobHead.RAVEN, ParticleTypes.ASH);
        PARTICLES.put(MobHead.BIGHORN_SHEEP, ParticleTypes.HEART);
        PARTICLES.put(MobHead.BOAR, ParticleTypes.HEART);
        PARTICLES.put(MobHead.DEER, ParticleTypes.HEART);
        PARTICLES.put(MobHead.DWARF_RABBIT, ParticleTypes.HEART);
        PARTICLES.put(MobHead.SQUIRREL, ParticleTypes.HEART);
        PARTICLES.put(MobHead.PENGUIN, ParticleTypes.HEART);
        // Giants: no sound of their own, particles only
        PARTICLES.put(MobHead.ARMORED_GIANT, ParticleTypes.SMOKE);
        PARTICLES.put(MobHead.GIANT_MINER, ParticleTypes.SMOKE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MobTrophyBlockEntity(pos, state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public boolean isPathfindable(BlockState state, BlockGetter level, BlockPos pos, PathComputationType type) {
        return false;
    }
}
