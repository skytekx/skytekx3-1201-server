---
type: Runbook
title: Deploy
description: systemd unit, ServerLinux.sh launcher, mcrcon, and firewall for the Arclight server.
tags: [server, deploy, systemd, rcon]
timestamp: 2026-06-29
---

# Deploy

Runs **without `screen`** — under systemd, controlled over RCON. Full host walkthrough:
[`../../README.md`](../../README.md#self-hosting-the-server).

## Layout
- Server lives under `/opt/minecraft-server/server/skytekx3-1201-server`, owned by an unprivileged
  `minecraft` system user.
- [`../../systemd/skytekx3.service`](../../systemd/skytekx3.service) — `User=minecraft`, `Nice=-20`,
  **Java 17** (`/usr/lib/jvm/java-17-openjdk`), Aikar G1GC flags, 11 G heap, `-jar arclight.jar nogui`.
- [`../../ServerLinux.sh`](../../ServerLinux.sh) — the dev/foreground launcher (same flags; `JAVA`/`JAR`/`MEM` env-overridable).

## RCON
- The unit's `ExecStop` is `mcrcon -H localhost -P 65535 -p ${RCON_PW} stop`.
- `${RCON_PW}` comes from an `unit.conf` `EnvironmentFile` (gitignored) next to the repo:
  `RCON_PW=<your rcon password>`. See [secrets](secrets.md).
- Build mcrcon once: `git clone https://github.com/Tiiffi/mcrcon.git && cd mcrcon && gcc mcrcon.c -o mcrcon`.

## Firewall
`ufw allow 22/tcp`, `ufw allow 25565/tcp`, then `ufw enable`. RCON (65535) stays loopback-only — never expose it.

## Enable
```bash
ln -s "$PWD/systemd/skytekx3.service" /etc/systemd/system/
systemctl daemon-reload && systemctl enable --now skytekx3.service
```

Related: [Arclight runtime](arclight.md), [world reset](world-reset.md), [secrets](secrets.md).
