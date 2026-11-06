---
type: Mod
title: Mekanism Extra Modules port
description: The pack's Forge 1.20.1 port of Mekanism: Extra Modules 0.1.0-beta.3, a 1.21.1 NeoForge addon with three MekaSuit body armor modules, how it was adapted to Mekanism 10.4, what it changes in play, how it was tested, its license terms and the steps for the server's mods folder.
tags: [modpack, mods, mekanism, draconicevolution, port, mixin]
timestamp: 2026-11-06
---

# Mekanism Extra Modules port

Mekanism: Extra Modules is an addon that adds three modules for the MekaSuit body armor. Upstream
builds it only for Minecraft 1.21.1 on NeoForge with Mekanism 10.7 (Modrinth project `TfCrGUbp`,
newest version 0.1.0-beta.3 of 2026-09-13). Since 2026-11-05 the pack bundles a port to Forge 1.20.1
and the pack's Mekanism 10.4.16, built from [ports/mekanism-extra-modules/](../../ports/mekanism-extra-modules).
Upstream is a beta with 677 downloads on Modrinth (API of 2026-11-05), so expect balance changes.

| Property | Value |
|---|---|
| Jar | File [local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar](../../local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar), 659,044 bytes, SHA-1 `3c40952af5145c740fe6f8fc90ac191574bb2b2f` since the review fixes (fa64b7f). The first bundled build of 74bf989 had SHA-1 `ca7f070e3f78405968a6fe93257fe235e4f3e925` |
| Mod | The id `mekanism_extra_modules`, the name, the author entry and the license `LGPL-3.0-or-later` stay upstream's. The version carries the suffix `-skytekx.1` |
| Upstream | Repository https://github.com/Lumysia/mekanism-extra-modules, tag `v1.21.1-0.1.0-beta.3`, commit `1548dd6f2d41906e845c626895cb35769a33a61e` |
| Needs | Mekanism and Mekanism Tools `[10.4.16,10.5)`. Draconic Evolution is optional and pinned to `[3.1.2.621]`, the build that the mixin was checked against (`gradle.properties:28`, `META-INF/mods.toml` of the jar) |
| Sides | Both. The modules, items and the module registry sync to the client |
| Source | Folder [ports/mekanism-extra-modules/](../../ports/mekanism-extra-modules). Its `UPSTREAM.md` lists every change with its reason and the test results |
| Build | Gradle 8.2.1, NeoForged ForgeGradle 6.0.21, Librarian 1.2.0.7-dev-SNAPSHOT, Parchment `2023.07.30-1.20.1`, MixinGradle 0.7.38, Java 17, Forge 47.1.70 userdev, the setup of [SkyTekx DE Fixes](draconic-fixes.md). Byte reproducible |

## The modules

All three go into the MekaSuit body armor through the Modification Station, start disabled and run on
the suit's energy (`registry/ExtraModuleRegistry.java:31` to `:36` and `:65`). The values below are
the defaults of the server config.

| Module | What it does |
|---|---|
| Phase Guard Unit | While it is enabled and the suit can pay 500,000 FE (1,250,000 J) per tick, the player takes no damage and does not die, and fire and fall distance are cleared. It needs all four MekaSuit pieces (`requireFullMekaSuit`) |
| Chaos Anchor Unit | Only with an active Phase Guard, it also stops the charged laser of the Draconic Evolution Chaos Guardian, which writes health directly and calls `die`, so no damage event can stop it. It draws no energy of its own |
| Emergency Revival Unit | When the player would die and the suit can pay 10,000,000 FE, the death is canceled, the player gets 4 health, Regeneration II, Absorption II and Fire Resistance, and every harmful effect is removed |

The recipes (`data/mekanism_extra_modules/recipes/`) build the Phase Guard Unit from reinforced
alloys, ultimate control circuits, two Energy Tablets and a Laser Dissipation Unit, and the other two
from a Phase Guard Unit with atomic alloys, ultimate control circuits and obsidian or Totems of Undying.
The circuit, alloy and obsidian inputs are the forge tags that Mekanism 10.4 uses in its own module
recipes, so [AlmostUnified](almostunified.md) needs no new entry. The mod adds no material, block,
worldgen or loot.

## Port

The port keeps upstream's behaviour and config keys. The main adaptations, all in commits on
`ports/mekanism-extra-modules` (9813e0a to 5739305, the review fixes 33745b0 to e2dabb9):

- Mekanism 10.4 knows no `Holder` based module API. A small wrapper turns each module's
  `RegistryObject` into the `IModuleDataProvider` that 10.4 takes, the suit lookups go through
  `IModuleHelper.load` and `isEnabled`, and energy is a Mekanism `FloatingLong` (8284e90).
- Forge 47 has no `EntityInvulnerabilityCheckEvent`. Phase Guard cancels `LivingAttackEvent` at the
  highest priority instead (`service/ProtectionService.java:66`), the first hook of `Player.hurt`, so
  neither other listeners nor Mekanism's own MekaSuit absorption see a blocked hit.
- A mod that calls `actuallyHurt` directly skips `Player.hurt` and that event. Upstream's check also
  stops that path, so Phase Guard cancels `LivingHurtEvent` at the highest priority as well
  (33745b0, `:87`), the first hook of `actuallyHurt`. Armor, absorption hearts and Mekanism's MekaSuit
  absorption, a `LivingHurtEvent` listener that draws suit energy, then never see the hit. Until
  33745b0 only the health loss was stopped, by a zero in `LivingDamageEvent` (`:100`), which stays as
  the last backstop.
- The Draconic Evolution mixin keeps upstream's two `@Redirect` injections on `Player.setHealth` and
  `Player.die` in `LaserBeamPhase.serverTick` and copies no Draconic Evolution code. Its call targets
  remap through a refmap, because Forge 1.20.1 runs on SRG names. A mixin plugin skips it without
  Draconic Evolution.
- Each redirect requires one match (`defaultRequire` 1). A changed target is not just logged. Mixin
  throws when `LaserBeamPhase` first loads, which happens with the first Chaos Guardian and not at
  boot (e2dabb9), so a clean boot proves nothing about it. The range therefore pins Draconic Evolution
  3.1.2.621 (a7f22ef), and Forge refuses any other build at mod loading, as a test copy with the range
  `[3.1.2.620]` showed.
- The recipes moved to the 1.20.1 folder `recipes/` with forge tags (1c8bc5a).
- From upstream `db62401`, a kill with the damage type `minecraft:generic_kill` bypasses Phase Guard
  and Emergency Revival (ec037bb, `service/ProtectionService.java:290`). On the server only the vanilla
  `/minecraft:kill` makes such a kill. The plain `/kill` is the command of EssentialsX 2.22.0, because
  `spigot.yml:36` to `:40` hands only four other commands back to vanilla. That command kills with
  `setHealth(0)`, which CraftBukkit turns into a death with the generic damage source, so Phase Guard
  and Emergency Revival stop it like any other death. The harness ran that `CraftPlayer.setHealth(0)`
  call on Arclight. Nobody ran the EssentialsX command itself.
- Upstream's branch `fix/phase-guard-kill` (9f51277) narrows the bypass to the vanilla command with a
  MixinExtras `@WrapOperation`. It is not on upstream `main`, and the port does not bundle MixinExtras,
  so a mod that calls `kill()` on a player also gets through, see `UPSTREAM.md`.
- An own fix keeps the tick handler away from dead players (ff76194). A dead player keeps ticking until
  it respawns, and upstream would then set its health back to `minHealth` whenever the suit survived
  the death. Since 0005853 the check keys on a death that went through (`:130`), not on health, so a
  player whose health a mod wrote to 0 without a death is still rescued, as upstream does.
- Upstream's own agent instruction files stay out of the source (b685363), because their rules
  conflict with this repository's [AGENTS.md](../../AGENTS.md).

Upstream's config screen (576425a) needs NeoForge's `ConfigurationScreen` and is left out. The rest of
the later upstream commits is a refactor without a change on 1.20.1, see `UPSTREAM.md`.

## Pack fit

- It sits next to [SkyTekx DE Fixes](draconic-fixes.md), which changes a constant in the same method
  `LaserBeamPhase.serverTick`. The mixin export of the Forge and the Arclight test boots shows both
  injections side by side, so the two do not collide.
- Arclight keeps the Forge hooks that the mod uses. Arclight's own `ServerPlayer.die` calls Forge's
  `onLivingDeath` before Bukkit's `PlayerDeathEvent`, and `Player.hurt` fires `LivingAttackEvent`
  before Bukkit's `EntityDamageEvent` (javap of `common.jar` in the pack's Arclight jar). Arclight's
  `damageEntity0`, which runs `actuallyHurt` for players, fires `LivingHurtEvent` and returns before
  Bukkit's damage event as well. So a canceled hit or death never reaches the plugins, as with a Totem
  of Undying.
- The script [combat_1_7.js](kubejs.md) only changes the attack speed and does not touch damage or
  death.
- Phase Guard also blocks void damage (test of 2026-11-05). A player who falls out of the End or below
  the Overworld keeps falling until the suit runs dry. In the Skylands the travel functions teleport a
  falling player before the void damage starts (`skylands-datapack/data/skytekx3/functions/travel/tick.mcfunction`).
- Draconic Evolution's modular armor has Undying modules of its own (Wyvern, Draconic and Chaotic,
  language file of the 3.1.2.621 jar). The two mods do not interact and only overlap in balance.

## Config

The server config is `world/serverconfig/mekanism_extra_modules-server.toml`, and the client gets a
copy at login. Forge writes it back from memory at every stop, so edit it only while the java process
is gone, as for the other server configs in [AGENTS.md](../../AGENTS.md). Every value is in FE and
converts through Mekanism's FE to Joule rate.

## Operator steps

Git does not hold `mods/`, and the owner mirrors it by hand. Deploy the jar to `mods/` only together
with the pack release that bundles it, because the mod registers items and Mekanism modules on both
sides, and a client without the same jar is refused at login. For that deploy:

1. Stop the server over RCON and wait until the java process is gone.
2. Copy `local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar` into `mods/` and check its
   SHA-1 `3c40952af5145c740fe6f8fc90ac191574bb2b2f`. Mekanism and Mekanism Tools 10.4.16.80 and
   Draconic Evolution 3.1.2.621 are there already.
3. Start the server and check the boot for `Compat loaded: mekanism=true, mekanismtools=true,
   draconicevolution=true` and for no new ERROR line against the [baseline](../server/logs-and-crashes.md#boot-error-baseline).
4. Clients get the same jar with that `.mrpack`.

The mod adds no world generation, so old and new chunks are the same to it. To remove a player who
wears an active Phase Guard or a powered Emergency Revival, use `/minecraft:kill`, since the plain
`/kill` of EssentialsX does not get through. After a Draconic Evolution update the boot stops with a
message that this mod only supports 3.1.2.621. Load `LaserBeamPhase` with the new build on a test
copy, then widen `de_range` in `gradle.properties` and rebuild.

## License

The code and assets are LGPL-3.0-or-later. The jar carries the LGPL v3, the GPL v3 that it
incorporates, Mekanism's MIT notice for the textures that upstream edited from Mekanism textures and a
NOTICE that names the upstream commit, the changes, the date and the Corresponding Source in
`ports/mekanism-extra-modules/`. The pack ships the same texts in
`licenses/mekanism_extra_modules-0.1.0-beta.3-skytekx.1/` and a zip of the port directory as
`licenses/mekanism_extra_modules-0.1.0-beta.3-skytekx.1-sources.zip`. The permission record
`modrinth-permissions/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar.md` points the source at
`tree/master/ports/mekanism-extra-modules`, so `tools/mrpack_permissions.py` refuses a public build
until master holds the port and this jar. The upstream README banner stays out of the repo, because
its C2PA metadata names an image generation service. The license sets no non-commercial condition.

## Tests

Two rounds on 2026-11-05, both with a scratch harness outside the repo that drove a server player
through every module over RCON. The tables are in `ports/mekanism-extra-modules/UPSTREAM.md`.

- The first round, with the jar of 74bf989, booted plain Forge 47.4.18 with Mekanism, with and without
  Draconic Evolution and SkyTekx DE Fixes, with 0 ERROR and 0 FATAL lines. The Arclight copy of that
  round held only Mekanism, Mekanism Tools, the harness and the jar (`draconicevolution=false` in its
  log), so it tested neither the mixin nor a plugin on Arclight.
- The second round, with the jar of fa64b7f, booted plain Forge 47.4.18 and the pack's Arclight jar,
  each with Draconic Evolution 3.1.2.621 and SkyTekx DE Fixes, with 0 ERROR and 0 FATAL lines. On both,
  a direct `actuallyHurt` under Phase Guard left suit energy and absorption hearts unchanged, while
  without Phase Guard the same call drew 240,000 J or took 6 absorption. The exported `LaserBeamPhase`
  carries the redirects of both mods on both servers, and `CraftPlayer.setHealth(0)` on Arclight was
  canceled by Phase Guard or answered by Emergency Revival.

Nobody has tested a real client, the HUD icons, the module screen, a Chaos Guardian fight, the
EssentialsX `/kill` command itself or a boot with EssentialsX yet. The full pack of 98bf164 with the jar
of fa64b7f booted on Forge 47.4.18 and on Arclight on 2026-11-06 without plugins, and its mixin into
`LaserBeamPhase` applied on both, see [NeoForge ports](neoforge-ports.md#status).
