---
type: Mod
title: SkyTekx Compat
description: The pack's own small Forge mod. The pack bundles skytekx-compat 2.1.3, the Mac mini runs 2.1.5, and 2.1.6 is built and tested for the server. It guards Bird's Nests against short loot table paths, keeps Skylands structures out of the void since 2026-10-31, answers Infernal Mobs queries on the server thread, keeps Falling Meteors in the overworld and protects Skylands blocks since 2026-11-01, and retries Mojang session checks since 2026-11-04. Version 2.1.6 adds the Sundering damage on Arclight, copies chunk tags that wait for their write and names entities that fail to load. It began as the skytekx-worldgen mod.
tags: [content, mod, compat, mixin]
timestamp: 2026-11-04
---

# SkyTekx Compat

SkyTekx Compat is the only Forge mod written for this pack. It began as skytekx-worldgen, which carried
the Skylands structure placement and the Skylands travel handler. Since the vanilla rework of
2026-07-12 it held one mixin that keeps Bird's Nests from breaking loot tables. Version 2.1.0 of
2026-10-31 added three worldgen mixins that keep the structures of the Skylands off the void, see
[Skylands void floor](#skylands-void-floor). Versions 2.1.1 and 2.1.2 of 2026-11-01 tightened the start
check after two reviews, and 2.1.2 added a fourth worldgen mixin for the columns that pieces fill
downward. Version 2.1.3 of 2026-11-01 added two mixins for other mods, see
[Infernal Mobs queries](#infernal-mobs-queries) and
[Falling Meteors only in the overworld](#falling-meteors-only-in-the-overworld). Version 2.1.4 of
2026-11-01 keeps Skylands blocks safe from mob griefing and explosions, on the server only, see
[Skylands worldgen](skylands-worldgen.md). Version 2.1.5 of 2026-11-04 asks Mojang again when a session
check fails, see [Mojang session retries](#mojang-session-retries). Version 2.1.6 of the same day fixes
three server bugs, see [Sundering on Arclight](#sundering-on-arclight),
[Chunk tags waiting for their write](#chunk-tags-waiting-for-their-write) and
[Entities that fail to load](#entities-that-fail-to-load).

| Property | Value |
|---|---|
| Jar | Bundled for the pack as [local-mods/skytekx-compat-2.1.3.jar](../../local-mods/skytekx-compat-2.1.3.jar), 17,325 bytes, SHA-256 `0059f9b31d295b86...`. The Mac mini runs 2.1.5, 32,137 bytes, SHA-256 `59c92a13ccd7fc22...`. Version 2.1.6 is `build/libs/skytekx-compat-2.1.6.jar` in the source repo, 43,442 bytes, SHA-256 `3f71a2f174e64be4...`, and is not live yet |
| Mod id | Still `skytekx`, display name "SkyTekx Compat", MIT, author "SkyTekx" |
| Needs | Forge 47 or later and Minecraft 1.20.1 up to 1.21. No declared dependency on Bird's Nests, Infernal Mobs or Falling Meteors |
| Source | Private GitHub repo `skytekx/skytekx-compat` as `origin`, cloned at `~/git/skytekx-worldgen`, branch `master`. Head 62e5616 is 2.1.6. Its four commits are not pushed yet, `origin/master` is 88b3759 |
| Build | Gradle 8.8, ForgeGradle 6.0, Parchment `2023.09.03-1.20.1`, MixinGradle 0.7, Mixin annotation processor 0.8.5, Java 17 (`~/jdk17/jdk-17.0.19+10`), Forge `1.20.1-47.4.6` |

The jar in `local-mods/` is byte identical to `build/libs/skytekx-compat-2.1.3.jar` in the source repo.
The Mac mini ran it from 2026-11-01 06:10 and 2.1.4 (18,383 bytes) from about 21:19 that day. It runs
2.1.5 since 2026-11-04 10:33. Before 06:10, from 02:51 it ran 2.1.2 (14,260 bytes, SHA-256
`c424dda546c40151...`), from 00:58 2.1.0 (12,330 bytes, SHA-256 `7eb835067e332cf1...`). Both now lie in
`~/skytekx3-backups/jars-orig/` there. Version 2.1.1 (12,818 bytes, SHA-256 `c1633905c56903ec...`) only ran on a test server. Packs 3.0.5 to 3.0.7 and the owner's Prism instance carry 2.0.0 (3,728 bytes, SHA-256
`16805d83d52c7c5f...`), which stays fine on the client, see [server list](#server-list). At boot
2.1.0 and later log "SkyTekx compat: birdsnests loot-table guard and Skylands void floor active".
Version 2.0.0 logged "SkyTekx compat: birdsnests loot-table guard active" (`logs/2026-07-12-1.log.gz:235`).

## The Bird's Nests guard

Bird's Nests 2.1.5 (`local-mods/birdsnests-1.20.1-2.1.5.jar`) listens to every loot table load in
`panda.birdsnests.DecayLeafEventHandler.onLootTableLoad`. It cuts the `blocks/` prefix off the table's
path with `substring(7)` without checking it. A table with a path shorter than 7 characters, for
example `minecraft:loot` or `apotheosis:book`, throws `StringIndexOutOfBoundsException`, and Forge then
drops that table ("Couldn't parse element"). The boot of 2026-06-30 03:33 logged 4 such exceptions
(`logs/2026-06-30-19.log.gz`).

The mixin `BirdsnestsDecayLeafFixMixin` injects at the head of that method (cancellable, `remap = false`
because the target is a mod method) and cancels it for every table whose path does not start with
`blocks/`. The handler only cares about leaf loot, so its own behaviour stays the same. The mixin config
`skytekx.mixins.json` is required and sets `defaultRequire` 1.

The fix lives in this mod and not in the Bird's Nests jar, so it survives a fresh download of Bird's
Nests. The Ad Astra table `data/minecraft/loot_tables/loot.json`, one of the short paths, is also
deleted by `tools/patch_mods.py` (see [boot fixes](boot-fixes.md)).

## Skylands void floor

Vanilla places structure parts at the first free height of a column. Over the void of the Skylands that
height is Y 0, the bottom of the world. After the June terrain came back on 2026-10-31, village streets
and houses, the merchant camps of Dungeons Arise and AE2 meteorites hung at Y 0 to 12 between the
islands. The camps brought rose bushes, the "flowers in the void" that the owner reported at 23:10 (see
[Skylands worldgen](skylands-worldgen.md#the-void-floor)). The island density closes at Y 12, so the
class `SkylandsFloor` treats a column whose first free height is below Y 13 as void. All four worldgen
mixins act only in the dimension `skytekx3:skylands`, which they recognize by the noise settings
`skytekx3:skylands` of its generator or by the dimension key.

| Mixin | Target | What it does |
|---|---|---|
| Jigsaw children, `JigsawPlacerSkylandsFloorMixin` | Method `JigsawPlacement$Placer.tryPlacingChildren`, redirect of both calls to `ChunkGenerator.getFirstFreeHeight` | A jigsaw child that is not a rigid piece on a rigid parent goes to the first free height under the parent's jigsaw block. Over the void the redirect answers 4096 blocks below the world, so every candidate lies outside the jigsaw bounds and the connector closes as it does at any dead end |
| Terrain matching, `GravityProcessorSkylandsFloorMixin` | Method `GravityProcessor.processBlock`, at the head | A terrain matching piece moves each of its blocks onto the heightmap of the block's own column. Over the void the block is dropped instead, so a street that overhangs an island edge ends at the edge |
| Structure starts, `StructureSkylandsFloorMixin` | Method `Structure.generate`, at the return | Cancels a start whose first piece reaches below Y 13 or whose center column is void, a scattered feature piece (swamp hut, desert pyramid, jungle temple) with a void column anywhere under its box, and an AE2 meteorite with a void column within 15 blocks of its chunk or with sky stone below Y 13. A structure whose own biomes include `skytekx3:sky_void` is left alone, since it floats over the void by design. These are the 22 floaters of `#skytekx3:has_structure/sky_floaters` and the Keep Kayra and Mechanical Nest of When Dungeons Arise |
| Column fills, `StructurePieceSkylandsFloorMixin` | Method `StructurePiece.fillColumnDown`, at the head | A piece fills such a column from a start block down to the first block that a structure may not replace, at the latest down to Y 2. The swamp hut builds its four stilts this way and the desert pyramid its base. Since 2.1.2 the column is filled only when the fill meets such a block at Y 13 or higher. Otherwise the whole column is left out, and the piece overhangs the void there |

AE2 15.4.12 puts a meteorite at the mean surface height within twice its radius (at most 15 blocks) of a
random point in its chunk. When the heights vary, it sinks the meteorite by three quarters of the way
down to the lowest column. A single void column counts as height 0, so a meteorite near an island edge
ended at Y 7 to 12 under the island. The rule keeps meteorites inside the islands. The Overworld keeps
all of its meteorites. The piece box of a meteorite is one block tall at its center, while
`MeteoritePlacer` fills sky stone from 8 blocks below the center to 8 above. Its decay pass then starts
9 blocks below the center and fills each replaceable block that has a block above it with a copy of
that block, so the lowest sky stone gets a copy one block further down. Since 2.1.2 the floor test
therefore counts a meteorite from 9 blocks below its box (2.1.1 counted 8). Few chunks of the Skylands are free of void for
15 blocks around, so meteorites stay rare there. Test 5 held 1 in 16,129 chunks, and the pregen to
radius 2000 on the Mac mini held 1 in 63,001.

A swamp hut, desert pyramid or jungle temple is one `ScatteredFeaturePiece`. When it is placed, the
swamp hut and the jungle temple sink to the mean height of the columns under the box and the desert
pyramid to the lowest one, which counts a void column as Y 0. The swamp hut then fills its four oak
log stilts down to the ground, over the void down to Y 2. Only the swamp hut can start on the islands,
the other two only through `/place structure`. Since 2.1.1 the start check reads every column under
the box of such a piece, 63 for a swamp hut and 441 for a desert pyramid, and cancels the start when
one of them is void. That check only sees the noise terrain. A stilt column that is empty under the
hut, below an overhang or where a carver cut the island, still reached Y 2, which
`StructurePieceSkylandsFloorMixin` of 2.1.2 stops when the hut is placed.

The exemption for `sky_void` reads `Structure.biomes()` since 2.1.1. Version 2.1.0 read the biome test
that the caller of `Structure.generate` passes. During the worldgen that is the structure's own
biomes, but `/place structure` passes a test that accepts every biome, so every placement by command
skipped all checks.

Before 2.1.0 went to the server, no other jar touched the same code: of the 236 jars in `mods/`,
Waystones mixes into `tryPlacingChildren` at `Registry.getHolder`, Structure Gel into
`Structure.generate` at its head and with a `@ModifyVariable` on the pieces builder, and no jar into
`GravityProcessor`. Arclight has no mixin on any of the three. The refmap maps the targets to
`m_227264_`, `m_223221_`, `m_7382_` and `m_226596_`. For 2.1.2 no jar mixes into
`StructurePiece.fillColumnDown` either. Blue Skies, Twilight Forest and DimDoors only call it from
their own pieces in their own dimensions. The refmap of 2.1.2 adds `m_73528_`.

## Infernal Mobs queries

The client of Infernal Mobs 1.20.1.11 sends a `MobModsPacket` query for every mob that joins its world,
to learn the mob's modifiers. The server half of `MobModsPacket.handle` ran straight on the Netty IO
thread and called `Level.getEntity` there. Arclight's async catcher (`defaultOperation=block`, see
[Arclight](../server/arclight.md#configuration-files)) turns such a call into a task for the server
thread, parks the IO thread until the task ran and logs `Async chunk entity get`. From 03:02 to 04:40
on 2026-11-01 the Mac mini logged 6,130 of these, all on one IO thread, in bursts of up to 42 within
about 0.2 s, and every stack dump in `debug.log` passes through `MobModsPacket.handle`. While the
thread waits, the connections on it read no packets.

The mixin `InfernalMobsQueryOnServerThreadMixin` hooks the head of `handle`. For a query (`sentFromServer` 0)
that arrives on the server outside the server thread, it schedules the original method with Forge's
`enqueueWork`, marks the packet handled and cancels. On the server thread the original body runs
unchanged and answers the client. Answers from the server and everything on the client keep the old
path. The handler belongs to the instance that registered the packet, so the mixin reads the private
`sentFromServer` of the decoded packet through a cast to the mixin class, which Mixin turns into a
cast to `MobModsPacket`.

On the test server at 06:05 the owner's client sent 61 queries. The `Server thread` handled all of
them, the client received the modifiers of an infernal mob, and no `Async chunk entity get` came up.
The stalls lasted at most about 0.2 s each, so they explain hiccups, but not on their own the reports
of items that need several passes to pick up. The merge radius is the other suspect, see
[Arclight](../server/arclight.md#configuration-files).

## Falling Meteors only in the overworld

Falling Meteors Reimagined 2.5 decides per level in the static
`MeteorManager.isDimensionWhitelisted(ServerLevel)` whether its climate timer queues natural meteors
and comets there and whether a thrown Meteor Summoner may call one. With the default
`whitelistedDimensions = [0, 1, 7]` it accepts the overworld, the End and every dimension whose type
is natural and has sky light, which includes the Skylands. The owner ruled on 2026-11-01 at 05:52
that meteors fall only in the main dimension. The mixin `FallingMeteorsOverworldOnlyMixin` returns true for
`minecraft:overworld` and false everywhere else. A summoner thrown elsewhere reports the wrong
dimension and calls nothing. The op commands `/meteor`, `/comet` and `/kittyattack` do not ask the
method and work everywhere. Alien Creepers still spawn through the mod's biome modifier in every
biome of `#minecraft:is_overworld`, and that tag holds the 14 island biomes of the Skylands.

On the test server at 06:02 a summoner projectile dropped onto a stone block called a falling meteor
in the overworld and nothing in the Skylands, the Nether or the End. The End proves the mixin, since
the default list allows it. A natural meteor from `/meteor` then crashed at X 97 Z 11 next to the
owner's client and left 166 meteor and 8 rare meteor blocks, with no error in the log.

## Server list

Forge compares the version of each mod in the server list ping. Packs 3.0.5 to 3.0.7 carry 2.0.0 with
the default display test `MATCH_VERSION`, which marks a server as incompatible unless the server reports
exactly "2.0.0" for the mod id `skytekx`. Versions 2.1.0 to 2.1.2 therefore report "2.0.0" in the
ping and accept any server version on the client (`SkytekxWorldgen.SERVER_LIST_VERSION`), and so do
2.1.3 to 2.1.6. Every fix up to 2.1.4 acts on the server, so a client never needs them. Only the join
retry of 2.1.5 runs in the client. Version 2.1.6 changes the server only, because the pack keeps 2.1.3
by the ruling of 2026-11-04 11:53. It adds no network channel, no registry entry and no class that a
client needs, and its four new mixins sit in the "server" list. The server list ping of the test server
listed the same 277 mods and 180 channels with 2.1.5 and with 2.1.6, in both with "2.0.0" for
`skytekx` and no channel of it.

## Mojang session retries

On 2026-11-03 from 20:13 to at least 20:41 Mojang's session server answered about half of all requests
with HTTP 503. The server turned away 7 of 11 logins in that time with `Failed to verify username!`, and
clients stopped with "Failed to log in: null" before they reached the server, see the
[log](../log.md). A second request a moment later would usually have worked. Version 2.1.5 asks again.

| Side | Mixin | What it does |
|---|---|---|
| Server | The mixin `MinecraftServerSessionRetryMixin`, at the return of `MinecraftServer.getSessionService` | Wraps the session service for "User Authenticator" threads only. When `hasJoinedServer` answers null or throws `AuthenticationUnavailableException`, it asks up to four more times after pauses of 0.5, 1, 2 and 3 s, and it starts no new try 12 s after the first one |
| Client | The mixin `ClientHandshakeJoinRetryMixin`, at the return of `ClientHandshakePacketListenerImpl.getMinecraftSessionService` | Wraps the service off the render thread only. When `joinServer` throws `AuthenticationUnavailableException`, or a plain `AuthenticationException` without a message or with a 5xx status, it posts again up to three times within 6 s. Invalid credentials, bans and missing privileges are never retried |

Nothing is cached and there is no offline fallback, so a player only gets in with a profile that Mojang
returned for this very login. The owner asked on 2026-11-04 whether the check could be cached, so that
outages no longer keep players out. A cache was turned down on purpose: every player who comes through
the Cloudflare tunnel arrives from the loopback address, so the server cannot tell those players apart by
address, and a cached name would let anyone with a cracked client join under it during an outage. A login
that Mojang really refuses ends as before, about 7 s later. When Mojang is down completely, no retry
helps, but players who are already online never notice, because the server only asks Mojang at login.

The server mixin sits in the "server" list of `skytekx.mixins.json` and the client mixin in the
"client" list, so a dedicated server never loads the client one. Anything unexpected inside a wrapper
keeps the first answer. During an outage the server logs "Mojang did not confirm the session of
'<name>' (...), retry N of 4 in M ms" and "Mojang confirmed the session of '<name>' on retry N, X ms
after the first try".

The server part runs on the Mac mini since 2026-11-04 10:33, and its boot log shows "Mixing
MinecraftServerSessionRetryMixin from skytekx.mixins.json into net.minecraft.server.MinecraftServer".
The client part only reaches players whose pack carries 2.1.5, see [open points](#open-points).

The command `./gradlew build --offline` runs `SessionRetryTest` (24 cases, 103 checks) as part of the
task `check`. An end to end test on a scratch Arclight 1.0.6 server with a fake session host accepted
logins after one or two 503 answers and turned away a cracked client after five tries, 6.5 s after the
first. The client mixin was only checked in the bytecode and the refmap and has not run in a real client
yet.

## Sundering on Arclight

Sundering (`attributeslib:sundering`) is the opposite of Resistance in Apothic Attributes 1.3.7. Each
level adds 20 percent of the damage. On plain Forge the mod adds that share in the Resistance step of
`LivingEntity.getDamageAfterMagicAbsorb`. Its redirect `apoth_sunderingHasEffect` (priority 1000)
answers true, so the step runs with or without Resistance, and its redirect of the `Math.max` in the
step adds the share. Arclight 1.0.6 applies Resistance in a damage modifier of its own, and its
`ApotheosisCompatMixin` redirects the same `hasEffect` call with `arclight$mutePotion` (priority 1500),
which always answers false. Mixin keeps the redirect with the higher priority and skips the other one
("@Redirect conflict. Skipping" in `debug.log`), so the step never runs on the server and Sundering added
no damage, see [Arclight](../server/arclight.md).

The mixin `LivingEntitySunderingMixin` of 2.1.6 notes whether the Resistance step of the running call
reached its `getEffect` call. Only when it did not, it adds 20 percent per Sundering level right before
enchantment protection, for damage that is in neither `#minecraft:bypasses_effects` nor
`#minecraft:bypasses_resistance`, as Apothic Attributes does. Arclight hands the method the damage after
Resistance, while Apothic Attributes takes the share from the damage before it, so the mixin works that
amount back out from the Resistance level, `amount * 25 / (25 - 5 * level)`. Resistance I and
Sundering I still cancel out. Where the redirect of Apothic Attributes does apply, as on plain Forge,
the step runs and Apothic Attributes adds the share itself, so the mixin adds nothing and the bonus
never counts twice. The first bonus after a start logs "Sundering added its damage for the first time
since the start". The mixin sits in the "server" list, so it only loads on a dedicated server and never
in single player.

On the test server nine iron golems with 1000 health and no AI each took one `/damage` of 10. The test
uses iron golems, because Livestock Overhaul replaces a summoned pig or cow at once and L2Hostility
gives hostile mobs levels. The table gives the health after the hit. The last column comes from a run
of 2.1.6 in which Arclight's mute was taken out, see below.

| Golem | Effects | Damage type | Health with 2.1.5 | Health with 2.1.6 | Health with 2.1.6 without the mute |
|---|---|---|---|---|---|
| 0 | None | Generic, `minecraft:generic` | 990 | 990 | 990 |
| 1 | Sundering I | Generic, `minecraft:generic` | 990 | 988 | 988 |
| 2 | Sundering II | Generic, `minecraft:generic` | 990 | 986 | 986 |
| 3 | Sundering I and Resistance I | Generic, `minecraft:generic` | 992 | 990 | 992 |
| 4 | Resistance I | Generic, `minecraft:generic` | 992 | 992 | 993.6 |
| 5 | Sundering I | Out of world, `minecraft:out_of_world`, which bypasses Resistance | 990 | 990 | 990 |
| 6 | Sundering I | Starve, `minecraft:starve`, which bypasses effects | 990 | 990 | 990 |
| 7 | Sundering II and Resistance II | Generic, `minecraft:generic` | 994 | 990 | 994 |
| 8 | Sundering V | Generic, `minecraft:generic` | 990 | 980 | 980 |

The 2.1.6 run logged "Sundering added its damage for the first time since the start, 10.0 to 12.0 on
minecraft:iron_golem". The headless `SunderingTest` compares 495 cases of damage, Resistance and
Sundering with the formula of Apothic Attributes, rebuilt from its bytecode, and finds at most a relative
difference of 3.4e-7, which is float rounding.

For the last column the test server ran a test-only copy of the Arclight mod jar, which Arclight
extracts to `.arclight/mod_file/`, without the entry `LivingEntityMixin$ApotheosisCompatMixin` in
`mixins.arclight.core.json`. That run on 2026-11-04 from 15:16 to 15:17 had no "@Redirect conflict"
line in `debug.log`, so the redirect of Apothic Attributes applied as on plain Forge. The step ran,
Apothic Attributes added the share, and the mixin added nothing and logged no "Sundering added its
damage". Golems 1, 2 and 8 lost 12, 14 and 20 health, the share once. A second share would have taken
14.4, 19.6 and 40. Without the mute both Arclight's modifier and the vanilla step apply Resistance, so
golems 3, 4 and 7 lost less health than on a real server. Golems 3 and 7 kept 992 and 994 with the
share once and would have kept 990 with it twice. The original mod jar went back afterwards, and its
SHA-256 matches `common.jar` in `arclight.jar` again.

A few differences to plain Forge remain. Resistance V or more leaves no damage on Arclight, so Sundering
adds nothing there, while Apothic Attributes lets 20 percent per Sundering level through. A caller of
`getDamageAfterMagicAbsorb` outside Arclight's damage path gets no Resistance on Arclight, but a share
worked out as if Resistance had taken its part. Mekanism's robit is one, because it runs its own
`actuallyHurt`, so a robit with Resistance and Sundering at once takes too much. In the Bukkit damage
event the share shows up in the `MAGIC` modifier, which can then be positive.

## Chunk tags waiting for their write

A chunk save hands its tag to the IO worker, which queues it and writes it later on its own thread. When
the same chunk is read again before that write, vanilla 1.20.1 answers `IOWorker.loadAsync` with the
queued tag itself. Then `ChunkMap.readChunk` runs `ChunkStorage.upgradeChunkTag` on it on a background
worker, which puts a `__context` key into the tag and removes it again. A write in that moment fails in
`CompoundTag.write` with `ConcurrentModificationException`, and the worker logs "Failed to store chunk".
Each Skylands pregen on the Mac mini hit this once on 2026-10-31, see
[Logs and crashes](../server/logs-and-crashes.md#chunk-save-race-during-pregen).

The failed write is not simply dropped. The method `RegionFileStorage.write` still closes its stream, so
the region file keeps the part of the tag that was written before the failure, without the end of the
tag. Until the chunk is saved again, every load of it fails with "Failed to read chunk" (an
`EOFException` under "Loading NBT data"), and the game generates the chunk again from scratch without any
other message. A chunk that is read back during its write and unloads again before anything changes in it
can come back as fresh terrain.

The mixin `IOWorkerPendingCopyMixin` of 2.1.6 hands out a deep copy of the queued tag instead. It changes
the first `Optional.ofNullable` call of the task in `loadAsync` (`m_223494_`), which only runs for a
chunk with a queued write. A chunk read from the region file is handed out as before, and a queued delete
still reads as no chunk. The copy is made on the IO thread, the only thread that writes the queued tag. A
headless measurement on 64 full chunk tags of the test world, 59 to 191 KB of NBT and 85 KB on average,
took about 0.1 ms per copy, and only a read of a chunk with a queued write pays it. Arclight 1.0.6 has no
mixin on `IOWorker`, and Steam 'n' Rails only injects at the head of `store`, before the tag is queued.

The headless `PendingChunkTagsTest` drives the real vanilla `IOWorker` on a temporary folder. Changing
the queued tag while the worker wrote it failed 5 of 5 writes with the stack seen on the Mac mini, and
none of the 5 entries read back. Changing the copy failed none of 20 writes, and all 20 entries held the
queued data.

On the test server a KubeJS probe queued writes of 32 tags with 4,000 keys each, read the same chunks
back at once and changed what the reads handed out, as `upgradeChunkTag` does. With 2.1.5, 31 of the 32
reads got the queued tag itself, 31 writes failed with "Failed to store chunk", and the region file kept
1 complete entry and 31 truncated ones with 703 to 3,494 of their 4,001 keys. With 2.1.6 no read got a
queued tag, no write failed and all 32 entries were complete. The truncated region file of the 2.1.5 run
went into the 2.1.6 world under another name. Force loading one of its chunks logged "Failed to read
chunk" for that chunk and 13 neighbours, and after the save the chunk held new terrain with status
`full`. A Chunky pregen of 441 chunks in the Skylands with 2.1.6 took 15 s, all 441 chunks reached the
disk with status `full`, and no "Failed to store chunk" came up. The Mac mini hit the race twice in
about 320,000 chunks, so such a short pregen only shows that the copy keeps saving and loading intact.

## Entities that fail to load

In vanilla, `EntityType.loadStaticEntity` drops an entity whose saved data does not load and logs only
"Exception loading entity: " with the stack trace. On 2026-11-03 the Mac mini logged four such lines with
"Entity has invalid position", which `Entity.load` throws when a coordinate of `Pos` is NaN. An infinite
coordinate is clamped to the world limit first and loads. None of the four lines named the entity.

Since 2.1.6 the logger `SkytekxCompat` adds one line right after the vanilla one, for example

```
Dropped entity minecraft:pig (IllegalStateException: Entity has invalid position) while loading chunk [240, 240] in minecraft:overworld: Pos [NaN, 70.0, NaN], Motion [0.0, 0.0, 0.0]
```

It names the type id, the innermost cause, the chunk whose entities were loading, the dimension and the
raw `Pos` and `Motion` lists. An entity that a spawner, a structure or a command loads shows "outside
chunk loading" instead of a chunk. At most 10 such lines are logged in a minute, and when lines were
left out, the first line after that minute ends with "(N earlier ones were not logged)". Vanilla still
drops the entity, and an error in the logging is swallowed. The mixin
`EntityTypeRejectedEntityLogMixin` redirects the vanilla warning in `loadStaticEntity` (`m_20669_`) and
logs it unchanged first. The mixin `EntityStorageChunkContextMixin` only remembers the chunk while the
lambda of `EntityStorage.loadEntities` (`m_223456_`) collects its entities on the server thread. Both
sit in the "server" list.

For the test, entities went into three entity chunks of the stopped test world. Chunk [240, 240] got a
pig at [NaN, 70, NaN] and an arrow at [NaN, 70, 3845.5], and in the 2.1.6 run also a valid iron golem.
Chunk [246, 240] got 12 zombies with a NaN position and chunk [252, 240] a cow with a NaN height. Force
loading the three chunks gave 15 vanilla lines in both runs. With 2.1.5 none of them named an entity.
With 2.1.6, 11 lines named them: both of [240, 240], 8 of the 12 zombies within the same minute, and
the cow a minute after the first line, with "(4 earlier ones were not logged)" at its end. The iron
golem loaded, and after the save the entity file held only the golem, as in vanilla. The headless
`RejectedEntityLogTest` checks the line, the rate limit and that the chunk is forgotten after a failed
load.

## Rebuild

```sh
cd ~/git/skytekx-worldgen
JAVA_HOME=~/jdk17/jdk-17.0.19+10 ./gradlew build -Djava.net.preferIPv4Stack=true
cp build/libs/skytekx-compat-2.1.3.jar ~/git/skytekx3-1201-server/local-mods/
```

Copy a jar into `local-mods/` only when it should ship in the pack. Versions 2.1.4 to 2.1.6 are for the
server only, and since the ruling of 2026-11-04 11:53 the pack keeps 2.1.3. A newer build must
therefore work with 2.1.3 clients, with no new network channel, no registry entry and no class that a
client needs. Client mixins stay in the "client" list.

Up to 2.1.2 the Gradle cache lacked MixinGradle 0.7.38 and two of its dependencies, so `--offline`
failed. The builds of 2.1.3 on 2026-11-01 and of 2.1.6 on 2026-11-04 ran with `--offline`.

The Gradle task `check` runs four headless tests, each a plain `main()`, because the offline cache has
no JUnit. The test `SessionRetryTest` has 103 checks, `SunderingTest` has 16, `PendingChunkTagsTest` 36
and `RejectedEntityLogTest` 18. The last three run in `build/`, because Minecraft's logging writes a
`logs` folder into the working directory.

Copy the new jar into the server's `mods/` as well and restart. The build needs Java 17 and copies of
Bird's Nests, Infernal Mobs 1.20.1.11 and Falling Meteors 2.5 in `libs/` (compile only, for the mixin
processor), which the repo tracks.

## Open points

- The rename stopped at the artifact. The Gradle root project is still `skytekx-worldgen`, the package
  is `com.skytekx.worldgen`, the main class is `SkytekxWorldgen`, the logger is named `SkytekxWorldgen`
  and the jar's `pack.mcmeta` says "SkyTekx Worldgen".
- The old `build/libs/skytekx-worldgen-1.0.0.jar` (12,955 bytes, 2026-06-30 05:42) still lies next to
  the new jar.
- The pack keeps 2.1.3 by the ruling of 2026-11-04 11:53, so players get no join retry during Mojang
  outages, see [Decisions](../decisions.md).
- Which mod writes entities with a NaN position is still open. The next one shows up with its type and
  chunk in a "Dropped entity" line, see [Entities that fail to load](#entities-that-fail-to-load).
- No plain Forge dedicated server ran 2.1.6. The run without Arclight's mute took the path of plain
  Forge on the test server and counted Sundering once, see
  [Sundering on Arclight](#sundering-on-arclight). Before a build with this mixin goes into the pack,
  run the golem test once on a plain Forge dedicated server. Single player cannot show it, because the
  mixin never loads there.
- The folder `modrinth-permissions/` still has `skytekx-worldgen-1.0.0.jar.md` next to
  `skytekx-compat-2.1.3.jar.md` (see [publish](../modpack/publish.md)).
- The command `/locate` and explorer maps use `Structure.findValidGenerationPoint`, not `generate`.
  Beyond the pregenerated radius they can point at a swamp hut or a meteorite that the start check then
  cancels.
- The void floor has no headless test. It was tested on throwaway copies of the server (tests 1 to 6),
  see [Skylands worldgen](skylands-worldgen.md#the-void-floor).
- Five jigsaw placers of other mods never reach the redirected `tryPlacingChildren`, so their children
  over the void would still land at Y 0. Today only two of their structures can start on an island,
  and neither needs the redirect, see
  [Skylands worldgen](skylands-worldgen.md#no-structures-over-the-void). Keep the island biomes out of
  their tags and run the void scan after any biome tag change.
- A boot of 2.1.3 without Falling Meteors on the test server (2026-11-01 06:13) came up normally.
  Mixin only logged `@Mixin target net.meteor.meteors.world.MeteorManager was not found` as a warning,
  although the mixin config is required. A missing Bird's Nests or Infernal Mobs should behave the
  same, but nobody tested those two.

## History

- On 2026-06-29 the skytekx-worldgen mod registered the structure placement type `skytekx:surface_spread`
  for the first Skylands structures (see [Skylands worldgen](skylands-worldgen.md#history)). It ran on
  the server but was not bundled, so mrpack 3.0.4 shipped without it.
- On 2026-06-29 at 20:10 the Forge tick handler `SkylandsFallHandler` moved into the mod after the KubeJS
  fall script failed, and the jar joined `local-mods/` (commit bf898e6). On 2026-06-30 04:37 the handler
  also got the way up (2eceeb1), see [Skylands travel](aether-fall.md).
- On 2026-06-30 at 06:21 the Bird's Nests mixin arrived, and the jar grew from 11,986 to 12,955 bytes
  (commit d14ea2b).
- On 2026-07-12 the source became a git repo. Commit 64d3126 (21:43) imported version 1.0.0 as it ran on
  the server. Commit 2b49bba (21:44) deleted `SkylandsFallHandler` (124 lines),
  `SurfaceSpreadStructurePlacement` (129), `SurfaceCheckContext` (54) and the `ChunkGeneratorMixin` (49),
  and trimmed the main class, 385 lines in total. Commit 68efb3f renamed the artifact to skytekx-compat
  and set version 2.0.0. In this repo the jar swap landed with commit 174fbf6, although the message of
  dc8b470 claims it.
- On 2026-10-31 at 23:49 version 2.1.0 added the [Skylands void floor](#skylands-void-floor) (commit
  9636058) and the [server list](#server-list) version (66c0847, 3c3a6a0).
- On 2026-11-01 at 00:44 commit db56da3 exempted structures whose biomes include `skytekx3:sky_void`.
  Without it the start check also cancelled floaters whose center hung over the void, and a test world of
  radius 1000 held 16 floater starts instead of 25.
- On 2026-11-01 at 00:58 version 2.1.0 went live on the Mac mini, and 2.0.0 went to
  `~/skytekx3-backups/jars-orig/` there. The Skylands generated again to radius 2000 held no block below
  Y 13 and 93 floaters, see [Skylands worldgen](skylands-worldgen.md#deploy-of-2026-11-01).
- On 2026-11-01 at 01:54 version 2.1.1 fixed three findings of the review of 2.1.0. Commit f6c0c41 reads
  the structure's own biomes for the `sky_void` exemption, d2f1778 cancels scattered feature starts with
  a void column under them, 1db0e06 keeps the sky stone of meteorites above the floor, and 630fca4 sets
  the version, so the two builds no longer share the name 2.1.0. Test 5 found the same 135 starts as test
  4 and no block below Y 13, see [Skylands worldgen](skylands-worldgen.md#the-void-floor). Version 2.1.1
  only ran on the test server.
- On 2026-11-01 at 02:31 version 2.1.2 fixed the two findings of a review of 2.1.1 that ran from 01:55 to
  02:23. Commit 7a1a308 counts a meteorite from 9 blocks below its center, 5171fbc corrects the class
  comment on the desert pyramid, 976138e adds `StructurePieceSkylandsFloorMixin`, and 26a493d sets the
  version. In test 6 a swamp hut over carved stilt columns dropped 44 oak logs below Y 13 with 2.1.1 and
  none with 2.1.2, while stilts into shallow pits stayed the same. The server repo bundles 2.1.2 since
  a76fbdd.
- On 2026-11-01 at 02:51 version 2.1.2 went live on the Mac mini after a warning in chat, and 2.1.0 went
  to `~/skytekx3-backups/jars-orig/` there. The server was up at 02:52:03 with the 3 accepted boot errors
  and logged the void floor line.
- On 2026-11-01 at 05:45 version 2.1.3 began with commit 7d58122, which answers Infernal Mobs queries on
  the server thread, and 23d4173 set the version. At 06:00 commit c6d170b added the Falling Meteors
  mixin. The server repo bundles 2.1.3 since 871e1d8.
- On 2026-11-01 at 06:10 version 2.1.3 went live on the Mac mini together with Falling Meteors
  Reimagined, without Get It Together Drops and with the Spigot item merge radius at 0.5. Nobody was
  online, and 2.1.2 went to `~/skytekx3-backups/jars-orig/` there. The server was up at 06:11:04 with the
  3 accepted boot errors.
- On 2026-11-01 at 21:19 version 2.1.4 (commit 1735f0f, version 423c054) went live on the Mac mini
  together with the noon-only Skylands. It keeps Skylands blocks safe from mob griefing and explosions.
- On 2026-11-04 version 2.1.5 (commits c7b89ac and 88b3759) added the
  [Mojang session retries](#mojang-session-retries). It went live on the Mac mini at 10:33 with nobody
  online, and 2.1.4 went to `~/skytekx3-backups/jars-orig/` there.
- On 2026-11-04 version 2.1.6 (commits 2fe49db, 0224635, 872310e and 62e5616) adds the
  [Sundering share on Arclight](#sundering-on-arclight), the
  [copy of queued chunk tags](#chunk-tags-waiting-for-their-write) and the
  [line that names dropped entities](#entities-that-fail-to-load), on the server only. It passed the test
  server from 13:35 to 13:39, compared with a run of 2.1.5 from 13:11 to 13:14. After fixes to comments
  and commit messages the jar was built again. Its classes are byte identical to the tested ones, and it
  booted once more from 13:54 to 13:56 with the same golem health values, server list and probe result. A
  run from 15:16 to 15:17 without Arclight's mute showed that the Sundering share counts once where the
  redirect of Apothic Attributes applies. It waits for its deploy.

Related: [boot fixes](boot-fixes.md), [mod set](../modpack/mod-set.md),
[Skylands worldgen](skylands-worldgen.md), [content](index.md).
