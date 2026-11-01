---
type: Runbook
title: Deploy on Debian (systemd)
description: The Linux deployment path for a Debian or Ubuntu host with the systemd unit, ServerLinux.sh, mcrcon, ports, firewall and DNS, plus the files that git does not hold.
tags: [server, deploy, systemd, rcon, linux, debian]
timestamp: 2026-11-01
---

# Deploy on Debian (systemd)

This page is the Linux path. It targets a Debian or Ubuntu host and runs the server under systemd
without `screen`, controlled over RCON. The step by step host setup (packages, user, mcrcon build,
clone) is in the README section [Self-hosting the server](../../README.md#self-hosting-the-server).
The production host is an Apple M4 Mac mini, see [Mac mini (macOS, launchd) deployment](macos.md).

The systemd unit has not run on any logged host. Every logged run happened on the build machine,
started by hand from the checkout and stopped over RCON (see [Logs and crashes](logs-and-crashes.md)).
Treat the unit as untested. The facts below about ports, mods and stopping come from the logged runs.

## Layout

- The server lives in `/opt/minecraft-server/server/skytekx3-1201-server`, a clone of this repo owned
  by the unprivileged system user `minecraft`.
- The unit file is `systemd/skytekx3.service`. It runs as `User=minecraft` with `Nice=-20`, sandboxing
  (`ProtectHome`, `PrivateDevices`, `NoNewPrivileges`, `PrivateTmp`), write access to the server
  directory only, and `Restart=always` with `RestartSec=3`.
- Its `ExecStart` runs Java 17 with `-Xms11G -Xmx11G`, Aikar's G1 flags including
  `-XX:+AlwaysPreTouch`, and `-jar arclight.jar nogui`. The heap is sized for a 16 GB host, see
  [Performance](performance.md#heap-and-memory).
- Its `ExecStop` runs `/usr/local/bin/mcrcon -H localhost -P 65535 -p ${RCON_PW} stop`.
- The foreground launcher `ServerLinux.sh` uses the same flags. The variables `JAVA`, `JAR` and `MEM`
  override its defaults. In a foreground run, stop the server by typing `stop` into the console, not
  with Ctrl+C (see [Stop path](logs-and-crashes.md#stop-path)).

## Java path

The unit and `ServerLinux.sh` call `/usr/lib/jvm/java-17-openjdk/bin/java`, which is the Fedora layout.
Debian and Ubuntu install `openjdk-17-jre-headless` under `/usr/lib/jvm/java-17-openjdk-<arch>`, for
example `java-17-openjdk-amd64`. Fix the path in the `ExecStart` line of `systemd/skytekx3.service`
before the first start, and pass `JAVA=` to `ServerLinux.sh`. If the distribution no longer ships Java
17, install Temurin 17 from Eclipse Adoptium, the build that every logged run used. Why Java 17 and
not 21 is on [Arclight runtime](arclight.md#java-17-only).

## What git does not hold

A clone alone does not run the server. Copy these from the reference install on the build machine.

- The mod folder `mods/`. It holds the 234 jars the server really runs, including ten jars patched in
  place and BrassAmber BattleTowers, which exists nowhere else in the repo (see
  [Arclight runtime](arclight.md#mods-and-plugins)). Copy it as it is.
- The plugin jar `plugins/EssentialsX-2.22.0.jar` and the folder `plugins/Essentials/`. Its user data
  (`userdata/`, `usermap.bin`, `uuids.bin`) only belongs on the new host if the world moves along. See
  [Plugins](../modpack/plugins.md).
- The world in `world/`, if the existing world moves along. Git only tracks the three datapacks in
  `world/datapacks/`. A fresh world needs the seed and the datapack order set by hand, see
  [World reset](world-reset.md#fresh-world).
- The live `server.properties`. Start from `server.properties.example` and set a new RCON password, see
  [Secrets](secrets.md).
- The lists `ops.json`, `whitelist.json` and `banned-*.json`, if they should carry over. Git ignores
  them.

Do not fill `mods/` with `tools/resolve_all.py`. It never reads `modrinth.index.json`. It resolves the
newest release of a hard-coded list of slugs, which still contains Radium, the mod that crashed
Arclight on 2026-06-27. It also wants the Modrinth token as its first argument, and it hard-codes the
build machine's paths. No tool in the repo restores `mods/` from the pinned index yet. A rebuild from
the index means four steps.

1. Download every index entry whose `env.server` is not `unsupported`, and check its hash against the
   index.
2. Copy the `local-mods/` jars into `mods/`.
3. Run `python3 tools/patch_mods.py`.
4. Add BrassAmber BattleTowers, which is on the build machine but in no pack, see
   [Mod set](../modpack/mod-set.md#jars-outside-the-index).

Every index entry downloads from the Modrinth CDN since d3391bf. The tool landscape is on
[Tools](../modpack/tools.md).

## RCON and mcrcon

The unit reads `RCON_PW` from `unit.conf` in the repo root, that is
`/opt/minecraft-server/server/skytekx3-1201-server/unit.conf`. Git ignores the file (`.gitignore:48`).
Write it by hand with the same password as `rcon.password` in `server.properties`, and make it readable
for its owner only.

```bash
echo "RCON_PW=<the rcon.password from server.properties>" > unit.conf
chmod 600 unit.conf
```

The example `systemd/unit.conf.example` says that ansible renders the password from
`vault_rcon_password`. No playbook exists, so nothing renders it. The leading `-` in
`EnvironmentFile=-...` makes a missing `unit.conf` silent. If the file is missing, `ExecStop` sends an
empty password and the RCON stop fails.

Build mcrcon from Tiiffi/mcrcon and install it as `/usr/local/bin/mcrcon`, as the README shows. The
repo's `tools/rcon.py` is a standard library alternative that sends one command per call, for example
`python3 tools/rcon.py 127.0.0.1 65535 "$RCON_PW" stop`. Both tools take the password as an argument,
so it shows in the process list while they run.

## Stopping and restarting

The `ExecStop` command returns as soon as the server has accepted `stop`. Saving takes another 5 to 9
seconds (see [Stop path](logs-and-crashes.md#stop-path)). With `KillMode=none` systemd kills no
process. It marks the unit as stopped while the JVM is still saving, and it leaves the JVM running if
the RCON stop failed. Wait until the java process is gone (`pgrep -f 'java .*arclight[.]jar nogui'`)
before you start the unit again or touch `world/`. Current systemd versions also warn that
`KillMode=none` is deprecated.

A synchronous `ExecStop` would close both gaps. It sends `stop` and then waits for the main process to
exit. This line is a candidate and has not been tested.

```ini
ExecStop=/bin/sh -c '/usr/local/bin/mcrcon -H localhost -P 65535 -p "$RCON_PW" stop; while kill -0 "$MAINPID" 2>/dev/null; do sleep 1; done'
```

The unit restarts the server after a crash (`Restart=always`), and also after an in-game `/stop`. A
watchdog halt (`max-tick-time=60000`) therefore turns into a restart. The file `spigot.yml` asks for
its own restart script `./start.sh`, which does not exist, so restarts only come from systemd.

## Ports and firewall

| Port | Protocol | Purpose | Expose |
|---|---|---|---|
| 25565 | TCP | Game | Yes |
| 24454 | UDP | Simple Voice Chat | Yes |
| 65535 | TCP | RCON | Never |
| 22 | TCP | SSH | Admins only |

All three server listeners bind every interface. The last boot on the build machine logged
`Starting Minecraft server on 0.0.0.0:25565`, `RCON running on 0.0.0.0:65535` and
`Voice chat server started at 0.0.0.0:24454` (`logs/2026-07-12-1.log.gz`). RCON binds to `server-ip`,
which is `0.0.0.0` (`server.properties.example:48`), so only the firewall keeps it private. Voice chat
reads its port from `config/voicechat/voicechat-server.properties:9` and leaves the bind address empty.

```bash
ufw allow 22/tcp
ufw allow 25565/tcp
ufw allow 24454/udp
ufw enable    # The default policy denies the rest, RCON 65535 included
```

Forge also advertises the server on the LAN every 1.5 seconds, because
`world/serverconfig/forge-server.toml:19` sets `advertiseDedicatedServerToLan = true`. A public server
does not need that. On Linux the game socket uses epoll (`Using epoll channel type` in the boot log).

## DNS

The domain skytekx.com serves the website through the Cloudflare proxy. On 2026-10-31 it had no
`_minecraft._tcp` SRV record and no `mc.` or `play.` host, and TCP 25565 on it timed out. A Minecraft
client given `skytekx.com` cannot connect, although the website and the README say it can.

A public server needs a DNS-only (unproxied) record for its address, for example `mc.skytekx.com`, and
an SRV record that points the bare domain at it.

```text
_minecraft._tcp.skytekx.com.  SRV  0 5 25565 mc.skytekx.com.
```

Behind a home router, forward 25565/tcp and 24454/udp to the host.

## Enable the unit

```bash
cd /opt/minecraft-server/server/skytekx3-1201-server
ln -s "$PWD/systemd/skytekx3.service" /etc/systemd/system/
systemctl daemon-reload
systemctl enable --now skytekx3.service
systemctl status skytekx3.service
```

## History

- The old page put `unit.conf` next to the repo. The unit reads it from the repo root.
- The old page and the README told operators to download a stock Arclight jar and to fill `mods/` with
  `tools/resolve_all.py`. The first loses the crash fix in the hardened jar, the second installs the
  wrong mod versions.
- The old firewall rules opened only 22/tcp and 25565/tcp, which leaves voice chat unreachable.

Related: [Arclight runtime](arclight.md), [Mac mini (macOS, launchd) deployment](macos.md),
[Secrets](secrets.md), [World reset](world-reset.md), [Logs and crashes](logs-and-crashes.md).
