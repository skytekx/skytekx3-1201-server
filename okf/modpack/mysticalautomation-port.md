---
type: Reference
title: Mystical Automation port
description: The Forge 1.20.1 port of Mystical Automation 1.0.8 in ports/mysticalautomation, what it changes against upstream, how it was tested on Forge and Arclight, what nobody verified yet and how the operator deploys it.
tags: [modpack, mods, mysticalautomation, mysticalagriculture, port]
timestamp: 2026-11-05
---

# Mystical Automation port

Mystical Automation (mod id `mysticalautomation`) adds seven machines to Mystical Agriculture. The
Infuser turns essences into the next tier, the Crafter crafts a set recipe, the Farmer plants, grows
and harvests seeds on a soil block inside the machine, and the Fertilizer applies bone meal and the
Mystical Agriculture fertilizers to the crops in front of it. The Infusion Altarnator, the Awakening
Altarnator and the Enchanternator run the recipes of the Infusion Altar, the Awakening Altar and the
Enchanter. All of them burn solid fuel into an internal Forge Energy buffer and take the Machine
Upgrades of Mystical Agriculture. Upstream builds the mod only for NeoForge 1.21.1 and newer, so the
pack bundles a port to Forge 1.20.1 from `ports/mysticalautomation/`. The work is done for SkyTekx, a
modpack by ts4.party.

| Property | Value |
|---|---|
| Upstream | Repository BlakeBr0/MysticalAutomation, branch `1.21` at 4149601b (2026-10-02, "Release 1.0.8"), MIT License (`ports/mysticalautomation/UPSTREAM.md`) |
| Fork | Folder `ports/mysticalautomation/`, vendored in 06bc715, ForgeGradle 6.0.54 with Forge 47.4.0 userdev and Parchment 2023.06.26 on Java 17 (6c402ab) |
| Jar | File `local-mods/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar`, SHA-1 d2891fe346f5f6878789385e45742c6583a8d7c7, 308,953 bytes, the same from two clean builds |
| Dependencies | Cucumber 7.0.16, Mystical Agriculture 7.0.24 and Mystical Agradditions 7.0.12, all CDN entries of `modrinth.index.json` that the pack already has |
| Sides | Required on both sides. The mod adds blocks, items, menus and a network channel `mysticalautomation:main` with protocol version 1, which refuses a peer without it |
| License | MIT, with the MIT notices of Cucumber and Mystical Agriculture for the parts the port takes from their 1.21 lines, see the record `modrinth-permissions/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar.md` |

## What the port changes

The file `ports/mysticalautomation/UPSTREAM.md` lists every change, and each changed line in the source
carries a comment that names the reason. In short:

- The Java sources moved from NeoForge 21.1 to Forge 47 (5ac5d5a). Registries, menus, capabilities,
  the config and the network channel use the Forge forms, and the mod keeps every registry id, NBT
  key, slot layout and machine number of upstream.
- The machines work against the APIs of Mystical Agriculture 7 and Cucumber 7 instead of 8. They
  implement the upgradeable machine interface of Mystical Agriculture 7, so its Machine Upgrades apply
  with a right click as on its own machines. A few Cucumber 8 classes that version 7 lacks live in the
  packages of this mod.
- Minecraft 1.20.1 sends container data as shorts and slot counts as bytes. The port sends every data
  value as two halves (5ac5d5a) and repeats a slot of more than 127 items in full over its channel
  (abe46cd), since the Infuser and the Enchanternator hold up to 512 items per input slot.
- The data moved to the 1.20.1 folders and fields, and the common `c:` tags became `forge:` tags,
  which AlmostUnified works with (f837508). The tag `mysticalautomation:fertilizers` lists bone meal,
  Fertilized Essence, Mystical Fertilizer and the optional `#forge:fertilizers`.
- Upstream bugs that matter on a public server are fixed. A client message could put any item into
  any slot of the open menu (9c308bf), the Enchanternator gave the top level for the price of level
  one and kept the item it enchanted (00f87c5), the JEI recipe transfer read past its slot views and
  bone meal cost the Fertilizer only its scan rate (b4faeee).
- The CraftTweaker integration stays out, because CraftTweaker is not in the pack. Datapacks and
  KubeJS add Farmer recipes with `event.custom` and the JSON form in `UPSTREAM.md`.

## Pack fit

- The seven machine recipes use `forge:ingots/iron`, `forge:gems/diamond`, the crafting table and
  items of Mystical Agriculture. Their outputs exist in no other mod, so no recipe conflicts, and
  `config/almostunified/unify.json` needs no change.
- The mod brings 102 recipes. Seven craft the machines, 17 Farmer recipes come from files of the mod
  (10 vanilla crops and 7 tiers of Inferium Seeds, one of them only with Mystical Agradditions), and
  the mod builds 78 more Farmer recipes at each reload for the enabled crops of Mystical Agriculture.
  The count of 78 is the Cucumber log line `Registered 78 recipes` of a test boot, which drops to
  `Registered 0 recipes` with `dynamicMysticalAgricultureRecipes = false` in
  `mysticalautomation-common.toml`. Mystical Agriculture 7 adds no such recipes with its defaults.
- No world generation, no mixins and no Bukkit plugin side. The Fertilizer uses Forge's fake player
  `[Minecraft]` to apply fertilizers, the only fake player of the mod.
- The guide book of Mystical Agriculture (Patchouli) gets a category Automation with one entry per
  machine, which link to entries that Mystical Agriculture 7.0.24 has.

## Tests

On 2026-11-05 a plain Forge 47.4.18 server with the jar, Cucumber, Mystical Agriculture, Mystical
Agradditions, JEI 15.74.0.233 and Patchouli 85 (the files of the index) booted three times. The test
copy sat outside the server folder, and its logs are not in the repository.

| Check | Result |
|---|---|
| Boot | Reached `Done` each time with 0 FATAL lines. The first boot logged one ERROR, `No key layers in MapLike[{}]` of `DedicatedServerProperties`, from the flat world of the test copy without generator settings. The second and third boot logged 0 ERROR lines. Every stop over RCON ended with `java exit 0` |
| Registries | `loot spawn` dropped each of the seven block items from its loot table, and `setblock` placed each machine with its block entity |
| Infuser | It turned Inferium into Prudentium Essence, four for one, from a stack of 300 in one slot. A hopper above it filled the Inferium slot, and a hopper below it pulled the Prudentium out |
| Crafter | It crafted the Infuser recipe of the mod from its ghost grid and real inputs, which also shows that the shaped recipe with its `forge:` tags and feature flag condition loads |
| Farmer | With the Awakened Supremium Upgrade it grew wheat on farmland and Dirt Essence with Fertilized Essence on Inferium Farmland, a recipe the mod builds at the reload. A hopper fed seeds and farmland into an empty Farmer and kept a stick and an essence back |
| Altarnators | The Infusion Altarnator made Dirt Seeds and the Awakening Altarnator made Awakened Supremium Blocks, both with the Awakened Supremium Upgrade |
| Enchanternator | Set to level 2 it took 32 Quartz, 128 Experience Essence and one book and gave a Sharpness II book. Set to level 3 it made a Sharpness III Diamond Sword from 48 and 192 |
| Fertilizer | It used bone meal on the wheat in front of it |
| Blocks | A `setblock` over a filled Crafter dropped nothing, as the Clearable support should, and a destroyed Farmer dropped itself, its upgrade and its contents |
| Restart | Energy, upgrades, inventories and modes came back after the restart |
| Reload | A `/reload` rebuilt the recipes and the Farmer input cache without an error |
| Dedicated server | The class load log shows no class of the packages `client` or `compat.jei` of the mod |

A second test copy on the hardened Arclight jar of the repository
(`arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`) with the same mods reached `Done`, ran the
Infuser with its hoppers, the Crafter, the Farmer, the Enchanternator at level 2 and the Fertilizer
with the Awakened Supremium Upgrade, which grew eight of nine wheat crops to full age with its fake
player, and stopped over RCON with 0 ERROR lines.

Not verified yet, because no client joined a test server:

- The screens, their widgets and buttons, the JEI categories, the recipe transfer, the ghost slots and
  the slot counts above 127 in the Infuser and the Enchanternator on a client.
- The guide book entries in a client with Patchouli.
- The full pack. The test copies held only the mod and its dependencies.

## Deploy

The mod is new to the pack, so no world holds its blocks yet. Players need the jar too, so it reaches
the server with the next pack.

1. Build or take `local-mods/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar` and check its SHA-1
   against the table above.
2. With the java process gone, copy the jar into the server's `mods/`. Cucumber 7.0.16, Mystical
   Agriculture 7.0.24 and Mystical Agradditions 7.0.12 are there already.
3. Start the server and compare the boot with the baseline on
   [logs and crashes](../server/logs-and-crashes.md#boot-error-baseline). The mod writes
   `config/mysticalautomation-common.toml` with every machine enabled.
4. Mirror `mods/` to the Mac mini with `macos/sync-to-mac.sh` as usual.
5. Before a public pack, merge the branch that carries `ports/mysticalautomation/` into master and
   push it, since the permission record links the source there, see [publish](publish.md).

Related: [mod set](mod-set.md#bundled-jars), [publish](publish.md),
[AlmostUnified](almostunified.md).
