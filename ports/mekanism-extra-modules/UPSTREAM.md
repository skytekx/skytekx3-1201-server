# Upstream: Mekanism: Extra Modules (1.20.1 Forge port base)

This directory is a plain source snapshot (`git archive`, no nested `.git`) of Mekanism: Extra
Modules, the base of the SkyTekx port of the mod from Minecraft 1.21.1 NeoForge down to Minecraft
1.20.1 Forge 47 and Mekanism 10.4.

| | |
|---|---|
| Repository | https://github.com/Lumysia/mekanism-extra-modules |
| Ref | Tag `v1.21.1-0.1.0-beta.3` (annotated tag object `774116e5e01d91cd982f3e0894f45c9f99737bc2`) |
| Commit | Hash `1548dd6f2d41906e845c626895cb35769a33a61e`, subject "chore(release): prepare 0.1.0-beta.3 (#19)" |
| Commit date | 2026-09-13T11:47:15-04:00 |
| Upstream version | Version `0.1.0-beta.3` in `gradle.properties` (Minecraft 1.21.1, NeoForge 21.1.221, Mekanism 1.21.1-10.7.18.84) |
| Released | Modrinth project `TfCrGUbp`, version `WRuXXPxB`, file `mekanism_extra_modules-0.1.0-beta.3.jar` (SHA-1 `bb4a133c6ba7eb673e9cc60a9906b3a45762150f`) |
| Later upstream commits | Commits `576425a` (config screen), `1ce73d3` (Draconic detection) and `db62401` (protection refactor, mixin loading) on `main` up to `db624015cc5b3fbd37e7abb41d6f9f8345ad3eb6` (2026-10-27), with the same license files |

Every file in the snapshot is byte identical to `git archive 1548dd6` of the upstream repository,
with the one exclusion listed below. Changes made on top of the snapshot are separate commits in
this repository's history (`git log -- ports/mekanism-extra-modules`).

## Files left out of the snapshot

| Upstream file | Why it is left out |
|---|---|
| File `docs/banner.png` | The README banner. It is not needed for the build or the jar. Its C2PA metadata names the OpenAI Media Service API, `gpt-image` and the digital source type `trainedAlgorithmicMedia` (checked with `strings` on 2026-11-05). The README links it by its upstream URL, so the link still works. |

## License

- Code and assets: GNU Lesser General Public License v3.0 or later (`LGPL-3.0-or-later`). The
  `LICENSE` file is the unchanged LGPL v3 text (7652 bytes, SHA-256
  `e3a994d82e644b03a792a930f574002658412f62407f5fee083f2555c5f23118`, byte identical to
  https://www.gnu.org/licenses/lgpl-3.0.txt). The file `gradle.properties` sets
  `mod_license=LGPL-3.0-or-later`, and the README section "License" says the same. The Modrinth
  project declares `LGPL-3.0-or-later`.
- Upstream commit `be44c67` (2026-07-09, "chore: switch license to LGPL-3.0-or-later") is the last
  change of `LICENSE`. The first commit `def113d` used the AGPL v3. No source header, asset license
  or third party notice exists at the base.
- The LGPL v3 incorporates the terms of the GNU GPL v3, so the port ships the GPL v3 text next to
  the LGPL text, in this directory and in the jar.
- Mekanism derived assets, which upstream does not mark: the three item textures
  `textures/item/{chaos_anchor,emergency_revival,phase_guard}_unit.png` share 199 of 256 pixels
  with Mekanism's `textures/item/module_base.png`, `gui/hud/emergency_revival_unit.png` is pixel
  identical to Mekanism's `gui/hud/nutritional_injection_unit.png`, and
  `gui/hud/phase_guard_unit.png` is a recolor of Mekanism's `gui/hud/gravitational_modulation_unit.png`
  (240 of 256 pixels). The pixel counts are against the pack's `Mekanism-1.20.1-10.4.16.80.jar`.
  Mekanism is MIT licensed, so these files keep Mekanism's MIT notice, which the port adds.
- The Draconic Evolution integration is one mixin with two `@Redirect` injections into
  `LaserBeamPhase.serverTick`. It copies no Draconic Evolution code, and the port keeps it that
  way, because Draconic Evolution uses the CoFH "Don't Be a Jerk" license.

## Build changes

Upstream builds with NeoForge ModDevGradle 2.0.146 on Java 21. The port replaces that build with
the ForgeGradle setup of `../skytekx-defixes`, so both share the Forge and Minecraft caches. All
build changes are dated 2026-11-05.

| File | Change |
|---|---|
| File `build.gradle` | Rewritten for NeoForged ForgeGradle 6.0.21, Parchment Librarian 1.2.0.7-dev-SNAPSHOT and MixinGradle 0.7.38 on Java 17, with Forge 47.1.70 userdev and Parchment 2023.07.30-1.20.1. The mixin annotation processor writes `mekanism_extra_modules.refmap.json`. Mekanism, Draconic Evolution, Brandon's Core and CodeChicken Lib are `compileOnly` dependencies, fetched from the Modrinth CDN as the exact files of the pack's `modrinth.index.json`, and the task `verifyPackJars` checks their SHA-1 before every compile. None of them is bundled or committed. The jar keeps upstream's `preserveFileTimestamps = false` and `reproducibleFileOrder = true` and is reobfuscated to SRG names. |
| File `settings.gradle` | Plugin repositories of NeoForged, Parchment and Sponge, and the Foojay toolchain resolver. The project name stays `mekanism-extra-modules`. |
| File `gradle.properties` | Minecraft 1.20.1, Forge, Parchment and Mixin versions, the version `0.1.0-beta.3-skytekx.1`, the `mods.toml` version ranges and the Modrinth coordinates and SHA-1 of the four compile jars. Upstream's mod id, name, license, group and description stay. |
| Files `gradlew`, `gradlew.bat`, `gradle/wrapper/` | Added. Upstream has no wrapper. These are the copies every port in this repository uses (wrapper jar SHA-256 `33ad4583fd7ee156f533778736fa1b4940bd83b433934d1cc4e9f608e99a6a89`), and `distributionSha256Sum` pins the official Gradle 8.2.1 all zip. |
| File `.gitignore` | One exception added, so the wrapper jar is tracked despite upstream's `*.jar` rule. |
| File `src/main/resources/META-INF/mods.toml` | Replaces `neoforge.mods.toml`. Same mod id, license, authors, logo and description. Forge 47 and Minecraft 1.20.1, Mekanism and Mekanism Tools `[10.4.16,10.5)`, Draconic Evolution optional from 3.1.2. The display URL, issue tracker and credits point to this repository, because this is an unofficial build. |

Build check (from this directory):

```
JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew build -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain -q
```

The jar is `build/libs/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar`.
