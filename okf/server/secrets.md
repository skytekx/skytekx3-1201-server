---
type: Reference
title: Secrets
description: Where the RCON password, the Modrinth token and the vault password live, which files carry secrets or player data, and what has to be rotated.
tags: [server, secrets, security, rcon, ansible]
timestamp: 2026-10-31
---

# Secrets

No secret value is in git. A search of every branch with `git log --all -S` found no Modrinth token and
no real RCON password, only the placeholder `__SET_FROM_ANSIBLE_VAULT__`. Secret values never go into
docs, commit messages or logs. This page only says where they are.

## Where each secret lives

| Secret | Where it lives | Who uses it |
|---|---|---|
| RCON password | The live `server.properties` on each host, `unit.conf` on a systemd host, and the vault as `vault_rcon_password` | The stop path through mcrcon or `tools/rcon.py`, and admins |
| Modrinth personal access token | The vault as `vault_modrinth_pat` | The scripts `tools/resolve_all.py` and `tools/publish_modrinth.py`, which take it as their first command line argument |
| Vault password | Outside git, meant for `ansible/.vault_pass.txt`, which does not exist yet | The `ansible-vault` tool |

The vault key names come from `systemd/unit.conf.example:2`, `server.properties.example:42` and notes
from the build sessions. Nobody can check them without the vault password. No tool reads the vault,
so the token is always passed by hand.

## The ansible vault

- The vault file is `ansible/group_vars/all/vault.yml`, encrypted with ansible-vault (AES256), tracked in
  git and kept at mode 0600.
- The config `ansible/ansible.cfg` sets `vault_password_file = .vault_pass.txt`. Ansible resolves that
  path against the folder of the config file, so the password file belongs at
  `ansible/.vault_pass.txt`. The `.vault_pass.txt` rule in `.gitignore` covers that place.
- Ansible only reads this config when it runs inside `ansible/` or when `ANSIBLE_CONFIG` points at it.
- The config also names an inventory file that does not exist, and `ansible/playbooks/` is empty. No
  playbook renders `server.properties` or `unit.conf`, although their placeholders say so. Copy the
  values by hand.

Read the vault from the repo root by typing the password at the prompt, or from inside `ansible/` once
the password file exists.

```bash
ansible-vault view --ask-vault-pass ansible/group_vars/all/vault.yml
# With ansible/.vault_pass.txt in place
cd ansible && ansible-vault view group_vars/all/vault.yml
```

## Files that carry secrets or player data

- The live `server.properties` holds `rcon.password`. Git only tracks `server.properties.example`,
  whose `rcon.password` is a placeholder (line 42).
- The systemd `unit.conf` holds `RCON_PW`, see [Deploy](deploy.md#rcon-and-mcrcon). Git ignores it.
- The files `ops.json`, `whitelist.json`, `banned-*.json`, `usercache.json` and `usernamecache.json` hold
  player names and UUIDs. They are personal rather than secret, and git ignores them.
- EssentialsX keeps player data in `plugins/Essentials/userdata/`, `usermap.bin` and `uuids.bin`. These
  are untracked but not ignored, so a careless `git add -A` would commit them. See
  [Plugins](../modpack/plugins.md).

## RCON exposure

- RCON listens on port 65535 and binds to `server-ip`, which is `0.0.0.0`. It answers on every
  interface unless a firewall blocks it. Never open 65535 in ufw on Debian (see
  [Deploy](deploy.md#ports-and-firewall)). For macOS see the
  [Mac mini (macOS, launchd) deployment](macos.md).
- The RCON protocol sends the password in clear text. Use it from the same host only, against
  `127.0.0.1`.
- The tools mcrcon (`-p`) and `tools/rcon.py` and the unit's `ExecStop` pass the password as a command
  line argument. It is visible in the process list while they run, which is acceptable on a host
  without other users.

## Exposed values to rotate

The Modrinth token and the vault password were pasted into an AI assistant prompt on 2026-06-27. They
remain in plain text in the build machine's local prompt history, and the vault password also sits in a
local memory note of that assistant. Treat both as exposed. Before the next publish, revoke the token
on Modrinth, create a new one, rekey the vault with `ansible-vault rekey` and store the new token in it.

The login password of the Mac mini's admin account was pasted into a prompt on 2026-10-31 at 16:02. It
is also the account's sudo password, and the assistant passed it to `sudo -S` on the remote command
line during the setup of that day. So it sits in the same prompt history and in that session's
transcript. Change it on the Mac mini in System Settings or with `passwd`. No file in this repo or on
the Mac mini holds it.

## Outside this repo

The skytekx.com website deploys with a Cloudflare token that is kept outside this repo on the build
machine, see [Related repos](../related.md).

## History

The old page said that nothing secret is committed, that the tools read the Modrinth token from the
vault, and that the vault password file sits in the repo root. Only the first statement holds. The
tools take the token as an argument, and the configured path resolves to `ansible/.vault_pass.txt`.

Related: [Deploy](deploy.md), [Publish](../modpack/publish.md), [Tools](../modpack/tools.md).
