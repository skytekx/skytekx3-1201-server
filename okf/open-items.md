---
type: Backlog
title: Open items
description: Everything that is still open on SkyTekx 3 on 2026-10-31, from owner rulings that were never built and choices only the owner can make to known defects, unverified claims and stale docs.
tags: [open-items, backlog, todo]
timestamp: 2026-11-04
---

# Open items

State on 2026-10-31 at 20:00, with the work of that day committed locally. Each item names its
evidence and the page with the details. The rulings behind the first section are on
[Decisions](decisions.md). The move to the Mac mini has its own open points on the
[Mac mini](server/macos.md) page.

## Rulings not yet built

- Loot chances roughly like SkyTekx 2 (2026-06-27 21:46). The Hexxit flowers of the same ruling are
  done since 3f5eb2f2, see [boot fixes](content/boot-fixes.md#gameplay-overrides). The loot part of
  `kubejs/server_scripts/gameplay.js` points at a script that was never written. See
  [KubeJS](modpack/kubejs.md#open-rulings).
- Chocobos only in the Skylands (2026-06-30 04:01). In the old world the shared Skylands pack loaded
  at position 119, before the ChocoCraft mod pack, so its four overrides lost. A fresh world loads the
  file packs last (commit 1f8f4ef). The `level.dat` of the Halloween world of 2026-10-31 lists
  `mod:chococraft` at 214 and `file/skytekx3_skylands` at 274, so the overrides should win there, but
  nobody checked in game. See
  [Skylands worldgen](content/skylands-worldgen.md#known-defect-the-chococraft-overrides-lose).
- Skylands travel in singleplayer (2026-06-29 19:33). The `.mrpack` puts the datapacks, since 2883e32
  the fixes pack too, under `overrides/world/datapacks/`, which a singleplayer save never reads. See
  [Skylands travel](content/aether-fall.md#open-points).
- A public Modrinth project (2026-06-29 10:11). The draft holds versions up to 3.0.7, but six bundled
  jars are All Rights Reserved without a grant, and the EMI of those versions drops the Chipped recipes
  against their JEI build. The EMI fork in git since 2026-11-03 fixes that. See
  [Publish](modpack/publish.md#what-blocks-a-release).
- A public server on skytekx.com (2026-06-29 09:54). Since 2026-10-31 21:13 the game is reachable as
  `hex.skytekx.com` through a Cloudflare Tunnel, but only with the client mod Modflared, which pack
  3.0.7 brings since 21:55 and which has no cloudflared for Linux on ARM. Voice chat does not pass the
  tunnel, as the owner accepted at 21:48, and the website names neither the address nor Modflared. See
  [Mac mini](server/macos.md#public-access).
- The old SkyTekx start menu (2026-06-27 20:49 to 21:49). FancyMenu, Konkrete and Melody are not in the
  index, the old buttons and texts were not rebuilt, and `menu-assets/` still waits for the original
  images. See [Client](modpack/client.md#main-menu).
- Real advancement names (2026-06-28 22:55). The resource pack `resourcepacks/SkyTekxFixes/` is
  untracked, not shipped and not enabled. See [Client](modpack/client.md#resource-packs).
- The 1.7 swing on every client (2026-06-29 12:10). The old animation needs Nostalgic Tweaks with its
  old swing setting, and the pack ships no config for it. See [Combat](content/combat.md#swing-animation).
- The recipe port from SkyTekx 2 (2026-06-27 10:34). The port notes in `docs/RECIPE_PORT.md` still have
  VERIFY rows, and most of `kubejs/server_scripts/gameplay.js` is commented out. See
  [KubeJS](modpack/kubejs.md).
- No dashes in commit messages (2026-06-29 22:14). Spaced hyphens used as dashes remain in the bodies of
  5ae7f82 and 573cc25. A fix means another history rewrite and force push.

- Our own mod for molten metals (2026-11-04 14:33). The owner asked for a fluid for every material
  that makes an ingot and has none in Tinkers' Construct, such as palladium and yellorium. The mod
  SkyTekx Molten is being built. Clients need its fluids, so it goes live together with a pack update.
  The melting recipe of the Galosphere sterling horse armor waits for it, see
  [Boot fixes](content/boot-fixes.md#gameplay-overrides).

## Owner decisions

- Mob Grinding Utils 1.1.0 joined `local-mods/` on 2026-11-04 (ee238bea) and is All Rights Reserved,
  with no license in its GitHub repository. It blocks a public pack like the six older blockers, so a
  public pack needs the permission of vadis365 or Flanks255 or a pack without it. See
  [Mod set](modpack/mod-set.md#additions-of-2026-11-04).
- Packs 3.0.6 and 3.0.7 still hold Placeable Items, which the server dropped on 2026-11-01 (787cb1f).
  A client with the mod is refused at login, so every player disables it by hand until a pack 3.0.8
  without it goes up. See [Mod set](modpack/mod-set.md#history).
- Falling Meteors Reimagined 2.5 runs on the server since 2026-11-01 06:11 and is in no pack. A client
  without it cannot join, so every player adds the jar by hand, as with BattleTowers. Its `mods.toml`
  declares LGPL 2.1 or later and its CurseForge project All Rights Reserved, so whether a pack 3.0.8
  bundles it is the owner's choice. See [Mod set](modpack/mod-set.md#jars-outside-the-index).
- The Backpack port runs on the server since 2026-11-01 08:09 and is in no pack. A client without it
  cannot join, so until a pack 3.0.8 brings it every player adds `backpack-1.20.1-2.0.1.1.jar` by hand
  and removes Sophisticated Backpacks and Sophisticated Core. That pack has to bundle the jar through
  `local-mods/` together with `backpack-1.20.1-2.0.1.1-sources.zip`, since the LGPL asks for the
  source to come with the jar, and drop the two Sophisticated entries from the index. See
  [Mod set](modpack/mod-set.md#backpack).
- The ruling of 2026-11-01 05:52 keeps meteors off the sky islands, but the mod's Alien Creepers spawn
  through `data/meteors/forge/biome_modifier/add_alien_creeper_spawns.json` in every biome of
  `#minecraft:is_overworld`, and that tag holds the 14 island biomes of the Skylands. Keep them there,
  or override the modifier in a datapack so they spawn only in the main dimension. See
  [SkyTekx Compat](content/compat-mod.md#falling-meteors-only-in-the-overworld).
- Packs 3.0.6 and 3.0.7 ship JEI 15.74.0.233 from the update of 092140d with EMI 1.1.24, which cannot
  bridge it and drops 7,243 Chipped recipes. In the install test of 3.0.6 the client ran out of its
  8 GB heap after the join and froze, and with 12 GB it played. Since 2026-11-03 git ships the EMI fork
  instead (1ea5bb29, as 1.1.24-skytekx.2 since 2026-11-04), which implements the JEI 15.74 API, and
  the server and both MacBooks run it. A pack built from git still needs an install test, the heap
  included. See [Mod set](modpack/mod-set.md#bundled-jars) and
  [Publish](modpack/publish.md#test-of-2026-10-31).
- Six bundled jars are All Rights Reserved without a grant: Aether Genesis, Connectivity, Cupboard,
  Deadly World, Fireflies and Hexxit Additions. Ask for permission, replace or remove them before a
  public release. See [Publish](modpack/publish.md#what-blocks-a-release).
- The Halloween changes of 2026-10-31: Nyx lunar events in the Skylands with a blood moon chance of 0.5
  (a00ecce), pumpkin and jack o'lantern patches on the islands (5ec6b72) and the Halloween server icon
  (f92aee3). Keep them after Halloween or revert. See [Nyx](content/nyx.md#configuration).
- Twelve of the builds that came with the mod update of 2026-10-31 are betas, and Naturalist moved to a
  different version line. The ruling of 2026-06-27 asks for the most stable configuration. Keep them or
  pin the last releases. See [Mod set](modpack/mod-set.md#update-of-2026-10-31).
- The author address of the local commits of 2026-10-31. They carry `skytekx@erebos.xyz` from the
  repo's local git config, while the 40 commits before them use one other address. On 2026-06-29 18:38
  the owner had the address of the first commit changed. Settle the address before the push. See
  [Related repos and services](related.md#this-repo-on-github).
- Plugin telemetry and update checks. The bStats switch is on, PluginMetrics is not opted out, the
  Essentials update check is on, and Iron's Lib fetches Patreon data at runtime. The log audit of
  2026-11-04 found that Iron's Lib 2.3.0 also sends a server id, the player count and its version to
  code.redspace.io every 5 minutes, with no config switch, so only a jar patch or a firewall rule stops
  it. Turning them off fits
  the owner's rule against telemetry. See [Plugins](modpack/plugins.md#metrics-and-update-checks).
- The Overworld also gets the Skylands structure sets. Villages at spacing 24 double the vanilla density
  there, and ten Dungeons Arise ground structures place on the island grid instead of their own spacing.
  Keep or gate. See [Performance](server/performance.md#side-effect-in-the-overworld).
- The performance levers: pregenerate a radius of 5000 or more per dimension and lower view and
  simulation distance to 8 and 6 (now 10 and 10 on both hosts). The Halloween world on the Mac mini is
  pregenerated to radius 1500 in the Overworld and 3000 in the Skylands. A radius of 5000 in the
  Overworld would need about 6.8 GB of the 9.4 GiB that were free there on 2026-10-31 at 21:05, so more
  disk comes first. See [Performance](server/performance.md#levers) and
  [Mac mini](server/macos.md#measured-numbers).
- Rotate the exposed values. The Modrinth token and the vault password were pasted into a prompt on
  2026-06-27, and the Mac mini login on 2026-10-31 16:02. All three sit in plain text in the local prompt
  history of the build machine. See [Secrets](server/secrets.md#exposed-values-to-rotate).
- The two local backup branches `backup-pre-reconstruct` and `backup-pre-scrub` hold the history before
  the rewrites of 2026-06-29. Keep or delete them.
- Weak mobs gather in the Skylands. They do not burn there, because the fixed noon never counts as day,
  so 59 stood in the loaded part of the Skylands a minute after they came back on 2026-11-02. If that is
  too many, a KubeJS rule could let undead burn under open sky in the Skylands or thin out spawns there.
  See
  [Skylands worldgen](content/skylands-worldgen.md#sleep-and-hostile-mobs).
- One sleeper for everybody. Since 2026-11-02 14:10 the rule `playersSleepingPercentage` is 0, so one
  sleeping player turns night into day for the whole server. It was set to make sleeping in the Skylands
  work. The value 50 would ask for half of the players in a dimension instead.
- Saltpeter, niter and rods. Railcraft's saltpeter and Thermal's niter stay two items, and the rod tags
  that only Create Crafts & Additions and Thermal Extra fill have no preferred mod. Joining them needs a
  choice of the item that wins. See [AlmostUnified](modpack/almostunified.md#limits).

- Big pregens with players online. The out of memory crash of 2026-10-31 23:17 came from a Skylands
  pregen at radius 3000 with three players online, and every big pregen pushed the 9 GB heap above 90
  percent. Run pregens only with nobody online and in steps with a restart in between. The JVM flag
  `-XX:+ExitOnOutOfMemoryError` in `ServerMac.sh` would end a later out of memory at once, so players
  and chunks roll back together to the last autosave instead of only the players being saved.
- Drops on the Cloudflare tunnel. Players on the tunnel lost the connection 7 times between 2026-11-01
  22:25 and 2026-11-03 21:37, players in the LAN never. The connector log on the Mac mini has no error at
  those minutes, so the stall starts on the client side. A client `latest.log` after the next drop would
  show whether Modflared or a frozen game is the cause. A relay on a VPS or playit.gg is the other way.
- Hollow logs in the Skylands. Since 2026-11-04 the hollow oak logs of Hexxit Additions generate in the
  four overworld biomes of the mod only. The Skylands use their own biome ids, such as
  `skytekx3:forest`, and get none. Adding those ids to the biome modifier would bring the logs to the
  islands, see [Boot fixes](content/boot-fixes.md#gameplay-overrides).
- Fluids for the gems of other mods. Tinkers' Construct melts diamond and emerald into fluids of their
  own, but gems such as zanite and skyjade have none, so their gloves and tools cannot melt. Whether
  they get fluids too was asked on 2026-11-04 and is open.
- Recipes for the other uncraftable items of Extra Utilities Reborn Again. Version 1.2.0-rc10 registers
  17 more names without a recipe, among them the Resonator, which the four resonator recipes of the mod
  need, the Stone, Iron, Reinforced Large and Demonically Gargantuan Drums and the Halitosis Generator.
  Since 2026-11-04 only the Auto Executor has a recipe of the fixes pack, see
  [Boot fixes](content/boot-fixes.md#gameplay-overrides).
- The Auto Executor ignores hardness. It breaks any block it faces that is not air, bedrock and End
  portal frames included, because it calls `Level.destroyBlock` without a check. A guard in
  skytekx-compat would stop that, and whether it should was asked on 2026-11-04 and is open.

## Defects to fix

- The tracked world copies of the Skylands datapacks on the build machine are behind their sources
  since the commits of 2026-10-31 (1f8f4ef, 5ec6b72, 0cf350a and 7b51eb5). They belong to the old world,
  which stays as it was. The Halloween world on the Mac mini has current copies. Sync and commit them
  only if the old world comes back. See
  [World reset and pregen](server/world-reset.md#datapacks-and-pack-order).
- A new world on a host other than the Mac mini does not get the fixes pack. The script
  `tools/sync_skylands_datapack.sh` writes only the two Skylands packs into `world/datapacks/`. Since
  29927ae `macos/sync-to-mac.sh` mirrors the fixes pack into the Mac mini's world, elsewhere it is
  copied by hand. See [World reset and pregen](server/world-reset.md#fresh-world).
- The old Prism instance `SkyTekx3` is behind the server. It still has the old builds of Polymorph,
  Balm, Waystones and Simple Voice Chat among others. The owner plays in `SkyTekx3-test` since
  2026-10-31, so this matters only if the old instance is used again. See [Client](modpack/client.md#mods).
- The ansible setup is a stub. The config `ansible/ansible.cfg` expects `.vault_pass.txt`, there is no
  inventory and `ansible/playbooks/` is empty, although `systemd/unit.conf.example` and
  `server.properties.example` say that ansible renders the RCON password. See
  [Secrets](server/secrets.md#the-ansible-vault).
- The systemd unit sets `KillMode=none` and stops through an asynchronous `mcrcon` call, so systemd
  neither waits for the save nor stops a JVM whose RCON stop failed. A synchronous stop is documented as
  an untested candidate. See [Deploy on Debian](server/deploy.md#stopping-and-restarting).
- The legacy resolver `tools/resolve_all.py` ignores the pinned index and resolves a hard-coded slug list
  against the live API, Radium and Better Combat included. Nobody should run it. Remove or rewrite it.
  See [Tools](modpack/tools.md#the-legacy-resolver).
- Config drift. Nine tracked files under `config/` differ from git and five mod configs are untracked,
  all written by server boots between 2026-06-29 and 2026-07-12. Review them, then commit or restore.
- The Mac mini's configs differ from the checkout. A checksum dry run of `macos/sync-to-mac.sh` on
  2026-10-31 at 20:00 found 15 files under `config/` that a full sync would change on the Mac: those of
  AttributeFix, Enchanting Plus, Infernal Mobs, Livestock Overhaul, the ModernFix mixins, Packet Fixer,
  Sophisticated Core, Storage Drawers, Tinkers' Construct, TiCEX (two files), the Apotheosis names,
  Iron Chests and Simple Voice Chat, plus the old `collective.json5`, which Collective 8.72 replaced
  with `config/serilum/collective.json`. The mods updated that day rewrote several of them at their
  first boot on the Mac, and six are also among the drifted files of the item above. Review the Mac's
  versions and commit them, or make the sync skip configs. See
  [Mac mini](server/macos.md#deploy-and-update).
- The owner reported on 2026-11-01 at 04:25 that dropped items often need several passes to be picked
  up. Two suspects went on 2026-11-01 at 06:11, the parked network thread of Infernal Mobs' queries and
  the merge radius of 2 of Get It Together Drops. The main cause came to light on 2026-11-03: about one
  dropped item in four hung in the air on the server while the game showed it on the ground, and the
  fix of 3a671f08 is live since the restart at 11:13 that day. A merge still hands the freshest pickup
  delay to the pile. Only a new case, with its time and place, would show that something is left. See
  [Arclight](server/arclight.md#floating-items) and
  [SkyTekx Compat](content/compat-mod.md#infernal-mobs-queries).
- Four broken entities with an invalid position were dropped on 2026-11-03 while chunks near the
  origins of the overworld and the nether loaded ("Exception loading entity"). Vanilla throws this in
  `Entity.load` for a NaN coordinate, and which mod writes such entities is still open. Since
  skytekx-compat 2.1.6 a "Dropped entity" line names the next one with its type, chunk and raw
  position, see [SkyTekx Compat](content/compat-mod.md#entities-that-fail-to-load).
- Most tools and armor of other mods do not melt in the smeltery, because Tinkers' Construct ships
  melting recipes only for vanilla items and its own compat metals. Since 2026-11-04 the fixes pack
  melts horse armor and gloves, see [Boot fixes](content/boot-fixes.md#gameplay-overrides). A
  generator over all crafting and smithing recipes would cover the rest, with each piece giving back
  what its cheapest recipe takes, and it would also check the recipes of Tinkers for more dupes like
  the one of the vanilla horse armor.

## Not verified

- The nine mods of 2026-11-04 in play. A test server ran their blocks for half a minute, but nobody
  has opened their screens, built an ME network with the AE addons or used a Cyclic machine in a
  client. The deploy of 3.1.3 has to move the fixes pack to the end of the pack order once after the
  first start, see [Boot fixes](content/boot-fixes.md#load-order). See
  [Mod set](modpack/mod-set.md#additions-of-2026-11-04).
- Advanced Loot Info joined the index on 2026-11-04 after one boot of a test server, and no client has
  opened its tabs yet. The test of the next pack should open the loot tabs of a block, a mob and a
  chest in EMI on the server and note how much longer the join takes with the loot that the server
  sends, see [Mod set](modpack/mod-set.md#advanced-loot-info).
- The jar patches of 2026-11-04 in play. Nobody has opened the Simply Jetpacks 2 guide book in a
  client of a pack that ships `SimplyJetpacks2-1.20.1-2.11.0.1-skytekx.1.jar`, or updated a 3.1.2
  instance to such a pack in Prism Launcher or the Modrinth App. Nobody has cloned an enchanted book
  at a BiblioCraft printing table to see the levels it takes. The TiCEX patch was tested with the
  summoned swords of a fake player, not of a real player, and the heal of Necrotic was not seen. See
  [Boot fixes](content/boot-fixes.md#jar-patches).
- The Sundering mixin of skytekx-compat 2.1.6 never ran on a plain Forge dedicated server. On the test
  server a run with Arclight's mute redirect taken out let the redirect of Apothic Attributes apply as
  on plain Forge, and the share counted once. Run the golem test on a plain Forge dedicated server
  before a build with this mixin goes into the pack, see
  [SkyTekx Compat](content/compat-mod.md#sundering-on-arclight).
- The copy of queued chunk tags in skytekx-compat 2.1.6 passed a probe and a pregen of 441 chunks on
  the test server. The Mac mini hit the race twice in about 320,000 chunks, so only a big pregen there
  without "Failed to store chunk" confirms it, see
  [SkyTekx Compat](content/compat-mod.md#chunk-tags-waiting-for-their-write).
- The wither aura of the Withering Archer was tested on a zombie, not on a player, see
  [KubeJS](modpack/kubejs.md#apotheosis-riders).
- The melting recipes of 2026-11-04 in game. Nobody has melted a horse armor or a glove since they
  went live, see [Boot fixes](content/boot-fixes.md#gameplay-overrides).
- Modflared in a running game. Nobody has joined through it yet. The second MacBook is set up for it,
  but its sign-in and first launch are left to the person at that Mac. Where Modflared runs, and that
  on Linux on ARM it only logs an error and cannot join through the tunnel, was read from its
  bytecode. See [Mac mini](server/macos.md#public-access).
- The jar patches of `tools/patch_mods.py` were applied again to the new builds of Ad Astra, Deep Aether
  and TiCEX. The boots of the mod update on the Mac mini show the baseline of 0 FATAL and 3 ERROR lines,
  so the patched jars load, but nobody checked in game that each patch still does its job. See
  [Boot fixes](content/boot-fixes.md#jar-patches).
- The fixes pack replaces 6 loot tables and 3 advancements of mods that got new builds on 2026-10-31
  (DragNs Livestock Overhaul, Iron's Spellbooks and Naturalist). If the new builds fixed those files, the
  overrides now hide working loot. See [Boot fixes](content/boot-fixes.md#the-override-datapack).
- Skylands travel was not tested in game since the rework of 2026-07-12, and nothing softens the fall
  from Y 300 into the Overworld. See [Skylands travel](content/aether-fall.md#testing-status).
- The ChocoCraft overrides in a fresh world, see the first section.
- The vanilla and Spigot test boots of 2026-07-12 left no logs.
- Java 21. The only Java 21 boot failed on a missing `irons_lib`, and the same error hit Java 17 later
  that day. Java 21 was never retried, so the claim that Arclight crashes on Java 21 is unproven. Commit
  5b0f0d2 took it out of the Linux launcher and the systemd unit, and `ServerMac.sh` no longer makes it.
  See [Arclight runtime](server/arclight.md#java-17-only).
- The rebuild of the hardened Arclight jar from `patches/` was never repeated, and nobody checked whether
  a newer upstream Arclight 1.20.1 build fixes the shutdown race. See
  [Arclight runtime](server/arclight.md#rebuilding-it).
- The systemd unit never ran on any logged host.
- Load with 20 players. Only one player account ever joined, and no run had GC logging. See
  [Server](server/index.md#load-and-players).
- Whether Aikar's flags were active in the runs of 2026-07-12. Only the 11 GB heap is proven.
- The run of 2026-06-28 stopped logging at 23:50 and the run of 2026-06-30 ended on 2026-07-03 after a
  tick of 60.90 seconds, both without a crash report. See
  [Logs and crashes](server/logs-and-crashes.md#runs-that-ended-without-a-report).
- The three Spark reports return HTTP 404 on 2026-10-31, so the profile figures cannot be rechecked. See
  [Performance](server/performance.md).
- The AllTheLeaks memory figures were read as baseline and current use without a look at its source.
- The `/datapack` commands on [World reset and pregen](server/world-reset.md#fresh-world) that move a
  pack to the end are untested. A fresh world did not need them, since the Halloween world put the three
  file packs in the last three of 280 places by itself.
- The compat mod requires its Bird's Nests mixin config, but Bird's Nests is no declared dependency.
  A run without Bird's Nests was never tried. See [SkyTekx Compat](content/compat-mod.md#open-points).
- Whether packs 3.0.4 and 3.0.5 were ever uploaded to Modrinth. Checking needs the token.
- The Nostalgic Tweaks defaults for the old swing come from the owner's client config only.
- The structure at Overworld x 0, y 110, z 451 that the owner wanted in the Skylands was never
  identified.
- The SkyTekx logo's origin before 2026-06-28 18:12 is not recorded.
- Why Better Combat, Memory Leak Fix, Geophilic Backported, Platform and VanillaBackport were parked is
  not recorded, nor why the Forge pack overrides Botania's placed features (commit 0f78e9d).
- About 7,000 machine recipes hide their ingredients from the recipe probe of 2026-11-02, so whether
  they take every variant of a material was not checked. No chunk generated since 17:33 that day was
  checked for Mekanism tin and lead either, only the loaded config. See
  [AlmostUnified](modpack/almostunified.md#one-item-per-material).

## Docs and housekeeping

- Two notes still say Nyx is gone, `kubejs/server_scripts/recipe_fixes.js:28` and the Nyx rows of
  `docs/RECIPE_PORT.md`. See [Nyx](content/nyx.md#open-points).
- The GitHub repo description says "kitchen-sink" and points at a Modrinth project that is not public.
  See [Related repos and services](related.md#this-repo-on-github).
- The website still names the ts3.party community in two lead texts, counts different mod totals in
  its badge and its text, promises a public server and a working "Play on Modrinth" link, and its guide
  has a broken symlink step. See
  [Related repos and services](related.md#claims-on-the-site-that-are-wrong-on-2026-10-31).
- The compat mod source in `~/git/skytekx-worldgen` has no remote, and the old name skytekx-worldgen
  survives in some places. See [SkyTekx Compat](content/compat-mod.md#open-points).
- The commits of 2026-10-31 are not pushed (remote master 940f07e, checked on 2026-10-31 at 20:00), and the
  tracking ref `origin/master` is stale at bd5f18b. See
  [Related repos and services](related.md#this-repo-on-github).

Related: [OKF index](index.md), [Decisions](decisions.md), [Change log](log.md).
