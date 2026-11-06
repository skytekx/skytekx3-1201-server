---
type: Reference
title: Mystical Automation port
description: The Forge 1.20.1 port of Mystical Automation 1.0.8 in ports/mysticalautomation, what it changes against upstream, how it was tested on Forge and Arclight, what nobody verified yet and how the operator deploys it.
tags: [modpack, mods, mysticalautomation, mysticalagriculture, port]
timestamp: 2026-11-06
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
| Upstream | The upstream Mystical Automation repository (`ports/mysticalautomation/UPSTREAM.md` has the URL), branch `1.21` at 4149601b (2026-10-02, "Release 1.0.8"), MIT License |
| Fork | Folder `ports/mysticalautomation/`, vendored in a63ea3b, ForgeGradle 6.0.54 with Forge 47.4.0 userdev and Parchment 2023.06.26 on Java 17 (0483257) |
| Jar | File `local-mods/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar`, SHA-1 585446d50038e4f254c2fb2b012b616495ff5ed0, 312,817 bytes since e6b9182, the same from two clean builds. The first bundled jar of c3bb6eb had SHA-1 d2891fe346f5f6878789385e45742c6583a8d7c7 and lacked the fixes of 50aaaed, e80e47f and b4fda97 |
| Dependencies | Cucumber 7.0.16, Mystical Agriculture 7.0.24 and Mystical Agradditions 7.0.12, all CDN entries of `modrinth.index.json` that the pack already has |
| Sides | Required on both sides. The mod adds blocks, items, menus and a network channel `mysticalautomation:main` with protocol version 1, which refuses a peer without it |
| License | MIT, with the MIT notices of Cucumber and Mystical Agriculture for the parts the port takes from their 1.21 lines, see the record `modrinth-permissions/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar.md` |

## What the port changes

The file `ports/mysticalautomation/UPSTREAM.md` lists every change, and each changed line in the source
carries a comment that names the reason. In short:

- The Java sources moved from NeoForge 21.1 to Forge 47 (bb7e135). Registries, menus, capabilities,
  the config and the network channel use the Forge forms, and the mod keeps every registry id, NBT
  key, slot layout and machine number of upstream.
- The machines work against the APIs of Mystical Agriculture 7 and Cucumber 7 instead of 8. They
  implement the upgradeable machine interface of Mystical Agriculture 7, so its Machine Upgrades apply
  with a right click as on its own machines. A few Cucumber 8 classes that version 7 lacks live in the
  packages of this mod.
- Minecraft 1.20.1 sends container data as shorts and slot counts as bytes. The port sends every data
  value as two halves (bb7e135) and repeats a slot of more than 127 items in full over its channel
  (82878fa), since the Infuser and the Enchanternator hold up to 512 items per input slot.
- The data moved to the 1.20.1 folders and fields, and the common `c:` tags became `forge:` tags,
  which AlmostUnified works with (85cce3c). The tag `mysticalautomation:fertilizers` lists bone meal,
  Fertilized Essence, Mystical Fertilizer and the optional `#forge:fertilizers`.
- Upstream bugs that matter on a public server are fixed. A client message could put any item into
  any slot of the open menu (66b49ff), the Enchanternator gave the top level for the price of level
  one and kept the item it enchanted (298bf2c), the JEI recipe transfer read past its slot views and
  bone meal cost the Fertilizer only its scan rate (654dab4).
- The integration review of 2026-11-05 found three more, and the fork fixes them. The Crafter made
  special recipes such as dyed shulker boxes from its ghost grid alone, without taking an item, which
  upstream fixed only on its 26.1 line in 7a481dc (50aaaed). The Crafter and both Altarnators built
  their result from the ghost grid and tested the real inputs without NBT, so an enchanted ghost over
  a plain input gave enchanted output, and now every ghost slot takes real items equal to it and the
  result comes from the items taken (e80e47f). The Farmer put a whole result into an empty slot after
  merging part of it, and dropped what found no room, and now it adds each result once and waits while
  its outputs are full (b4fda97).
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
- The Crafter does not make special recipes (`Recipe#isSpecial`), such as dyed shulker boxes,
  firework rockets, tipped arrows or book copies. They keep items back in the grid, which the Crafter
  does not do. A ghost grid with two variants of one tag, such as oak and birch planks for a recipe
  that takes any planks, takes each variant as the ghost shows it.
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

The integration retested the jar of e6b9182 on the same two test copies on 2026-11-05. The Forge
copy booted at 07:37 UTC and the Arclight copy at 07:42 UTC, both reached `Done` with 0 ERROR and 0
FATAL lines, and both stopped over RCON with exit code 0. Each machine was placed with `setblock`,
an NBT set ghost grid and inventory, coal as fuel and a hopper below it.

| Check | Result |
|---|---|
| Crafter, special recipe | A ghost shulker box with 64 diamond blocks and a red dye gave nothing in more than three minutes on Forge, with empty real inputs and with the same two items as real inputs, which stayed in place. Arclight gave nothing in 35 seconds. The first jar made three dyed shulker boxes in 28 seconds, as the review saw |
| Crafter, ghost NBT | For Prudentium Leggings, a ghost of Inferium Leggings with Protection IV over plain real leggings gave nothing and took nothing, on Forge and on Arclight. Plain over plain gave plain Prudentium Leggings, and enchanted over enchanted kept Protection IV on the result |
| Crafter, buffer | A ghost ring of eight cobblestone over one stack of 64 in the first slot gave a furnace and left 56, so one stack still feeds the grid, as a hopper fills it |
| Awakening Altarnator | A Sharpness V ghost Supremium Sword over a plain one gave nothing. Plain over plain gave an Awakened Supremium Sword and took 40 of each essence, in the slot layout of the JEI transfer and in the one of the altar of Mystical Agriculture. Enchanted over enchanted kept Sharpness V |
| Infusion Altarnator | A renamed ghost Prosperity Seed Base over a plain one gave nothing, plain over plain gave Dirt Seeds |
| Farmer | A cocoa result of 3 against an output slot of 62 left 64 and 1, on Forge and on Arclight. With every output slot full it kept its progress and stayed off while its energy rose, and it finished once one slot was free |

A second test copy on the hardened Arclight jar of the repository
(`arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`) with the same mods reached `Done`, ran the
Infuser with its hoppers, the Crafter, the Farmer, the Enchanternator at level 2 and the Fertilizer
with the Awakened Supremium Upgrade, which grew eight of nine wheat crops to full age with its fake
player, and stopped over RCON with 0 ERROR lines.

Not verified yet, because no client joined a test server:

- The screens, their widgets and buttons, the JEI categories, the recipe transfer, the ghost slots and
  the slot counts above 127 in the Infuser and the Enchanternator on a client.
- The guide book entries in a client with Patchouli.

The test copies above held only the mod and its dependencies. The full pack of 052a0e4 with the jar of
e6b9182 booted on Forge 47.4.18 and on Arclight on 2026-11-06 with the error baseline of the test
copies, see [NeoForge ports](neoforge-ports.md#status).

## Deploy

The mod is new to the pack, so no world holds its blocks yet. Players need the jar too, so it reaches
the server with the next pack.

1. Build or take `local-mods/MysticalAutomation-1.20.1-1.0.8-skytekx.1.jar` and check its SHA-1
   against the table above. A server that got the first jar (SHA-1 d2891fe3) before e6b9182 needs
   the new one in its place.
2. With the java process gone, copy the jar into the server's `mods/`. Cucumber 7.0.16, Mystical
   Agriculture 7.0.24 and Mystical Agradditions 7.0.12 are there already.
3. Start the server and compare the boot with the baseline on
   [logs and crashes](../server/logs-and-crashes.md#boot-error-baseline). The mod writes
   `config/mysticalautomation-common.toml` with every machine enabled.
4. Mirror `mods/` to the Mac mini with `macos/sync-to-mac.sh` as usual.
5. Before a public pack, merge the branch that carries `ports/mysticalautomation/` into master and
   push it, since the permission record links the source there, see [publish](publish.md).

The mod has no world generation, so nothing waits for new chunks. Clients need the same jar, which
the next pack brings them.

Related: [mod set](mod-set.md#bundled-jars), [NeoForge ports](neoforge-ports.md#mystical-automation),
[publish](publish.md), [AlmostUnified](almostunified.md).
