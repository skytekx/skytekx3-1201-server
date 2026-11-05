---
type: Plan
title: Ender IO backport
description: Which features of the Ender IO 1.21.1 line (8.2.x) go onto its 1.20.1 Forge base (6.2.x) for the pack, in which bundles and order, which upstream work stays out, and the license, savegame, Arclight and operator rules of the fork.
tags: [modpack, mods, enderio, backport]
timestamp: 2026-11-05
---

# Ender IO backport

The pack ships Ender IO 6.2.18-beta as the bundled jar `local-mods/EnderIO-1.20.1-6.2.18-beta-all.jar`
(SHA-1 `33587f98f2d96507de0e9d2f57f5ab35328ace7d`). Upstream develops the mod on its 1.21.1 NeoForge
line, Ender IO 8.2.x. This plan backports an additive subset of that line onto a vendored copy of
upstream's `1.20.1` branch in `ports/enderio/`, the same way as the
[Twilight Forest backport](twilightforest-backport.md). The fork keeps the mod id `enderio` and every
registry id, NBT key, block state property, capability id and config key, so the live world, the
configs and the addons keep working. The work is done for SkyTekx, a modpack by ts4.party.

Effort is given in estimated human hours, the unit of the Twilight Forest plan. The plan holds 31
features in 10 bundles, 244 h in all, and no bundle is over 40 h. The machine readable plan is
`ports/enderio-backport-inventory.json`. Its `plan.bundles` holds each bundle with its feature ids,
hours, dependencies and a full spec (upstream commits, translation notes, savegame notes, tests and the
common rules in the first bundle), `plan.excluded` holds the upstream work left out with its reason,
and `critic_notes` holds the review that corrected the first draft. The critic notes give 29 features
and 241 h, while the bundle list sums to 31 features and 244 h. The bundle list is the reference. The
file `ports/enderio-backport-carryover.json` will record what a bundle hands to a later one. It does
not exist yet. Until it does, the rules that the fix pack hands to later bundles sit in the common
rules of the first bundle's spec.

| Property | Value |
|---|---|
| Upstream | Team-EnderIO/EnderIO on GitHub, The Unlicense (`ports/enderio/LICENSE.txt`) |
| Base | Branch `1.20.1` at 18ee1b17 (2026-03-16), which is 6.2.18-beta (907a9c37) plus the community fixes f9e1806c and 18ee1b17 (`ports/enderio/UPSTREAM.md`, Upstream table) |
| Port source | Branch `1.21.1`, Ender IO 8.2.x. The merge base with `1.20.1` is 41ed3d5e |
| Reference only | Upstream's stalled branch `experiment/1.20.1-backport` (8 commits of 2026-03 on `1.21.1` at 010b057e), read for API translations. Its 8.x core is not used |
| Fork | Folder `ports/enderio/` with the 6.x layout `src/{api,core,main,machines,conduits,armory}`, Java 17, ForgeGradle 6.0.54 (`UPSTREAM.md`, Build changes) |
| Target version | Version 6.3.0-skytekx.1 in `EnderIO-1.20.1-6.3.0-skytekx.1-all.jar`, the jarJar jar with Registrate and GraphLib3 inside |
| Ships as | A jar in `local-mods/` that replaces the 6.2.18-beta jar |
| Build check | The tasks `compileJava`, `compileMachinesJava`, `compileConduitsJava` and `compileArmoryJava` in one call, with `/opt/jdk17`, `-Xmx2g` and `LC_ALL=C.UTF-8`. The task `compileJava` alone covers only the api, core and main source sets, and the module tasks run `processResources`. A full build needs `LC_ALL=C.UTF-8` because of the lang file `no_no<U+200C>.json` (`UPSTREAM.md`, Building) |
| Addon check | The script `bp2/enderio-tools/eiocompat.py` in the workflow scratchpad must print `TOTAL MISSING: 0` for the built jar against `local-mods/eio_legacy_machines-0.4.9.jar` |

## Status on 2026-11-05

The vendored base (8799e05), the reproducible Java 17 build of 6.3.0-skytekx.1 (dd66c98) and the
license texts and the change notice in the jar (e508261) are on the branch
`claude/twilight-forest-port-mods-j2owbq`. The built `-all` jar has the SHA-1
`25bfbf933d402abb0b7ecbdedd260a1a878a9a3c`. It matches the shipped 6.2.18-beta jar except for
f9e1806c, 18ee1b17 and the metadata, and the compat scan finds 0 missing symbols for EnderIO Legacy
Machines, Exchangers and AllTheLeaks (inventory, `plan.excluded` entry `fork-bootstrap`).

The fork infrastructure is done in 6a3268f (protocol bump), 90c9f8b (JSON data maps), 10ba7e1
(optional Curios support) and effd892 (review fixes). The jar built from effd892 passes `compileJava`
and a full build, and the compat scan still finds 0 missing symbols for EnderIO Legacy Machines,
Exchangers and AllTheLeaks. Nothing uses the data maps or the Curios helper yet. The data map sync was
only tested as an encode and decode round trip in one process, since no headless client was at hand,
so a real client join that shows the values in JEI is still open for the Vat bundle. A dedicated
`runServer` of the fork without Curios is still open.

The fix pack is done in 1e5fde6 (fluid tank), 59e7770 (capacitor bank items), 6f5774a (ghost and
filter slots), 49bb41c (machine outputs and IO), 91a7f49 (recipe outputs), 46b3e5f (small fixes),
be881bc (packet validation) and f142998 (review fixes). The integration compiled every source set and
ran a full build, whose `-all` jar has the SHA-1 `5128f86d85be037887fc4ca9016aebeff4beeca1`, and the
compat scan still finds 0 missing symbols for EnderIO Legacy Machines, Exchangers and AllTheLeaks. A
dev `runServer` of the fix pack with Curios and AE2 on the classpath reached `Done` (workflow
scratchpad, `bp2/enderio/b1/runserver2.log` line 6889). No NBT key, packet format, protocol version
or generated resource changed. Nobody has run the in game tests of the fix pack yet, see the end of
[Fix pack](#fix-pack).

The Vat is the first content bundle, done in e7e29bd (the machine), a175998 (the recipes and data)
and bef7784 (review fixes). The integration compiled every source set, ran `runData`, which changed
nothing beyond the two tag files of bef7784, and ran a full build, whose `-all` jar has the SHA-1
`9594b5708d5dbd5779a64de42bb98b5bbcb4ad79`. The compat scan still finds 0 missing symbols for
EnderIO Legacy Machines, Exchangers and AllTheLeaks. A dev `runServer` with a temporary test class,
never committed, reached `Done` and passed 38 of 38 checks (workflow scratchpad,
`bp2/enderio/b2int/runserver-int.log`). Nobody has joined with a client yet, see the end of
[Vat](#vat). The other content bundles are not ported yet. The pack still ships the 6.2.18-beta jar,
and no pack boot has run the fork.

| Bundle | What it covers | Hours | Needs | Status |
|---|---|---|---|---|
| Fork infrastructure (`eio-b0-fork-bootstrap`) | Network protocol bump, JSON data maps for 1.20.1, optional Curios support | 10 | The vendored base | Done in 6a3268f, 90c9f8b, 10ba7e1 and effd892. The vendor, version and license work of the old feature `fork-bootstrap` is done in 8799e05, dd66c98 and e508261 |
| Fix pack (`eio-b1-6x-fix-pack`) | Fluid tank and sponge dupes, the stacked capacitor bank dupe, ghost and filter slot dupes, machine outputs that void, recipe outputs, small fixes, validation of client packets | 26 | Fork infrastructure | Done in 1e5fde6, 59e7770, 6f5774a, 49bb41c, 91a7f49, 46b3e5f, be881bc and f142998 |
| Vat (`eio-b2-vat`) | The unpowered Vat and its nine fermenting recipes | 32 | Fix pack | Done in e7e29bd, a175998 and bef7784 |
| Obelisks (`eio-b3-obelisks`) | Inhibitor, relocator and attractor obelisks on the 6.x obelisk base | 25 | Fix pack, Vat | Planned |
| Charger and keybinds (`eio-b4-charger-keybinds`) | Wireless charger with two antennas, keybinds for the travel staff and the electromagnet with Curios | 23 | Fix pack, obelisks | Planned |
| Filters (`eio-b5-filters`) | Big, big advanced and limited item filters, filter tooltips | 28 | Fix pack | Planned |
| Conduit improvements (`eio-b6-conduit-qol`) | Energy and fluid conduit priorities, strong redstone power, the filter GUI from the conduit GUI | 31 | Filters | Planned |
| Conduit facades (`eio-b7-conduit-facades`) | Four facade items on the 6.x conduit bundle, AE2 facades as conduit facades | 32 | Conduit improvements | Planned |
| Powered spawner (`eio-b8-powered-spawner`) | Capture mode and the Mind Killer | 23 | Fix pack, charger and keybinds | Planned |
| Visual polish (`eio-b9-visual-polish`) | Connected glass through Athena, machine particles, conduit movement particles | 14 | Powered spawner, conduit facades | Planned |

### Order

After the fork infrastructure and the fix pack, the work runs in two lanes (inventory, `critic_notes`,
Structure and ordering).

- The machine lane runs the Vat, the obelisks, the charger and keybinds and the powered spawner one
  after the other, because all four edit `MachineBlocks`, `MachineBlockEntities`, `MachineMenus`,
  `MachinePackets`, `MachinesConfig` and `MachineRecipeProvider`.
- The conduit and filter lane runs the filters, the conduit improvements and the facades one after the
  other, because they share the filter items of `EIOItems`, the filter menus, `ConduitBlock` and
  `ConduitBundle`.
- The charger and keybinds also wait for the fix pack, because both edit `TravelHandler.shortTeleport`.
- The visual polish comes last, after both lanes.
- Both lanes edit the generated `lang/en_us.json` and `lang/en_ud.json`. At integration the keys of both
  sides are merged, or `runData` runs, and neither side simply wins.

## Scope

The research of 2026-11-04 named what to take (workflow scratchpad, `bp2/research-result.json`, record
`enderio`). The plan takes the Vat and its recipes, the wireless charger and its antennas, the
attractor, inhibitor and relocator obelisks, conduit facades, the big, advanced and limited filters
with tooltips and with the filter GUI reachable from conduits, the capture mode of the powered spawner
and the Mind Killer, energy and fluid conduit priorities, strong redstone power, keybinds for the
staff and the electromagnet with Curios, connected glass, machine particles and the dupe fixes that
also apply to 6.x. Forced conduit connections and item conduit priorities are already in the base.
Everything is added on the 6.x core, and no 8.x core code is ported.

### Fork infrastructure

- The network protocol of `CoreNetwork` goes from "1.0" to "1.0-skytekx.1"
  (`src/core/java/com/enderio/core/common/network/CoreNetwork.java:16`). Packet ids follow the order of
  registration across core, `EIOPackets`, `MachinePackets` and `ConduitNetwork`, so any new packet
  shifts later ids. Later bundles only append packets at the end of their module's list. The second
  channel `enderio:main` of `EIONetwork` stays at "1" (`EIONetwork.java:8`), since only light nodes
  use it.
- A small `JsonDataMap` loader replaces NeoForge data maps. It reads
  `data/<ns>/data_maps/<item|block>/<name>.json` in upstream's format with `replace`, `values` and
  `remove`. Its reload listener extends `SimplePreparableReloadListener` and reads every pack's copy of
  the file through `ResourceManager.getResourceStack` in pack order, so later packs win and `replace`
  drops the lower ones (`src/core/java/com/enderio/core/common/data/JsonDataMap.java:291` and `442`).
  A `SimpleJsonResourceReloadListener` would only see the copy of the top pack. Ids and tags resolve
  on the first lookup after a reload or a tag update, and unknown ids are skipped with a debug line.
  The server sends the values to clients as `S2CDataMapSync` on `OnDatapackSyncEvent`, the last packet
  of the core list (`CoreNetwork.java:40`), so every base, machines and conduits packet id moves up by
  one. The Vat reagents and the charger antennas are its two consumers. Server code such as machine
  tasks and tick code reads `getServerValue`, while screens and JEI read `get`, because on a physical
  client an integrated server worker thread counts as the client side.
- Curios becomes an optional dependency (`[5.2.0,)`, both sides,
  `src/main/resources/META-INF/mods.toml:51`). Curios 5.2.0 is the first release with
  `CuriosApi.getCuriosInventory(LivingEntity)`, and its beta builds sort below it. The pack ships
  `curios-forge-5.14.1+1.20.1` (`modrinth.index.json:1014`). Every Curios call sits in a nested class
  that loads only when Curios is present. The travel staff and the electromagnet join the `charm` slot
  (`EIOItems.java:329` and `335`) through `data/curios/tags/items/charm.json`, and
  `data/enderio/curios/entities/enderio.json` gives the player that slot. The fork ships no slot size
  for `charm`, because Cyclic already defines it with size 4 and Aether's override pack with size 2.

### Fix pack

Each item names the upstream commit it ports and the commit on the branch.

- The fluid tank checks the full fluid amount before it shrinks the input (fad146640), checks the
  recipe again before it runs it (1b36f8464), mends a tool only into an empty output (95fb1e4be) and
  skips fluids without a bucket (500f11729), all in 1e5fde6. The sponge dupe of
  `data/enderio/recipes/tank_empty/sponge.json` was not live in 6.x. A stale cached recipe only turns
  out one more dry sponge once the input shrinks after the fill, the order of fad146640, so the recheck
  guards that new order.
- A stack of capacitor bank items splits its energy per item (7a5e3ebf3, 59e7770). Before, a stack of
  64 charged once, and every placed bank carried the full charge. Beyond upstream, `setEnergyStored`
  now writes the energy into `BlockEntityTag`, the compound it reads back, so a freshly crafted bank
  keeps what a charger pays in.
- Ghost slots and filter slots can no longer be picked up, ghost slots ignore every click type except
  pick up and quick move (aba7ff872), and a filter menu locks the player slot that holds the opened
  filter (8c7952cf7), all in 6f5774a. The entity filter menu now shares the click guard of the other
  two, which closes a number key that deleted the hotbar stack, and a swap onto the opened filter is
  ignored. The class `EntityFilterSlot` joins the Inventory Sorter blacklist next to `ItemFilterSlot`
  and `FluidFilterSlot` (`EnderIO.java:188-190`). Inventory Sorter 23.0.11 matches the exact class name
  (`InventorySorter.isSlotBlacklisted` calls `getClass().getName()`). The machine slots
  `GhostMachineSlot` and `PreviewMachineSlot` were already on that list (`EIOMachines.java:84-85`).
  Upstream's change to `ImpulseHopperMenu` in aba7ff872 only adds imports, and the impulse hopper gets
  the ghost guard from `MachineMenu`.
- Crafting machines check their outputs against a snapshot of the inventory, so a second output can
  no longer void (dccb83cda, 49bb41c). The check inserts copies into the snapshot (f142998), because
  Forge's `ItemStackHandler.insertItem` keeps an inserted stack by reference, and upstream's version
  would grow the outputs of the task when two of them stack. The fluid distribution pulls into empty
  tanks again (f600657d8). The item push of `TransferUtil.moveItems` now extracts for real before it
  inserts, a SkyTekx hardening of the 6.x method.
- SAG Mill tag outputs keep their count (4061eacf8, 91a7f49). No shipped recipe changes its yield,
  because the three tag outputs, for coal, sand and wheat, all have count 1. Soul binding keeps the
  input tag when the output is the same item (aa963f47d), which fixes the powered spawner rebind
  recipe of 6.2.5 (f49ba60d) that lost the capacitor and the IO config. The soul binder also crafts its
  outputs again from the current inputs when it places them, so a swapped input cannot copy a
  capacitor. The mossy cobblestone dupe dc01be82e does not apply, because 6.x has no such SAG Mill
  recipe.
- The small fixes in 46b3e5f are a copy in `ChunkBoundLookup` (69b0bd57f), the fire crafting sound
  config (4b57a5733), the swimming pose after a staff blink into a wall (59b00dd66), no doubled
  modifiers on loot capacitors (ed55f0d53) and a guard on the AE2 first tick of ME conduits
  (4bb7e90e9, `AE2InWorldConduitNodeHost.java:110-112`), which also skips a node that was removed
  before its first tick. The same commit stops `EntityFilterSlot` from creating an entity in the
  client level. The JVM verifier loads `ClientLevel` for that call, so opening an entity filter on a
  dedicated server would throw `NoClassDefFoundError`.
- The packet validation in be881bc is a SkyTekx fix without an upstream commit, and both upstream
  lines have the bug. The packets `C2SDataSlotChange`, `C2SSetConduitExtendedData` and
  `C2SSetConduitConnectionState` accepted any block position without a reach or loaded check, the
  base trusted the slot index in `EnderBlockEntity.serverHandleBufferChange` (line 224 of 8799e05),
  and `ConduitBlockEntity.handleExtendedDataUpdate` passed client NBT into the full `deserializeNBT`.
  Any client could therefore set any data slot of any loaded machine and the stored energy of any
  energy conduit. The handlers now require a loaded position within reach, accept client values only
  for slots registered through `addClientDataSlot`, apply conduit GUI data through a new `applyGuiNBT`
  that ignores the stored energy and keep the server's filter and upgrade items in a connection state.
  Obelisk, vacuum and drain ranges are clamped, and travel anchor names are cut to 50 characters. The
  travel request also refuses a target that takes no teleports, except an elevator jump.
- The reach test is `player.canReach(pos, 1.5)`, the test of `MachineBlockEntity.stillValid` and
  `ConduitBlockEntity.stillValid`. It measures from the eye and follows the `forge:block_reach`
  attribute, so a packet passes exactly while its screen may stay open (f142998, `Packet.java:51`).

The common rules in the spec of the first bundle carry what the fix pack hands to later bundles:
client data slots, obelisk ranges, the conduit GUI data path, conduit items, the packet reach and the
output check.

Nobody has run these tests yet. The in game tests on a world copy cover tank recipes with a nearly
full tank, the sponge and wet sponge swap, mending with an occupied output, charging stacked capacitor
banks, ghost slots with Inventory Sorter, a SAG Mill with two outputs stalled, the powered spawner
rebind keeping its capacitor and ME conduits reloading next to an AE2 controller. Forged packets from
100 blocks away need a modified client. The entity filter fix was not run on a production dedicated
server, and its only evidence is a javac verifier test and a reading of the bytecode.

### Vat

- The template is 1cb2e1420 ("Add the Vat"), with the JEI category b7b355a74, the full output fill
  check 9aff4169f, both reagent slot orders e4645aa88 and the recipe fixes b1e1ab917 and 4b37b9d86.
- The Vat is unpowered and has no capacitor, two tanks of 8000 mB and two reagent slots. The dump and
  move buttons use two packets appended to `MachinePackets`.
- Forge 47 has no `SizedFluidIngredient`, so the fork adds its own `FluidInput` (a fluid or a fluid tag
  plus an amount). The recipe type is `enderio:vat_fermenting`.
- The fork keeps the 6.x save format of `MachineFluidHandler` (`Tanks`, `MachineFluidHandler.java:29`).
  The nested format of 1cb2e1420 would break every existing tank, XP obelisk and soul engine.
- Nine recipes ferment hootch, rocket fuel, fire water, cloud seed, concentrated cloud seed, liquid
  sunshine, nutrient distillation, dew of the void and vapor of levity. Liquid darkness stays out with
  the phantom solar panels.
- The reagent tags translate the 1.21 tags. The tag `enderio:meat` lists eleven vanilla meats, because
  `#minecraft:meat` exists only since 1.20.5. The tag `enderio:wind_charges` holds
  `minecraft:phantom_membrane`, because the wind charge is 1.21 content and an empty tag would make the
  concentrated cloud seed uncraftable. A datapack can change that default.
- The tags `enderio:crops` and `enderio:seeds` hold the forge tags plus the vanilla items that only
  the NeoForge 1.21.1 tags `c:crops` and `c:seeds` list, which upstream points them at (bef7784,
  `MachineItemTagsProvider.java:32-33`). Forge 47's `forge:crops` holds only beetroot, carrot, nether
  wart, potato and wheat (`forge-1.20.1-47.4.0-universal.jar`), so the crops tag adds cactus, cocoa
  beans, melon, pumpkin and sugar cane, and the seeds tag adds torchflower seeds. Without sugar cane
  the reagent slot refused it, and its 0.5 factor in `vat_reagent.json` never applied.
- The output is capped at the output tank, see [Local divergences](#local-divergences).
- The Vat is the first survival source of `enderio:nutrient_distillation`. The Killer Joe of EnderIO
  Legacy Machines compares that exact fluid id.

The bundle is done in e7e29bd, a175998 and bef7784. Beyond the list above it brings these changes.

- The block uses `baseMachine` with the 6.x machine model, and both power states of its blockstate
  point at the one model `vat_combined`, as upstream has no active model
  (`MachineBlocks.java:252`). The item keeps the tanks, the reagents and the running task through the
  `copy_nbt` loot of the 6.x machines.
- The 6.x `MachineFluidHandler` checks `canExtract` in `drain(FluidStack)` but not in `drain(int)`,
  which always drains the first tank, here the Vat input. The Vat's handler overrides `drain(int)` to
  drain only the output tank (`VatBlockEntity.java:184-194`). The bucket interaction of
  `IFluidItemInteractive` gains the `canExtract` and `canInsert` checks of 1cb2e1420
  (`IFluidItemInteractive.java:38` and `55`). The fluid tank, its only other user, allows both, so it
  behaves as before.
- The task builds its output again from the reagents it consumes when it places the output
  (`VatBlockEntity.java:285`). The 6.x task fixes its outputs when it starts and consumes the inputs
  after the output is placed, so a reagent swapped for a weaker one of the same tag during the task
  could otherwise keep the higher output. This is the rule of the soul binder fix in 91a7f49.
- The JEI transfer goes through `VatRecipeTransferHandler`. It checks the fluid in the input tank like
  the 8.x `VATTransferHelper` and hands only the two reagent slots to JEI's basic transfer
  (`VatRecipeTransferHandler.java:72-77`), because JEI's own transfer counts every input slot and
  would refuse the fluid slot. The base has no `ServerboundTransferItemsPacket`.
- The packets `VatMoveTankPacket` and `VatDumpTankPacket` sit at the end of `MachinePackets`
  (`MachinePackets.java:24-25`), so they shift the ids of the conduit packets. The protocol
  "1.0-skytekx.1" of 6a3268f covers every packet of the unreleased 6.3.0-skytekx.1 and was not
  raised again. Later bundles of the machine lane append after the two Vat packets.
- Unchanged from 6.x, `MachineFluidHandler.drain(int)` drains tank 0 without a `canExtract` check in
  every other machine. Its identity check against `FluidStack.EMPTY` never fails, because
  `MachineFluidTank.drain` returns a new stack (`MachineFluidHandler.java:186-197`). External pipes can therefore still pull the insert only XP tank of the soul
  binder. The obelisk, charger and spawner bundles keep this in mind for any machine with several
  tanks.

The test class of the integration placed Vats in a dev server with Curios and AE2 on the classpath.
It checked the nine loaded recipes, the data map values, the reagent tags with sugar cane, melon and
torchflower seeds, which items the reagent slots take in both orders, the bucket and capability rules
of both tanks, the move and dump actions, the drop and its placed copy, hootch in both reagent orders,
nutrient distillation, blue ice capped at 8000 mB and its wait while the output is full. All 38 checks
passed (workflow scratchpad, `bp2/enderio/b2int/runserver-int.log`). The porter's earlier run of the
same class passed 32 of 33 (`bp2/enderio/b2/runserver1.log`), and its one failure, the drop check,
passed in the rerun with a fresh world. Both runs show only the two dev ERROR lines of the fix pack
run, the client mixin of Athena on the dev classpath and the empty `layers` key of the dev
`server.properties`.

Nobody has tested these yet. No client joined, so the screen layout, the move and dump buttons sent
as real packets, all nine recipes in JEI and EMI with the reagent factors, the JEI transfer, two
players in one Vat menu and the synced data map values on a client are open. The Killer Joe of
EnderIO Legacy Machines burning Vat made nutrient distillation was not run, because the dev
environment cannot load the production jar of that mod. Its evidence is the matching fluid id and the
compat scan. Forged move and dump packets from out of reach or without the menu open need a modified
client.

### Obelisks

- The obelisk manager capability is generic, and Forge erases the type argument of a
  `CapabilityToken` (checked in `fmlloader-1.20.1-47.4.0`). A second token of the same interface would
  return the aversion manager. The inhibitor and relocator managers therefore get their own non-generic
  interfaces next to the unchanged aversion one.
- The aversion obelisk gets two fixes. Event positions use `BlockPos.containing`, because
  `AversionObeliskManager.java:34` truncates toward zero and misses chunks at negative coordinates. Its
  spawn check consumes energy for real on the second call.
- The inhibitor obelisk (f216165c6, d8261275b, a0189250c, 8254bda11, 71328eba8) cancels Forge's
  `EntityTeleportEvent` when the start or the target lies in its range. The commands `/tp` and
  `/spreadplayers` stay exempt. Ender IO's own travel posts that event (`TravelHandler.java:287`), and
  so do Mekanism, Tinkers' Construct, Ars Nouveau, Iron's Spells, Botania, Apotheosis, Cataclysm,
  Twilight Forest, Create, Thermal, Cyclic, endermen and shulkers in the pack.
- The relocator obelisk (f216165c6) moves filtered natural spawns into its range with `moveTo`, not
  `teleportTo`, see [Arclight notes](#arclight-notes).
- The attractor obelisk (461cf0511, 65fe4d639, ef97d54da, d061a0bb6) pulls filtered mobs and skips
  bosses unless the config allows them. Its fake player is created on the first server tick, never on
  load, because a load time creation crashed next to Flux Networks upstream (ef97d54da).

### Charger and keybinds

- The wireless charger (4ad73fff0, e65b3feb0) needs a capacitor and power. It charges the energy items
  in the inventories and Curios slots of the players in its range. The antenna above it adds 16 or 32
  blocks of range through the data map `range_extender`.
- The charger charges every 5 ticks by default with the transfer scaled to match (new config key
  `chargeInterval`), which bounds the slot sync traffic on a shared server.
- Upstream computes the range with a block update during load. The fork computes it on load without an
  update and syncs it on the first server tick, the pattern upstream fixed for obelisks in 4dd6e5e09.
- The travel staff and the powered dark steel tools no longer flicker while they charge in the hand.
  The electromagnet and the levitation staff already avoid that through `PoweredToggledItem`.
- New keybinds (565ef4c3e) blink with the travel staff and toggle the electromagnet, also from a
  Curios slot. The server checks the item, the cooldown and the energy. Both keys ship unbound, see
  [Local divergences](#local-divergences).

### Filters

- The basic item filter keeps 5 slots and the advanced item filter 10, with their ids and the NBT keys
  `IsNbt`, `IsInverted` and `ItemEntries`. Ender IO 8.0 enlarged all filters, and the fork does not
  follow that.
- New are `enderio:big_item_filter` and `enderio:big_advanced_item_filter` with 36 slots each and the
  recipes of a829f16dd. The advanced filters gain a damage mode (ignore, damaged, undamaged) as a 6.x
  cycle button.
- The new `enderio:limited_item_filter` (1a85694c2, f0b62d322, 292bd84ed) keeps a count per entry.
  Item conduits clamp what they move to that count through a new additive API interface
  `LimitingItemStackFilter`. EnderIO Legacy Machines only calls `test()`, so its Farming Station and
  Killer Joe treat the limited filter as a plain whitelist.
- Filter items show their entries in a tooltip like a bundle (824d70feb, 0c79259d9, ba8618f6f), and
  unconfigured filters say so. The tooltip classes register on the client only.

### Conduit improvements

- The template is the item conduit priority of 6.2.12 (37890e67d), a per side `Priority` from -9999 to
  9999 with plus and minus buttons.
- Energy conduits get the same priority, and the round robin index stays per priority group. The 8.x
  equal distribution (57fc40d61) stays out.
- Fluid conduits get priorities on the pressurized and ender tiers only, as upstream does (6ecc1c788).
  In 6.x those are `FLUID2` and `FLUID3` (`EIOConduitTypes.java:44-47`). Fluid locking stays.
- Redstone conduits can output strong power on chosen sides (`StrongSides`), so the signal passes
  through a block, as with 8.0.
- The filter in a conduit connection opens from the conduit GUI (04e69b8e6, 7fe86947b). The server
  checks the position and the reach through `Packet.canAccessBlock`, the conduit and the filter, and a
  back button returns to the conduit menu. The menu closes when the bundle, the conduit or the connection disappears.
- The GUI sends only its own fields, and the server clamps them. The energy path never touches the
  stored energy.

### Conduit facades

- The fork adds four facade items, `enderio:conduit_facade`, `hardened_conduit_facade`,
  `transparent_conduit_facade` and `transparent_hardened_conduit_facade`, with one facade per bundle
  in the style of 7.1 (78e627263, 1a14aa55e). The Painting Machine paints them. Hardened facades
  resist explosions, and transparent ones show the conduits to a player who holds a Yeta wrench or an
  item of `#enderio:hide_facades` (3c8717b0c).
- A faced bundle takes the shape, light, friction, sound, explosion resistance and drops of its facade.
- The ghost pass of 78e627263 renders at `AFTER_TRIPWIRE_BLOCKS`, which Forge 47 has and Embeddium
  fires. Removing one facade keeps the others of the chunk (3a7275c78, b6c650ebf). The
  `AddSectionGeometryEvent` renderer of 397c648f5 does not exist in Forge 47 and stays out.
- AE2 facade items work as basic facades and drop back as themselves. The fixes ff23e13c4, eb53f388b,
  7de602e53, bceadbded and ce6d39e8a come along. AE2 cable facades stay untouched.
- The 6.x code already has a disabled facade path (`ConduitBlock.java:73`, `ENABLE_FACADES = false`)
  and a per direction map `Facades` in the bundle save (`ConduitBundle.java:375`). The new design
  replaces both, see [Savegame notes](#savegame-notes).

### Powered spawner

- Capture mode (a90e5c512, 5169b356b, f8049645c) fills empty soul vials instead of spawning, for the
  same energy per vial. It blocks while the output is full and honours both blacklists. The mode
  changes through a data slot, so no new packet is needed.
- In the pack's spawn type `ENTITYTYPE` (`config/enderio/machines-common.toml:160`) a captured vial
  holds only the entity type. Only `COPY` mode writes the full entity, as upstream's capture task does.
  Copying the full tag in `ENTITYTYPE` mode would dupe held items, armour and villager trades.
- The vial stacking of 1853ceeb5 does not apply, because the filled soul vial stacks to 1 in 6.x
  (`EIOItems.java:283-284`).
- The Mind Killer (84bfe2477) sits above a powered spawner and spawns mobs without AI. A tick handler
  still lets them fall and be pushed into grinders. Upstream credits that handler to the Apothic
  Spawners project, see [License conditions](#license-conditions).
- The same commit brings two `COPY` mode fixes, a fresh UUID for each copy and no second spawn
  finalisation. The pack uses `ENTITYTYPE`, so they matter little here.

### Visual polish

- Clear glass and fused quartz connect through Athena 3.1.2, a client only mod of the pack
  (`modrinth.index.json:354`), with the ten CTM textures of cb6a40551. Coloured glass keeps its tinted
  model.
- Athena 3.1.2 sets no render type, since its `AthenaBakedModel` does not override `getRenderTypes`
  (javap, inventory `critic_notes`). The fork wraps the connected model in a cutout render type, or the
  glass would render solid. Without Athena the glass falls back to today's model.
- Machines show particles while they run (f761802d6, particles only), with a client config switch.
  The sounds of that commit stay out.
- Walking and landing on a conduit bundle shows conduit or facade particles (6ae3dd177).

## Mods that build on it

| Mod | Version or file | How it uses Ender IO | What the plan keeps |
|---|---|---|---|
| EnderIO Legacy Machines | 0.4.9, `local-mods/eio_legacy_machines-0.4.9.jar` | Requires `enderio` `[0,)` and links the 6.x API (`ports/enderio/UPSTREAM.md:80`) | The 21 linked members stay, checked by the compat scan after every bundle |
| Exchangers | 3.5.1, `local-mods/Exchangers-1.20.1-3.5.1.jar` | Optional `enderio` `[1.0,)`, recipes with Copper Alloy and the iron and energized gears | The 6.x items and alloy recipes, since the 8.1 rebalance is excluded |
| Applied Energistics 2 | 15.4.12 (`modrinth.index.json:264`) | ME conduits, and AE2 facades through `AE2Integration` | The ME conduits with the first tick guard, AE2 facades as conduit facades |
| Mekanism | 10.4.16.80 (`modrinth.index.json:1884`) | Chemical and heat conduits of 6.1 | Unchanged |
| Spartan Shields | 3.1.1 (`modrinth.index.json:2514`) | Optional dark steel riot shields, with models and textures in its jar | The 6.x dark steel items and the armory stay |
| Curios | 5.14.1 (`modrinth.index.json:1014`) | Optional `curios` `[5.2.0,)` with the charm tag and the player slot data (10ba7e1, effd892). Nothing calls the helper yet | New optional support for the staff and the electromagnet |
| Athena | 3.1.2, client only (`modrinth.index.json:354`) | The CTM model of the capacitor bank | Connected glass for clear glass and fused quartz |
| AllTheLeaks | 1.1.1, `local-mods/` | Mixins into two Ender IO methods with hash checks | Both methods stay untouched |
| Inventory Sorter | 23.0.11, `local-mods/` | Skips the slot classes Ender IO names in its IMC | New ghost and filter slots join that list |
| Soul Shards datapack | The file `kubejs/data/soulshards/recipes/soulsteeler.json` | A recipe of type `enderio:enchanting` | The 6.x recipe formats stay |
| AlmostUnified | The config `config/almostunified/unify.json:10` | Lists `enderio` in `modPriorities` | The `forge:` tags of the ported content |

No KubeJS script references Ender IO. The only file under `kubejs/` that names it is the Soul Shards
recipe.

## Porting rules

Every bundle spec in the inventory repeats these rules in its common rules part.

- Data components of 1.21.1 become item NBT with the 6.x key names of `CoreNBTKeys`, `MachineNBTKeys`
  and `EIONBTKeys`. Data attachments become Forge capabilities. Packets become 6.x `Packet` classes
  appended to their list. Holders become Registrate entries, and `c:` tags become `forge:` tags, which
  AlmostUnified relies on.
- Data folders keep their 1.20.1 names: `recipes/`, `loot_tables/`, `advancements/recipes/` and
  `tags/items`, `blocks`, `fluids` and `entity_types`.
- Forge 47 has no `SizedFluidIngredient`, no `AddSectionGeometryEvent`, no `EntityTickEvent` (the fork
  uses `LivingEvent.LivingTickEvent`) and no content or item tags of vanilla 1.20.5 and later.
- Every new capability gets its own non-generic interface with `@AutoRegisterCapability`.
- No code calls `setChanged`, `sendBlockUpdated` or a neighbour's block state from `setLevel` or
  `onLoad`. That is the chunk load deadlock upstream fixed in 4dd6e5e09 and ef97d54da. Such work waits
  for the first server tick.
- Client only classes stay under `client/` and register from client only events, so the dedicated
  server starts. The fork adds no mixins.
- AllTheLeaks 1.1.1 patches `CountedIngredient.getItems` and `WrappedEnchanterRecipe.getLapis` through
  mixins that check the method hash. The fork never changes those two methods.
- Every new filter or ghost slot class joins the Inventory Sorter blacklist in `EnderIO.java`.
- The fork commits its datagen output in `src/generated/resources`, the English `en_us.json` and the
  upside down `en_ud.json` included. A changed provider means hand written generated files that match
  the provider byte for byte, and the release runs `runData` to reconcile them.
- EnderIO Legacy Machines 0.4.9 links 10 classes and 21 members of the 6.x API, among them `IOMode`,
  `ItemStackFilter.test`, `EIOCapabilities.FILTER`, the `MachineBlockEntity`, `MachineMenu` and
  `MachineScreen` members it overrides and the `IOConfigButton` constructor (inventory, common rules).
  None of these signatures changes, and the compat scan runs after every bundle.

## Excluded

Left out on purpose (inventory, `plan.excluded`):

| Upstream work | Reason |
|---|---|
| The 8.x conduit core (`conduit-core-rewrite-8x`) | Ender IO 7.0 and 8.0 change the save format of conduit bundles and networks, "one way due to save data format changes", and no migrator from 6.x exists. It would also raise the Arclight risk. Priorities, strong power, the filter GUI and facades are rebuilt on the 6.x core instead |
| Mixed tier conduits (`mixed-tier-conduits`) | Ender IO 8.2 cannot be downgraded without loss of data, and P2P energy conduits void every stored conduit buffer |
| Fluid conduit simplification (`fluid-conduit-simplification`) | Dropping fluid and chemical locking belongs to the 8.2 conduit core. The 6.x conduits keep locking |
| Tiered energy conduits (`tiered-energy-conduits`) | Enhanced and ender energy conduits are datapack conduit types of the 7.x core, while 6.x registers conduit types in code |
| Energy conduit rework (`energy-conduit-rework-and-throttling`) | Equal distribution, removed throttling and per node caps target the 7.x and 8.x energy network. Only the priority is ported |
| Rename and rebalance of 8.1 (`conduit-rename-and-recipe-rebalance-8-1`) | Renamed conduits, no Copper Alloy, no wood and stone gears and new alloy recipes. Exchangers 3.5.1, Mystical Agriculture recipes and quests use the 6.x ids |
| Removal of the Primitive Alloy Smelter (`primitive-alloy-smelter-removal`) | Removes a block of 6.x worlds without a migration |
| Removal of the enchantments (`enchantment-removal`) | Soulbound, Auto Smelt, Repellent, Shimmer, Withering and XP Boost sit on items in the live world and in `config/apotheosis/enchantments.cfg` and `config/enderio/base-common.toml` |
| Dark steel armory changes (`dark-steel-armory-changes`) | The 1.21 armory went through a flag experiment and was removed in 8.1. The 6.x armory with the dark steel riot shields of Spartan Shields stays |
| Removal of the lights (`light-blocks-removal`) | Removes blocks placed in 6.x worlds |
| Farming Station of 8.x (`farming-station-8x`) | EnderIO Legacy Machines 0.4.9 already brings a Farming Station and the Killer Joe on the 6.x API |
| Ender IO block and Niard (`ender-io-block-and-niard-experiments`) | Unfinished upstream work behind feature flags |
| Weather obelisk (`weather-obelisk`) | Not on the list to take. On a shared server any player could change the weather, so it needs an owner decision and a permission gate. About 10 h on top of the obelisk base |
| Phantom solar and the solar rewrite (`phantom-solar-and-solar-rewrite`) | A new solar tier, solar fluids and a rewritten solar graph. Liquid darkness and its Vat recipe go with it |
| Block detector (`block-detector`) | Not on the list to take. Upstream gave it a model and a recipe only on 2026-10-28 |
| Capacitor bank graph (`capacitor-bank-graph-rework`) | A move of 1536 lines that risks the energy of every bank in the live world. The stack dupe is fixed in the fix pack |
| Machine networking rewrite of 7.1 (`machine-networking-rewrite-7-1`) | Internal, and the 7.1 notes say a rollback loses IO config and redstone settings |
| Machine push buffs of 7.1 (`machine-io-push-buffs`) | Balance experiments that raise the cost per tick. The 6.x item transfer moves one item per call and has no dupe there |
| Retexture and GUI redesign of 8.x | An asset and GUI overhaul of about 30 h. The damage mode of the filters uses a 6.x cycle button instead of the new picker |
| Machine sounds (`machine-sounds`) | The 1.21.1 sounds count as CC BY-NC 3.0 material from freesound, and the commit ships no credit list. The particles of the same commit are ported |
| Vial of the void (`void-potential-vial-of-the-void-exp-rod`) | Renames the experience rod, which breaks saves without aliases, and adds a new progression |
| Fire crafting rework (`fire-crafting-rework`) | A progression change outside the scope |
| SAG Mill additions (`sag-mill-recipe-additions`) | Raw ore recipes and compat recipes. AlmostUnified and Mekanism govern the pack's ore processing, and KubeJS can add them later |
| Soul and binding extras (`soul-and-binding-qol`) | Extras outside the scope plus fixes for the unified soul API of 8.x |
| Erase recipes (`erase-recipes`) | Extras outside the scope, 75 generated files |
| Chemical, Laser IO and Refined Storage filters (`chemical-laserio-rs2-filters`) | Chemical filters are new content outside the scope, and Laser IO and Refined Storage are not in the pack |
| FTB Ultimine and Endergy (`ftb-ultimine-and-endergy`) | FTB Ultimine is not in the pack, and Endergy is a separate addon |
| Travel anchor rendering (`travel-anchor-rendering-polish`) | Client polish. The suffocation fix of 59b00dd66 is in the fix pack |
| JEI and Jade extras (`jei-jade-extras`) | Bookmarks and click areas are in 6.x already. The Vat transfer helper comes with the Vat |
| Lapis copy in the enchanter JEI category (`jei-enchanter-lapis-copy`) | Commit 83177fc53 changes `WrappedEnchanterRecipe.getLapis`, which AllTheLeaks 1.1.1 already fixes through a hash checked mixin. A changed method would make AllTheLeaks skip its fix |
| Recipe format changes (`grinding-ball-data-map-and-recipe-format-changes`) | Data maps for grinding balls, `SizedFluidIngredient` tank recipes and components in recipes would break pack datapacks and `kubejs/data/soulshards/recipes/soulsteeler.json` (type `enderio:enchanting`) |
| Optional fluid codec of the Vat (`vat-optional-fluidstack-codec`) | Commit fec605fba fixes a NeoForge component codec. The port keeps fluids in NBT |
| Smaller 8.x changes (`misc-8x-qol`) | Vacuum range, wrench rotation, external capacitor insertion, throttling and idle energy configs, renderer fixes of 8.x and buffer fixes for pooled buffers that 6.x does not use |
| Painted walls (`painted-walls`) | New content outside the scope. Save safe and about 3 h if the owner wants it |
| Athena resource pack (`athena-resourcepack-restructure`) | Athena 4.x on 1.21.1 only. The pack ships Athena 3.1.2 |
| Regression fixes of 8.x (`8x-regression-fixes`) | They fix code that the NeoForge port or the 8.x conduit core introduced. Spot checks found the 6.x paths free of these bugs |
| Translation refresh (`translations-refresh`) | The 8.x lang files use 8.x keys. Only English entries for ported content are added |
| Port and build churn (`port-and-build-churn`) | NeoForge, Regilite and MDG ports, restructures, formatting, game tests and CI, with no player effect |
| The 8.x core of the experiment branch (`experiment-1201-backport-core`) | Read for API translations only. Its core would bring the one way conduit format and drop the compatibility with EnderIO Legacy Machines |

Already in the 6.x base, so no work (same file):

| Upstream work | Where it is |
|---|---|
| Vendor, version and licenses (`fork-bootstrap`) | Commits 8799e05, dd66c98 and e508261 on this branch |
| Unreleased fixes of the 1.20.1 branch (`unreleased-1201-fixes`) | The vendored tip carries f9e1806c, 18ee1b17 and the item conduit stack size fix of 6.2.18 |
| Forced connections (`forced-connections`) | Ender IO 6.2.7 (af6eb5219, 98255e163) |
| Item conduit priority (`item-conduit-priority`) | Ender IO 6.2.12 (37890e67d), the template for the energy and fluid priorities |
| Aversion obelisk and entity filter (`aversion-obelisk-and-entity-filter`) | Ender IO 6.2.1 (75bb8b250). Its energy and position bugs are fixed in the obelisk bundle |
| Rebind recipe of the powered spawner (`powered-spawner-rebind-recipe`) | Ender IO 6.2.5 (f49ba60d). Its data loss is fixed in the fix pack |
| Conduit probe copy and paste (`conduit-probe-copy-paste`) | Ender IO 6.2.3, 6.2.14 and 6.2.15. The conduit bundle adds the new priorities to it |
| JEI ghost slots and click areas (`jei-ghost-slots-and-click-areas`) | Ender IO 6.2.2 (6257e420e) |
| Yeta wrench pickup (`yeta-conduit-pickup-qol`) | The careful stepping check of fd3fa0cdf and the pickup sound of 370ba25c6 |
| Mekanism chemical and heat conduits and CC:Tweaked (`mekanism-chemical-heat-conduits-cct`) | The conduit overhaul of 6.1 (d1035ddf1) |
| Refined Storage conduits (`refined-storage-conduits`) | Ender IO 6.1. Refined Storage is not in the pack |
| Obelisk shapes (`obelisk-voxel-shapes`) | Ender IO 6.2.12 (1855ea5da) |
| Load fixes for large conduit networks (`large-conduit-network-load-fixes`) | Ender IO 6.2.9 and 6.2.10 |
| Fixes made on both branches in 2024 (`shared-1201-1211-fixes`) | Filter dupe, items voiding in filters, wrench conduit dupe, pickup noise, item loss, conduit upgrade voids, XP obelisk lockup and the crafter fixes, each checked as an ancestor of `1.20.1` |

## License conditions

- Ender IO is under The Unlicense (`ports/enderio/LICENSE.txt`). The license check of the research
  refuted the first verdict of "no conditions", because parts of the jar carry other terms (workflow
  scratchpad, `bp2/research-result.json`, `license_checks`).
- Code adapted from Patchouli, the `GhostBuffers` and `GhostRenderLayer` classes and the `initBuffers`
  method of `IOConfigWidget` and the block rendering of the JEI `FireCraftingCategory`, is
  CC BY-NC-SA 3.0. The jar as a whole may therefore only be passed on for non-commercial purposes,
  with attribution and with changes to those parts shared alike (`ports/enderio/NOTICE`, Distribution).
- The jar carries the MIT notices for the soul data reload listener `SoulDataReloadListener`, for the
  Mekanism derived `IFluidItemInteractive` and temp file save of `ConduitSavedData`, for GraphLib3 and
  for checker-qual. It carries Apache-2.0 for Guava, failureaccess, listenablefuture, Error Prone
  annotations and JSR 305, and an MPL-2.0 source link for Registrate MC1.20-1.3.11.
- All texts ship in the jar as `LICENSE_enderio`, `NOTICE_enderio` and `META-INF/licenses/enderio/`
  (e508261, `UPSTREAM.md`, Build changes). The 6.2.18-beta release jar carries none of them, and its
  `mods.toml` says `CC0`.
- The build is marked unofficial through the suffix `-skytekx.N`, the `mods.toml` description and the
  manifest's `Implementation-Vendor` (dd66c98, e508261). The release plugins that publish to the
  official Ender IO projects are removed.
- The source is published under `ports/enderio/` in this repository, and `NOTICE` points at the git
  history of that folder.
- A bundle that ports code derived from a third party appends it to `NOTICE`. If the data map shim
  reuses the soul data reload listener, it joins that MIT entry.
- The Mind Killer tick handler of 84bfe2477 carries upstream's credit to the Apothic Spawners project.
  Its license is not checked yet. The port either checks it and adds a `NOTICE` line, or writes the
  four lines from scratch.
- The 1.21.1 sounds stay out unless the CC BY-NC 3.0 credit list of their freesound sources is
  recovered and shipped with them.
- New textures and models come from the 1.21.1 tip of the same repository, whose `LICENSE.txt` is the
  same Unlicense text as on `1.20.1` (license check of the research).
- The permission record `modrinth-permissions/EnderIO-1.20.1-6.2.18-beta-all.jar.md` still says that
  the Unlicense "unconditionally permits" redistribution, which the Patchouli parts contradict. The new
  jar needs a record that names the non-commercial condition, the NOTICE and the SHA-1 of the
  reproducible build, plus the license texts in `licenses/`. That rewrite is separate pack work.

## Savegame notes

- No registry id, NBT key, block state property, capability id, saved data key or config key is
  renamed or removed.
- The new registry entries are the blocks `enderio:vat`, `inhibitor_obelisk`, `relocator_obelisk`,
  `attractor_obelisk`, `mind_killer`, the wireless charger and its antennas
  `wireless_charger_antenna` and `wireless_charger_antenna_advanced`, the items `big_item_filter`,
  `big_advanced_item_filter`, `limited_item_filter` and the four conduit facades, and the recipe type
  `enderio:vat_fermenting`. The Vat also registers the block entity type and the menu `enderio:vat`
  and the recipe serializer `enderio:vat_fermenting` (e7e29bd). Its saved data uses the 6.x keys
  `Fluid`, `Items` and the task keys of the other crafting machines.
- Two level capabilities are new, `enderio:inhibitor_obelisk_manager_capability` and
  `enderio:relocator_obelisk_manager_capability`. The obelisk managers live at runtime only and are
  rebuilt on load.
- The new NBT keys are optional, with a default that matches today's behaviour. They are `DamageMode`
  on filters (ignore), `Priority` on energy and fluid conduit sides (0), `StrongSides` on redstone
  conduits (0), `FacadeProvider` on conduit bundles (none), `SpawnerMode` on powered spawners (spawn)
  and the persistent entity tag `enderio:movable`.
- The powered spawner gains two inventory slots after the capacitor. That is safe only because
  f9e1806c sizes `MachineInventory` from the slot layout and ignores the saved `Size` key, so no code
  may read `Size` again.
- Tanks keep the 6.x fluid save format, so existing tanks, XP obelisks and soul engines load
  unchanged.
- Bundles keep reading the old `Facades` map but never write it again. Existing maps are empty,
  because 6.x never enabled facades.
- Existing basic and advanced filters keep their sizes and entries, and existing spawners stay in spawn
  mode with their soul, capacitor and progress.
- A rollback to 6.2.18-beta would lose the new blocks and items like any removed registry entry, and
  the new keys would be ignored. Nobody has tested a rollback.
- A load of a copy of the live world with conduits, tanks, obelisks and spawners must pass before any
  deploy (inventory, `critic_notes`, For the ship stage).

## Arclight notes

The research rated the Arclight risk medium for the 8.x conduit core, which ticks across chunks. The
additive subset keeps the 6.x conduit core, so the risk is low. These points need a look on the
production Arclight.

- The inhibitor obelisk cancels Forge's `EntityTeleportEvent`. Nobody has checked whether Bukkit
  plugin teleports post that event, so `/home` and `/tpa` of EssentialsX need a test inside a range.
- The relocator obelisk sets the position of a fresh mob with `moveTo`. In `FinalizeSpawn` the mob is
  not in the level yet, and Arclight wraps `teleportTo` in its Bukkit teleport handling.
- The wireless charger writes to player inventory stacks, throttled by `chargeInterval`.
- Capture mode spawns no entity, and the spawn reason of the powered spawner stays `SPAWNER`. A mob
  limit plugin, if one is ever added, needs a check.
- Before the fix pack any client could change data slots of loaded machines anywhere and the stored
  energy of energy conduits. The server runs EssentialsX as its only plugin (see
  [plugins](plugins.md)), so no claim plugin limited that. The fix pack closes it in be881bc and
  f142998. A player within reach can still change the settings of any machine, as the screens allow.
- The fork adds no mixins, so nothing collides with the Bukkit patches.

## Local divergences

These differ from upstream on purpose and must survive a re-sync.

- The version is 6.3.0-skytekx.1 in the 6.x series, and the network protocol is "1.0-skytekx.1".
- Both new keybinds ship unbound. Upstream binds them to G and M, which the pack already uses. G opens
  Curios and is bound by Ars Nouveau, Simple Voice Chat, LittleTiles and GuideME. M opens Xaero's
  World Map and is bound by Simple Voice Chat and LittleTiles (scan of the `KeyMapping` constructors in
  the pack jars, inventory `critic_notes`).
- The tag `enderio:wind_charges` holds phantom membrane, and `enderio:meat` lists the vanilla meats.
- The Vat caps its output at the 8000 mB output tank. Upstream multiplies the reagent factors and
  stalls for good above the tank size: cloud seed with packed ice makes 9000 mB, with blue ice 81000 mB.
- The tags `enderio:crops` and `enderio:seeds` hold the forge tags plus the vanilla items of the
  NeoForge 1.21.1 tags `c:crops` and `c:seeds` that the forge tags lack.
- The Vat drains only its output tank through `drain(int)`, and its task builds the output again from
  the reagents it consumes. Both are SkyTekx fixes without an upstream commit.
- Vat recipes read their input and output objects like the 6.x tank recipes, with `fluid` or `tag`
  plus `amount`. Only the reagent field names `left_reagent` and `right_reagent` match 8.x, so an 8.x
  recipe file does not load unchanged.
- Capture mode writes only the entity type in `ENTITYTYPE` mode and the full entity only in `COPY` mode.
- The fork ships no size for the Curios `charm` slot.
- The basic and advanced item filters keep 5 and 10 slots.
- The damage mode of the filters is a 6.x cycle button.
- Data maps run through the fork's own reload listener in upstream's JSON format.
- The charger charges every `chargeInterval` ticks, 5 by default.
- The packet validation with the reach test of the menus, the hardened item push of
  `TransferUtil.moveItems`, the output recheck of the soul binder, the entity filter slot without a
  client level, the energy fix of the aversion obelisk and its position rounding are SkyTekx fixes
  without an upstream commit.
- The output check of crafting machines inserts copies into its snapshot, where upstream's dccb83cda
  inserts the outputs themselves.
- Connected glass wraps Athena's model in a cutout render type.

## Open questions

Nobody has verified these yet (inventory, `critic_notes`, Things nobody has verified).

- Whether Bukkit plugin teleports post Forge's `EntityTeleportEvent`, which decides whether the
  inhibitor blocks `/home` and `/tpa`.
- Whether upstream's electromagnet pulls items from a Curios slot or only toggles there. If it does not
  pull, the Curios class gets a curio tick that runs the magnet logic.
- The license of the Apothic Spawners snippet.

The owner may also want the content left out with a small cost: the weather obelisk (about 10 h, with
a permission gate) and the painted walls (about 3 h). The wind charge default is the owner's to change
([Decisions](../decisions.md)).

## Operator notes

- Before a release, every bundle passes the compile tasks of every source set, a full build with
  `LC_ALL=C.UTF-8` and the compat scan with `TOTAL MISSING: 0` against EnderIO Legacy Machines. A
  dedicated `runServer` reaches `Done` with and without Curios.
- The release stage runs `runData` and reconciles the generated resources, then swaps the `-all` jar
  into `local-mods/` and removes the 6.2.18-beta jar, so two Ender IO jars never load together. The
  same swap goes into `mods/` by hand, as `AGENTS.md` asks (see [mod set](mod-set.md)). The pack
  build takes its files from git HEAD, so the jar, the permission record and the license texts are
  committed first.
- Server and clients update together. The protocol "1.0-skytekx.1" and the new registry entries make
  Forge refuse a client of an older pack at login.
- The first boot writes the new config keys, such as `chargeInterval`, the obelisk ranges,
  `attractor_pull_bosses` and the particle switch. Forge appends missing keys and keeps old values.
  The regenerated `config/enderio/*.toml` files are committed. The `"Spawn Type"` stays `ENTITYTYPE`.
  Config files change only while the java process is gone.
- A test on a copy of the live world with the production Arclight jar comes before the deploy. It loads
  conduits, tanks, obelisks and spawners, checks the inhibitor against pearls, chorus fruit, `/tp`,
  `/home` and `/tpa`, and reloads an attractor next to a Flux Networks point. The boot keeps the
  baseline of 2 accepted ERROR lines (see
  [logs and crashes](../server/logs-and-crashes.md#boot-error-baseline)).
- The server stops over RCON with `save-all flush` and `stop`, and the java process must be gone before
  the jar swap.
- Players learn that the travel staff and electromagnet keys are unbound and sit under the Ender IO
  category of the controls, that the inhibitor obelisk blocks pearls and other teleports in its range,
  and that the Vat now makes nutrient distillation for the Killer Joe.

## History

- The research of 2026-11-04 recommended finishing upstream's experiment branch, forward porting it to
  8.2.12 and writing a migration of 6.x worlds, about 480 h (workflow scratchpad,
  `bp2/research-result.json`, record `enderio`). The plan replaced that with the additive subset,
  because the 8.x conduit format is one way, EnderIO Legacy Machines links the 6.x API and Exchangers
  uses Copper Alloy, which 8.1 removed.
- The first inventory draft had 10 bundles of 232 h, with the vendor, version and license work as part
  of the first bundle (workflow scratchpad, `bp2/enderio/structured.json`). The review moved the done
  work to the excluded list, chained the machine and the conduit bundles into two lanes, added the
  packet validation and the AE2 guard to the fix pack and corrected the capability, Athena, keybind,
  Curios and spawner details (inventory, `critic_notes`).
- The first packet validation of the fix pack (be881bc) refused a packet when the block centre was more
  than 8 blocks from the feet, the distance of a vanilla container. The review replaced it in f142998
  with the reach test of the menus, because the Apotheosis 7.4.8 reach affix and gems open screens from
  9 to 13 blocks away, and every change made there was dropped.

Related: [Twilight Forest backport](twilightforest-backport.md), [mod set](mod-set.md),
[publish](publish.md), [Arclight runtime](../server/arclight.md), [open items](../open-items.md).
