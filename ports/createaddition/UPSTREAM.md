# Upstream: Create Crafts & Additions (1.20.1 backport base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Create Crafts &
Additions, the base of the SkyTekx 1.20.1 Forge backport of the mod's 1.21.1 line.

| | |
|---|---|
| Repository | https://github.com/mrh0/createaddition |
| Ref | branch `1.20.1` |
| Commit | `42b6f2d06f0412a7cee787d767449d5b1b3b81b8` (the merge of PR #1200) |
| Commit date | 2026-11-02T23:02:03+01:00 |
| Upstream version | `mod_version = 1.3.3` (Minecraft 1.20.1, Forge 47.4.0, Create 6.0.8-289) |
| Port source (later commits) | branch `1.21.1` @ `658a390b8ff4daa17013e9447b29949330c8a42a` (2026-11-02, `mod_version = 1.7.3`; release 1.7.2 on 2026-10-01) |

Every file in the snapshot is byte-identical to `git archive 42b6f2d` of the upstream repository,
including the upstream `.github/`, `.vs/` and screenshot files. Changes made on top of the snapshot
are separate commits in this repository's history (`git log -- ports/createaddition`).

## What the base contains beyond the shipped 1.3.3 jar

The pack shipped `createaddition-1.20.1-1.3.3.jar` from Modrinth (built 2025-11-10). Its resources
are identical (line endings aside) to upstream commit `838cfab` (2025-11-10, "Merge pull request
#1088 ... Fixed PEI visual in Forge"), which is the 1.3.3 release point. The base commit
`42b6f2d` adds these merged but unreleased upstream changes, all of them wanted:

- `8e996ec` (PR #1189, merged in `a82f60e`): the Liquid Blaze Burner needs a Straw when a
  Schematicannon prints it (`LiquidBlazeBurnerBlockEntity#getRequiredItems`).
- `7ce2204` (PR #1167, merged in `0077170`): new barbed wire block model and texture.
- Translations: `ae01f10` ru_ru (PR #1061), `2fc2bcf` new vi_vn, `d246b82` / `3c4b57e` /
  `8bf7647` zh_cn (PR #1106), `257b673` es_ar (PR #1200).

No registry id, block state, NBT key or config key changed between 1.3.3 and the base.

## License

- Code and assets: MIT License, `Copyright (c) 2024 MRH` (`LICENSE`, kept unchanged; `mods.toml`
  says `MIT License, Copyright 2022 MRH0`). `LICENSE` also notes that the sounds come from
  pixabay.com (Pixabay Content License) and freesound.org (CC0). The Pixabay sounds may only ship
  inside the mod jar, never as standalone files.
- `RollingMillBlockEntity` contains code "taken and adapted from the Create repo", and a few
  textures (rolling mill casing, digital adapter, portable energy interface, copper wire casing, the
  seed oil and bioethanol buckets) are edited copies of Create textures. All of them were added to
  CC&A between 2021 and 2023, when Create was MIT-licensed as a whole (`Copyright (c) 2019
  simibubi`). Create's assets are All Rights Reserved since Create commit `9dcc21950`
  (2025-09-21): the backport must not copy any Create asset dated 2025-09-21 or later.
- The Electric Pump that upstream drew in 2026 (source change 28) reuses Create files from before
  that date. Its block and item models copy the two 12x3x12 flange cuboids, positions and UVs, of
  Create's `models/block/mechanical_pump/block.json`, and 22.6% of the opaque pixels of its texture
  match Create's `textures/block/pump.png`. Create 6.0.8 for 1.20.1 ships these files as the git
  blobs `34cb63e` and `8f3d8f7`, and the Modrinth jar `create-1.20.1-6.0.3.jar` (published
  2025-03-14, SHA-512 `645e99ae...` as the Modrinth API lists it) already holds both blobs byte for
  byte. Both blobs therefore date from the time when Create's assets were MIT, and the NOTICE
  lists them with the other parts derived from Create.
- Upstream jars carry no LICENSE file. The SkyTekx build adds `LICENSE` and the SkyTekx `NOTICE`
  (what changed, the upstream commits, where the source is, Create's MIT notice for the parts
  derived from Create) to the jar; see build changes 7 and 8.

## Build changes

The snapshot builds unchanged on Java 17 with its own toolchain: ForgeGradle 6.0.6, Forge 47.4.0
userdev, Parchment 2023.06.26-1.20.1, Gradle 8.1.1, Create 6.0.8-289 (slim), Ponder 1.0.91, Flywheel
1.0.5, Registrate MC1.20-1.3.3, JEI 15.20.0.106 and CC:Tweaked (CurseMaven file 4740605). All
repositories in `build.gradle` still resolve. The compiler settings are upstream's (javac defaults,
no `-Werror`; the 36 compiler warnings of the base are upstream's). The changes below only make the
build reproducible and mark the jar as a modified build.

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew compileJava -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

The jar: `./gradlew build` (same flags) writes
`build/libs/createaddition-<minecraft_version>-<mod_version>.jar`, today
`createaddition-1.20.1-1.7.3-skytekx.1.jar`, which `reobfJar` rewrites in place. That slim reobf jar
is the jar to ship, as upstream ships the plain jar. The `-all.jar` that `jarJar` makes next to it
(with MixinExtras 0.4.1 nested) is not shipped: upstream's release jar does not nest it either, and
in the pack Create 6.0.8 already nests mixinextras-forge 0.4.1 while Forge resolves the newer 0.5.5
that another mod brings. The fire and fluid mixins of the ignitable fluids (source change 13) use
MixinExtras, and `@WrapMethod` needs 0.4.0 or later. The slim jar can rely on Create for it, because
`mods.toml` makes Create a mandatory dependency with the range `[6.0.3,)`. Every Create jar in that
range nests it: the Modrinth jars `create-1.20.1-6.0.3.jar` to `create-1.20.1-6.0.8.jar` each list
`io.github.llamalad7:mixinextras-forge` 0.4.1 with the range `[0.4.1,)` in
`META-INF/jarjar/metadata.json` (checked on 2026-11-05 against the files and SHA-512 hashes of the
Modrinth API).

1. Gradle wrapper: `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` are the copies
   that every other port in this repository uses (wrapper jar SHA-256 `33ad4583...`, the official
   Gradle 7.1 to 7.3.3 wrapper; upstream's `c5a643cf...` is the official 7.6/7.6.1 wrapper, both
   checked against Gradle's published wrapper checksums), so that only one wrapper binary runs in
   this repository. `gradle-wrapper.properties` keeps upstream's Gradle 8.1.1
   (`gradle-8.1.1-bin.zip`), because ForgeGradle 6.0.6 fails to configure on Gradle 8.2.1
   (`FlatDirRepositoryDescriptor.<init>` no longer exists), and adds `distributionSha256Sum` with
   the official checksum of that zip.
2. `gradle.properties`: `mixingradle_version` `0.7-SNAPSHOT` -> `0.7.38`. The snapshot on
   repo.spongepowered.org is the same Jenkins build (b38, git `f800b26d`) as the 0.7.38 release,
   but a snapshot can change under us.
3. `settings.gradle`: `rootProject.name = 'createaddition'`, so the project name (and with it the
   manifest's `Implementation-Title`) does not depend on the name of the checkout directory.
4. Version: `mod_version = 1.7.3-skytekx.1`, and `build.gradle` keeps upstream's
   `version = "${minecraft_version}-${mod_version}"`. The jar is
   `createaddition-1.20.1-1.7.3-skytekx.1.jar`, and Forge shows the version
   `1.20.1-1.7.3-skytekx.1` (`mods.toml` uses `${file.jarVersion}`, the manifest's
   `Implementation-Version`). The part 1.7.3 names the 1.21.1 line being backported, and
   `-skytekx.N` marks the unofficial build. This is upstream's own form (its 1.20.1 jars report
   `1.20.1-1.3.3`) plus the suffix, the same scheme as the pack's other forks
   (`twilightforest-1.20.1-4.3.2508-skytekx.1-universal.jar`). In Maven version order, which Forge
   uses for dependency ranges, `1.20.1-1.7.3-skytekx.1` sorts above `1.20.1-1.3.3`, so a dependent
   range in upstream's form such as `[1.20.1-1.2.0,)` accepts it. It also sorts above the range
   `[1.20.1-1.0.0b,1.20.1-1.2.3]` that gates the AllTheLeaks fix `UntrackedIssue001` for
   `EnergyNetworkManager#instances`, which therefore stays off, as it does for 1.3.3. An earlier
   commit of this fork used `1.7.3-1.20.1-skytekx.1` with `version = mod_version`, which sorts
   below `1.20.1-1.3.3` (7 is less than 20). No jar with that version was shipped.
5. Reproducible jar: the `jar` task sets `preserveFileTimestamps = false` and
   `reproducibleFileOrder = true`, and the manifest no longer has `Implementation-Timestamp`. Two
   clean builds of the same commit give the same SHA-1.
6. `.gitignore`: also ignores `run-data/`, `classes/`, `mcmodsrepo/`, `.mixin.out/` and logs.
7. Licence files in the jar: the `jar` task adds `LICENSE` (upstream's, unchanged) and `NOTICE`
   (SkyTekx's, in this directory) to the jar root as `LICENSE_createaddition` and
   `NOTICE_createaddition`. MIT asks for the licence text in every copy, and upstream jars carry
   none. The manifest's `Implementation-Vendor` reads `createaddition (modified build: SkyTekx)`.
8. `mods.toml`: `license` keeps `MIT License, Copyright 2022 MRH0` and points to the two files;
   `issueTrackerURL` goes to https://github.com/skytekx/skytekx3-1201-server/issues, since bugs
   of this build are ours; `credits` adds "Modified, unofficial 1.20.1 backport by SkyTekx, not
   supported by MRH0". `authors`, `displayURL`, `displayName` and `description` are unchanged.

## Source changes

These commits change the mod itself on top of the base. Each later commit that ports a feature adds
its item here, and the NOTICE in the jar sums them up.

1. Config: `config/Config.java` defines the keys of the 1.21.1 `CommonConfig` at `658a390` with
   upstream's names, sections, comments, defaults and ranges. They are
   `wires.connector_network_internal_buffer`, the new sections `liquid_blaze_burner` and
   `electric_pump`, and `amulet_effect_enabled`, `electrum_amulet_charge_rate`,
   `capacitor_capacity` and `capacitor_charge_rate` in `misc`, 11 keys in all. The one divergence
   is `liquid_blaze_burner_max_liquid_capacity`, whose default stays 4000 mB, the tank size of the
   live burners, instead of the 1000 mB of upstream `6cbf853`. Loading an older
   `createaddition-common.toml` keeps its values and appends the new keys. The Liquid Blaze Burner
   reads its two keys since item 4, the Capacitor its two since item 22 and the Electrum Amulet
   `amulet_effect_enabled` and `electrum_amulet_charge_rate` since item 23. The other keys wait for
   their bundles, and every reader calls `get()` inside a method or supplier, never in a static
   initialiser.
2. Network: the channel `createaddition:main` goes from protocol `1` to `2`, and its message ids
   are the constants `PACKET_OBSERVE` (0), `PACKET_ENERGY_NETWORK` (1) and
   `PACKET_TIME_REMAINING` (2) in `CreateAddition.java`. Id 2 carries the `TimeRemainingPacket`
   of the goggle timers (the 1.20.1 form of `54cf0c9`, item 5). The channel accepts
   only its own protocol, so Forge 47 ends a login between this build and an upstream 1.20.1 build
   such as 1.3.3 with `Connection closed - mismatched mod channel list`.
3. The Modular Accumulator capability stops when the controller is the part itself and after four
   hops, so a broken controller link or a cycle of two controllers no longer ends in a
   `StackOverflowError` (upstream `f6dd767`, the hop limit is local). Goggles observe the
   controller, which syncs at once after a multiblock update (`4ae8f91`). The safe NBT of a
   controller carries `Size` and `Height` for schematics, and the block item does not auto join
   while a Symmetry Wand is in the hotbar (`9dd1b46`). No NBT key changes.
4. The Liquid Blaze Burner takes its tank and heat capacity from
   `liquid_blaze_burner_max_liquid_capacity` and `liquid_blaze_burner_max_heat_capacity` through
   the static methods `getMaxLiquidCapacity` and `getMaxHeatCapacity`, which replace the constant
   `MAX_HEAT_CAPACITY` (`5935afa`, read when needed instead of in a static field initialiser). A
   switch between normal and superheated fuel starts the time of the new fuel (`0a92b74`), and the
   renderer fetches each buffer right before its use (`9adb5b0`). The 1000 mB recipe probe of
   `de9d7e7` is not ported, because the fluid ingredient of Create 6.0.8 for 1.20.1 ignores the
   amount (`FluidIngredient.testInternal` compares fluids and tags only).
5. The new `TimeRemainingPacket` (id 2, server to client only, one VarInt) answers the
   `ObservePacket` of a Liquid Blaze Burner or a Tesla Coil, and the goggles show the burn or
   charge time left (`54cf0c9`). The client handler only stores the value, so no client class is
   reachable from the server. The coil averages its last 20 charging ticks, sends -1 while it gets
   no energy, which the goggles show as an infinity sign, and shows its powered state only while a
   recipe draws energy (`7cb0ef9`). The short time format moves to `Util.formatTime`, which the
   JEI Liquid Burning category also uses, and en_us gains the keys `tooltip.liquid_burning.*`,
   `tooltip.charging.*` and `tooltip.alternator.info`.
6. The soundscape tick negates Create's client option `enableAmbientSounds` (`a68965a`). The base
   read it without the negation and cut every CC&A soundscape while the ambient sounds were on.
   The tick also returns while the client has no level (`f83f9fd`).
7. The Electric Motor, Alternator and Rolling Mill register their generator speed, capacity and
   impact in Create's `BlockStressValues` for the item and goggle tooltips, with suppliers that
   read the config when Create asks (`f3091d0`). The motor names itself as its stress config key
   instead of the Water Wheel, and the alternator's goggles start with Create's kinetic stats. The
   block entities keep their own stress methods, so the stress in a network does not change, and
   the alternator keeps its `MEDIUM` speed requirement, which `f3091d0` drops.
8. All 21 locales name the death messages `death.attack.createaddition.barbed_wire` and
   `death.attack.createaddition.tesla_coil`, each with `.player`, as the message ids of the two
   damage types ask. The base used `attack.death.createaddition.*` or keys without the namespace.
   Upstream `ced6d21` fixed en_us only.
9. The Electric Motor peripheral throws a `LuaException` for a non finite speed, angle or
   distance, and the motor ignores a non finite rpm (`9dd1b46`). Unlike upstream, `setSpeed`
   clamps the speed to `electric_motor_rpm_range` before the cast to float, so a finite value
   beyond the float range still means full speed. The Digital Adapter peripheral
   returns floats instead of ints for stress, capacity, speed, pulley and piston distance and the
   bearing angle (`393c51f`), which Lua scripts see.
10. Barbed wire is waterloggable (`7a66e82`) and wrenchable (`a44784e`). The new block state
    property `waterlogged` defaults to false, so saved wire loads unchanged, and the blockstate
    file covers it because its variant keys leave the property out. Unlike upstream it also
    schedules a water tick in `updateShape`.
11. The girder connector's particle texture is `create:block/industrial_iron_block` instead of
    the `chute_block` that Create 6.0.8 lacks (`043b22e`). The Rolling Mill also rolls paper into
    straw (`recipes/rolling/straw_from_paper.json`, `ced6d21`, filed under createaddition instead
    of upstream's create namespace). The bioethanol and seed oil buckets are in `forge:buckets`
    (`7a66e82`), and the spool recipe makes 24 spools from two iron plates and an iron rod
    (`7cb0ef9`, `9dd1b46`).
12. The Electrum Block gets the loot table `blocks/electrum_block.json` and joins
    `minecraft:mineable/pickaxe`, `minecraft:needs_iron_tool`, `minecraft:beacon_base_blocks`,
    `forge:storage_blocks/electrum` and `forge:storage_blocks` (`a7382cc`, `5c44881`). From the
    same datagen the Rolling Mill joins `minecraft:mineable/axe`, the Biomass Pellet Block
    `minecraft:mineable/hoe`, and the chocolate and honey cakes `create:brittle` and
    `farmersdelight:mineable/knife`. All of them are hand written, since the base commits no
    datagen output.
13. Ignitable fluids: upstream `0d13505`, `18388a0`, `6af9b4f`, `f224bb4`, `eb5eff3` and `afcd352`.
    Fluids in the fluid tag `forge:ignites` catch fire like a flammable block. Four new mixins in
    `mixin/` carry it: `BaseFireBlockMixin` lets fire be placed into such a fluid, `FireBlockMixin`
    makes `canCatchFire` accept it, raises the ignite odds next to it to at least 75 and its
    flammability in `tryCatchFire` to at least 250 and keeps a fire burning next to it,
    `FlowingFluidMixin` stops it from flowing sideways into fire and, when it falls into
    a fire while `doFireTick` is on, sets fire to the free blocks beside that spot, and
    `FireBlockInvoker` gives that code `FireBlock.getStateForPlacement(BlockGetter, BlockPos)`. The
    tag is `data/forge/tags/fluids/ignites.json`, hand written since the fork has no datagen. It
    names `#forge:biofuel`, `creosote`, `crude_oil`, `plantoil`, `ethanol`, `gasoline`, `diesel` and
    `biodiesel` as optional tags where upstream names the same `c:` tags. `index/CATags.java` holds
    the `TagKey`, which upstream keeps in its datagen class `CATagRegister`, and one local change:
    every tag check of the mixins goes through `CATags.Fluids.ignites`, which also counts a flowing
    fluid whose source fluid is in the tag. Upstream checks the fluid alone, but Immersive
    Engineering 10.2.0 lists only `immersiveengineering:creosote` in `forge:creosote` (and only the
    source fluids in `forge:plantoil`, `forge:ethanol` and `forge:biodiesel`), so flowing creosote
    would flow into fire and falling creosote would not spread it. The lang key is
    `tag.fluid.forge.ignites` in `en_us` and `ko_kr`, as upstream's `tag.fluid.c.ignites`. NeoForge's
    `checkBurnOut` is Forge 47's `tryCatchFire` with the same descriptor, and the other targets keep
    their names. `createaddition.mixins.json` gains `"refmap": "createaddition.refmap.json"`, which
    `build.gradle` already had the annotation processor write (it was empty, since the three Create
    mixins use `remap = false`). The refmap maps the vanilla targets to their SRG names
    (`canBePlacedAt` `m_49255_`, `isAir` `m_60795_`, `tick` `m_213897_`, `getIgniteOdds` `m_221156_`,
    `getStateForPlacement` `m_53470_`, `canSpreadTo` `m_75977_`, `spreadTo` `m_6364_`,
    `LevelAccessor.setBlock` `m_7731_`), the names that javap shows in the Forge 47.4.18 server jars
    (`forge-1.20.1-47.4.18-server.jar` and `server-1.20.1-20230612.114412-srg.jar`). The Forge
    methods have no SRG name: the two injectors into `canCatchFire` and `tryCatchFire` set
    `remap = false`, because the annotation processor fails the build on an unmapped target method,
    and the `isFireSource` call in `tick` stays an unmapped `@At` target, which the processor only
    warns about. Seed oil and bioethanol keep their `minecraft:water` tag, which upstream `ced6d21`
    drops. Upstream's rain values, ignite odds of 100 and a flammability of 300, are kept as code
    but never apply at a fluid block. Both branches ask `Level.isRainingAt` at the fluid's own
    position, and the `MOTION_BLOCKING` heightmap counts every block with a fluid state, so its top
    lies one block above the fluid and `isRainingAt` returns false there. Fire beside such a fluid
    survives rain through `keepFire`, which makes the fluid count as a fire source in
    `FireBlock.tick`, so `tick` neither puts the fire out in rain nor lets it burn out.
14. Wire graph: the connector rework of upstream `126ed38` with the buffer config of `393c51f` and the
    `EnergyNetwork` tidy ups of `9dd1b46`. The package `energy/network` gains `WireGraph` (the saved
    data `createaddition_wires` of each dimension), `WireGraphVerifier`, `WireVertex`, `WireSlot`,
    `PortKey` and `WireNodeKind`. `EnergyNetwork` loses its world walk and reads
    `wires.connector_network_internal_buffer`. `IWireNode.getWireNode` never loads a chunk, and the
    connector and relay block entities report loads, unloads, wire changes and removals to the graph,
    which also ticks the relays. `EnergyNetworkManager` stays as an empty deprecated class with its
    field `instances`, which AllTheLeaks 1.1.1 links. The translation to 1.20.1 uses
    `DimensionDataStorage.computeIfAbsent(load, create, name)`, `SavedData.save(CompoundTag)`,
    `TickEvent.LevelTickEvent` in phase `START` and the Forge 47 `ChunkEvent.Load` of a `LevelChunk`.
    No Sable code is ported. Two local changes in the verifier. It accepts a saved chunk as an answer
    only with the status `minecraft:full` and a `block_entities` list, and a lookup that throws leaves
    the position unresolved, so a misread tag can never mark a real wire end missing. It also drops
    the answer of a chunk read that does not arrive on the server thread. Once the server is stopped,
    `MinecraftServer.execute` runs a task inline on the calling thread (`scheduleExecutables` is false
    after `isStopped`), so a read that completes during `/stop` would change the graph on the IO worker
    while the server thread saves it. The next boot checks such a position again.
15. Command: `commands/WireGraphCommand.java` of `126ed38` adds `/cca_wires stats`, `verify` and
    `rebuild` at permission level 2, registered in `CreateAddition.onRegisterCommandEvent` next to
    `CCApiCommand`. Each action works on the graph of the dimension the command runs in.
16. Wire rendering: the `WireNodeRenderer` part of `126ed38`. The renderer draws off screen, measures
    the view distance to the nearest part of the wires and draws a wire whose far end the client has
    not loaded to the centre of that block instead of skipping the rest. On Forge 1.20.1 the block
    entity owns the render box, and Create 6.0.8 caches it in `CachedRenderBBBlockEntity`, so the
    connector and relay block entities override `createRenderBoundingBox()` with the box of every wire
    end (`IWireNode.wireRenderBounds`) and call `invalidateRenderBoundingBox()` whenever their nodes
    change. For the wire the local player is placing, `ClientEventHandler` stores the start node and
    the player's box in the common class `energy/HeldWireBounds`, and the client only override of
    `getRenderBoundingBox()` grows the cached box by it. The block entities never touch `LocalPlayer`
    or `Minecraft`. The Sable branch of the renderer is not ported.
17. Shears: `3abfa39` lets shears cut every wire of a connector, with the wires dropped unless the
    player is in creative mode and the sheep shear sound. The handler is NeoForge's `useItemOn` upstream
    and `Block.use` in `AbstractConnectorBlock` here. A loaded far end lets go at once, an unloaded one
    through the wire graph when it loads. One local check: the far end only drops its wire if that
    wire still leads back to the sheared connector.
18. Festive wire: `e1cd591` treats festive light wire like copper wire, so it can no longer join two
    large connectors (`WireConnectResult.REQUIRES_HIGH_CURRENT` in `IWireNode.connect`). The check
    only runs when a wire is placed, so existing festive wires between large connectors stay.
19. Connector capability retry: the 1.20.1 form of `d239617` ("Connector cache logic"). Upstream moved
    the connector's neighbour lookup to NeoForge's `BlockCapabilityCache`, which has no Forge 1.20.1
    counterpart. The fork keeps its `LazyOptional` with the invalidation listener, and
    `AbstractConnectorBlockEntity.lazyTick` looks the neighbour up again every 10 ticks while a
    connector in push or pull mode has no external storage and the neighbour's chunk is loaded. Before,
    a connector whose first lookup found no storage stayed without it until a block update. On Forge
    1.20.1 the neighbour of a ticking connector is always in a chunk at full level, and `getBlockEntity`
    waits for it, so in practice this is a neighbour whose block entity or capability is not ready at
    the first lookup (found by the migration test).
20. Graph check at load (local change, found by the migration test): `WireGraph.load` also queues every
    saved node for a check against the world (`verifyAll`), not only the far ends the graph has never
    seen. Upstream trusts its save until a node's chunk loads. On a test world that went back to 1.3.3
    for one boot, where a connector in an unloaded chunk was broken, the upstream logic kept carrying
    energy over the missing connector until `/cca_wires rebuild`. With the check the graph matched the
    world right after the boot. The checks read saved chunks in the background and never load a
    chunk.
21. Alternator with two shafts: upstream `7cb0ef9`, with the recipe of `5c2b9ea`.
    `AlternatorBlock.hasShaftTowards` accepts both faces of the facing axis, so rotation passes
    through the alternator, and the housing of the shape spans 2 to 14 along the axis instead of 0
    to 14. `AlternatorRenderer` draws a half shaft on each face while Flywheel does not visualize
    the level, and the new `AlternatorVisual` with two `RotatingInstance`s of `SHAFT_HALF` replaces
    `OrientedRotatingVisual` in `CABlockEntities`. The models `block/alternator/block.json` and
    `item.json` are upstream's two sided ones, `block_half_shaft.json` and `item_half_shaft.json`
    keep the one sided shape, and the unused `block_old.json` and `item_old.json` are gone. The
    models use CC&A's own textures and refer to `create:block/axis` and `create:block/axis_top`,
    and no texture changes. The mechanical crafting recipe takes a second row of andesite alloy
    instead of the Capacitor. Block state, NBT and the speed requirement `MEDIUM` stay. A placed
    alternator whose back face touches a shaft of another kinetic network joins that network on
    the first server tick after its chunk loads, with no player action. Create 6.0.8 sets
    `updateSpeed` in the `KineticBlockEntity` constructor, and the first `tick` runs
    `attachKinetics`, `RotationPropagator.handleAdded` and `propagateNewSource`. That breaks a
    block with drops when the two networks turn in opposite directions or too fast, and lets the
    faster network overpower the slower one otherwise. On a test world saved by 1.3.3 the first
    fork boot dropped an alternator that had a counter rotating shaft behind it and raised one
    with a faster shaft behind it from 16 to 32 RPM. The read-only `tools/alternator_scan.py` of
    the pack repository lists such alternators in a world before the upgrade.
22. Capacitor as an FE item: upstream `5c2b9ea` and `51e4a38`. The new `item/CapacitorItem`
    stores up to `capacitor_capacity` FE and moves at most `capacitor_charge_rate` FE per call. It
    shows a light blue bar (`0x00BFFF`) and the stored energy in its tooltip through
    `Util.getTextComponent`. The energy lives in the stack NBT key `energy`, where upstream uses
    the key `energy` of the `CUSTOM_DATA` component. At 0 FE the key goes, and the whole tag goes
    when nothing else is in it, so an empty capacitor stacks with the capacitors of 1.3.3 (an empty
    tag does not stack with no tag). Upstream's `RegisterCapabilitiesEvent.registerItem` becomes
    `Item.initCapabilities` with a plain `ICapabilityProvider` for `ForgeCapabilities.ENERGY`. It is
    not serializable, so no `ForgeCaps` tag is written. Two local changes: an empty capacitor still
    stacks to 64 instead of upstream's 16, which would oversize the stacks in the world, and a
    charged one stacks to 1 (`getMaxStackSize(ItemStack)` of `IForgeItem`). The storage only
    charges and discharges a stack of exactly one capacitor and reports `canReceive` and
    `canExtract` as false otherwise. Upstream charges a whole stack as one item, which then splits
    into several charged capacitors and dupes energy, on a Tesla Coil depot, in another mod's
    charger or by the Electrum Amulet. The bar width is capped at full when the capacity in the
    config shrinks below the stored energy.
23. Electrum Amulet: upstream `5c2b9ea`, `b0b8bc9` and `4414193`. Held in the main or the off hand,
    the amulet charges the FE item in the other hand with a chance of 1 in 300 per tick by 300 times
    `electrum_amulet_charge_rate` FE, gives the player Shocking for 40 ticks and plays `little_zap`
    while `audio_enabled` is on. `amulet_effect_enabled` turns it off. This replaces the Luck and
    Speed that the base gave below four hearts. The class keeps its 1.20.1 name `ElectrumAmulet`
    (upstream `ElectrumAmuletItem`) and the rarity `UNCOMMON`, and it asks the stack for
    `ForgeCapabilities.ENERGY` where upstream asks for `Capabilities.EnergyStorage.ITEM`. The charge
    is computed in long and capped at `Integer.MAX_VALUE`, so a large rate in the config cannot
    overflow. `textures/item/electrum_amulet.png` is upstream's new 16x16 texture (blob `263f385`),
    an edit of CC&A's own amulet texture. The license check `texcmp.py` finds it equal to the old
    texture in 90 percent of the pixels that either of them covers, and finds no match among the
    textures of Create 6.0.8. The recipe takes `forge:dusts/diamond` (CC&A's Diamond Grit) and a
    `forge:plates/zinc` instead of the emerald, in the pattern ` WW`, `EZW`, `GE `. The en_us
    tooltip summary is upstream's new one. The other 20 locales keep the summary of the old effect
    until the translations bundle.
24. Tesla Coil light: upstream `5490949`. The block properties give light level 10 while the
    block state `powered` is true. The state and its model are in the base. Light is part of the
    saved chunk, so a coil saved as powered keeps its old light until it toggles. The Sable parts
    of `5490949` stay out (inventory, excluded `sable-aeronautics-compat`).
25. Charging in Sequenced Assembly: upstream `b070742` (release 1.7.2). The charging recipe is now a
    Create processing recipe that implements `IAssemblyRecipe`, as upstream `715895e` made it,
    because Create 6.0.8 takes only such recipes as steps (`SequencedRecipe.fromJson`) and casts
    their serializer to `ProcessingRecipeSerializer` for the recipe sync. The serializer
    `ChargingRecipeSerializer` reads the JSON form of the 1.20.1 line (`input`, `result`,
    `maxChargeRate`) as well as Create's form (`ingredients`, `results`, `max_charge_rate`), in any
    mix, as the rolling serializer of the base already does for KubeJS. The serializer id and the
    recipe type stay `createaddition:charging`, `energy` stays required, and a recipe without a max
    charge rate is still limited only by the coil's own rate (upstream makes the key required). The
    Tesla Coil asks `SequencedAssemblyRecipe.getRecipe` for a charging step before the plain
    charging recipes, compares the cached stack by item and tags, since every step carries the same
    transitional item, looks the step up again when the charge is complete and puts out
    `rollResults()`. A Sequenced Assembly tooltip names a step of 10000 FE "Charge 10.0K⚡ with Tesla
    Coil" (the new en_us key `createaddition.recipe.charging.sequence`), and JEI draws it as a
    powered coil (`ChargingAssemblySubCategory`). The recipe hands that class to Create only through
    a lazy supplier, and `getDescriptionForAssembly` is client only, as in Create, so the dedicated
    server never loads them. The recipe sync now sends Create's processing format with the energy
    and the rate after it. Protocol 2 of item 2 already keeps 1.3.3 clients off, and no fork jar
    with the old format was shipped. The public fields `ingredient`, `output` and `id` of the old
    class give way to `getIngredient()`, `getResultItem()` and Create's `getId()`, and `find` gains
    an overload that takes the stack. No pack jar links any of them.
26. Gold to electrum: six charging recipes turn gold into electrum on the Tesla Coil at a max
    charge rate of 360 (upstream `d2234ce`), an ingot for 36000 FE, a nugget for 4000, a block for
    324000, a rod or a wire for 18000 and a sheet for 36000
    (`recipes/charging/electrify_gold_*.json`). They take the tags `forge:ingots/gold`,
    `forge:nuggets/gold`, `forge:storage_blocks/gold`, `forge:rods/gold`, `forge:plates/gold` and
    `forge:wires/gold` where upstream names the items, so that the gold plate of another mod
    matches too, and use the JSON form of the 1.20.1 line. The same upstream commit lowers
    `channeling.json` to 1000000 FE at 360, which is not ported. The base keeps its own 10000000
    FE at 1000 from `f18dd66`.
27. Copper tiles: nine charging recipes deoxidize Create's copper tiles, tile slabs and tile stairs
    one step, from oxidized to weathered, weathered to exposed and exposed to plain, for 4000 FE
    at a max charge rate of 200, like the shingle recipes of the base. Upstream `c08b593` added
    them by hand under the same names, and `0ca5477` generates them as `deoxidize_create_*`. Each
    file has the base's format and is named after its output, as `copper_tiles.json`,
    `exposed_copper_tiles.json` and `weathered_copper_tiles.json` and the same for
    `copper_tile_slab` and `copper_tile_stairs`. The 1.21 copper blocks that 1.20.1 lacks
    (chiseled copper, bulbs, doors, grates and trapdoors) stay out, and so does the special
    recipe type `createaddition:deoxidizing` of `1f606d3`, which upstream registers no more at
    `658a390`.
28. Electric Pump: upstream `6cbf853`, `9dd1b46` and the block entity part of `562d383` (release
    1.7.1). The new block, item and block entity type are `createaddition:electric_pump`.
    The block `blocks/electric_pump/ElectricPumpBlock` extends Create's `PumpBlock`, so pipes treat
    it as a pump (`PumpBlock.isPump` is an instanceof check). It adds the state `powered`, which a
    redstone signal sets and which stops the pump, and a shape with a centre column. It has no shaft
    or cog, since `hasShaftTowards`, `isSmallCog`, `isLargeCog` and `isDedicatedCogWheel` return
    false, so no shaft or cogwheel next to it connects. Its block entity extends `PumpBlockEntity`
    and sets its own speed with a scroll value from 0 to `pump_rpm_range` (32 when placed). It holds
    `pump_capacity` FE, takes up to `pump_max_input` FE per tick on every side and draws
    `pump_fe_at_max_rpm` times max(speed, 32) / 256 FE per tick, rounded and at least 1, so 15 FE/t
    at 32 RPM with the default of 120. It runs while it has that much stored and no signal. It has
    no kinetic link: `attachKinetics` and `setSource` do nothing, stress and capacity are 0, and
    `getGeneratedSpeed` returns the pump speed while it runs, so that
    `KineticBlockEntity.validateKinetics` keeps the speed. The NBT keys are `Active` and `energy`,
    written by `write` and `writeSafe`. The goggles show the draw at the current speed, and the pump
    plays the dynamo soundscape while it runs. The renderer `ElectricPumpRenderer` (Flywheel off)
    and the visual `ElectricPumpVisual` pulse two copies of the partial
    `CAPartials.ELECTRIC_PUMP_PARTIAL`. The NeoForge capability registration becomes a `LazyOptional`
    that `getCapability(ForgeCapabilities.ENERGY)` returns for every side, invalidated in
    `invalidateCaps` and created anew in `reviveCaps`, so a connector drops the storage when the
    chunk unloads. The `HolderLookup.Provider` parameters of `read`, `write` and `writeSafe` go away.
    The blockstate with its 6 facing variants, `models/item/electric_pump.json`, the three models in
    `models/block/electric_pump/` and the 32x32 texture `textures/block/electric_pump/block.png` are
    upstream's files byte for byte, the first two from its generated resources. The mechanical
    crafting recipe (dried kelp, a mechanical pump, two copper spools and a capacitor) and the loot
    table are upstream's generated files in the folders `recipes/` and `loot_tables/`, the recipe
    with `acceptMirrored` and `item` in place of `accept_mirrored` and `id`. The pump joins
    `minecraft:mineable/pickaxe`, and en_us names it. Two local changes: the goggles write fe/t like
    those of the other CC&A blocks of this fork, where upstream writes a lightning sign, and a placed
    pump takes `powered` from its neighbours at once, where upstream waits for the next neighbour
    update. The ponder scene `electric_pump_flow` of `562d383` belongs to the ponders bundle.
    One local fix needs a fourth mixin into Create. A change to a pipe makes Create's
    `FluidPropagator.propagateChangedPipe` wipe the pressure of the whole pipe network and then ask
    the pumps it meets to spread theirs again, but in Create 6.0.8 for 1.20.1 it only asks a block
    entity whose block is `AllBlocks.MECHANICAL_PUMP` (javap of the method). A test server showed
    the Electric Pump keep its own pressure while its pipes had none and no fluid moved, after a
    flip of the pump and after the pipe at its face was replaced, until its speed changed. The
    mixin `mixin/FluidPropagatorMixin` wraps that `BlockEntry.has` call with `@WrapOperation` and
    also accepts the Electric Pump. It sets `remap = false` like the other Create mixins, and
    Arclight does not patch Create.
    Two traits of upstream stay as they are (review of the pump bundle). The pump has no hysteresis,
    so one that gets a little less FE than it draws switches on and off every one to three ticks,
    and each switch runs `PumpBlockEntity.updatePressureChange` with two `propagateChangedPipe`
    searches of the pipe network and sends the block entity to the clients. Schematics keep the
    stored FE, because Create saves `writeSafe` into them, and a printed pump can only spend it on
    itself. The wrap of the mixin sets no `require`, like the other mixins of the fork, so it would
    stop applying without an error if a Create update dropped the call. Every Create update
    therefore checks the wrapper in the exported `FluidPropagator`.
29. Servo Motor: upstream `a8a51a2`, `84c7b2c`, `89f01b9`, `9dd1b46` and `a1b6da6` (release 1.7.2).
    The new block, item and block entity type `createaddition:servo_motor` is a bearing that turns a
    `BearingContraption` to a target angle. Redstone on its top side drives it towards the Max Angle
    and on the opposite side towards the Min Angle, and the two signals are summed. A right click
    with an empty hand assembles or disassembles it. The wrench rolls it around its facing (block
    state `roll`, 0 to 3), and `rotate`, `mirror` and Create's `TransformableBlock.transform` turn
    its facing and top together. The angles count from the servo's front, so the contraption and the
    disc turn the other way round for north, west and down (the 1.7.2 fix in `a1b6da6`). The class
    `ServoMotorBlockEntity` extends Create's `MechanicalBearingBlockEntity`, whose protected fields
    and methods are the same in Create 6.0.8 for 1.20.1 (javap). The movement mode, the Max Angle
    and the Min Angle get their own `BehaviourType`, net id and NBT key (`RotationMode`, `MaxAngle`,
    `MinAngle`), so they coexist with the speed scroll value (`ScrollValue`). The block entity also
    saves `TargetAngle`, `servo_active` and `energy` next to the bearing's `Running` and `Angle`. It
    runs on FE with the buffer, input, speed range and consumption of the `electric_motor` config,
    draws nothing until it is assembled and a tenth of that while it holds its angle, and ignores a
    speed that is not finite (`9dd1b46`). The translation drops the `HolderLookup` of the
    behaviours' `write` and `read`, turns `useItemOn` into `use` with the results of Create's
    `MechanicalBearingBlock.use`, and makes the energy capability a `LazyOptional` that
    `invalidateCaps` invalidates. The renderer, the Flywheel visual, the blockstate with its 24
    variants and the models are upstream's files, unchanged. The model `block_rolled.json` rolls the
    block model with a root `transform`, which Forge 47 reads (`ExtendedBlockModelDeserializer`),
    and a script checked that every variant turns the model's front to `facing` and its top to
    `ServoMotorBlock.getTop`. The shapeless recipe `recipes/crafting/servo_motor.json` (Mechanical
    Bearing, Electric Motor and Electron Tube) and the loot table are hand written in the 1.20.1
    folders, the block joins `minecraft:mineable/pickaxe`, and en_us names it Servo Motor. The
    recipe advancement is left out, as for every other recipe of the fork. The texture
    `textures/block/servo_motor/block.png` (32x32) is CC&A's own. The script `texcmp.py` finds no
    texture of that size in Create 6.0.8 that shares more than 8% of its opaque pixels. The port
    makes three local changes. The target angle follows a new Min or Max Angle while the redstone
    signal stays the same. Upstream recomputes it only when the signal changes, so a new scroll
    value or a peripheral call moved nothing until the redstone changed. The block entity marks
    itself changed while the angle moves, and the setters of the two limits go through
    `ScrollValueBehaviour.setValue`, which marks it changed too. Upstream only sent the data, so a
    save kept an angle up to ten seconds old (the eager chunk save of 1.18 and later) and a limit
    set through the peripheral could be lost. On a test server stopped mid rotation, the servo came
    back at 73.8 degrees, although it had already turned down to 49.8 degrees before the save. The
    goggles show `fe/t` like the fork's Electric Motor instead of the lightning sign of the 1.21.1
    line. The ponder scene and the servo's entries in the ponder tags come with the ponder bundle.
