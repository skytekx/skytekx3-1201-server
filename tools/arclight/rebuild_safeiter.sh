#!/usr/bin/env bash
# Rebuild arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar from the upstream Arclight Trials 6de9fec
# snapshot plus the hardened DistanceManagerMixin.arclight$safeIter (patches/arclight-trials-
# distancemanager-safeiter.patch). See okf/server/arclight.md for why it is built this way.
#
# usage: tools/arclight/rebuild_safeiter.sh <upstream-6de9fec.jar> [<asm-libraries-dir>]
#   upstream jar: https://files.hypoglycemia.icu/v1/files/arclight/branches/Trials/versions-snapshot/1.0.6-SNAPSHOT-6de9fec/forge
#                 sha256 578f58662661cd07d381319034eea329b37bda4514e3d2168f6351a0afeb1299
#   asm dir:      a libraries/org/ow2/asm folder with asm + asm-tree 9.x (an Arclight or Forge 47 server
#                 install has one; default ./libraries/org/ow2/asm)
# Needs JDK 17 (java on PATH or $JAVA), python3, unzip and git (the donor class is read from 88552a9).
set -euo pipefail
cd "$(git rev-parse --show-toplevel)"
UP="${1:?upstream 6de9fec jar}"
ASM="${2:-libraries/org/ow2/asm}"
JAVA="${JAVA:-java}"
OUT=arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar
VER=arclight-1.20.1-1.0.6-SNAPSHOT-6de9fec-safeiter2
CLS=io/izzel/arclight/common/mixin/core/server/level/DistanceManagerMixin.class

echo "578f58662661cd07d381319034eea329b37bda4514e3d2168f6351a0afeb1299  $UP" | sha256sum -c -
asm=$(ls "$ASM"/asm/9.*/asm-9.*.jar | sort -V | tail -1)
tree=$(ls "$ASM"/asm-tree/9.*/asm-tree-9.*.jar | sort -V | tail -1)

W=$(mktemp -d); trap 'rm -rf "$W"' EXIT
# Donor: the hardened method compiled from the patch, in the jar 88552a9 shipped (its class is Mojmap,
# but the transplanted method references no Minecraft members, which Transplant.java enforces).
git show 88552a9:arclight-forge-1.20.1-1.0.6-6de9fec-safeiter.jar > "$W/si.jar"
unzip -p "$UP" common.jar > "$W/up-common.jar"
unzip -p "$W/si.jar" common.jar > "$W/si-common.jar"
unzip -p "$W/up-common.jar" "$CLS" > "$W/base.class"
unzip -p "$W/si-common.jar" "$CLS" > "$W/donor.class"

"$JAVA" -cp "$asm:$tree" tools/arclight/Transplant.java "$W/base.class" "$W/donor.class" "$W/DistanceManagerMixin.class" 'arclight$safeIter'
python3 -I tools/arclight/repack.py common "$W/up-common.jar" "$W/common.jar" "$W/DistanceManagerMixin.class" "$CLS"
# A new Implementation-Version is mandatory: Arclight reuses .arclight/mod_file/<version>.jar unchecked.
python3 -I tools/arclight/repack.py outer "$UP" "$OUT" "$W/common.jar" "$VER"
ln -sfn "$OUT" arclight.jar
echo "built $OUT"
# Zip timestamps differ from the tracked jar; the content must not.
git show "HEAD:$OUT" > "$W/tracked.jar" 2>/dev/null && python3 -I - "$W/tracked.jar" "$OUT" <<'PY'
import io, sys, zipfile
def entries(path):
    outer = zipfile.ZipFile(path)
    d = {n: outer.read(n) for n in outer.namelist() if n not in ('common.jar', 'META-INF/MANIFEST.MF')}
    inner = zipfile.ZipFile(io.BytesIO(outer.read('common.jar')))
    d.update({'common.jar!' + n: inner.read(n) for n in inner.namelist()})
    return d
a, b = entries(sys.argv[1]), entries(sys.argv[2])
bad = sorted(n for n in a.keys() | b.keys() if a.get(n) != b.get(n))
print('content identical to the tracked jar' if not bad else 'DIFFERS from the tracked jar: %s' % bad[:10])
sys.exit(1 if bad else 0)
PY
