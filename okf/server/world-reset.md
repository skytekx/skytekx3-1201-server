---
type: Runbook
title: World reset / regen
description: Full all-dimension regeneration + Chunky pregen, keeping the seed and datapacks.
tags: [server, world, worldgen, pregen]
timestamp: 2026-06-29
---

# World reset / regen

Run this after [worldgen / structure changes](../content/skylands-worldgen.md) so new structures appear
across every dimension. Keeps the **seed** (`level.dat`) and the **datapacks**; only chunk data is wiped.

```bash
# 1. graceful stop (RCON)
mcrcon -H localhost -P 65535 -p "$RCON_PW" "save-all flush" stop
# 2. back up first
tar czf ~/skytekx3-world-backup-prewipe.tgz -C <repo> world
# 3. wipe generated chunk data; KEEP level.dat + datapacks (+ serverconfig)
rm -rf world/region world/entities world/poi world/DIM-1 world/DIM1 world/dimensions
# 4. restart
systemctl start skytekx3.service        # or ./ServerLinux.sh
# 5. pregen overworld + skylands (radius 1000 ≈ 16,129 chunks each)
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky world minecraft:overworld" "chunky radius 1000" "chunky start"
mcrcon -H localhost -P 65535 -p "$RCON_PW" "chunky world skytekx3:skylands"   "chunky radius 1000" "chunky start"
```

`world/dimensions/` holds every modded dim (aether, blue_skies, twilightforest, ad_astra, ae2,
bloodmagic, dimdoors, irons_spellbooks, mocreatures, **skytekx3/skylands**, …); they all regen.
Non-pregenned dims regenerate on first entry.

## Gotchas
- **Shutdown can hang** ~60 s ("threads not shutting down… force exiting"). If it does, SIGKILL the
  java pid as a fallback, then confirm it's gone before relaunching.
- **Always pregen after a wipe** — otherwise the first players pay the full worldgen cost.

Related: [deploy](deploy.md), [skylands worldgen](../content/skylands-worldgen.md), [aether fall](../content/aether-fall.md).
