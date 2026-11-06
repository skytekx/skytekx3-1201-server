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
    `defaultRequire` 1 and the refmap. The mixins land with their features. `AnvilMixin` and
    `ItemCombinerMenuAccessor` of `6f56772` came with the Item Tag filter (source change 47), and
    `client.MinecraftMixin` of `3b29799` comes with the Engineer's Goggles. Before source change 47
    the annotation processor wrote no refmap, so the jar had none and Mixin logged one WARN line at
    boot that the reference map could not be read. The dev runs get the properties
    `mixin.env.remapRefMap` and `mixin.env.refMapRemappingFile`, so the mixins of the deobfuscated
    mod jars apply there. The two mixins of `main` touch nothing that Arclight patches:
    `AnvilMixin` injects at the head of `AnvilMenu.mayPickup` and, as a local addition of source
    change 47, at the head of `AnvilMenu.createResult`. Arclight's `RepairContainerMixin`
    redirects the `ResultContainer.setItem` calls in `createResult`, injects at its call of
    `broadcastChanges` and modifies two of its constants, none of them at the head, and its
    `ItemCombinerMixin` injects at the head of `ItemCombinerMenu.stillValid` and only shadows
    `inputSlots` (mutable, so Arclight may put its own container there) and `resultSlots`, which
    the accessor reads at each call (javap of `common.jar` in
    `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`). The client mixin `MinecraftMixin`
    injects at the head of `Minecraft.shouldEntityAppearGlowing`. No mixin uses `@Overwrite`.

## Source changes

These commits change the mod itself on top of the base. Each later commit that ports a fix or a
feature adds its item here, and the NOTICE in the jar sums them up.

1. Optional dependencies: `mods.toml` declares `patchouli` (`[1.20.1-81,)`), `curios`
   (`[5.2.0+1.20.1,)`) and `jei` (`[15.0,)`), each with `mandatory=false`, ordering `NONE` and
   side `BOTH`, so Forge checks the version range of each one that is present. That check is
   their only effect, since the mod list of Forge 47 shows no dependencies (`ModListScreen` of
   Forge 47.4.0 shows the name, version, credits, authors, URL, child mods, license and
   description). The Curios bound was `[5.0,)` until the integration of source change 49 raised
   it, see there. Upstream declares
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
   base (`6bcbead`) with only `src/main/java/invtweaks/` removed, built as above: SHA-1
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
9. Tags: the base shipped no `forge:` tag at all. The class `ActuallyTags` and the two tag
   generators now hold the tags of `main` with every `c:` tag mapped to its Forge 47 name, as the
   1.20.4 line still had them (`1d581fc`, `c952c77`, `9689b4c`, `af8f1f1`, `e37019a`, `8d56947`,
   `e3dd329`, `4789e61`). Black quartz is `forge:gems/black_quartz`, its ore
   `forge:ores/black_quartz` in `forge:ores` and `forge:ores_in_ground/stone`, its block and the
   twelve crystal and empowered crystal blocks `forge:storage_blocks/<name>` (#1418). Rice, coffee,
   canola and flax fill `forge:seeds/<crop>` and `forge:crops/<crop>` and through them `forge:seeds`
   and `forge:crops`, the rice slimeball `forge:slimeballs`, and the canola, flax and rice crop
   blocks `minecraft:crops` (#1471). The AIOTs join `minecraft:swords`, `axes`, `pickaxes`,
   `shovels` and `cluster_max_harvestables` (#1450). Forge 47 has no `forge:tools/pickaxes` and the
   like, since its `forge:tools` takes the vanilla tool tags. Stairs, slabs and walls join the
   vanilla block and item tags and the pickaxe tag. New in the mod's namespace are `crystal_blocks`,
   `lamps`, `stone_ore_replaceables`, `deepslate_ore_replaceables`, the block tag `worm_drop` with
   grass blocks, and `buckets/<fluid>` for the four fluid buckets, since Forge 47 has no common
   bucket tag for `main`'s `c:bucket/<fluid>` and `c:buckets`. The tag `forge:holds_items` keeps its
   Forge name. Tiny coal and tiny charcoal get no `forge:nuggets` tag, because AlmostUnified would
   unify them with coal and charcoal. AlmostUnified lists neither black quartz nor a crystal as a
   material, so these tags unify nothing of the mod's own items. The tag `worm_drop` has no reader
   yet (the worm change of `e37019a` comes with the worm fixes), and the Curios `charm` tag and the
   banner pattern tags come with their features. The 20 `forge:` files that the 1.20.4 line also
   generates match it byte for byte. The other 24 are the storage block tags of the crystals and
   empowered crystals, which came after that line (#1418, `c952c77`), and they match the `c:` files
   of `main` with the namespace changed. Two mods of the pack read the crop tags. Farmer's Delight
   1.3.4 cooks with `forge:crops/rice` in five recipes (`data/farmersdelight/recipes/cooking/`
   `cooked_rice.json`, `dog_food.json`, `fried_rice.json`, `mushroom_rice.json` and
   `stuffed_pumpkin_block.json`), so the mod's rice counts there. The flax seeds fill
   `forge:crops/flax`, as `main` puts them into `c:crops/flax`, and Supplementaries 3.1.44 makes
   its rope, sack and doormat from that tag (`data/supplementaries/recipes/rope.json`, `sack.json`
   and `doormat_2.json`), so the seeds count as flax in those recipes as they do upstream.
10. Recipes: the two recipe generators carry the crafting, smelting and stonecutting recipes of
    `main` for every block and item the base registers, which adds 111 files to the 299 of the
    base. They are 33 machine recipes, among them the generators, crushers, breakers, placers,
    phantom faces and boosters, the Feeder, Bio Reactor, Heat Collector, Energizer, Enervator,
    Experience Solidifier (with experience bottles, `9492973`), Greenhouse Glass, Lava Factory and
    Player Interface (`2040c68`, `1e32301`, `ae6695a`, `5a921e1`, `7c7a095`, `8abfa24`,
    `b69847d`, `3f35689`, `b68cfec`, `cad5721`, `9d76fad` and the recipe parts of `5182ac8`,
    `7bcebe6`, `adc276e`, `6886aca`, `8419cc6`, `17d8dcc` and `ccfb8d1`). Then the black quartz
    block, pillar and chiseled block (`e8df077`), smooth black quartz by smelting (`40012ae`), the
    sixteen lamps and the lamp controller (the recipe parts of `248011b` and `c09b365`), the 24
    compress and decompress recipes of the six crystal and six empowered crystal blocks
    (`8f73813`), the ring and rice dough (`2040c68`), rice paper (`cf80d17`), the Ender Star
    (`1440baa`), sticky pistons from any `forge:slimeballs` and slime blocks from rice slimeballs
    (`06ef714`, `f524ecc`), rice dough smelted to bread (`c29830a`) and black quartz ore smelted and blasted
    (`d1f77f3`). Walls, slabs and stairs give 6, 6 and 4 as vanilla does, and 24 stonecutter
    recipes cut them and the black quartz variants from their block. The black quartz wall, slab
    and stairs are made from the black quartz block instead of the pillar, whose own wall, slab
    and stairs used the same pattern (`d0f8c24`, `fc63fa0`, #1465). The stonecutting and smelting
    helpers save under the mod's namespace, since the vanilla helpers of 1.20.1 use `minecraft`.
    The invisibility upgrade keeps the base's `forge:glass/black` instead of the black stained
    glass that the 1.21 port of `main` switched to. Not here are the drill dye recipes (they need
    the keep data shapeless recipe of the drill fixes), the reset recipes of the machines that
    keep their contents, the Item Tag, the banner patterns and the compat recipes, which come with
    their features. Apart from those, the folder holds every top level, compress and decompress
    file of `main`, and each one matches its file on `main` except for the 1.20.1 form (the
    stonecutting count at the top and `forge:conditional` around the tiny coal recipes) and two
    SkyTekx fixes. The Lens of the Killer takes a `forge:partial_nbt` enchanted book whose
    `StoredEnchantments` hold Sharpness V. The base and the 1.20.4 line build that ingredient with
    `ItemStack#enchant`, which writes the tool tag `Enchantments`, into a strict `forge:nbt`
    ingredient. No villager, loot or anvil book carries that tag, so the lens could not be crafted
    in survival, although JEI showed the recipe. The branch `main` matches the stored enchantments
    through its data components. The partial match also takes books with a `RepairCost` or further
    enchantments. The slime block recipe `tagged_slime_block` takes the rice slimeball instead of
    `forge:slimeballs`. In the pack that tag also holds the sky, ichor and ender slime balls of
    Tinkers' Construct 3.12.1.231 and the maze slime ball of Twilight Forest 4.3.2508-skytekx.1,
    whose own 3x3 recipes make their slime blocks, and two matching recipes let automated
    crafters turn them into vanilla slime blocks. The vanilla recipe already covers the slime ball,
    so the rice slimeball keeps the purpose of `06ef714`. The sticky piston recipe keeps the tag,
    since its output is the vanilla one and no other recipe of the pack overlaps it.
11. Advancements: the generator writes the 18 advancements of `main` with the same ids, parents,
    icons and criteria. The fix of `50407e9` is the 1.20.2 `DisplayInfo` with an `Optional`
    background and has nothing to do on 1.20.1. The note about an advancement for the underwater
    treasure chest goes, since that content is gone (`863c2b7`). The eleven advancement titles in
    `en_us.json` are in title case (`4b3a6a0`, #1457). On the base the coal generator, leaf
    generator, coffee maker, crusher, double crusher and the three phantom faces had no recipe, so
    eight of the 17 children of the root could not be crafted. The recipes of source change 10
    give each of them a recipe, but the coffee branch of the tree stays out of reach for now.
    Coffee beans, canola, flax and rice have no source in this state, because the plant
    generation of the base is commented out (`mod/gen/AAWorldGen.java` lines 105 to 113, and
    `ActuallyAdditions.java:145` keeps `//AAWorldGen gen`) and no biome or loot modifier exists.
    So `pickup_coffee` and its child `craft_coffee_machine` cannot be earned, and the Coffee Maker,
    Canola Press, Fermenting Barrel and Oil Generator cannot be crafted in survival. They become
    reachable with the feature `plant-worldgen` of the bundle `aa-world-content`, which ports the
    biome modifiers `add_coffee`, `add_canola`, `add_flax` and `add_rice` of `main`
    (`neoforge/biome_modifier/` there).
12. JEI: the plugin registers the categories whose recipe types exist on the base, with the
    API of JEI 15. The fermenting, laser and empowerer categories move into their own packages
    as on the newer lines (`2f50f5e`), and their catalysts and recipes lose the comments that
    called them disabled. New is the category of the Lens of the Miner with its two lang keys
    and the lens as catalyst (`b866f50`). In the laser category the Atomic Reconstructor is a
    render only slot (`jei/laser/LaserRecipeCategory.java:69`) and stays the registered catalyst
    of the category (`JEIActuallyAdditionsPlugin.java:67`). In the mining lens category it is a
    render only slot as well, and the lens is the catalyst
    (`jei/lens/MiningLensRecipeCategory.java:72-73`). Upstream made this change so that EMI's
    recipe tree, which reads the JEI categories, no longer asks for one reconstructor per craft
    (`d1eb4ca`, `fa4ccab`, changelog 1.3.16). That effect is expected here too, but nobody has
    tested it yet. No client has opened these categories in JEI 15.74.0.233 or EMI
    1.1.24-skytekx.2 of the pack. The slots line up with the new
    `gui_nei_atomic_reconstructor.png` of `4a1e483` (PR #1508), the only change of that texture
    since 2016. The categories keep `getBackground`, since the switch to `getWidth`, `getHeight`
    and a drawn background of `31e2f47` follows JEI 19. The click areas of `53414c4` need the
    coffee category and the powered furnace fixes and come with them, like the coffee, crusher
    and pressing categories, the powered furnace catalyst and the energy subtype interpreter.
13. Dedicated server split: the look at overlays of the Atomic Reconstructor, the laser relays, the
    Lava Factory Controller, the phantom blocks, the Player Interface and the Vertical Digger leave
    their common block classes. Upstream `668f7ffe` moved them into client only `IBlockHud` classes
    in `mod/blocks/blockhuds/`, which each block hands out through `IHudDisplay.getHud()`. Here
    `IHudDisplay` is an empty marker, and `ClientEvents` registers the overlays by block class in the
    client only `BlockHuds`, so no common class holds an instance of a client class. The tick of the
    Engineer's Goggles moves from the item into `ClientEvents` (`844efe42`). The four clientbound
    packet handlers call the client only `PacketHandlerClient` through
    `DistExecutor.unsafeRunWhenOn(Dist.CLIENT)` instead of carrying `@OnlyIn` methods that a
    dedicated server strips (`844efe42`, `8fd9afe8`). The helper `PacketHandlerHelper` splits into
    `PacketHelperClient` and `PacketHelperServer` as in `8fd9afe8`, and the second one also sends the
    laser packet that `AssetUtil` sent. The laser relays build their mode lines as Components
    (`0226078a`, `d1ceffca`), and the other I18n calls of `3a767e18` and `d64b530f` go too. That also
    gives the menus of the Item Filter, the drill and the Advanced Laser Relay their titles, where
    the base looked up the missing keys `container.actuallyadditions.filter.name` and
    `container.actuallyadditions.drill.name`. The common `ItemCoffee.MilkIngredient.getExtraText` no
    longer calls I18n. A scan of the compiled classes found five more leaks, all fixed: an unmarked
    `AssetUtil.renderLaser` overload, the `FluidDisplay` accessors of `FermentingRecipe`, the break
    packet in `WorldUtil.breakExtraBlock`, the client listeners of the `ActuallyAdditions`
    constructor and `TileEntityPhantomface.renderParticles`, a client only method that the common
    `clientTick` called. The class `StringUtil` is client only as a whole. After the change an ASM
    scan of the build (every method without `@OnlyIn(Dist.CLIENT)` in every class outside the client
    classes) finds client references only in the lambdas that `DistExecutor` runs, and no call from
    common code to a member that a dedicated server strips. Not part of this change: the Patchouli
    guard of `a20c25eb`, which comes with `PatchouliPages` and the Patchouli manual, the filter
    buttons of the whitelist screen in `3a767e18`, which need the `FilterSettingsGui` rework of the
    sacks and filters, and the Curios rule, since the base calls no Curios API (the Crafter on a
    Stick hotkey keeps its calls in a compat class).
14. Packet hardening: a local deviation, since upstream `main` still trusts the client (its
    `ServerPayloadHandler.handleButtonToTile` and siblings read the dimension, the position and the
    player id from the packet). The base took `WorldID`, `X`, `Y`, `Z` and `PlayerID` from the GUI
    packets, so any client could press buttons on any machine in any dimension as any player, load
    and generate chunks through `getBlockEntity`, throw a `NullPointerException` with an unknown
    `WorldID` and a `ClassCastException` with the id of an entity that is no player. Now the sender
    is the only player, a packet whose `WorldID` or `PlayerID` names another level or another entity
    is dropped, and the block must sit in a loaded chunk within 8 blocks of the sender, the reach of
    `TileEntityBase.canPlayerUse` that every machine menu checks in `stillValid`. The distance check
    runs before any chunk access. Spectators cannot press buttons, as with vanilla container clicks,
    and a number that is not finite is dropped. The handler list `DATA_HANDLERS` keeps its order and
    indices, the wire format of the base, but each handler is now registered as serverbound or
    clientbound (`addServerbound`, `addClientbound`), and `PacketClientToServer` and
    `PacketServerToClient` only decode the handlers of their direction. The base decoded any index
    in both directions, so a client could name a clientbound handler, which threw an
    `AbstractMethodError` on a dedicated server and ran client code with the guest's NBT on a LAN
    host. The handler `PLAYER_DATA_TO_SERVER` keeps its index 8 but does nothing (the base let any
    client overwrite the booklet data of any online player by UUID, and `main` reads nothing from it
    any more). A failing handler logs a WARN that names the player instead of vanishing in the
    future of `enqueueWork` (`main` disconnects the player instead). Each dropped packet logs one
    WARN line that names the sender, within the limit per sender of source change 17. The
    `PROTOCOL_VERSION` of the channel `actuallyadditions:main` goes from 4 to 5, so a client of the
    base is refused at login. That rests on the version check of Forge's `SimpleChannel`
    (`PROTOCOL_VERSION::equals` on both sides), and nobody has logged in with a stock 1.1.52 client
    yet, so the clean mismatch disconnect is not verified. Of `9d166ac9` only the split of client
    and server handlers is taken, its typed payloads are not ported. The firework box clamps its
    numbers itself since source change 17, because a finite number out of range crashed its tick.
    The lower bound of the button id of the XP Solidifier stays with that feature
    (`TileEntityXPSolidifier.java:208`). The Vertical Digger, the Ranged Collector and the sack menu
    already ignore unknown button ids. Later handlers (the hotkey of the Crafter on a
    Stick, the Item Tag filter) go through `addServerbound` without another protocol bump, since no
    build ships in between. The dev only test mod `aadevtest` in `src/devtest` (a source set that
    only the client dev run loads, the jar task packs `sourceSets.main` alone) drives these tests
    from a dev client against a dedicated server, see its class `AADevTestClient`. A test run
    against the jar of this change on a dedicated Forge 47.4.18 server (with JEI, Patchouli, Curios,
    The One Probe and Mekanism, the mods of the dev runs) logged one WARN for each crafted packet
    and kept running: the indices 0, 1, 5, 7, 99 and minus 1, an unreadable packet, a press 20
    blocks away, one at the unloaded position x and z 29999000, one with the `WorldID` of the
    Nether, one with the entity id of a pig, one with an unknown and one with an invalid `WorldID`,
    and the player data of another UUID. The distance check dropped the far positions before any
    chunk access. The same run placed, right clicked, wrenched and broke the blocks in id order on
    that server without a `NoClassDefFoundError` or an invalid dist error, up to the Hopping Item
    Interface: the base lacks its `facing` property, so its first server tick crashes the server
    (`TileEntityItemInterfaceHopping.java:129`), which the port of the item interfaces fixes. The
    harness skips it until then.
15. Menu validity: the drill, the Item Filter and the firework box returned `true` from
    `stillValid`, so their screens never closed. The drill menu read its upgrades from
    `inventory.getSelected()` when it opened and wrote them back into `inventory.getSelected()` when
    it closed (unchanged on `main`). A client that switched the selected hotbar slot by packet while
    the menu was open got the remaining upgrades written into a second drill, while the first drill
    kept every upgrade the player had taken out, a duplication. Now `ContainerDrill` remembers the
    hotbar slot and the drill stack of the moment it opened, `stillValid` fails once the selected
    slot changes or the slot holds another stack or none, and the upgrades go only into the
    remembered drill. Since a drill could also be copied while the menu is open (a crafted drop of
    the selected item splits a copy off the stack), every change of the upgrade slots is written
    into the drill right away and not only at close. The keys of the drill NBT (`Items`) do not
    change. `ContainerFilter` gets the same remembered slot and stack, which keeps its ghost items
    out of another filter. `ContainerFireworkBox` now knows its box, as on `main` (the field
    `fireworkbox`, the client reads the position that `BlockContainerBase.openGui` sends), and
    checks `canPlayerUse`, the 8 block reach of the other machine menus. The existing
    `SlotImmovable` on the drill's own hotbar slot and the `SWAP` check stay. Upstream `f2596f97`
    (the block position in `openMenu`) and `82aaacf1` (`getBlockPos()` in `canPlayerUse`) are
    already in effect on the base, which opens its menus through `NetworkHooks.openScreen(player,
    tile, pos)` and reads the same field. The test mod gains the drill, filter and walk away tests.
16. Player data: the clientbound `SYNC_PLAYER_DATA` read `context.getSender()`, which is null for a
    clientbound packet, so the client never got its player data (the bat wings state) and logged the
    ERROR "Tried to receive Player Data for the current player" at each login. The client handler in
    `PacketHandlerClient` now takes the local player, as upstream's
    `ClientPayloadHandler.handleSyncPlayer` takes the player of the clientbound flow.
    `PlayerData.getDataFromPlayer` cast every level to `ServerLevel` and threw a
    `ClassCastException` for client code, and now hands the client its own `PlayerSave` as upstream
    `2a4e9666` does (a new one when another player logs in on the client). The bookmarks, the trials
    and the tutorial flag of the 1.12 booklet are gone from `PlayerSave`, as upstream `c8e90aef`
    removes them, and so is the booklet part of `PacketHelperClient.sendPlayerDataToServer`. The
    world save keeps its name `actuallyadditions_worldsave` and its keys `Networks` and
    `PlayerData`, and a player entry keeps `BookGotten`, `HasBatWings`, `BatWingsFlyTime` and, in
    the sync packet, `ShouldDisableWings`. An old save with the keys `Bookmarks`, `Trials` and
    `DidTutorial` loads unchanged, the keys are ignored and drop out at the next write. `WorldData`
    itself needs no change, its `load` and `save` already have the 1.20.1 form without a
    `HolderLookup`. The test of the source changes 13 to 16 ran the test mod against the jar of this
    change on a dedicated Forge 47.4.18 server and on the pack's Arclight
    (`arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`), each with JEI, Patchouli, Curios, The One
    Probe and Mekanism. On both the client received its player data at login without the ERROR, the
    15 crafted packets of source change 14 got one WARN each, the valid press toggled the near
    crusher while the far one and the one in the Nether kept their state, and a button id of minus 1
    on the XP Solidifier logged its `ArrayIndexOutOfBoundsException` as one WARN. After the slot
    switch the drill screen and the filter screen were closed, the first drill still held the
    upgrade that stayed in it, the second drill had no upgrades and the taken upgrade lay in the
    inventory. Every machine screen that opened was closed by the server once the player stood 10
    blocks away (17 on Forge, 22 on Arclight). The run placed, right clicked, wrenched and broke 122
    of the 125 blocks and used all 107 items with no ERROR line, no exception and no crash on the
    server. The three blocks left out crash the base: the Hopping Item Interface the server (source
    change 14), the Ranged Collector and, by its code, the Advanced Item Laser Relay the client,
    whose screens tick a filter GUI that the base never builds (`GuiRangedCollector`,
    `GuiLaserRelayItemWhitelist`). The ports of the ranged collector and of the sacks and filters
    fix those. A short savegame test built a coal generator with coal, two energy laser relays 5
    blocks apart and a crusher with the OLD jar `237e0fc6`, wrote a network between the relays and a
    player entry with the booklet keys into the world save in the format of the base, and loaded the
    world with the jar of this change: the crusher held 4180 CF that only the network could have
    brought, and the save still held the network. A player entry that the jar of this change wrote
    holds `BookGotten`, `HasBatWings` and `BatWingsFlyTime` and no booklet keys.
17. Review fixes of the server safety bundle: the review found that one finite number packet still
    crashed the server through the Firework Box. `onNumberReceived` stored any number, so a type
    chance of minus 1 made `Weight.of` throw "Weight should be >= 0" in `getRandomType`, and a Value
    Play of 2e9 overflowed the bound of `Mth.nextInt` in `getRandomWithPlay`. Both throw in the tick
    of the block entity or in a neighbour update, and since the box saves the values as `Play` and
    `TypeChance0` to `TypeChance4`, the world crashed again after every restart. Upstream `main`
    clamps nothing either (`TileEntityFireworkBox.onNumberReceived`). The box now clamps every
    number to the slider ranges of `main`'s `GuiFireworkBox` (lines 48 to 61): Value Play 0 to 5,
    charge amount 1 to 4, flight time 1 to 3, color amount 1 to 6, area 0 to 4 and the seven
    chances 0 to 1. An unknown id changes nothing, and a change calls `setChanged` before
    `sendUpdate` as on `main`. `readSyncableNBT` applies the same clamps, so a box that already
    holds bad values or a crafted save loads safely, and the NBT keys stay the same. The WARN lines
    of source change 14 had no limit, so a modified client could fill the log and the disk, since
    neither the pack nor Arclight limits the packet rate. `PacketHandler` now logs at most 20 WARN
    lines per sender in 60 seconds, then one line that the rest is suppressed and, with the next
    WARN after the window, one line with their count. The Item Laser Relay puts its two compass
    lines on one line with a separator, as upstream `cf0349b1` does, because `LaserRelayHud` draws
    the text with one `drawString` call. On the client only the sync packet of the local player keys
    the client copy of the player data (`PlayerData.getClientData`), and any other player of the
    client level gets a throwaway `PlayerSave`, so a remote player in a tick event cannot replace
    the synced bat wings state once that tick is enabled. The test mod sends a type chance of
    minus 1 and a Value Play of 2e9 to a Firework Box with 1000 CF, reads the stored values after
    its next shot, and then sends 30 packets with the unknown handler index 99.
    The test mod ran against the jar of this change (SHA-1 `3855717cd50ea85d3e11971532235d93d5e3832f`,
    4616133 bytes, two builds) on a dedicated Forge 47.4.18 server and on the pack's Arclight
    safeiter2, with the mods of source change 16. On both
    the box held `Play` 5 and `TypeChance0` 0.0 after the two packets, fired at its next shot (its
    energy went from 1000 to 500 CF), logged no WARN for the two numbers and kept running. On
    Arclight the 30 flood packets after the 16 WARN lines of the other crafted packets gave 4 more
    WARN lines and one line that the rest is suppressed (the Forge run predates the flood case). The
    other results match source change 16: 122 of the 125 blocks and all 107 items ran with no ERROR
    line and no crash on the server, the client got its player data at login, and every machine
    screen that opened was closed by the server 10 blocks away (21 on Forge, 22 on Arclight).
18. Textures, models and sounds: the 1.20+ retexture of upstream's texture artist replaces the art
    of the base. That is Retexture 2.0 (`9cf9601`) and 2.1 (`6542628`) with the crops (`c0dfed8`),
    lenses and snail (`a60d11d`, `6bd17fd`), crystals (`9d83857`), batteries (`f8fa256`), drills
    (`d136820`) and the worm (`4721f51`). The hand written block models are the ones of `main` too,
    which brings the texture fix of the firework box (`fbdfc2e`) and the cull faces of the hopping
    item interface (`9b6dd7d`, #1401) and of the greenhouse glass (`817797a`). Two blocks still do
    not show their new model in the world. The Greenhouse Glass keeps the `RenderShape.INVISIBLE` of
    the base until the greenhouse glass port brings the Java part of `817797a`, so only its model
    and cull faces came here. The Bio Reactor has no blockstate, because `BlockStateGenerator` keeps
    its line commented out until the generators port gives the block its facing (`822913f`), so the
    world block shows the missing model and a resource reload logs an error for it. Every file in
    `textures/block/` and `textures/item/` is now byte identical to `main` at `cd9d9c6`. Eight files
    of `main` stay out: the four banner pattern items (`659779b`, they come with the banner
    patterns), `item_tag.png` (`79b2084`, it comes with the Item Tag filter), and `youtube.png`,
    `black_quartz_deepslate_ore.png` and the animation file of the deleted
    `double_crusher_rotation_animation.png`, which nothing on `main` uses. The base's
    `paper_cone.png` stays, since the backport keeps the item. The six crystal cluster models keep
    the state of `6542628` until source change 20 brings the fixed models of `9282dcb`, and the lamp
    controller model keeps it until source change 21. No model uses a key that 1.20.1 does not read.
    A scan of all 403 model files finds only the top level keys `parent`, `textures`, `elements`,
    `display`, `ambientocclusion`, `render_type`, `overrides`, `loader`, `fluid`, `credit` and
    `texture_size` (the last two are Blockbench notes that every version ignores), and only `from`,
    `to`, `rotation`, `shade` and `faces` in the elements. The AIOTs show their art again: on the
    base all thirteen `*_aiot.png` were the same 291 byte placeholder and the art sat in unused
    `*_paxel.png` files, which `908a57b` renames. Rice, flax and coffee have four stage textures
    like canola, and `BlockStateGenerator` maps the eight ages to them as `19afc5f` does (age 0
    stage 1, ages 1 to 3 stage 2, ages 4 to 6 stage 3, age 7 stage 4). The base wrote the models
    `<crop>_2` and `<crop>_3` twice with different textures, so the last write won and the stage 2
    and 4 textures never rendered. The renderers follow the new models: the lens in the Atomic
    Reconstructor sits at z 0.45 (`a60d11d`) and the worm at y 0.75 (`4721f51`). The coffee machine,
    crusher and reconstructor sounds are the mono files of `52b5765`, since Minecraft positions only
    mono sounds in the world. Gone are the 1.12 leftovers that no 1.20.1 loader reads and that
    shipped in the jar: `blockstates_old/` (98 files), `models_old/` (271), `textures/blocks_old/`
    (229), `textures/items_old/` (249), `textures/models_old/` (20), the three loot tables in
    `assets/actuallyadditions/loot_tables/` (loot tables only load from data packs, and the Engineer
    house brings the data copy of `engineer_house`) and `structures/andrew_period_house.nbt`
    (DataVersion 819, a 1.12 palette with `block_grinder` and `minecraft:log`), and the two dead
    files that `4ef880a` deleted: the 1.12 `mcmod.info` at the jar root and the creative tab
    background
    `assets/minecraft/textures/gui/container/creative_inventory/tab_actuallyadditions.png`, which no
    tab names, since `ActuallyTabs` sets no background. Of the 632 block and item textures of the
    base, 340 are gone (34 are new and 259 of the 292 kept ones changed). No model, blockstate,
    particle file or Java string names them, and `60ec534` deleted the same files on `main` after
    the retexture had moved them into `unused/` folders. The animation files of the six crystal
    items go with them, since the new crystal textures have a single frame. Three hand written
    models that no blockstate or item names and that point to textures which do not exist go too:
    `double_powered_furnace`, `green_block` and `white_block` (`main` still carries them). The
    texture folder shrinks from 1211 to 407 files. Nothing to port from `d49d29b`, which fixed an
    animation file of the deleted `blocks_old/`, or from `9eec468`, which adds two logo pictures to
    `pics/` that the jar does not carry. License: the retexture came into the MIT repository after
    the relicensing (`9cf9601` is dated 2024-04-03, a month after `33f5541`), through the pull
    requests #1395 and #1396, the Modrinth project lists the artist in its team and declares MIT,
    and neither ref has a separate asset license. Nobody confirmed this with the artist. The credits
    line in `mods.toml` names the porters and the 1.20+ texture artist with the wording of
    `4ef880a`, as `main` does. The `runData` output of this change differs from the committed one
    only in the nine crop models and the `.cache` file of the blockstate provider.
19. Block shapes: `mod/blocks/VoxelShapes.java` is the file of `main` at `cd9d9c6`, and a block
    overrides `getShape` exactly where `main` does. The retexture models of the crusher, double
    crusher, energizer, enervator, farmer, feeder, fluid collector and placer, item interface,
    lamp controller, lava factory controller, leaf generator, long range breaker, player
    interface, powered furnace, ranged collector, vertical digger and experience solidifier fill
    the block, so `6686f1f` and `9cf9601` dropped their detailed shapes and these blocks use the
    full cube. The base still gave them shapes of the old models. The coal and oil generators
    share `SIMPLE_GENERATOR_SHAPE` (`6686f1f`, `2bea730`), the display stand and the empowerer
    `SIMPLE_STAND_SHAPE` (`2bea730`), and the Phantom Booster is one 6 by 16 by 6 pillar (`6686f1f`,
    `2cadd9e`). The canola press, battery box, fermenting barrel, firework box, greenhouse glass,
    heat collector, hopping item interface, shock suppressor, coffee machine and laser relays get
    the shapes of their new models (`4721f51`, `9cf9601`). The coffee machine and the laser relays
    build their other facings from one shape with rotation helpers (`2bb53e5`, `00d2dc6`) instead
    of hand written copies, which let `a2ab76c` cut the file from 1816 to 354 lines. The nested
    class `VoxelShapeUtils` holds those helpers. Upstream's comment in the file says they are
    taken from Mekanism (MIT, commit `46bc2dc` of Mekanism), so the NOTICE carries Mekanism's
    copyright and permission notice. The class `CrystalClusterShapes` gets its reader with source
    change 20. Not ported from `6686f1f`: its change to `AllInOneTool.useOn`, which fixes the AIOT
    till and flatten code of `4d3ba55` that comes with the AIOT fixes.
20. Crystal clusters: a cluster grows out of the face it is placed on, as on `main` since 1.3.21
    (`b8e1cd6`). The block already had the property `facing`, but the base set it from the look
    direction of the player and its blockstate drew the one upright model for every state. The
    blockstate generator now writes the six facings with the rotations of `fullyDirectionalBlock`,
    the same files as `main`, and `getShape` uses the six rotations of
    `VoxelShapes.CrystalClusterShapes` (source change 19). A cluster needs a sturdy face behind it,
    so it cannot be placed against air or a slab side and breaks when that support goes
    (`745bb6c`). On 1.20.1 `canSurvive` and `updateShape` are public methods, `main` overrides the
    protected ones of 1.20.5 and later. The six block models are the ones of `9282dcb`, which
    share the new parent `raw_ore_template` with its credit line of `13897fa` and give each crystal
    its palette texture. Broken without silk touch, a cluster drops two to eight shards of its
    crystal with explosion decay, with silk touch the cluster itself (`09592c6`). The base dropped
    the cluster in every case. The loot tables differ from `main` only in the 1.20.1 form of the
    silk touch predicate. Savegame: no property changes. A cluster that the old jar placed keeps
    its stored facing, so one placed while looking down (facing up) stands as before, and one
    placed while looking sideways now hangs on the side its facing names. No world with the mod
    existed before this change, so there is no migration.
21. Lamps: the sixteen lamps show their unlit texture again (`248011b`, changelog 1.3.13). The
    base wrote the model `lamp_<color>` twice in `buildLitState`, first with the unlit and then
    with the lit texture, so the second write won and a lamp looked lit in both states. The
    generator now writes `lamp_<color>` and `lamp_<color>_on`, and the blockstates point the
    state `lit=true` at the second one. The model of the Lamp Controller is the one of `248011b`,
    which turns its front to the top of the model, the side that the rotations of
    `fullyDirectionalBlock` expect, so the controller faces the player who placed it. Before, its
    front pointed north in the model and ended up on a side or the bottom. A lamp toggled with an
    empty hand now switches on the server only and sends a block update to every client. The base
    set the new state on both sides with `UPDATE_INVISIBLE` alone, so the server never sent it and
    other players kept the old model while the light level changed. Upstream `main` still has
    that line (`useItemOn`), which the lit and unlit models made visible. A dye now recolours the
    lamp with the dye of the hand that used it and ends the click. The base took one item from the
    selected hotbar slot and passed the click on, so with a dye in the off hand the item in the
    main hand was lost and the dye stayed (`main` keeps these lines in `useItemOn` too). The Lamp
    Controller walks the connected lamps breadth first with a hash set, skips unloaded positions,
    sets only the lamps whose `lit` differs and stops after 4096 lamps. The base recursed once per
    lamp and kept the visited positions in a list that never held the checked offsets, so a field
    of a few thousand lamps on a redstone clock lagged the server and could overflow the stack in
    a neighbour update. `main` has the same walk. Both are local divergences. The recipes of the
    lamps and the controller and the tag `actuallyadditions:lamps` came with source changes 9 and
    10 (`248011b`, `c09b365`), so the name of the controller loses its `(wip)` label here, as in
    `248011b`. The `runData` output has 16 new and 32 changed files, and all 49 lamp and controller
    files of `src/generated` are byte identical to `main`.
22. Block sounds: the wood casing sounds like wood and the iron, ender and lava factory casings
    like metal (`ec7b26c`). The Bio Reactor, coal and oil generators, coffee machine, both
    crushers, display stand, empowerer, energizer, enervator, farmer, lava factory controller and
    powered furnace use the metal sound (`43312e2`, #1429), the crystal blocks the amethyst block
    sound and the clusters the amethyst cluster sound (`48cd171`, #1427). The base gave all of
    them stone, the clusters glass. The helper `ActuallyBlocks.miscBlockProperties` is a method
    that returns a fresh `Properties` for each block, as in `a69eaed` (#1441): the base shared one
    instance, and once a casing sets its sound on it, every block built after it would get that
    sound too.
    The vanilla shape tags of this item of the plan (`8d56947`, `e3dd329`, changelog 1.3.8) came
    with source change 9, and the six files `minecraft:stairs`, `slabs` and `walls` in the block
    and item tags already match `main`. The one difference left in `minecraft:mineable/pickaxe`,
    the Fluid Placer (`07c59fb`), comes with the fluid collector and placer fixes. No data file
    changes, and no block, item, property or NBT key either.
23. Breakers, placers and the Vertical Digger: the drop chance helper
    `WorldUtil.fireFakeHarvestEventsForDropChance` posts the break event of the shared fake player
    and answers 1 when the block can be harvested with a Netherite AIOT in hand, as on `main`
    (`17d8dcc`, `902e806`, #1425). The base always answered 0, so the Long-Range Breaker and the
    Vertical Digger mined nothing. On Forge 47 `BlockState#canHarvestBlock` posts the harvest check
    that `EventHooks.doPlayerHarvestCheck` posts on `main`, and the fake player gets its previous
    item back afterwards, where `main` leaves the AIOT in its hand. The Breaker breaks through the
    same helper (`217fc73`, PR #1515), so it now breaks snow and other blocks that need a tool. It
    still leaves water, lava and Forge fluid blocks alone, as on 1.12, where `main` lost that
    check. The Breaker runs its work timer in the instance tick (`a117dd5`), which the Placer also
    reaches, so the Placer places again, and `WorldUtil.useItemAtSide` asks the block in front with
    `canBeReplaced` instead of a fixed false (`a117dd5`). The Digger checks the right tool with a
    Netherite AIOT (`e91c156`, #1421, `902e806`), mines the blocks and items of `forge:ores` in ore
    mode, reads its two block lists from the common config (`17d8dcc`, the base read the 1.12
    enum, whose lists are null) and has its Mode and Reset buttons back. The Phantom Breaker ticks
    with the ticker of the Phantom Placer, whose work timer its own ticker never ran (`ccfb8d1`),
    the Phantom Placer has its side button with the tooltip, and both open their screens. Unlike
    `main` the phantom block passes the click on when the player holds a Phantom Connector or
    when no screen opens, so the connector binds without sneaking and blocks can still be placed
    against a phantomface. The redstone toggle of the Breaker, Placer, Dropper, Firework Box,
    Ranged Collector, Long-Range Breaker, Atomic Reconstructor and phantom blocks ends the click
    (`5ce4f9a`). The check for a bound position no longer compares the level with itself
    (`d088aba`). The Long-Range Breaker, the Vertical Digger and the phantom blocks show their
    titles, and their names lose the `(wip)` label. The Vertical Digger has its name under its
    registry id `vertical_digger`, the base had it under the unused key `block.actuallyadditions.miner`.
    Savegame: the Digger keeps `CheckX`, `CheckY` and `CheckZ`. `CheckY` now means not started
    at the min build height minus 1 and done at the min build height, as in `ae6f160` (PR #1514).
    The base used -1 and 0, which are real layers below Y 0. The additive key `CheckYMin` holds the
    min build height that `CheckY` was written against. A digger without it, one saved by the base
    or a fresh one, starts again from its own Y minus 1 once and then writes the key, and a saved
    run continues at its layer after a restart. The Digger marks its block entity changed at each
    new layer, since the chunk was not saved again while it only passed air, so a restart went
    back to an older layer (seen on the test server, a local fix). This does not copy `738f098`, which sets `CheckY`
    to the min build height minus 1 at every load and so restarts every digger at each chunk load.
    The look at overlay of the Digger reads the new states, where `main` still tests 0 and -1.
    The Phantom Breaker computes the drops at the bound block with that block's own block entity,
    breaks through the same drop chance helper as the Breaker and leaves fluid blocks alone. The
    base and `main` used the position and the block entity of the Phantom Breaker itself, so a
    bound shulker box lost its contents and `copy_nbt` read the Phantom Breaker. The Long-Range
    Breaker leaves water, lava and Forge fluid blocks alone too, where the base and `main` deleted
    every source in its line, since a fluid needs no tool. `WorldUtil.useItemAtSide` asks
    `canBeReplaced` with the fake player in the place context and inside its `try`, and it hands
    back a copy of the stack in the hand and then empties the hand of the shared fake player.
    The base passed a null player outside the `try` and handed back the very stack that the fake
    player held. These three are local changes of the integration review. Left for the Arclight
    hardening: the named fake player in place of the shared `[Minecraft]` one.
24. Phantomfaces, the Player Interface and boosters: a phantomface hands out a `LazyOptional` of
    its own for the handler of its target. It invalidates it when the target, the block at the
    target or the range changes and when the face is removed or unloaded, and it follows the
    invalidation of the target's own one, so it never hands out an invalidated one again. This is
    the Forge 47 form of the capability invalidation of `ae6695a` and `b8e284d`, which call
    `invalidateCapabilities` on NeoForge. The base returned the target's `LazyOptional` itself,
    so a pipe that cached it kept feeding the old target after the face was bound elsewhere. The
    face also remembers the block entity whose handlers it holds and drops them, at the next
    request and in its own tick, when the block entity at the target is another one or is
    removed. The block entities of the mod never invalidate their own handlers, so after the
    target chunk unloaded and loaded again the face would have kept the handlers of the removed
    copy, and an Energyface or Liquiface would have drained energy or fluid that the reloaded
    copy still held (a local fix of the integration review). The
    look at overlay of the faces draws its wrapped lines again (`ae6695a`), since the base's
    `StringUtil.drawSplitString` draws nothing, and the range lines keep their green and red. The
    Player Interface keeps one `LazyOptional` per player and invalidates it when the player
    changes, leaves the range, logs out or respawns and when the block goes away (`44e0554`). The
    base made a new one at every call whose supplier returned null without a player, so asking it
    for its handler threw. It reads the key `Player` that it writes (`d2d0afa`). The base looked
    for `PlayerLeast`, which `putUUID` never writes, so it lost its player at every load. The
    Player Probe checks for the key `UUID` that it writes (`d2d0afa`), where the base looked for
    `UUIDMost` and never bound an interface. Both still read the old long pairs `PlayerMost` and
    `PlayerLeast` and `UUIDMost` and `UUIDLeast`, and the probe clears both forms. The Player
    Interface compares the squared distance with the squared range, so its range is 32 blocks
    as on 1.12 and in the manual. The base and `main` compare it with the plain range, about 5.7
    blocks. This is a local divergence. The Phantom Booster keeps its block entity, so that the
    type `actuallyadditions:phantom_booster` stays registered, but it no longer ticks, since the
    faces only look for the block (`ae6695a` removed the block entity). Its shape of `2cadd9e`
    came with source change 19, and the recipes of the faces and the booster of `ae6695a` and
    `5a921e1` with source change 10. The faces, the booster, the Player Interface, the Phantom
    Connector and the Player Probe lose the `(wip)` label. Savegame: no key, id or state changes.
    A Player Interface saved by the base stays bound to its player.
25. Fluid Collector and Fluid Placer: the work timer lives in the instance tick (`e4fdc6d`), which
    the ticker of the Fluid Placer also reaches, so the placer places again. The placer no longer
    drains a bucket into a source block that is already in front of it (`f524ecc`). The base
    only checked for Forge fluid blocks, which vanilla water is not. The collector empties a full
    water or lava cauldron into its tank with the bucket sound and leaves an empty cauldron
    (`ea1a072`, #1440). Unlike `main` only the collector does that, since `main` also lets the
    placer take from a cauldron. The cauldron classes are the same on 1.20.1. A fluid without a
    block, such as the potions and the tea of Create, stays in the tank of the placer, where the
    base and `main` set air and voided a bucket every 15 ticks (a local fix). The Fluid Placer
    joins `minecraft:mineable/pickaxe` (`07c59fb`), the one difference to `main` that source
    change 22 left in that tag, so a pickaxe breaks it at its normal speed and it drops itself.
    The provider `BlockTagsGenerator` lists it, and the generated tag file and its hash in the
    cache of the provider match. Savegame: no key, id or state changes.
26. Farmer: the configure item of the common config (`farmerConfigurator`, a compass by default)
    cycles the work area through 1, 3, 5 and so on up to `farmerArea` and starts the sweep again,
    and the look at overlay `FarmerHud` shows the area and how to change it (`b66af84`). The
    overlay is a client only `IBlockHud` that the `ClientEvents` constructor registers, and the
    three lang keys of `b66af84` come with it. The Farmer leaves soil alone that already holds the
    plant, any `FarmBlock` as in `b66af84` and modded farmland through Forge's `canSustainPlant`,
    and tills dirt with the hoe item (`b26c47d`, #1522). Forge's `getToolModifiedState` asks the
    block, so a modded soil turns into its own farmland, for example the rich soil of Farmer's
    Delight into rich soil farmland, where the base set vanilla farmland on every dirt. The hoe is
    unbreakable, since it now wears like a real one, the energy of the tilling is only used when
    the soil changed, and the hoe plays the till sound once. Worms do not drop when a fake player
    tills (`b26c47d`), and the worm check reads the level of the event, so a tilling without a
    player no longer throws (`a65dccf`, #1480). A planted crop makes its place sound (`bfd8eb8`). Nether wart is planted
    again (`124cac1`): the base cast the item `Items.NETHER_WART` to `IPlantable`, which threw at
    every try. Potatoes and carrots already worked on 1.20.1 through Forge's `IPlantable`, so
    `b23ba8f` (#1464) only adds its fallback for a seed or crop item whose block is no
    `IPlantable`, and the harvest puts those items back into the seed slots as well (`690aa4b`).
    Savegame: the additive key `Area` holds an area set with the configure item. A farmer without
    it works the area of the config as before, and the client gets the area that the server
    works.
27. Atomic Reconstructor and lenses: the renderer of the reconstructor returns when the block at
    its position is no longer a reconstructor (`713568d`), which crashed the client when it read
    the facing of another block. Like upstream it asks the level for that block. The first port
    asked the block entity for its cached state, which stays a reconstructor while a section that
    is not rebuilt yet still renders the block entity of a reconstructor that the client has just
    broken, so breaking one in survival still crashed the client (found by the test of item 45).
    Every laser of the server is a beam particle now, the
    reconstructor's too (`337fbfb`): the particle has its own render type with additive blending
    (`fb80bf3`, `58b9cbc`), and `AssetUtil.renderLaserParticle` draws the tube. Item 44 replaces
    the format position, colour and lightmap and the begin and end for each beam that this item
    first had with the position and colour format of `main` in one buffer. The base wrote the
    particle into the tesselator without beginning it, which throws, and spawned no particle for
    any laser but the reconstructor's, whose beam its block entity renderer drew. The particle
    leaves out the texture coordinates of `main`, which point at the white sprite of Forge in a
    format that has no texture, and it only spawns when the client has a level and a player, where
    `main` can read a null player. The lens sits in the middle of the block again (`337fbfb`).
    Death by the Lens of Certain Death shows its five messages again (`c995908`): the message id
    already holds the mod id, so the base looked for keys with the mod id twice, and the lang keys
    are now `death.actuallyadditions.atomic_reconstructor.1` to `.5` as on `main`. The flavour name
    in the tooltip of the reconstructor item reads the level of the tooltip, which can be null
    (`ae60e77`, #1415), so the common class no longer names the client class `Minecraft`.
    Lenses stack to 64 (`c0e0480`), and a stack of the same lens with the same tags takes the
    lens out of the reconstructor. The slot holds one lens, so a hopper cannot fill it with a
    stack. The Lens of Color keeps the size of an item stack it converts (`1316849`, #1477), the
    base gave back one item for a whole stack. The Lens of the Miner reads the ores of stone from
    `actuallyadditions:stone_ore_replaceables`, of deepslate from
    `actuallyadditions:deepslate_ore_replaceables` and of netherrack from
    `forge:ore_bearing_ground/netherrack`, the Forge name of `main`'s
    `c:ore_bearing_ground/netherrack` (`af8f1f1`). The base used `forge:stone`, which holds
    deepslate and tuff too, so deepslate got the stone ores as well. It gains copper and black
    quartz ore in stone (`6005bee`, `d9cae5b`, `e1cd1b4`), ancient debris in netherrack
    (`f471f4f`) and the eight deepslate ores (`482fc7a`, `d9cae5b`) with `main`'s weights. The 20
    generated recipes and the hashes in the cache of `MiningLensGenerator` are written by hand to
    match the provider. The result of a mining lens recipe reads an optional `count`, the 1.20.1
    form of `main`'s item with count codec (`08b4f92`, `7fd9c10`). The ores of other mods are hand
    written recipes in `src/main/resources/data/actuallyadditions/recipes/mininglens/`, each with
    a `forge:mod_loaded` condition, so Forge skips them when the mod is missing and JEI never
    lists them (`e1cd1b4`, `911356a`, `82c99e0`). They are the five Mekanism ores (tin, osmium,
    fluorite, lead and uranium, checked in Mekanism 1.20.1 10.4.16.80 of the pack), the silver
    ore of Immersive Engineering (`immersiveengineering:ore_silver`, checked in 10.2.0-183 of the
    pack), the uraninite ore of Powah (checked in the Forge jar `Powah-5.0.11.jar` for 1.20.1 from
    Modrinth, SHA-1 `f6dca27e`) and the inert crystal ore of ElementalCraft (checked in
    `elementalcraft-1.20.1-6.0.1.jar` from Modrinth, SHA-1 `602f902e`), all with `main`'s weights. Left out
    are the sets of Elements of Power and XyCraft, since no 1.20.1 jar of them could be checked,
    and of Modern Industrialization, whose 1.20.1 builds are for Fabric only. AlmostUnified turns
    the Mekanism tin and lead and the Immersive Engineering silver results into the ores of its
    priority mod, as it does with other recipes. The Lens of the Miner loses its `(wip)` label.
    Savegame: no key, id or state changes. A reconstructor that holds a lens keeps it.
28. Display Stand and Empowerer: putting an item in swings the hand, in creative too (`350da9b`,
    `a841121`). The Empowered Emeradic Crystal and its block ask for short grass, `minecraft:grass`
    on 1.20.1, instead of the two block tall grass (`d8ad92c`, #1410). On `main` the item is
    `Items.SHORT_GRASS`, its 1.20.3 name, the only vanilla id after 1.20.1 that `main` uses here.
    The provider `EmpoweringRecipeGenerator`, the two generated recipes and their hashes in the
    cache of the provider match. Savegame: no key, id or state changes.
29. Greenhouse Glass: the glass renders its model with the cutout layer again, and the faces
    between two panes are culled (`817797a`, `1224c1d`). The base returned the render shape
    `INVISIBLE`, the gap that source change 18 left. The texture has only fully clear and fully
    solid pixels, so the cutout layer of `main` shows it as the translucent layer would. The model
    of `main` came with source change 18, its recipe of `3f35689` with source change 10. The search
    for the plant below the glass goes down to the bottom of the world, where the base and `main`
    stop at Y 0. The bone meal check keeps the 1.20.1 signature with the client flag, which
    `06bb77b` drops for 1.20.4. The glass loses its `(wip)` label. Savegame: no key, id or state
    changes.
30. Crushers: the Double Crusher crushes. Its block was registered as `BlockCrusher(false)`, so it
    made a plain crusher block entity, and the static ticker of `TileEntityCrusherDouble` only
    called the base tick, while the crushing ran in the static ticker of `TileEntityCrusher`. As in
    `ef6e52f8` the crushing now runs in the instance `serverTick`, and the block is
    `BlockCrusher(true)` with its own title. The screens draw their own background instead of the
    inventory texture, the auto split button is a vanilla button with a tooltip (`fc7032fa`,
    `bfaf90b4`), and shift click moves crushable items into the input slots again (the base had the
    whole `quickMoveStack` commented out). The server checks the recipe list of the API for that,
    and the client predicts the move with its synced recipe manager, since only the server fills
    the API list (`ResourceReloader`). The generator writes the 49 crushing recipes of `main`
    (`ef6e52f8`, `7c91da5a`, #1405) in the 1.20.1 result form with `item`, `count` and `chance`, and
    each one matches its file on `main` in ingredient, items, counts and chances. The base's single
    `crushing/bone_crusher` is now `crushing/bone` as on `main`. The serializer takes a missing
    `chance` as 1.0 and a missing `count` as 1 as the result codec of `main` does (`bb84d539`), and
    refuses a recipe with no result or more than two with a `JsonSyntaxException`, so the recipe
    manager logs and skips that recipe as `main` does with the error of its codec. An
    `IllegalStateException`, which the base threw for no result, is not caught by the recipe
    manager of Forge 47 and stops a dedicated server at boot (a local fix of the integration
    review). Ores crush to two raw ores, iron and deepslate iron with a 20
    percent chance of raw gold and copper with 5 percent. AlmostUnified keeps those outputs
    vanilla, because `minecraft` leads its `modPriorities` (`config/almostunified/unify.json:3`).
    The crusher recipes themselves came with source change 10 (`7bcebe6`). JEI gets the crusher
    category of `ef6e52f8` with both crushers as catalysts and their click areas (`1bcf011` drops
    the old commented category). Both crushers share one menu type, so the screen factory
    `CrusherScreen.create` now builds the `CrusherDoubleScreen` for the double one, whose click
    area upstream registers but never reaches. The names lose their `(wip)` label, and the auto
    split tooltip keys are lowercase as in `ef6e52f8`. The Powered Furnace parts of `fc7032fa` and
    `bfaf90b4` come with its fixes. Savegame: the NBT keys `FirstCrushTime`, `SecondCrushTime`,
    `IsAutoSplit` and `Items` stay. A Double Crusher placed with the base jar saved an
    `actuallyadditions:crusher` block entity with three slots. It still loads with its items, but
    the chunk does not tick it in a `crusher_double` block (`LevelChunk.BoundTickingBlockEntity`
    checks `BlockEntityType.isValid` and logs one WARN "invalid for ticking"), so it has to be
    broken and placed again. The base gave the Double Crusher no recipe, so only a creative world
    can hold one.
31. Powered Furnace: JEI lists it as a catalyst of the smelting category (`ca3f84ef`), and a click
    on the arrows of its screen opens that category (`53414c43`). The auto split button is a
    vanilla button at the top left of the screen with a tooltip, where the base drew a small
    button over the corner of the energy bar (the furnace parts of `fc7032fa` and `bfaf90b4`, and
    `73f7af4d`). The item shows the tooltip "Previously Double Furnace" again (`bf4be229`, the 1.12
    item block tooltip whose lang key the base still had). The name loses its `(wip)` label
    (`4b55999a`), and the tick uses its own level and position parameters (`323c0756`). The block
    already gives light level 12 while it works, so `bbf42da6` only drops the comment that doubted
    it. No data, NBT key or block state changes.
32. Coal Generator: it burns anything with a furnace burn time, as `d5757a14` (#1435) does, at
    `ENERGY_PER_TICK` 20 CF/t for the burn time of the item, which is the rate that each of the six
    solid fuel recipes of the base gave (32000 CF over 1600 ticks for coal and charcoal, 2000 over
    100 for a stick, 4000 over 200 for tiny coal, 320000 over 16000 for a coal block and 400000 over
    20000 for a lava bucket). Burn times come from `ForgeHooks.getBurnTime` with the smelting type,
    the furnace lookup with Forge's `FurnaceFuelBurnTimeEvent`, so logs, planks, blaze rods and
    modded fuels work too. On Forge 47 `ItemStack.getBurnTime`, which `main` calls on NeoForge,
    only returns what an item sets itself, -1 for the vanilla fuels. A lava bucket leaves its empty bucket in the slot (`StackUtil.shrinkForContainer`),
    which automation may then pull out. Tiny coal and tiny charcoal already burn for 200 ticks on
    the base through `Item#getBurnTime`, which is what the furnace fuel data map of `3a2f298e`
    gives them on `main`. A fuel hovered in the screen shows what it generates (the tooltip key
    `coal_generator_stats` of `d5757a14`), the screen no longer draws its dark background twice
    (`bb57f309`, #1462), and shift click uses the same burn time check (`a5172ea2`). The generator
    writes no solid fuel recipes any more, so the six files under `recipes/solid_fuel/` are gone,
    and the tag change of `9492973a` to the coal block recipe has nothing left to change. Unlike
    `main`, the recipe type and serializer `actuallyadditions:solid_fuel` and the API list
    `SOLID_FUEL_RECIPES` stay, unused, so that a data pack that still ships such a recipe loads.
    Savegame: `BurnTime` and `MaxBurnTime` keep their meaning. The key `currentRecipe` of a
    burning generator is no longer written and is ignored on load, and the generator finishes the
    burn at 20 CF/t, the rate it had before. Tiny coal and tiny charcoal lose their `(wip)` label,
    as on `main` (a local fix of the integration review).
33. Other generators: the Leaf-Eating Generator reads `leafGeneratorCooldown`,
    `leafGeneratorCPPerLeaf` and `leafGeneratorArea` of the common config (source change 6), as
    `5182ac8b` does. The base read the 1.12 enums in `mod/config/values/`, which nothing loads, so
    the energy per leaf was 0 and the generator never ate a leaf. It now takes a leaf every 5
    ticks for 300 CF from the blocks within 7 of it (a 15 block cube), counting every
    `LeavesBlock` and every block in `minecraft:leaves`. Unlike `main` the generator posts the break
    event of the shared fake player at its own position before it takes a leaf
    (`WorldUtil.fireFakeBreakEvent`), so a protection mod or, on Arclight, a Bukkit claim plugin can
    keep the leaf. A refused leaf makes the next of the shuffled leaves the candidate, up to 8 of
    them, and a cycle without a leaf makes no energy. The Heat Collector asks the same event before
    it removes a lava or magma block next to it. It collects only the leaves out of the cube instead
    of every position first, and `leafGeneratorArea` is a radius from 1 to 16 with a comment that
    says so, where `main` calls it the odd size of a 7x7x7 area and takes any value up to
    `Integer.MAX_VALUE`. These are local changes of the integration review. The Bio Reactor faces the player who
    places it (`822913f0`), so the generator writes its blockstate, which matches `main`, and the
    block no longer shows the missing model that source change 18 left. Its screen draws the
    energy bar and its tooltip and labels the output with the lang key
    `misc.actuallyadditions.energy_tick` instead of the missing `actuallyadditions.cft`, and the
    menu has its title (`fc75b13b`). The recipes of the Leaf-Eating Generator, the Bio Reactor and
    the Heat Collector came with source change 10 (`5182ac8b`, `8abfa241`, `8419cc67`). The three
    blocks, the six AIOTs and the Laser Wrench lose their `(wip)` label (`5182ac8b`, `8419cc67`,
    `5a9f7d91`). Nothing to port from `b261d92b` and `f8cc5252`: they fix the 1.20.4 codec of the
    liquid fuel recipes, which had read `total_energy` into the burn time and the other way round,
    and the 1.20.1 serializer of the base already reads both keys into the right fields, as the
    four generated `liquid_fuel` files show. Savegame: the Bio Reactor gains the block state
    property `facing`, and a Bio Reactor placed with the base jar loads facing north, the default
    of `DirectionalBlock.Container`. No NBT key changes.
34. Coffee Maker and coffee: the ingredients are the recipe type and serializer
    `actuallyadditions:coffee_ingredient` of `971fdd65`, with the snake case keys `max_amplifier`
    and `extra_text` of `718494a9`, on the 1.20.1 serializer. The generator writes the nine
    ingredients of `main`, and the seven with effects match `main` byte for byte. The tagged milk
    looks at `forge:milk` behind `forge:not` and `forge:tag_empty` inside `forge:conditional`, as
    `main` does with `c:milk`. The base had none of this. Its ingredients were the API list of
    `CoffeeIngredient` objects, and `ItemCoffee.initIngredients` added only the milk bucket and a
    HarvestCraft soy milk, with the effect of milk commented out. The cup was registered as a
    plain item, so it could not be drunk, and the machine brewed from full cups into coffee
    beans. Now the API list `COFFEE_MACHINE_INGREDIENTS` holds the recipes (`ResourceReloader`
    fills it), `ItemCoffee.MilkIngredient` and its client only `getExtraText` are gone (`a22ab366`),
    and `addCoffeeMachineIngredient` does nothing, as on `main`. The cup is `ItemCoffee` with three
    sips, the drink animation and no repair (`e6156bfc`, #1512). It applies its effects from the
    copy of the stack, only on the server, and gives back an empty cup after the last sip
    (`0d27a4a3`, `c2b2cc4d`, #1422). The tooltip shows the stored seconds right (`d4cc7b07`, with
    the 1.20.1 `StringUtil.formatTickDuration`, which takes ticks). The cup stores each effect by
    its registry name under `ID` (`56402e46`), keeps the keys `Counter`, `Duration` and `Amplifier`,
    and an ingredient that raises an effect no longer wipes the other NBT of the cup or writes the
    effects twice (`90cecc8e`). The machine brews from empty cups into coffee (`fc20f4a9`), uses up
    its water through `AATank.drainInternal` (`5c4e584a`, #1422, the tank refuses outside drains),
    takes empty cups from automation (`56d730d6`, changelog 1.3.16), moves empty cups into the cup
    slot on shift click (`0a6aa654`) and no longer lets the click through to the held item
    (`09673dd1`). The ingredient lookups that a client runs, shift click and JEI, use the recipes
    the client received, since only the server fills the API list. JEI gets the coffee category
    with the Coffee Maker as catalyst and the click area of `53414c43`, and the old commented
    category goes (`96992eb8`, `c585fc99`). The extra text is translated (`f58948e7`, #1403), and
    the three JEI lang keys of `971fdd65` are added. Local divergence: milk works again. On `main`
    the milk ingredients have no effect, so `effect` returns false, the milk is never used up and
    the cup stays as it was, although JEI shows "+01:00, -1 Level". The two milk recipes carry the
    additive key `"milk": true`, which `main` would ignore, and such an ingredient does what the
    1.12 `MilkIngredient` did: every effect that the slots before it added loses one amplifier and
    gains 120 seconds, an effect at the lowest amplifier goes, and the milk is used up and leaves
    its bucket. The JEI line now says "+02:00, -1 Level", which matches those 120 seconds and the
    manual text of the base. The names lose their `(wip)` label (`0a6aa654`), and the coffee beans
    lose it as well, as on `main` (a local fix of the integration review). Not ported:
    `56322ebc` only adds a TODO about the saturation. Savegame: the item `coffee_cup` keeps its id
    and becomes a stack of one, so a stack of base cups, which only creative could hold, keeps its
    count until it is split. The machine keeps its NBT keys. New ids: the recipe type and the
    recipe serializer `actuallyadditions:coffee_ingredient`.
35. Canola Press and Fermenting Barrel: JEI gets the pressing category of `0632253a` with the
    Canola Press as catalyst and the accessor `PressingRecipe.getInput` it reads, and the
    fermenting category fills its arrow over the time of the recipe (`62ff6acd`). Shift click into
    the press predicts with the recipes the client received, since only the server fills the API
    list of the pressing recipes. The menu fix of `056ec5c1` only adapts the 1.20.4 `RecipeHolder`
    and has nothing to do on the base, whose press already moves pressable items on shift click.
    The crop tag of `9689b4c1` came with source change 9, and its two other files only add TODO
    comments to the fermenting and liquid fuel recipes. No data, NBT key or block state changes.
36. Energizer and Enervator: both menus get the armor slot class of `556fa487` and the off hand
    slot of `c1d10d12` (#1456) next to the boots, with the new `gui_energizer.png` of that commit,
    which is byte identical to `main`. The four armor slots show the empty slot icons of the
    player inventory (`e7e2de68`), play the equip sound and keep armor with the Curse of Binding
    on a player who is not in creative mode, which the Enervator did not check before. On 1.20.1
    a slot takes what `ItemStack.canEquip` allows there, which asks `Mob.getEquipmentSlotForItem`
    unless the item decides itself, as the armor slots of vanilla's inventory do. The base and
    `main` take any `ArmorItem` in any of the four slots, so boots fitted the helmet slot. The GUI
    crash that `556fa487` fixes comes from the 1.21 map of empty slot textures and does not exist
    on 1.20.1, so no access transformer is needed: the slot uses the public `EMPTY_ARMOR_SLOT_*`
    constants of `InventoryMenu`. The block entities name their menus (`e7e2de68`), and the names
    lose their `(wip)` label. The recipes came with source change 10 (`b69847d1`). Local fix: the
    input slot of both machines holds one item, and a finished item moves out alone. Both machines
    copied the whole input stack into the output and took one item from the input, which, for a
    stack of energy items, duplicated all but one of them (unchanged on `main`). Savegame: no
    NBT key or block state changes. An input stack that an old save holds keeps its count until it
    is taken out.
37. Experience Solidifier: its screen has the nine buttons again (1, 5, 10, 20, 30, 40, 50, 64 and
    All), as vanilla buttons at the places of the commented 1.12 ones, and the stored amount moves
    down to y 34 (`a4cc12d9`). A press sends the button packet and predicts the change of the
    player's experience on the client, as upstream does. The block entity names its menu with the
    existing lang key `container.actuallyadditions.xpSolidifier`, where `main` added a second key
    `experienceSolidifier` with the same text. The name loses its `(wip)` label. Local hardening:
    `onButtonPressed` refuses a negative id (`TileEntityXPSolidifier.java:208` on the base checked
    only the upper bound), so the crafted press with id minus 1 of the test mod changes nothing and
    throws no `ArrayIndexOutOfBoundsException` any more (source change 14). The recipe with
    experience bottles came with source change 10 (`b68cfecd`, `9492973a`). The experience formulas
    excerpted from Ender IO keep their credit, and `NOTICE` names them. No NBT key changes.
38. Firework Box: the settings screen of `fd92ee85` and `5c13128a` works, with its twelve sliders
    in two columns, the background `gui_firework_box.png` of `5c13128a` (512 by 512, byte identical
    to `main`) and the title above it. The block entity names its menu (`fff34a3a`), and the name
    loses its `(wip)` label (`41e85391`). The base screen drew nothing and had no slider. The
    slider ranges are the ranges that the box clamps every number to since source change 17
    (Value Play 0 to 5, charge amount 1 to 4, flight time 1 to 3, color amount 1 to 6, area 0 to
    4 and the seven chances 0 to 1), so the server keeps that clamp in `onNumberReceived` and
    `readSyncableNBT`. Two local changes to the screen of `main`: a slider sends its number when
    the mouse lets go of it, also outside of it, or when a key moves it, where `main` sends one
    packet for each step of a drag and so one block update to every client near the box, and an
    integer slider sends the whole number it shows, where `main` sends the raw value, which the
    server cuts down (a slider at 4.6 showed 5 and stored 4). The screen builds no sliders when
    the client found no box at the position of the menu (source change 15). No NBT key changes.
39. Automatic Feeder: the screen shows the animal count and whether the animals can breed when the
    mouse is over the heart, a tooltip that the base had commented out (`73be71af`), the search
    box of five blocks is centered on the feeder (`73be71af`, the base reached one block less to
    the positive sides), the menu has its title (`c418b867`) and the name loses its `(wip)` label.
    The comparator output follows the animal count as `5db04c0b` (#1504, changelog 1.3.22) does:
    0 without animals, then 1 to 15 as the count rises to the limit of 30, where the base always
    gave the 0 of `TileEntityBase`. A change of the output updates the comparators, as the
    Coal Generator does it. `Mth.lerpDiscrete` of `main` does not exist on 1.20.1, and its formula
    goes past 15 above the limit, so the port caps the count at the limit, which the commit message
    of `5db04c0b` describes. The recipe came with source change 10 (`7c7a095c`). No NBT key
    changes.
40. Lava Factory: the look at overlay of the controller says again whether the multiblock is
    complete. The base called `StringUtil.drawSplitString`, whose body is commented out, so the
    overlay drew nothing. As in `6886aca6` it now uses `GuiGraphics.drawWordWrap` with Components,
    in the client only `LavaFactoryControllerHud` of source change 13, and reads the block entity
    with an `instanceof` check. The lang key of the complete factory is
    `tooltip.actuallyadditions.factory.working.desc` as on `main` (`6886aca6` renamed the code
    and the lang key differently, `main` has both as `working.desc`). The casing recipe that gives
    4 came with source change 10 (the port.MD change list, `main`), the casing is called Lava
    Factory Casing as on `main`, and both names lose their `(wip)` label. The Phantom overlay calls
    the same empty helper and comes with the phantom block fixes. No data, NBT key or block state
    changes.
41. Laser relays: the render type of the lasers uses the block sheet, the main target and the
    shader `position_color_tex_lightmap` (`8374a4d`). The base set no shader state at all. The
    beam takes its alpha into the colour for the additive blend, maps the white sprite of Forge
    and no longer draws the innermost tube of width 0 (`8374a4d`), and it returns when the client
    has no level (`78258dc`). The relay renderer draws its lasers up to 32 blocks away
    (`eb79636`). The infinite render box of that commit is the `getRenderBoundingBox` of
    `TileEntityLaserRelay` on Forge 1.20.1, which the base already returns, so the commented out
    copy that upstream `845aaf8` deletes does not exist here. A click on a relay with the relay
    configure item (a compass by default) returns `SUCCESS` (`99e1663`). A crouching click with
    it reaches the relay again, since a server listener of `PlayerInteractEvent.RightClickBlock`
    denies the item and allows the block (`99e1663`), so the priority of an item relay can go
    down. The listener takes the place of the empty spawner listener of `CommonEvents`, as
    upstream does, and the item relay reads `isCrouching` instead of `isShiftKeyDown`. The item
    of the item laser particle rises from the relay that it leaves and falls into the relay that
    it goes to (`99e1663`). The base drew it the other way round. An energy relay counts a
    transfer and its loss only when the receiver takes energy in the real call (`9fe3fa1`,
    changelog 1.3.7). A storage such as the Mekanism energy cube that accepts a simulated
    transfer and refuses the real one no longer costs the sender the loss for nothing. The fluid
    relay in input mode pulls at most `fluidLaserTransferRate` mB per tick (default 10000) from
    each tank next to it (`1f78ecc`), where the base pulled without a limit. Upstream `e870e5c`
    drops a `Tesselator.begin` that the 1.21 port had added to `renderLaserParticle`, which the
    1.20.1 code never had, and `8cdd5ac` drops a comment. Savegame: the relay NBT keys `Mode`
    and `Priority` and the connection data stay the same.
    The dev client test of this bundle ran the test mod with a local scenario, which is not
    committed, against the jar of its four commits (SHA-1 `0bf32839`) on a dedicated Forge
    47.4.18 server with JEI, Patchouli, Curios, The One Probe and Mekanism 10.4. Two energy
    relays joined with the Laser Wrench carried the 200000 CF of a Single Battery in a Battery
    Box on one side into a Mekanism basic energy cube on the other side, which ended with 451910
    J, and the battery ended empty. With `fluidLaserTransferRate` set to 100, an input only
    fluid relay next to a Mekanism tank with 8000 mB of water moved 1100, 1200 and 1000 mB in
    three steps of about 10 ticks into the tank behind the second relay. A plain compass click
    turned the energy relay from `BOTH` to `OUTPUT_ONLY`, and one plain and two crouching clicks
    left an item relay at priority minus 1. Screenshots of the dev client show the energy, fluid
    and item lasers. Not tested: a storage that refuses the real transfer (`9fe3fa1`) and the
    pack's Arclight.
42. Item interfaces: the Hopping Item Interface has the hopper facing `facing` again, `down`,
    `north`, `south`, `west` or `east`, set from the clicked face as a hopper does, and it turns
    and mirrors with the structure (`bd392b0`, changelog 1.2.14). The base had no property, so
    its block entity threw in the server tick when it read `FACING`. It pulls from the inventory
    above it and pushes the way it faces, and `WorldUtil.extractItem` reads the item handler
    again (`5a1eabe`), which the base had commented out, so the interface never moved an item.
    The Item Interface numbers the slots of all handlers in its network one after the other and
    skips relays in unloaded chunks (`5a1eabe`). The base started at 0 again for every handler,
    so each handler covered the slots of the one before and most of a network was out of reach.
    The wrapper `SlotlessableItemHandlerWrapper` keeps the `LazyOptional` of Forge, and its unused
    slotless part is deprecated as in `5a1eabe`. A block entity update packet without a tag no
    longer throws on the client (`26758cb`), which happened when a Hopping Item Interface was
    placed. The state without the facing falls back to `down` in the block entity. The debug
    line `System.out.println(cap.getSlots())` that `bd392b0` added and `main` still has stays
    out. The model without cull faces of `9b6dd7d` (#1401, changelog 1.2.14), which fixes the
    faces that vanished next to other blocks, came with source change 18. The blockstate keeps the
    single variant of `main` for all five facings, so the model does not turn with the facing, as
    upstream, and `BlockStateGenerator` still writes it with `standardBlock`. Upstream has no side
    model to turn. Savegame: a Hopping Item Interface saved without the property loads with the
    default `facing=down` and keeps pushing down.
    In a run of the same test a Hopping Item Interface facing east pulled the 32 cobblestone out
    of the chest above it into the chest of its item network and pushed them on into the chest
    east of it, and a hopper fed 8 raw iron through an Item Interface and an item relay on top
    of a furnace into the input slot of the furnace. The server safety harness of source change
    14 then placed, right clicked, wrenched and broke the Hopping Item Interface with no ERROR
    line and no crash on the server. It skipped only the Ranged Collector and the Advanced Item
    Laser Relay, whose screens the sacks and filters port fixes. On Arclight the interface pulls
    from inventories and item entities without the Bukkit `InventoryMoveItemEvent` or
    `InventoryPickupItemEvent` that a vanilla hopper fires, as other modded item transport does.
    Not tested: a Hopping Item Interface under a claimed chest on the pack's Arclight.
43. Battery box and energy items: the Battery Box writes its charge on all four sides, in white
    and with the normal display mode, and centres the name on its own width (`c1ee118`). The base
    pushed and popped the same turn four times, so the four lines landed on one side, black and
    see through. The box hands out its own `LazyOptional` for the battery in its slot and
    invalidates it when the battery leaves or another one comes in, the Forge form of
    `invalidateCapabilities` in `6596a8e`, changelog 1.3.7. The base handed out the battery's own
    `LazyOptional`, which stays valid after the battery is taken out, so a cable or machine that
    caches it kept charging or draining a stack that was no longer in the box. The durability bar
    of the energy items shows the charge, `round(13 / max * energy)`, in the rainbow colour of
    `87f753a` and the fill direction of `24763a4` (the base returned 0 or 1). The bar reads the
    NBT key `Energy`, which the client stack carries, the colour comes from the client only
    `ActuallyAdditionsClient` through `DistExecutor`, and `maxPower` and `transfer` are public as
    in `24763a4`. The creative tab lists an empty and a charged stack of every energy item
    (`f0184f4`), where the base listed only a charged one, and JEI tells them apart with the
    subtype interpreter `EnergyInterpreter` (`c86a290`, `3cf1815`, #1485). JEI 15 takes a string
    there, so it returns the legacy string of `main`, `charged` for a full item and `uncharged`
    for any other, and a part charged stack looks up the empty entry. The energy capability of an
    item starts from the NBT key `Energy` when Forge gathers it. A stack read from a packet, such
    as a charged battery that a creative player takes from the tab, has no capability data to
    deserialize, so its storage started empty while its tag said full. The tab writes the charge
    as an int before anything reads the capability, where the base wrote a double. The drill fix
    of `b8575c4` has nothing to change here: on 1.20.1 the drill uses `defaultDurability(0)`,
    which leaves it without durability, while the 1.21 `durability(0)` made it breakable.
    Savegame: the item energy stays in the NBT key `Energy`, and a double `Energy` written by the
    base tab still reads as an int. No id or block state changes.
    In the same test the hotbar showed the bar of a half charged Single Battery at half width
    and of a full drill at full width in the rainbow colour, and empty batteries with an empty
    bar. A battery taken out of the box by hand left the slot of the box empty. JEI listed two
    entries each for the Single and the Quintuple Battery, the drill and the Teleport Staff. Not
    tested: a caching cable that lets go of a battery that leaves the box.
44. Particles and laser render: the beam particle renders (`fb80bf3`). The base wrote it into the
    tesselator without beginning it, which throws, and spawned no particle for any laser at all,
    since `spawnLaserWithTimeClient` only reset the beam of an Atomic Reconstructor. Every laser
    of the server is a beam particle now, the beams of the Vertical Digger and the Leaf Generator
    too, with the same text as the port of `fb80bf3` and `337fbfb` in the world machines bundle,
    which also takes the beam out of the reconstructor renderer (item 27). This item replaces the
    lightmap format of item 27 and its begin and end of the tesselator for each beam. The particle
    has its own render type with additive blending (`fb80bf3`, `58b9cbc`), and it draws position
    and colour only (`7fb62cb`) with the shader `position_color` (`db1bb6e`, changelog 1.3.9,
    compatible with Iris, which is Oculus on 1.20.1). The 1.20.1 particle engine hands the render
    type the buffer of the tesselator, so `ParticleBeam.LASER_RENDER` begins it in that format,
    sets the shader and draws all beams of a frame at its end, the part that the 1.21 engine does
    with the buffer that `begin` returns. The relay lasers use `position_color` too (`db1bb6e`),
    with the colour dimmed to 0.4 as there, and lose the texture coordinates of `8374a4d`, which
    their format never had (`de635bb`). The item particle of the item laser relays has its own
    render type (`b3f87a6`, #1486, changelog 1.3.18), and its item render no longer binds the block
    atlas and changes its mipmap blur in the middle of the particle pass. Translucent particles
    next to the item particles glitched before, since the item particle sat in the translucent
    particle sheet. The 1.21 type begins a buffer that stays empty, so the 1.20.1 type only sets
    the blending and puts it back at the end. The beam keeps its colour as three floats and an
    alpha in `BeamParticleData`, the 1.20.1 form, where `7fb62cb` packs the alpha into the int
    colour of the 1.21 particle data. The client test with and without Oculus and Embeddium has not
    been run (not verified). No id, key or packet changes.
    In the same test, with the particle setting at all, the particle engine of the client held
    particles of the render types `actuallyadditions:laser_particle` and
    `actuallyadditions:item_particle` and none in the translucent sheet. A screenshot shows the
    beam of an Atomic Reconstructor, which only spawns for players within 8 blocks of its start
    or end as on `main`, and closer screenshots show the cobblestone items over the item relays.
    The client log has no ERROR line but the one of the sound system of the headless client.
45. Machines that keep their contents: the Coal, Oil and Leaf-Eating Generators, both crushers, the
    Display Stand, the Powered Furnace, the Atomic Reconstructor, the Coffee Maker, the Canola
    Press and the Farmer keep their energy when broken, the Oil Generator, the Coffee Maker, the
    Fluid Placer and Collector, the Canola Press and the Fermenting Barrel keep their fluids, and
    the reconstructor, the Fluid Placer and Collector, the Breaker, the Placer and the Dropper keep
    their redstone mode (`c1e274d`, `f05b284`, changelog 1.3.10). Upstream collects data components
    with `collectImplicitComponents` and copies them with `CopyComponentsFunction`. Here the loot
    tables copy the same values with `minecraft:copy_nbt` from the block entity into
    `BlockEntityTag` under their NBT keys: `Energy`, `IsPulseMode`, the coffee cache `Cache`, a
    single tank as `FluidName`, `Amount` and `Tag` at the root of the block entity, and the two
    tanks of the barrel as `tanks.inputTank` and `tanks.outputTank`. The vanilla `BlockItem` merges
    that tag into the new block entity on placement. The base already copied the energy of seven of
    these machines, the reconstructor with its pulse mode (the lines after `//Special Drops` in
    `LootTableGenerator` of `96547b2`, as in `226a439`). A key that is missing, such as `Tag` of a
    fluid without NBT, is skipped. The function copies a key whatever its value, so a Breaker,
    Placer or Dropper in its default mode drops with `IsPulseMode` 0 and an empty fluid machine
    with the fluid `minecraft:empty` and the amount 0. Such a drop stacks with a newly crafted
    machine only after the reset recipe below. The same holds on `main`, where
    `collectImplicitComponents` always sets the pulse mode and the tank (`f05b284`). The keys
    `StopDrop` and `Redstone`, the work timers and the inventory stay behind, and the Farmer leaves
    its compass area with the block as on `main`, which copies only its energy. The copy in
    `FluidContents.get()` of `2b8884d` keeps a tank from sharing the stack of a data component, and
    the NBT form reads a new `FluidStack` at every load, so it needs no such guard. The base read
    the 1.12 tag `Data` with `SAVE_BLOCK` in `BlockContainerBase.setPlacedBy` after that merge, and
    the empty read cleared the tanks and the coffee cache again. The method is gone, as on `main`
    since the 1.21 port (`23815b7`, removed in `b66af84`). The Leaf-Eating Generator gets the
    energy item with its tooltip (`c1e274d`), and the reconstructor item shows its energy and its
    redstone mode each only when it carries them.
    The 17 shapeless recipes `actuallyadditions:reset/<block>` of `a85dcdc` craft each of these
    machines alone into an empty one, since their result has no tag.
    A machine with `StopDrop` 1, which the Engineer's House of `main` sets on every machine of its
    template, drops nothing and tells a survival player that it broke (`74982c3`, #1436, changelog
    1.3.9). Upstream returns air from `playerWillDestroy`, so the loot of air applies. On 1.20.1
    that method returns nothing, so `BlockContainerBase.getDrops` gives no drops when the block
    entity of the loot context has `StopDrop`. That also holds for explosions, the breakers of the
    mod and `/loot ... mine`, which pass the block entity as well. The message comes only from the
    server, so it shows once. The inventory of such a machine still drops in `onRemove`, as on
    `main` and on 1.12. The plan of the bundle wanted it kept back as well, but the machines of
    the house template have empty inventories (dump of `andrew_period_house.nbt` on `main`), so
    that would only destroy what a player put in.
    A tile counts as powered before it reacts to a pulse (`c197f65`), so a neighbour update from
    its own pulse cannot start a second pulse.
    The lamps, the Fermenting Barrel and the Leaf-Eating Generator drop when broken with any tool
    (`632c347`, #1447, changelog 1.3.11). The three property helpers in `ActuallyBlocks` lose
    their explicit `requiresCorrectToolForDrops` as upstream, but `Properties.copy(Blocks.STONE)`
    takes over the flag of stone (javap of `BlockBehaviour$Properties.copy` in Forge 47.4.0), so
    the machines built on them still need a pickaxe to drop. On `main` the same helpers use
    `ofFullCopy(Blocks.STONE)`, which copies that flag too (1.21.1 source not checked, not
    verified).
    Savegame: no id, block state or NBT key changes. The NBT on the item is additive, and an item
    without `BlockEntityTag`, as the base dropped it for most of these machines, places an empty
    machine as before.
    In a test on a dedicated Forge 47.4.18 server with the jar of this change (SHA-1
    `fa32053c00d39df8614ca6c7a7698a6565cb82b1`) and the dev client of the fork under Xvfb, with a
    local test scenario that is not committed, a survival player broke 14 of these machines with
    set contents, picked each up and placed it back. The Coal Generator, the Double Crusher and the
    Placer were left out of these cycles. The Coal Generator only ran in the `StopDrop` case and
    the Placer only as the machine that places a crusher. Every item carried exactly the kept keys,
    the tooltips showed the energy and, on the reconstructor, the pulse mode, and every placed
    machine had the energy, tanks, coffee cache and pulse mode it had before. The Farmer came back
    without its area. A crusher crafted alone in the inventory grid lost its tag and placed with 0
    energy. A crusher and a Coal Generator with `StopDrop` 1 dropped no machine, the crusher
    dropped the iron ore of its inventory, and the message showed once. The command
    `/loot spawn ... mine` gave no item for a crusher with `StopDrop` and a tagged crusher for a
    normal one. Broken by hand, the Leaf-Eating Generator, the Fermenting Barrel and a lamp
    dropped and a crusher did not. A Placer of the mod placed a tagged crusher through its fake
    player with its energy. The server log has no ERROR line. Not tested: the pack's Arclight
    and a pulse that recurses before `c197f65`. On Arclight the merge of `BlockEntityTag` runs in
    `BlockItem.updateCustomBlockEntityTag` while CraftBukkit captures the block states, the path
    that shulker boxes take as well. The ship stage should place a tagged crusher and a filled Oil
    Generator as a player on the Arclight test server and check that the energy and the tank come
    back (not verified).
46. Sacks and filters: the Traveler's Sack picks items up again. A sack with the pickup toggle on
    in any slot but the selected one takes the items its filter lets through (`edccaec7`,
    `521cf822`). The base read the 1.12 contents and filter from the stack's NBT, wrote the items
    back there, where the sack menu never looked, and checked a filter that no menu saved. The
    sack menu gets its pickup button and the filter buttons of `main` (`85725a0f`, `79c8bcfb`,
    `38f48247`). The toggle goes into the stack's NBT key `AutoInsert` at once and when the menu
    closes (`669e8437`), where `main` has the data component `AUTO_INSERT`. The filter of the
    Traveler's Sack lives in its `SackData` under the key `Filter` next to the inventory
    (`adce5984`), and a filter toggle marks the sack data for saving, which `main` does not do.
    The menu keeps the base's id `actuallyadditions:bag_container` (`main` renamed it to
    `sack_container`). The Void Sack gets its own menu `actuallyadditions:void_sack_container`
    with a deletion slot and its own screen (`87601c1e`, `6ab2a067`). Its filter and toggle live in
    the stack's NBT keys `Filter` and `AutoInsert` (`main` has `FILTER_SETTINGS` and
    `AUTO_INSERT`, `521cf822`, `bc20d82f`), and each change goes into the stack at once, as the
    drill menu does since source change 15. The base opened no menu for the Void Sack. Both sack
    menus are bound to the hotbar slot and the stack they opened with, as the drill and the Item
    Filter are since source change 15, so they close when the slot changes and write only into
    their own sack. The base returned `true` from `stillValid` and wrote its options into
    whatever stack was selected when it closed. A sack no longer opens nothing on a block entity:
    `useOn` passes, as on `main`, which commented out the old emptying into a clicked inventory
    because it read the contents from the stack's NBT. The `SackManager` keeps its sacks in one
    map for each world. The base and `main` share one static map, so a client that opened a
    second world saved the sacks of the first one into the second one's file. Forge 47 calls
    `Player.take` for an `ALLOW` result of `EntityItemPickupEvent` (`ItemEntity.playerTouch`,
    javap of the mapped Forge 47.1.3 jar), and its packet plays the pickup sound on the client, so
    the sound of `669e8437` is not added.
    The filters match by damage and by NBT (`6389668b`, `9000eb3f`), with the four buttons WH,
    MO, DM and DA of `main`, and the mod mode compares the mod and the damage only. A filter slot
    takes its ghost item from the cursor and gives a filter back to the cursor (`85725a0f`,
    `87601c1e`). The base read the selected hotbar item, so a click with an empty cursor put a
    copy of the held sack into the slot. The `FilterSettings` NBT keys stay `Whitelist`, `Mod`,
    `NBT` and `Items` and gain `Damage`. The buttons send `idOffset` plus the ordinal of
    `FilterSettings.Buttons`, with the NBT button fix of `f46de7d9`, and their tooltips wrap at
    200 pixels, the width of the 1.12 code, which `main` leaves unwrapped with a TODO. The
    tooltip of the mod button has the two lines of `bc5dc448`, which the inventory places in the
    screen fixes of the manual and compat bundle, since it only touches this file and its lang
    keys. The lang keys of the damage and NBT buttons follow `6389668b`, and the Item Filter,
    the Traveler's Sack and the Void Sack lose their `(wip)` label.
    The Advanced Item Laser Relay screen gets its filter buttons, its two smart buttons and the
    smart tooltip of `3a767e18`, and the buttons go to the relay itself with the ids of
    `f46de7d9` (changelog 1.3.13): 0 to 3 for the inbound filter, 4 to 7 for the outbound one, 8
    and 9 for the smart buttons. The base handed every id to both filters and used 2 and 3 for
    the smart buttons. A filter change and a button press mark the relay for saving, which the
    base and `main` do not do, so a filter changed in a chunk where nothing else changed was lost
    at the next restart. The Item Filter title of `3f49a259` was already fixed by source change
    13. The fluid relay of `87601c1e` gives each receiver its share of the stack. The base had
    the test the wrong way round, so it offered every receiver the whole stack, or nothing when
    there were more receivers than millibuckets.
    On Arclight, which overwrites `ItemEntity.playerTouch`, the Forge event comes first and the
    Bukkit `PlayerPickupItemEvent` and `EntityPickupItemEvent` follow only for what the
    inventory can still hold (javap of `ItemEntityMixin` in `common.jar` of
    `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`). A plugin that cancels the Bukkit
    pickup therefore does not stop a sack, as with other modded pickups. Savegame: additive. The
    sack data gains the key `Filter`, the filters gain `Damage`, and a sack keeps its `UUID` and
    `AutoInsert`. A Traveler's Sack of the base loads with an empty filter, since the base kept
    its filter in the item NBT, which no menu read. No id changes but the new menu id.
    The porter's test ran the dev client with a local scenario, which is not committed
    (`bp2/actuallyadditions/fs-tools/devtest-filterssacks.patch` in the workflow scratchpad),
    against the jar of the four commits of the bundle (SHA-1
    `f81afeeb6c09dd9268858c427c1d67c44892acc3`, 3117412 bytes, one build) on a dedicated Forge
    47.4.18 server with JEI, Patchouli, Curios, The One Probe and Mekanism 10.4. With the pickup
    toggle on, a Traveler's Sack in the first hotbar slot took 16 cobblestone that the player
    touched, and its stack held `AutoInsert:1b`. With a deny filter of an iron pickaxe of damage
    10 and a named diamond and with damage and NBT matching on, the sack took a pickaxe of
    damage 0 and two plain diamonds and left the damaged pickaxe and the named diamond to the
    inventory. With the toggle off, 4 stone went into the inventory and the stack held
    `AutoInsert:0b`. A Void Sack with an allow list of cobblestone and the toggle on destroyed 8
    cobblestone and let 8 dirt into the inventory, and its stack held the filter under `Filter`
    and `AutoInsert:1b`. The smart button of an Advanced Item Laser Relay put the stone, oak log
    and iron ingot of the chest below it into its inbound filter, and its buttons turned on the
    inbound damage matching and made the outbound allow list a deny list
    (`LeftFilter.Damage:1b`, `RightFilter.Whitelist:0b`). After a restart of the server the sack
    held the same items and ghost items, its filter had the packed settings 12 (damage and NBT)
    and the toggle off, and the Void Sack, the relay and the Ranged Collector kept their
    filters. The server safety harness of source change 14 then ran with nothing skipped. It
    placed, right clicked, wrenched and broke all 125 blocks and used all 108 items with no
    ERROR line and no crash on the server, and the screens of the Ranged Collector, both sacks,
    the Item Tag and the Crafting Table On A Stick opened and were closed. The click on the
    Advanced Item Laser Relay opened no screen in that run, which clicks a few ticks after a
    teleport, so the relay screen is covered by the scenario only. Not tested: the pickup on the
    pack's Arclight and next to a plugin that cancels the Bukkit pickup.
    The review at the integration added three local deviations from `main`. The pickup of the
    Traveler's Sack skips the tag `forge:holds_items`, which the slots of the sack menu refuse as
    well. Neither the fork nor any jar of the pack fills that tag (no `tags/items/holds_items.json`
    in the 275 jars of the pack cache or in `local-mods/`), so a shulker box still goes into the
    sack by pickup and through the menu until a datapack or a later bundle fills it. The pickup
    still ignores the target of an item entity, as `main` does, since `ItemEntity.playerTouch`
    checks it only after the Forge event and the field `target` has no getter on 1.20.1 (javap of
    the mapped Forge 47.4.0 jar). A sack in the main hand returns `sidedSuccess`, where `main`
    passes on both sides, so the client no longer goes on to use the item in the off hand once the
    sack opened. The buttons of the Advanced Item Laser Relay act only for a player who has the
    relay's menu open, as source change 48 describes for the Ranged Collector. The integration test
    ran a dedicated Forge 47.4.18 server with the same mods and the jar of the review fix (SHA-1
    `a022b6306c14157f59c6b9861ab5c5a82d135e9c`, 3126365 bytes) and the dev client with a further
    local scenario `fsint`, which is not committed either (`bp2/actuallyadditions/srv-fsint-run3/`
    and `fsint-tools/devtest-fsint.patch` in the workflow scratchpad). The server log has no ERROR
    line. In survival, a Traveler's Sack in the main hand returned `SUCCESS`
    on the client, its screen opened, and the 16 snowballs in the off hand stayed. A shulker box
    that the player touched went into a sack with the pickup toggle on, as the empty tag predicts.
    The relay guard was not exercised.
47. Item Tag filter: the new item `actuallyadditions:item_tag` of `6f56772` holds one item tag,
    and every filter slot of the mod matches the items in that tag (`FilterSettings.check`). The
    tag id sits in the stack's NBT key `tag` as a string, the 1.20.1 form of the attachment of the
    1.20.4 line, which saved a compound with that key, and of the data component `ITEM_TAG` of
    `main`. Renaming the item in an anvil to the id of an existing item tag sets it at the cost 0
    (`CommonEvents.onAnvilEvent`, `main` parses the name with `ResourceLocation.tryParse`), and
    the mixin `AnvilMixin` with the accessor `ItemCombinerMenuAccessor` lets the player take that
    result, which vanilla's `mayPickup` refuses at the cost 0. The handler acts only while the
    right slot is empty, a local deviation from `main` (`CommonEvents.java:233-247` on `main`).
    The event keeps the material cost 0, and `AnvilMenu.onTake` empties the whole right slot for a
    material cost of 0 or less, so on `main` an Item Tag renamed with any stack on the right
    deletes that stack. With an item on the right, vanilla gives an Item Tag no result, since it
    is neither damageable nor enchantable. A name that names no item tag cancels the event.
    `ForgeHooks.onAnvilChange` then returns false and `createResult` returns before it clears the
    result slot, so the result of the last valid name stayed there and the mixin let the player
    take it. `AnvilMixin` therefore also clears the result slot at the head of `createResult`
    while an Item Tag lies in the left slot, through the accessor's `getResultSlots`, another
    local addition, so the anvil shows no result for such a name. The refmap maps the accessor to
    the fields `f_39769_` and `f_39768_` and the injections to `m_6560_` and `m_6640_`. The item
    also opens its own screen in the new menu
    `actuallyadditions:item_tag_container`, which `main` registers but never opens (its `use` is
    commented out), and whose button handler there only clears the tag, so the tag the screen
    sends was lost. Here Enter sends a tag that exists on the client through the new serverbound
    handler `GUI_STRING_TO_CONTAINER_HANDLER` (index 9 of `DATA_HANDLERS`, no protocol bump,
    source change 14). The handler checks the sender like the other screen packets, drops a text
    of more than 256 characters with a WARN and hands the rest to the sender's open menu while
    that menu is still valid. The menu parses the text with `ResourceLocation.tryParse` and sets
    it only when the item tag exists on the server. Enter on an empty box clears the tag through
    button 0. The menu is bound to the hotbar slot and the stack it opened with, as the other item
    menus. The screen shows whether the typed id is a tag, which `main` leaves at its last state
    when a valid id names no tag. The item has the texture of `79b2084` and the flat item model
    of `ActuallyItems.SIMPLE_ITEMS` (the item part of `19afc5f`), and the recipe of `6f56772`
    (paper and black quartz). On Arclight the result skips the Bukkit `PrepareAnvilEvent`, as
    every result of a Forge `AnvilUpdateEvent` does: `ForgeHooks.onAnvilChange` puts it into the
    result slot itself, and Arclight's `RepairContainerMixin` only redirects the `setItem` calls
    in `AnvilMenu.createResult` to post that event (javap of `common.jar` in
    `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`). Not tested on Arclight with EssentialsX.
    Savegame: new ids only.
    In the same test the Item Tag screen showed the tag `minecraft:logs` of the held item, and
    Enter with `forge:ingots` set the NBT key `tag` to `forge:ingots`. After the restart an
    anvil showed no result for the name `forge:no_such_tag`, and the name `forge:ingots` gave an
    Item Tag with that tag at the cost 0, which the player took. A Ranged Collector with that
    Item Tag in its allow list took 3 iron and 2 gold ingots and left 5 cobblestone on the
    ground. The jar boots alone on a dedicated Forge 47.4.18 server (without Curios, Patchouli
    and JEI) with 0 ERROR and 0 FATAL lines and no WARN about the reference map. Not tested: an
    Item Tag in a laser relay network.
    In the integration test of source change 46, an Item Tag on the left and 64 diamonds on the
    right with the name `forge:ingots` showed no result, a shift click on the result slot took
    nothing, and the diamonds stayed. With the diamonds taken out the result came back. The name
    `forge:ingotsx` then left the result slot empty and a shift click there took nothing, and the
    name `forge:ingots` gave the tagged Item Tag again, which the player took, while the 64 diamonds
    stayed in the inventory. The same scenario against the jar of the bundle before the review
    (SHA-1 `f81afeeb`) showed the tagged result with the diamonds on the right, and taking it
    deleted all 64 diamonds (`bp2/actuallyadditions/srv-fsint-ctrl/`).
48. Ranged Collector: its screen opens again and its filter works (`adc276e8`). The base never
    built the filter buttons, so `containerTick` of the screen threw a `NullPointerException` on
    the client as soon as the screen opened, and the collector ignored every button. Since its
    filter starts as an empty allow list, the collector picked up nothing, and nothing could
    switch the filter. Now the four filter buttons of source change 46 send their ids to the
    collector, the screen shows the title `container.actuallyadditions.rangedCollector`, and the
    block loses its `(wip)` label. A change of a filter slot or button marks the collector for
    saving, which the base and `main` do not do. The recipe of `adc276e8` came with source
    change 10. On Arclight the collector takes item entities without the Bukkit
    `InventoryPickupItemEvent` that a vanilla hopper posts, as the Hopping Item Interface of
    source change 42 does. Savegame: the NBT key `Filter` keeps its form and gains `Damage`.
    In the same test the screen of the Ranged Collector opened, and after its WH button made the
    allow list of the Item Tag test a deny list, the collector also took the 5 cobblestone and 4
    dirt dropped next to it.
    A local deviation from `main` from the review at the integration: the filter buttons of the
    collector act only for a player who has its menu open, and those of the Advanced Item Laser
    Relay only for a player who has the menu of that relay open (`onButtonPressed` of both block
    entities). The button to tile handler of source change 14 checks the sender, a loaded chunk and
    a distance of 8 blocks, so a player whom a protection plugin keeps from opening the screen could
    still switch the filter of another player's collector by packets. In the integration test of
    source change 46, the buttons 0 and 2 sent by packet without the menu left the filter of a
    collector at `Whitelist:1b` and `Damage:0b`, and the WH button of the open screen then set
    `Whitelist:0b`. Against the jar before the review the same two packets set `Whitelist:0b` and
    `Damage:1b`.
49. Crafting Table On A Stick: the crafting grid works again (`c4d81aea`). The base opened
    `CraftingMenu` with `ContainerLevelAccess.NULL`, which runs nothing it is handed, so the grid
    never showed a result and the items in it were deleted when the menu closed. The menu now
    has the player's level and position, and its `stillValid` returns `true` as on `main`, since
    no crafting table stands there. The new key bind `key.actualladditions.crafting_stick_open.desc`
    (unbound by default, category `key.actuallyadditions.category`, the keys of `main` with its
    spelling) opens the crafter when it is in either hand, in the 36 slots of the main inventory or
    in a Curios slot (`b5cb3434`, #1417, changelog 1.3.6). The client registers the key in
    `RegisterKeyMappingsEvent` and sends it from `TickEvent.ClientTickEvent`, both only on the
    physical client. The key goes to the server through the new serverbound handler
    `HOTKEY_HANDLER` (index 10 of `DATA_HANDLERS`, no protocol bump, source change 14), which
    checks the sender like the screen packets, drops an unknown hotkey with a WARN and opens the
    crafter only when the player has one. `main` sends its own `HotkeyPacket` payload. Every call
    into the Curios API sits in the new class `CuriosCompat`, which `hasCrafterOnAStick` only
    calls while `Util.curiosLoaded` is set (`ModList` at mod construction), so a server without
    Curios never loads a Curios class. `main` imports `CuriosApi` in the common item class. On
    Curios 5 for 1.20.1 `CuriosApi.getCuriosInventory` returns a `LazyOptional`, which the compat
    class resolves before `findFirstCurio`. The item goes into the tag `curios:charm`
    (`ActuallyTags.Items.CURIOS_CHARM`), and the new data provider `data/Curios.java` writes
    `data/actuallyadditions/curios/entities/player.json`, which gives players the charm slot in the
    entity format of Curios 5.14.1 (keys `entities` and `slots`, checked against
    `CuriosEntityManager` of `curios-forge-5.14.1+1.20.1.jar`). The data generator adds the provider
    only when Curios is loaded, as in the dev runs. Not tested on Arclight. Savegame: none, the
    key bind is client only.
    In the same test the key, bound to K by the test mod, opened the crafter from a slot of the
    main inventory and from the Curios charm slot, which the data of this change gives the
    player, and opened nothing once the crafter was gone. Two oak logs in the grid showed 4 oak
    planks as the result, and closing the screen gave the logs back. The server without Curios
    of source change 47 reached `Done`. Not tested: a client and a server that both lack Curios.
    The integration raised the Curios range in `mods.toml` from `[5.0,)` to `[5.2.0+1.20.1,)`, the
    form that Mekanism 10.4 uses for the same bound. The API jars of Curios `5.2.0-beta.3+1.20.1`
    and `5.2.0+1.20.1` from Modrinth show that `CuriosApi.getCuriosInventory` and
    `ICuriosItemHandler.findFirstCurio`, which `CuriosCompat` calls, first appear in 5.2.0 (javap),
    and the entity files under `curios/entities` came with 5.2.0-beta.1 (`docs/CHANGELOG.md` of the
    Curios branch 1.20.x). With an older Curios the hotkey would throw a `NoSuchMethodError`. The
    pack ships 5.14.1.
50. Drills: the drill traces the block it mines within the Forge block reach (`d36cb9c7`,
    `cf8e56ab`, `2fbf2984`), so the 3x3 and 5x5 areas lie on the face the player mines. The base
    traced with a `WorldUtil` stub that always answered the bottom face of 0, 0, 0, and the area
    always lay flat. The new helper `Util.getReachDistance` reads the attribute
    `ForgeMod.BLOCK_REACH`, which `NeoForgeMod.BLOCK_REACH` replaced on `main`. The drill tag gets
    the speed of the augments (`d9961a39`), the drop check knows the current power (`ed8ea0e6`)
    and the drill is never damageable (`424427fd`). The attack attributes are built once under
    the base attack UUIDs of `Item` and give 9 attack damage and 1 attack speed when charged
    (`05138857`, `0ebbd811`, `8977b3ba`, #1461). The placer augment lets the hotbar stack place
    itself with a context of its own (`7e7fd0d8`, `846172e6`, `9bd1fa63`), so `ItemStack#useOn`
    posts the Forge place events and the hotbar stack shrinks. The base placed the block from a
    copy of the drill stack. The client outlines the extra blocks of the area (`f390ffd4`). The
    tooltip lists the installed augments and the placer slot (`d84518fe`, `b7875a09`, #1463), and
    the augment prints its slot (`ccfb8d16`). The drill screen refuses a higher speed tier without
    the lower one, keeps the lower one while a higher one is in and refuses duplicates
    (`75c3757e`, #1488, changelog 1.3.24). The 16 shapeless recipes
    `actuallyadditions:drill_coloring/dye_drill_<colour>` dye a drill and keep its tag, with the
    keep data recipe of `e0a8eed3` (new serializer `actuallyadditions:copy_nbt_shapeless`) and the
    target ingredient of `db9095e0` (#1459). The lang drops the `(wip)` labels of the drills,
    augments and drill core (`154add17`) and names the area augments as `d1438dd6` does.
    Local deviations from `main` in this change:
    - Upstream moved the area break into a `BreakEvent` listener for 1.21 (`32625541`, `92dfcb47`).
      The 1.20.1 port keeps `Item#onBlockStartBreak` and guards it with a `ThreadLocal` flag, so a
      vein miner that breaks more blocks inside the break event breaks only its own block on a
      nested call. Main guards the same case with a check derived from Apotheosis.
    - The outline helpers that `main` keeps in `AssetUtil` live in the client only class
      `ClientEvents`, so no lambda of a common class names the client vertex classes (the dist
      scan of the bundle, `tl-scan1.txt`).
    - The keep data recipes copy the donor's energy as well, the 1.20.1 form of the attachment
      copy of `99ea93eb` (`RecipeKeepDataShaped.copyData`). The energy capability writes the NBT
      key `Energy` only when the stack is saved or copied, so the key can lag behind the storage.
      The copy happens only when the donor holds more than 0.
    - The ingredient factory `TargetNBTIngredient.of` takes a `TagKey<Item>` as in `e0a8eed3`.
      With the raw `TagKey` javac bound the call to `Ingredient.of(TagKey<Item>)`, which wrote a
      plain tag ingredient.
    - The upgrade checks read the NBT once per call and never add an empty tag to a drill they
      only read.
    Savegame: the drill keeps the NBT keys `Energy` and `Items`, and the augment keeps
    `SlotToPlaceFrom` with its slot plus one encoding. The off by one that `b7875a09` fixes came
    with the 1.21 data component and never existed in this NBT form. New ids: the 16 recipes and
    the serializer above.
51. All in one tools: the AIOTs till, flatten and strip (`4d3ba552`, `9e0ce035`). The Forge hook
    `IForgeBlock#getToolModifiedState` asks the stack in the hand whether it can perform the
    action, and the AIOT answered only the four dig actions, so it never changed a block. It now
    performs `HOE_TILL`, `SHOVEL_FLATTEN` and `AXE_STRIP` as well. Without sneaking it strips
    like an axe and else tills like a hoe, and while sneaking it flattens like a shovel, with the
    corrected check of `6686f1f4`. The unused `DrillAugmentItem`, which `ItemDrillUpgrade`
    replaced, goes, and the AIOT no longer implements the empty `IActuallyItem` (`a678b3e0`). The
    vanilla tool tags of `4789e61e` (#1450) came with source change 9. The damage and attack speed
    fixes `9a418c1c` and `906aa08e` (#1424, changelog 1.3.6 and 1.3.7) repair a 1.21 regression
    and are not needed here, since `DiggerItem` already gives 4 plus the tier's attack damage
    bonus and an attack speed of minus 2 under the base attack UUIDs.
    Local deviations from `main` in this change:
    - The strip check asks `consumesAction()` instead of `SUCCESS`, since the axe answers
      `CONSUME` on the server. With the check of `main` the server would also try the hoe on the
      block the axe had just stripped.
    - The AIOTs also join `forge:tools/pickaxes`, `axes`, `shovels` and `swords`, and
      `forge:tools/paxels`. Forge 47 itself defines none of them (source change 9), but pack mods
      do. Farmer's Delight 1.3.4 ships the pickaxe, axe and shovel tags, Immersive Engineering
      10.2.0 all four, and Mekanism Tools 10.4.16 and Mystical Agradditions 7.0.12 the paxel tag
      (file lists of the jars in the pack). The new public constants `ActuallyTags.Items.TOOLS_*`
      name these tags.
    Savegame: none. New ids: entries in the five forge tool tags.
52. Tools and other items: the Bowl of Water picks up water again. The base left its registration
    on the Forge bus commented out (`9f852348`). Its trace stops at source blocks, and the water
    leaves through `BucketPickup`, so a waterlogged block keeps its block (`ecb8d46f`,
    `3191a63d`, #1499, PR #1500). Placing water traces within the Forge block reach. The Handheld
    Filler takes its second corner from the block the player looks at (`d1438dd6`, `2fbf2984`)
    and is named `Filler` as on `87b02617`, with the registry id `handheld_filler` unchanged. The
    `WorldUtil` stubs of the base go (`ecb8d46f`). The Teleport Staff pays 200 plus 200 times the
    square root of the Manhattan distance (`c812512a`, `bae1ef5e`), where the base charged 200
    per square block, so any jump past a few blocks cost more than a full staff. Worms till the
    dirt and grass around them again and water a 3x3 of farmland (`a6f50e2f`). Hoeing a block of
    the block tag `actuallyadditions:worm_drop` (`e37019a5`) rolls the new loot table
    `actuallyadditions:worm_drop` (`159fd67c`, `02ca59fe`, `ba90c665`, `c8045ff5`) with the same
    chance of 5 percent for one or two worms as before. A missing player rolls with luck 0
    (`acb52994`, #1489). The worm renderer accepts a worm without a name (`ce0e39c4`, `af5c1389`,
    changelog 1.3.25) and the entity gets its name (`958aead8`), so a worm summoned by command no
    longer crashes the client. The fertilizer, the Laser Wrench, the Phantom Connector and the
    Player Probe use the stack of the context and message only a present player (`e08ef13a`).
    The Ring of Growth takes the empty check of `ac4d53bd`. The recipes of the filler, the ring
    and the Ender Star came with source change 10.
    Local deviations from `main` in this change:
    - The click that fills a bowl ends with a swing, since the bowl is used up.
    - The filler skips a trace that misses and reads its block without adding an empty tag.
    - A tilling that a mod only simulates drops no worms.
    - The id of the worm loot table lives in the new class `LootTableUtil`
      (`LootTableUtil.WORM_DROP`), so runtime code never reaches into the data generator.
    Local deviations from the review at the integration (`693daa2`):
    - The filler saves the clicked block in its default state with only its orientation (the
      vanilla properties `facing`, `axis`, `half`, `face` and `rotation` and the slab `type` with
      a double slab as a bottom slab), never waterlogged, and
      normalizes a state it reads back the same way. The full state of `main` let a double slab,
      eight snow layers, four sea pickles, a full composter, a beehive full of honey or mature
      wheat come out of the filler for one item and drop their whole loot when broken. On the
      base that did not matter, since the filler never filled. A saved block that reads as air,
      such as a block of a removed mod, counts as no block.
    - The filler skips a position that spawn protection, the world border or adventure mode
      keeps from the player (`Level#mayInteract`, `Player#mayUseItemAt`) and posts the Forge
      place event for every block. Arclight hands that event to Bukkit as a `BlockPlaceEvent`.
      A cancelled place is undone with the snapshot, costs no item and no energy and ends the
      job. The filler looks for the item first and takes it only after the event.
    - The Bowl of Water posts the Forge break event for the water it picks up and for a block
      that its water washes away, and the place event for the water, which it takes back on a
      cancel. The spill tick of the config value `waterBowlSpilling` checks spawn protection and
      adventure mode like a placed bowl. A refusal on the server sends the inventory again, since
      the client already showed the changed bowl. The slot check of the spill tick no longer
      reads the one slot off hand list with the index of an armor slot above 0.
    - The Teleport Staff jumps on a miss only while the player looks no more than 5 degrees
      upward, as on the base and as the manual says. Upstream `c812512a` jumps on every miss and
      throws a player who looks up 100 blocks into the air.
    Savegame: the worm keeps `Timer`, the bowl `lastX` and `lastY`, and the filler `state` with
    the keys `First*`, `Second*` and `Curr*`. The key `state` now holds a normalized block state.
    New ids: the loot table `actuallyadditions:worm_drop`. The block tag of that name was already
    there.
53. Engineer's Goggles: the infrared goggles outline the entities within 8 blocks for the wearer.
    The base set the glowing tag of the client copies of the entities, but the client draws the
    outline from the glowing flag that the server syncs, so nothing glowed. As in `3b297993`
    (#1523, changelog 1.3.26) the client mixin `client.MinecraftMixin` on
    `Minecraft#shouldEntityAppearGlowing` (refmap `m_91314_`) asks `ClientEvents.shouldGlow`, so
    no entity data changes on either side. The tick already lives in `ClientEvents` since source
    change 13 (`844efe42`). The rendering fix of `6159fda4` (#1449) repairs the 1.21 armor layers.
    On 1.20.1 the material name `actuallyadditions:goggles_armor_material` already resolves to
    `goggles_armor_material_layer_1.png`, the same texture as `goggles_layer_1.png` of `main`, and
    the unused `goggles_armor_material_layer_2.png` goes as in `6159fda4`.
    Local deviations from `main` in this change:
    - The goggles keep the entities in a static set only and look them up there, where `main`
      marks them with `hasGlowingTag`. The set empties when the goggles come off or the client
      leaves the level, and drops an entity that died or left the range or the level.
    - From the review at the integration (`693daa2`), the wearer is left out of the set, so the
      own model no longer glows in third person view.
    Savegame: none. New ids: none.
    The dedicated Forge 47.4.18 server of the bundle test, with Patchouli, Curios, JEI and
    Mekanism next to the jar of the integration (SHA-1
    `eec659a2c452a4043ed1ad7c39e2d084f7394161`), reached `Done` with 0 FATAL and 0 ERROR lines and
    no crash report. A worm summoned by command lived, the loot table
    `actuallyadditions:worm_drop` loaded and rolled, and the client mixin stayed off the server
    (`bp2/actuallyadditions/srv-tlint1/`). Not tested in game: the drill areas with fortune and
    silk touch, the placer augment, the drill screen, the dye recipes, the AIOT actions, the bowl,
    the filler with its normalized state and its events, the staff and the goggles. The ship stage
    runs those tests (carryover `aa-ship`).
54. Crops and the Tiny Torch: a ripe crop drops as on 1.12 (`506d481d`, changelog 1.3.11). Canola
    gives two to five canola, rice one to three rice, flax two to six string and coffee two to four
    beans, each with one or two seeds, and both pools take the uniform fortune bonus. An unripe
    crop drops its seed. The four generated loot tables are byte identical to the `loot_table`
    files of `main` at `cd9d9c6`. A right click on a ripe crop gives the drops minus one seed to
    the player and sets the crop back to age 0. On `main` the click goes on to the held item after
    the harvest, which spends bone meal on the replanted crop on the server while the client still
    sees the ripe one. Here the harvest consumes the click and swings the hand, as the sweet berry
    bush does. The four crops get the block state property `persistent` of `38d6404b`, default
    `false`. A crop with the flag set survives a block update on any block that is not air, so the
    patches of the world generation, which set it, no longer pop off their grass. Inside a
    `WorldGenRegion` the flag counts for nothing, so the placement still checks the soil. The
    blockstates keep their eight `age` variants, which match both values of the new property, so
    no state lacks a model, and the data generator, which checks that every state is covered,
    writes them unchanged. The seeds and crops go into the vanilla composter table in common setup
    with the chances of the generated data map of `3a2f298e` (#1431, changelog 1.3.8). Canola,
    coffee beans, flax seeds and rice compost at 0.65, canola and rice seeds at 0.3. On `main` the
    crop entries of coffee beans and flax seeds replace their seed entries, and the port keeps
    those effective values. The furnace fuel half of `3a2f298e` is not needed, as the tiny coal and
    tiny charcoal return a burn time of 200 themselves. The Tiny Torch gives light 12 instead of 8
    (`4cc4b40c`), has no collision (`d3f22bb5`, changelog 1.3.20) and stands upright when the top
    or bottom face of a block is clicked (`a6a9467d`, #1409). The base set the facing `down`, which
    its property does not hold, so the placement threw an `IllegalArgumentException`. The property
    is now built from every direction but down, the same five values as before. Savegame: the new
    property is additive. A crop of an older world has no saved `persistent`, loads with its age
    and `false` and behaves as before. The Tiny Torch keeps its five facings.
55. Black quartz ore: the base generates nothing, as `mod/gen/AAWorldGen.java` is commented out
    in full. The ore generates as on `main` (`02422d65`, `97c7a969`, `ec4b79ff`). The configured
    feature `actuallyadditions:ore_black_quartz` is a `minecraft:ore` of size 6 with no discard
    chance, whose two targets `minecraft:stone_ore_replaceables` and
    `minecraft:deepslate_ore_replaceables` both place `black_quartz_ore`, since no deepslate
    variant exists. The placed feature of the same id has a count of 8, `in_square`, a trapezoid
    height range from Y -25 to 45 and the `minecraft:biome` filter, which keeps each placement in
    the biomes that the modifier gave the feature to. The new classes `ActuallyConfiguredFeatures`,
    `ActuallyPlacedFeatures` and `ActuallyBiomeModifiers` feed the `DatapackBuiltinEntriesProvider`,
    and the record `gen/modifier/BoolConfigFeatureBiomeModifier` adds its features in the phase
    `ADD` when the named switch of the common config is on. Its serializer
    `actuallyadditions:bool_config_feature_modifier` is a plain `Codec` in
    `ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS`, as Forge 47 wants it (`main` registers a
    `MapCodec`), and the modifier `add_black_quartz` lands in
    `data/actuallyadditions/forge/biome_modifier/` instead of `neoforge/biome_modifier/`. It names
    `#minecraft:is_overworld` and the switch `generateQuartz` of source change 6. The three
    generated files match those of `main` at `cd9d9c6` byte for byte. The condition codec
    `bool_config_condition` that `main` registers in the same class is a NeoForge recipe
    condition, and the base already has its Forge 47 form. In the SkyTekx Skylands the void
    columns carry the biome `skytekx3:sky_void`, which no overworld tag lists (only
    `#skytekx3:has_structure/sky_floaters` and two Dungeons Arise tags name it, see
    `skylands-datapack/data/`), so the biome filter drops every placement there, and the 14 island
    biomes that `skylands-datapack/data/minecraft/tags/worldgen/biome/is_overworld.json` adds get
    the ore. The ore drops black quartz with the ore fortune bonus and itself with silk touch
    (`abbc1ae9`, `005b3ccc`, ticked in `port.MD` as upstream did). The base dropped the ore block,
    marked as temporary. Savegame: the world generation only reaches chunks that do not exist yet.
