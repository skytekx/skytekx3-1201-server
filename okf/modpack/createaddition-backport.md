---
type: Plan
title: Create Crafts & Additions backport
description: Which features of the Create Crafts & Additions 1.21.1 line (1.7.3) go onto its 1.20.1 Forge base for the pack, in which bundles and order, which upstream work stays out, and the license, savegame and operator rules of the fork.
tags: [modpack, mods, createaddition, create, backport]
timestamp: 2026-11-05
---

# Create Crafts & Additions backport

The pack ships Create Crafts & Additions (CC&A, mod id `createaddition`) 1.3.3 from Modrinth as
`mods/createaddition-1.20.1-1.3.3.jar` (`modrinth.index.json:954`). Upstream develops the mod on its
1.21.1 NeoForge line, which reached 1.7.3 on 2026-11-02. This plan backports what works on Minecraft
1.20.1 with Forge 47 onto a vendored copy of upstream's `1.20.1` branch in `ports/createaddition/`,
the same way as the [Twilight Forest backport](twilightforest-backport.md). The fork keeps the mod id
and every registry id, NBT key, block state and config key, so the live world, the configs and the
addons keep working. The work is done for SkyTekx, a modpack by ts4.party.

Effort is given in estimated human hours, the unit of the Twilight Forest plan. The plan holds 34
features in 10 bundles, 136.5 h in all, and no bundle is over 40 h. The machine readable plan is
`ports/createaddition-backport-inventory.json`. Its `plan.bundles` holds every feature with its
upstream commits, translation notes, savegame notes and tests, `plan.excluded` the upstream work left
out, and `critic_notes` the review that corrected the first draft. The file
`ports/createaddition-backport-carryover.json` will record what a bundle hands to a later one. It does
not exist yet.

| Property | Value |
|---|---|
| Upstream | The Create Crafts & Additions repository named in `ports/createaddition/UPSTREAM.md`, MIT License |
| Base | Branch `1.20.1` at 42b6f2d (2026-11-02), `mod_version` 1.3.3 plus the community fixes merged after the 1.3.3 release |
| Port source | Branch `1.21.1` at 658a390 (2026-11-02), `mod_version` 1.7.3. Release 1.7.2 came out on 2026-10-01, and the repository has no git tags |
| Fork | Folder `ports/createaddition/`, Java 17, ForgeGradle 6.0.6, Create 6.0.8 (`UPSTREAM.md`, Build changes) |
| Target version | Version 1.20.1-1.7.3-skytekx.1 in `createaddition-1.20.1-1.7.3-skytekx.1.jar`, the slim reobf jar from `build/libs/` |
| Ships as | A jar in `local-mods/` that replaces the Modrinth entry at `modrinth.index.json:954` |
| Build check | The `compileJava` call in `UPSTREAM.md`, run with `/opt/jdk17` and `-Xmx2g` |

## Status on 2026-11-05

The vendored base (d8d06f5), the reproducible Java 17 build (28bde03), the license files in the jar
(c276dbd), the baseline bundle and the small fixes are on the branch
`claude/twilight-forest-port-mods-j2owbq`. The small fixes are the first ported features. The
pack still ships the Modrinth jar 1.3.3, and no boot has run the fork, so the boot and login checks
of the baseline and the in game tests of the small fixes wait for the [release](#release).

| Bundle | What it covers | Hours | Needs | Status |
|---|---|---|---|---|
| Baseline (`cca-baseline`) | Version fix, new config keys, packet id and protocol bump | 4.5 | Nothing | Done in d7d4fad, 35706d8, 0611ece and ecf5303 on top of d8d06f5, 28bde03 and c276dbd. The boot and login checks belong to the release |
| Connector rework (`cca-wiregraph`) | The WireGraph, the `/cca_wires` command, the live world migration test, wire render culling, shears, festive wire, the connector capability retry | 34.5 | Baseline | Planned |
| Small fixes (`cca-phase1-fixes`) | Accumulator, Liquid Blaze Burner, goggle timers, sounds, stress tooltips, death messages, CC:Tweaked hardening, barbed wire, data fixes, Electrum Block | 21.5 | Baseline | Done in 010ffd5, 1ad4bf1, 8fb90f8, 0bb10b7, f855f94, 66076fb, 2f39e85, fa15e47, 95a599b, 47f954c and d4ee92e, with the review fix 8224da6. The in game and boot tests belong to the release |
| Changes of 1.6.0 (`cca-160-rework`) | Alternator with two shafts, Capacitor as an FE item, Electrum Amulet, Tesla Coil light | 12.5 | Baseline, small fixes | Planned |
| Ignitable fluids (`cca-ignitable-fluids`) | Fire over fluids in the `forge:ignites` tag, four vanilla mixins and the refmap | 6.5 | Baseline | Planned |
| Charging recipes (`cca-charging-sequenced-assembly`) | Charging steps in Sequenced Assembly, gold to electrum, Create copper tiles | 10.5 | Baseline, small fixes | Planned |
| Electric Pump (`cca-electric-pump`) | The new pump block with its renderer, visual and recipe | 11 | Baseline, small fixes | Planned |
| Servo Motor (`cca-servo-motor`) | The Electric Servo Motor and its CC:Tweaked peripheral | 18 | Baseline, small fixes | Planned |
| Ponders and translations (`cca-ponders-and-translations`) | Three new ponder scenes with their NBT converted to 1.20.1, and 41 new lang keys | 9 | Connector rework, the 1.6.0 changes, ignitable fluids, charging, pump, servo | Planned |
| Release (`cca-pack-release`) | Pack wiring, license and model checks, compat scan, boots on Forge and Arclight, rollout, docs | 8.5 | All other bundles | Planned |

After the baseline, the connector rework, the small fixes and the ignitable fluids can run in
parallel. The 1.6.0 changes, the charging recipes, the pump and the servo wait for the small fixes,
which add the packet the goggle timers need. The pump and the servo both add entries to `CABlocks`,
`CABlockEntities`, `CAPartials`, `CAPonders`, `mineable/pickaxe.json` and `en_us.json`, so they are
integrated one after the other. The charging bundle does not wait for the 1.6.0 changes, because the
two share no file beyond what the small fixes already order (inventory, `critic_notes` item 5).

## Scope

The research for this backport split the work into fixes first and new content second. The fixes
are the WireGraph rework (energy across unloaded chunks), the Modular Accumulator
`StackOverflowError`, the Portable Energy Interface after a train reconnects, the wire spool stacking
and the Liquid Blaze Burner config. The new content is the Electric Pump, the Electric Servo Motor
with a CC:Tweaked peripheral, charging in Sequenced Assembly with JEI, the alternator, capacitor,
amulet and Tesla Coil changes of 1.6.0, ignitable fluids by tag and the new ponders. The Sable and
Aeronautics compat stays out. The research records are in the workflow scratchpad
(`bp2/research-result.json` and `bp2/discovery-result.json`), not in this repo.

Two of the named fixes need no port. The 1.20.1 base already invalidates the Portable Energy
Interface capability (`PortableEnergyInterfaceBlockEntity:33-50`) and already clears the wire spool
tag (`WireSpool.java:89`, `107`). Both only need a check in game, see [Excluded](#excluded).

### Baseline

- The version fix (d7d4fad) set `mod_version = 1.7.3-skytekx.1` (`gradle.properties:9`) and
  restored upstream's `version = "${minecraft_version}-${mod_version}"` (`build.gradle:33`). The
  branch now builds `createaddition-1.20.1-1.7.3-skytekx.1.jar`. Its manifest says
  `Implementation-Version: 1.20.1-1.7.3-skytekx.1`, the version that Forge shows through the
  `${file.jarVersion}` of `mods.toml`. Maven's `ComparableVersion` sorts it above the shipped
  `1.20.1-1.3.3` and outside the AllTheLeaks gate `[1.20.1-1.0.0b,1.20.1-1.2.3]`. The form follows
  the other forks of the pack, such as `twilightforest-1.20.1-4.3.2508-skytekx.1-universal.jar`. The
  older form is in [History](#history).
- The same commit updated `NOTICE`, item 4 of the build changes in `UPSTREAM.md` and the comment in
  `build.gradle`, and cites the base in `UPSTREAM.md:10` by its PR number only.
- Two clean builds of ecf5303 give the same slim jar with SHA-1
  `7671e5a93338228bbb7f01d55ba9fbb32508e239`. Later bundles change it.
- The shipped jar is the slim reobf jar. The `-all` jar of `jarJar.enable()` (`build.gradle:22`) only
  adds a nested MixinExtras. Create 6.0.8 nests mixinextras-forge 0.4.1, and the pack resolves 0.5.5
  (debug log of the Forge pack boot on 2026-11-04 in the workflow scratchpad, line 1115, "Selected
  file mixinextras-forge-0.5.5.jar").
- All 11 new config keys landed in one commit (35706d8) in `config/Config.java`, the spec of
  `createaddition-common.toml`. They are `wires.connector_network_internal_buffer` (80000, 393c51f),
  a section `liquid_blaze_burner` with a tank of 4000 mB and a heat capacity of 10000 (5935afa), a
  section `electric_pump` with `pump_rpm_range` 256, `pump_fe_at_max_rpm` 120, `pump_max_input` 5000
  and `pump_capacity` 5000 (6cbf853, 9dd1b46), and the amulet and capacitor keys (5c2b9ea, b0b8bc9,
  4414193). The servo reuses the `electric_motor` keys. The baseline itself read none of them. The
  Liquid Blaze Burner reads its two keys since 1ad4bf1, and each later bundle wires its own keys.
  Loading the tracked `config/createaddition-common.toml` through
  `Config.loadConfig` outside a boot kept its 41 values and appended the 11 keys.
- Every config value is read lazily inside a method or supplier. Forge 47 throws an
  `IllegalStateException` when a value is read before the common config loads, and upstream 5935afa
  does exactly that in a static field initialiser.
- The network channel `createaddition:main` registered `ObservePacket` as id 0 and
  `EnergyNetworkPacket` as id 1 under protocol "1". Commit 0611ece raised the protocol to "2"
  (`CreateAddition.java:72`) and named the ids `PACKET_OBSERVE`, `PACKET_ENERGY_NETWORK` and
  `PACKET_TIME_REMAINING` (`CreateAddition.java:83-85`), so an old client gets a clean refusal at
  login instead of a desync. The baseline only reserved id 2. The `TimeRemainingPacket` of 54cf0c9
  took it with the goggle timers of the [small fixes](#small-fixes) (8fb90f8), registered for the
  direction to the client only (`CreateAddition.java:144`). The protocol stays at "2".
- The `NOTICE` in the jar and the section Source changes of `UPSTREAM.md` list the config keys and
  the protocol bump (ecf5303).

### Connector rework

- The WireGraph of 126ed38 (1275 lines added, 181 removed) keeps a graph of every connector and relay
  per dimension in the saved data `createaddition_wires`. Energy and relays work across unloaded chunks,
  and no wire lookup loads a chunk any more. Today `level.getBlockEntity` on the server thread loads
  remote chunks synchronously.
- The verifier reads saved chunks of unloaded wire ends through `ChunkStorage.read`, which ends in
  `IOWorker.loadAsync`, and handles the answer on the server thread. See
  [Arclight notes](#arclight-notes) for the local hardening.
- A deprecated empty `EnergyNetworkManager` stays with its static field `instances`. AllTheLeaks 1.1.1
  links that field (`UntrackedIssue001`). The check is gated off by version, but the stub costs nothing.
- The admin command is `/cca_wires stats`, `verify` and `rebuild` at op level 2, registered next to
  the existing `CCApiCommand`. The research called it `/wiregraph`, but the code of 126ed38 names it
  `cca_wires`.
- The wire renderer of 126ed38 gets a bounding box that spans every wire end, distance culling and
  off screen rendering. Create 6.0.8 caches the render box in `CachedRenderBBBlockEntity`, so the port
  overrides `createRenderBoundingBox()` and invalidates it when the nodes change. A client side flag
  grows the box for the wire the local player is placing, and the block entity never touches
  `LocalPlayer` or `Minecraft`.
- Shears on a connector cut all its wires (3abfa39). Festive wire can no longer join two large
  connectors, like copper wire (e1cd591, `IWireNode.java:563-565`). Existing festive wires stay.
- A connector retries its neighbour's energy capability in the lazy tick while it has none, because a
  chunk load sends no neighbour update. This is the 1.20.1 form of d239617
  (`AbstractConnectorBlockEntity:375-395`).
- No Sable code is ported.

### Small fixes

- The Modular Accumulator no longer recurses into itself in `handlerForCapability` (f6dd767, the
  same code was at `ModularAccumulatorBlockEntity:347-350` in the base). A local hop limit of four
  (`MAX_CONTROLLER_HOPS`, `ModularAccumulatorBlockEntity:349-360`) also stops a cycle of two
  controllers, and a valid multiblock needs one hop. Goggles show the controller's data (4ae8f91),
  schematics keep the size through `Size` and `Height` in the safe NBT, and a Symmetry Wand in the
  hotbar stops the auto join (`ModularAccumulatorBlockItem.java:79`, 9dd1b46). Done in 010ffd5.
- The Liquid Blaze Burner reads its tank and heat capacity from
  `liquid_blaze_burner_max_liquid_capacity` and `liquid_blaze_burner_max_heat_capacity`
  (`Config.java:272`, `275`, 5935afa). The static methods `getMaxHeatCapacity` and
  `getMaxLiquidCapacity` (`LiquidBlazeBurnerBlockEntity:71-75`) replace the static final field of
  the base and read the value when it is needed. A fuel switch resets the burn time instead of
  adding to it (0a92b74). The renderer fetches each buffer right before use (9adb5b0). Done in
  1ad4bf1.
- The probe fix de9d7e7 is not ported. The fluid ingredient of Create 6.0.8 for 1.20.1 compares the
  fluid and its tag only (`FluidIngredient.testInternal`), so the base never had the bug that the
  1000 mB probe works around.
- Goggles show the burn time left on the Liquid Blaze Burner and the charge time left on the Tesla
  Coil (54cf0c9, 7cb0ef9) through the new `TimeRemainingPacket` with id 2, sent from the server to
  the client only. The client handler stores the value through `DistExecutor`, so no client class
  loads on the dedicated server. A static javap check confirmed that, but no boot has. The burner
  answers an observation with the time packet alone (`LiquidBlazeBurnerBlockEntity:485-487`),
  because `ObservePacket.sendUpdate` already sends the update packet to the observing player. The
  JEI Liquid Burning category takes the short format of `Util.formatTime` and shows 20m instead of
  20 min, as upstream does. Done in 8fb90f8.
- The soundscapes play while Create's ambient sounds are on, through the negation at
  `CASoundScapes.java:79` (a68965a) and a null level guard (f83f9fd). Done in 0bb10b7.
- Stress values register through `BlockStressValues` (`CABlocks.java:61-78`), so item and goggle
  tooltips show them (f3091d0). Network stress does not change. The alternator keeps its minimum
  speed MEDIUM, see [Excluded](#excluded). Done in f855f94, which keeps the CRLF line endings of
  upstream's `CABlocks.java` so that the pump and servo bundles merge without a whole file conflict.
- Death messages by barbed wire and the Tesla Coil show their text. All 21 locales use the keys
  `death.attack.createaddition.barbed_wire` and `death.attack.createaddition.tesla_coil` with their
  `.player` forms (`en_us.json:319-324`), which the damage types look up. The base had
  `attack.death.createaddition.*` in six locales and keys without the namespace in the other 15.
  Done in 66076fb.
- The CC:Tweaked peripheral of the Electric Motor raises a Lua error on NaN or infinity, and the
  motor ignores a speed that is not finite (9dd1b46). As a local change the peripheral clamps a
  finite speed to `electric_motor_rpm_range` before the cast to float
  (`ElectricMotorPeripheral.java:62`), so a value beyond the float range still means full speed.
  The Digital Adapter returns floats instead of integers (393c51f). Done in 2f39e85 and 8224da6.
- Barbed wire can be waterlogged (7a66e82) and wrenched (a44784e). Unlike upstream it schedules a
  water tick in `updateShape` and takes the waterlogged state also when it is placed without a
  player (`BarbedWireBlock.java:32-71`). Saved wire loads with `waterlogged=false`. Done in fa15e47.
- Four data fixes came along: the girder particle texture (043b22e), a second straw recipe from
  paper next to the bamboo one (ced6d21, filed as `createaddition:rolling/straw_from_paper`), the
  bioethanol and seed oil buckets in `forge:buckets` (7a66e82) and a spool recipe that makes 24 from
  iron plate and iron rod (7cb0ef9, 9dd1b46). Done in 95a599b.
- A mined Electrum Block drops itself and mines fast with a pickaxe. The 1.3.3 jar and the base
  ship no loot table for it and leave it out of `minecraft:mineable/pickaxe`. Upstream fixed it
  through datagen (a7382cc, 5c44881). The hand written fix also adds the block to
  `minecraft:needs_iron_tool`, `minecraft:beacon_base_blocks` and `forge:storage_blocks/electrum`,
  which the parent block tag `forge:storage_blocks` names as `#forge:storage_blocks/electrum`. It
  adds the Rolling Mill to the axe tag, the Biomass Pellet Block to the hoe tag and the two cakes to
  `create:brittle` and `farmersdelight:mineable/knife`. The biomass storage block tags of the 1.21
  datagen stay out, since the inventory does not list them. Done in 47f954c.
- Commit d4ee92e lists the ten fixes as items 3 to 12 of the section Source changes in
  `UPSTREAM.md` and sums them up in the `NOTICE`. An incremental and a clean build of 8224da6 give
  the same slim jar with SHA-1 `251b86adafcff4689eb0008ba99a1e305cb85997`, and `compileJava` ends
  with the same 36 warnings as the build of the porter.

### Changes of 1.6.0

- The alternator takes a shaft on both faces of its axis and gets a Flywheel visual with two half
  shafts (7cb0ef9, 5c2b9ea). Its mechanical crafting recipe trades the Capacitor for a second row of
  andesite alloy.
- The Capacitor becomes an FE item that stores `capacitor_capacity` and moves `capacitor_charge_rate`
  per tick (5c2b9ea, 51e4a38). The energy lives in the stack NBT key `energy`, and an empty capacitor
  carries no tag, so it stacks with old ones. Two local changes apply, see
  [Local divergences](#local-divergences).
- The Electrum Amulet, held in either hand, now and then charges the FE item in the other hand,
  shocks the player for 40 ticks and plays a zap (5c2b9ea, b0b8bc9, 4414193). This replaces the Luck
  and Speed below four hearts. The texture is a new edit of the mod's own amulet texture, and the
  recipe uses diamond grit and a zinc plate instead of the emerald.
- The Tesla Coil gives light level 10 while it is powered (5490949).

### Ignitable fluids

- Fire spreads over fluids in the fluid tag `forge:ignites`, keeps burning next to them, and falling
  fluid spreads it further when `doFireTick` is on (0d13505, 18388a0, 6af9b4f, f224bb4, eb5eff3,
  afcd352). The tag lists `#forge:biofuel`, creosote, crude oil, plant oil, ethanol, gasoline, diesel
  and biodiesel as optional entries. It is not `c:ignites`.
- Four mixins go into `BaseFireBlock`, `FireBlock` and `FlowingFluid`. They need a refmap, which the
  mod had never used: `createaddition.mixins.json` had no `refmap` key, and the built refmap was
  empty because the three Create mixins use `remap=false`. The port adds the key (56f628f). Forge
  methods without SRG names act in two ways. As the target method of an injector, as `canCatchFire`
  and `tryCatchFire` are, they fail the annotation processor, so those two injectors set
  `remap = false`. As an `@At` target, as `isFireSource` in `tick` is, they only make it warn, which
  is correct at runtime. The build is not weakened to hide either.
- Upstream's `checkBurnOut` is `tryCatchFire` on Forge 1.20.1 (javap on
  `forge-1.20.1-47.4.18-server.jar`).
- A flowing fluid counts as igniting when its source fluid is in the tag (`CATags.Fluids.ignites`).
  Immersive Engineering 10.2.0 tags only its source fluids, and without this the first Arclight test
  showed flowing creosote running into a fire and falling creosote spreading none. This is a local
  change.
- In the pack the tag catches the creosote, plant oil, ethanol and biodiesel of Immersive
  Engineering, the creosote of Railcraft, the biofuel of Industrial Foregoing, the creosote and crude
  oil of Thermal (thermal_core 11.0.6.24, nested in Thermal Foundation 11.0.6.70), the gasoline and
  diesel of Thermal Extra and the seed oil and bioethanol of CC&A. The scan read the eight tag files
  under `data/forge/tags/fluids/` in the pack jars and in the jars nested under `META-INF/jarjar/`,
  which Forge loads too. Thermal Foundation lists `thermal_core-1.20.1-11.0.6.24.jar` in its
  `META-INF/jarjar/metadata.json`, and that jar's `creosote.json` and `crude_oil.json` name
  `thermal:creosote` and `thermal:crude_oil`. Of the two only the crude oil has a fluid block
  (`assets/thermal/blockstates/crude_oil_fluid.json`, with no creosote counterpart), so it is the
  Thermal fluid that can lie in the world and burn there. Ad Astra puts its oil in `forge:oil`, which
  the tag does not name.
- Tested on Arclight with Create, Immersive Engineering, Supplementaries, Moonlight,
  NostalgicTweaks, Ad Astra and Thermal Foundation and `-Dmixin.debug.export=true` (test boots of
  2026-11-05 in the workflow scratchpad). Each of the seven injectors sits once in the exported
  classes, next to those of Arclight and the other mods. A plain Forge 47.4.18 boot with the same
  mods shows the same seven. A dispenser with flint and steel lit creosote and seed oil but not
  honey. Pools of creosote and seed oil with a fire in the middle burned down to 1 and 5 of 25
  blocks in a minute, a honey pool kept all 25. Sources beside a fire did not flow into it, honey
  did. Creosote and seed oil that fell into a fire lit the four blocks beside it. In rain the fire
  in a creosote pool lived on. With `doFireTick` off nothing burned or spread. Supplementaries' own
  lumisene fire was not tried.

### Charging recipes

- Charging becomes a step of Sequenced Assembly with a JEI sub category that draws a powered Tesla
  Coil (b070742). Create 6.0.8 only accepts `ProcessingRecipe` steps, so `ChargingRecipe` becomes a
  `ProcessingRecipe` with a serializer that still reads the old keys `input`, `result`, `energy` and
  `maxChargeRate`. The base already does the same for rolling.
- The recipe type and serializer stay `createaddition:charging`. All shipped charging recipes, 21
  copper steps, channeling and the AE2 charged certus quartz, must still parse at boot. No pack jar,
  datapack or KubeJS script ships a charging recipe (`kubejs/` has no `createaddition` reference).
- Six new recipes charge gold into electrum (d2234ce) with `forge:` tag inputs, so a gold sheet or
  wire that AlmostUnified swapped to another mod still matches. AlmostUnified turns the outputs into
  Thermal electrum where Thermal has the item (`config/almostunified/unify.json`, `modPriorities`).
- Nine new recipes deoxidize Create's copper tiles, slabs and stairs (0ca5477, 1f606d3), in the format
  of the existing files in `data/createaddition/recipes/charging/`.

### Electric Pump

- The Electric Pump (6cbf853, 9dd1b46, 562d383) is a Create pump that runs on FE at its own set speed
  of up to 256 RPM, stops on a redstone signal and has no kinetic link. Pipes treat it as a pump,
  because `PumpBlock.isPump` checks the class.
- It brings the ids `createaddition:electric_pump` for the block, the item and the block entity type,
  a renderer, a Flywheel visual, a mechanical crafting recipe and a loot table.
- Its block and item models reuse the two flange cuboids of Create's
  `create:block/mechanical_pump/block.json`, see [License conditions](#license-conditions).

### Servo Motor

- The Electric Servo Motor (a8a51a2, 84c7b2c, 89f01b9, 9dd1b46, a1b6da6) is a bearing that turns a
  contraption to a target angle between a minimum and a maximum. Redstone on the top side drives it to
  the maximum, on the opposite side to the minimum. It runs on FE from the `electric_motor` config and
  includes the 1.7.2 direction fix for north, west and down.
- It brings the ids `createaddition:servo_motor` for the block, the item and the block entity type,
  and the NBT keys `MaxAngle`, `MinAngle`, `TargetAngle`, `servo_active` and `energy`.
- Its CC:Tweaked peripheral reads and sets speed, angles and limits, assembles and disassembles, and
  raises a Lua error on input that is not finite (84c7b2c, 9dd1b46). CC:Tweaked 1.120.2 in the pack
  has the API. The fork's `COMPUTERCRAFT.md` lists the new methods.

### Ponders and translations

- New scenes cover the Electric Pump (562d383), the connectors and their removal, and the servo
  (d0993a0). The schematics are the mod's own builds and share only the vanilla snow and concrete
  floor with Create's schematics.
- The 1.21 schematics are DataVersion 3955 and store `LastKnownPos` and `Controller` as int arrays.
  The 1.20.1 code reads them as compounds (`ModularAccumulatorBlockEntity:390-393`, Create's
  `FluidTankBlockEntity`), so the scenes need DataVersion 3465 and compound positions, or the tanks and
  accumulators fall apart.
- The ponder plugin drops its duplicate call of the controller fix (8c4c64d).
- The 41 new English keys come with their translations from the 1.21.1 zh_cn, ru_ru, pt_br, ja_jp,
  es_es, es_mx, fr_fr and ko_kr files (3e49327). The locales and keys that only 1.20.1 has stay.
- The five keys `createaddition.tooltip.liquid_burning.*`, `tooltip.charging.*` and
  `tooltip.alternator.info` of the small fixes are only in en_us (8fb90f8) and get their translated
  values here. The death message keys are already renamed in all 21 locales (66076fb), so this
  bundle must not add them again under the old names.

### Release

- One `chore(mods)` commit adds the fork jar to `local-mods/` and drops the Modrinth entry of 1.3.3.
  The same jar replaces `mods/createaddition-1.20.1-1.3.3.jar` by hand, see
  [Operator notes](#operator-notes).
- A permission record in `modrinth-permissions/` and the license texts in `licenses/` come with it.
- A binary compat scan of all pack jars for links into `com/mrh0/createaddition/` is expected to find
  only AllTheLeaks and `EnergyNetworkManager.instances`. A first scan of the jar of 8224da6 against
  the 265 jars of the Forge pack boot of 2026-11-05 (`packboot-forge/mods` in the workflow
  scratchpad, among them Create 6.0.8, Steam 'n' Rails 1.7.3 and CC:Tweaked 1.120.2) found no
  missing class, field or method. The scan runs again on the release jar.
- Full pack boots on Forge 47.4.18 and on the production Arclight must meet the
  [boot baseline](../server/logs-and-crashes.md#boot-error-baseline), parse every charging recipe and
  show every CC&A mixin applied with `-Dmixin.debug.export`.
- The Forge mod list of that boot shows `createaddition` as `1.20.1-1.7.3-skytekx.1`. Only the
  manifest and the `${file.jarVersion}` of `mods.toml` were checked in the baseline.
- A stock 1.3.3 pack client logs in to the fork server, and a fork client logs in to a 1.3.3 server.
  Both logins must end with `Connection closed - mismatched mod channel list`, and the mismatch
  screen must name `createaddition:main` (Forge 47 `HandshakeHandler`, the protocol bump of
  0611ece). No client was available for the baseline.
- The first fork boot appends the 11 new keys to `config/createaddition-common.toml` and keeps its
  41 old values. Upstream's `Config.loadConfig` writes the file with `WritingMode.REPLACE`
  (`Config.java:304`), so the key order inside the sections changes. Commit the rewritten file
  while the server is stopped and before `tools/build_mrpack.py`, which takes every override from
  git HEAD.
- The in game tests of the small fixes have not run. On a copy of the live world they are a 3x3x5
  Modular Accumulator with goggles on parts other than the controller, a schematic print of it at
  its real size and a Symmetry Wand in the hotbar while placing accumulators. They also cover
  barbed wire saved by 1.3.3 that loads dry, barbed wire placed in water, turned with a wrench and
  picked up with a sneak click, an Electrum Block that an iron pickaxe mines and that drops itself,
  and a beacon on an electrum base.
- The boot tests of the small fixes have not run either. A dedicated Forge 47.4.18 server and the
  production Arclight must load no client class (only a static javap check of the new and changed
  classes ran), and a client must show the burn and charge timers on the goggles, the stress values
  in the item tooltips and the death messages of barbed wire and the Tesla Coil in chat.
- The server handler of `ObservePacket` calls `getBlockEntity` at any position a client names,
  without a loaded chunk check or a distance check, and only the client keeps its cooldown of 10
  ticks. A modified client could make the live server load or generate chunks anywhere. This
  predates the fork, but the small fixes add the Tesla Coil and the accumulator controller as
  observed block entities. The connector rework or this bundle guards `sendUpdate` with
  `hasChunkAt` and a distance of 64 blocks and limits how often each player may send the packet,
  which needs no protocol change.
- The docs work adds the row in [Mod set](mod-set.md), the carryover file and the final status here.

## Excluded

| Upstream work | Reason |
|---|---|
| Sable and Aeronautics compat (`sable-aeronautics-compat`) | Out of scope. Sable and Aeronautics exist only for NeoForge 1.21.1. This covers the docking connector (a58a938, e1f3f50), b1a9674, the Sable parts of 9dd1b46 and 5490949 and the 1.5.11 Aeronautics crash fix |
| NeoForge port churn (`neoforge-port-churn`) | About 180 commits from 23d5509 to 1ebd90c (2025-03-20 to 2025-07-16) and later API changes. They are translated where a ported feature needs them and never ported on their own |
| NeoForge datagen (`neoforge-datagen`) | The 1.20.1 tree writes every resource by hand and commits no datagen output, so new files are written by hand. Generated 1.21 blockstates and models are the mod's own files in a format 1.20.1 reads and may be copied. Recipes, loot tables and tags are translated |
| Common `c:` tags (`c-common-tags`) | Upstream moved to `c:` tags (3e8a818, a95515d, 29db159). AlmostUnified relies on `forge:` tags, so the port keeps them |
| Wire spool stacking fix (`wire-spool-stacking-fix`) | Commit 42fe46d fixes a bug of the 1.21 data components. The 1.20.1 `WireSpool` already clears the tag (`WireSpool.java:89`, `107`). Verify only |
| Portable Energy Interface capability (`pei-capability-invalidate`) | Commit 9c6dc1a is NeoForge specific. The 1.20.1 code already invalidates the old capability (`PortableEnergyInterfaceBlockEntity:33-50`). Verify on a train only |
| Connector capability cache (`connector-cache-blockcapabilitycache`) | Commit d239617 is the NeoForge `BlockCapabilityCache` cleanup. Its 1.20.1 form is the connector capability retry in the connector rework |
| Recycling recipe conflict (`recycling-recipe-conflict`) | The base already ships `data/create/recipes/crushing/*_recycling.json` (e3fc63e) |
| Tesla Coil damage type (`tesla-coil-damage-type`) | Part of 7a66e82, already in the base as 22546fb |
| Biofuel and Blaze Cake burn times (`biofuel-and-blaze-cake-burn-times`) | The base already has 24000 and 3200 (b453db8, bcea84e, f5da39c) |
| Liquid Blaze Burner container rewrite (`liquid-blaze-burner-fluid-container-rewrite`) | Commit 3ec4663 is a NeoForge rewrite. The 1.20.1 code uses `FluidUtil.tryEmptyContainerAndStow`. Verify only |
| Liquid Blaze Burner tank of 1000 mB (`liquid-blaze-burner-capacity-default-1000`) | Commit 6cbf853 silently lowers the tank default from 4000 to 1000 mB, a savegame and behaviour change. The port keeps 4000 |
| Liquid Blaze Burner Flywheel visual (`liquid-blaze-burner-flywheel-visual`) | Commits c6d4e98 and db34ece fix the rendering of the 1.21 port. The 1.20.1 renderer works |
| Electric Motor visual (`electric-motor-visual`) | The `ElectricMotorVisual` of 5c2b9ea is dead code upstream and never registered |
| Stator block stub (`stator-block-stub`) | Commit 7cb0ef9 adds an empty class |
| Removal of two Create mixins (`contraption-and-speed-controller-mixin-removal`) | Commits ca6785a and 3d442fd remove mixins that Create 6.0.8 on 1.20.1 and the Digital Adapter still need. The fork keeps them |
| Removal of compat recipes (`compat-recipe-removals`) | Commits ced6d21 and affbdd8 remove Immersive Engineering and Tinkers' Construct recipes because those mods were missing on 1.21. The fork keeps them |
| Mekanism heater compat test (`mekanism-heater-compat-test`) | Added in affbdd8 and removed again in 32323d8, no net change |
| Water tag removal for seed oil and bioethanol (`seed-oil-bioethanol-water-tag-removal`) | Commit ced6d21 changes the physics of fluids already in the world, and ignitable fluids do not need it. The owner may revisit |
| Removal of `create:safe_nbt` (`safe-nbt-tag-removal`) | Commit 7a66e82 drops the tag, but Create 6.0.8 for 1.20.1 still uses it |
| Removal of the accumulator collision shape (`modular-accumulator-collision-shape-removal`) | Commit 9dd1b46 removes it without explanation and with no fix a player would see |
| Creative tab in search only (`creative-tab-search-only`) | Cosmetic (ced6d21) |
| Liquid burning recipe codec crash (`liquid-burning-recipe-codec-crash`) | Commit 46018f8 fixes the NeoForge `SizedFluidIngredient` codec, which 1.20.1 does not use |
| Fixes of the NeoForge port (`port-era-neoforge-fixes`) | Commits 19df542, d79e333, 6457b96, 8207188, e9727ae, 7489814, f3a4751, a150f60, c9ef28b, 5eace6f, 2270916 and the relay and light connector fixes of ced6d21 repair bugs that the NeoForge port or its datagen introduced. The arm interaction categories (c10800c, 7f08dae, 8a9524f) are already in the base as 34cb284 |
| Debug ponder scene (`debug-ponder-scene`) | Commit 691cafb and `DebugScenes` are for development only. The real fix, 8c4c64d, is part of the new ponders |
| Build and repository tooling (`build-and-repo-tooling`) | Bumps of MDG, Gradle, NeoForge and Create, `lang_validator.py`, README, CHANGELOG, templates and the icon move have no player effect |
| Cosmetic lang changes (`cosmetic-lang-changes`) | The rename to "Diamond Grit Sand Paper" and other wording churn. Only the keys of ported features are merged |
| Vanilla content of 1.20.5 and later (`vanilla-1-20-5-content`) | No ported feature needs it. Data components, `HolderLookup`, `RecipeHolder` and `ItemInteractionResult` are API churn that the translation rules handle |
| Removal of the alternator speed requirement (`alternator-speed-requirement-removal`) | Commit f3091d0 ("stress tooltip") also comments out `AlternatorBlock.getMinimumRequiredSpeedLevel` (MEDIUM) without a changelog line, so upstream alternators make energy below 30 RPM. That is a balance change, not a fix. The port keeps MEDIUM (`AlternatorBlock.java:77-79`). The owner may revisit |
| Deoxidizing the copper blocks of 1.20.3 (`deoxidize-1-20-3-copper-blocks`) | Commits 0ca5477, 1f606d3 and 40b1de2 also cover chiseled copper, copper doors, trapdoors, bulbs and grates, which exist only since vanilla 1.20.3. The part valid on 1.20.1, Create's copper tiles, is ported |
| Tags for wrong tool tiers (`tool-tier-incorrect-for-tags`) | The 1.21 datagen writes `minecraft:incorrect_for_wooden_tool` and its stone and gold forms, which exist since 1.20.5. On 1.20.1 the tag `minecraft:needs_iron_tool` does the job, and the Electrum Block fix adds it |
| Tag merge of connector and accumulator recipes (`connector-and-accumulator-recipe-tag-merge`) | Upstream folds the gold and electrum recipes of the large connector and the accumulator into the item tags `large_connector_usable_rods` and `modular_accumulator_usable_wires`. The results stay the same, players gain nothing, and recipe ids would change |
| Empty cake loot tables and food tags (`cake-empty-loot-and-food-tags`) | The empty cake loot tables and the `forge:foods/edible_when_placed` and biomass storage tags of the 1.21 datagen change nothing for players |

## License conditions

- The fork stays under the MIT License of the upstream project. Upstream jars carry no license text,
  so the jar ships the full upstream `LICENSE` as `LICENSE_createaddition` (c276dbd, `UPSTREAM.md`
  build change 7).
- The `mods.toml` keeps the upstream author attribution and adds a credit that marks the build as a
  modified, unofficial backport (`UPSTREAM.md` build change 8).
- The jar's `NOTICE_createaddition` names the base commit 42b6f2d and the port source 658a390, marks
  the build as unofficial with the suffix `-skytekx.N` and lists the changes
  (`ports/createaddition/NOTICE`). Each bundle adds its content to that list.
- Create's MIT notice covers the parts the mod took from Create between 2021 and 2023, while Create was
  MIT as a whole: the output ejection in `RollingMillBlockEntity`, the shape builder `CAShapes` and nine
  edited textures, all listed in `NOTICE`.
- No Create asset dated 2025-09-21 or later may enter the fork. Create's assets are All Rights Reserved
  since Create commit 9dcc21950 of that day (`UPSTREAM.md`, License).
- The Pixabay sounds ship inside the jar only, never as loose files in `licenses/`, the pack or the
  repo outside `ports/`.
- The bundled jar needs a record in `modrinth-permissions/` (MIT, the upstream author, unofficial build,
  the NOTICE and the SHA-1 of the reproducible build) and the license texts in `licenses/`, named like
  `licenses/FluxNetworks-1.20.1-7.2.1.15-LICENSE.txt`.
- Before the release, the texture check `bp2/license-createaddition/scripts/texcmp.py` in the workflow
  scratchpad runs on every texture the port adds: `electric_pump/block.png`, `servo_motor/block.png`,
  `item/electrum_amulet.png` and the barbed wire texture that came with 7ce2204. It compares them with
  the assets of Create 6.0.8 and Create 6.0.10. A first run against 6.0.8 found no overlap at or above
  25 percent. The run against 6.0.10 has not happened.
- The pump's block and item models reuse the two 12x3x12 flange cuboids of Create's
  `mechanical_pump/block.json` (blob 34cb63e in Create 6.0.8). Nobody has checked the date of that blob
  yet. If `git log --find-object=34cb63e` in a Create clone dates it before 2025-09-21, the pump model
  joins the Create derived list in `NOTICE`. Otherwise the flanges are redrawn. The other new models
  share at most one trivial cuboid with any Create model.

## Savegame notes

- No registry id, NBT key, block state, config key or saved data key is renamed or removed. New are the
  ids `createaddition:electric_pump` and `createaddition:servo_motor`, the saved data
  `createaddition_wires`, the barbed wire state `waterlogged` and the config keys of the baseline.
  Missing states load as their default, and Forge appends missing config keys and keeps old values.
- The WireGraph builds itself on the first boot. The wire data of each connector (`nodes` with `id`,
  `other`, `type` and the position, plus the legacy `node0` to `node7`) is the same on both branches,
  so the graph starts from the loaded block entities and walks every far end through saved chunk NBT.
  The network buffers, which 1.3.3 keeps in memory only, start empty and persist from then on.
- A test on a copy of the live world with the production Arclight jar must pass before any deploy:
  stats and verify, a Chunky run of radius 500 around a network, unload and reload with relays toggled
  while unloaded, a restart, a region scan that compares every connector's node list with the 1.3.3
  copy, the same with skytekx-compat 2.1.3 and 2.1.6, and a round trip to 1.3.3 and back.
- A rollback to 1.3.3 ignores `data/createaddition_wires.dat`, but a later upgrade would trust the stale
  graph for unloaded chunks. So a rollback deletes that file in every dimension folder of the world,
  or the operator runs `/cca_wires rebuild` in each dimension after the upgrade.
- The existing world changes its behaviour in four ways. Wires and relays work across unloaded
  chunks. An alternator whose back face touches another kinetic network joins it on its next update,
  and a speed conflict breaks a block, so the live world should be scanned or the players warned.
  Fire spreads over the oils and fuels that [Ignitable fluids](#ignitable-fluids) lists, Thermal
  crude oil among them, in loaded chunks. Tesla Coils saved as powered keep their old light until they toggle.
- Existing stacks stay valid: empty capacitors still stack to 64, charged ones do not stack, and
  existing festive wires between two large connectors stay.

## Arclight notes

- The verifier reads unloaded chunks through `ChunkStorage.read` and `IOWorker.loadAsync`. Arclight
  1.0.6 has no mixin on `IOWorker`, its `ChunkMapMixin` only redirects `upgradeChunkTag`, and Steam 'n'
  Rails only injects at the head of `IOWorker.store`, before the tag is queued.
- SkyTekx Compat 2.1.3, which the pack bundles, hands out the queued tag itself while a worker can
  still add and remove `__context` on it. Version 2.1.6 on the Mac mini hands out a copy (see
  [SkyTekx Compat](../content/compat-mod.md#chunk-tags-waiting-for-their-write) and
  [Logs and crashes](../server/logs-and-crashes.md#chunk-save-race-during-pregen)). A misread tag could
  lead the verifier to mark a wire end missing and delete a real wire. The fork therefore accepts a
  saved tag only with status `minecraft:full` and a `block_entities` list, and otherwise keeps the
  position unresolved. This is a local change.
- The fire mixins chain with Arclight's own mixins on `FireBlock`, `BaseFireBlock` and `FlowingFluid`
  and with those of Supplementaries, Moonlight, NostalgicTweaks and Ad Astra (javap of the Arclight
  mixins, `critic_notes` item 4). The fire that falling fluid spreads fires no Bukkit
  `BlockIgniteEvent`, so a protection plugin could not cancel it. The `plugins/` folder holds no such
  plugin today.
- The research rates the new blocks, block entities and visuals as low risk.

## Local divergences

These differ from upstream on purpose and must survive the next upstream sync.

- The version keeps upstream's form `1.20.1-<version>` plus `-skytekx.N`.
- The Liquid Blaze Burner tank stays at 4000 mB, the hard coded value of the 1.20.1 base and now
  the default of `liquid_blaze_burner_max_liquid_capacity` (`Config.java:272`), not upstream's new
  1000.
- Config values are read lazily, never in a static initialiser.
- The Modular Accumulator stops its capability lookup after four hops.
- The Electric Motor peripheral clamps a finite speed before the cast to float.
- Barbed wire schedules a water tick in `updateShape` and takes the waterlogged state also when it
  is placed without a player.
- The probe of de9d7e7 is left out, since the 1.20.1 fluid ingredient ignores the amount.
- An empty Capacitor stacks to 64 as in 1.3.3, because upstream's 16 would oversize existing stacks.
  A charged one stacks to 1.
- A Capacitor charges and discharges only as a single item. Upstream charges a whole stack as one item,
  which splits into several charged capacitors and dupes energy.
- The WireGraph verifier only trusts full saved chunks, see [Arclight notes](#arclight-notes).
- The alternator keeps its minimum speed MEDIUM.
- Ignitable fluids use `forge:ignites`, and seed oil and bioethanol keep their `minecraft:water` tag.
- A flowing fluid ignites when its source fluid is in `forge:ignites`.
- The fork keeps the Create mixins and the compat recipes that upstream removed.
- The Electrum Block fixes are hand written, since the fork has no datagen.
- The electrify recipes take `forge:` tags as inputs.
- The paper to straw recipe lives at `data/createaddition/recipes/rolling/straw_from_paper.json`, not
  in upstream's `create:rolling/paper`.
- The ponder schematics are converted to the NBT of 1.20.1.

## Open owner decisions

The plan ports these with upstream's or the safe default, and the owner may rule otherwise
([Decisions](../decisions.md)).

- The Digital Adapter returns floats to scripts instead of integers (393c51f). Ported by default.
- The spool recipe makes 24 from iron plate and rod. Ported by default.
- An empty Capacitor stacks to 64.
- Electrum comes from charged gold without silver.
- The alternator keeps its speed requirement, and seed oil and bioethanol keep the water tag.

## Operator notes

- Server and clients update together. The protocol bump and the new registry entries refuse a client
  of an older pack at login.
- Mirror the jar by hand while the java process is gone: remove `mods/createaddition-1.20.1-1.3.3.jar`
  and copy the fork jar from `local-mods/` into `mods/`. Then boot and commit the refreshed
  `config/createaddition-common.toml` before `tools/build_mrpack.py`, which takes overrides from git
  HEAD.
- Deploy only after the live world test under [Savegame notes](#savegame-notes) has passed, and take
  a backup of `world/` first.
- The command `/cca_wires stats`, `verify` or `rebuild` runs over RCON, for example
  `python3 tools/rcon.py 127.0.0.1 65535 "$RCON_PW" cca_wires stats`.
- A boot is clean when it reaches `Done` with 0 FATAL lines, no crash report, no watchdog and only the
  two accepted ERROR lines, and when every charging recipe parses.
- Announce the alternator with two shafts, the new Electrum Amulet and the fire over oils to the
  players.

## Changelog for players

Planned, not shipped.

### New for players

- The Electric Pump and the Electric Servo Motor, the servo with a CC:Tweaked peripheral.
- Charging steps in Sequenced Assembly, gold to electrum on the Tesla Coil and deoxidizing of Create's
  copper tiles.
- Goggle timers on the Liquid Blaze Burner and the Tesla Coil.
- The Rolling Mill rolls paper into straw.
- Shears that cut all wires of a connector.
- Ponders for the pump, the servo and the connectors.

### Changed for players

- Wires and relays carry energy across unloaded chunks.
- The alternator takes a shaft on both sides.
- The Electrum Amulet charges FE items instead of giving Luck and Speed.
- The Capacitor holds FE.
- Fire spreads over oils and fuels.
- The spool recipe makes 24 from iron plate and rod.
- The Tesla Coil glows while powered.
- Festive wire no longer joins two large connectors.
- The Digital Adapter returns fractions to scripts instead of whole numbers.
- JEI shows the burn time of liquid fuels as 20m instead of 20 min.

### Fixed for players

- A mined Electrum Block drops itself.
- Death messages by barbed wire and the Tesla Coil show their text.
- The Modular Accumulator no longer crashes with a stack overflow.
- The mod's ambient sounds play while Create's ambient sounds are on.
- Barbed wire can be waterlogged and wrenched.
- Stress values show in the tooltips.

## History

- Commit 28bde03 first versioned the fork as `1.7.3-1.20.1-skytekx.1` with `version = mod_version`
  and built `createaddition-1.7.3-1.20.1-skytekx.1.jar`. Maven's `ComparableVersion` sorts that
  below the shipped `1.20.1-1.3.3` (7 is less than 20), so a dependent range in upstream's form such
  as `[1.20.1-1.2.0,)` would reject it. The version fix d7d4fad replaced it before any jar with that
  version shipped.

Related: [Mod set](mod-set.md), [Twilight Forest backport](twilightforest-backport.md),
[AlmostUnified](almostunified.md), [Arclight](../server/arclight.md),
[Logs and crashes](../server/logs-and-crashes.md).
