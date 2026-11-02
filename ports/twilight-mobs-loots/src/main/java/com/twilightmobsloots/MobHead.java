package com.twilightmobsloots;

import com.mojang.serialization.Codec;
import net.minecraft.util.StringRepresentable;

/**
 * The Twilight Forest mob a trophy belongs to (48 regular mobs; Twilight Forest already ships trophies
 * for its bosses, so those are not duplicated).
 *
 * <p>Blocks, the block entity and the item share this enum; the client maps it to a Twilight Forest
 * model layer and entity texture. The serialized name is the Twilight Forest entity id, which is also the
 * path of its loot table {@code twilightforest:entities/<name>}.
 */
public enum MobHead implements StringRepresentable {
    // Dark forest / common hostiles
    REDCAP("redcap"),
    REDCAP_SAPPER("redcap_sapper"),
    KOBOLD("kobold"),
    BLOCKCHAIN_GOBLIN("blockchain_goblin"),
    LOWER_GOBLIN_KNIGHT("lower_goblin_knight"),
    UPPER_GOBLIN_KNIGHT("upper_goblin_knight"),
    LOYAL_ZOMBIE("loyal_zombie"),
    RISING_ZOMBIE("rising_zombie"),
    LICH_MINION("lich_minion"),
    // Labyrinth / underground
    MINOTAUR("minotaur"),
    MAZE_SLIME("maze_slime"),
    PINCH_BEETLE("pinch_beetle"),
    SLIME_BEETLE("slime_beetle"),
    HELMET_CRAB("helmet_crab"),
    HARBINGER_CUBE("harbinger_cube"),
    ROVING_CUBE("roving_cube"),
    // Towers / castle
    ADHERENT("adherent"),
    DEATH_TOME("death_tome"),
    CARMINITE_BROODLING("carminite_broodling"),
    CARMINITE_GHASTGUARD("carminite_ghastguard"),
    CARMINITE_GHASTLING("carminite_ghastling"),
    CARMINITE_GOLEM("carminite_golem"),
    TOWERWOOD_BORER("towerwood_borer"),
    SKELETON_DRUID("skeleton_druid"),
    TROLL("troll"),
    // Spiders / beetles / insects
    HEDGE_SPIDER("hedge_spider"),
    KING_SPIDER("king_spider"),
    SWARM_SPIDER("swarm_spider"),
    FIRE_BEETLE("fire_beetle"),
    MOSQUITO_SWARM("mosquito_swarm"),
    TINY_BIRD("tiny_bird"),
    // Wolves / snow
    WINTER_WOLF("winter_wolf"),
    HOSTILE_WOLF("hostile_wolf"),
    MIST_WOLF("mist_wolf"),
    STABLE_ICE_CORE("stable_ice_core"),
    UNSTABLE_ICE_CORE("unstable_ice_core"),
    ICE_CRYSTAL("ice_crystal"),
    // Ghosts / yeti
    WRAITH("wraith"),
    YETI("yeti"),
    // Passive animals
    RAVEN("raven"),
    BIGHORN_SHEEP("bighorn_sheep"),
    BOAR("boar"),
    DEER("deer"),
    DWARF_RABBIT("dwarf_rabbit"),
    SQUIRREL("squirrel"),
    PENGUIN("penguin"),
    // Giants (the trophy renders a player head with the viewer's skin)
    ARMORED_GIANT("armored_giant"),
    GIANT_MINER("giant_miner");

    /** Serialization codec (kept from upstream; block codecs do not exist in 1.20.1). */
    public static final Codec<MobHead> CODEC = StringRepresentable.fromEnum(MobHead::values);

    private final String serializedName;

    MobHead(String serializedName) {
        this.serializedName = serializedName;
    }

    @Override
    public String getSerializedName() {
        return this.serializedName;
    }
}
