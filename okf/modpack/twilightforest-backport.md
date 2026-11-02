---
type: Plan
title: Twilight Forest 4.8 backport
description: Which TF 4.8 (1.21.1) features are backported onto TF 4.3 for 1.20.1 Forge, in which order, and which TF addons fit the pack.
tags: [modpack, mods, twilightforest]
timestamp: 2026-11-02
---

# Twilight Forest 4.8 → 1.20.1 backport plan

Base: TF **4.3** (`1.20.1` branch, Forge 47) vendored in `ports/twilightforest/`. Source of new content: TF **4.8** (`1.21.1` branch, latest, 2026-10-13).
Only content that works sensibly on 1.20.1 is ported; the rest is listed under *Excluded*. Effort is human-hours for an experienced Forge modder (agents port in parallel).

**133 features, ≈1038 h**, in 11 waves.

## Savegame impact

Waves 1-6 add only new registry ids, new NBT keys or new optional SavedData, and change worldgen only in ungenerated chunks, so they need no TF dimension reset. These items touch existing saved data in a controlled way. Candelabra: a player-placed 4.3 candelabra gains has_candle_1..3 and a block entity and loads empty; it is WIP in 4.3 and never generated. Jars: firefly, cicada and moonworm jars gain a BE with the default twilight_oak lid. Hydra: NumHeads changes to a bitmask, so a living Hydra may reload once with a different head set. spiral_bricks gains waterlogged=false. Magic Paintings: Lucid Lands grows from 2x2 to 3x3 as in 4.8, so a Lucid Lands painting placed in 4.3 (creative only, the item was NYI) shifts half a block, and if the 3x3 area does not fit it pops off as an item that keeps its variant and has to be placed again. Naga Courtyard: keep TFNCTr, TFNCDu and TFNCSt registered as aliases, or old courtyards lose their terraces. Lich Tower: keep every old TFLT* piece type registered; 4.3's unused revamp stubs TFLTC2ST and TFLTSTRm become no-op aliases. Chiseled bookshelf: keep death_tome_spawner and empty_canopy_bookshelf registered and do not port 4.8's removal. Seed: removing seed.js must keep the identical seed value, or the landmark grid shifts. The Beardifier bridge makes terrain_adaptation take effect in the TF dimension for the first time, so structures that straddle old and new chunks can show a terrain seam. The worldgen waves 7 and 8 add berry bushes, oreberries, lakes, hollow-tree and fallen-trunk structures, Highlands Underground, carver changes, canopy and glacier blanketing, the Aurora Palace and Courtyard reworks, and the new Lich Tower. They only reach unexplored chunks, so on a pregenerated server a TF dimension reset (delete the twilightforest dimension folder, overworld untouched) followed by Chunky pregen is recommended once after wave 7 and again after wave 8. The overworld and the Skylands are unaffected. The excluded chunkgen overhaul is the only change that would have forced a full TF terrain reset and a level.dat generator rewrite.

## Shared infrastructure

- Codec/API shim layer for 4.3: MapCodec/StreamCodec become Codec + KeyDispatchDataCodec.of / FriendlyByteBuf; Codec.withAlternative becomes ExtraCodecs.withAlternative; ResourceKey<LootTable> becomes ResourceLocation; BootstrapContext becomes BootstapContext. Interaction and gameplay APIs: ItemInteractionResult/useItemOn/useWithoutItem become use(); Holder<MobEffect> becomes MobEffect; ItemAbilities become ToolActions; PathType becomes BlockPathTypes; ResourceLocation.fromNamespaceAndPath becomes new ResourceLocation. Plus a Java 21 to 17 sweep (List.getFirst/getLast, SequencedCollection, switch patterns).
- Beanification removal convention: @Component/@Autowired/@PostConstruct classes become static singletons or EventBusSubscribers, and reload listeners go through AddReloadListenerEvent. enumextensions.json becomes IExtensibleEnum.create where Forge 47 allows it (ItemDisplayContext, Rarity); otherwise drop it.
- Item data layer (TFItemData) replacing Data Components with stack NBT or BlockEntityTag. Covers CASKET_DAMAGE, JAR_LID and CONTAINER, CANDELABRA_DATA, ORE_SCANNING/ORE_DATA/ORE_FILTER/ORE_RANGE, EMPERORS_CLOTH, and flask contents (keep 4.3's 'Potion'/'Uses'/'Breakage'), later the Traveller's modifier payloads. One place keeps NBT key names stable.
- Entity and player data layer replacing Data Attachments with Forge capabilities or getPersistentData(). TFPlayerData holds the portal timer, flask tracking, banish flag and charm state; 4.3's shield capability stays. TFMobData holds multiplayer_fight participants, leash_pathfinder_override and the zombified-player profile. Includes clone-on-death and a client sync packet.
- Networking: register each new packet on 4.3's TFPacketHandler SimpleChannel with a protocol bump. Packets: TFBossBarPacket, UpdateDeathTimePacket, SpawnCharmPacket, SyncQuestsPacket, StructureProtectionPacket v2, LifedrainParticlePacket, WipeOreMeterPacket and SetMasonJarItemPacket, with the Traveller's packets later.
- Data-map replacement: one generic SimpleJsonResourceReloadListener, synced through OnDatapackSyncEvent where the client needs it, for 4.8's NeoForge DataMapTypes ominous_fire (entity conversions), magic_map_color and ore_map_color. Compostables go into ComposterBlock.COMPOSTABLES in enqueueWork.
- Datapack and static registry plumbing via DataPackRegistryEvent.NewRegistry and RegistryBuilder: template_marker_handler, template_marker_handler_list, structure_speleothem_settings, chunk_blanket_processors and travellers_modifiers (datapack); template_marker_handler_type and chunk_blanket_type (static).
- Resource migration rules for copied 4.8 data. Folder renames: structure/ to structures/, loot_table/ to loot_tables/, recipe/ to recipes/, advancement/ to advancements/, tags/block|item|entity_type to tags/blocks|items|entity_types. Content renames: c: tags to forge:, short_grass to grass, data-driven set_enchantments/set_components to 1.20.1 NBT loot functions. Keep 4.3 loot table ids (chests/*) for existing unopened chests.
- 4.8 NBT template import check (DataVersion 3955). 1.20.1's DFU leaves newer data untouched, and the scanned palettes (lich_tower, camp, courtyard, feature, final_castle) contain no post-1.20.1 vanilla blocks. Still check item stacks in block-entity NBT (the 1.20.5 'count' vs 'Count' key) and decorated pots (no inventory in 1.20.1), and re-save doubtful templates in a 1.20.1 dev world.
- TF Beardifier bridge inside ChunkGeneratorTwilight.doFill (feature custom-density-terraforming): vanilla beard, CustomDensitySource and a TF IPieceBeardifierModifier interface, with no coremod because 4.3 owns the TF fill loop. Pass StructureManager into doFill and tune the scale against the unnormalized warp noise. Deferred here from `knight-stronghold-fixes`: 4.3 still buries every Knight Stronghold piece through the structure-wide `terrain_adaptation: bury`, including the access chamber and the surface ruins. 4.8 gives the unprotected `KnightStrongholdComponent` pieces `TerrainAdjustment.NONE` through the per-piece PieceBeardifierModifier (e7ab1ae68, 64b89ff38), so the bridge's IPieceBeardifierModifier port must do the same for the 4.3 `StructureTFStrongholdComponent` pieces that have `spawnListIndex` -1.
- Registry remap policy: extend 4.3's TFRemapper (MissingMappingsEvent) for block and item renames. For registries without MissingMappings (structure piece types, processors), keep old ids registered as aliases to the new deserializer: TFNCTr, TFNCDu and TFNCSt map to CourtyardTerrace, and 4.3's stubs TFLTC2ST and TFLTSTRm become no-op pieces. Never port 4.8's removal of death_tome_spawner and empty_canopy_bookshelf.
- Coremod strategy: keep 4.3's JS coremods (META-INF/asm), extend conquered.js for StructureStart.loadStaticStart (knight_y), and add armor.js for Emperor's Cloth. Add a Mixin config to the fork only for the Traveller's hooks: water walk, unrestrained, FOV, food exhaustion, slime block, MouseHandler#turnPlayer, PhantomSpawner and snowy grass.
- Verification harness: a fixed-seed TF biome and landmark dump compared before and after worldgen changes, plus structure-debug-commands, run against a copy of the server seed before each release. Boot rule from AGENTS.md: reach Done with 0 FATAL, crash or watchdog entries.
- Pack-side glue in this repo: rebuild the local-mods/ TF jar from the fork (it is now named twilightforest-1.20.1-4.3.2508-skytekx.1-universal.jar). AlmostUnified is already prepared: `twilightforest` is last in modPriorities, so once TF datagen tags twilightforest:copper_nugget as forge:nuggets/copper it unifies to immersiveengineering:nugget_copper. The jerky need no unification entry. Ship any remaining compat tags through kubejs/server_scripts/tag_fixes.js.

## Wave 1: Crash, concurrency and multiplayer-correctness fixes (no new ids, no reset)

Every item here is a bug checked in the 4.3 tree: the EncasedFireJet STATE crash, the shared carver RandomSource, chunk-loading cloud rain checks, static charm and flask state shared across players, boss loot loss through subList(28), ISTER allocating per frame, and the aurora shader crash. All are cheap and need neither a world reset nor new ids. They stabilise the Arclight server before any content lands. Ship as 4.3.1.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `worldgen-crash-fixes` — Hard crash and hang fixes that affect 4.3 today | bugfix | easy | 5 | none |
| `worldgen-thread-safety` — Worldgen concurrency and determinism fixes | bugfix | medium | 10 | none (biome/layer output identical if RNG order is preserved; adds configured_fe |
| `worldgen-perf-fixes` — Worldgen and tick performance fixes (thorns, clouds, config caching) | bugfix | easy | 5 | none |
| `charm-system-rework` — Charm of Life/Keeping and casket: multiplayer-safe state, PlayerRevive/Cosmetic Armor compat, totem animation | bugfix | easy | 7 | none |
| `boss-bugfixes` — Boss loot-chest, AI and multipart sync fixes | bugfix | easy | 4 | none |
| `flask-tracking-per-player` — Per-player potion flask advancement tracking | bugfix | easy | 3 | none |
| `client-render-fixes` — Client performance and crash fixes (ISTER, armor models, aurora shader, PatchModel) | client | easy | 5 | none |

## Wave 2: Remaining 4.3-applicable bugfixes and data-only tweaks

Bugfixes and JSON-level changes against code that 4.3 shares unchanged. Several (compat-tags, loot-table-fixes, spawn-cost-tuning, the stalactite rebalance) can ship immediately through KubeJS or a datapack without rebuilding the jar. The teleporter fixes prevent bedrock destruction and void spawns, and the knight_y fix only needs an extension of 4.3's conquered.js. Worldgen effects reach new chunks only.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `tf-teleporter-safety` — Twilight portal teleporter safety and correctness | bugfix | medium | 12 | none (optional new SavedData twilightforest_teleporter_cache) |
| `structure-bugfixes-misc` — Landmark structure and feature-template bugfixes | bugfix | easy | 10 | none (new chunks only; a Hedge Maze or Labyrinth that straddles already-generated chunks can show a seam at the old/new chunk border) |
| `knight-stronghold-fixes` — Knight Stronghold fixes (persisted start Y #2138, entrances, ruins, bounding boxes) | bugfix | easy | 5 | none (adds optional knight_y to start NBT) |
| `tree-feature-bugfixes` — Tree and root generation fixes | bugfix | easy | 5 | none (new trees only) |
| `block-spike-fixes` — Stalactite and stalagmite (BlockSpikeFeature) fixes | bugfix | easy | 3 | none |
| `entity-gameplay-bugfixes` — Mob, projectile and damage-source fixes | bugfix | easy | 8 | none (obsolete SpawnMore NBT ignored) |
| `item-block-bugfixes` — Item, block and enchantment fixes for 4.3 | bugfix | easy | 8 | none |
| `client-visual-fixes` — Model, renderer and particle bugfixes | client | easy | 5 | none |
| `loot-table-fixes` — Loot table fixes applicable to 4.3 | bugfix | easy | 2 | none |
| `advancement-fixes` — Advancement fixes (arctic dyed, glass sword, arborist) | bugfix | easy | 2 | none |
| `compat-tags` — Create and Forbidden Arcanus compat tags, stripped log convention tags | compat | easy | 1.5 | none |
| `hollow-hill-stalactite-rebalance` — Hollow hill ore and stalactite rebalance (data) | data | easy | 1 | none (new hollow hills only) |
| `spawn-cost-tuning` — Biome mob spawn retuning and frost restriction nerf | data | easy | 1.5 | none (applies on reload) |
| `surface-rule-fixes` — Surface rule fixes (no podzol or snow underwater) | worldgen | easy | 1.5 | none (new chunks only) |
| `biome-visuals-and-dimension-type` — Biome fog and water colours, enchanted grass gradient, dimension_type tweaks | client | easy | 2.5 | none |
| `translation-refresh` — Crowdin translation refresh | data | easy | 2 | none |
| `asset-model-texture-fixes` — Resource-only model and texture fixes | bugfix | easy | 2 | none |
| `misc-data-tweaks` — Small tag, recipe and stonecutter additions | data | easy | 3 | none |
| `curios-compat-fixes` — Curios compat fixes | bugfix | easy | 1.5 | none |
| `config-option-additions` — New standalone config options | qol | easy | 3 | none |
| `tf-tp-command` — /tf tp operator command | qol | easy | 1.5 | none |
| `remove-seed-coremod` — Drop seed.js coremod in favour of WorldUtil.getOverworldSeed() | other | easy | 1.5 | none if the seed value is identical (otherwise the landmark grid shifts and the  |
| `small-block-property-fixes` — Small block-property and sound fixes that also apply to 4.3 | bugfix | easy | 2 | none |
| `crumble-horn-recipe-additions` — Additional Crumble Horn conversions (#2237) | data | easy | 1 | none |
| `tag-driven-hooks` — Tag-driven hooks for modpacks (fire jet fuel, pedestal, thorns immunity, repair ingredients, convention tags) | data | easy | 2.5 | none |

## Wave 3: Shared structure and worldgen infrastructure

Built once and reused by the Lich Tower, Camp, Fallen Trunk, Hollow Trees, Courtyard and Final Castle hooks. The debug commands come first because they are the verification tool for everything after. The Beardifier bridge is the key 4.3-specific decision: 4.3's own doFill replaces the 1.21 tf-asm and PieceBeardifierModifier stack, which keeps the chunkgen overhaul out of scope.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `structure-debug-commands` — Structure debug commands (display_pieces, count_loot, generator_radius, map_locator) | qol | easy | 5 | none |
| `custom-density-terraforming` — TF Beardifier bridge + CustomDensitySource + structure density functions | mechanic | medium | 18 | changes worldgen for new chunks: 4.3 structures that already declare terrain_ada |
| `twilight-jigsaw-framework` — TF jigsaw pieces and data-driven template pools | other | medium | 24 | new ids only |
| `template-marker-handlers` — Data-driven template marker handlers | data | easy | 8 | new ids only |
| `structure-processors-48` — New structure processors (infest, soft replace, spawner, update marking, vertical decay, multiswizzle) | worldgen | easy | 6 | new ids only |
| `avoid-landmark-grid-placement` — avoid_landmark_grid placement for minor structures | worldgen | easy | 3 | new ids only |
| `structure-terrain-sampling` — Multi-sample weighted terrain height for structure placement | worldgen | medium | 5 | none (newly placed starts only) |
| `avoid-landmark-modifier-v2` — no_structure placement modifier v2 (piece-accurate clearance) | worldgen | medium | 8 | none (decoration in new chunks) |
| `landmark-wrapped-structure-types` — 'progression' and 'landmark' wrapper structure types | worldgen | easy | 4 | new ids only |

Wrapper notes for pack datapacks (4.3-specific):

- On 4.3, `twilightforest:progression` only locks a structure when its structure set uses `twilightforest:forced_landmark` for an existing TFLandmark (replacing that landmark's set) and the structure is in the `#twilightforest:landmark` structure tag. 4.3 has no datapack-usable `landmark_grid` placement, and the progression checks look the start up at the landmark grid centre.
- The nested blocks (`hint_creature`, `decoration_clearance`, and `avoid_additional_structures` on `twilightforest:avoid_landmark_grid`) decode with 1.20.1's lenient `optionalFieldOf`. A parse error inside one silently reads as absent instead of failing the datapack. Write `hint_item` in the 1.20.1 ItemStack format (`id`/`Count`/`tag`), not 4.8's `count`/`components`.
- The 4.8 codec reshape is not ported. TF's own landmark and progression structures keep 4.3's flat fields, so 4.8 `worldgen/structure/*.json` files must be flattened by hand (see the wave 3 entry in `ports/twilightforest-backport-carryover.json`).

## Wave 4: Boss and mob upgrades (high value on a multiplayer server)

multiplayer-boss-scaling is event and tag based, so it has no BaseTFBoss dependency (checked), and it gives the best value per hour for a group server. BaseTFBoss and boss bars precede the Knight Phantom, Hydra and Lich work. The mob items are small and add only sound and loot ids.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `multiplayer-boss-scaling` — Multiplayer-inclusive boss fights (health and loot scaling, group credit) | mechanic | medium | 10 | new ids only; the health bonus applies to new spawns |
| `boss-base-class-refactor` — BaseTFBoss common base and client-synced death animations | mechanic | medium | 10 | none if the 4.3 boss NBT keys are kept |
| `custom-rgb-boss-bars` — Custom RGB boss bars with per-phase colour | client | medium | 7 | none |
| `knight-phantom-rework` — Knight Phantom death animation and shared loot | boss | medium | 10 | new loot table id; Knight Stronghold boss loot moves from the stronghold_boss ch |
| `hydra-death-and-named-heads` — Hydra death animation, name-taggable heads, head reload fix | boss | medium | 8 | NumHeads changes from a count to a bitmask, so a Hydra alive in a save may reloa |
| `ur-ghast-tweaks` — Ur-Ghast decoupled from Ghastguard, trap and fireball fixes | boss | easy | 5 | none |
| `lich-fight-fixes` — Lich fight bugfixes (clones persisted, teleport home, no self-hits) | boss | easy | 6 | none (new optional NBT keys) |
| `rising-zombie-overhaul` — Rising Zombie overhaul (hidden until looked at, then digs out) | mob | easy | 5 | none (same id; existing rising zombies switch to the new behaviour) |
| `death-tome-tweaks` — Death Tome lectern ambush tweaks | mob | easy | 1.5 | none |
| `new-mob-sounds` — Dedicated sounds for beetles, helmet crab, penguin, squirrel, troll and deer | client | easy | 5 | new ids only |
| `helmet-crab-rework` — Helmet Crab model rework and rare blue variant | mob | easy | 4 | none (new NBT key) |
| `mob-balance-tweaks` — Mob balance tweaks (troll, yeti, squirrel, tiny bird, hostile wolf) | mob | easy | 2 | none |
| `passive-tempt-tags` — Passive mob tempt items as item tags | data | easy | 1.5 | new ids only (7 item tags) |
| `shika-senbei` — Shika Senbei deer cracker | item | easy | 2 | new ids only |
| `hostile-mount-fixes` — Pinch Beetle rider desync fix and boat smashing | mob | easy | 2 | none |
| `ice-bomb-rework` — Ice Bomb projectile rework | mechanic | easy | 1.5 | none |
| `block-and-chain-fixes` — Block and Chain rework (rendered links, break tags, adventure mode, world border) | mechanic | medium | 5 | new ids only (block tags) |

## Wave 5: Mechanics and QoL that finish 4.3's WIP/NYI items

4.3 ships the Ore Meter and Magic Painting as NYI and the flasks as WIP. These features finish them and harden progression locks against block placement. All of them sit on existing 4.3 systems (shield capability, SimpleChannel, magic_paintings registry), so the risk is moderate and there is no worldgen impact.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `progression-protection-overhaul` — Progression lock overhaul (no block placing, per-piece protection) | mechanic | medium | 10 | none |
| `fortification-shield-rework` — Fortification shield HUD indicator, sounds and fixes | qol | easy | 5 | new ids only |
| `misc-event-tweaks` — Misc gameplay event tweaks | qol | easy | 3 | none |
| `tf-portal-delay-gamerules` — Twilight portal warm-up delay, gamerules and overlay | mechanic | medium | 8 | none (new gamerules) |
| `weather-renderer-rewrite` — Locked-biome weather renderer rewrite (1.7 parity) | client | medium | 6 | none |
| `potion-flask-rework` — Brittle and Greater Potion Flask finished | item | medium | 8 | none (flask NBT layout may change for WIP flasks) |
| `uncrafting-table-improvements` — Uncrafting Table recipe book, fixes and config | qol | medium | 10 | none |
| `magic-map-improvements` — Magic Map conquered markers, charting tag, data-driven colours, Quest Grove conquerable | item | medium | 10 | none (map data gains an optional list) |
| `questing-ram-datapack-quests` — Datapack-driven Questing Ram quest | data | medium | 6 | none |
| `jei-plugin-upgrades` — JEI plugin upgrades (uncrafting XP cost, layout, FakeEntityType) | compat | easy | 6 | none |
| `ore-meter` — Ore Meter implemented (scan, HUD table, filter) | item | medium | 12 | none (existing id) |
| `magic-painting-upgrades` — Magic Paintings finished: frames, opacity modifiers, 3 new paintings | client | medium | 12 | new ids only (existing painting variant ids kept) |

Already done in wave 2 (`config-option-additions`, 5d070d8), skip them here: portalCreationPermission in `progression-protection-overhaul`, and disableIngredientSwitching (with its SyncUncraftingTableConfigPacket field and the `2-skytekx.3` protocol bump) in `uncrafting-table-improvements`.

## Wave 6: Cheap new blocks and items (Lich Tower prerequisites included)

Mostly easy registrations that add new ids only. Most of the Lich Tower's block dependencies (terrorcotta, carpet, rope, finished fence, casket, candelabra with BE) land here, so the tower later becomes structure work only. The candelabra is the only existing block whose state changes; 4.3 never generates it.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `pocket-watch` — Rabbit's Pocket Watch | item | easy | 2 | new ids only |
| `mystic-crown` — Mystic Crown and Crown Splinters | item | easy | 3 | new ids only |
| `loyal-zombie-babies` — Baby Loyal Zombies | mob | easy | 2 | none |
| `emperors-cloth` — Emperor's Cloth (invisible armor) and no-template smithing | item | medium | 10 | new ids only |
| `wrought-iron-fence-finials` — Wrought Iron Fence finished (finials, posts) and Wrought Iron Bar | block | easy | 5 | new ids only (existing fence states compatible) |
| `canopy-window` — Canopy Window and Window Pane | block | easy | 1.5 | new ids only |
| `terrorcotta-blocks` — Terrorcotta Arcs, Curves and Lines | block | easy | 3 | new ids only |
| `connected-texture-loader` — Generic connected-texture model loader | client | easy | 4 | none |
| `coronation-carpet` — Coronation Carpet (connected carpet) | block | easy | 3 | new ids only |
| `rope` — Rope block and item | block | easy | 6 | new ids only |
| `tf-wood-trapped-chests` — Trapped chests for all 8 TF woods | block | easy | 6 | new ids only |
| `maze-slime-block` — Maze Slime Ball and Maze Slime Block | block | easy | 3 | new ids only |
| `keepsake-casket-skull-chest` — Keepsake Casket finished and Skull Chest fallback | block | medium | 12 | new ids only |
| `candelabra-overhaul` — Candelabra overhaul (any candles, dim/ominous lighting, comparator) | block | medium | 12 | changes existing block: candelabra gains has_candle_N and a BE, so player-placed |
| `reactor-debris-visual` — Carminite Reactor debris mimics nearby blocks (1.7 look) | block | medium | 5 | new block entity id on an existing transient block |
| `trollsteinn-light-states` — Trollsteinn light reaction and lit item model | block | easy | 2 | none |
| `uberous-soil-improvements` — Uberous Soil improvements | block | easy | 2 | none |
| `tree-core-particles` — Magic tree core and ore magnet particles | client | easy | 4 | new ids only |
| `miniature-structures` — Dark Tower and Labyrinth miniature structures | block | easy | 3 | new ids only |
| `block-qol-bundle` — Small block QoL changes | qol | easy | 6 | none (spiral_bricks gains waterlogged=false default) |
| `item-polish-bundle` — Item polish (animations, particles, sounds) | qol | easy | 6 | new ids only (loot table) |

## Wave 7: Worldgen content for newly generated chunks

Everything here changes only ungenerated chunks. Bundle it into one release and do a single TF dimension reset plus Chunky pregen, rather than resetting per feature. Berry bushes come before oreberries and camps. The hollow-tree structure replaces 4.3's HollowTreeFeature and feeds the fallen-trunk exclusion zone. Highlands Underground uses 4.3's existing biome_layers (checked).

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `small-lakes-and-frozen-lakes` — TF small lakes, frozen lakes, lakes in more biomes | worldgen | easy | 4 | new ids only; new chunks only |
| `enchanted-forest-overhaul` — Enchanted Forest vegetation overhaul | worldgen | easy | 5 | new ids only; new chunks only |
| `dark-forest-ground-and-bushes` — Dark Forest ground cover and oak/dark-oak bushes | worldgen | easy | 3 | new ids only (dark_mushrooms dropped); new chunks only |
| `vegetation-placement-misc` — Misc biome vegetation and placement tuning | data | easy | 5 | none (new chunks only) |
| `canopy-tree-shape-and-density` — Canopy forest density fix and 1.7.10-style canopy trees | worldgen | easy | 4 | worldgen change, new chunks only (denser forests) |
| `mega-trees` — Mega Canopy trees and Mega Oak rename | worldgen | easy | 8 | new ids only (configured feature ids not persisted); new chunks only |
| `cave-carver-rework` — TF cave carver rework (tf_caves) | worldgen | easy | 5 | new chunks only (seams in caves at old and new chunk borders) |
| `highlands-underground-troll-caves` — Highlands Underground biome, troll-cave tunnels and troll-cave vegetation | worldgen | medium | 12 | new biome id; underground Highlands differ in new chunks (a TF dimension reset i |
| `ruins-and-feature-templates` — Ruined Foundations revamp, Hollow Stumps, Druid Hut loot and template tweaks | worldgen | medium | 9 | new loot table ids; new chunks only |
| `berry-bushes` — Berry bushes (4 surface, 4 Dark Tower), berries and worldgen | block | medium | 18 | new ids and placed features; new chunks and newly generated Dark Towers only |
| `oreberries` — Oreberry bushes (iron, gold, copper, essence) and copper nugget | block | medium | 8 | new ids only; new chunks only |
| `speleothem-config-datadriven` — Datapack-driven structure speleothem settings | data | medium | 10 | none for saved data; the datapack path and format change |
| `chunk-blanketing` — Data-driven Dark Forest canopy and glacier blanketing | worldgen | medium | 8 | canopy and glacier shape change in new chunks; new registries only |
| `hollow-tree-structure` — Giant Hollow Oaks as datapack-configurable structures (1.7.10 port), swamp variant, sapling growth | structure | medium | 16 | new ids only; already-generated feature trees stay |
| `fallen-trunk-structure` — Fallen Trunks (hollow fallen logs with spawner and loot) | structure | medium | 18 | new ids only; new chunks only |
| `aurora-palace-rework` — Aurora Palace floor-plan rework and guaranteed Snow Queen | structure | easy | 8 | none for piece ids; newly generated palaces only |
| `naga-courtyard-terraces` — Naga Courtyard terrace rework and terrain clearing | structure | medium | 8 | keeps the old piece ids as aliases, so existing courtyards load; new courtyards  |
| `giant-house-split` — Troll clouds as a structure piece (giant house cloud fixes) | structure | medium | 8 | new piece id TFCloud; old troll caves keep their old cloud; new chunks only |
| `final-castle-template-hooks` — Final Castle modpack template hooks and punchable WIP text | structure | medium | 8 | none for existing castles |

Live-server notes for `chunk-blanketing` and `speleothem-config-datadriven`:

- The TF dimension's level.dat generator keeps `dark_forest_canopy_height` (19 on worlds created with 4.3), but the value is now ignored. New Dark Forest chunks get the 4.8 canopy: 14 blocks above the floor and as thick as the biome share instead of 1.5x. Where explored terrain meets new chunks the canopy steps by about 5 blocks. The TF dimension reset above avoids that. Without a reset, pregenerate around the Dark Forest borders, or ship a server datapack that overrides `data/twilightforest/twilight/chunk_blanket_processors/dark_forest_canopy.json` with `"height": 19`. That only removes the height step, because the processor has no thickness field.
- Stalactite lists moved from `data/<ns>/stalactites/` to `data/<ns>/twilight/stalactites/` in the 4.8 format. A 4.3-format datapack still loads: its entries become ore stalactites of that hill size and every larger one. An entry whose blocks all belong to absent mods is skipped with an error instead of becoming a stone spike. Per-block ore weights must be 0 or more.

## Wave 8: Lich Tower and Lich

The headline 4.8 content, scheduled once all infrastructure (wave 3) and decor blocks (wave 6) exist. Lich fight fixes shipped in wave 4, so this wave is the full rework. The tower is the single largest item (about 70h); plan a second TF dimension reset or accept that new towers appear only in unexplored areas.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `ominous-fire-exanimate-essence` — Exanimate Essence and Ominous Fire (with zombification) | mechanic | medium | 14 | new ids only |
| `ominous-candles` — Ominous Candles (17 colours) | block | medium | 8 | new ids only |
| `sinister-spawner` — Sinister Spawner | block | medium | 10 | new ids only |
| `jar-revamp-mason-jar` — Jar revamp: swappable log lids, wobble, Mason Jar | block | medium | 18 | new ids; existing 4.3 firefly and cicada jars (no BE in 4.3, checked) gain a BE  |
| `chiseled-canopy-bookshelf` — Chiseled Canopy Bookshelf (book-fed Death Tome spawner) | block | medium | 10 | new ids only (old ids kept) |
| `scepter-rework` — Scepter repair recipe, essence repair, Renewal enchantment, new scepter FX | mechanic | medium | 12 | new ids; the old shapeless scepter repair recipe ids are removed |
| `lich-boss-rework` — Lich boss fight overhaul (phases, clones, minions, candles, death animation) | boss | hard | 28 | new ids only (attributes, particles, tag); existing Liches load with defaults |
| `lich-tower-revamp` — Lich Tower revamp (jigsaw tower, graveyard yard, wings, bridges, new boss room) | structure | hard | 70 | The structure id twilightforest:lich_tower is unchanged, and the old TFLT* piece |

## Wave 9: Construct update: drying racks, foods, camps, recipe-viewer polish

Drying racks and tanned leather are prerequisites for Traveller's Gear, and camps need the jigsaw markers and berry bushes from earlier waves. The EMI and Jade/TOP work comes last so every category exists. The AlmostUnified side is done (twilightforest is ranked last). Give copper_nugget the forge:nuggets/copper tag in TF datagen and the jerky forge:foods/jerky; neither needs a KubeJS tag.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `drying-rack` — Drying Racks (19 woods) and the twilightforest:drying recipe type | block | medium | 16 | new ids only |
| `dried-foods-and-tanning` — Jerky, gelatinous drops, Stale Bread, Moss Soup and the tanning chain | item | easy | 6 | new ids only |
| `camp-structure` — Traveller camps (small jigsaw surface structure) | structure | medium | 14 | new structure and structure_set; new chunks only |
| `jade-top-providers` — Jade and TOP providers for the drying rack and chiseled shelf | compat | easy | 2 | none |
| `emi-native-plugin` — Native EMI plugin | compat | medium | 12 | none |

## Wave 10: Traveller's Gear sub-project

About 150h, built entirely on Data Components, Data Attachments and around 10 tf-asm transformers. It needs a Mixin config in the fork. Isolated so it never blocks the earlier releases; item-display goes last as the lowest-value goggles modifier.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `travellers-gear` — Traveller's Gear core: 6 armor pieces, armor material, models, base recipes | item | hard | 30 | new ids only |
| `travellers-modifier-framework` — Traveller's Gear modifier system (registry, recipes, grindstone, broken state) | mechanic | hard | 30 | new ids only |
| `travellers-wings-mobility` — Wings: Double Jump, Sidestep, Gradual Glide, High Jump, Agile Ranger | mechanic | medium | 20 | new ids only |
| `travellers-boots-modifiers` — Boots: Slimy Soles, Unrestrained, Water Walk, Straight Ahead, Step Up | mechanic | medium | 18 | new ids only |
| `travellers-vest-modifiers` — Vest: Stealth, Arrow Magnetism, Efficient Eater, Perfect Dodge, Haste, Swift Swim | mechanic | medium | 10 | new ids only |
| `travellers-goggles-modifiers` — Goggles: Zoom, Aquatic Agility, Red Thread Vision, All-Night Goggles | mechanic | medium | 12 | new ids only |
| `travellers-belt-swap-hotbar` — Belt: Swap Hotbar | mechanic | medium | 7 | new ids only |
| `travellers-auto-repair` — Auto Repair modifier | mechanic | medium | 5 | new ids only |
| `travellers-goggles-item-display` — Goggles Item Display HUD (maps, compass, clock, moon dial) | client | hard | 16 | new ids only |

## Wave 11: Optional: density-shaped landmark terrain

Cosmetic replacement of 4.3's working block deformers. Do it only if the Beardifier bridge has proven stable and another TF dimension reset is acceptable.

| Feature | Kind | Portability | h | Savegame |
|---|---|---|---|---|
| `landmark-density-terrain` — Density-shaped terrain for Hollow Hills, Hydra Lair, Yeti Cave and Labyrinth (optional) | worldgen | hard | 20 | changes landmark terrain in new chunks; a TF dimension reset is recommended for  |

## Local divergences from upstream (keep on re-sync)

Deliberate behaviour that differs from TF 4.8. An upstream re-sync must not silently drop or re-add these.

- `client-render-fixes` / `PatchModel.getQuads`: returns `List.of()` for every non-null side and the quads only for the null (unculled) side. 4.3 and 4.8 return the full list for all 7 sides. No patch quad has a cull face, so vanilla and Embeddium draw the null list anyway, and the per-side copies only made every moss/clover patch be baked and drawn 7 times. A mod that samples one face (e.g. Xaero's map colours via `getQuads(state, UP, rand)`) now gets an empty list and falls back to the null list or `getParticleIcon()`, which returns the same sprite. For strict 4.8 parity, delete the `if (side != null) return List.of();` guard.
- `knight-stronghold-fixes` / Aurora Palace walls: 4.3's `IceTowerProcessor` is kept. 4.8 replaced it with a single-state `IceTowerRandomBlockSelectorFactory` only because 69edc8075 dropped the aurora_block `VARIANT` property, and that change is excluded here (`aurora-block-noise-model`). The processor keeps the `Math.abs(x + z) % 16` variant pattern and draws no random numbers, so loot and decorations in a palace keep the 4.3 random stream and a palace that straddles old and new chunks stays consistent. The other five block selectors use the 4.8 `structures/selectors/*Factory.make()` factories. Port `aurora-block-noise-model` before switching the Ice Tower to the factory.
- `tree-feature-bugfixes` / root tracing (`FeaturePlacers.traceRoot`, `traceExposedRoot`): a root strand ends at a `minecraft:features_cannot_replace` block and keeps stepping over `twilightforest:tree_roots_skip` blocks, including roots the same tree already placed. 42c15869c ends a strand on any tree_roots_skip block, so in 4.8 every TreeRootsDecorator strand after the first dies on the shared start block. In 4.8 HEAD (e04ca7909) exposed roots only skip features_cannot_replace blocks; here they end there as well. Both are stricter than upstream about protected blocks and keep multi-strand roots, so root shapes differ from 4.8 near protected blocks.
- `magic-painting-upgrades` / `MagicPaintingRenderer`: the `day_time` opacity modifier uses the day time wrapped to 0-24000; 4.8 feeds the raw day counter, so its day/night layers stop lining up after the first day. A lightning time recorded in another world (higher game time than the current level) is ignored instead of keeping the lightning layers lit. The default back texture lives in common code (`MagicPaintingVariants.DEFAULT_BACK_TEXTURE`) instead of 4.8's client-only `MagicPaintingTextureManager`, which a dedicated server must not load.

## Critic corrections (applied during porting)

- `twilight-jigsaw-framework`: Lower the effort by about 2h. The LiquidSettings.IGNORE_WATERLOGGING blocker does not need a waterlog-stripping processor. In 1.20.1 StructurePlaceSettings still has the boolean keepLiquids, with setKeepLiquids(boolean) and shouldKeepLiquids() (verified in the 1.20.1 client_mappings.txt), and 1.21's LiquidSettings enum replaced exactly that boolean. setLiquidSettings(IGNORE_WATERLOGGING) therefore becomes setKeepLiquids(false). The selection_priority and placement_priority blockers stay: 1.20.1 JigsawBlockEntity and SinglePoolElement have neither (verified), so read the raw jigsaw NBT from StructureBlockInfo.nbt.
- `lich-tower-revamp`: (1) The plan says the leash 'uncooking' transformer is likely unnecessary on 1.20.1. That is wrong: 1.20.1 PathfinderMob still has tickLeash() and shouldStayCloseToLeashHolder() (verified in mappings), so leashed yard zombies would keep pathing back to the fence. Port PathFinderUnrestrainedByLeashTransformer as a JS coremod on PathfinderMob.shouldStayCloseToLeashHolder (AND the result with !persistentData flag at each IRETURN); add about 2h. (2) The painting_variant tags tower_paintings and tower_boss_paintings list 1.21-only variants (baroque, meditative, prairie_ride, unpacked, backyard, bouquet, cavebird, changing, cotan, endboss, fern, finding, lowmist, orb, owlemons, passage, pond, sunflowers, tides). On 1.20.1 the tag fails to load and LichTowerMagicGallery/LichTowerSegment.placePainting gets nothing, so prune the tags to 1.20.1 variants or mark the entries required:false. (3) 4.8 loot tables use enchanted_count_increase, copy_components and registry-keyed set_enchantments, and one table drops minecraft:bundle, which is feature-flagged off in 1.20.1. (4) Templates place twilightforest:skull_candle with the 4.8 blockstate candle count while 4.3 keeps the count in the BE, so those skulls load without candles. final_castle/temp/gazebo.nbt carries a neoforge:attachments fortification_shields entry that 1.20.1 ignores.
- `emperors-cloth`: Only three new JS coremods are needed: HumanoidArmorLayer.renderArmorPiece, ElytraLayer.shouldRender and CapeLayer.render. The mob-detection part (ArmorHooks.modifyArmorVisibility on LivingEntity.getVisibilityPercent) maps onto Forge 47's LivingEvent.LivingVisibilityEvent#modifyVisibility, verified in the forge-1.20.1-47.1.70 sources, so no coremod is needed there. Effort drops by about 1.5h.
- `custom-density-terraforming`: The savegame and world-impact risk is understated. Once the doFill bridge evaluates the vanilla Beardifier, every terrain_adaptation that 4.3 already declares takes effect in the TF dimension: bury on knight_stronghold, labyrinth, troll_cave and yeti_cave; beard_thin on dark_tower, final_castle, hedge_maze, lich_tower, naga_courtyard and quest_grove. yeti_cave would get bury plus 4.3's deformTerrainForYetiLair, a double deformation; 4.8 sets yeti_cave to none for this reason. The Labyrinth gets bury on top of its raiseHills mound. Either make the bridge honour only CustomDensitySource and TF IPieceBeardifierModifier pieces (ignore vanilla terrain_adaptation in the TF dimension), or ship 4.8's per-structure terrain_adaptation values (yeti_cave none; hedge_maze and final_castle beard_box) together with it. Otherwise wave 3 already changes landmark terrain in new chunks.
- `candelabra-overhaul`: This is not self-contained. CandelabraBlock line 202 needs TFItems.EXANIMATE_ESSENCE and the #twilightforest:scepters tag (scepter-rework) to switch on the ominous state, so add depends_on ominous-fire-exanimate-essence and scepter-rework, or move the essence item and the tag earlier. The block also gains a 'lighting' NORMAL/DIM/OMINOUS state next to has_candle_1..3; savegame_impact should mention both.
- `jar-revamp-mason-jar`: No moonworm_jar exists in 4.3 or in 4.8. Only firefly_jar and cicada_jar become JarBlocks (FireflyJarBlock and CicadaJarBlock with the shared twilightforest:jar BE), plus the new mason_jar. The mason jar is also placed directly by 6 Lich Tower templates (5x5/potion, 7x7/desk, jar_study, shelved_jars, 9x9/cauldron_keep, tiered_spawner), not only from code.
- `remove-seed-coremod`: Keep it easy, but put a verification gate in front of it. 4.3 seed.js stores the seed of the last WorldOptions constructed into TFDimensionSettings.seed, which LazyAreaContext and KeyBiomesLayer use. On the Arclight hybrid, extra WorldOptions instances (server.properties parsing, Bukkit or plugin worlds) can mean that value is not the overworld seed. Before switching to WorldUtil.getOverworldSeed(), log both values on the live server; if they differ, the switch shifts the biome and landmark map and the TF dimension needs a reset.
  - Deploy gate (ported in 0ab3a18; **status: not yet run**, it must pass before the first deploy of a jar that contains 0ab3a18). On this server the risk is low: Arclight uses the vanilla world layout (world/DIM-1, world/dimensions), there are no Bukkit plugins and `level-seed` is blank, so after a restart the 4.3 hook holds the level.dat seed. It drifts only after `DedicatedServerProperties` is rebuilt (e.g. `/whitelist on|off`), which rolls a random seed until the next restart. If the live value differs, KeyBiomesLayer and LazyAreaContext offsets shift for TF chunks generated after the deploy.
    1. On the live server, still on the stock 4.3 jar, restart it. Do not run `/whitelist on|off` (or anything else that reloads server.properties) before the check.
    2. Add a temporary `kubejs/server_scripts/seedcheck.js` (do not commit it): `ServerEvents.loaded(e => console.info('[seedcheck] hook=' + Java.loadClass('twilightforest.init.TFDimensionSettings').seed + ' level=' + e.server.getWorldData().worldGenOptions().seed()))`
    3. Restart, compare the two numbers in latest.log (or logs/kubejs/server.log), and compare both with `mcrcon -H localhost -P 65535 -p "$RCON_PW" "seed"`. Both script values pass through a JS double, so compare them with each other exactly; against `/seed` only the leading 15-16 significant digits are meaningful.
    4. Equal: delete seedcheck.js and deploy. Different: deploy only together with a TF dimension wipe: back up `world/dimensions/twilightforest`, delete it, then pregen, as in [world-reset](../server/world-reset.md).
    5. Record the result here: date, both values, and whether a TF dimension wipe was needed. Result: _pending_.
- `multiplayer-boss-scaling`: The classification is right, but port from HEAD (EntityEvents.adjustEntityHealthInMultiplayerFights, addQualifiedGroupPlayerIfNeeded and grantGroupAdvancementIfNeeded), not from the original PR commit. 5db77f784 (#1987) is the commit that introduced BaseTFBoss and rewrote every boss class, so cherry-picking it pulls in boss-base-class-refactor. 4.3's boss spawner BEs already call ForgeEventFactory.onFinalizeSpawn (verified), so MobSpawnEvent.FinalizeSpawn fires.
- `death-tome-tweaks`: This also includes MiscEvents.addTomesToLecterns: using a Death Tome spawn egg on an empty lectern spawns a lectern-bound tome (setOnLectern). It is a few lines on PlayerInteractEvent.RightClickBlock; add about 0.5h.
- `config-option-additions`: Also missing is disableSkullCandleCreation, a new common option that gates makeSkullCandle in EntityEvents. 4.8's bossesSpawnDropChests is just the renamed 4.3 bossDropChests, so keep the 4.3 key. portalCreationPermission is listed here and again in progression-protection-overhaul, and disableIngredientSwitching is listed here and again in uncrafting-table-improvements; do each in one place only.
  - Resolved in 5d070d8: portalCreationPermission (with adminOnlyPortals kept as a legacy switch; the check is in 4.3's TFTickHandler portal scan, not 4.8's ProgressionEvents) and disableIngredientSwitching (an extra boolean in SyncUncraftingTableConfigPacket after allowShapeless, network protocol bumped to `2-skytekx.3`) are done here. progression-protection-overhaul and uncrafting-table-improvements must skip them, or the ForgeConfigSpec keys get defined twice and the protocol gets bumped twice.
- `loot-table-fixes`: 4.8 moved most chest loot tables out of chests/: aurora_cache, darktower_*, hill_1-3, labyrinth_*, stronghold_cache/room, tree_cache, troll_*, useless, well, fancy_well, foundation_basement and quest_grove_dropper now sit at twilightforest:<name>. New tables (hedge_cloth, fallen_trunk_loot, camp_tent, camp_pot, camp_armor_rack) also sit at the root. Keeping 4.3's chests/* ids is right, but every 4.8 structure JSON, template or Java constant copied later (TFLootTables, camp templates with LootTable NBT) must be remapped. The plan's 'twilightforest:chests/hedge_cloth' is a 4.3-convention id, not the 4.8 id.

- ordering: candelabra-overhaul (wave 6) references TFItems.EXANIMATE_ESSENCE and #twilightforest:scepters, which arrive in wave 8 (ominous-fire-exanimate-essence, scepter-rework). Move the exanimate_essence item and the scepters tag into wave 6, or move the candelabra to wave 8.
- ordering: custom-density-terraforming (wave 3) switches on 4.3's existing terrain_adaptation values (bury and beard_thin) for every landmark in new chunks the moment it lands. That contradicts the 'waves 1-6: no worldgen change' statement in savegame_summary. Gate the bridge to CustomDensitySource-only until wave 7 (or 11), or plan the first TF dimension reset right after wave 3.
- ordering: AlmostUnified and KubeJS unification of twilightforest:copper_nugget is scheduled in wave 9, but copper_nugget appears in wave 7 (oreberries). Add it to config/almostunified unify.json and modPriorities in wave 7, or Create and TF nuggets coexist for two releases. Done ahead of wave 7: copper is already a unified material and twilightforest sits last in modPriorities, so the TF nugget unifies to Immersive Engineering's as soon as it carries forge:nuggets/copper.
- ordering: knight-stronghold-fixes (wave 2) and progression-protection-overhaul (wave 5) both touch the 4.3 ProgressionEvents knight-stronghold hack ('pos.getY() >= sea level - 2 → unprotected'). 4.8 removed that hack only when per-piece protection landed (e9116aa11), so keep it in wave 2 and remove it in wave 5. Otherwise the stronghold surface becomes protected or unprotected inconsistently between releases.
- ordering: weather-renderer-rewrite (wave 5) is ported from HEAD, where TFWeatherRenderer.renderLockedStructure consumes the per-piece boxes of StructureProtectionPacket v2. Add depends_on progression-protection-overhaul and keep that order inside wave 5.
- ordering: lich-fight-fixes (wave 4) contains #2384 (c576062db), which calls FortificationShieldAttachment.addShieldBreakParticles and the new shield_break particle from fortification-shield-rework (wave 5). Move fortification-shield-rework into wave 4 or defer that line.
- ordering: Do not copy 4.8 worldgen/structure/*.json for the new minor structures (hollow_tree, swamp_hollow_tree, fallen_trunk, camp, giant_house) before the codec changes are in. 4.8 nests decoration_clearance{} and controlled_spawns{}, replaces hint_mob with hint_creature, and adds structure_icon, while 4.3's LandmarkStructure and ProgressionStructure codecs expect flat chunk_clearance_radius, allow_biome_*, hint_mob and hint_item. Either port the codec reshape first (it belongs with avoid-landmark-modifier-v2 and landmark-wrapped-structure-types in wave 3) or hand-convert every JSON in waves 7 and 9.
- ordering: Duplicated work across waves: 'Lich bombs spare items' appears in lich-fight-fixes (wave 4) and misc-event-tweaks (wave 5); portalCreationPermission appears in config-option-additions (wave 2) and progression-protection-overhaul (wave 5); disableIngredientSwitching appears in config-option-additions (wave 2) and uncrafting-table-improvements (wave 5). Assign each to one feature. Resolved for the two config options: both are done in config-option-additions (5d070d8), see its critic note above.
- ordering: lich-tower-revamp (wave 8) depends on rising zombies spawning per piece through SpawnIndexProvider. In 4.3 structure spawns run through ChunkGeneratorTwilight.gatherPotentialSpawns and getMobsAt, not 4.8's LevelEvent.PotentialSpawns handler (EntityEvents.structureSpecialSpawns). Wire SpawnIndexProvider into 4.3's ChunkGeneratorTwilight.gatherPotentialSpawns as part of the tower work and budget it there.

## Excluded (not sensible on 1.20.1)

- `chunkgen-density-overhaul`: Merges terrain-density-rewrite, terrain-density-router-split and the terrain half of caves-trollcave-gianthouse. 4.8 deletes ChunkGeneratorTwilight and the TFTerrainWarp stack and switches to minecraft:noise with biome_driven_terrain/noise routers and a biome_terrain_data registry. The port would take 60-90h, change all TF terrain, require a TF dimension reset plus rewriting the stored level.dat generator, and break 4.3's landmark picking, troll clouds, canopy, glaciers and spawn lists, which all hang off ChunkGeneratorTwilight. The structure benefits (beards, CustomDensitySource) are reachable for about 18h through the doFill Beardifier bridge (custom-density-terraforming). Cost is out of proportion.
- `landmark-placement-rework`: Replacing BiomeForcedLandmarkPlacement and the TFLandmark enum with LandmarkGridPlacement is tied to the chunkgen overhaul and 4.3's ChunkGeneratorTwilight.createStructures. It brings no player-facing gain on 4.3. The useful part (progression and landmark wrapper types) is kept as landmark-wrapped-structure-types; structure_icon is not portable.
- `structure-map-icons`: Merges map-icons-vanilla-decoration-registry and vanilla-map-decoration-icons. MapDecorationType became a registry in 1.20.5; it is a closed enum in 1.20.1. 4.3's TFMagicMapData and maprendercontext.js already draw the icons, and conquered markers are ported in magic-map-improvements.
- `hostile-wolf-variants`: Depends on the 1.20.5+ WolfVariant registry and its textures. The target-goal changes are kept in mob-balance-tweaks.
- `model-consolidation-refactor`: An internal refactor of the Jappa/classic model split with no visible change. Individual model fixes are taken in client-visual-fixes.
- `trophy-renderer-unification`: Part of the 4.8 entity-model refactor, with no gameplay value.
- `boats-to-vanilla-enum`: Relies on NeoForge enumextensions (Boat.Type). On 1.20.1 it would delete existing twilightforest:boat and chest_boat entities from saves.
- `chest-boat-texture-generator`: Merges chest-boat-texture-composite. Built for the vanilla Boat.Type move; 4.3's TwilightChestBoat already has full textures.
- `tf-chest-vanilla-blockentity`: BlockEntityTypeAddBlocksEvent and registry aliases are NeoForge 1.21 only. It would rename the existing tf_chest block entity for no gain. Trapped chests are ported with their own BE instead.
- `api-refactors-skip`: Sign and chest BE removal, the data-map move for crumble and transformation recipes, class renames and Rarity/BoatType extensions are API migrations with no player benefit, and some would change saved blocks.
- `skull-candle-blockstate-migration`: Moving the candle count from the BE into the blockstate needs a datafixer, or placed skull candles reset. The player-facing #2119 candle removal is kept in block-qol-bundle.
- `aurora-block-noise-model`: Merges aurora-noise-model. A cosmetic refactor that drops the variant property 4.3's IceTowerProcessor sets. Low value relative to the risk.
- `music-discs-jukebox-songs`: jukebox_song is a 1.21 vanilla registry; 4.3's RecordItem discs are equivalent.
- `neoforge-data-maps`: NeoForge-only. 4.3 already implements crumble and transformation as recipe types. The new crumble 'chance' field could be added to 4.3 recipe JSON if ever wanted.
- `tick-freeze-weather`: /tick (TickRateManager) does not exist in 1.20.1.
- `feather-fan-mace-compat`: The Mace and impulse fall-damage API are 1.21-only.
- `neo-only-misc`: Breeze lifedrain, the NeoForge config screen, FIRESTARTER ItemAbility, BlockCapabilityDirectionalCache and enum-extension ItemDisplayContext all depend on 1.20.5+ content or NeoForge-only APIs.
- `update-url-interceptor`: Upstream release plumbing; a private backport must not point at upstream's 4.8 update feed.
- `tf-asm-java-coremod`: A build migration forced by NeoForge dropping JS coremods; Forge 47 runs 4.3's JS coremods fine. Individual hooks come with their features.
- `beanification-di`: Merges beanification-mdg2-infra. A DI framework and build-system change with no gameplay effect; stripped during porting (see shared infrastructure).
- `mc-version-ports-1205-121`: Merges port-infrastructure-1-21. Port commits and their own regressions (arrow dupe 091d816fd, armor durability #2041, hint book #2055, map X #2064 and others) do not exist in 4.3.
- `death-tome-spawner-tweak`: Superseded in 4.8 by the chiseled canopy bookshelf, which is ported instead.
- `rei-plugin`: REI is not in the SkyTekx pack (checked modrinth.index.json and local-mods); about 20h for no user.
- `tips-mod-compat`: Merges tips-compat. The Tips mod is not in the pack.
- `animal-variant-datapack-registries`: Merges critter-variants-datapack. Only datapack-addable rabbit and bird textures, at about 6h with a risk of resetting existing variants. Not worth it.
- `brazier`: Still tagged #twilightforest:wip in 4.8 and placed by no worldgen; the brazier parts of lighting-decor-renderers are dropped with it.
- `four-leaf-clover`: From misc-simple-items: a placeholder item with no recipe, loot or use in 4.8. moss_soup moved to dried-foods-and-tanning.

## Twilight Forest addons

### In the pack (2026-11-01)

The version blocker below (note 1) is fixed. `ports/twilightforest/gradle.properties` sets
`mod_version=4.3.2508-skytekx.1`, so the fork jar is `twilightforest-1.20.1-4.3.2508-skytekx.1-universal.jar`
and Forge sees `4.3.2508-skytekx.1`. Maven orders the unknown qualifier after `4.3.2508` and before
`4.3.2509`, which satisfies every `[4.3.x,)` range and still fails the 1.21.1-only `[4.8,)` ranges. Bump the
trailing number for each fork release that goes into `local-mods/`.

Each addon below is the newest 1.20.1 Forge build. Env is client / server as written to
`modrinth.index.json`.

| Addon | Version | Source | Env | License | Why |
|---|---|---|---|---|---|
| Twilight's Flavors & Delight | 2.2.8 | Modrinth index | required / required | LGPL-2.1 | Farmer's Delight crossover: TF knives, Maze Stove, Fiery Pot, ~67 foods. |
| Spartan Weaponry: Twilight Forest | 1.20.1-3.1.1 | Modrinth index | required / required | MIT | Ironwood, steeleaf, knightmetal and fiery tiers for the pack's Spartan Weaponry. |
| Spartan Weaponry Addon Toolkit (library) | 1.20.1-1.6.1 | Modrinth index | required / required | MIT | Required by Spartan TF (`[1.4.1,1.20.1-2.0.0)`). |
| Twilight Forest - Dungeons & Villages | 1.2.3 | Modrinth index | required / required | ARR, Modrinth-hosted | Villages, hedge dungeons and trading inside the TF dimension. |
| Twilight Forest: The Lost Blocks | 1.20.1-1.5.0 | Modrinth index | required / required | MIT | Builder blocks: towerwood and thorn sets, stairs, slabs and walls for TF stone. |
| Farmer's Cutting: Twilight Forest | 1.20.1-1.0 | Modrinth index | optional / required | MIT | 50 cutting-board recipes for TF woods. Data only. |
| Apothic Category Compat | 2.1.0 | Modrinth index | unsupported / required | MIT | Apotheosis loot categories for TF scepters and other modded weapons, pack-wide. Server side only. |
| Twilight Treehouses | 1.0.2.1 | Modrinth index | optional / required | ARR, Modrinth-hosted | Four jigsaw treehouse dungeons in TF forest biomes. Worldgen data only. |
| Twilight Aether | 1.20.1-1.0.0 | Modrinth index | required / required | MIT | Aether accessory gloves and cape in TF materials. |
| Twilight Construct | 1.0.7 | `local-mods/` (CurseForge file 6462175) | both | LGPL-2.1 | Eight TF materials for Tinkers' Construct. |
| Twilight Tweaks | 1.2 (1.20.x) | `local-mods/` (CurseForge file 4702979) | both | LGPL-2.1 | Final-boss spawner hook and re-fightable bosses (Time Powder on spawner remnants). |
| The King of Twilight | 1.0.1 | `local-mods/` (CurseForge file 8029727) | both | MIT | The Twilight Lord final boss, spawned through Twilight Tweaks. |
| Twilight Mobs Loots (1.20.1 backport) | 1.1.1-1.20.1-skytekx.1 | `local-mods/`, built from `ports/twilight-mobs-loots/` | both | MIT | Trophies for 48 regular TF mobs: 10% player-kill drop (giants 50%, +5% per Looting level), floor and wall blocks with the mob's sound and particles, wearable on the head or in the Curios head slot or a new `trophy` slot. |

Distribution rules: a Modrinth-hosted addon goes into the index, even when it is All Rights Reserved,
because the file comes from the Modrinth CDN. A CurseForge-only addon is bundled in `local-mods/` only
with a license that allows redistribution. Each bundled jar must be byte-identical to its CurseForge
file and needs an attribution file in
[`modrinth-permissions/`](../../modrinth-permissions/README.md). All three bundled addons were checked
against the CurseForge `licenseType`, the source repository LICENSE and the jar's mods.toml.

Twilight Mobs Loots exists only for 1.21.1 NeoForge, so it is backported from source (upstream commit
`41e447de`, 1.1.1, MIT) in [`ports/twilight-mobs-loots/`](../../ports/twilight-mobs-loots/README.md).
Build `ports/twilightforest` first, then `./gradlew build` in the port; the jar is byte-reproducible.
It requires `twilightforest` `[4.3.2508-skytekx.1,)`, so `local-mods/` must carry the fork jar, not the
older `4.3` build: until that swap (the pack-side glue item above) lands, Forge refuses to load the
addon and the full pack does not boot. Not ported: the Twilight Moonshine Moon Rabbit compat (no 1.20.1
build), and re-registering TF's boss trophies as curios (the fork already does it). Upstream's
`trophy_silver.png` is a copy of TF's CC BY-NC-SA `trophy_minor.png`; the backport does not ship it and
uses `twilightforest:item/trophy_minor` instead, so the jar stays MIT only (SHA-1 `03478ccb`, 135505
bytes, recorded in its `modrinth-permissions/` file).

Pack-side glue for these addons:
- `config/twilighttweaks-common.toml` makes the final-boss spawner run
  `summon twilightforestfinalboss:twilight_lord`. Its keys end in a space
  (`"The command that the spawner should run: "`); without it, Forge resets the value.
- `kubejs/server_scripts/tag_fixes.js` strips TF&D's 15 Neapolitan-only foods from
  `farmersdelight:sweets`, `farmersdelight:snacks` and `diet:sugars`. Neapolitan is not in the pack,
  and the unregistered ids otherwise drop those tags whole.
- `tools/patch_mods.py` writes `minVersion` into `mixins.tf_dnv.json`.
- The `skytekx3_fixes` datapack ports Twilight Tweaks' six uncrafting recipes to the 4.3 `input` format.
  Bell, anvils and knightmetal ingot are converted; tipped arrow and written book are disabled because
  TF ships its own.
- AlmostUnified lists `twilightforest` last in `modPriorities`. See [AlmostUnified](almostunified.md).

Compatibility checks against the fork (2026-11-01 build): every `twilightforest/*` class, field and
method that the 12 jars link exists. Every mixin target and injection point resolves, including the TF
targets of The Lost Blocks and the three Twilight Tweaks coremods. A full-pack dedicated-server boot on
a fresh world (plain Forge 1.20.1-47.4.18) reached `Done` with 0 FATAL, crash or watchdog lines and no
ERROR from an addon or from TF. The tracked Arclight jar could not be used for this test: its own
`MinecraftServerMixin` failed in the test sandbox even with an empty `mods/`.

Re-check these when the matching 4.8 code is backported, because the addons hook them directly:
- The Lost Blocks mixins: `HedgeBlock#attack` (anchor `INVOKE Level#scheduleTick`),
  `HedgeBlock#playerDestroy` (anchor `INVOKE Block#playerDestroy`), `ThornsBlock#<init>`,
  `#createBlockStateDefinition` and `#doThornBurst`, and `FinalCastleBossGazeboComponent#postProcess`
  (HEAD).
- Twilight Tweaks coremods: `BossSpawnerBlockEntity.tick` (hook after the first `PUTFIELD`, which must
  stay `spawnedBoss = true`), `FinalBossSpawnerBlockEntity.spawnMyBoss` (before the first `ICONST_0`),
  and `FinalCastleBossGazeboComponent.postProcess` (before the first `GETSTATIC`). The 4.8 boss rework
  touches the first two.
- Symbols the added jars link that 4.8 removed or renamed (the freeze-list in note 2):
  `util.TwilightItemTier`, `enums.TwilightArmorMaterial`, `init.TFDimensionSettings`,
  `world.registration.TFGenerationSettings`, `block.TFLogBlock`, `item.recipe.TransformPowderRecipe`
  and `TFItems.CICADA`, `FIREFLY` and `MOONWORM`. Also keep `block.SpecialMagicLogBlock` and the
  `ItemTagGenerator` and `EntityTagGenerator` tag constants, which Twilight Tweaks, Twilight Construct and
  Spartan TF use.
- Content the addons expect from newer upstream TF: Treehouses place `twilightforest:rope` and
  `twilightforest:mason_jar` (air until `rope` and `jar-revamp-mason-jar` land). Dungeons & Villages
  rolls `twilightforest:chests/casket_loot` (missing, so that one pool is empty) and carries `twilightforest:jar`
  block-entity data.

### Pending permission

- **Twilight Spellbooks** 1.2.0 (Mangelox; CurseForge 1585352) is All Rights Reserved and CurseForge only.
  CurseForge's `allowModDistribution: true` only lets third-party launchers download it through the
  CurseForge API. It does not allow bundling it in a Modrinth `.mrpack`. The jar's `twilightforest
  [4.3.2508,)` requirement is now met, and its other dependencies match the pack. Once Mangelox gives
  written permission, add the jar to `local-mods/` with a `modrinth-permissions/` file (Special
  permission, plus the permission as proof).

### Planned backports (1.21.1-only addons)

These exist only for 1.21.1 NeoForge. They are ported as separate work once the 4.8 code they depend
on is in the fork (details in the table below):
- **Create: Shimmer**: the only maintained Create x TF bridge. It needs Create: Dragons Plus for 1.20.1,
  a rework of the 1.21 recipe API, and the author's OK for its All Rights Reserved assets.
- **Alternative Twilight (Remastered)**: after the 4.8 boss rework is backported. It replaces the
  1.20.1 Alternative Twilight, which conflicts with that rework.
- **Spiced Twilight**: after the 4.8 entity and Naga changes.
- **Twilight Crossbow**: optional small port. It needs data components and the 1.21 crossbow code
  rewritten for 1.20.1. (Twilight Mobs Loots is done, see [In the pack](#in-the-pack-2026-11-01).)

### Research (2026-11-01)

| Addon | Action | Fits pack | Versions |
|---|---|---|---|
| [Twilight's Flavors & Delight (TF + Farmer's Delight)](https://modrinth.com/mod/twilight-delight) | add_existing_1201_build | yes | 1.20.1 Forge/NeoForge 2.2.8 (2026-10-28, current), 1.21.1 NeoForge 3.2.9. Requires twilightforest [4.2,) and farmersdelight [1.20.1-1.3,). B |
| [Spartan Weaponry: Twilight Forest](https://modrinth.com/mod/spartan-weaponry-twilight-forest) | add_existing_1201_build | yes | 1.20.1 Forge/NeoForge 1.20.1-3.1.1 (2025-08-01). No 1.21.1. Needs spartanweaponry [3.1.1,) (pack has 3.2.1), Spartan Weaponry Addon Toolkit  |
| [Twilight Construct](https://www.curseforge.com/minecraft/mc-mods/twilight-construct) | add_existing_1201_build | yes | 1.20.1 Forge/NeoForge 1.0.7 (2025-04-25). No 1.21.1. twilightforest [0,), tconstruct [0,). Every TConstruct class it references exists in th |
| [Twilight Forest - Dungeons & Villages](https://modrinth.com/mod/the-twilight-forest-dungeons-villages) | add_existing_1201_build | yes | 1.20.1 Forge/NeoForge 1.2.3 (2025-11-02), 1.21.1 NeoForge 2.0.3 (2026-10-24). twilightforest [4.3.1735,). |
| [Twilight Forest: The Lost Blocks](https://modrinth.com/mod/twilight-forest-the-lost-blocks) | add_existing_1201_build | yes | 1.20.1 Forge/NeoForge 1.20.1-1.5.0, 1.21.1 NeoForge 1.21.1-1.5.0. Declares twilightforest '[1.20.1,)', which passes. |
| [Farmer's Cutting: Twilight Forest](https://modrinth.com/mod/farmers-cutting-twilight-forest) | add_existing_1201_build | yes | 1.20.1 Forge/NeoForge 1.20.1-1.0 (2024-11-22), 1.21.1 NeoForge 1.21.1-1.0. |
| [Apothic Category Compat](https://modrinth.com/mod/apothic-category-compat) | add_existing_1201_build | yes | 1.20.1 Forge 2.1.0 (2026-06-17), 1.21.1 NeoForge 2.1.0. Every dependency is optional ([0,)). |
| [Twilight Treehouses](https://modrinth.com/mod/twilight-treehouses) | add_existing_1201_build | yes | 1.20.1 Forge 1.0.2.1, 1.21.1 NeoForge 1.0.2 (2025-03-25). Declares no TF dependency; it is data plus one class. |
| [Twilight Aether](https://modrinth.com/mod/twilight-aether) | add_existing_1201_build | yes | 1.20.1 Forge 1.20.1-1.0.0 (2024-08-16). No 1.21.1. Requires The Aether. Its mods.toml declares no TF dependency, but it does link TF classes |
| [Twilight Spellbooks: Iron's Spells 'n Spellbooks Addon (Mangelox)](https://www.curseforge.com/minecraft/mc-mods/twilight-spellbooks-irons-spells-n-spellbooks) | add_existing_1201_build | yes | 1.20.1 Forge 1.2.0 and 1.21.1 NeoForge 1.2.0 (2026-08-07). Its dependencies match the pack exactly: irons_spellbooks 1.20.1-3.16.1, irons_li |
| [Twilight Tweaks (jodlodi)](https://www.curseforge.com/minecraft/mc-mods/twilight-tweaks) | add_existing_1201_build | yes | 1.20.x Forge 1.2 (2023-08-13), 1.21.1 NeoForge 1.3 (2025-05-18). Its TF versionRange is the malformed ',)', which Forge treats as 'any'. |
| [The King of Twilight](https://www.curseforge.com/minecraft/mc-mods/the-king-of-twilight) | add_existing_1201_build | yes | 1.20.1 Forge 1.0.1 (2026-05-03). No 1.21.1. Needs ISS and GeckoLib, both already in the pack. |
| [Alternative Twilight (1.20.1)](https://modrinth.com/mod/alternative_twilight) | skip | no | 1.20.1 Forge 1.20.1-1.1.0 (2025-06-06). The 1.21.1 line is a separate remake. Its TF range is the soft '4.3.2145', which accepts any version |
| [Alternative Twilight (Remastered)](https://modrinth.com/mod/alternative-twilight-remastered) | needs_backport | yes | 1.21.1 NeoForge only, 0.0.2 (2026-05-15). 34 classes, 3 mixins. Source github.com/Lvedy114/alternative-twilight. |
| [Create: Shimmer](https://www.curseforge.com/minecraft/mc-mods/create-shimmer) | needs_backport | yes | 1.21.1 NeoForge only (1.3.1, 2026-08-13). Needs create [6.0.7,), create_dragons_plus [1.8.0,), curios 9 and twilightforest [4.7.0,). |
| [Twilight Mobs Loots](https://modrinth.com/mod/twilight-mobs-loots) | backported_bundled | yes | 1.21.1 NeoForge 1.1.1 only. twilightforest [4.8,5.0). 24 classes, no mixins. Done: ported in `ports/twilight-mobs-loots/` (22 classes, Twilight Moonshine compat dropped) and bundled as `local-mods/twilight_mobs_loots-1.1.1-1.20.1-skytekx.1.jar`. |
| [Twilight Crossbow (baguchi)](https://modrinth.com/mod/twilight-crossbow) | needs_backport | yes | 1.21.1 NeoForge 1.21.1-1.5.0 only. TF [0,). 23 classes, 4 mixins. |
| [Spiced Twilight (baguchi)](https://modrinth.com/mod/spiced-twilight) | needs_backport | yes | 1.21.1 NeoForge 1.21.1-2.0.0 only. twilightforest [4.8.3287,). 32 classes, 9 mixins into 4.8 entity code. |
| [Mystic Crown Compat](https://modrinth.com/mod/mystic-crown-compat) | skip | no | 1.21.1 NeoForge 1.1.0 only. twilightforest [4.8,). 14 classes, 5 mixins. |
| [Twilight Forest Cave Fix](https://modrinth.com/mod/twilight-forest-cave-fix) | skip | no | 1.20.1 Forge 1.0.0 only. twilightforest [4.3.2145,). |
| [Twilight Iris Xe Fix / TF-Intel-Fix](https://modrinth.com/mod/twilight-iris-xe-fix) | skip | no | 1.20.1 Forge 1.0.0 each. |
| [Cloudertinker](https://www.curseforge.com/minecraft/mc-mods/cloudertinker) | skip | no | 1.20.1 Forge 1.2.11 (2026-10-02). twilightforest [4.2.1518,). TConstruct references resolve against 3.11.2. |
| [Cogs of Carminite (jodlodi)](https://www.curseforge.com/minecraft/mc-mods/cogs-of-carminite) | skip | no | 1.20.1 Forge/NeoForge 1.0.23 (2024-07-01), built for Create 0.5.1. No Create 6 build. |
| [Twilight Spark's Delight](https://www.curseforge.com/minecraft/mc-mods/twilight-sparks-delight) | skip | no | 1.20.1 Forge 1.0.9 and 1.21.1 NeoForge 1.0.9 (2026-10-23). farmersdelight [1.3.4,), twilightforest [0,). |
| [Umbral Skies (TeamTwilight/Drullkus)](https://www.curseforge.com/minecraft/mc-mods/umbral-skies) | skip | no | 1.20.1 Forge/NeoForge 1.3 (2023-09-25; deps twilightforest [4.3.0,), aether [1.20.1-1.0.0-beta.1.2,)), 1.21.1 NeoForge up to 1.6.2 (2026-06- |
| [Every Compat (Wood Good)](https://modrinth.com/mod/every-compat) | skip | no | 1.20.1 Forge 1.20-2.9.28 (2026-10-25), 1.21.1 2.11.56. Requires Moonlight, which the pack has. |
| [ACOFI (Alternate Create Of Fiery Items) / Twilight Forest Smithing](https://modrinth.com/mod/acofi) | skip | no | ACOFI 1.20.1 Forge 1.2.0 (twilightforest [4.3.1735,)) and 1.21.1. TF Smithing 1.20.1 Forge/NeoForge and 1.21.1 (data only). |
| [Twilight Forest Bosses Resurrection / Twilight Boss Respawn](https://www.curseforge.com/minecraft/mc-mods/tw-resurrection) | skip | no | TFBR 1.20.1 Forge 4.2.2 and 1.21.1 (2025-12-26), twilightforest [4.3,). Boss Respawn 1.20.1 Forge and 1.21.1 1.0.0 (2026-08-23), twilightfor |
| [Twilight Forest Final Boss (Castle Keeper) / Twilight Endgame / Twilight Forest: Finale](https://www.curseforge.com/minecraft/mc-mods/twilight-forest-final-boss) | skip | no | Castle Keeper 1.20.1 Forge 1.2.0 (2025-03; the 1.21.1 remake is a separate mod). Twilight Endgame 1.20.1 Forge and 1.21.1 1.0.0 (2026-10-27/ |
| [Other 1.20.1 TF addons that need mods not in the pack](https://modrinth.com/mod/kaleidoscope-twilight) | skip | no | Most have 1.20.1 Forge builds. Goety Twilight needs TF [4.3.2508,). |
| [Boss Refactor: Twilight Forest / TF: Reviving Ancient Buildings](https://www.curseforge.com/minecraft/mc-mods/boss-refactor-twilight-forest) | skip | no | 1.20.1 Forge only, about 1.6K and 700 downloads. |
| [Twilight's Additions / Twilight's Big Swords / Travellers Set Plus / Spellbooks of Twilight / Reliquified TF](https://www.curseforge.com/minecraft/mc-mods/twilights-additions) | skip | no | 1.21.1 NeoForge only. |
| [Twilight Boss Music](https://www.curseforge.com/minecraft/mc-mods/twilight-boss-music) | skip | no | 1.20.1 Forge 1.1 (2025-03). |

SOURCES / METHOD: I queried the Modrinth API (v2/search, project/{slug}/version) and CurseForge (search pages plus api.cfwidget.com for file lists). Latest 1.20.1 Forge and some 1.21.1 jars were downloaded read-only to /tmp/claude-0/-home-user-skytekx3-1201-server/b8d056e1-624d-50f6-b93c-43ffe737fcb9/scratchpad/addons/jars and jars21, and I checked only their mods.toml, mixin configs and bytecode string constants. Nothing was executed. Addon class references were checked against our local-mods/twilightforest-1.20.1-4.3-universal.jar, the 4.8 source tree, and the pack's Create 6.0.8, TConstruct 3.11.2 and Aether 1.5.2 jars. Upstream TF on CurseForge: 1.20.1 = 4.3.2508, 1.21.1 = 4.8.3345 (2026-10-28).

1) BLOCKER: TF version string. Our TF jar reports mod version "4.3": build.gradle has `version = CIRevision ?: mod_version`, gradle.properties has mod_version=4.3, and the manifest has Implementation-Version 4.3. Forge rejects every addon with a build-qualified range: D&V and ACOFI [4.3.1735,), Spartan TF [4.3.1893,), Cave Fix [4.3.2145,), Twilight Spellbooks and Goety Twilight [4.3.2508,). Fix: build the backport jar with -PCIRevision=4.3.2508.<n> (anything >= 4.3.2508). Stay on 4.3.x rather than 4.8.x; the 1.21-only addons pin [4.8,) anyway.

2) Freeze-list for the backport: 1.20.1 addons link these 4.3 symbols, which 4.8 deleted or renamed. Keep them, or add deprecated shims, or the addons crash with NoClassDefFoundError/NoSuchFieldError:
- util.TwilightItemTier (Spartan TF, TF&D, Cloudertinker)
- enums.TwilightArmorMaterial (Twilight Aether, Umbral Skies)
- init.TFDimensionSettings and world.registration.TFGenerationSettings, renamed in 118cdcb41 (Twilight Construct, Twilight Tweaks)
- block.TFLogBlock and item.recipe.TransformPowderRecipe (TF&D)
- util.EntityUtil (bf90b4a54) and block.TFChestBlock (17602f730) (boss respawn mods)
- network.ThrowPlayerPacket/TFPacketHandler and capabilities.* (Alternative Twilight, Cloudertinker)
- fields TFItems.CICADA/FIREFLY/MOONWORM (TF&D, D&V)

Mixin targets to keep stable: HedgeBlock, ThornsBlock and FinalCastleBossGazeboComponent (Lost Blocks). Keep 4.3 recipe ids/paths and tag names too, since datapack addons override by path.

3) Port these upstream fixes instead of using addons. TFCavesCarver: 939ceb293, 5c68bab19, 3b6def4c5, b0ee6b0db, 057fa40e7, e5eb085cb, d60cc688b, 51b29421c; this replaces Twilight Forest Cave Fix, whose @Overwrite would conflict. aurora.fsh: bdbc5c901 and 8bca68e8a; this replaces the Iris Xe and Intel fix mods.

4) Coupled decision: backporting the 4.8 boss rework (BaseTFBoss, rewritten Naga/Lich/Hydra/UrGhast/KnightPhantom) rules out Alternative Twilight 1.20.1, Boss Refactor and Boss Respawn, but enables porting Alternative Twilight Remastered and Spiced Twilight later.

5) Distribution: Modrinth-hosted addons go into modrinth.index.json and then tools/resolve_all.py. They are TF&D, Spartan TF plus spartan-weaponry-addon-toolkit, D&V, Lost Blocks, Farmer's Cutting TF, Apothic Category Compat, Twilight Treehouses and Twilight Aether; ARR is fine there because Modrinth hosts the files. CurseForge-only addons can go into local-mods/ only with a redistributable license: Twilight Construct (LGPL-2.1 source), Twilight Tweaks (LGPL-2.1), King of Twilight (MIT). Twilight Spellbooks is ARR and needs written permission from Mangelox before it can go in the .mrpack.

6) Recommended order: TF&D, Spartan TF, Twilight Construct, D&V, Lost Blocks, Farmer's Cutting TF, Apothic Category Compat, Treehouses, Twilight Aether, then Twilight Tweaks plus King of Twilight, then Twilight Spellbooks (after permission). Boot-test after each group (Done reached, 0 FATAL). Create: Shimmer is the only 1.21.1-only addon worth a real backport (Create 6 API is mostly shared; it also needs Create: Dragons Plus and permission for its ARR assets). Twilight Mobs Loots and Twilight Crossbow are optional small ports.
