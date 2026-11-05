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
- Code excerpts that upstream marks as taken from other mods, all of them kept:
  `TileEntityXPSolidifier` (experience formulas excerpted from Ender IO with permission),
  `misc/BannerHelper` (a banner pattern helper excerpted from Additional Banners with permission,
  called from `ActuallyAdditions#setup`), `ClientRegistryHandler` (an empty method whose body is
  commented out, excerpted from Tinkers' Construct with permission) and
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
  backport. The One Probe and Mekanism appear only as `runtimeOnly` dev run dependencies.
- The client fetches `specialPeopleStuff.properties` from upstream's `1.16` branch on GitHub for
  its special renders. The update checker is commented out, so the server makes no network call.

Gaps of the base that the backport has to close, since upstream never released it:

- The block `bio_reactor` has no blockstate, the block `vertical_digger` has no `en_us` name, and
  the items `booklet` and `crate_keeper` have no item model. Upstream fixed all four on the
  1.20.4 line.
- The `pack.mcmeta` still says `pack_format` 6 (the 1.16 value). The data generator cache
  `src/generated/resources/.cache/` ends up in the jar as 18 `.cache/` entries, as it does
  upstream.

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
   That choice belongs to the release.
5. Reproducible jar: the `jar` task sets `preserveFileTimestamps = false` and
   `reproducibleFileOrder = true`, and the manifest no longer has `Implementation-Timestamp`. Two
   clean builds of the same commit give the same SHA-1.
6. Javadoc: the task `javadoc` reads the sources as UTF-8. Upstream's `build` runs it for the
   javadoc artifact, and under a host locale that is not UTF-8 (POSIX here) it failed on the
   copyright sign in the API file headers.
7. Ignored files: the fork's `.gitignore` also ignores `/logs/`, `*.log`, `.mixin.out/` and
   `mcmodsrepo/`.
