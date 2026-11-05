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
