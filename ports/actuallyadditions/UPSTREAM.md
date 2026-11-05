# Upstream: Actually Additions (1.20.1 backport base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Actually Additions,
the base of the SkyTekx 1.20.1 Forge backport of the mod's newer NeoForge lines.

| | |
|---|---|
| Repository | https://github.com/Ellpeck/ActuallyAdditions |
| Ref | Branch `1.20.1` |
| Commit | `226a439154a8f103424783ad7afc1e33fe59cbd7` ("Update .gitignore") |
| Commit date | 2024-03-04T23:05:21+01:00 |
| Upstream version | `mod_version=1.1.52` (Minecraft 1.20.1, Forge 47.1.3, Parchment 2023.09.03, JEI 15.3.0.4) |
| Released | Never. No 1.20.1 build exists on Modrinth or CurseForge |
| Port source | Branch `main` @ `cd9d9c63254f471f966ee51b078afdf47af5f852` (2026-06-19, `mod_version=1.3.26`, NeoForge 1.21.1, release 1.3.26) |
| Intermediate steps | Branch `1.20.4` @ `0ebbd811fd511e83fa50eea36f62df38b1e267c8` (2024-06-27, 1.2.14, NeoForge 20.4) and branch `1.21` @ `d9b0a714d1303569f913f855260376309484ba00` (2024-10-23, 1.3.6, NeoForge 21.1) |

The branches `1.20.4`, `1.21` and `main` fork from the base branch at `97532ff` (2024-03-04, "Mark
tiny torch in port.md"). The base has four commits after that fork point: `0e3fec7` (a filter
tweak that `1.20.4` carries as `00418d2`), the merge `59459b1`, the relicensing `33f5541` (its
twin on the other branches is `717199e`) and `226a439`. Branch `main` is 591 commits ahead of the
fork point.

Every file in the snapshot is byte-identical to `git archive 226a439` of the upstream repository
(blob hashes and file modes checked), with the four exclusions listed below. Changes made on top
of the snapshot are separate commits in this repository's history
(`git log -- ports/actuallyadditions`).

## Files left out of the snapshot

| Upstream path | Why it is left out |
|---|---|
| `src/main/java/de/ellpeck/actuallyadditions/mod/util/crafting/RecipeHandler.java` | The 36 lines are all commented out and compile to nothing, and nothing else refers to the class. Its header says the code is an edited copy of a Biomes O' Plenty class under CC BY-NC-ND 4.0, which forbids sharing adaptations. |
| `libs/FastWorkbench-1.12.2-1.3.2.jar` | A third party binary for Minecraft 1.12.2 that `build.gradle` never references. |
| `logs/debug.log`, `logs/latest.log` | Two empty log files. |

## License

- Code and assets: MIT License (`LICENSE`, kept unchanged with its 2019 copyright line). The
  `mods.toml` field `license` says `MIT`. Upstream commit `33f5541` (2024-03-04, "Change license
  to MIT") replaced the old custom license `LICENSE.md` with this file on branch `1.20.1`, and
  `717199e` did the same on the newer lines. Both cite upstream issue #1391, "Change license to
  MIT on all relevant (active) branches", which the repository owner opened on 2024-03-04 and
  closed as completed on 2024-03-06. The original author had already moved the 1.16.4 rewrite
  branch `clean-start` to MIT in `9d6118e` (2020-09-06).
- The same MIT `LICENSE` sits on branch `main` at `cd9d9c6`. The Modrinth project
  (`actually-additions`) declares MIT, and its team lists the owner, a maintainer and the artist
  of the 1.20+ textures. The CurseForge page (project 228404) shows the field "Custom License",
  whose text was not retrieved (not verified). The `LICENSE.md` that `33f5541` removed was itself
  permissive enough for this use: it allowed forking and modifying the code and the use in
  modpacks.
- No separate license covers the textures, models, sounds or translations at either ref. Every
  asset came into the repository by commits and pull requests to it. The original author added
  the four sounds in upstream `a86389d` (2015-12-02) and `6fe1948` (2015-12-13), and no commit
  names a third party source for them (not verified beyond the history).
- 402 Java files at the base still carry the old per file header that points to the "Actually
  Additions License" at `http://ellpeck.de/actaddlicense`. The relicensing above supersedes it,
  and the snapshot keeps the headers unchanged.
- Code excerpts that upstream marks as taken from other mods, all of them kept in the base:
  `TileEntityXPSolidifier` (experience formulas excerpted from Ender IO with permission),
  `misc/BannerHelper` (a banner pattern helper excerpted from Additional Banners with permission,
  called from `ActuallyAdditions#setup`), `ClientRegistryHandler` (an empty method whose body is
  commented out, excerpted from Tinkers' Construct with permission, removed by source change 8) and
  `booklet/misc/GuiAAAchievements` (a 1.12 class inside a block comment, partially excerpted from
  Botania with permission). Two methods are marked as copied from vanilla Minecraft code
  (`AssetUtil`, `AwfulUtil`).
- Not part of this base, and not to be ported: `data/StructureUpdater.java` on branches `1.21` and
  `main`, a data generation helper under "Blu's License of Common Sense" that upstream uses with
  its authors' permission. That permission does not extend to this fork.
- Upstream jars carry no LICENSE file. The SkyTekx build adds `LICENSE` and the SkyTekx `NOTICE`
  to the jar, as MIT asks for the notice in every copy.

## What the base contains

These counts come from the registries in the Java sources and from the jar that the build below
produces. The ids are also cross checked against the blockstates, item models and `en_us.json`.

- It registers 121 blocks (117 through `AABlockReg`, each with its block item and most with a
  block entity type, plus the four crop blocks), 107 further items, 25 menu types, 9 recipe types,
  4 sounds, 2 particle types and 1 entity type. The `en_us.json` has 961 keys.
- The jar holds 398 top level classes, one for every Java file with a live type. Of the 487
  Java files, 91 are commented out in full, remnants of the 1.12 code: the booklet, achievements,
  world generation, villages, giant chests, the smiley cloud and older compat code. So the base
  has no world generation at all (`gen/AAWorldGen` is dead code) and no in game manual.
- It integrates with JEI only (`compileOnly` API 15.3.0.4). Patchouli and Curios come with the
  backport (build change 10). The One Probe and Mekanism appear only as `runtimeOnly` dev run
  dependencies.
- The client fetches `specialPeopleStuff.properties` from upstream's `1.16` branch on GitHub for
  its special renders (source change 5 moves it to the list on `main`). The update checker is
  commented out, so the server makes no network call.
- The source folder `src/main/java/invtweaks/` holds two annotation types of the Inventory Tweaks
  API, which the jar carries as the package `invtweaks.api.container` (source change 3 removes
  them).

Gaps of the base that the backport has to close, since upstream never released it:

- The block `bio_reactor` has no blockstate, the block `vertical_digger` has no `en_us` name, and
  the items `booklet` and `crate_keeper` have no item model. Upstream fixed all four on the
  1.20.4 line. Source change 7 adds the two item models.
- The `pack.mcmeta` still says `pack_format` 6 (the 1.16 value). The data generator cache
  `src/generated/resources/.cache/` ends up in the jar as 18 `.cache/` entries, as it does
  upstream. Source changes 2 and 4 close both.

Registry ids that differ on `main` at `cd9d9c6` (from the same scan of `main`):

- New there: the block `crate_small`, the banner pattern items `book_pattern`, `drill_pattern`,
  `leaf_blo_pattern` and `phan_con_pattern`, the item `item_tag`, the menus
  `item_tag_container`, `sack_container` and `void_sack_container`, the recipe type
  `coffee_ingredient`, the loot modifiers `bat_loot` and `dungeon_loot`, the villager profession
  and point of interest `engineer`, a biome modifier serializer, a condition codec and an
  ingredient type. The data components (21 types) are the 1.21 form of item NBT.
- Gone there: the items `dough` and `paper_cone`, the menu `bag_container` (now
  `sack_container`) and the recipe type `solid_fuel`. The backport keeps every id of the base
  and adds the new ones.

## Build changes

The snapshot compiles unchanged on Java 17 against its own toolchain: ForgeGradle 6.0.54 (from
upstream's range `[6.0,6.2)`), Librarian `1.2.0.7-dev-SNAPSHOT` (from `1.+`), Gradle 8.1.1, Forge
47.1.3 userdev, Parchment 2023.09.03-1.20.1 and JEI 15.3.0.4 from maven.blamejared.com. All
repositories in `build.gradle` and `settings.gradle` still resolve. The compiler settings are
upstream's (javac defaults with UTF-8 sources, no `-Werror`). The task `compileJava` prints only
the javac notes about deprecated and unchecked operations. The changes below only make the build
reproducible and mark the jar as a modified build.

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew compileJava -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

The jar: `./gradlew build` (same flags) writes
`build/libs/ActuallyAdditions-1.20.1-1.1.52-skytekx.1.jar`, which `reobfJar` rewrites in place
with SRG names. That jar is the one to ship. The `-dev`, `-api`, `-sources` and `-javadoc` jars
next to it are upstream's Maven artifacts and are not shipped.

1. Gradle wrapper: `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` are the copies
   that the Twilight Forest and Create Crafts & Additions ports use (wrapper jar SHA-256
   `33ad4583...`). Upstream's `c5a643cf...` is the official Gradle 7.6 wrapper. Both were checked
   against Gradle's published wrapper checksums for the Create Crafts & Additions port. The file
   `gradle-wrapper.properties` keeps upstream's Gradle 8.1.1 and adds `distributionSha256Sum`
   with the official checksum of `gradle-8.1.1-bin.zip`.
2. Plugins: `net.minecraftforge.gradle` is pinned to `6.0.54` and
   `org.parchmentmc.librarian.forgegradle` to `1.2.0.7-dev-SNAPSHOT`, the versions that
   upstream's ranges resolve to today, so that a new plugin release cannot change the build. The
   Librarian version is the one that `ports/skytekx-defixes` pins too.
3. Project name: `settings.gradle` sets `rootProject.name = 'actuallyadditions'`, so the
   project name does not depend on the name of the checkout directory.
4. Version: `mod_version=1.1.52-skytekx.1`. The script `build.gradle` keeps upstream's
   `archivesName = "ActuallyAdditions-$game_version"` and `version = "$mod_version"` but no longer
   appends the Jenkins `BUILD_NUMBER`, so a CI variable cannot change the jar name. The jar is
   `ActuallyAdditions-1.20.1-1.1.52-skytekx.1.jar`, and Forge shows the version
   `1.1.52-skytekx.1` (`mods.toml` uses `${file.jarVersion}`, the manifest's
   `Implementation-Version`). The part 1.1.52 names the 1.20.1 branch, and `-skytekx.N` marks the
   unofficial build. No pack mod declares a dependency range on Actually Additions, and no world
   has the mod yet, so the number can still change before the first release. If the backport
   brings the content close to the 1.3.x line, `1.3.26-skytekx.N` would say that more clearly, the
   way the Create Crafts & Additions fork names the line it backports (`1.20.1-1.7.3-skytekx.1`).
   That choice belongs to the release. The plan keeps 1.1.52: the code base is 1.1.52, and a
   1.3.26 base number would claim parity with a NeoForge 1.21.1 build that has data components
   and 1.21 content, and it would point bug reports at the wrong changelog. Once the backport
   bundles have landed, this file and the NOTICE state the content level as "content parity:
   upstream 1.3.26 (`cd9d9c6`) except the exclusions".
5. Reproducible jar: the `jar` task sets `preserveFileTimestamps = false` and
   `reproducibleFileOrder = true`, and the manifest no longer has `Implementation-Timestamp`. Two
   clean builds of the same commit give the same SHA-1. With the build changes 1 to 9 and no
   source change, three clean builds gave the jar SHA-1
   `c37140ce60ec6da5ac33ebea66b3f9fa3951f7fd` (4560170 bytes). That jar does not start in the
   pack (source change 8), so the OLD side of the savegame test of the release is the base with
   only the boot fix of source change 3, jar SHA-1 `237e0fc65275a0204fe7008f144c395207e41205`
   (4557655 bytes).
6. Javadoc: the task `javadoc` reads the sources as UTF-8. Upstream's `build` runs it for the
   javadoc artifact, and under a host locale that is not UTF-8 (POSIX here) it failed on the
   copyright sign in the API file headers.
7. Ignored files: the fork's `.gitignore` also ignores `/logs/`, `*.log`, `.mixin.out/` and
   `mcmodsrepo/`.
8. License files in the jar: the `jar` task adds `LICENSE` (upstream's, unchanged) and `NOTICE`
   (SkyTekx's, in this directory) to the jar root as `LICENSE_actuallyadditions` and
   `NOTICE_actuallyadditions`. MIT asks for the license text in every copy, and upstream jars
   carry none. The manifest's `Implementation-Vendor` reads
   `Actually Additions (modified build: SkyTekx)`.
9. Mod metadata: in `mods.toml` the field `license` keeps `MIT` and points to the two files, the
   new `issueTrackerURL` goes to https://github.com/skytekx/skytekx3-1201-server/issues since
   bugs of this build are ours, and `credits` adds "Modified, unofficial 1.20.1 build by SkyTekx,
   not supported by the Actually Additions team". The fields `authors`, `displayName` and
   `description` are unchanged.
10. Integrations at compile time: Patchouli `1.20.1-85-FORGE` (maven.blamejared.com) and Curios
    `5.14.1+1.20.1` (maven.theillusivec4.top, the repository of upstream `a556377`) are
    `compileOnly` through `fg.deobf`, the versions the pack ships. Patchouli is the full jar and
    not its API, because the manual pages of `main` use Patchouli client classes outside the API
    (`PageReconstructor` extends `vazkii.patchouli.client.book.page.abstr.PageSimpleProcessingRecipe`,
    `PatchouliPages` calls `vazkii.patchouli.client.book.ClientBookRegistry`). Curios needs only its
    `api` jar. Both full jars are `runtimeOnly` for the dev runs, the way upstream `654465b` and
    the `localRuntime` lines of `main` add them. JEI stays at the 15.3.0.4 API. The Patchouli
    Provider data generator of `654465b` is not added, since the fork writes the manual by hand.
11. Mixin: the plugin `org.spongepowered.mixin` is pinned to `0.7.38` and the annotation
    processor `org.spongepowered:mixin:0.8.5:processor` writes the refmap
    `actuallyadditions.refmap.json`, the setup of `ports/littletiles` and
    `ports/skytekx-defixes`. Forge 47 does not read the `[[mixins]]` block of the NeoForge
    `mods.toml` that upstream `6f56772` added, so the jar manifest names the config in
    `MixinConfigs`, as `ports/enderio/build.gradle:342` does. The config
    `src/main/resources/actuallyadditions.mixins.json` is the one of `main` with
    `compatibilityLevel` `JAVA_17` instead of `JAVA_21`, `minVersion` 0.8.5, `required`,
    `defaultRequire` 1 and the refmap. Its lists stay empty until the mixins land with their
    features (`AnvilMixin` and `ItemCombinerMenuAccessor` of `6f56772` with the Item Tag filter,
    `client.MinecraftMixin` of `3b29799` with the Engineer's Goggles). Until then the annotation
    processor writes no refmap, so the jar has none and Mixin logs one WARN line at boot that the
    reference map could not be read. The dev runs get the properties `mixin.env.remapRefMap` and
    `mixin.env.refMapRemappingFile`, so the mixins of the deobfuscated mod jars apply there. The
    two mixins of `main` touch nothing that Arclight patches: `AnvilMixin` injects at the head of
    `AnvilMenu.mayPickup`, while Arclight's `RepairContainerMixin` only redirects and injects in
    `AnvilMenu.createResult` and its `ItemCombinerMixin` injects at the head of
    `ItemCombinerMenu.stillValid` and leaves `inputSlots` alone (javap of `common.jar` in
    `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`). The client mixin `MinecraftMixin`
    injects at the head of `Minecraft.shouldEntityAppearGlowing`. No mixin uses `@Overwrite`.

## Source changes

These commits change the mod itself on top of the base. Each later commit that ports a fix or a
feature adds its item here, and the NOTICE in the jar sums them up.

1. Optional dependencies: `mods.toml` declares `patchouli` (`[1.20.1-81,)`), `curios` (`[5.0,)`)
   and `jei` (`[15.0,)`), each with `mandatory=false`, ordering `NONE` and side `BOTH`, so Forge
   checks the version of each one that is present and the mod list names them. Upstream declares
   none of the three in `neoforge.mods.toml` on `main`. Only its publish block marks Patchouli
   as required and JEI and Curios as optional on Modrinth and CurseForge (`main`
   `build.gradle:287-289`), and its manual says in chat when Patchouli is missing (`19cfbc92`,
   which comes with the Patchouli manual). The pack ships Patchouli `1.20.1-85-FORGE`, Curios
   `5.14.1+1.20.1` and JEI `15.74.0.233`.
2. Pack format: `pack.mcmeta` says `pack_format` 15, the resource and data pack format of
   Minecraft 1.20.1, instead of the 1.16 value 6 and its 1.16 comment. Upstream moved on with each
   line (26 on `1.20.4`, 34 on `1.21` and `main`).
3. Dead code: the folder `src/main/java/invtweaks/` is gone, as upstream `9bc38d2` removed it on
   `1.20.4`. Its two annotation types `ChestContainer` and `InventoryContainer` came from the
   Inventory Tweaks API without a license note, and the pack's Immersive Engineering ships the
   same package `invtweaks.api.container`, which stops Forge's module layer with a split package
   (source change 8). The fully
   commented `mod/inventory/ContainerGiantChest.java`, the only file that imported them, is gone
   too (upstream dropped it on `1.21` in `268cf65`). So is the fully commented
   `mod/util/crafting/RecipeHelper.java`, whose header names a third party author and the MIT
   License without the notice text. Both compiled to nothing.
4. Build: `processResources` leaves out `.cache/**`, so the hash cache of the data generator no
   longer ends up in the jar.
5. Special renders: the client thread `ThreadSpecialFetcher` reads
   `specialPeopleStuffNew.properties` from upstream's `main` branch (upstream `d9b0a71`) instead
   of `specialPeopleStuff.properties` from the `1.16` branch, which last changed in 2021. Only
   the client starts it (`ActuallyAdditionsClient` registers `SpecialRenderInit`), so the server
   still makes no network call.
6. Config: `mod/config/CommonConfig.java` is the `CommonConfig` of `main` on `ForgeConfigSpec`,
   36 keys in the sections `other`, `machineSettings`, `worldgenSettings` and `itemsSettings`,
   with upstream's names, comments, defaults and ranges. New against the base are `doBatDrops`
   (`b943530`), `farmerConfigurator` (`b66af84`), `leafGeneratorCooldown`,
   `leafGeneratorCPPerLeaf` and `leafGeneratorArea` (`5182ac8`), `fluidLaserTransferRate`,
   `verticalDiggerExtraWhitelist` and `verticalDiggerBlacklist` (`1be720f`, `17d8dcc`) and the
   five switches `generateQuartz`, `generateCanola`, `generateFlax`, `generateCoffee` and
   `generateRice` (`16cd3a6`, `2ffe78d`). The list `Drill Extra Whitelist` is
   `drillExtraWhitelist` from the start, as `1be720f` renamed every key with spaces. The base
   was never released, so no `actuallyadditions-common.toml` with the old name exists. The key
   `furDropChance` is gone, as in `9c6859c`, since nothing read it. The lists use
   `defineListAllowEmpty` and the configurator strings `define` without a validator (`e22b161`).
   The base defined the drill list with an anonymous `ArrayList` subclass as the default, which
   no loaded value matched, so Forge corrected the file at every load (twice at the second boot
   of the base jar `c37140ce`, never at the second boot of this change). A new
   `mod/config/ClientConfig.java` holds `hideEnergyOverlay` (`12527cd`, `9c6859c`), which hides
   the energy overlay of a looked at block (`ClientEvents`, read on each frame as `e21713e`
   does). Only the physical client registers it, in the `DistExecutor` branch of the
   `ActuallyAdditions` constructor. The config reload handler only reads the common config,
   resolves the farmer configurator too and falls back to air for an id that does not parse
   (`9c6859c`, `b66af84`). The readers of the other new keys come with their features: the
   Leaf Generator (`5182ac8`), the Vertical Digger lists (`17d8dcc`), the Fluid Laser Relay
   (`1f78ecc`), the farmer configurator (`b66af84`), the bat wing drops (`b943530`) and the
   world generation switches (`16cd3a6`, `2ffe78d`). Until then those blocks keep reading the
   1.12 enums in `mod/config/values/`, which nothing loads. The translation keys of the built in
   config screen (`a831cbb`, `1be720f`, `8d01e07`) are not added, because Forge 47 has no such
   screen.
7. Data generators: the providers of the base reproduce the committed `src/generated` byte for byte
   (a `runData` into an empty folder rewrote all 906 files unchanged, only the 18 `.cache` files
   differ in their time stamp). On top of that, as on the newer lines: the booklet and the Crate
   Keeper get item models (upstream `3ce5335`, `4876677`), the Crate Keeper with the old texture of
   `textures/items_old/` copied to `textures/item/crate_keeper.png` as `3ce5335` does. The set
   `ActuallyItems.SIMPLE_ITEMS` keeps the flat items, which now use `item/generated`, and the new
   set `ActuallyItems.TOOLS` holds the AIOTs, drills, batteries, rings, leaf blowers, sacks and the
   other held items, which keep `item/handheld` (`4876677`, 63 item models change their parent).
   Every crop stage model renders as `minecraft:cutout` (`49ccc20`, `93242df`). The base rendered
   all 16 of them solid, and the crop textures of each stage stay the base's until the retexture
   brings the four stage textures of `19afc5f`. A crushing recipe writes its second output only
   when it has one (the 1.20.1 form of `c1b23b5`), so `crushing/bone_crusher.json` loses its empty
   `minecraft:air` entry and the serializer reads it with one output, as the 1.3.21 recipes of
   `main` do. The two recipe builder classes carry the inspection suppression of `a706f37`. Not
   needed on 1.20.1: `ead1d9b` and the generated part of `4a16be4` (the base already writes
   `forge:item/bucket` and `forge:fluid_container`, the Forge 47 names), `50407e9` (the 1.20.2
   `DisplayInfo` with an `Optional` background), `b70c791` (the 1.21 holder lookup of enchantments,
   1.20.1 uses `Enchantments` directly) and the `FluidAA` part of `a706f37`
   (`FluidType.initializeClient` is not deprecated in Forge 47). The datagen runs `dcc2220`,
   `556f4a7`, `a7e2d62` and `88b0775` only refresh the output of their own lines. Each change is in
   `src/generated` as `runData` writes it, with the `.cache` files of the three providers that
   changed.
8. Boot: the base jar `c37140ce` reaches `Done` alone on a dedicated Forge 47.4.18 server with 0
   ERROR lines, and its dev client (Forge 47.1.3 userdev) reaches the title screen. In the Arclight
   pack it does not start. Immersive Engineering 10.2.0-183 ships the same package
   `invtweaks.api.container`, and the module layer stops with a `ResolutionException` before any
   mod loads (modules immersiveengineering and actuallyadditions export package
   `invtweaks.api.container` to module enderio, Arclight safeiter2 pack boot of 2026-11-05). Source
   change 3 is therefore the one boot fix the base needs. The OLD jar of the savegame test is the
   base (`a6bcde1`) with only `src/main/java/invtweaks/` removed, built as above: SHA-1
   `237e0fc65275a0204fe7008f144c395207e41205`, 4557655 bytes. It reaches `Done` in the Arclight
   pack, and so does the jar of this change, both with the same three ERROR lines as the pack
   without the mod (the AllTheLeaks line of the boot error baseline and two lines that every boot
   of the fresh test copy shows) and no `Mixin apply failed`. This change also removes
   `mod/ClientRegistryHandler.java`. The base registered it on the Forge bus
   (`ActuallyAdditionsClient.java:97`), although its two listeners take the mod bus events
   `ModelEvent.RegisterGeometryLoaders` and `ModelEvent.BakingCompleted`. Upstream `f9897f0` moved
   it to the mod bus and `2e394d9` deleted it, since both listener bodies are commented out.
   Forge 47 accepts such a registration without a check (`MinecraftForge.EVENT_BUS` is a plain
   `BusBuilder` bus), so the listeners only never ran. Not needed on the base: `9ec20f4` (the base
   already registers its particle providers in `RegisterParticleProvidersEvent`,
   `ActuallyAdditions.java:115` and `159`) and `758001e` (a regression of the 1.21 line, the base
   wraps its client listeners in `DistExecutor`, `ActuallyAdditions.java:112`). The base still
   registers the fully commented `DungeonLoot` on the Forge bus (`ActuallyAdditions.java:105`),
   which is harmless and goes away with the loot modifiers (`4a16be4`).
