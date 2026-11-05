---
type: Mod
title: Mekanism Extra Modules port
description: The pack's Forge 1.20.1 port of Mekanism: Extra Modules 0.1.0-beta.3, a 1.21.1 NeoForge addon with three MekaSuit body armor modules, how it was adapted to Mekanism 10.4, what it changes in play, how it was tested, its license terms and the steps for the server's mods folder.
tags: [modpack, mods, mekanism, draconicevolution, port, mixin]
timestamp: 2026-11-05
---

# Mekanism Extra Modules port

Mekanism: Extra Modules is an addon that adds three modules for the MekaSuit body armor. Upstream
builds it only for Minecraft 1.21.1 on NeoForge with Mekanism 10.7 (Modrinth project `TfCrGUbp`,
newest version 0.1.0-beta.3 of 2026-09-13). Since 2026-11-05 the pack bundles a port to Forge 1.20.1
and the pack's Mekanism 10.4.16, built from [ports/mekanism-extra-modules/](../../ports/mekanism-extra-modules).
Upstream is a beta with 677 downloads on Modrinth (API of 2026-11-05), so expect balance changes.

| Property | Value |
|---|---|
| Jar | File [local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar](../../local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar), 658,607 bytes, SHA-1 `ca7f070e3f78405968a6fe93257fe235e4f3e925` (6ae4f8f) |
| Mod | The id `mekanism_extra_modules`, the name, the author entry and the license `LGPL-3.0-or-later` stay upstream's. The version carries the suffix `-skytekx.1` |
| Upstream | Repository https://github.com/Lumysia/mekanism-extra-modules, tag `v1.21.1-0.1.0-beta.3`, commit `1548dd6f2d41906e845c626895cb35769a33a61e` |
| Needs | Mekanism and Mekanism Tools `[10.4.16,10.5)`. Draconic Evolution from 3.1.2 is optional (`META-INF/mods.toml` of the jar) |
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
`ports/mekanism-extra-modules` (a2df2b5 to 0efb61b):

- Mekanism 10.4 knows no `Holder` based module API. A small wrapper turns each module's
  `RegistryObject` into the `IModuleDataProvider` that 10.4 takes, the suit lookups go through
  `IModuleHelper.load` and `isEnabled`, and energy is a Mekanism `FloatingLong` (1a72d2d).
- Forge 47 has no `EntityInvulnerabilityCheckEvent`. Phase Guard cancels `LivingAttackEvent` at the
  highest priority instead (`service/ProtectionService.java:48`), the first hook of `Player.hurt`, so
  neither other listeners nor Mekanism's own MekaSuit absorption see a blocked hit. A zero in
  `LivingDamageEvent` (`:64` of the same file) catches mods that call `actuallyHurt` directly.
- The Draconic Evolution mixin keeps upstream's two `@Redirect` injections on `Player.setHealth` and
  `Player.die` in `LaserBeamPhase.serverTick` and copies no Draconic Evolution code. Its call targets
  remap through a refmap, because Forge 1.20.1 runs on SRG names. A mixin plugin skips it without
  Draconic Evolution.
- The recipes moved to the 1.20.1 folder `recipes/` with forge tags (5451679).
- From upstream `db62401`, `/kill` now bypasses Phase Guard and Emergency Revival, so operators can
  still remove a player (a630f5c, `service/ProtectionService.java:50`, `:66` and `:78`).
- An own fix keeps the tick handler away from dead players (f3735ca,
  `service/ProtectionService.java:36`). A dead player keeps
  ticking until it respawns, and upstream would then set its health back to `minHealth` whenever the
  suit survived the death.

Upstream's config screen (576425a) needs NeoForge's `ConfigurationScreen` and is left out. The rest of
the later upstream commits is a refactor without a change on 1.20.1, see `UPSTREAM.md`.

## Pack fit

- It sits next to [SkyTekx DE Fixes](draconic-fixes.md), which changes a constant in the same method
  `LaserBeamPhase.serverTick`. The mixin export of the test boot shows both injections side by side,
  so the two do not collide.
- Arclight keeps the Forge hooks that the mod uses. Arclight's own `ServerPlayer.die` calls Forge's
  `onLivingDeath` before Bukkit's `PlayerDeathEvent`, and `Player.hurt` fires `LivingAttackEvent`
  before Bukkit's `EntityDamageEvent` (javap of `common.jar` in the pack's Arclight jar). So a canceled
  hit or death never reaches the plugins, as with a Totem of Undying. Only damage that a mod applies
  through `actuallyHurt` reaches Bukkit's damage event before Phase Guard sets it to zero.
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

Git does not hold `mods/`, and the owner mirrors it by hand. For the next deploy:

1. Stop the server over RCON and wait until the java process is gone.
2. Copy `local-mods/mekanism_extra_modules-0.1.0-beta.3-skytekx.1.jar` into `mods/`. Mekanism and
   Mekanism Tools 10.4.16.80 are there already.
3. Start the server and check the boot for `Compat loaded: mekanism=true, mekanismtools=true,
   draconicevolution=true` and for no new ERROR line against the [baseline](../server/logs-and-crashes.md#boot-error-baseline).

Players get the jar with the next `.mrpack`.

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

Plain Forge 47.4.18 servers booted the jar with Mekanism, with and without Draconic Evolution and
SkyTekx DE Fixes, with 0 ERROR and 0 FATAL lines, and the pack's Arclight jar did the same. A scratch
harness, not in the repo, drove a server player through every module. The results are in
`ports/mekanism-extra-modules/UPSTREAM.md`. Nobody has tested a real client, the HUD icons, the module
screen or a Chaos Guardian fight yet.
