# Ender IO: vendored upstream source

`ports/enderio/` is the source of the SkyTekx build of Ender IO for Minecraft 1.20.1 / Forge 47.
It started as a plain snapshot of upstream's `1.20.1` branch. Later backports from the 1.21.1 line
are commits on top of that snapshot in this repository, see `okf/modpack/` for the plan.

## Upstream

| | |
|---|---|
| Repository | https://github.com/Team-EnderIO/EnderIO |
| Ref | branch `1.20.1` |
| Commit | `18ee1b17bf6879298e911fff751204b07f2e4228` |
| Commit date | 2026-03-16 22:25:44 UTC (Reece Mackie, "chore: Try to change how we break the block when wrenched to fix an obscure issue") |
| Last release on the ref | `v6.2.18-beta` = `907a9c3721730957fad6794ceed9ed0f591dabac` (2026-03-08) |
| Unreleased commits in the snapshot | `f9e1806c` "fix: Never trust inventory NBT Size key." (2026-03-16), `18ee1b17` (wrench block break) |
| Port source for later backports | branch `1.21.1` (Ender IO 8.2.x, NeoForge 1.21.1); merge-base with `1.20.1` is `41ed3d5e869f888c588d2460ebf9fcf8540fd1ab` |
| Reference only | branch `experiment/1.20.1-backport` (8 commits by Reece Mackie, 2026-03, on `1.21.1@010b057e`); read for API translations, its 8.x core is not used |

The snapshot was made with

```bash
git -C <clone> archive 18ee1b17bf6879298e911fff751204b07f2e4228 | tar -x -C ports/enderio
```

so it has no nested `.git` and keeps every upstream file, including `LICENSE.txt`, `doc/license/`
and the committed datagen output in `src/generated/resources/`. Upstream's `.gitattributes`
(`* text=auto`) applies inside this directory, so git stores `build.gradle`, `.gitattributes` and
`.gitignore`, which upstream committed with CRLF line endings, with LF. No other file changed.

## License summary

- Ender IO code and assets: **The Unlicense** (`LICENSE.txt`). Upstream documents third-party grants
  in `doc/license/`: NeepNoop's dust textures and DTFuel's alloy textures are CC0, and the
  TravelAnchors contributors allowed their code to be copied and relicensed.
- Code from other projects inside Ender IO's own classes:
  - **Patchouli** (Vazkii), **CC BY-NC-SA 3.0 Unported**: `GhostBuffers`, `GhostRenderLayer` and
    `initBuffers` in `src/machines/.../client/gui/widget/ioconfig/IOConfigWidget.java`, and the block
    rendering in `src/main/.../integrations/jei/category/FireCraftingCategory.java`. Because of these
    parts the built jar may only be distributed **non-commercially**, and changes to those parts
    are shared alike.
  - **Commoble** (Joseph Bettendorff), MIT: `src/machines/.../souldata/SoulDataReloadListener.java`.
  - **Mekanism** (Aidan C. Brady), MIT: `src/machines/.../io/fluid/IFluidItemInteractive.java` and the
    temp-file save in `src/conduits/.../network/ConduitSavedData.java`.
  - **Apothic Spawners** (Stormraven Studios, LLC), MIT: the movement step for mobs without AI in
    `src/machines/.../blockentity/task/SpawnerMachineTask.java` (`tickNoAIMobs`), which Ender IO
    84bfe2477 took from its `ASEvents` class. The text is `licenses/ApothicSpawners-MIT.txt`.
- Libraries the build packs into `META-INF/jarjar/`: Registrate (MPL-2.0), GraphLib3 (MIT,
  gigaherz / David Quintana), and GraphLib3's dependencies Guava, failureaccess, listenablefuture,
  Error Prone annotations, JSR 305 (Apache-2.0) and checker-qual (MIT).

## Building

Java 17 only (the build's toolchain is 17, and the pack's Arclight server dies on Java 21), and a
UTF-8 locale:

```bash
cd ports/enderio
LC_ALL=C.UTF-8 JAVA_HOME=/opt/jdk17 PATH=/opt/jdk17/bin:$PATH ./gradlew build \
  -Porg.gradle.java.installations.paths=/opt/jdk17 -Dorg.gradle.jvmargs=-Xmx2g --console=plain
```

Upstream has one file name that is not ASCII, `src/main/resources/assets/enderio/lang/no_no<U+200C>.json`
(a stray zero-width non-joiner; the release jar has it too). Under the POSIX/C locale Java cannot
encode that name, and `processResources` fails with "Failed to create MD5 hash for file
.../no_no???.json as it does not exist". `compileJava` alone does not touch resources and works in
any locale.

`build/libs/EnderIO-1.20.1-6.3.0-skytekx.1-all.jar` is the jar for `local-mods/` (the jarJar jar
with Registrate and GraphLib3 inside, like upstream's release `-all` jar). `build` also writes the
plain jar without the bundled libraries, `-api` and `-sources`. A quick check is
`./gradlew compileJava` with the same options.

## Build changes against upstream

Everything that SkyTekx changed in the build files is marked `SkyTekx:` in a comment there.

- **Fixed version** `6.3.0-skytekx.1` (`mod_version` in `gradle.properties`). Upstream derives the
  version from the `BUILD_VERSION`/`NIGHTLY` environment or, locally, from `git rev-parse` of the
  enclosing repository, which in this repository would give `6.2-dev-<branch>-<sha>`.
  `mod_version_series` goes from 6.2 to 6.3, like upstream's own `experiment/1.20.1-backport`
  (`versionSeries=6.3`). The version stays in the 6.x series and above 6.2.18: EnderIO Legacy
  Machines 0.4.9 requires `enderio` `[0,)`, Exchangers 3.5.1 takes `enderio` `[1.0,)` (optional).
- **Release plugins removed:** `com.modrinth.minotaur` 2.+, `me.hypherionmc.modutils.modpublisher`
  1.+ with its `publisher { }` block, and the plugin repository `maven.firstdarkdev.xyz` that only
  served modpublisher. They publish to the official Ender IO Modrinth and CurseForge projects, which
  an unofficial build must never do, and they only ran when `BUILD_VERSION` and `CHANGELOG` were
  set. `maven-publish` stays (its Rover656 repository needs upstream's credentials).
- **Pinned plugins:** ForgeGradle `[6.0,6.2)` -> `6.0.54`, MixinGradle `0.7.+` -> `0.7.38`,
  ModGradle `4.0.+` -> `4.0.6`, which is what the ranges resolved to on 2026-11-04. The Parchment
  librarian plugin stays at `1.+`: it only publishes snapshots, and the build does not use it
  (the mappings channel is sugarcane, from ModGradle).
- **Pinned jarJar libraries:** Registrate `MC1.20-1.3.11` and GraphLib3 `3.0.7`, the jars inside
  the 6.2.18-beta release. `jarJar.pin` keeps upstream's ranges (`[MC1.20-1.3.3,)`, `[3.0.4,3.1)`)
  in `META-INF/jarjar/metadata.json`, so Forge's jar-in-jar selection behaves as before; without the
  pin the bundled jar would be whatever is newest on the maven at build time.
- **Reproducible jars:** every `Jar` task sets `preserveFileTimestamps = false` and
  `reproducibleFileOrder = true`, and the manifest has no `Implementation-Timestamp` any more, like
  `ports/twilightforest`. `Implementation-Vendor` says that this is an unofficial modified build.
- **License files in the jars:** `jar` and `jarJar` add `LICENSE.txt` as `LICENSE_enderio`,
  `NOTICE` as `NOTICE_enderio`, and `licenses/` as `META-INF/licenses/enderio/`.
- **`.gitignore`:** a SkyTekx block for the build and run outputs of this port.
- Curios is a new optional dependency for the charm slot support that the backport takes from 8.2.5:
  the repository `https://maven.theillusivec4.top/` (exclusive for `top.theillusivec4.curios`) and
  `curios-forge` `5.14.1+1.20.1` (`curios_version` in `gradle.properties`, the version that the pack
  ships) as `compileOnly` and `localRuntime`. The jar does not bundle it.

Not changed: the Gradle wrapper (8.8), the repositories of upstream's mod dependencies, the foojay toolchain
resolver, the source sets, the mixin and reobf setup, the compiler settings (no warnings were
turned off), and the committed datagen output.

## Files added by SkyTekx

- `UPSTREAM.md` (this file)
- `NOTICE`: the change notice, shipped as `NOTICE_enderio`
- `licenses/`: the license texts of the third-party code and the bundled libraries
- in `src/main/resources/META-INF/mods.toml`, the `license` field names all licenses (upstream said
  `CC0`), `issueTrackerURL` points to the SkyTekx repository, and the description says that the
  build is unofficial. The optional dependency on `curios` `[5.2.0,)` came with the backport.
  Curios 5.2.0 is the first release with `CuriosApi.getCuriosInventory(LivingEntity)`, which
  `CuriosCompat` calls, and its beta builds sort below it.
