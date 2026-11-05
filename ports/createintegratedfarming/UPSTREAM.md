# Upstream: Create: Integrated Farming (1.20.1 Forge port base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Create: Integrated
Farming, the base of the SkyTekx port of the mod from NeoForge 1.21.1 down to Forge 1.20.1.

| Field | Value |
|---|---|
| Repository | https://github.com/DragonsPlusMinecraft/CreateIntegratedFarming |
| Ref | Branch `1.21.1/6.0.0-dev`, the only branch. The repository has no tags, and `HEAD` points at this branch |
| Commit | `1f63780108d1d51f3b2c9ebdf4bb0cd5cb3f5906` ("[version] Release 1.4.3") |
| Commit date | 2026-09-21T14:16:01+08:00 |
| Upstream version | `mod_version = 1.4.3` (Minecraft 1.21.1, NeoForge 21.1.248, Create 6.0.10, Create: Dragons Plus 1.11.9) |
| Matching release | Modrinth version `EWfVwbHy`, `create-integrated-farming-1.4.3.jar`, SHA-1 `27cbb530828aa375450d005426e609d96cb254e9`. All 285 Java files of its sources jar are byte-identical to this commit |

Every file in the snapshot is byte-identical to `git archive 1f63780` of the upstream repository,
including the upstream `.github/`, `.idea/` and `codeformat/` files. The changes made on top of the
snapshot are separate commits in this repository's history (`git log -- ports/createintegratedfarming`).

## License

- Code: GNU Lesser General Public License version 3 or later. `LICENSE.txt` is the verbatim LGPL 3.0
  text followed by the GPL 3.0 text, with no additional terms. `gradle.properties` says
  `mod_license = LGPL-3.0-or-later`, and every Java file carries the header of
  `codeformat/HEADER.txt`: "Copyright (C) 2025 DragonsPlus" and
  "SPDX-License-Identifier: LGPL-3.0-or-later". The Modrinth project (`9k1pAsfR`) lists
  LGPL-3.0-only. The fork meets the GPL 3.0 terms for conveying a modified work, which satisfies
  every reading of the labels.
- Textures from Farmer's Delight: `textures/block/farmersdelight/` (the basket and the safety net)
  are copies of Farmer's Delight textures under the MIT License, Copyright (c) 2020 vectorwing.
  `FARMERS_DELIGHT_LICENSE.txt` next to them stays in the source and in the jar.
- Parts from Create: `CustomHarvestBehaviour#harvestBlock` adapts Create's
  `BlockHelper.destroyBlockAs` (marked `@CodeReference(... license = "mit")`), and
  `vacuum_harvester.png` contains pixel blocks of Create's `cam_linkage.png` and
  `rotation_speed_controller.png`. Both Create blobs predate Create commit `9dcc21950`
  (2025-09-21), which made Create's assets All Rights Reserved, so they came under Create's earlier
  "MIT License, Copyright (c) 2019 simibubi". The fork ships that notice as `LICENSE-CREATE.txt`.
- Not verified: `textures/block/chicken.png` looks like pixel art derived from the vanilla chicken,
  a Mojang asset that the Minecraft usage guidelines govern like other mod textures. Nobody compared
  it with the vanilla files.

## Build changes

Upstream builds for NeoForge 21.1.248 with ModDevGradle 2.0.141 on Gradle 9.5.0 and Java 21. The fork
builds for Forge 1.20.1 with the toolchain of the other Create addon forks in `ports/`: ForgeGradle 6.0.6,
Forge 47.4.0 userdev, Parchment 2023.06.26-1.20.1, Gradle 8.1.1 and Java 17, against Create 6.0.8-289
(slim), Ponder 1.0.91, Flywheel 1.0.5 and Registrate MC1.20-1.3.3. The jar runs on the Forge 47.4.18 of
the pack.

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew build -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

The build writes `build/libs/create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar`, which `reobfJar`
rewrites in place. That jar is the one to ship.

1. Gradle wrapper: `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` are the copies that
   every other port in this repository uses (wrapper jar SHA-256 `33ad4583...`), and
   `gradle-wrapper.properties` names Gradle 8.1.1 with the official `distributionSha256Sum`
   `e111cb99...`. Upstream's wrapper jar (`7d3a4ac4...`) and Gradle 9.5.0 are gone.
2. `build.gradle` is rewritten for ForgeGradle 6 and keeps upstream's layout: the main source set plus
   one source set per integration under `src/integration/<name>/`, compiled against main and merged
   into the one jar. Only the integrations of mods that the pack carries remain (see source changes).
   The publishing blocks (Maven, CurseForge, Modrinth), the run configurations and the IDE settings are
   gone, since the fork publishes nothing itself.
3. Spotless is gone. Upstream applies it on every build (`build.dependsOn(spotlessApply)`), which
   rewrites the sources in the checkout. `codeformat/HEADER.txt` and `codeformat/formatter-config.xml`
   stay, and every Java file keeps its header.
4. Dependencies: Create: Dragons Plus is the Modrinth file of the pack's index entry (project
   `dzb1a5WV`, version `eMalpJtP`, `CreateDragonsPlus-1.11.9.jar`). The Farmer's Delight and
   Supplementaries integrations compile against the pack's files from the Modrinth Maven
   (`FarmersDelight-1.20.1-1.3.4.jar`, `supplementaries-1.20-3.1.44-forge.jar` with
   `moonlight-1.20-2.16.38B-forge.jar`). JEI 15.20.0.106 provides the API. MixinExtras 0.4.1 is compile
   only, because every Create jar of the range `[6.0.8,6.1.0)` nests `mixinextras-forge` 0.4.1, as the
   Create Crafts & Additions fork records.
5. `gradle.properties`: Forge, Minecraft and dependency versions for 1.20.1, and
   `mod_version = 1.4.3-skytekx.1`. The jar name carries the Minecraft version
   (`version = "${minecraft_version}-${mod_version}"`), and `mods.toml` carries `mod_version`. The
   part 1.4.3 names the upstream release, and `-skytekx.N` marks the unofficial build.
6. `settings.gradle`: the Forge plugin repository and `rootProject.name = 'create-integrated-farming'`,
   so the project name does not depend on the checkout directory.
7. Mod metadata: `src/main/templates/META-INF/neoforge.mods.toml` becomes the Forge `mods.toml` in the
   same place, still expanded by the `generateModMetadata` task. `license` stays `LGPL-3.0-or-later`,
   `issueTrackerURL` points at https://github.com/skytekx/skytekx3-1201-server/issues, since bugs of this
   build are ours, and `credits` says that this is a modified, unofficial port. `authors`,
   `displayURL`, `displayName` and `description` are upstream's. The dependencies are Forge
   `[47.1.33,48)`, Minecraft `[1.20.1]`, Create `[6.0.8,6.1.0)` (upstream asks for `[6.0.10,)`, which
   does not exist for 1.20.1), Create: Dragons Plus `[1.11.9,)`, and the optional Farmer's Delight and
   Supplementaries. `src/main/resources/pack.mcmeta` declares pack format 15, since Forge 47 does not
   generate one.
8. Reproducible jar: the `jar` task sets `preserveFileTimestamps = false` and
   `reproducibleFileOrder = true`, and the manifest has no timestamp. Two clean builds of the same
   commit give the same SHA-1.
9. License files in the jar: `LICENSE.txt` (upstream's LGPL and GPL texts, unchanged), the new
   `LICENSE-CREATE.txt` (Create's MIT notice, the text of Create's `LICENSE` before `9dcc21950`, taken
   byte for byte from the `LICENSE-CREATE.txt` in `CreateDragonsPlus-1.11.9.jar`) and `NOTICE`, which
   says that this is a modified, unofficial build, names the upstream commit, sums up the changes and
   points at this directory as the source. `FARMERS_DELIGHT_LICENSE.txt` stays next to the textures it
   covers.
10. `.gitignore`: also ignores `classes/`, `mcmodsrepo/`, `.mixin.out/` and logs.
11. `reobf.jar.libraries` also gets the compile classpath of every integration source set. reobfJar maps a
    call to an inherited vanilla method through the class hierarchy of its owner, and without the
    integration targets the calls of `TomatoHarvestBehaviour` and `FlaxHarvestBehaviour` to
    `CropBlock#getAge` and its siblings kept their Mojang names. The first test boot crashed there with a
    `NoSuchMethodError` (commit 3b2fabcc).

## Source changes

The port keeps every registry id, block state, NBT key, config key and data file name of 1.4.3. The
items below are what differs from upstream, with the reason.

1. Entry points: Forge 47 loads one `@Mod` class per mod id, while NeoForge also constructs the
   `@Mod(dist = CLIENT)` class `CIFClient`, the data class `CIFData` and one class per integration.
   `CIFCommon` now constructs them: `CIFClient.init` through `DistExecutor`, `CIFData` while data
   generation runs, and the integration entry points by class name, since they live in their own
   source sets. Each integration still checks that its mod is loaded.
2. Capabilities: Forge 47 has no `RegisterCapabilitiesEvent`. The roost and the vacuum harvester return
   their output handler from `getCapability` on the sides that upstream registers (every side but the
   roost's front, every side but the harvester's bottom) and invalidate it in `invalidate()`. The
   Farmer's Delight basket gets the behaviour provider of Create: Dragons Plus through
   `AttachCapabilitiesEvent`, built once per basket on the first query. `BasketBehaviourProvider` hands
   every other capability on to the basket, because Create 6.0.8's `DirectBeltInputBehaviour` inserts
   through the capability of its own block entity, which is the provider here.
3. Data maps: the synced data maps `create_integrated_farming:chicken_food` for items and fluids become
   `ChickenFoodMap` (new). It reads `data/create_integrated_farming/data_maps/item/chicken_food.json` and
   `.../fluid/chicken_food.json` from every data pack in pack order in the NeoForge format (`values` keyed
   by ids and `#tags`, `replace`, `remove`, later entries win) on each reload, and resolves tags when
   asked. 1.20.1 has no `ItemStack.SINGLE_ITEM_CODEC`, so `ChickenFoodItem` reads `using_converts_to`
   in its 1.21.1 object form `{"id": "minecraft:bowl"}` and also as a plain item id, and writes the
   object form. The new `ChickenFoodSyncPacket` sends the resolved maps to clients at every data pack sync, as
   NeoForge syncs the two maps, so that feeding by hand is predicted on the client.
4. Network: a `SimpleChannel` `create_integrated_farming:main` with protocol `1` replaces the payload
   registrar. Message 0 is the roosting display snapshot, message 1 the chicken food maps, both from the
   server to the client. The display records encode on `FriendlyByteBuf` through a small `BufCodec` (new)
   in place of `StreamCodec`.
5. Blocks: `Block#use` replaces `useItemOn` and `useWithoutItem`. A `PASS` from the item step falls
   through to the main hand step, as `PASS_TO_DEFAULT_BLOCK_INTERACTION` does on 1.21.1. The block codecs
   go, the methods that 1.20.1 declares public become public, and `isPathfindable` takes the 1.20.1
   arguments. The roosts hold the empty roost as a `Supplier<Block>`, since the `BlockEntry` of
   Registrate for 1.20.1 is no `Holder`.
6. Loot: loot tables are named by `ResourceLocation` and come from the `LootDataManager`. The roosting
   display parser writes a table to JSON with the Gson of `LootDataType.TABLE` and reads nested tables
   from `name`, the 1.20.1 field, as well as `value`.
7. Fishing: the catch delay uses the Lure level (5 seconds per level, as 1.21.1's time reduction), the
   catch luck the Luck of the Sea level, and the loot parameters `KILLER_ENTITY` and
   `DIRECT_KILLER_ENTITY`. The catch delay and catch chance draw from the random source of the level,
   since 1.20.1 entities keep theirs protected. `Math.ceilDiv` (Java 18) becomes `Mth.positiveCeilDiv`.
8. Harvesting: `CustomHarvestBehaviour#harvestBlock` follows Create 6.0.8's `destroyBlockAs`, which drops
   the experience of the `BlockEvent.BreakEvent` instead of NeoForge's `BlockDropsEvent`. The enchantments
   of Create Enchantable Machinery come from the NBT list `Enchantments`. `collectOrDropItem` of Create
   6.0.10 is `dropItem` in 6.0.8, which also puts the item into the contraption storage first.
9. Mixins: the five mixins into Create's own methods set `remap = false`. `SeatBlockMixin` maps
   `updateEntityAfterFallOn` (a vanilla override, `m_5548_`) through the refmap
   `create_integrated_farming.refmap.json` and checks leashes on `Mob`, since 1.20.1 has no `Leashable`.
   No mixin targets a Minecraft class, so Arclight's patches do not meet them. The access transformer
   uses SRG names. `UniversalConditionalMixinPlugin` goes with the integration mixin configs that used it.
10. Client: `VacuumHarvesterMovementBehaviour#renderInContraption` and `#createVisual` carry
    `@OnlyIn(Dist.CLIENT)`, as Create's interface does, so that a dedicated server strips them.
11. Data files: recipes, loot tables, advancements and tags move to `recipes/`, `loot_tables/`,
    `advancements/` and `tags/blocks|items/`. Recipe results use `item`, the wooden rod ingredient
    `forge:rods/wooden`, load conditions the `forge:` types under `conditions`, and the advancements get
    the `minecraft:recipes/root` parent. The chicken food maps use the 1.20.1 uniform int provider form.
    The item map names the new tag `create_integrated_farming:chicken_food` with the six chicken foods of
    1.20.1 (`Chicken.FOOD_ITEMS`), the fluid map `#forge:plantoil`. The block atlas file goes, because
    it only served the removed roosts.
12. Lava Fishing Net drops: Forge 1.20.1 loot tables have no load conditions, and a table that names the
    net's item fails to load with an error while the block is not registered. The conditional loot table
    goes, and `LavaFishingNetBlock#getDrops` drops the block under the same `survives_explosion` rule.
13. Chicken food remainder (local fix): the roost drops a copy of the `using_converts_to` stack of the
    chicken food map. Upstream hands the shared stack to `Containers.dropItemStack`, which empties it, so
    only the first feeding returned it.
14. Lava Fishing (local addition): upstream registers the Lava Fishing Net only with Nether Depths
    Upgrade, Tide, Starcatcher or Confluence, none of which the pack carries. The new `lavafishing`
    source set lets Lava Fishing 1.2.1 (mod id `lavafishing`) fill the net, after upstream's Nether Depths
    Upgrade integration: its catch provider rolls `lavafishing:gameplay/fishing/nether/fishing` in the
    Nether and `lavafishing:gameplay/fishing/lava/fishing` elsewhere, the nets capture the entities of
    the `lavafishing` namespace, and the Ponder scene shows the Quartz Fish. Lava Fishing also enables the
    block, its recipe and its advancement. It is referenced by registry names only. Its three classes
    `LavaFishingIntegration`, `LavaFishingNetCatchProvider` and `LavaFishingNetPonderExample` are
    modified copies of upstream's `NDUIntegration`, `NDUFishingNetCatchProvider` and
    `NDUFishingNetPonderExample`, so their headers keep "Copyright (C) 2025 DragonsPlus" and add the
    SkyTekx line next to it.
15. Twilight Delight: the Ponder scene names the Huge Lily Pad and Huge Water Lily by registry id instead
    of `TFItems`, so the source set needs no compile dependency on the Twilight Forest fork. The depot is
    emptied with `setHeldItem(ItemStack.EMPTY)`, since 1.20.1's depot has no `clearContent`.
16. Ponder schematics: the ten schematics keep upstream's DataVersion 3955, and their block palettes only
    name blocks that exist in 1.20.1. The roost of `ponder/roost/operate.nbt` stored its egg in the
    1.21.1 item form `{count: 1, Slot: 0, id: "minecraft:egg"}`, which `ItemStack.of` of 1.20.1 reads
    as an empty stack, because it reads the byte `Count`. That entry is now `{Count: 1b, Slot: 0, id:
    "minecraft:egg"}`, so the scene shows the egg again. No other schematic holds an item stack.
17. Recipe advancements (local fix): upstream's six advancements under `advancements/recipes/` name the
    recipes without their `crafting/` path, for example `create_integrated_farming:roost` for the recipe
    `create_integrated_farming:crafting/roost`. Their `has_the_recipe` criteria and `rewards` now name
    the `crafting/` ids, so the recipe book unlock fires. 1.20.1 ignores unknown ids there without a
    word, and EMI and JEI never depended on it.

## Left out

| Part | Reason |
|---|---|
| The integration source sets for Atmospheric, Autumnity, Confluence, Cook's Collection, Corn Delight, Crabber's Delight, Create Enchantable Machinery, Cultural Delights, Delight o' Flight, Ender's Delight, Environmental, Festive Delight, Haunted Harvest, Hearth and Harvest, My Nether's Delight, Neapolitan, Nether Depths Upgrade, Jaden's Nether Expansion, Nether's Exoticism, Sable, Starcatcher, Tide, Untitled Duck Mod, Upgrade Aquatic, Vanilla Backport and Windswept | The SkyTekx pack carries none of these mods. Their 1.20.1 builds, where any exist, have other APIs, and nothing could test them. Their source stays in the git history (commit 6e33465f) |
| The duck, goose, turkey and warm or cold chicken roosts | They belong to the removed integrations (Untitled Duck Mod, Environmental, Confluence, Autumnity and Vanilla Backport). The warm and cold chickens are 1.21.5 content |
| The datagen output of the port | The datagen classes compile against Registrate MC1.20-1.3.3 and Create: Dragons Plus 1.11.9, but nobody ran them. The files under `src/generated/` are upstream's output, converted by hand (source change 11). A datagen run would rewrite them and is not verified to give the same files |
| Spotless | See build change 3 |
