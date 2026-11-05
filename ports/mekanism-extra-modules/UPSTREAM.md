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

## Source changes

All source changes are dated 2026-11-05. The places where the port had to work differently from
upstream carry a comment that starts with `SkyTekx port`. The behaviour stays upstream's unless a
row says otherwise.

| File | Change |
|---|---|
| Class `MekanismExtraModules` | Forge 47 entry point: the mod bus from `FMLJavaModLoadingContext`, the server config through `ModLoadingContext`, the game listeners on `MinecraftForge.EVENT_BUS`. |
| Class `registry.ExtraModuleRegistry` | Forge `DeferredRegister` and `RegistryObject` instead of NeoForge `DeferredHolder`. Mekanism 10.4's builder `ModuleDataBuilder.custom` replaces 10.7's `customInstanced`, and the module item is named through an `IItemProvider`. The creative tab and the IMC to the MekaSuit body armor are upstream's. |
| Class `registry.ModuleProvider` | New. Mekanism 10.4 takes an `IModuleDataProvider` where 10.7 takes a `Holder<ModuleData<?>>`. This one wraps a module's registry object and takes its id from there, so the module items can be created before the module registry is filled. |
| Classes `module.PhaseGuardModule`, `module.EmergencyRevivalModule` | The 10.4 signature of `addHUDElements` (module, player, consumer) with the suit stack from the module itself. Energy is a Mekanism `FloatingLong`. |
| Class `service.EnergyService` | Joules as `FloatingLong`. The module is loaded with `IModuleHelper.load`, and a 10.4 module draws energy from the stack it was loaded from. |
| Class `service.SuitModuleService` | Module lookups through `IModuleHelper.load` and `IModuleHelper.isEnabled`, because 10.4 has no `IModuleContainer`. Item ids through `ForgeRegistries`. |
| Class `service.ProtectionService` | Forge 47 events. `TickEvent.PlayerTickEvent` at the end of the server tick replaces `PlayerTickEvent.Post`. Forge 47 has no `EntityInvulnerabilityCheckEvent`, so Phase Guard cancels `LivingAttackEvent` at the highest priority. That event is the first hook of `Player.hurt` and fires after vanilla's own `isInvulnerableTo` check, so no other listener sees the blocked hit. A mod that calls `actuallyHurt` directly skips `Player.hurt` and that event, while upstream's invulnerability check also stops `actuallyHurt` at its first line. So Phase Guard also cancels `LivingHurtEvent` at the highest priority, the first hook of `actuallyHurt` in Forge 47, which fires before armor, absorption hearts and Mekanism's MekaSuit absorption, a `LivingHurtEvent` listener that draws suit energy (`CommonPlayerTickHandler.onLivingHurt` of Mekanism 10.4.16). Forge's `LivingDamageEvent` replaces `LivingDamageEvent.Pre` and stays as the last backstop. Mob effects are plain `MobEffect` values in 1.20.1. |
| Classes `service.ChaosCompatService`, `config.ExtraModulesConfig`, `mixin.ExtraModulesMixinPlugin` | Forge packages: `ModList`, `ForgeConfigSpec` and `LoadingModList`. Same config file, keys, defaults and ranges. |
| Class `service.ProtectionService`, own fix | The player tick handler skips dead players. A dead player keeps ticking until respawn, and `Player.tick` fires the post tick event in that state too. Upstream would then draw suit energy and set the dead player's health back to `minHealth` whenever the suit survives the death, with keepInventory or after a death that Phase Guard let through, such as `/kill`. The check keys on a death that went through and not on health. A `LivingDeathEvent` listener at the lowest priority, which only sees deaths that nobody canceled, notes the player in a set that lives in memory only, and the clone and logout handlers clear it. A player whose health a mod wrote to 0 without a death is therefore still rescued by the tick, as upstream does. |
| Class `mixin.LaserBeamPhaseMixin` | Still two `@Redirect` injections, on `Player.setHealth` and `Player.die` in `LaserBeamPhase.serverTick`. The two call targets are Minecraft methods, which Forge 1.20.1 runs under SRG names, so their `@At` sets `remap = true` and the refmap maps them to `m_21153_` and `m_6667_`. The method name `serverTick` stays unmapped. Checked against the bytecode of Draconic Evolution 3.1.2.621. |
| File `mekanism_extra_modules.mixins.json` | Java 17 compatibility level, the refmap, `defaultRequire` 1 so a changed target shows up as an error in the log, and `@Overwrite` forbidden. The config stays not required and keeps upstream's plugin, which skips the mixin without Draconic Evolution. |

## Data changes

All data changes are dated 2026-11-05. Models, textures and the 30 language files stay unchanged,
because their 1.21.1 format is the same in 1.20.1.

| File | Change |
|---|---|
| Folder `data/mekanism_extra_modules/recipes/` | The three shaped recipes move from the 1.21 folder `recipe/` to the 1.20.1 folder `recipes/`, and the result names its item with `item` instead of `id`. Patterns, results and the Mekanism item inputs (Energy Tablet, Laser Dissipation Unit, Phase Guard Unit, Totem of Undying) stay. The circuit, alloy and obsidian inputs use the forge tags that Mekanism 10.4 itself declares and uses in its own module recipes (`forge:circuits/ultimate`, `forge:alloys/ultimate`, `forge:alloys/elite`, `forge:obsidian`), so AlmostUnified and other mods that fill those tags work with them. Each tag holds exactly the Mekanism item upstream names (checked in `Mekanism-1.20.1-10.4.16.80.jar`). |
| File `pack.mcmeta` | Pack format 15 (Minecraft 1.20.1) instead of 48. |

## Later upstream changes

The port takes these changes from upstream commits after the base, dated 2026-11-05 here.

| Upstream commit | What the port takes |
|---|---|
| Commit `db624015cc5b3fbd37e7abb41d6f9f8345ad3eb6` (2026-10-27) | Only the `/kill` bypass: Phase Guard and Emergency Revival ignore the damage type `minecraft:generic_kill`, so an operator can still remove a protected player. In 1.20.1 `LivingEntity.kill()` hurts with `genericKill()` (checked in the Forge 47.1.70 userdev jar). The class `service.ProtectionService` changes. |

What the port leaves out of the later upstream commits, and why:

| Upstream commit | Left out | Reason |
|---|---|---|
| Commit `576425a` | The config screen and its 30 translated label sets | It uses NeoForge's `ConfigurationScreen`, which Forge 47 lacks. The server config file and its comments stay the way to tune the modules. |
| Commit `1ce73d3` | Dropping the `strictDraconicOnly` check and option | The check only looks at the death message id. Draconic Evolution 3.1.2.621 names its laser damage type `draconicevolution.guardian_laser` (`data/draconicevolution/damage_type/guardian_laser.json`), which the check accepts, so the base behaves the same. |
| Commit `db62401` | The `@WrapWithCondition` injections, the `[[mixins]]` entry of `neoforge.mods.toml`, Phase Guard state in memory, the body armor only lookup, the removal of the Mekanism Tools dependency and of the `debug` and `allowMixinProtection` options | The license review of this port keeps upstream's two `@Redirect` injections of the base. Forge 47 reads the `MixinConfigs` manifest entry, so the mixin already loads. The other parts are refactors without a behaviour change on 1.20.1, and the base config keys stay stable. The pack ships Mekanism Tools anyway. |

## License files and notice

Upstream jars carry no license file. The port adds these files on 2026-11-05, in this directory and
in the jar.

| File here | In the jar | What it is |
|---|---|---|
| File `LICENSE` | Entry `LICENSE_mekanism_extra_modules` | Upstream's unchanged LGPL v3 text. |
| File `GPL-3.0.txt` | Entry `LICENSE_mekanism_extra_modules_GPL-3.0` | The GPL v3 text, which the LGPL v3 incorporates. It is byte identical to https://www.gnu.org/licenses/gpl-3.0.txt (35149 bytes, SHA-256 `3972dc9744f6499f0f9b2dbf76696f2ae7ad8af9b23dde66d6af86c9dfb36986`, fetched on 2026-11-05). |
| File `Mekanism-MIT.txt` | Entry `LICENSE_mekanism_extra_modules_Mekanism-MIT` | Mekanism's MIT license with its copyright notice for the derived textures, byte identical to `LICENSE` of the Mekanism branch `1.21.x` (https://raw.githubusercontent.com/mekanism/Mekanism/1.21.x/LICENSE, SHA-256 `38ec6b47166e816850c10da7962a1d80c47380cbf76a05eef9da0f103a6abb75`, fetched on 2026-11-05). The branch `1.20.x` has the same text with the years 2017 to 2023. |
| File `NOTICE` | Entry `NOTICE_mekanism_extra_modules` | States that this is an unofficial modified build, names the upstream URL and commit, lists the changed files with the date, the license and where the Corresponding Source is, and names the Mekanism derived textures. |

The file `mods.toml` keeps `license="LGPL-3.0-or-later"` and `authors="Lumysia"`, and its credits
name the upstream commit, the license files and the source of the build. The jar manifest names the
vendor as an unofficial SkyTekx build.

## Build and test results

The jar `mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar` has SHA-1
`ca7f070e3f78405968a6fe93257fe235e4f3e925` and 658,607 bytes. A second build from a clean
`git archive` of this directory in another path gave the same SHA-1 (2026-11-05).

Tests on 2026-11-05, on plain Forge 47.4.18 servers with Java 17, `-Xmx3G`, Mekanism and Mekanism
Tools 10.4.16.80 and the jar, all three from the files of the pack's `modrinth.index.json`. A scratch
harness mod that is not part of this source drove a `ServerPlayer` without a client, ticked it as the
server does and called the damage paths. RCON ran every check.

| Check | Result |
|---|---|
| Boot without Draconic Evolution | `Done` with 0 ERROR and 0 FATAL lines. The mod logs `draconicevolution=false`, and the mixin plugin skips the Draconic Evolution mixin. |
| Boot with Draconic Evolution 3.1.2.621, Brandon's Core 3.2.1.302, CodeChicken Lib 4.4.0.528 and SkyTekx DE Fixes 1.0.0 | `Done` with 0 ERROR and 0 FATAL lines. The exported `LaserBeamPhase` (`-Dmixin.debug.export=true`) has both redirects of this mod and the `@ModifyConstant` of SkyTekx DE Fixes in `serverTick`, so the two mods do not collide. |
| Registries | Three items of the class `ItemModule` and three Mekanism modules. Each module lists `mekasuit_bodyarmor` as its only supported item, so the IMC reached Mekanism. Phase Guard and Emergency Revival render a HUD element, and all three start disabled. |
| Items | Each item summoned as an item entity and read back with `data get`. |
| Recipes | 3 of this mod among 2758 recipes (1198 crafting) without Draconic Evolution, 2937 with it. Each recipe, filled with the first item of each ingredient tag, matched and crafted its result. |
| Phase Guard | Magic damage of 5 and 1000, void damage and a direct `actuallyHurt` all did nothing while the module was on. It drew 1,250,000 J per tick (500,000 FE). Without the module, magic damage of 5 took 5 health. |
| Emergency Revival | Lethal magic damage left the player alive with 4 health, Absorption II, Regeneration II and Fire Resistance, removed Poison and Wither, kept Haste and drew 25,000,000 J (10,000,000 FE). With an empty suit the player died. |
| Chaos Anchor | Only with Phase Guard active, `protectDirectLaserHealthWrite` and `protectChaosLaser` with the Draconic Evolution damage type `draconicevolution.guardian_laser` returned true. Without Phase Guard, or with a generic damage source, they returned false. |
| Kill command and dead players | With Phase Guard and Emergency Revival on and keepInventory true, `kill()` killed the player, and over the next 20 ticks its health stayed 0 and the suit energy did not change. |
| Respawn copy | The clone event copied the mod's player data with the Phase Guard flag cleared. |
| Client classes | The class load log of the dedicated server shows 37 classes of this mod and none of a client package. |
| Arclight | The pack's hardened Arclight 1.0.6 jar with the same mods booted with 0 ERROR lines. Phase Guard, Emergency Revival, the kill bypass and the dead player check behaved as above. Arclight's `ServerPlayer.die` calls Forge's `onLivingDeath` before Bukkit's `PlayerDeathEvent`, so a revival never fires that event, and `Player.hurt` fires `LivingAttackEvent` before Bukkit's `EntityDamageEvent`. |

Not tested: a real client, the HUD icons and the module screen, and a Chaos Guardian fight.
