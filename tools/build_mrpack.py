#!/usr/bin/env python3
"""Assemble SkyTekx3.mrpack (client+server) from the committed modrinth.index.json.
- reads modrinth.index.json as committed (never rewrites it) and checks versionId/forge against
  VERSION/FORGE below
- splits client-only jars out of the server mods/ dir
- zips overrides (config, kubejs, defaultconfigs) + index into the .mrpack
"""
import json, os, shutil, zipfile, sys

REPO = "/home/snekmin/git/skytekx3-1201-server"
VERSION = "3.1.0"
FORGE = "47.4.6"

# --- permission guard: every bundled jar needs modrinth-permissions/<jar>.md with Status OK ---
# Runs before anything is written or moved. --allow-unpermitted builds a private/draft pack under a
# separate name (SkyTekx3-<ver>-UNPERMITTED.mrpack), which publish_modrinth.py never picks up; it also
# re-checks the jars inside whatever .mrpack it uploads (tools/mrpack_permissions.py).
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import mrpack_permissions

out = REPO+f"/SkyTekx3-{VERSION}.mrpack"
bad = mrpack_permissions.check(REPO, mrpack_permissions.local_mod_jars(REPO))
if bad:
    print("local-mods/ jars without distribution permission (modrinth-permissions/ Status is not OK):")
    for fn, why in bad:
        print("  %s -- %s" % (fn, why))
    if "--allow-unpermitted" not in sys.argv:
        if os.path.exists(out):
            os.remove(out)  # leave nothing uploadable behind from an earlier build
            print("removed the existing", out)
        sys.exit("refusing to build a public .mrpack; resolve them first, or pass --allow-unpermitted "
                 "for a private/draft build that must not be uploaded")
    out = REPO+f"/SkyTekx3-{VERSION}-UNPERMITTED.mrpack"
    print("--allow-unpermitted: building a private/draft pack as", out, "-- do NOT upload it")

# --- mod file list: always the committed index ---
# The index is the source of truth and is shipped as committed. (Until 3.1.0 a resolve_all.py scratch
# list silently replaced it and was written back over the index, dropping every index-only entry.)
INDEX = REPO + "/modrinth.index.json"
index = json.load(open(INDEX, encoding="utf-8"))
files = index["files"]
paths = [f["path"] for f in files]
problems = []
dups = sorted({p for p in paths if paths.count(p) > 1})
if dups:
    problems.append("duplicate paths: %s" % dups)
if index.get("versionId") != VERSION:
    problems.append("versionId %r != VERSION %r (bump them together)" % (index.get("versionId"), VERSION))
if index.get("dependencies", {}).get("forge") != FORGE:
    problems.append("dependencies.forge %r != FORGE %r" % (index.get("dependencies", {}).get("forge"), FORGE))
if problems:
    sys.exit("modrinth.index.json does not match this build: " + "; ".join(problems))

# --- split client-only jars out of the server mods/ dir ---
clientonly = REPO+"/client-only-mods"
os.makedirs(clientonly, exist_ok=True)
moved=[]
for f in files:
    if f["env"]["server"] == "unsupported":
        fn = os.path.basename(f["path"])
        src = os.path.join(REPO, "mods", fn)
        if os.path.exists(src):
            shutil.move(src, os.path.join(clientonly, fn)); moved.append(fn)
print("client-only moved out of server mods/:", len(moved), moved)

# --- assemble the .mrpack (out is set by the permission guard above) ---
if os.path.exists(out): os.remove(out)
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    z.write(REPO+"/modrinth.index.json", "modrinth.index.json")
    # overrides: config, kubejs, defaultconfigs
    for sub in ("config", "kubejs", "defaultconfigs"):
        base = os.path.join(REPO, sub)
        if not os.path.isdir(base): continue
        for root, _, fns in os.walk(base):
            for fn in fns:
                full = os.path.join(root, fn)
                rel = os.path.relpath(full, REPO)
                z.write(full, "overrides/"+rel)
    # Skylands dimension datapacks -> ship into the server's world/datapacks/
    # shared pack is vanilla safe, forge pack carries the mod integrations
    for src, name in (("skylands-datapack", "skytekx3_skylands"),
                      ("skylands-datapack-forge", "skytekx3_skylands_forge")):
        dp = os.path.join(REPO, src)
        if not os.path.isdir(dp): continue
        for root, _, fns in os.walk(dp):
            for fn in fns:
                full = os.path.join(root, fn)
                rel = os.path.relpath(full, dp)
                z.write(full, "overrides/world/datapacks/"+name+"/"+rel)
    # Local/bundled mod jars (built from source, e.g. Twilight Forest) -> overrides/mods/
    lm = os.path.join(REPO, "local-mods")
    bundled = []
    if os.path.isdir(lm):
        for fn in sorted(os.listdir(lm)):
            if fn.endswith(".jar"):
                z.write(os.path.join(lm, fn), "overrides/mods/"+fn)
                bundled.append(fn)
    print("bundled local mods:", bundled)
size = os.path.getsize(out)
print(f"WROTE {out} ({size//1024} KiB) | {len(files)} mod files | client-only {len(moved)}")
print("server mods/ jar count:", len([n for n in os.listdir(REPO+'/mods') if n.endswith('.jar')]))
