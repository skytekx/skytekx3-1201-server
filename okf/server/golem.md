---
type: Plan
title: Golem, the Rust hybrid server
description: The plan for Golem, an Aosabi based Rust host that replaces Arclight, runs Forge 47.4.18 with every mod unchanged in an embedded JVM, adds a GraphQL API and ports EssentialsX, EssentialsXSpawn, LuckPerms and the Spigot world settings to Rust plugins.
tags: [server, golem, arclight, aosabi, rust, graphql, plugins, performance]
timestamp: 2026-11-06
---

# Golem, the Rust hybrid server

Golem replaces Arclight. It is a Rust program built on Aosabi that owns the server process, starts
the JVM inside itself and boots Minecraft 1.20.1 with Forge 47.4.18 and every mod of the pack
unchanged. Around the game it adds a GraphQL API, a plugin runtime in Rust and native code for the
hot paths of world generation. The Bukkit plugins the server uses today are ported to Golem plugins.

Nothing of this plan is built yet. Every duration on this page is an estimate that nobody measured.

## Owner requirements

These come from the session of 2026-11-06 (claude.ai session `016dYAZDqtsLdVqTQQoTV4RM`).

- More performance for the modded server, with multicore use where possible.
- The server is written in Rust on Aosabi and replaces Arclight as a hybrid.
- Every Forge mod that works on Forge keeps working.
- A GraphQL API in addition to plugins.
- Bukkit plugins stay possible but must run more efficiently, and dropping the Bukkit layer is the
  better choice if it is clearly faster.
- The plugins in use are ported, and the Skylands stay as they are, because the datapack generator is
  already optimal.
- The project is called Golem and lives in the repository `skytekx/golem`.
- Quirks and security issues of the ported plugins are fixed during the port, not carried over.
- Every quirk and every security issue gets its own GitHub issue, and the pull request links all of
  them.
- The owner asked whether Golem can be licensed under the GNU GPL 2, see [License](#license).
- Every commit is pushed to `skytekx/golem` as soon as it exists.
- Golem keeps its own OKF wiki.

## Why the game itself stays Java

Forge mods are not written against an API. They subclass vanilla classes, read their private fields
through access transformers and inject code into the middle of vanilla methods through Mixins. A scan
of all 216 server jars of `modrinth.index.json`, the 49 jars of `local-mods/`, the Forge 47.4.18 binary
patches and the Arclight jar against the Mojang mappings of the 1.20.1 server gave these numbers on
2026-11-06.

| Measured | Value |
|---|---|
| Top level vanilla classes in the 1.20.1 server | 3,568 (5,460 with inner classes) |
| Classes that Forge patches | 494 |
| Classes that Arclight patches | 507 |
| Classes that the mods patch with Mixins or access transformers | 855 |
| Classes that at least one of the three patches | 1,205 (34 percent) |
| Classes that nobody patches | 2,363 (66 percent) |
| Classes that Arclight alone patches | 164 |
| Mixin injection points into server side vanilla code | 2,569 |
| Mods that patch `ServerLevel`, `Entity`, `MinecraftServer` and `ChunkMap` | 26, 39, 15 and 11 |

A patched class must stay real Java bytecode with the exact fields and instructions the patches
target, or the mods fail to load. So Minecraft, Forge and the mods run unchanged inside a JVM that
Golem embeds. Golem moves to Rust what sits around the game, plus the unpatched hot spots.

The scan only sees declared patches. It misses Mixins that a mod switches off at runtime and access
through reflection. The scripts and results were kept in the session scratchpad and are not in the
repo yet.

## Architecture

```text
Golem process (Rust, an Aosabi service)
  GraphQL API          queries, mutations, subscriptions, auth, TLS
  Plugin runtime       Golem plugins in Rust: essentials, spawn, permissions, world settings
  Native library       noise and density functions, region file compression
  JVM host             starts and stops the JVM through the JNI invocation API
    Embedded JVM (Java 17)
      Minecraft 1.20.1 and Forge 47.4.18, unchanged
      Every mod of the pack, unchanged
      Golem bridge mod (Java): Forge events to Rust, actions from Rust on the server thread
```

The parts in detail.

- The JVM host starts Java 17 in the Golem process with the same arguments that Forge's
  `unix_args.txt` gives the stock launcher and calls `cpw.mods.bootstraplauncher.BootstrapLauncher` on
  a dedicated thread. On macOS the JVM must not run on the main thread. The host registers the native
  methods of the bridge mod itself, so no separate library has to be loaded.
- The stop path keeps the rule of [Arclight runtime](arclight.md): Golem never stops the server with a
  signal. It queues `save-all flush` and `stop` on the server thread and waits until the JVM has
  returned. Golem passes `-Xrs` so that the JVM leaves the signal handling to the host.
- The bridge mod is a small Forge mod. It listens on the Forge event bus for joins, leaves, chat,
  commands, deaths, respawns, block interaction and server ticks and hands them to Rust. It drains a
  queue of actions from Rust at the end of every server tick, so Rust never touches game state from
  another thread.
- The plugin runtime calls Golem plugins on the server thread for events that can be cancelled or
  changed, with a time budget per call that the API reports. Plugins that only observe use the
  subscriptions of the GraphQL API instead.
- The GraphQL API runs on Aosabi and replaces RCON for operators and tools. It binds to localhost and
  reaches the outside only through the Cloudflare Tunnel with authentication through
  `aosabi-auth`. RCON stays available during the switch.
- The native library holds the world generation work of [Native acceleration](#native-acceleration).

## Where the code lives

Golem lives in its own repository, `skytekx/golem`, as the owner decided on 2026-11-06. It names Aosabi
by path or git as long as the Aosabi crates are not on a registry. Aosabi promises no unsafe block in
its own code and puts embedded targets first
([Aosabi principles](https://github.com/netsnek/aosabi/blob/main/Documentation/principles.md)). A JVM
host needs unsafe JNI calls and only runs on servers, so it does not belong into the Aosabi core. The
bridge mod lives in the same repository as a Gradle subproject.

## License

The owner wants the GNU GPL 2. Golem takes `GPL-2.0-or-later`, not `GPL-2.0-only`, for these reasons.

- Aosabi is MIT, but some of its files are Apache-2.0, the code it takes from Pylon in
  `aosabi-core`, `aosabi-serve`, `aosabi-query` and `aosabi-build` ([Aosabi licensing](https://github.com/netsnek/aosabi/blob/main/Documentation/licensing.md)).
  Its TLS stack uses `ring`, which is under Apache-2.0 and ISC terms as well. The Free Software
  Foundation counts Apache-2.0 as compatible with the GPL 3 but not with the GPL 2 only. A Golem binary
  under `GPL-2.0-only` that links Aosabi would carry a license conflict.
- Under `GPL-2.0-or-later` the source stays GPL 2 and every combined binary is distributed under the
  GPL 3, which takes the Apache-2.0 parts.
- Forge is LGPL-2.1, which the bridge mod may link under either GPL version. LuckPerms is MIT. Neither
  blocks the choice.
- EssentialsX is GPL-3.0. Golem does not copy or translate its code. The port is written from the
  documented behaviour and from tests against the running plugin, so the GPL 3 of EssentialsX does
  not reach Golem. Arclight and CraftBukkit code is not used either.
- Golem never ships Minecraft. The server jar comes from Mojang's download as today.

Every source file carries `SPDX-License-Identifier: GPL-2.0-or-later` on its first line, and
`LICENSES/` holds the texts of the GPL 2 and of every license a dependency brings. A license check in
CI, such as `cargo deny`, refuses a dependency whose license is incompatible. This is a reading of the
licenses and not legal advice.

## Repository rules

These hold for everyone who works on `skytekx/golem`, human or agent.

- Push every commit as soon as it is made. No commit stays only local, not even for the length of a
  session, so that a lost machine or container never loses work. Work in progress goes to a branch and
  is pushed there, and only `main` waits for review.
- Keep commits granular, one concern per commit, so that pushing each one at once stays readable.
- Keep the OKF wiki in `okf/` of the Golem repository, in the same Open Knowledge Format as this wiki:
  front matter with `type`, `title`, `description`, `tags` and `timestamp`, an `index.md` with the
  state and a page map, a change log, a decisions page and open items. A change that alters behaviour,
  config, the API, a ported command or a plugin's data updates the matching wiki page in the same pull
  request. Every quirk and security issue of [Quirks and security](#quirks-and-security) gets a line on
  the wiki page of its plugin with the link to its issue.
- The Golem wiki holds the design and the operation of Golem itself. This wiki keeps the pages about
  the SkyTekx 3 server and links to the Golem wiki.

## Quirks and security

The port is a chance to fix what the plugins do wrong today. Golem does not copy a behaviour only
because EssentialsX, LuckPerms, Spigot or Arclight have it.

- A quirk is a behaviour that surprises players or operators, breaks a documented intent or needs a
  workaround. A security issue is anything that lets a player or an outsider get access, data, items,
  money or control they should not have, or take the server down.
- Every quirk and every security issue found while porting gets its own GitHub issue in
  `skytekx/golem` before the fix, with the label `quirk` or `security`. The issue states the old
  behaviour, with evidence such as a log line, a config line or a test against the running plugin,
  and the behaviour Golem has instead.
- Every pull request that ports or changes a plugin lists all issues it fixes in a section of its
  body, one link per issue with `Fixes #n`. A pull request without that section does not merge. A
  pull request that found no quirk and no security issue says so in that section.
- As long as the live server still runs EssentialsX and the repository is public, a security issue
  that is exploitable on the live server is filed as a private security advisory of the repository
  instead of a public issue. The pull request links the advisory in the same section, and the
  advisory is published after the switch.

These are known at the start. Each one gets its issue when the work begins.

| Area | Quirk or security issue today | Golem |
|---|---|---|
| Permissions | The option `use-bukkit-permissions: true` makes the `player-commands` list of Essentials do nothing, so the config says one thing and the server does another ([Plugins](../modpack/plugins.md#permissions)) | One permission system with one source of truth |
| Permissions | LuckPerms and its 39 nodes exist only in an H2 database on the Mac mini, so a new host has none ([Plugins](../modpack/plugins.md#open-points)) | Groups and nodes as a tracked config file that seeds the database |
| Homes | Without `home.bed`, `/home` of a player with no home ignores the bed and goes to the spawn ([Plugins](../modpack/plugins.md#permissions)) | The bed comes first by default |
| Data | The player data of Essentials sits in `plugins/`, which git does not ignore ([Plugins](../modpack/plugins.md#files)) | The Golem store lives outside the tracked tree and is ignored |
| Telemetry | bStats and the update check are on ([Plugins](../modpack/plugins.md#metrics-and-update-checks)) | No telemetry |
| Entities | Spigot's activation range left items hanging in the air until misc was set to 0 ([Floating items](arclight.md#floating-items)) | Items are exempt by design |
| Stop path | A signal stop and Arclight's shutdown thread could work on chunks at the same time ([Arclight runtime](arclight.md)) | Golem stops only through the server thread |
| Remote control | RCON sends its password in clear text and must never be exposed | The GraphQL API with authentication and TLS replaces it |

The port also checks these classes of issues and files what it finds: chat and name formatting codes
that fake system messages, spam through `tpa`, `msg` and `mail` without limits, money that goes
negative or rounds wrong in `pay` and `eco`, teleports into unsafe or unloaded places, and API calls
without rate limits.

## The GraphQL API

A first schema, to be refined while the bridge grows.

```graphql
type Query {
  server: Server!
  players: [Player!]!
  player(name: String!): Player
  dimensions: [Dimension!]!
  plugins: [Plugin!]!
  permissions(group: String!): [String!]!
}

type Mutation {
  runCommand(command: String!): CommandResult!
  teleport(player: String!, dimension: String!, x: Float!, y: Float!, z: Float!): Boolean!
  setGameMode(player: String!, mode: GameMode!): Boolean!
  kick(player: String!, reason: String): Boolean!
  setPermission(group: String!, node: String!, value: Boolean!): Boolean!
  saveAll: Boolean!
  stop: Boolean!
}

type Subscription {
  chat: ChatMessage!
  joins: Player!
  leaves: Player!
  deaths: DeathEvent!
  ticks: TickStats!
}
```

The type `Server` carries TPS and MSPT, the uptime, the Forge and mod versions and the time budget of every
plugin. Aosabi sends subscriptions as server sent events over HTTP, see the Aosabi Rust guide,
section 11.

## Bukkit plugins

The Bukkit plugins in use are ported, so Golem needs no Bukkit layer for this server. A Bukkit layer
stays an option for later. It has to be Java, because plugins are Java, and it would build Bukkit
events only for event types that a plugin listens to and create the Bukkit wrapper objects only when a
plugin asks for them. That is 6 to 12 months of work and only worth it if the owner wants other
Bukkit plugins.

Dropping Bukkit is likely to pay off more through the mods it allows than through Arclight's own
overhead. Arclight does not appear as a source in the worldgen profile of
[Performance](performance.md), but Radium crashed Arclight on 2026-06-27
([Performance](performance.md)). Without Arclight the server can try Radium or Canary and ServerCore
again. The comparison under [Verification](#verification) measures both effects before the switch.

## Plugin port

The plugins come from [Plugins](../modpack/plugins.md). The Golem plugins keep the command names and
aliases players know, so nobody has to learn new commands.

### EssentialsX 2.22.0

The default group on the Mac mini holds 39 EssentialsX nodes. Admin commands stay with operators.
Which admin commands operators really use is not known. The usage audit under
[Before the port](#before-the-port) answers that before the admin list is built.

| Area | Commands and nodes | Golem plugin |
|---|---|---|
| Teleport requests | Commands `tpa`, `tpahere`, `tpaccept`, `tpdeny`, `tpacancel`, `tpauto` | Plugin `golem-essentials` with a request table, a timeout and a cooldown |
| Return | Nodes `back` and `back.ondeath` | Last location per player, written on teleport and on death through the bridge |
| Homes | Commands `home`, `home.bed`, `sethome`, `sethome.multiple`, `delhome` | Homes per player with a limit per group. A player without a home goes to the bed and then to the spawn, as `spawn-if-no-home` does today |
| Warps | Commands `warp`, `warp.list`, admin `setwarp` and `delwarp` | Warps with an optional node per warp |
| Messages | Commands `msg`, `mail`, `mail.send`, `ignore`, `me`, `helpop` | Private messages, reply, offline mail, an ignore list. Ops get `helpop` messages |
| Info | Commands `list`, `seen`, `realname`, `motd`, `rules`, `help`, `ping`, `getpos`, `compass`, `depth` | Read only commands. The commands `motd` and `rules` read text files |
| Status | Command `afk` | AFK flag with automatic AFK after idle time and a mark in the player list |
| Other | Command `suicide` | Kills the player through the vanilla damage source so mods see a normal death |
| Economy | Commands `balance`, `balancetop`, `pay`, `worth`, admin `eco` | Accounts per player with the values of `worth.yml`, exposed through the GraphQL API |
| Signs | Node `signs.use.*` | Only built if the sign audit finds Essentials signs in the world |
| Admin | Command `gm` and its forms `gmc`, `gms`, `gma`, `gmsp`, and the others the audit finds | Thin wrappers over vanilla commands where vanilla has one |

The kits of `kits.yml` are the sample kits and no group may use them, so they are not ported. The
random teleport of `tpr.yml` is not in the default group and is only ported if the audit shows use.

### EssentialsXSpawn 2.22.0

The port is `spawn` and `setspawn` with the alias `espawn`. Both listeners are off today
(`respawn-listener-priority: none` and `spawn-join-listener-priority: none`), so respawns and first
joins stay vanilla and Golem adds no listener for them. Until an operator sets a spawn, `spawn` leads
to the world spawn of the Overworld.

### LuckPerms 5.5.71

The port is `golem-permissions` with groups, inheritance, nodes with wildcards, values per player and
a store in SQLite next to the world. It registers itself as the permission handler of Forge's
PermissionAPI, so mods that ask Forge for permissions see the same groups. Operators change groups
in game with `/perm`, over the GraphQL API, or during the switch over RCON.

### Telemetry

bStats, the MCStats leftover and the EssentialsX update check are not ported. That follows the owner's
no telemetry rule that [Plugins](../modpack/plugins.md#metrics-and-update-checks) names.

### Data migration

A migration tool in Golem reads the plugin data once with the server stopped and writes the Golem
store.

| Source on the Mac mini | Target |
|---|---|
| Files `plugins/Essentials/userdata/*.yml` with homes, last and logout location, balance, mail, ignore list, nickname | Players, homes, back locations, accounts, mail and ignore lists |
| Folder `plugins/Essentials/warps/` | Warps |
| The spawn of EssentialsXSpawn | Spawn |
| Files `plugins/Essentials/worth.yml`, `motd.txt` and the rules text | Golem config and text files |
| LuckPerms export from `lp export` | Groups, nodes and player values |

The tool prints counts per kind and fails on any entry it cannot place. The player data holds personal
data, so it stays out of git as today, see [Secrets](secrets.md).

## Spigot world settings

Arclight applies `spigot.yml` and `bukkit.yml`. Most values there equal vanilla and need no port. That
holds for the spawn limits and `ticks-per` of `bukkit.yml`, the growth modifiers, the hunger values,
the despawn rates, the hopper values and the structure seeds. These differ from vanilla and move into
the plugin `golem-world`.

| Setting | Value today | Port |
|---|---|---|
| Entity activation range | Animals 32, monsters 32, raiders 48, misc 0, inactive villagers tick | Bridge hook on entity ticks with the same ranges. Misc stays at 0 so that items never skip ticks, see [Floating items](arclight.md#floating-items) |
| Entity tracking range | Players 48, animals 48, monsters 48, misc 32, display 128, other 64 | Bridge hook on the tracking range per entity type |
| Merge radius | Items 0.5, experience 3.0 | Experience merge radius through the bridge. Items match vanilla |
| Mob spawn range | 6 chunks | Bridge hook on the natural spawn radius |
| Max tick time | 50 ms for block entities and entities | Ported only if the load test shows that it ever triggers |
| Max TNT per tick | 100 | Bridge hook |

Before the switch a pregenerated test region is compared between Arclight and Golem to prove that the
structure seeds of `spigot.yml` were the vanilla salts. If a structure moves, Golem sets the salts.

## Native acceleration

This part follows the hot spots of [Performance](performance.md). Chunk generation spends 50 to 62
percent of its time in vanilla code, most of it in terrain noise. The scan shows that no Forge patch,
no Arclight patch and no mod touches `SimplexNoise`, `ImprovedNoise`, `NormalNoise`, `Aquifer`, `Mth`
or `RegionFile`.

- Noise in Rust. The Java classes keep their exact signatures and call the Rust library in batches of
  a whole chunk. Results must match Java bit for bit, so the Rust code avoids fused multiply add and
  fast math. `PerlinNoise` is patched by Aether Redux and keeps its patched method in Java.
- Density functions compiled to Rust per dimension. Types that mods add, such as those of Draconic
  Evolution, stay Java callbacks. The Skylands datapack stays the single source and runs through the
  compiled path without changes.
- Region file compression in Rust with libdeflate, in the same format. `NbtIo` is patched by Forge and
  stays Java.
- Pathfinding in Rust only if the load test shows that it costs time. `PathFinder` is unpatched but
  reads world state from patched classes all the time, so it may not pay off.

C2MEF, the Forge port of C2ME, spreads chunk work over several cores and stacks with the native code.
It is tested in the measurement step. The expected gain of the native code is roughly 1.2 to 1.6 times
faster chunk generation per core. That number is a judgement, not a measurement.

## Verification

- A comparison with the same world copy, the same pregen and 20 simulated players: Arclight as today,
  plain Forge, plain Forge with Radium or Canary and ServerCore, and later Golem. Each run records
  Spark profiles, GC logs, MSPT and chunks per second.
- Golden samples for the plugin port. Scenarios such as `tpa` with timeout, `back` after death, homes
  across dimensions, mail to an offline player and `pay` run on Arclight with EssentialsX and on Golem,
  and the resulting positions, messages and stored data must match.
- Golden samples for the native code. A recorder logs inputs and outputs of the target methods during
  real generation in every dimension, and a pregenerated region must be identical block for block
  between vanilla and Golem.
- A clean Golem boot reaches `Done` with 0 FATAL lines and no crash report. The Essentials warning
  about the hybrid server leaves the boot error baseline of
  [Logs and crashes](logs-and-crashes.md#boot-error-baseline).

## Work plan

Two engineers. Durations in weeks of one engineer.

| Work | Content | Effort |
|---|---|---|
| Before the port | Load test setup, the four way comparison, command usage audit from the Mac mini logs, sign audit of the world | 2 to 3 |
| JVM host | Golem boots unchanged Forge with the full mod set and the Halloween world, stop path, logs, crash recovery, launchd and systemd units | 3 to 4 |
| Bridge mod | Events to Rust, action queue on the server thread, native method registration | 3 to 4 |
| GraphQL API | Schema above, auth, server sent events, the Cloudflare Tunnel route | 3 to 5 |
| Plugin runtime | Rust plugin trait, server thread calls with time budgets, config and SQLite store | 3 to 4 |
| Permissions | Plugin `golem-permissions`, Forge PermissionAPI handler, LuckPerms import | 3 to 4 |
| Essentials port | Plugin `golem-essentials` and the spawn commands, economy, migration tool, golden sample scenarios, the issues of [Quirks and security](#quirks-and-security) | 7 to 10 |
| World settings | Plugin `golem-world` with activation range, tracking range and the other hooks | 2 to 3 |
| Switch | Test copy run, migration on the Mac mini, launchers, docs in this wiki | 2 to 3 |
| Native acceleration | Golden sample recorder, noise, density functions, region files | 20 to 32 |

Golem without the native code is 28 to 40 weeks of work, about 4 to 5 months with two engineers. The
native code runs in parallel and ends about 8 to 10 months after the start.

### Before the port

- Grep the Mac mini logs for `issued server command` to list the Essentials commands players and
  operators really use.
- Scan the world's region files for signs whose first line is an Essentials sign tag such as `[Buy]`,
  `[Sell]`, `[Warp]` or `[Balance]`.
- Run the four way comparison above and record the numbers on [Performance](performance.md).

## Risks

- Forge and some mods behave differently when Arclight is missing. The comparison run catches this.
- Mods that ask Forge's PermissionAPI may expect nodes that `golem-permissions` must list first.
- A crash in the JVM takes the Golem process down with it. The launchd and systemd units restart the
  whole process, as they do today.
- The JNI boundary costs time per call. All native calls run in batches, and the golden sample run
  also measures speed.
- Golem stays bound to Minecraft 1.20.1 and Forge 47. A move to another version means new scans and a
  new bridge.

## Open decisions

- Whether the license is `GPL-2.0-or-later` as this page recommends, or another GPL version.
- Whether `skytekx/golem` is private until the switch, which would let security issues be public
  issues there from the start.
- Whether a Bukkit layer is ever wanted for plugins beyond the ported ones.
- Whether the economy of EssentialsX is in use at all, which the usage audit decides.

Related: [Arclight runtime](arclight.md), [Plugins](../modpack/plugins.md),
[Performance and worldgen profiling](performance.md), [Mac mini](macos.md),
[Logs and crashes](logs-and-crashes.md).
