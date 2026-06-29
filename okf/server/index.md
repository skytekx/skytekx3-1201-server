---
type: Index
title: Server
description: Running, deploying and operating the Arclight server.
tags: [server, ops]
timestamp: 2026-06-29
---

# Server

The SkyTekx 3 server is **[Arclight](arclight.md)** (Forge 1.20.1 + Bukkit hybrid), deployed under
systemd and operated over RCON.

- **[Arclight runtime](arclight.md)** — the loader, the Java 17 constraint, the plugin layer.
- **[Deploy](deploy.md)** — systemd unit, `ServerLinux.sh`, mcrcon, firewall.
- **[World reset](world-reset.md)** — full regen + pregen procedure.
- **[Secrets](secrets.md)** — vault, gitignored credential files, RCON.

See [`../../README.md`](../../README.md) for the from-scratch self-hosting walkthrough.
