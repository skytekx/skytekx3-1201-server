#!/usr/bin/env python3
"""Assemble SkyTekx3-<version>.mrpack (client and server) from the committed tree.
- writes modrinth.index.json from the committed index with this version, summary and Forge
- moves client-only jars out of the server mods/ dir
- zips the index and the overrides, all read from git HEAD, so edits that are not committed
  (configs rewritten by a server boot, for example) stay out of the pack
- writes the server list of the clients as client-overrides/servers.dat
"""
import io, json, os, shutil, struct, subprocess, tarfile, zipfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
VERSION = "3.0.8"
FORGE = "47.4.18"   # The Forge build that the server (inside Arclight) and the owner's client run

def git(*args):
    return subprocess.run(["git", "-C", REPO, *args], check=True, capture_output=True).stdout

# Mod file list from the committed index, deduped by path (last wins)
files = list({f["path"]: f for f in json.loads(git("show", "HEAD:modrinth.index.json"))["files"]}.values())

index = {
    "formatVersion": 1,
    "game": "minecraft",
    "versionId": VERSION,
    "name": "SkyTekx3",
    "summary": "Hexxit × Tekkit × SkyFactory, reborn on Minecraft 1.20.1.",   # The tagline of skytekx.com
    "files": files,
    "dependencies": {"minecraft": "1.20.1", "forge": FORGE},
}
index_json = json.dumps(index, indent=2) + "\n"   # trailing newline, as update_mods.py writes it
with open(REPO+"/modrinth.index.json", "w") as fh:
    fh.write(index_json)

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

# --- overrides: (folder in git, folder in the pack) ---
# The datapacks go to the server's world/datapacks/. The shared Skylands pack is vanilla safe, the
# forge pack carries the mod integrations, and the fixes pack replaces broken data of other mods.
# Bundled jars (built from source or not on Modrinth) go to mods/.
OVERRIDES = [
    ("config", "config"), ("kubejs", "kubejs"), ("defaultconfigs", "defaultconfigs"),
    ("skylands-datapack", "world/datapacks/skytekx3_skylands"),
    ("skylands-datapack-forge", "world/datapacks/skytekx3_skylands_forge"),
    ("world/datapacks/skytekx3_fixes", "world/datapacks/skytekx3_fixes"),
    ("local-mods", "mods"),
    # Sources of bundled LGPL ports, shipped next to their jars as the license asks.
    ("licenses", "licenses"),
]
# The Chunky task records are tracked by mistake and only hold the build machine's pregen state.
SKIP = ("config/chunky/tasks/",)
present = [src for src, _ in OVERRIDES if git("ls-tree", "--name-only", "HEAD", "--", src).strip()]
tree = tarfile.open(fileobj=io.BytesIO(git("archive", "--format=tar", "HEAD", "--", *present)))

def pack_path(rel):
    if rel.startswith(SKIP): return None
    for src, dst in OVERRIDES:
        if rel.startswith(src+"/"):
            if src == "local-mods" and not rel.endswith(".jar"): return None
            return "overrides/"+dst+rel[len(src):]
    return None

# --- client-overrides: the server list, so a new instance shows the server right away ---
# Modflared (in the index) opens the Cloudflare Tunnel behind this address. The server never reads it.
SERVERS = [("SkyTekx", "hex.skytekx.com")]

def nbt_str(text):
    b = text.encode("utf-8")
    return struct.pack(">H", len(b)) + b

def servers_dat(entries):
    """servers.dat is uncompressed NBT: a root compound with a list "servers" of compounds {name, ip}."""
    out = b"\x0a" + nbt_str("") + b"\x09" + nbt_str("servers") + b"\x0a" + struct.pack(">i", len(entries))
    for name, ip in entries:
        out += b"\x08" + nbt_str("name") + nbt_str(name) + b"\x08" + nbt_str("ip") + nbt_str(ip) + b"\x00"
    return out + b"\x00"

# --- assemble the .mrpack ---
out = REPO+f"/SkyTekx3-{VERSION}.mrpack"
if os.path.exists(out): os.remove(out)
counts = {}
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("modrinth.index.json", index_json)
    for m in sorted((m for m in tree.getmembers() if m.isfile()), key=lambda m: m.name):
        dst = pack_path(m.name)
        if not dst: continue
        z.writestr(dst, tree.extractfile(m).read())
        top = "/".join(dst.split("/")[1:4]) if dst.startswith("overrides/world/") else dst.split("/")[1]
        counts[top] = counts.get(top, 0) + 1
    z.writestr("client-overrides/servers.dat", servers_dat(SERVERS))
print("overrides from git HEAD:", counts)
print("client-overrides/servers.dat:", SERVERS)
size = os.path.getsize(out)
print(f"WROTE {out} ({size} bytes) | {len(files)} mod files | client-only {len(moved)}")
print("server mods/ jar count:", len([n for n in os.listdir(REPO+'/mods') if n.endswith('.jar')]))
