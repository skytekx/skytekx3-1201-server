---
type: Reference
title: Secrets
description: Where credentials live — ansible vault, gitignored credential files, RCON.
tags: [server, secrets, security]
timestamp: 2026-11-02
---

# Secrets

**Nothing secret is committed.** Real values live in the ansible vault; credential-bearing runtime
files are gitignored.

## Ansible vault
- `ansible/group_vars/all/vault.yml` is ansible-vault encrypted (AES256). It holds `vault_modrinth_pat`
  — the Modrinth PAT that `tools/publish_modrinth.py` uses to [publish the pack](../modpack/publish.md)
  (the legacy `tools/resolve_all.py` takes it too). Assembling [`mods/`](../modpack/mod-set.md) from the
  index needs no token.
- The vault password is **never committed**: put it in `.vault_pass.txt` (gitignored);
  `ansible/ansible.cfg` points to it.
- View: `ansible-vault view ansible/group_vars/all/vault.yml`.

## Gitignored credential files
- `server.properties` — carries the RCON password. Only `server.properties.example` is committed.
- `unit.conf` — the systemd `EnvironmentFile` with `RCON_PW=…` (see [deploy](deploy.md)).

## RCON
- Port **65535**, **loopback only** — never open it in [ufw](deploy.md). Used by mcrcon for graceful stop.

Related: [deploy](deploy.md), [publish](../modpack/publish.md).
