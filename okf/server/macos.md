---
type: Runbook
title: Mac mini (macOS, launchd) deployment
description: The production host, an Apple M4 Mac mini on macOS 15, with the Java 17 launcher, the launchd jobs for server, RCON guard and backup, the sync from the build machine, the Halloween world of 2026-10-31 and the numbers measured there.
tags: [server, deploy, macos, launchd, rcon, pf, backup, world]
timestamp: 2026-10-31
---

# Mac mini (macOS, launchd) deployment

The server runs on an Apple M4 Mac mini in the LAN. The macOS service manager launchd starts it at boot
with nobody logged in, keeps it running and stops it cleanly over RCON. The build machine (the Asahi
laptop with the git checkout) pushes mods, configs and datapacks to it with `macos/sync-to-mac.sh`.
The first run on the Mac mini happened on 2026-10-31 with a new world. The owner made the Mac mini the
server "for the time being" on 2026-10-31 at 16:12 and asked to optimize everything for it at 16:34.

## Host

| Item | Value |
|---|---|
| Hardware | Mac16,10, Apple M4 with 4 performance and 6 efficiency cores, 16 GB RAM |
| System | Apple macOS 15.6 on wired Ethernet (Wi-Fi off) with a fixed LAN address |
| Account | One admin user runs the server and owns the checkout in its home folder. Its password is not in the repo |
| Java | Temurin 17.0.21 arm64 from `brew install --cask temurin@17`, found by `/usr/libexec/java_home -F -v 17`. The flag `-F` makes the lookup fail when Java 17 is missing, instead of falling back to another installed Java |
| Tools | Homebrew in `/opt/homebrew` (not on the PATH of a non-login SSH command), htop, and the system `/usr/bin/python3` 3.9 for `tools/rcon.py`. The region scans run in a separate Python environment with numpy |
| Disk | The data volume holds 228 GiB. On 2026-10-31 at 21:05, after the pregens and the first backup, `df -h /` reported 9.4 GiB free. Watch it, the backups need room |

The host was slimmed for headless use on 2026-10-31, as the owner asked at 16:02.

- Third party launch agents and daemons (Docker, Windscribe, F5, Microsoft, OneDrive, Google
  updaters) are disabled with `launchctl disable`, and all login items are gone. Siri, photo and media
  analysis and similar Apple agents are disabled for the user.
- Spotlight indexing is off (`mdutil -a -i off`), location services and Bluetooth are off.
- Power management keeps the Mac awake and brings it back after a power cut: sleep 0, disksleep 0,
  powernap 0, womp 1 and autorestart 1. The script `macos/install.sh` sets these on every run.
  Displaysleep is 1.
- FileVault is off. With FileVault on, macOS waits at the unlock screen after a power cut and the
  server would not come back.
- The application firewall is off and Time Machine has no destination. The packet filter pf guards
  RCON, see below.
- After a reboot with nobody logged in, macOS uses about 3.1 GB of RAM.

On 2026-10-31 the system log stops at 16:35:35 without a panic report and the next boot is at 16:52.
That points to a power cut, and autorestart brought the Mac back.

## Files

| File | Role |
|---|---|
| Script `ServerMac.sh` | The launcher. Java 17, a 9G heap, Aikar's G1 flags, a 512M code cache, headless AWT, a soft limit of 10240 open files when the shell has less (launchd's 65536 stays), and the SIGTERM handling described below. The Mac mini still runs the version before the fixes of 2026-10-31 evening until the next watched restart |
| Plist `macos/com.skytekx.server.plist` | LaunchDaemon `com.skytekx.server`. Runs `ServerMac.sh` as the server user at boot, `KeepAlive` true, `ThrottleInterval` 30, `ExitTimeOut` 180, `ProcessType` Interactive, 65536 open files, stdout to `/dev/null`, stderr to `logs/launchd-stderr.log` |
| Plist `macos/com.skytekx.rcon-guard.plist` | LaunchDaemon `com.skytekx.rcon-guard`. Runs as root at boot and loads the pf anchor |
| Rules `macos/pf-rcon-guard.conf` | The anchor rules: pass TCP 65535 on `lo0`, drop new connections to it (SYN without ACK) on every other interface. The loaded anchor still drops every packet to 65535 until `install.sh` runs again |
| Plist `macos/com.skytekx.backup.plist` | LaunchDaemon `com.skytekx.backup`. Runs `macos/backup.sh` daily at 05:00 as the server user |
| Script `macos/backup.sh` | Pauses saving over RCON, flushes, archives `world/` to `~/skytekx3-backups`, keeps the newest 3. It skips the run unless the disk has room for an archive as large as the newest one plus 4 GB, and a failed run removes its partial archive and turns saving back on |
| Script `macos/skytekx3ctl` | The commands `status`, `start`, `stop`, `restart`, `rcon <command>`, `log` and `backup` |
| Script `macos/install.sh` | Run with sudo. Checks for Java 17 and `/usr/bin/python3`, sets the power options, fills in the plist placeholders, installs the pf anchor, enables the three jobs and loads them. A second run reloads them, which restarts a running server cleanly and starts a stopped one. It waits until each old job is gone before it loads the new one. The tunnel job is installed by hand, see [Public access](#public-access) |
| Script `macos/sync-to-mac.sh` | Run on the build machine. Pushes the checkout to the Mac mini |

## Stop and restart

Arclight must never get a signal. A SIGTERM sends it down its shutdown thread, which skips the stop
handlers, and every logged signal stop saved badly (see
[Logs and crashes](logs-and-crashes.md#stop-path)). On `launchctl bootout` and on every shutdown and
reboot, launchd sends SIGTERM. So `ServerMac.sh` starts the JVM as a child when it has no terminal, and a
SIGTERM or SIGINT to the script becomes an RCON `stop`. If RCON does not answer within 90 seconds
(the server is still booting), the script falls back to the signal. In a terminal the script hands over
to the JVM as before, and `stop` is typed into the console.

| Task | Command (in the server directory) |
|---|---|
| Status, players and tick times | Run `macos/skytekx3ctl status` |
| Restart | Run `macos/skytekx3ctl restart`. It sends RCON `stop`, and `KeepAlive` starts the server again |
| Stop and keep it down | Run `macos/skytekx3ctl stop`. It disables the job, so the server stays down across reboots, boots it out, which stops the server over RCON, and waits up to 200 s until the JVM has exited and launchd has removed the job |
| Start after a stop | Run `macos/skytekx3ctl start`. It enables the job and loads it |
| Console command | Run `macos/skytekx3ctl rcon "<command>"`. RCON replies longer than 4 KB are cut off by `tools/rcon.py` |
| Follow the log | Run `macos/skytekx3ctl log` |

### Tests of 2026-10-31

Each path ran once on the Mac mini between 19:26 and 19:36. Each stop went through RCON, none logged a
line from the `Server Shutdown Thread`, and each of the four boots reached `Done` with the baseline of
0 FATAL and 3 accepted ERROR lines (see [Logs and crashes](logs-and-crashes.md#boot-error-baseline)) and
with 344 WARN lines.

| Test | Started | Result |
|---|---|---|
| Reinstall, a second `sudo macos/install.sh` | 19:26:01 | RCON stop, then a new boot at 19:26:53 with `Done` after 32 s. The first try failed, see below |
| Restart, `macos/skytekx3ctl restart` | 19:27:39 | KeepAlive started the server again at 19:27:46, `Done` after 32 s |
| Stop, `macos/skytekx3ctl stop` | 19:33:16 | Returned after 8 s with the JVM gone and the job removed |
| Start, `macos/skytekx3ctl start` | 19:33:32 | Reached `Done` after 32 s |
| Backup, `macos/skytekx3ctl backup` | 19:34 | Took 16 s with the server online. It wrote `world-2026-10-31-1934.tgz` (688 MB) and switched saving back on |
| Reboot, `sudo reboot` | 19:34:48 | RCON stop at shutdown. The server booted at 19:35:14 with nobody logged in and reached `Done` after 35 s. The pf anchor was loaded, 65535 was closed to the LAN, 25565 answered, and `logs/launchd-stderr.log` stayed empty |

The first attempt at the second run of `install.sh` stopped the server cleanly but failed to load the
job again with `Bootstrap failed: 5: Input/output error`. The command `launchctl bootout` returns before
the job has exited, and the bootstrap came too early, so the server stayed down. Since then
`install.sh` waits until `launchctl print` no longer finds the job before it loads it, and
`skytekx3ctl stop` waits up to 200 s until the JVM has exited and launchd has removed the job. Both
waits held in the tests above.

## RCON guard

RCON binds every interface (`server-ip=0.0.0.0`) and sends its password in clear text. The macOS
application firewall cannot filter by port, so pf does it. The job `com.skytekx.rcon-guard` loads
`/etc/pf.anchors/com.skytekx` into the anchor `com.apple/250.skytekx` and enables pf. The system
`/etc/pf.conf` evaluates every `com.apple/*` anchor, so no system file is edited.

Verified on 2026-10-31 from the build machine: before the guard, port 65535 accepted connections from
the LAN. With the guard it times out, while 25565 stays open and RCON over loopback works. The anchor
also survives a reload of the main ruleset with `pfctl -f /etc/pf.conf`. That test ran with the first
rule, which dropped every packet to 65535, also the replies to the Mac's own outgoing connections from
that port. The rule in the repo drops only new connections. `pfctl -nvf` accepts it, but it is not
loaded yet.

## Deploy and update

First deploy, on the build machine:

```bash
macos/sync-to-mac.sh user@mac-mini --with-world   # Only for a first copy, refused when the Mac has a world
```

Then on the Mac mini, once:

```bash
cd ~/skytekx3-1201-server
sudo macos/install.sh
```

Later updates, on the build machine. The commands `stop` and `start` call sudo, so the SSH sessions
need a terminal (`-t`) for the password prompt:

```bash
ssh -t user@mac-mini 'cd ~/skytekx3-1201-server && macos/skytekx3ctl stop'
macos/sync-to-mac.sh user@mac-mini
ssh -t user@mac-mini 'cd ~/skytekx3-1201-server && tools/sync_skylands_datapack.sh && macos/skytekx3ctl start'
```

Without `--with-world` the sync never touches live state: `world/` except the fixes pack,
everything in `plugins/Essentials/` but `config.yml`, `server.properties` (it holds the RCON
password), ops, whitelist, bans, caches, `eula.txt`, `local/`, `server-icon.png` and the Chunky task
records in `config/chunky/tasks/`. It mirrors `mods/` and the two Skylands datapack sources exactly, so
removed files disappear on the Mac too, and it mirrors `world/datapacks/skytekx3_fixes/` straight into
the Mac's world, which takes effect at the next restart or `/minecraft:reload`. With `--with-world` it
refuses to run when the Mac already has `world/level.dat`, unless `--overwrite-world` is given too.
Other deletions are done by hand. The script never copies `ansible/` or the vault password file. Run
`tools/sync_skylands_datapack.sh` on the Mac after a datapack change. It mirrors the sources into
`world/datapacks/`.

The sync copies `config/` as it is in the checkout, and the Mac's configs have moved on since the
first copy. On 2026-10-31 at 20:00 a checksum dry run (`rsync -n -c` with the script's options) found
15 files under `config/` that differ. The mods updated that day rewrote several of them at their first
boot on the Mac, and `config/collective.json5` is gone there because Collective 8.72 moved its settings
to `config/serilum/collective.json`. A full sync would put the old versions back. Until the Mac's
versions are reviewed and committed, run such a dry run before every sync, see
[Open items](../open-items.md#defects-to-fix).

## The Halloween world

The owner asked for a new map with a light Halloween theme on 2026-10-31 at 16:56. The old world stays
untouched in `world/` on the build machine, with the 3.0.5 datapacks. The Mac mini got a fresh world.

| Item | Value |
|---|---|
| Seed | -6309212766548435273 |
| World spawn | 308 64 392, on grass inside a Biomes O' Plenty pumpkin patch about 499 blocks from 0,0 |
| MOTD | First line `§6§lSkyTekx3 §5§lHalloween`, second line `§dA Party Hexxit Deluxe Server` |
| Icon | The file `server-icon-halloween.png`, copied over `server-icon.png` on the Mac |
| Lunar events | Blood moons on about every second full moon (`bloodMoonChance` 0.5), and Nyx events in the Skylands too |
| Skylands | Pumpkin patches in 1 of 8 island chunks and small groups of jack o'lanterns in 1 of 32 |
| Spawn protection | 0 since 2026-10-31 22:49. With the default of 16 and one op on the server, nobody but the op could break or place blocks within 16 blocks of the spawn, where every new player lands (spawn radius 10) |

The spawn spot came from a biome map of the patch on an 8 block grid, to keep the 10 block spawn
radius inside the biome and off the pond in its northwest corner. In a fresh world the three file
packs load after all mod packs. They hold the last three of 280 places in the enabled list of
`level.dat`, so their overrides win.

To go back to the normal icon, copy `server-icon.png` from the checkout over the one on the Mac.

## Pregen and worldgen checks

Both dimensions were pregenerated around the world spawn (center 308 392) with Chunky on
2026-10-31. Each run went one dimension at a time with the server otherwise idle.

| Run | Dimension | Radius | Chunks | Time | Median rate |
|---|---|---:|---:|---:|---:|
| 18:13 to 18:32 | Skylands, before the fixes | 3000 | 142,129 | 19 min 15 s | 119.4 chunks/s |
| 18:32 to 18:40 | Overworld | 1500 | 35,721 | 8 min 1 s | 74.4 chunks/s |
| 18:55 to 19:15 | Skylands, again after the fixes | 3000 | 142,129 | 19 min 50 s | 116.4 chunks/s |

The first Skylands run showed two defects in the scans, petrified roots and windmill fields on the
islands below a floating windmill. Commits 0cf350a and 7b51eb5 fixed them, see
[Skylands worldgen](../content/skylands-worldgen.md#changes-of-2026-10-31). The Skylands were then
wiped (region, entities, poi and data, `uid.dat` kept) and generated again with the fixed packs.

After a `save-all flush`, the region scans of [Tools](../modpack/tools.md#region-scans) read the new
Skylands at 19:20 with the Python environment on the Mac. They took about two minutes.

- All 159,201 chunks were readable, 142,129 of them complete, and 93% of the columns are void. The
  fixed reader of 4ec39e3 confirmed that on a copy of the same files at 21:40.
- None of the structure starts has a piece below Y 24. The scan of 19:20 counted 268 starts. The fixed
  scan of the same files counts 296, because it also reads the 28 starts in chunks that are not full
  yet.
- Below Y 24 lie 1,826 blocks, all island bottoms, amethyst geodes and two small natural islands. The
  307 petrified root blocks and the two windmill field blocks at Y 1 of the first run are gone.
- The 10 windmills keep both parts, and their lowest piece sits at Y 139.

The chunk [-14, -52] that hit the save race at 18:57:56 is in the region file with status `full`, see
[Logs and crashes](logs-and-crashes.md#chunk-save-race-during-pregen).

## Measured numbers

The sampler logged the JVM's resident memory, its CPU share, the free memory and swap of the host and
the mean tick time once a minute.

| State | Resident memory | CPU | Mean tick | TPS | Swap |
|---|---|---|---|---|---|
| Skylands pregen, first run | 10.55 to 10.66 GB | Up to 947% | 0.9 to 4.4 ms | 20 | None |
| Overworld pregen | 10.11 to 10.67 GB | Up to 575% | 1.6 to 4.9 ms | 20 | None |
| Idle after the pregens, nobody online | 10.33 GB | About 33% | 0.1 to 1.3 ms | 20 | None |
| Skylands pregen, second run | 10.49 to 10.63 GB | 436% to 951% | 0.9 to 2.7 ms | 20 | None |
| Idle after the second run | 10.63 GB | About 33% | 0.1 ms | 20 | None |

The boot under launchd at 18:54:57 reached `Done` in 32 seconds with the baseline of 0 FATAL and 3
accepted ERROR lines (see [Logs and crashes](logs-and-crashes.md#boot-error-baseline)) and 387 WARN
lines. The first boot of the fresh world at 18:07 had the same 3 ERRORs and 1,658 WARNs, 1,316 of them
from Forge filling the new `world/serverconfig/` files with defaults.

The Overworld region folder held 618 MB at radius 1500 and the first Skylands run 785 MB at radius
3000. At that density a radius of 5000 would take about 6.8 GB for the Overworld alone and about 1.5
hours at the measured rate, which the free disk does not allow. Load with players is still unmeasured.

## Public access

No port can be forwarded to the Mac mini. Its uplink is a mobile connection behind carrier grade NAT:
on 2026-10-31 tracepath showed the private hops 172.20.136.193, 192.168.242.31 and 172.16.25.16 at
about 40 ms before the first public one, and there is no IPv6. So the earlier plan of a router forward
plus a DNS record without the Cloudflare proxy cannot work.

Since 2026-10-31 at 21:13 a Cloudflare Tunnel carries the game instead. The connector cloudflared on
the Mac mini dials out to Cloudflare, so no port has to be open.

| Part | Setup |
|---|---|
| Tunnel | Named `skytekx3-mac-mini`, in the Cloudflare account of the zone skytekx.com and configured on Cloudflare's side. Its only route sends `hex-tunnel.skytekx.com` to `tcp://localhost:25565` |
| DNS | The name `hex-tunnel.skytekx.com` is a proxied CNAME to the tunnel. The player address `hex.skytekx.com` has a TXT record, `cloudflared-route=hex-tunnel.skytekx.com`, and since 2026-10-31 22:08 the address 192.0.2.1, unproxied. That documentation address answers nothing, but without an address Minecraft stops at "Unknown host" before Modflared reads the TXT record. Since 22:15 `play.skytekx.com` has the same two records, as a second name for the same server |
| Connector | Homebrew's cloudflared 2026.10.7 in `/opt/homebrew/bin`. The LaunchDaemon `com.skytekx.cloudflared` ([plist](../../macos/com.skytekx.cloudflared.plist)) runs [run-skytekx-tunnel.sh](../../macos/run-skytekx-tunnel.sh) from `~/.cloudflared/` as the server user at boot, with `KeepAlive`. It logs to `~/Library/Logs/skytekx-cloudflared.log` |
| Token | The file `~/.cloudflared/skytekx.token`, mode 600. The wrapper hands it to cloudflared through the environment. It is not in the repo, see [Secrets](secrets.md) |
| Setup script | The script [cf_tunnel.py](../../tools/cf_tunnel.py) creates or updates the tunnel, its route, the CNAME and the TXT and A records of both player names and writes the token to a file. Every step is idempotent |

Players join through the client mod Modflared (Forge 1.20.1, 1.2.0+release.61, MIT, client only). It
reads the TXT record of `hex.skytekx.com` and runs the game connection through the route with its own
copy of cloudflared. At every start it asks the GitHub API for the newest cloudflared, and it downloads
it on the first start. Without Modflared the name does not resolve. Pack 3.0.7 brings Modflared as a
client only index entry (ca131c3) and lists `hex.skytekx.com` in its server list (d6c7d97). Pack 3.0.6
does not contain Modflared, so its players add the jar by hand. Simple Voice Chat needs 24454/udp,
which the tunnel does not carry, so voice chat only works in the LAN. On 2026-10-31 at 21:48 the owner
chose the pack with Modflared, not a relay on an owner server or playit.gg, and with it no voice chat
over the internet. They also kept the whitelist off.

Where Modflared runs was read from the bytecode of 1.2.0+release.61 with javap and not tested in a
running game. Its enum `CloudflaredDownload` maps Windows and Linux on x86 and amd64 and macOS on
x86_64 and aarch64 to a cloudflared download.

- Both macOS entries download `cloudflared-darwin-amd64.tgz`, an Intel build, so an Apple Silicon Mac
  needs Rosetta 2 (`softwareupdate --install-rosetta --agree-to-license`).
- Linux on ARM, such as the build machine, matches no entry. There `CloudflaredDownload.find()` throws
  an `IllegalStateException`, "Cloudflared could not be downloaded because no binary file was found for
  the current operating system". It throws inside the future that `TunnelManager.prepareBinary()`
  starts at client setup, whose `whenComplete` handler only logs it. So the game starts normally, a
  join to `hex.skytekx.com` fails, and a join by the LAN address works.

What was checked on 2026-10-31:

- The connector registered four connections in Vienna at 21:13.
- A server list ping through `cloudflared access tcp` from the build machine got "forge arclight/Trials
  1.20.1" with 0 of 39 players.
- The Cloudflare resolver 1.1.1.1 returns the TXT record.
- Nobody has joined through Modflared yet.

To set it up again:

1. On the build machine, with `CLOUDFLARE_ACCOUNT_EMAIL` and `CLOUDFLARE_Global_API_Key` (or
   `CLOUDFLARE_API_TOKEN`) in the environment, run `python3 tools/cf_tunnel.py <token file>`.
2. On the Mac mini, run `brew install cloudflared`. Copy the token to `~/.cloudflared/skytekx.token`
   (mode 600) and `macos/run-skytekx-tunnel.sh` to `~/.cloudflared/run-skytekx-tunnel.sh` (mode 700).
   Delete the token file on the build machine.
3. Fill in `__USER__` and `__HOME__` in `macos/com.skytekx.cloudflared.plist`, copy it to
   `/Library/LaunchDaemons/` (owner root:wheel, mode 644) and load it with
   `sudo launchctl bootstrap system /Library/LaunchDaemons/com.skytekx.cloudflared.plist`.
4. Look for "Registered tunnel connection" in `~/Library/Logs/skytekx-cloudflared.log`.

### Link drop of 2026-10-31

The Mac mini's Ethernet link went down at 21:22:01 and came back at 21:28:43 at 1 Gbit/s
(`AppleBCM5701Ethernet [en0]: Link down` and `Link up` in the system log). The Mac mini did not
reboot, the server kept running, and cloudflared reconnected by itself at 21:28:48. The router stayed
reachable from the build machine the whole time. The cause is not known. A cable, the switch port and
someone unplugging the Mac mini fit the log.

Related: [Server](index.md), [Arclight runtime](arclight.md), [World reset and pregen](world-reset.md),
[Logs and crashes](logs-and-crashes.md), [Secrets](secrets.md), [Deploy on Debian (systemd)](deploy.md).
