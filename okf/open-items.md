---
type: Backlog
title: Open items
description: Everything that is still open on SkyTekx 3 on 2026-10-31, from owner rulings that were never built and choices only the owner can make to known defects, unverified claims and stale docs.
tags: [open-items, backlog, todo]
timestamp: 2026-10-31
---

# Open items

State on 2026-10-31 at 20:00, with the work of that day committed locally. Each item names its
evidence and the page with the details. The rulings behind the first section are on
[Decisions](decisions.md). The move to the Mac mini has its own open points on the
[Mac mini](server/macos.md) page.

## Rulings not yet built

- No mob explosion damage to blocks in the Skylands (ruling of 2026-06-29 11:40). No gamerule, script,
  datapack or config does this, and the vanilla `mobGriefing` rule is global. It needs a KubeJS
  explosion handler or a mod. See [Skylands worldgen](content/skylands-worldgen.md#open-rulings).
- Loot chances roughly like SkyTekx 2 and not three Hexxit flowers side by side (2026-06-27 21:46).
  Nothing overrides the Hexbiscus placement, and the loot part of `kubejs/server_scripts/gameplay.js`
  points at a script that was never written. See [KubeJS](modpack/kubejs.md#open-rulings).
- Chocobos only in the Skylands (2026-06-30 04:01). In the current world the shared Skylands pack loads
  at position 119, before the ChocoCraft mod pack, so its four overrides lose. A fresh world loads the
  file packs last (commit 1f8f4ef), which should fix it, but nobody checked in game. See
  [Skylands worldgen](content/skylands-worldgen.md#known-defect-the-chococraft-overrides-lose).
- Skylands travel in singleplayer (2026-06-29 19:33). The `.mrpack` puts the datapacks under
  `overrides/world/datapacks/`, which a singleplayer save never reads, and the fixes pack is not in it at
  all. See [Skylands travel](content/aether-fall.md#open-points).
- A public Modrinth project (2026-06-29 10:11). Six bundled jars are All Rights Reserved without a grant,
  the Nyx download fails, three server jars are in no pack, and the texts are stale. See
  [Publish](modpack/publish.md#what-blocks-a-release).
- A public server on skytekx.com (2026-06-29 09:54). The domain is proxied by Cloudflare, has no
  `_minecraft._tcp` SRV record and no DNS-only host, and port 25565 is closed. See
  [Deploy on Debian](server/deploy.md#dns).
- The old SkyTekx start menu (2026-06-27 20:49 to 21:49). FancyMenu, Konkrete and Melody are not in the
  index, the old buttons and texts were not rebuilt, and `menu-assets/` still waits for the original
  images. See [Client](modpack/client.md#main-menu).
- Real advancement names (2026-06-28 22:55). The resource pack `resourcepacks/SkyTekxFixes/` is
  untracked, not shipped and not enabled. See [Client](modpack/client.md#resource-packs).
- The 1.7 swing on every client (2026-06-29 12:10). The old animation needs Nostalgic Tweaks with its
  old swing setting, and the pack ships no config for it. See [Combat](content/combat.md#swing-animation).
- The recipe port from SkyTekx 2 (2026-06-27 10:34). `docs/RECIPE_PORT.md` still has VERIFY rows, and
  most of `kubejs/server_scripts/gameplay.js` is commented out. See [KubeJS](modpack/kubejs.md).
- No real names in the credits (2026-06-29 10:24). The `authors` line in the Nyx jar's `mods.toml` and
  lines 13 and 19 of `modrinth-permissions/nyx-1.20.1-1.0.0.jar.md` carry a real name. The jar needs a
  rebuild from the fork. See [Nyx](content/nyx.md#open-points).
- No dashes in commit messages (2026-06-29 22:14). Spaced hyphens used as dashes remain in the bodies of
  5ae7f82 and 573cc25. A fix means another history rewrite and force push.

## Owner decisions

- AsgardShieldReloaded 0.2 crashed the server twice when a creeper exploded next to a player
  (2026-06-27 20:43 and 21:16). Remove it, patch it or accept the risk. See
  [Combat](content/combat.md#known-crash-asgard-shield-reloaded).
- BrassAmber BattleTowers runs on the server but is in no pack, and in the install test of 2026-10-31
  a client without it could not join. It is All Rights Reserved and only on CurseForge. Ask its author
  for a grant, have players add it by hand, or drop it, which removes its blocks and items from an
  existing world. AllTheLeaks and the Cataclysm Spellbooks build are bundled since 507a8f2. See
  [Mod set](modpack/mod-set.md#jars-outside-the-index).
- Pack 3.0.6 ships JEI 15.74.0.233 from the update of 092140d. EMI 1.1.24 cannot bridge it, drops 7,243
  Chipped recipes, and in the install test the client ran out of its 8 GB heap after the join and
  froze. JEI 15.20.0.132 showed neither. Roll JEI back in the index and on the server, or wait for an
  EMI build that matches, then test again. See [Publish](modpack/publish.md#test-of-2026-10-31).
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
  Essentials update check is on, and Iron's Lib fetches Patreon data at runtime. Turning them off fits the owner's
  rule against telemetry. See [Plugins](modpack/plugins.md#metrics-and-update-checks).
- The Overworld also gets the Skylands structure sets. Villages at spacing 24 double the vanilla density
  there, and ten Dungeons Arise ground structures place on the island grid instead of their own spacing.
  Keep or gate. See [Performance](server/performance.md#side-effect-in-the-overworld).
- The performance levers: pregenerate a radius of 5000 or more per dimension and lower view and
  simulation distance to 8 and 6 (now 10 and 10 on both hosts). The Halloween world on the Mac mini is
  pregenerated to radius 1500 in the Overworld and 3000 in the Skylands. A radius of 5000 in the
  Overworld would need about 6.8 GB of the 9.4 GiB that were free there on 2026-10-31 at 21:05, so more
  disk comes first. See [Performance](server/performance.md#levers) and
  [Mac mini](server/macos.md#measured-numbers).
- Access before the address goes public. The whitelist is off with 39 slots and online mode on, and
  `advertiseDedicatedServerToLan` is true in `world/serverconfig/forge-server.toml:19`. See
  [Server](server/index.md#facts-at-a-glance).
- Rotate the exposed values. The Modrinth token and the vault password were pasted into a prompt on
  2026-06-27, and the Mac mini login on 2026-10-31 16:02. All three sit in plain text in the local prompt
  history of the build machine. See [Secrets](server/secrets.md#exposed-values-to-rotate).
- The two local backup branches `backup-pre-reconstruct` and `backup-pre-scrub` hold the history before
  the rewrites of 2026-06-29. Keep or delete them.

## Defects to fix

- The tracked world copies of the Skylands datapacks on the build machine are behind their sources
  since the commits of 2026-10-31 (1f8f4ef, 5ec6b72, 0cf350a and 7b51eb5). They belong to the old world,
  which stays as it was. The Halloween world on the Mac mini has current copies. Sync and commit them
  only if the old world comes back. See [World reset and pregen](server/world-reset.md#datapacks-and-pack-order).
- A new world does not get the fixes pack. The sync script writes only the two Skylands packs into
  `world/datapacks/`, and `macos/sync-to-mac.sh` skips `world/` unless it runs with `--with-world`. Copy
  `world/datapacks/skytekx3_fixes/` by hand, as was done for the Halloween world on the Mac mini. See
  [World reset and pregen](server/world-reset.md#fresh-world).
- The owner's Prism instance is behind the server. It still has the old builds of Polymorph, Balm,
  Waystones and Simple Voice Chat among others and needs the new jars before the next join. See
  [Client](modpack/client.md#mods).
- Spigot's crash restart points at `./start.sh` (`spigot.yml:20-21`), which does not exist.
- The ansible setup is a stub. `ansible/ansible.cfg` expects `.vault_pass.txt`, there is no inventory
  and `ansible/playbooks/` is empty, although `systemd/unit.conf.example` and `server.properties.example`
  say that ansible renders the RCON password. See [Secrets](server/secrets.md#the-ansible-vault).
- The systemd unit sets `KillMode=none` and stops through an asynchronous `mcrcon` call, so systemd
  neither waits for the save nor stops a JVM whose RCON stop failed. A synchronous stop is documented as
  an untested candidate. See [Deploy on Debian](server/deploy.md#stopping-and-restarting).
- A vanilla 1.20.1 race drops a queued chunk write when the same chunk is read back during it
  (`ConcurrentModificationException` in `IOWorker`, once per Skylands pregen on the Mac mini). Nothing
  was lost so far. A fix would copy the queued tag in `IOWorker.loadAsync`, in the hardened Arclight
  jar or in the compat mod. See
  [Logs and crashes](server/logs-and-crashes.md#chunk-save-race-during-pregen).
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

## Not verified

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

## Docs and housekeeping

- Stale texts outside the OKF. The file `docs/MODPACK_DESCRIPTION.md` and the body on Modrinth list
  Better Combat and Radium, say Twilight Forest is not bundled, credit a handle and say
  "kitchen-sink", and the project description on Modrinth says it too. The changelog of 3.0.6 and the
  summaries in the scripts are current since 8e11bbc and b3ec68c. See [Publish](modpack/publish.md#what-blocks-a-release).
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
