#!/usr/bin/env bash
# Sync the canonical datapack sources into the live world datapack folder.
# Canonical: skylands-datapack/ (shared, vanilla safe) and skylands-datapack-forge/ (forge only).
# Run after every edit, the server reads world/datapacks/ at boot.
set -euo pipefail
cd "$(dirname "$0")/.."
rsync -a --delete skylands-datapack/ world/datapacks/skytekx3_skylands/
rsync -a --delete skylands-datapack-forge/ world/datapacks/skytekx3_skylands_forge/
echo "synced: skytekx3_skylands ($(find world/datapacks/skytekx3_skylands -type f | wc -l) files), skytekx3_skylands_forge ($(find world/datapacks/skytekx3_skylands_forge -type f | wc -l) files)"
