# Upstream: Mystical Automation (1.20.1 port base)

This directory started as a plain source snapshot (`git archive`, no nested `.git`) of Mystical
Automation, an addon for Mystical Agriculture that adds machines for infusion, crafting, farming,
fertilizing and the altars. Upstream only builds it for NeoForge 1.21.1 and newer. SkyTekx ports it
to Forge 1.20.1, against the Mystical Agriculture and Cucumber builds that the pack already ships.

| Field | Value |
|---|---|
| Repository | https://github.com/BlakeBr0/MysticalAutomation |
| Ref | Branch `1.21` (upstream has no git tags) |
| Commit | `4149601b9c443eb7de5ebab6ecc9b6bdd7d6462d` ("Release 1.0.8") |
| Commit date | 2026-10-02T20:10:48-05:00 |
| Upstream version | `1.21.1-1.0.8` (NeoForge 21.1.18, Parchment 2024.07.28, Cucumber 8.0.16, Mystical Agriculture 8.0.19, Mystical Agradditions 8.0.7, JEI 19.16.1.156) |
| Released as | Modrinth version `HdlBF75n`, file `MysticalAutomation-1.21.1-1.0.8.jar`, SHA-1 `586be8e7f3fedc700e4bbf845a55c4758f12db7e` |
| Not used | Branch `26.1` (Minecraft 26.1, release line 2.0.x) |

The commit is the head of branch `1.21`. Every resource in the released 1.0.8 jar is byte identical
to `src/main/resources` at this commit, apart from the expanded `neoforge.mods.toml` (license review
of 2026-11-05).

Apart from this file, every file of the first snapshot commit is byte identical to `git archive
4149601` of the upstream repository (187 files, blob hashes and file modes checked). Changes on top of it are separate commits
in this repository's history (`git log -- ports/mysticalautomation`).

## License

- Code and assets: MIT License, `LICENSE` at the ref ("Copyright (c) 2025 BlakeBr0"), kept
  unchanged. Git history shows the file only in the initial commit `b9ae7d9` (2025-05-14). The
  README links it, `neoforge.mods.toml` says `license="MIT"`, and the Modrinth project
  `mystical-automation` (id `wtZsRhqk`) declares MIT.
- No Java file carries a license header or a third party attribution. There is no NOTICE, no
  separate asset license and no contributor agreement.
- The only commits by others are three translations merged by pull request (`zh_cn` in `f3237f9`,
  `pt_br` in `cb997b5`, `ja_jp` in `24b75e1`). They fall under the repository's MIT license through
  GitHub's inbound equals outbound terms (not separately confirmed).
- The 21 PNG textures carry Photoshop metadata from 2025 and no rights fields. No 16x16 block
  texture matches a vanilla texture. The side textures are references to
  `mysticalagriculture:block/machine_side`, which Mystical Agriculture 7.0.24 has as well. The GUI
  backgrounds were not compared pixel by pixel with vanilla (not verified).
- The CurseForge license field (project 1265613) could not be read, because CurseForge answered 403
  (not verified). Every other source says MIT.
- The Gradle wrapper scripts `gradlew` and `gradlew.bat` carry their Apache 2.0 headers. They do not
  go into the mod jar.

The dependencies Cucumber, Mystical Agriculture and Mystical Agradditions are MIT ("Copyright (c)
2018 BlakeBr0") and stay CDN entries of `modrinth.index.json`.

## Build

Upstream builds with NeoForge ModDevGradle 2.0.83 on Java 21 for NeoForge 21.1.18. The port replaces
that build with ForgeGradle for Forge 1.20.1 on Java 17.

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew build -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

The jar is `build/libs/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar`, which `reobfJar` rewrites in
place with SRG names. That jar is the one to ship.

- Gradle wrapper: `gradlew`, `gradlew.bat` and `gradle/wrapper/gradle-wrapper.jar` are the copies
  that the Twilight Forest, Create Crafts & Additions and Actually Additions ports use (wrapper jar
  SHA-256 `33ad4583...`, scripts with their Apache 2.0 headers). Upstream's wrapper is Gradle 8.9 for
  ModDevGradle. `gradle-wrapper.properties` names Gradle 8.1.1 and pins `distributionSha256Sum` to
  the official checksum of `gradle-8.1.1-bin.zip`.
- Plugins: `net.minecraftforge.gradle` 6.0.54 and `org.parchmentmc.librarian.forgegradle`
  1.2.0.7-dev-SNAPSHOT, pinned like in `../actuallyadditions` and `../enderio`. Forge 47.4.0 userdev
  with Parchment 2023.06.26, as in `../createaddition`, so the ForgeGradle caches are shared. The pack
  runs Forge 47.4.18.
- Dependencies: Cucumber 1.20.1-7.0.16, Mystical Agriculture 1.20.1-7.0.24 and Mystical Agradditions
  1.20.1-7.0.12 from maven.blakesmods.com, the builds of the pack's `modrinth.index.json`. The task
  `verifyPackJars` checks their SHA-1 against the index before `compileJava`. JEI 15.74.0.233, the
  build of the pack, is on the compile classpath with its API only.
- Project name: `settings.gradle` sets `rootProject.name = 'mysticalautomation'`.
- Version: `mod_version=1.0.8-skytekx.1`. The jar keeps upstream's name form
  `MysticalAutomation-<minecraft>-<version>`. Forge shows the version `1.0.8-skytekx.1`.
- Reproducible jar: the `jar` task sets `preserveFileTimestamps = false` and
  `reproducibleFileOrder = true`, and the manifest has no timestamp.
- License files in the jar: the `jar` task adds `LICENSE` (upstream's, unchanged) and `NOTICE`
  (SkyTekx's) to the jar root as `LICENSE_mysticalautomation` and `NOTICE_mysticalautomation`. MIT
  asks for the license text in every copy, and the official 1.0.8 jar carries none. The manifest's
  `Implementation-Vendor` reads `Mystical Automation (modified build: SkyTekx)`.
- Mod metadata: `META-INF/neoforge.mods.toml` became `META-INF/mods.toml` for Forge 47. It keeps
  `license="MIT"`, the mod id, the display name, the authors and the description, and adds a line
  that the build is unofficial. The field `issueTrackerURL` points to
  https://github.com/skytekx/skytekx3-1201-server/issues, `credits` names the unofficial port, and
  upstream's `updateJSONURL` (api.blakesmods.com, 1.21 updates) is gone. The dependencies are Forge
  47 or newer, Minecraft 1.20.1, Cucumber 7.0.16 up to 8 and Mystical Agriculture 7.0.24 up to 8,
  each loaded before this mod.
- Ignored files: the fork's `.gitignore` also ignores `*.log` and `.mixin.out`.
