#!/usr/bin/env python3
"""Assemble SkyTekx3.mrpack (client+server) from resolved Modrinth metadata.
- writes modrinth.index.json
- splits client-only jars out of the server mods/ dir
- zips overrides (config, kubejs, defaultconfigs) + index into the .mrpack
"""
import json, os, shutil, zipfile, sys

REPO = "/home/snekmin/git/skytekx3-1201-server"
SC = "/tmp/claude-1000/-home-snekmin/5bd3e1de-030d-4270-9929-e638c4c6f4a6/scratchpad"
VERSION = "3.0.4"
FORGE = "47.4.6"

files = json.load(open(SC+"/mrpack_files.json"))
# dedupe by path (last wins)
byp = {}
for f in files:
    byp[f["path"]] = f
files = list(byp.values())

index = {
    "formatVersion": 1,
    "game": "minecraft",
    "versionId": VERSION,
    "name": "SkyTekx3",
    "summary": "Hexxit + Tekkit + SkyFactory-style tech/magic/adventure kitchen-sink — the gen-3 rebuild of SkyTekx on 1.20.1.",
    "files": files,
    "dependencies": {"minecraft": "1.20.1", "forge": FORGE},
}
os.makedirs(REPO, exist_ok=True)
json.dump(index, open(REPO+"/modrinth.index.json", "w"), indent=2)

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

# --- assemble the .mrpack ---
out = REPO+f"/SkyTekx3-{VERSION}.mrpack"
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
    # Skylands dimension datapack -> ships into the server's world/datapacks/
    dp = os.path.join(REPO, "skylands-datapack")
    if os.path.isdir(dp):
        for root, _, fns in os.walk(dp):
            for fn in fns:
                full = os.path.join(root, fn)
                rel = os.path.relpath(full, dp)
                z.write(full, "overrides/world/datapacks/skytekx3_skylands/"+rel)
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
