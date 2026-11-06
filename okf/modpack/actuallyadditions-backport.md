---
type: Plan
title: Actually Additions backport
description: How the unreleased 1.20.1 Forge branch of Actually Additions becomes a release for the pack, which fixes and features of the newer NeoForge lines up to 1.3.26 it takes in which bundles and order, which upstream work stays out, and the license, savegame, Arclight and operator rules of the fork.
tags: [modpack, mods, actuallyadditions, backport]
timestamp: 2026-11-06
---

# Actually Additions backport

Actually Additions (mod id `actuallyadditions`) is new to the pack. Upstream never released a build
for Minecraft 1.20.1. Its branch `1.20.1` stopped at 226a439 on 2024-03-04 with `mod_version` 1.1.52
for Forge 47.1.3, and the mod went on as NeoForge builds for 1.20.4, 1.21 and 1.21.1, up to release
1.3.26 of 2026-06-19 (`ports/actuallyadditions/UPSTREAM.md`, Upstream table). This plan makes the
vendored copy of that branch in `ports/actuallyadditions/` release quality and then backports the
bug and dupe fixes, the items the branch lacks, the balance and quality of life changes and the new
models and textures of the newer lines, the same way as the
[Twilight Forest backport](twilightforest-backport.md). The fork keeps the mod id and every registry
id, NBT key, block state and saved data key of the base, so a world made with the base build loads
unchanged. Energy stays on Forge Energy. The work is done for SkyTekx, a modpack by ts4.party.

Effort is given in estimated human hours, the unit of the Twilight Forest plan. The plan holds 67
features in 14 bundles, 264.5 h in all, and no bundle is over 40 h. The machine readable plan is
`ports/actuallyadditions-backport-inventory.json`. Its `plan.bundles` holds each bundle with its
feature ids, hours, dependencies and a spec that names for every feature the upstream commits, the
files, the Forge 47 translation, the savegame notes, the new ids and the test. The list
`plan.excluded` holds 16 entries of upstream work left out with their reasons, and `critic_notes`
holds the review that corrected the first draft. The review checked that each of the 551 upstream
commits after the fork point 97532ff that is not a merge appears in a bundle spec or an exclusion. A
second check against the critic's commit list in the workflow scratchpad (`aacritic/all.txt`) found
none missing in the committed file. The file `ports/actuallyadditions-backport-carryover.json` will
record what a bundle hands to a later one. It does not exist yet, and until the release commits it
the hand overs collect in the workflow scratchpad (`bp2/actuallyadditions/carryover.json`).

| Property | Value |
|---|---|
| Upstream | The Actually Additions repository named in `ports/actuallyadditions/UPSTREAM.md`, MIT License |
| Base | Branch `1.20.1` at 226a439 (2024-03-04), `mod_version` 1.1.52 for Forge 47.1.3, never released. It registers 121 blocks and 107 further items (`UPSTREAM.md`, What the base contains) |
| Port source | Branch `main` at cd9d9c63 (2026-06-19), release 1.3.26 for NeoForge 1.21.1 |
| Intermediate steps | Branch `1.20.4` at 0ebbd811 (1.2.14, NeoForge 20.4) and branch `1.21` at d9b0a714 (1.3.6, NeoForge 21.1). The 1.20.4 port commit 63fbbb33 has the base as its parent and is the reference for every reverse mapping |
| Fork | Folder `ports/actuallyadditions/`, Java 17, ForgeGradle 6.0.54, Forge 47.1.3 userdev, Parchment 2023.09.03 (`UPSTREAM.md`, Build changes) |
| Target version | Version 1.1.52-skytekx.1 in `ActuallyAdditions-1.20.1-1.1.52-skytekx.1.jar`, the reobf jar from `build/libs/`, see [Version](#version) |
| Ships as | A new jar in `local-mods/` and `mods/`. The Modrinth project has no 1.20.1 file the index could point at |
| Build check | The `compileJava` call in `UPSTREAM.md`, run with `/opt/jdk17` and `-Xmx2g`, with upstream's compiler settings |
| Integrations | Patchouli 85 (`modrinth.index.json:2169`), Curios 5.14.1 (`modrinth.index.json:1014`), JEI 15.74.0.233 (`modrinth.index.json:1584`) and EMI 1.1.24-skytekx.2 from `local-mods/`, all optional |
| Dependents | None. Of the 264 `mods.toml` files in the Arclight pack boot copy of the workflow scratchpad (`packboot-arclight/mods/`), none names `actuallyadditions` (scan of 2026-11-05) |

## Status on 2026-11-06

The vendored base (4142c4b), the reproducible Java 17 build of 1.1.52-skytekx.1 (9788dd8) and the
license and notice files in the jar (6bcbead) are on the branch
`claude/twilight-forest-port-mods-j2owbq`. Three clean builds of that state gave the jar SHA-1
`c37140ce60ec6da5ac33ebea66b3f9fa3951f7fd` (`UPSTREAM.md`, build change 5).

The baseline bundle is done. Its five commits are 54fb6c3 (fork metadata and the InvTweaks
removal), dbbfe12 (Patchouli, Curios and the Mixin setup), ef59d18 (the common config of `main`
and the client config), 8e0566f (the data generator fixes) and 51031ce (the boot smoke test), with
the review fix d3bd821. Two clean builds of 51031ce gave the jar SHA-1
`ba5e48f831da022f7eb45f763f1fdaf4db6e4794`. The review fix changes only a comment in `mods.toml`
and the text of `UPSTREAM.md`, and two clean builds of d3bd821 give
`342af9c288d7afb676105bf16b1466621edfa516` (4527513 bytes), which differs from `ba5e48f8` only in
`META-INF/mods.toml`.

The base jar `c37140ce` reaches `Done` alone on a dedicated Forge 47.4.18 server with 0 ERROR
lines, but it does not start in the Arclight pack. Immersive Engineering 10.2.0-183 ships the same
package `invtweaks.api.container`, and the module layer stops with a `ResolutionException` before
any mod loads (`UPSTREAM.md`, source change 8). So the OLD side of the savegame test is the base
6bcbead with only `src/main/java/invtweaks/` removed, jar SHA-1
`237e0fc65275a0204fe7008f144c395207e41205` (4557655 bytes). A copy and its build recipe sit in the
workflow scratchpad under `bp2/actuallyadditions/old-jar/`.

The Arclight pack boot copy of the workflow scratchpad ran three boots on 2026-11-05 with the
production jar `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar` (`packboot-logs/`, labels
`aa-ref-arclight`, `aa-old-arclight` and `aa-new-arclight`). The pack without the mod, the pack
with the OLD jar and the pack with the jar `ba5e48f8` each reached `Done` with 0 FATAL lines, no
crash report, no watchdog, a clean RCON stop and the same three ERROR signatures. They are the
AllTheLeaks line of the boot error baseline, a `RuntimeDistCleaner` line for `ModelBakery` and a
Nostalgic Tweaks line about its backup config, which every boot of the fresh test copy shows. No
boot logged a Mixin apply failure. The fork logs one WARN that the reference map
`actuallyadditions.refmap.json` could not be read, because the annotation processor writes it only
once a mixin exists. It goes away when the Item Tag filter brings the first mixin. The dev client
of the fork reaches the title screen and writes `actuallyadditions-client.toml`.

The data core bundle is done. Its four commits are 2053e6a (the `forge:` tags), 71370b7 (a recipe
for every block and item of the base), 63a8eff (the advancements) and a98f81c (the JEI categories),
with the review fix fb2f22c (`UPSTREAM.md`, source changes 9 to 12). The review fix lets the Lens
of the Killer take a real Sharpness V book and keeps the 3x3 slime block recipe off the slime balls
of Tinkers' Construct and Twilight Forest (see [Local divergences](#local-divergences)). The jar of
fb2f22c (SHA-1 `bb73c65ef81d17ef862f7231c28d66ebe67438d0`, 4605920 bytes, one build) reached `Done`
in 62.5 s on a dedicated Forge 47.4.18 server with AlmostUnified 0.11.0 and the pack config,
Farmer's Delight 1.3.4 and Patchouli 85, with 0 FATAL and 0 ERROR lines, a clean stop and an empty
`uncovered_recipes.txt` of AlmostUnified. Nobody has opened the JEI or EMI categories in a client
yet. This bundle makes the phantom placer and breaker, the Atomic Reconstructor and the Vertical
Digger craftable before the Arclight hardening gives them a fake player that Bukkit sees, so no jar
of this state may reach the live server.

The server safety bundle is done. Its four commits are f9b373b (client classes out of common
code), e2696b6 (the packet checks), 0b3c5ff (the drill, filter and firework menus) and b3f8fee
(the player data sync), with the review fix 426b002 (`UPSTREAM.md`, source changes 13 to 17). The
review found that one valid number packet still crashed the server through the Firework Box,
whose type chances and Value Play reached `Weight.of` and `Mth.nextInt` unchecked and were saved,
so the box now clamps every number on receipt and on load. It also limited the packet WARN lines
to 20 per sender in 60 seconds. The test mod `aadevtest` (source set `src/devtest`, not in the jar)
ran against the jar of 426b002 (SHA-1 `3855717cd50ea85d3e11971532235d93d5e3832f`) on a dedicated
Forge 47.4.18 server and on the pack's Arclight safeiter2. On both the 15 crafted packets and the
negative XP Solidifier button got one WARN each and changed nothing, the Firework Box kept `Play` 5
and `TypeChance0` 0.0 and fired, the drill and filter screens closed at the slot switch without a
dupe, every machine screen that opened closed 10 blocks away, the client got its player data, and
122 of 125 blocks and all 107 items ran with no ERROR line or crash on the server. On Arclight a
flood of 30 more packets gave 4 WARN lines and one suppression line. The Hopping Item Interface,
the Ranged Collector and the Advanced Item Laser Relay crash the base and wait for their ports, so
the harness skips them. The ASM scans of the review fix find client references only in the
lambdas that `DistExecutor` runs and no server call into a stripped member. Nobody has logged in
with a stock 1.1.52 client to see the protocol mismatch disconnect.

The assets bundle is done. Its five commits are d7357e9 (Retexture 2.0 and 2.1 and the removal of
the 1.12 leftovers), e6a4962 (the block shapes of `main`), 05f63d2 (directional crystal clusters),
94b6b3c (the lamps and the Lamp Controller) and 11ac001 (the block sounds), with the review fixes
e8f5b47 and 6c5aca4 (`UPSTREAM.md`, source changes 18 to 22). The bundle was written before the
server safety bundle landed, so its items moved from 13 to 17 up to 18 to 22 during the merge. The
Lava Factory Controller, Player Interface and Vertical Digger, whose shapes this bundle and whose
overlays the server safety bundle changed, keep the full cube of the first and the overlay classes
in `mod/blocks/blockhuds/` of the second. The first review fix makes a lamp toggled by hand change
for every player and drops the 1.12 `mcmod.info`. The second review found that the Lamp Controller,
which the data core made craftable, walked the connected lamps by recursion, one stack frame per
lamp, with a list that never held the checked offsets. A few thousand lamps on a redstone clock
would lag the server and could overflow the stack in a neighbour update. The walk is now breadth
first and stops after 4096 lamps. The same review found that a dye in the off hand took the item of
the selected hotbar slot, and it removed the unused creative tab background that `4ef880a` deleted
upstream. The main, devtest and test sources of 6c5aca4 compile, and one build gave the jar SHA-1
`24432d0c23eb8106cdfc7a0f24241edd90340952` (2959595 bytes, 2265 entries) with
`LICENSE_actuallyadditions` and `NOTICE_actuallyadditions` and without `mcmod.info`, the tab texture
or an `_old/` folder. No boot of this state is recorded, and nobody has run the client test of the
retexture yet. It cannot pass before the generators port gives the Bio Reactor a facing and with it
a blockstate (822913f0). The Greenhouse Glass rendered invisible until the Java part of 817797ad
came, which 707b2b7 of the world machines bundle brought (`UPSTREAM.md`, source changes 18 and 29).

The world machines bundle is done. Its seven commits are 6d88498 (the breakers, placers and the
Vertical Digger), 936c866 (the phantomfaces, the Player Interface and the boosters), e3b6d77 (the
Fluid Collector and the Fluid Placer), 806a7d1 (the Farmer), d58f5e7 (the Atomic Reconstructor and
the lenses), 28320f9 (the Display Stand and the Empowerer) and 707b2b7 (the Greenhouse Glass), with
the review fix dba4acd (`UPSTREAM.md`, source changes 23 to 29). The processing machines bundle,
written in parallel, numbered its source changes from 23 as well, and its merge moved them to
30 to 40. The review found that the phantomface kept handing out the handlers of a target
block entity that was gone. No block entity of the mod invalidates its own handlers, so once the
target was made anew at the same place, as after a chunk reload, an Energyface or a Liquiface
drained the removed copy while the live one kept its energy or fluid. The face now remembers the
block entity it forwards and drops the handlers when that block entity changes or is removed. The
review also found that the Phantom Breaker computed its drops at its own position with its own
block entity, as `main` still does, so a bound shulker box lost its contents. It now reads the
bound block, breaks through the harvest check and break event of the Breaker and leaves fluids
alone. The cheap notes of the review are in the same fix. The Long-Range Breaker no longer deletes
fluid sources, the Fluid Placer keeps a fluid without a block in its tank, the placer helper
`WorldUtil.useItemAtSide` checks the block in front with the fake player in its context and hands
back a copy of the stack, and the worm check reads the level of the tilling event (a65dccf9,
#1480).

The scripted test server of the bundle ran the mod alone on Forge 47.4.18 in a flat world (command
files `wmtools/cmds1.txt` to `cmds7.txt`, logs in `bp2/actuallyadditions/wm/` of the workflow
scratchpad). The Breaker broke stone, snow and a chest and kept water, the Long-Range Breaker broke
both blocks in its line, the miner lens made ores in stone, deepslate and netherrack
(`b2-latest.log`), and a new Vertical Digger started below its own height (`b4-latest.log`). In the
savegame test from the jar of the base to the jar of the bundle, a digger saved by the base started
once more from its own height and ran down to Y -64, a phantom itemface kept its binding and moved
items into its chest, and two Player Interfaces kept their players, one of them saved with the old
long pair (`sv-old-latest.log`, `sv-new1-latest.log`, `sv-new2-latest.log`). Every boot ended with 0
FATAL lines. The integration ran one more script (`bp2/actuallyadditions/wmint/int1.txt`) with the
jar of 707b2b7 and with the jar of dba4acd (SHA-1 `80c75636d809bde41710ec1cd7dfb4ecc5ae5bde`,
3015933 bytes, one build). An Energyface with two boosters fed a Long-Range Breaker from a Battery
Box 50 blocks away, and a `clone` made the box's block entity anew in one tick. With 707b2b7 the
receiver kept filling while the live box stayed at 100000 FE, and with dba4acd the live box lost
what the receiver got (`int-old-latest.log`, `int-new-latest.log`). The test makes the block entity
anew instead of waiting for the target chunk to unload, because the face asks for its target every
tick and so refreshes the one tick ticket of the chunk. That reading of the vanilla chunk code was
not checked in a log. A Phantom Breaker bound to a shulker box with five diamonds gave an empty box
with 707b2b7 and the box with the diamonds with dba4acd, and the Phantom Breaker and the Long-Range
Breaker kept a water source with dba4acd. The checks of the bundle's own script gave the same
results with both jars, and both boots logged only the ERROR line of the flat test world without
layers.

Nobody has opened a client with this state. The beam particle, the Greenhouse Glass on the cutout
layer, the overlays of the Farmer and the phantomfaces, the buttons of the miner and the Phantom
Placer and the JEI list of the mining lens wait for the client test of the release. The bundle also
ports fb80bf33 and 58b9cbc9, the laser render type and `AssetUtil.renderLaserParticle`, which the
energy and relays bundle ported as well, in the final form of `main` with db1bb6e7, 7fb62cb8,
de635bb7 and b3f87a6f. Its merge replaced the beam of this bundle with that form (see below and
`UPSTREAM.md`, source changes 27 and 44). No jar of this state may reach the live server before
the Arclight hardening, since the Phantom Breaker, the placers and the lenses still act as the
shared fake player `[Minecraft]`.

The processing machines bundle is done. Its eleven commits are af3d19b (the crushers), 493dc03
(the Powered Furnace), 08587ba (the Coal Generator fuels), cb66c85 (the Leaf-Eating Generator and
the Bio Reactor), 2efed8e (the Coffee Maker and its data driven ingredients), 291a711 (the Canola
Press and the Fermenting Barrel in JEI), 1320f71 (the Energizer and the Enervator), 681ca6f (the
Experience Solidifier), d683051 (the Firework Box screen), 2206218 (the Automatic Feeder) and
6ee0527 (the Lava Factory overlay), with the review fix 48a8724 (`UPSTREAM.md`, source changes 30
to 40). Both machine bundles dropped `(wip)` labels in the same lines of `en_us.json`, and the
merge keeps every label that either of them dropped. The review found that the crushing serializer
refused a recipe with no result or more than two with an `IllegalStateException`, which the recipe
manager of Forge 47 does not catch, so one such data pack recipe stopped a dedicated server at
boot. It now throws a `JsonSyntaxException`, which the recipe manager logs and skips, as `main`
does with the error of its result codec (bb84d539). The review also found that the Leaf-Eating
Generator, which the bundle turned on, and the Heat Collector removed blocks with no event. Both
now post the break event of the shared fake player first (see [Arclight notes](#arclight-notes)).
The cheap notes of the review are in the same fix. The generator collects only the leaves of its
cube, its area setting is a radius capped at 16, and coffee beans, tiny coal and tiny charcoal lose
their `(wip)` label as on `main`. The API list `ActuallyAdditionsAPI.COFFEE_MACHINE_INGREDIENTS`
now holds `CoffeeIngredientRecipe` objects, and `addCoffeeMachineIngredient` does nothing, both as
on `main` (971fdd65). No pack mod uses the API, see the property table.

The porter's test server ran the mod with Patchouli, Curios and JEI on Forge 47.4.18 by console
(`bp2/actuallyadditions/srv-pm-final/` in the workflow scratchpad, script `pm-tools/pmtest.sh`). It
reached `Done` with 0 FATAL and 0 ERROR lines. The Coal Generator burned a lava bucket, oak logs and
tiny coal, both crushers crushed, the Coffee Maker brewed with milk, and the Automatic Feeder gave a
comparator output (`srv-pm-final/console.log`). The integration ran three more boots with a test
data pack that holds a crushing recipe with three results, one without a result and one with two
(`bp2/actuallyadditions/pmint/`). With the porter's jar of the bundle before the merge and the
review fix (SHA-1 `929763b159614a8d910c215df42ac98a9b18fcd5`) the server stopped at boot with
"Failed to load datapacks, can't proceed with server load" (`old-console.log`). With the jar of
48a8724 (SHA-1 `b1548fd464ec23a10139bac799eff6c099a43bc7`, 3073698 bytes, one build) it reached
`Done`, logged the two parsing errors and crushed andesite with the recipe of two results. A
Leaf-Eating Generator in 405 persistent oak leaves made 5100 CF in about 5 seconds
(`new-latest.log`). With a test only mod that cancels every break event of a fake player it made 0
CF and every leaf stayed (`cancel-latest.log`, source in `pmint/canceller-src/`). The three
integration boots also logged the ERROR line of the flat test world without layers. The Heat
Collector path was compiled but not run, since it removes a block at random once in 200 tries after
1000 ticks. No boot of the bundle ran on Arclight. That AlmostUnified keeps the crushing outputs
vanilla rests on `minecraft` leading its `modPriorities` (`config/almostunified/unify.json:3`), and
no AlmostUnified boot checked it.

Nobody has opened a client with this state either. The crusher, coffee and pressing categories of
JEI, the fermenting arrow, the click areas, the firework sliders, the buttons of the Experience
Solidifier, the armor and off hand slots of the Energizer and the Enervator and the Bio Reactor
model in all four facings wait for the client test of the release.

The energy and relays bundle is done. Its four commits are ca8c52a (the laser relays), 43c2172 (the
item interfaces), 73db2b2 (the Battery Box and the energy items) and a7de6ee (the beam and item
particles and the laser render), with the review fix 01a811a (`UPSTREAM.md`, source changes 41 to
44). The porter based the bundle on an older tip, so the merge renumbered its source changes from 23
to 41 and kept both import sets of the JEI plugin. The world machines bundle had ported the beam
particle of fb80bf33 and 58b9cbc9 in a form whose render type only set the blending and whose
`AssetUtil.renderLaserParticle` began and ended the tesselator for each beam in the position, colour
and lightmap format. The energy and relays bundle ported the final form of `main`, where the render
type begins the buffer in the position and colour format and draws all beams at its end. The review
found that any mix of the two forms breaks the client at the first beam, since the buffer is then
either begun twice or written without a begin. The merge takes the final form in `ParticleBeam` and
`AssetUtil` whole, and source change 27 now points at source change 44. The review also found that
source change 42 claimed one blockstate variant for every facing of the Hopping Item Interface. The
blockstate keeps the single variant of `main`, so the model does not turn with the facing, as
upstream.

The porter's test ran a dedicated Forge 47.4.18 server with JEI, Patchouli, Curios, The One Probe
and Mekanism 10.4 and the dev client with the test mod under Xvfb, against the jar of the four
commits before the merge (SHA-1 `0bf32839`). Its results are in `UPSTREAM.md`, source changes 41 to
44. The integration repeated that test on the merged tree with the jar of 01a811a (SHA-1
`b9ddce0c51943455c941a755b8a7db23ae408b33`, 3079440 bytes, one build) and added a Leaf-Eating
Generator in 60 persistent oak leaves to the local scenario (`bp2/actuallyadditions/erint/` in the
workflow scratchpad, patch `devtest-energyrelays-int.patch`, script `er-tools/ertest.sh`). The
server reached `Done` with 0 ERROR lines and the 4 FATAL lines in which The One Probe reports its
new config file (`server-latest.log`). The client held up to 5 particles of the render type
`actuallyadditions:laser_particle`, 4 to 6 of `actuallyadditions:item_particle` and none in the
translucent sheet, and its log has no exception but the missing sound device and the Realms check of
the headless client (`client-latest.log`). The screenshots show the beam of the Atomic
Reconstructor, the beams of the Leaf-Eating Generator into the leaves, the energy, fluid and item
lasers and the items over the item relays (`screenshots/er-6-view5.png`, `er-3-view2.png` and
`er-10-items1.png`). The Mekanism cube ended with 451867.5 J, the input only fluid relay moved 1100
mB per sample at a `fluidLaserTransferRate` of 100, a plain compass click set the energy relay to
`OUTPUT_ONLY`, one plain and two crouching clicks left the item relay at priority minus 1, the
Hopping Item Interface moved 168 cobblestone from the chest above it to the chest east of it, and
JEI listed two entries each for the Single and the Quintuple Battery, the drill and the Teleport
Staff.

Five points of the bundle are not verified. They are the lasers and particles with Oculus shader
packs and Embeddium, the crouching compass click and the Hopping Item Interface next to a claim on
the pack's Arclight (see [Arclight notes](#arclight-notes)), a storage that accepts a simulated
energy transfer and refuses the real one (9fe3fa1c), and a caching cable that lets go of a battery
that leaves the Battery Box. The energy and fluid relays still build a new `LazyOptional` on every
capability call and never invalidate it, as the base did, while the Battery Box now invalidates its
own. All of these are in the carryover for the Arclight hardening and the release.

The filters and sacks bundle is done. Its five commits are a2b6cb4 (the sacks, the filters and the
buttons of the Advanced Item Laser Relay), 2c289eb (the Item Tag filter with the first two mixins),
0f90517 (the Ranged Collector), 0ecb2ca (the Crafting Table On A Stick with its key bind and Curios)
and 6ec115c (the porter's test record), with the review fix b3f3362 (`UPSTREAM.md`, source changes
46 to 49). The machine contents bundle landed first as source change 45, so the merge renumbered
this bundle's source changes from 45 to 48 up to 46 to 49 and kept both sets of `NOTICE` lines and
generated recipe hashes. The review found that the anvil handler of `main`
(`CommonEvents.java:233-247` on `main`) deletes the whole stack in the second slot of an anvil when
an Item Tag is renamed there, since the event keeps the material cost 0. The fork now acts only on
an empty second slot and clears a stale result at the head of `createResult`. The review fix also
raises the Curios range to `[5.2.0+1.20.1,)`, lets only a player with the open menu press the filter
buttons of the Ranged Collector and the Advanced Item Laser Relay, skips `forge:holds_items` in the
sack pickup and lets a sack in the main hand succeed on the client, see [Local
divergences](#local-divergences).

The porter's test ran the dev client with a local scenario on a dedicated Forge 47.4.18 server
against the jar of the four feature commits (SHA-1 `f81afeeb`), across one restart, and with the
server safety harness, which ran with nothing skipped. The integration added a further local
scenario (`bp2/actuallyadditions/fsint-tools/` in the workflow scratchpad) and ran it against the
jar of the review fix (SHA-1 `a022b6306c14157f59c6b9861ab5c5a82d135e9c`, 3126365 bytes) and, as a
control, against the porter's jar. With the review fix an Item Tag with 64 diamonds in the second
slot gave no result and kept the diamonds, a name without a tag after a valid one left the result
slot empty, two button packets without the menu left a collector's filter as it was, and the 16
snowballs in the off hand stayed when a sack opened in survival. The control lost all 64 diamonds
and let the packets switch the filter. The server log of both runs has no ERROR line. A shulker box
still went into a Traveler's Sack, because no jar of the pack fills `forge:holds_items`
(`UPSTREAM.md`, source change 46).

The serverbound handlers of the bundle take the indices 9 (`GUI_STRING_TO_CONTAINER_HANDLER`) and 10
(`HOTKEY_HANDLER`) of `DATA_HANDLERS` without a protocol bump, since no build with protocol 5 has
shipped. That order is fixed for the later bundles, which append after index 10. The tools bundle
added no handler (`git diff ababec7..693daa2` leaves the package `mod/network` unchanged). The
protocol stays 5 up to the first release, and any change of the handler list after that bumps it.

The tools bundle is done. Its four commits are 94af253 (the drills, their screen and the dye
recipes), ec4164c (the AIOT actions and the forge tool tags), 6839d1c (the Bowl of Water, the
Handheld Filler, the Teleport Staff and the worms) and a516398 (the Engineer's Infrared Goggles),
with the review fix 693daa2 (`UPSTREAM.md`, source changes 50 to 53). The merge onto the filters
and sacks bundle kept both sides of the item tag constants in `ActuallyTags`, of the lang lines of
the Item Filter and the Bowl of Water and of the generated recipe, item tag and loot table hashes.
The review found that the filler, which never filled on the base, became a duplication once it
worked. It placed the full saved state for one item, so a double slab, eight snow layers or mature
wheat came out of it for a single item and dropped their whole loot. The fork now saves and reads
the block in its default state with only its orientation (`mod/items/Filler.java`, `normalize`).
The review fix also gives the filler and the Bowl of Water the Forge block events, keeps the
Teleport Staff from jumping on a miss while the player looks up, and leaves the wearer out of the
goggles' outline, see [Local divergences](#local-divergences). The bundle removes the `WorldUtil`
helpers `getNearestPositionWithAir`, `getMovingObjectPosWithReachDistance` and
`getNearestBlockWithDefaultReachDistance`, so a later bundle traces with `Player#pick` and
`Util.getReachDistance` instead.

The integration booted the jar of the review fix (SHA-1
`eec659a2c452a4043ed1ad7c39e2d084f7394161`, 3151368 bytes) on a dedicated Forge 47.4.18 server next
to Patchouli, Curios, JEI and Mekanism with the porter's smoke script. The server reached `Done`
with 0 FATAL and 0 ERROR lines and no crash report, a worm summoned by command lived, and the loot
table `actuallyadditions:worm_drop` loaded and rolled (`bp2/actuallyadditions/tlint1.out` in the
workflow scratchpad). Nobody has tested the tools in game yet. The release runs those tests, among
them the drill areas with fortune and silk touch, the placer augment, the drill screen, the AIOT
actions, the bowl, the filler with its normalized state and the goggles on a dedicated server
(carryover `aa-ship`).

The pack does not ship the mod yet. Most new config keys have no reader yet, because their blocks
keep reading the 1.12 enums until their features land (`UPSTREAM.md`, source change 6).

| Bundle | What it covers | Hours | Needs | Status |
|---|---|---|---|---|
| Baseline (`aa-baseline`) | Optional dependencies and pack format 15, removal of the bundled InvTweaks annotations, Patchouli and Curios at compile time, the Mixin setup, the port script, the config of `main`, the datagen baseline and the boot smoke test | 16.5 | Nothing | Done in 4142c4b, 9788dd8 and 6bcbead (setup), 54fb6c3, dbbfe12, ef59d18, 8e0566f and 51031ce, review fix d3bd821 |
| Server safety (`aa-server-safety`) | Client classes out of common code, packet validation, menu validity with the drill upgrade dupe, player data sync | 18.5 | Baseline | Done in f9b373b, e2696b6, 0b3c5ff and b3f8fee, review fix 426b002 |
| Data core (`aa-data-core`) | Recipes, `forge:` tags, advancements and the JEI plugin shell | 21 | Baseline | Done in 2053e6a, 71370b7, 63a8eff and a98f81c, review fix fb2f22c |
| Assets (`aa-assets`) | Retexture 2.0 and 2.1, removal of the 1.12 asset leftovers, block shapes, directional crystal clusters, lamps, block sounds and the vanilla stairs, slab and wall tags | 20 | Baseline, data core | Done in d7357e9, e6a4962, 05f63d2, 94b6b3c and 11ac001, review fixes e8f5b47 and 6c5aca4 |
| Processing machines (`aa-processing-machines`) | Crushers, Powered Furnace, Coal Generator fuels, the other generators, Coffee Maker, Canola Press and Fermenting Barrel, Energizer and Enervator, Experience Solidifier, Firework Box, Automatic Feeder, Lava Factory | 33.5 | Server safety, data core, assets | Done in af3d19b, 493dc03, 08587ba, cb66c85, 2efed8e, 291a711, 1320f71, 681ca6f, d683051, 2206218 and 6ee0527, review fix 48a8724 |
| World machines (`aa-world-machines`) | Breakers, placers and the Vertical Digger, phantom blocks and boosters, Fluid Collector and Placer, Farmer, Atomic Reconstructor and lenses, Display Stand and Empowerer, Greenhouse Glass | 28.5 | Server safety, data core, assets | Done in 6d88498, 936c866, e3b6d77, 806a7d1, d58f5e7, 28320f9 and 707b2b7, review fix dba4acd |
| Energy and relays (`aa-energy-relays`) | Laser relays, item interfaces, Battery Box and energy items, laser and particle rendering | 13.5 | Server safety, data core, assets | Done in ca8c52a, 43c2172, 73db2b2 and a7de6ee, review fix 01a811a |
| Machine contents (`aa-machine-retain`) | Machines keep energy, fluids and redstone mode when broken, reset recipes, house machines that drop nothing, drops with the wrong tool | 7.5 | Processing machines, world machines, energy and relays | Done in fa08482 and 939981d, review fix 677bf9d |
| Filters and sacks (`aa-filters-sacks`) | Sacks and the Void Sack, filters with the advanced item relay fix, the Item Tag filter, Ranged Collector, Crafting Table On A Stick with a key bind and Curios | 21.5 | Server safety, data core, energy and relays | Done in a2b6cb4, 2c289eb, 0f90517, 0ecb2ca and 6ec115c, review fix b3f3362 |
| Tools (`aa-tools`) | Drills, AIOTs, small item fixes, Engineer's Goggles | 19.5 | Server safety, data core, energy and relays | Done in 94af253, ec4164c, 6839d1c and a516398, review fix 693daa2 |
| Arclight hardening (`aa-arclight-local`) | A named fake player with block events, a chunk guard for phantom blocks, an index of the relay networks, a throttle for the magnet ring | 15 | World machines, energy and relays, tools | Planned |
| World content (`aa-world-content`) | Crops, black quartz ore, wild crop patches, Engineer house and villager, loot modifiers, banner patterns | 25.5 | Data core, assets, machine contents | Planned |
| Manual and compat (`aa-ui-docs-compat`) | Manual on Patchouli, screen fixes, lang cleanup, Garden Cloche and Mekanism crusher recipes | 15 | Processing machines, world machines, energy and relays, machine contents, filters and sacks, tools, Arclight hardening, world content | Planned |
| Release (`aa-ship`) | Savegame test, pack wiring, licenses, README row and the final state of this page | 9 | Manual and compat, Arclight hardening | Planned |

The baseline comes first, then server safety and the data core. The assets wait for the data core,
because both edit the tag and recipe generators in `data/`. The three machine bundles (processing
machines, world machines, energy and relays) then run in parallel worktrees and are merged one at a
time. Machine contents waits for all three, because it edits the NBT handling of about fifteen of
their tiles. Filters and sacks wait for energy and relays, because their filter fix f46de7d9 also
changes the advanced item laser relay. The tools wait for energy and relays too, which rework the
energy items that the drills build on. The Arclight hardening waits for the tools, since it changes
the drill's area mining in `WorldUtil.breakExtraBlock`. World content waits for machine contents,
which handles the `StopDrop` key of the house machines. Manual and compat runs after every content
bundle so that the lang sweep comes last, and the release closes the plan (inventory,
`critic_notes`, Structure changes and Waves).

## Scope

The research for this backport rated the mod a good fit: a well known magitech mod with the Atomic
Reconstructor, the Empowerer, crystals, laser relays and canola oil power, rewritten upstream with
new models and textures. Its only library, Patchouli, and its optional Curios support are in the
pack (`bp2/discovery-result.json` in the workflow scratchpad, candidate `actually-additions`). Java
paths below are relative to the mod's root package folder in `src/main/java`, which holds `mod/`,
`data/` and `api/`, as in the inventory. Paths marked main are upstream's, all others the base's.

### Version

The fork keeps 1.1.52 as the base number and builds 1.1.52-skytekx.1. The code base is 1.1.52, and
`UPSTREAM.md` and `NOTICE` will state the content level as parity with upstream 1.3.26 (cd9d9c63)
except the exclusions. A base number of 1.3.26 would claim parity with a NeoForge 1.21.1 build that
has data components and 1.21 content, and it would point bug reports at the wrong changelog. The
alternative `1.3.26-skytekx.N` stays recorded in `UPSTREAM.md` (build change 4) for the release to
decide. Nothing in the pack depends on the version, see the property table.

### Baseline

- The setup stage already did the version, the reproducible jar and the license fields (9788dd8,
  6bcbead, `UPSTREAM.md` build changes 1 to 9).
- The file `mods.toml` gets optional dependencies on Patchouli (`[1.20.1-81,)`), Curios (`[5.0,)`,
  raised to `[5.2.0+1.20.1,)` by the filters and sacks bundle) and JEI (`[15.0,)`). Patchouli stays
  optional as upstream has it: the `neoforge.mods.toml` of `main` declares no Patchouli dependency,
  only the Modrinth publish block marks it required, and the manual warns in chat when Patchouli is
  missing (19cfbc92).
- The resource and data pack format becomes 15. The base still says 6 with a 1.16 comment
  (`src/main/resources/pack.mcmeta`).
- The folder `src/main/java/invtweaks/` goes, as 9bc38d2a does on 1.20.4. Its two annotation classes
  end up in the jar as the package `invtweaks.api.container`, their license is unchecked, and a
  second jar with the same package would stop Forge's module layer with a split package. The fully
  commented `ContainerGiantChest` that alone imports them goes too.
- Patchouli comes as the full jar at compile time, because the reconstructor page extends
  Patchouli's `PageSimpleProcessingRecipe` and uses `ClientBookRegistry`, which are not API (main
  `PatchouliPages.java:4`, `PageReconstructor.java:10-11`). Curios comes as its API jar.
- Mixins register through the manifest attribute `MixinConfigs`, the way
  `ports/enderio/build.gradle:342` does, because Forge 47 does not read the `[[mixins]]` block that
  `main` uses. The config targets Java 17 and Mixin 0.8.5. Its lists stay empty until the Item Tag
  filter and the goggles bring their mixins, so no refmap is written before that (dbbfe12).
- A port script in the scratchpad rewrites the mechanical NeoForge names, the 1.21 data folder
  names, the recipe keys and the `c:` tags of one upstream commit and applies it with
  `git apply --3way`. Generated files under `src/generated` are never patched from upstream.
- The common config of `main` (36 keys with its two Vertical Digger lists, ef59d18) replaces the
  base's 24 keys with upstream's camelCase names from the start, and a new client config holds
  `hideEnergyOverlay`. Values are read lazily, never in a static initialiser. The built in config
  screen of a831cbbd is NeoForge only.
- The data generators of the base first run untouched, and their diff to the committed
  `src/generated` (335 models, 120 blockstates, 109 recipes) stays in the scratchpad as the
  reference. Then the provider fixes land, among them item models for every item and the crushing
  recipe datagen of c1b23b52.
- The boot smoke test fixes only what stops a boot. Of the cited commits only f9897f06 applies:
  `mod/ActuallyAdditionsClient.java:97` registers `ClientRegistryHandler`, whose listeners take mod
  bus events, on the Forge bus. The fork deleted the class as upstream 2e394d9 did, since both
  listener bodies are commented out (51031ce). In the pack the InvTweaks removal was the one boot
  fix the base needed, see the status.

### Server safety

- Client classes leave common code (668f7ffe, 844efe42 and the I18n commits). On the base the HUD
  interface `IHudDisplay` takes `GuiGraphics` and `Minecraft`, the laser relay tiles call
  `I18n.get`, and `PacketHandler` holds client handlers. Upstream ties this to dedicated server
  crashes (#1434). The same class loading hits Arclight's dedicated server when the verifier meets a
  client type.
- Packet validation is a local fix, since upstream still trusts the client. On the base the four GUI
  packets take the dimension, the position and the player from the packet
  (`mod/network/PacketHandler.java:92-171`), so any client can press buttons on any machine as any
  player and load chunks. The handler list serves both directions (`PacketHandler.java:195-203`), so
  a client can also send the index of a client handler. The fix keeps the channel and its NBT
  format, uses only the sender, checks the dimension and a loaded position within 8 blocks, adds
  allow lists per direction and logs a WARN per refused packet, at most 20 per sender in 60
  seconds. It does not check that the menu of that machine is open, see
  [Arclight notes](#arclight-notes). The protocol goes from 4 to 5 once (`PacketHandler.java:175`).
- The Firework Box clamps every number to the slider ranges of `main` when it arrives and when the
  box loads. Upstream clamps nothing, and a type chance of minus 1 or a Value Play of 2e9 crashed
  the tick of the box again after every restart, since the box saves them (review of this bundle,
  `UPSTREAM.md`, source change 17).
- The drill screen dupes upgrades on the base. It writes them back into whatever hotbar slot is
  selected when it closes (`mod/inventory/ContainerDrill.java:130-136`, unchanged on `main`), and a
  client can change the slot by packet while the screen is open. The fix remembers the slot and the
  stack at open and closes the screen when either changes. The filter screen gets the same guard.
- The client never receives its player data on the base, because the clientbound handler reads the
  sender (`PacketHandler.java:134`), which is null on the client. The fix also stops writing the old
  manual keys `Bookmarks`, `Trials` and `DidTutorial` (c8e90aef) and still reads them.
- Later bundles build on the shape this bundle leaves. A new overlay such as the `FarmerHud` is a
  client only `IBlockHud` in `mod/blocks/blockhuds/`, and the `ClientEvents` constructor registers
  it with `BlockHuds.register`, because `IHudDisplay` is an empty marker without upstream's
  `getHud()`. The lava factory and phantom overlay fixes go into their existing HUD classes. New
  serverbound handlers such as the hotkey and the Item Tag go at the end of `PacketHandler.init`
  through `addServerbound`, clientbound ones through `addClientbound`, with no further protocol
  bump. The helper `PacketHandlerHelper` is split into the client only `PacketHelperClient` and
  `PacketHelperServer`, the laser relays return their display strings as Components, and the class
  `StringUtil` is client only.

### Data core

- Recipes grow from 109 generated files to the set of `main` (204), written in the 1.20.1 form by
  hand after the generator change. Conditions translate one to one to `forge:mod_loaded`,
  `forge:not` and `forge:tag_empty`.
- The base ships no `forge:` tag at all. The port adds the black quartz gem, ore and storage block
  tags, the twelve crystal storage blocks, the seeds and crops of rice, coffee, canola and flax,
  `forge:slimeballs` for the rice slimeball, the AIOT tool tags and the mod's own ore replaceable
  and lamp tags. The spec of `tags-forge` holds the full `c:` to `forge:` table for every tag that
  the data of `main` names.
- Advancements and the JEI plugin shell with the laser, mining lens and empowerer categories come
  here. The other categories land with their machines. EMI shows them through its JEI layer.
- The coffee branch of the advancement tree stays out of reach after this bundle. Coffee beans,
  canola, flax and rice have no source until the wild patches of the world content bundle land,
  because the plant generation of the base is commented out. So `pickup_coffee`,
  `craft_coffee_machine` and the recipes of the Coffee Maker, Canola Press, Fermenting Barrel and
  Oil Generator wait for that bundle (`UPSTREAM.md`, source change 11).

### Assets

- Retexture 2.0 (9cf9601e, 638 files) and 2.1 (65426282) with the crop, lens, crystal, battery,
  drill, AIOT and worm textures, the crate keeper model, mono sounds and the firework box texture.
  The changelog of 1.2.13 calls this new models and textures for almost every block and item.
- The 1.12 leftovers that ship in the jar and that no 1.20.1 loader reads go: `blockstates_old/`,
  `models_old/`, the asset folder `loot_tables/` and a 1.12 house structure with DataVersion 819.
- The block shapes of `main` with the rotation helper that upstream marks as taken from Mekanism
  (main `VoxelShapes.java:306-308`).
- Crystal clusters face the clicked side. The base already declares the property `facing` but always
  places them up (`mod/blocks/CrystalClusterBlock.java:32`, `58`).
- Lamps show their state, the lamp controller follows its rotation, and both get recipes. Casings,
  machines and crystals get their own sounds, and stairs, slabs and walls join the vanilla tags.
- Eight files of `main` stay out of the jar. The four banner pattern items come with the banner
  patterns (659779b1) and `item_tag.png` with the Item Tag filter (79b2084e), while `youtube.png`,
  `black_quartz_deepslate_ore.png` and the animation file of a deleted double crusher texture have
  no user on `main`. Three hand written models that `main` still carries go too, because nothing
  names them and their textures do not exist (`double_powered_furnace`, `green_block` and
  `white_block`, `UPSTREAM.md`, source change 18).
- Later bundles close the gaps this one leaves. The world machines bundle made the glass visible
  (707b2b7) and added the Fluid Placer to `minecraft:mineable/pickaxe` (07c59fb3, in e3b6d77). The
  generators port adds the Bio Reactor blockstate, and the AIOT fixes take the axe check of
  `AllInOneTool.useOn` from 6686f1f4, which the block shapes left out.

### Processing machines

- The crushers are finished with their recipes and the copper and deepslate ores (ef6e52f8,
  7c91da5a). The Powered Furnace becomes a JEI catalyst and lights up while it works.
- The Coal Generator burns anything with a furnace burn time (d5757a14, #1435), and tiny coal and
  tiny charcoal get one through `Item#getBurnTime`. The serializer `actuallyadditions:solid_fuel`
  stays registered so that third party datapacks still load.
- The Leaf Generator gets its config, the Bio Reactor a facing and a blockstate (822913f0), and the
  Heat Collector a recipe.
- The Coffee Maker reads its ingredients from the new recipe type
  `actuallyadditions:coffee_ingredient` (971fdd65) and gets the effect, water and automation fixes
  up to e6156bfc.
- The Canola Press, Fermenting Barrel, Energizer, Enervator, Experience Solidifier, Automatic Feeder
  and Lava Factory are fixed and get their recipes and JEI categories where upstream has them.
- The Firework Box gets its settings screen. A local clamp keeps every value in the slider range of
  `main` (main `GuiFireworkBox.java:48-61`), because the base stores any number a packet sends
  (`mod/tile/TileEntityFireworkBox.java:110-148`). The Experience Solidifier also refuses a negative
  button id (`mod/tile/TileEntityXPSolidifier.java:208`).

### World machines

- The Long-Range Breaker and the Vertical Digger mine again. On the base the drop helper always
  returns 0 (`mod/util/WorldUtil.java:293-305`). The Placer places again, and the Breaker breaks
  snow. The Phantom Breaker takes the drops of the bound block with that block's own block entity,
  and neither it nor the Long-Range Breaker deletes fluid blocks (review of the bundle, local).
- The Vertical Digger keeps its saved layer below Y 0. The base uses Y -1 as not started and 0 as
  done (`mod/tile/TileEntityVerticalDigger.java:57`, `105-121`). The port uses the minimum build
  height as in ae6f1603 and marks the new format with an additive key. It does not copy 738f098b,
  which restarts every digger from the top at each chunk load.
- The phantom blocks and boosters work and drop their capabilities when the target or its block
  entity changes. The Fluid Collector empties full cauldrons, and the Fluid Placer no longer wastes
  fluid or voids a fluid that has no block.
- The Farmer works with modded farmland, takes a compass for its area, handles nether wart, potatoes
  and carrots and leaves custom soil alone (b26c47dd, #1522).
- The Atomic Reconstructor no longer crashes its renderer or tooltip, takes a stack of lenses and
  gets the mining lens recipes for black quartz, ancient debris, deepslate and copper ores. The
  compat sets come for Mekanism and Immersive Engineering, which the pack has, and for Powah and
  ElementalCraft, whose ids were checked in their 1.20.1 Forge jars. Elements of Power and XyCraft
  wait for a 1.20.1 jar to check, and Modern Industrialization has only Fabric builds for 1.20.1.
- The emeradic empowering recipes ask for short grass, which is `minecraft:grass` on 1.20.1. The
  base has the same tall grass bug as upstream had before d8ad92c4. The Greenhouse Glass renders on
  the cutout layer without faces between two panes and has a recipe.

### Energy and relays

- Laser relays render right, fill Mekanism energy cubes that refuse a real transfer after a
  simulated one (9fe3fa1c), and the fluid relay rate comes from the config.
- The hopping item interface faces a direction again and moves items. The Battery Box renders and
  drops its capability on change, chargeable items show a bar, and JEI lists full and empty items as
  two entries.
- The laser and particle render types work with shader packs, on 1.20.1 through Oculus.

### Machine contents

- Generators, crushers, the display stand, the furnace, the reconstructor, the Coffee Maker and the
  Farmer keep their energy when broken, the fluid machines keep their fluids, and the redstone
  machines keep their mode (changelog 1.3.10). The 1.20.1 form is the loot function
  `minecraft:copy_nbt` into `BlockEntityTag`. Shapeless reset recipes clear an item.
- Machines in an Engineer house drop nothing (`StopDrop`, 74982c36). On 1.20.1 `playerWillDestroy`
  returns nothing, so the port suppresses the drops in `getDrops`. Blocks broken with the wrong tool
  still drop (632c3473).

### Filters and sacks

- The sacks pick items up, save their options and their filter, and the Void Sack gets its own menu
  and recipe. Filters match by damage and NBT, and the advanced item relay filters work (f46de7d9).
- The new item `actuallyadditions:item_tag` holds one item tag, set in its screen or by renaming it
  alone in an anvil at no cost, and every filter slot accepts it. Its string goes through the
  hardened packets, capped at 256 characters.
- The Ranged Collector picks up items again. The Crafting Table On A Stick opens with a key bind
  from the inventory or a Curios charm slot. Every Curios call sits in one compat class that runs
  only when Curios is loaded.

### Tools

- Drills get the range, attribute, highlight, placement, tooltip and dye fixes, and their screen
  refuses an upper augment without the lower one (75c3757e). The drill does not copy the cascade
  guard that `main` marks as taken from Apotheosis (main `DrillItem.java:257`). A thread local flag
  of its own keeps a nested break to its one block instead (`UPSTREAM.md`, source change 50).
- AIOTs till, flatten and strip and join the vanilla and the forge tool tags. Water bowl, filler,
  teleport staff, ring of growth and worms get their fixes, among them no crash when a fake player
  hoes grass (a65dccf9, which came with the review fix dba4acd). The filler keeps only the
  orientation of its saved block, and the staff keeps the base's rule for a miss (source change 52).
- The Engineer's Goggles render on the head, and the advanced ones outline the entities around the
  wearer for the wearer only through a client mixin (3b297993). The base set the glowing tag of the
  client copies from a client tick, which drew nothing (source change 53).

### Arclight hardening

All four parts are local, see [Arclight notes](#arclight-notes).

### World content

- Crops drop like on 1.12, harvest by right click, compost, and world generated plants survive a
  block update through a new block state property `persistent` (38d6404b). The Tiny Torch gets its
  fixes.
- Black quartz ore generates in stone and deepslate, size 6, count 8, between Y -25 and 45, behind
  the config switch `generateQuartz` (02422d65, ec4b79ff). The base generates nothing.
- Wild patches of canola, flax, coffee and rice generate with their own switches.
- The Engineer house joins five village house pools, and the Engineer villager works at an AA job
  block. The house is upstream's final structure with its DataVersion set to 3465 by a scratchpad
  script.
- Bats drop wings, and dungeon and other chests get crystals, drill cores, black quartz and wings,
  through global loot modifiers instead of the base's dead event handler.
- Four banner patterns come with their items and tags, with the textures under
  `assets/actuallyadditions/textures/entity/banner/` as 7245af2a moved them.

### Manual and compat

- The manual opens a Patchouli book with the category getting started and five entries, plus the
  reconstructor page type. The JSON is hand written from the generated files of `main`. Without
  Patchouli a chat line explains it.
- Screen fixes, the lang cleanup of the 131 entries marked work in progress, the Chinese translation
  and the removal of the eight 1.12 `.lang` files.
- Canola, coffee, flax and rice grow in the Garden Cloche of Immersive Engineering 10.2.0-183, and
  the Mekanism crusher turns a black quartz block into smooth black quartz, each behind a
  `forge:mod_loaded` condition.

### Release

- The savegame test, the jar in `local-mods/` and `mods/`, the permission record, the license text,
  the README row, the entry in [Mod set](mod-set.md), the carryover file and the final state of this
  page.

## Pack integration

- AlmostUnified 0.11.0 lists neither black quartz nor any crystal as a material
  (`config/almostunified/unify.json`, `materials` from line 39), so the new `forge:` tags unify
  nothing of the mod's own items.
- AlmostUnified does rewrite the compat outputs of the mining lens. Mekanism tin, lead and uranium
  ore and Immersive Engineering silver ore become the ores of the mod with the highest priority,
  Thermal where it has them. That is wanted. The crushing outputs stay vanilla because `minecraft`
  has the top priority.
- Tiny coal and tiny charcoal get no `forge:nuggets` tag, because AlmostUnified would unify them
  with the coal and charcoal materials.
- Farmer's Delight 1.3.4 also fills `forge:crops/rice`, so the mod's rice counts in its recipes,
  which is wanted. The flax seeds join `forge:crops/flax` as on `main`, and Supplementaries 3.1.44
  makes its rope, sack and doormat from that tag, so the seeds craft rope without growing flax. The
  owner can take them out of `forge:crops/flax` in `kubejs/server_scripts/tag_fixes.js` if that is
  unwanted (review of the data core bundle, not decided). Canola oil and the other three fluids
  have no `forge:` standard and keep the mod's own bucket tags.
- The new worldgen uses Forge biome modifiers on `#minecraft:is_overworld` and the `forge:` climate
  tags. The Skylands biome `skytekx3:sky_void` sits only in `#skytekx3:has_structure/sky_floaters`
  and two Dungeons Arise tags of the Skylands datapack. None of the tags the modifiers use holds it,
  and no village starts over the void, so no ore, patch or house lands there. The placed features
  keep the `minecraft:biome` filter of `main`. A pregenerated copy checked with `tools/void_scan.py`
  has to confirm it, see [Skylands worldgen](../content/skylands-worldgen.md).

## Excluded

| Upstream work | Reason |
|---|---|
| Port churn of NeoForge 20.4 (`neoforge-1204-port-churn`) | The port commit 63fbbb33 (227 files) and its follow ups only adapt APIs: capabilities, codecs, energy attachments, saved data and datagen helpers. Two of them, 9ec20f48 and f2596f97, are already in effect on the base. Each reverse mapping is read off 63fbbb33 when a feature needs it |
| Port churn of NeoForge 21.1 (`neoforge-121-port-churn`) | DeferredHolder registration, data components, payload networking and the like, from 23815b70 to d9b0a714. The fixes 8fd600dc and 758001e2 repair 1.21 regressions that the base does not have |
| New inventory format (`tile-inventory-format-change`) | Commit f40023cf writes machine inventories as one compound instead of the base's slot list. Porting it would empty every machine of an existing world |
| Structure updater (`structure-updater-blcs`) | The datagen helper `StructureUpdater.java` of 43a23883 carries a custom license whose permission does not reach this fork (`UPSTREAM.md`, License). A scratchpad script sets the DataVersion of the house instead |
| Storage crate (`crate-skeleton`) | Unfinished upstream content that upstream removed again (786d9d33, e9b5c7f8) |
| Enchantment rework of 1.21 (`enchantment-1-21-rework`) | The base already reads stored enchantment levels, and Forge 47 has no enchantment level event |
| Data driven systems of 1.21 (`data-driven-1-21-systems`) | Data maps, banner pattern JSON, the enchantment and armor material registries and `c:` tags. Their effects are ported by 1.20.1 means. The only newer vanilla id that `main` uses is short grass |
| Removal of unused items (`remove-unused-items`) | The items `dough` and `paper_cone` stay registered, since removing an id breaks existing worlds |
| Menu id rename (`menu-id-rename`) | The sack menu keeps the base id `bag_container` instead of `sack_container` |
| Removal of API and dead code (`api-and-dead-code-removal`) | No player effect. Only dead code that drags client classes into common code goes, in server safety |
| JEI 19 API (`jei-19-api`) | The pack runs JEI 15 |
| NeoForge config screen (`neoforge-config-screen`) | Commit a831cbbd uses a screen that Forge 47 lacks |
| Build, CI and release chores (`build-ci-and-release-chores`) | Gradle, CI, publishing, README, changelog and version bumps have no effect on the 1.20.1 jar |
| Code hygiene (`code-hygiene`) | Imports, logging, names and suppressions without player effect |
| Commits already on the base (`already-on-base`) | Commits 0e3fec76, 33f55411 and 226a4391 are in the snapshot |
| Datagen output sync (`generated-output-sync`) | The commits that only refresh `src/generated`. The fork writes its own 1.20.1 output |

## License conditions

- Code and assets are under the MIT License. Upstream commit 33f55411 replaced the old custom
  license on branch `1.20.1` on 2024-03-04, and 717199ea did the same on the newer lines, both
  citing upstream issue #1391. The same `LICENSE` with its 2019 copyright line sits on `main` at
  cd9d9c63 (`UPSTREAM.md`, License). The old per file headers that point to the former license are
  superseded and stay unchanged.
- The jar ships upstream's `LICENSE` as `LICENSE_actuallyadditions` and the SkyTekx `NOTICE` as
  `NOTICE_actuallyadditions` (6bcbead, build change 8). The `mods.toml` field `credits` marks the
  build as modified and unofficial, and bug reports go to the SkyTekx tracker (build change 9).
- The textures of the retexture came into the MIT repository after the relicensing (9cf9601e is
  dated 2024-04-03), and no separate asset license exists on either ref. The Modrinth project lists
  the artist on its team and declares MIT. Nobody has verified this with the artist.
- Excerpts that upstream marks as taken from other mods with permission: the experience formulas of
  Ender IO in `TileEntityXPSolidifier` stay with their credit. The banner helper from Additional
  Banners leaves with the banner patterns bundle, and its `NOTICE` line goes with it. The empty
  Tinkers' Construct excerpt left with `ClientRegistryHandler`, which the boot smoke test deleted
  (51031ce).
- The rotation helper in `VoxelShapes` is marked as taken from Mekanism, which is MIT. The comment
  stays, and `NOTICE` gets a Mekanism line with its copyright notice when the file lands.
- The snapshot already leaves out `RecipeHandler.java`, a commented copy of a class under CC
  BY-NC-ND 4.0 (`UPSTREAM.md`, Files left out). The baseline also deleted the fully commented
  `RecipeHelper.java`, whose header names a third party author and the MIT License without the
  notice text, and the InvTweaks annotations of unchecked license (54fb6c3).
- Patchouli, Curios and JEI are compile time dependencies only, and no class of them ships in the
  jar.
- The release adds a record in `modrinth-permissions/` (MIT, unofficial build, no 1.20.1 file on the
  Modrinth project, the project id checked against the Modrinth API) and the MIT text with its
  copyright line in `licenses/`, named after the jar like the other license files.

## Savegame notes

- No live world has the mod, so the savegame test is short. OLD is the base 6bcbead with its one
  boot fix, the InvTweaks removal, as the jar `237e0fc65275a0204fe7008f144c395207e41205`, because
  the vendored base build `c37140ce` does not start in the Arclight pack (see the status). NEW is
  the final build. The test places one of every block, fills machines, tanks, relay networks,
  phantom bindings, sacks, drills and batteries, stops over RCON, swaps the jar and checks that
  everything loads with its contents.
- No registry id, NBT key, block state value or saved data key of the base is renamed or removed.
  The items `dough` and `paper_cone`, the menu `bag_container` and the recipe serializer
  `actuallyadditions:solid_fuel` stay registered. The machine inventories keep the base's slot list.
- New block state properties load with their default: the Bio Reactor and the hopping item interface
  take their default facing, which is `down` for the interface, so it keeps pushing down
  (`UPSTREAM.md`, source change 42), and crops load with `persistent` false, the old behaviour.
  Crystal clusters keep their state, since the base already has the property.
- Additive NBT keys: the Vertical Digger gains the format marker `CheckYMin` and starts once more
  from its own height, the sack data gains `Filter`, every filter gains `Damage`, the Void Sack
  keeps `Filter` and `AutoInsert` on its stack, the Farmer gains its work area `Area`, and a broken
  machine item carries `BlockEntityTag` with only the kept keys. Firework Box values outside the
  slider ranges are clamped on load. Player data stops writing the old manual keys and still reads
  them.
- The drill keeps `Energy`, `Items` and the augment's `SlotToPlaceFrom`, the worm `Timer` and the
  Bowl of Water `lastX` and `lastY`. The Handheld Filler keeps its key `state` with the keys
  `First*`, `Second*` and `Curr*`, but reads a saved state back with only its orientation, so a
  filler that saved a double slab places a bottom slab (`UPSTREAM.md`, source change 52).
- New ids include the item `item_tag`, the menus `void_sack_container` and `item_tag_container`, the
  recipe type `coffee_ingredient`, four banner pattern items with their patterns, the loot modifier
  serializers `bat_loot` and `dungeon_loot`, the point of interest and profession `engineer`, the
  ore and patch features with their biome modifiers and the house pool. Each spec lists its own
  under New ids.
- A Double Crusher placed with the base jar saved a plain `actuallyadditions:crusher` block entity.
  It loads with its items, but the chunk does not tick it and logs one WARN "invalid for ticking",
  so it has to be broken and placed again (`UPSTREAM.md`, source change 30). The base gave the
  Double Crusher no recipe, so only a creative world can hold one. The Coal Generator no longer
  writes `currentRecipe` and ignores it on load, and the cup `coffee_cup` keeps its id and becomes a
  stack of one (`UPSTREAM.md`, source changes 32 and 34).
- Worldgen reaches new chunks only. The config files `actuallyadditions-common.toml` and, on the
  client, `actuallyadditions-client.toml` are new to the pack, so nothing needs a migration.

## Arclight notes

- Upstream breaks blocks as the shared fake player `[Minecraft]`, and on the base the Breaker posts
  no break event at all (`mod/tile/TileEntityBreaker.java:115`). The Phantom Breaker, the conversion
  lens and the mining lens change blocks without any event on `main` too. Since the world machines
  bundle, the Breaker, the Long-Range Breaker, the Vertical Digger and the Phantom Breaker post the
  break event of the shared fake player through the drop chance helper, while the lenses still post
  nothing (`UPSTREAM.md`, source change 23). Since the review fix 48a8724 of the processing machines
  bundle, the Leaf-Eating Generator and the Heat Collector post the same event through
  `WorldUtil.fireFakeBreakEvent` before they remove a leaf or a lava or magma block, and a refused
  block stays. The generator then tries up to 8 of its shuffled leaves in one cycle, so next to a
  claim it posts up to 8 events every 5 ticks (`mod/tile/TileEntityLeafGenerator.java`, lines 37 and
  96, and `UPSTREAM.md`, source change 33). The port adds one fake player per level with the fixed
  profile `[ActuallyAdditions]`, posts `BlockEvent.BreakEvent` and `EntityPlaceEvent` for every
  machine break and placement and reverts on a cancel. Arclight 1.0.6 turns both into the Bukkit
  `BlockBreakEvent` and `BlockPlaceEvent` with the fake player (javap by the review on the
  production jar `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar`, extract in the workflow
  scratchpad under `aacritic/arc/`).
- A claim plugin has to trust `[ActuallyAdditions]` for machines to work inside claims. The pack
  runs EssentialsX only. Fake players per owner would need a new key on every tile and go on the
  [open items](../open-items.md) at the release.
- A phantom block reads its target's block entity every tick before it checks the range (main
  `TileEntityPhantomface:141-170`), which loads the target chunk when the booster is gone. The port
  checks the range and `hasChunkAt` first.
- Every energy and fluid relay scans all networks and connections of the level in its tick
  (`mod/misc/apiimpl/LaserRelayConnectionHandler.java:71-120`, unchanged on `main`). A transient
  index by position replaces the scan, and the saved format stays.
- A crouching click with the relay configure item reaches a laser relay because a Forge listener of
  `PlayerInteractEvent.RightClickBlock` denies the item and allows the block
  (`mod/event/CommonEvents.java:49-54`, `UPSTREAM.md`, source change 41). Whether that result
  survives the Bukkit `PlayerInteractEvent` bridge in Arclight's `ServerPlayerGameMode.useItemOn` is
  not verified, since the energy and relays bundle ran on a plain Forge 47.4.18 server only. The
  Hopping Item Interface pulls from inventories and item entities without the Bukkit
  `InventoryMoveItemEvent` or `InventoryPickupItemEvent` that a vanilla hopper fires, as other
  modded item transport does (source change 42). Both go on the Arclight pack boot of the release
  (carryover).
- The magnet ring scans a 5 block box every tick for every ring. It scans every 4 ticks instead, and
  the pickup still goes through `ItemEntity.playerTouch`, so Bukkit pickup events fire.
- The anvil mixin injects at the head of `AnvilMenu.mayPickup` and at the head of
  `AnvilMenu.createResult`, and an accessor reads `ItemCombinerMenu.inputSlots` and `resultSlots`.
  Arclight's own anvil mixin redirects the `setItem` calls in `createResult`, injects at its
  `broadcastChanges` call and changes two constants, none of them at the head, so they do not
  collide (javap of `RepairContainerMixin` and `ItemCombinerMixin`, `UPSTREAM.md`, build change
  11). The result of a Forge `AnvilUpdateEvent` skips the Bukkit `PrepareAnvilEvent`, as every
  such result does. The goggles mixin is client only. No mixin uses `@Overwrite`.
- The sacks pick up items in Forge's `EntityItemPickupEvent`, which Arclight's `ItemEntityMixin`
  posts before the Bukkit `PlayerPickupItemEvent` and `EntityPickupItemEvent`, so a plugin that
  cancels those does not stop a sack (`UPSTREAM.md`, source change 46). The Ranged Collector takes
  item entities without the Bukkit `InventoryPickupItemEvent` and scans its area every unpowered
  tick (source change 48). Neither ran on the pack's Arclight yet (carryover).
- The drill's placer augment lets the hotbar stack place itself through `ItemStack#useOn`, so the
  Forge place events and Arclight's `BlockPlaceEvent` fire. That event names the drill as the item
  in hand. The drill's center block gets two break events, the one of
  `ServerPlayerGameMode.destroyBlock` and a second from `WorldUtil.breakExtraBlock`
  (`mod/util/WorldUtil.java:342`), and in creative the extra blocks get none. Both predate the
  bundle and go to the Arclight hardening (carryover `aa-arclight-local`).
- Since the review fix 693daa2 the Handheld Filler skips a position that spawn protection, the world
  border or adventure mode keeps from the player and posts the Forge place event for every block,
  and a cancelled place costs nothing and ends the job. The Bowl of Water posts the break event for
  the water it takes and for a block that its water washes away, and the place event for the
  water. On the base neither path was reachable, since the filler never filled and the bowl never
  picked up water. None of the tools ran on the pack's Arclight yet (carryover).
- The screen packets reach any machine within 8 blocks of the sender, whether or not its menu is
  open. Only the Ranged Collector and the Advanced Item Laser Relay check that the sender has their
  menu open since the review fix of the filters and sacks bundle. A claim plugin that cancels the
  right click never sees them, so a player could toggle machines in a foreign claim. The pack has no
  claim plugin or mod today. If one is added, the check in `PacketHandler.guiTarget` should also
  require that `player.containerMenu` belongs to that machine (review of the server safety bundle).

## Local divergences

These differ from upstream on purpose and must survive the next upstream sync.

- The version keeps the base number 1.1.52 plus `-skytekx.N`.
- The server validates every serverbound packet, see [Server safety](#server-safety).
- The drill and filter screens close when the selected slot changes, the Firework Box clamps its
  values, and the Experience Solidifier refuses negative button ids.
- The Vertical Digger marks its format with an additive key instead of resetting on every load.
- The Phantom Breaker takes the drops of the bound block with that block's own block entity. It,
  the Breaker and the Long-Range Breaker leave fluid blocks alone, the Fluid Placer keeps a fluid
  without a block in its tank, and the machine placer hands back a copy of the stack of the fake
  player. A phantomface drops the handlers of a target block entity that was replaced or removed,
  the Player Interface compares squared distances and so reaches 32 blocks as on 1.12, and only the
  Fluid Collector empties cauldrons (`UPSTREAM.md`, source changes 23 to 25). Upstream `main` takes
  the drops at the Phantom Breaker, lets its breakers delete fluid sources and gives the Player
  Interface a reach of about 5.7 blocks.
- The Hopping Item Interface leaves out the debug line `System.out.println(cap.getSlots())` that
  bd392b0a added and that `main` still runs for every item entity it picks up
  (`TileEntityItemInterfaceHopping.java:81` on `main`). A state without the facing falls back to
  `down` (`mod/tile/TileEntityItemInterfaceHopping.java:130`, `UPSTREAM.md`, source change 42).
- Machines break and place as the fake player `[ActuallyAdditions]` with block events, phantom
  blocks check the chunk first, relays use an index, and the magnet ring is throttled.
- The fork keeps the ids that `main` removed or renamed and the base's inventory format.
- Tags use `forge:` instead of `c:`, and tiny coal gets no nugget tag.
- The Lens of the Killer takes a `forge:partial_nbt` book with Sharpness V in `StoredEnchantments`,
  since the strict ingredient of the base and the 1.20.4 line matched no real book. The 3x3 slime
  block recipe takes rice slimeballs instead of every `forge:slimeballs` item, and the invisibility
  upgrade keeps `forge:glass/black` (`UPSTREAM.md`, source change 10).
- Patchouli stays optional, and the book is hand written without a Patchouli provider.
- Compat mining lens recipes ship only for mods whose 1.20.1 ids were checked.
- The Engineer house uses upstream's structure with a lowered DataVersion, without the structure
  updater.
- A lamp toggled by hand switches on the server only and sends the change to every client, a dye
  recolours a lamp from the hand that used it, and the Lamp Controller walks the connected lamps
  breadth first and stops after 4096 (`UPSTREAM.md`, source change 21). Upstream `main` keeps the
  client side toggle, the removal from the selected hotbar slot and the recursion.
- The loot tables of the crystal clusters use the 1.20.1 form of the silk touch predicate and
  otherwise match `main` (`UPSTREAM.md`, source change 20).
- Milk works again in the Coffee Maker. The two milk recipes carry the additive key
  `"milk": true`, and the effects that the slots before the milk added lose one amplifier and gain
  120 seconds, as the 1.12 `MilkIngredient` did. The JEI line
  `jei.actuallyadditions.coffee.extra.milk` reads "+02:00, -1 Level". On `main` milk does nothing
  and is never used up (`UPSTREAM.md`, source change 34).
- The input slot of the Energizer and the Enervator holds one item, and the output takes a copy of
  one. Copying the whole stack duplicated stacked energy items, and `main` still does that. Their
  armor slots ask `ItemStack.canEquip`, where `main` takes any armor item in any of the four slots
  (`UPSTREAM.md`, source change 36).
- Shift click into the crushers, the Coffee Maker and the Canola Press predicts the move with the
  recipes the client received, since only the server fills the recipe lists of the API. The screen
  factory of the crushers builds the screen of the Double Crusher for it, so its JEI click area
  applies (`UPSTREAM.md`, source changes 30, 34 and 35).
- The firework sliders send their value when the mouse lets go of them or a key moves them, not on
  every step of a drag, and an integer slider sends the whole number it shows (`UPSTREAM.md`,
  source change 38).
- The comparator output of the Automatic Feeder stays at 15 above the animal limit, and the menu of
  the Experience Solidifier uses the existing lang key `container.actuallyadditions.xpSolidifier`
  (`UPSTREAM.md`, source changes 37 and 39).
- The Leaf-Eating Generator and the Heat Collector post a break event before they remove a block,
  the generator collects only the leaves of its cube, and its area setting is a radius from 1 to 16
  with a comment that says so. Upstream `main` posts no event, collects every position of the cube
  and takes any area up to `Integer.MAX_VALUE` (`UPSTREAM.md`, source change 33).
- The Item Tag works in an anvil only with an empty second slot, and a name that names no tag
  leaves the anvil empty. On `main` the cost 0 of the result empties the second slot whatever it
  holds, and a stale result stays takeable. The Item Tag screen sets the tag, which `main`
  registers but never opens (`UPSTREAM.md`, source change 47).
- The sack menus are bound to the slot and the stack they opened with, a filter toggle marks the
  sack data or the relay for saving, the `SackManager` keeps one map per world, and the filter
  tooltips wrap at 200 pixels. The Traveler's Sack pickup skips `forge:holds_items` as the sack
  slots do, and a sack in the main hand succeeds on the client, so the off hand item is not used
  as well. The filter buttons of the Ranged Collector and the Advanced Item Laser Relay act only
  for a player with that block's menu open (`UPSTREAM.md`, source changes 46 and 48).
- The Crafting Table On A Stick calls Curios only through `CuriosCompat`, and the dependency asks
  for Curios 5.2.0 or newer (`UPSTREAM.md`, source change 49).
- The drill keeps `Item#onBlockStartBreak` with a thread local guard against nested breaks, its
  outline helpers live in the client class `ClientEvents`, and the keep data recipes copy the
  donor's energy. The AIOT strip check asks `consumesAction()`, and the AIOTs also join the forge
  tool tags. The id of the worm loot table lives in `LootTableUtil`, and a simulated tilling drops
  no worms (`UPSTREAM.md`, source changes 50 to 52).
- The Handheld Filler saves and reads its block in the default state with only its orientation,
  never waterlogged and with a double slab as a bottom slab, and it posts the Forge place event for
  every block. The Bowl of Water posts the break and place events. The Teleport Staff jumps on a
  miss only while the player looks no more than 5 degrees upward, as the manual says. Upstream
  `main` keeps the full state in the filler, posts no event for either item and lets the staff
  jump 100 blocks up on a miss (`UPSTREAM.md`, source change 52).
- The infrared goggles keep the entities in a set of their own instead of the glowing tag and
  leave the wearer out of it (`UPSTREAM.md`, source change 53).
- The jar leaves out eight textures and three dead block models of `main`, see
  [Assets](#assets).

## Operator notes

- Server and clients update together. The mod adds blocks and items, so a client without it cannot
  join, and the protocol bump refuses a client with another build.
- Mirror the jar by hand while the java process is gone: copy it from `local-mods/` into `mods/`.
  Then boot, and commit the new `config/actuallyadditions-common.toml` before
  `tools/build_mrpack.py`, which takes overrides from git HEAD. Edit that file only while the java
  process is gone.
- The ore, the crop patches and the Engineer houses only reach chunks that do not exist yet. The
  live world gets them in new areas only. Keep `/locate` sweeps off the live server.
- A boot is clean when it reaches `Done` with 0 FATAL lines, no crash report, no watchdog and only
  the two accepted ERROR lines, with no new ERROR line of the mod (see
  [Logs and crashes](../server/logs-and-crashes.md#boot-error-baseline)). Since the Item Tag filter
  brought the first mixins, the jar carries `actuallyadditions.refmap.json` and Mixin logs no WARN
  about it (`UPSTREAM.md`, build change 11).
- Announce the new mod to the players, with the manual that needs Patchouli and the key bind of the
  Crafting Table On A Stick.

## History

- The first draft of the inventory had 13 bundles and 261.5 h. The review moved the machine contents
  into their own bundle after the three machine bundles, moved the block sounds into the assets
  bundle before them, moved the advanced item relay filter fix into the filters bundle, cut the JEI
  feature to the plugin shell with three categories and set the dependencies of the Arclight, assets
  and manual bundles (inventory, `critic_notes`, Structure changes).
- The draft also planned to port f2596f97, 82aaacf1, 9ec20f48 and 758001e2, which the review found
  already in effect on the base or not needed.

Related: [Mod set](mod-set.md), [Twilight Forest backport](twilightforest-backport.md),
[Ender IO backport](enderio-backport.md), [AlmostUnified](almostunified.md),
[Arclight](../server/arclight.md), [Logs and crashes](../server/logs-and-crashes.md).
