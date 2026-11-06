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
   checks the version range of each one that is present. That check is their only effect, since
   the mod list of Forge 47 shows no dependencies (`ModListScreen` of Forge 47.4.0 shows the name,
   version, credits, authors, URL, child mods, license and description). Upstream declares
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
    Left for the Arclight hardening: the named fake player, block events for the Phantom Breaker,
    and its drops, which `main` still computes at the position and with the block entity of the
    Phantom Breaker itself.
24. Phantomfaces, the Player Interface and boosters: a phantomface hands out a `LazyOptional` of
    its own for the handler of its target. It invalidates it when the target, the block at the
    target or the range changes and when the face is removed or unloaded, and it follows the
    invalidation of the target's own one, so it never hands out an invalidated one again. This is
    the Forge 47 form of the capability invalidation of `ae6695a` and `b8e284d`, which call
    `invalidateCapabilities` on NeoForge. The base returned the target's `LazyOptional` itself,
    so a pipe that cached it kept feeding the old target after the face was bound elsewhere. The
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
