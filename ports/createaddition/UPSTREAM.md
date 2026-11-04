# Upstream: Create Crafts & Additions (1.20.1 backport base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Create Crafts &
Additions, the base of the SkyTekx 1.20.1 Forge backport of the mod's 1.21.1 line.

| | |
|---|---|
| Repository | https://github.com/mrh0/createaddition |
| Ref | branch `1.20.1` |
| Commit | `42b6f2d06f0412a7cee787d767449d5b1b3b81b8` ("Merge pull request #1200 from Texaliuz/patch-1") |
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
- Upstream jars carry no LICENSE file.

## Build changes

The snapshot builds unchanged on Java 17 with its own toolchain: ForgeGradle 6.0.6, Forge 47.4.0
userdev, Parchment 2023.06.26-1.20.1, Gradle 8.1.1, Create 6.0.8-289 (slim), Ponder 1.0.91, Flywheel
1.0.5, Registrate MC1.20-1.3.3, JEI 15.20.0.106 and CC:Tweaked (CurseMaven file 4740605). All
repositories in `build.gradle` still resolve. The compiler settings are upstream's (javac defaults,
no `-Werror`; the 36 compiler warnings are upstream's). The changes below make the build
reproducible.

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew compileJava -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

The jar: `./gradlew build` (same flags) writes `build/libs/createaddition-<mod_version>.jar`, which
`reobfJar` rewrites in place. That is the jar to ship, as upstream ships the plain jar. The
`-all.jar` that `jarJar` makes next to it (with MixinExtras 0.4.1 nested) is not shipped: upstream's
release jar does not nest it either, and the mod's code does not use MixinExtras.

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
4. Version: `mod_version = 1.7.3-1.20.1-skytekx.1`, and `build.gradle` uses `version = mod_version`
   instead of `"${minecraft_version}-${mod_version}"`. The jar is
   `createaddition-1.7.3-1.20.1-skytekx.1.jar`, and Forge shows the version
   `1.7.3-1.20.1-skytekx.1` (`mods.toml` uses `${file.jarVersion}`, the manifest's
   `Implementation-Version`). 1.7.3 names the 1.21.1 line being backported; `-skytekx.N` marks the
   unofficial build. Upstream's 1.20.1 jars report `1.20.1-1.3.3`. In Maven version order
   `1.7.3-1.20.1-skytekx.1` is above `1.3.3` but below `1.20.1-1.3.3`, so a dependent range written
   against upstream's full version string (`[1.20.1-1.3.0,)`) would reject it; no mod in the pack
   declares a `createaddition` dependency at all.
5. Reproducible jar: the `jar` task sets `preserveFileTimestamps = false` and
   `reproducibleFileOrder = true`, and the manifest no longer has `Implementation-Timestamp`. Two
   clean builds of the same commit give the same SHA-1.
6. `.gitignore`: also ignores `run-data/`, `classes/`, `mcmodsrepo/`, `.mixin.out/` and logs.
