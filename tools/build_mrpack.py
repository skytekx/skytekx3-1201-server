#!/usr/bin/env python3
"""Assemble SkyTekx3-<version>.mrpack (client and server) from the committed tree.
- refuses to build a public pack while a bundled jar in local-mods/ lacks distribution permission
  (modrinth-permissions/<jar>.md with Status OK), see tools/mrpack_permissions.py
- reads modrinth.index.json as committed (never rewrites it) and checks versionId/forge against
  VERSION/FORGE below
- moves client-only jars out of the server mods/ dir
- zips the index and the overrides, all read from git HEAD, so edits that are not committed
  (configs rewritten by a server boot, for example) stay out of the pack
- fetches the jars that local-mods/ names by a pointer file <jar>.fetch.json instead of holding them
  (tools/fetch_local_mods.py) into a cache, outside the tree unless $SKYTEKX3_FETCH_CACHE points into
  it, which the tools refuse unless git ignores that path. It checks their size and SHA-1 against the
  pointer and bundles them exactly like tracked jars. A failed download or check refuses the build,
  and so does a commit reachable from HEAD that holds such a jar, since a push would repost it
  (--allow-unpermitted builds the private/draft pack anyway)
- patches the bundled jars that tools/patch_mods.py lists in PACK_RULES (patch_mods.py read from git
  HEAD too) and ships each under the file name that pack_jar_name gives it, so local-mods/ keeps the
  pristine jars and players get the bytes the server runs
- writes the server list of the clients as client-overrides/servers.dat
"""
import io, json, os, shutil, struct, subprocess, sys, tarfile, types, zipfile

REPO = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
VERSION = "3.1.4"
FORGE = "47.4.18"   # The Forge build that the server (inside Arclight) and the owner's client run

def git(*args):
    return subprocess.run(["git", "-C", REPO, *args], check=True, capture_output=True).stdout

# --- bundled jars that the pack ships patched: PACK_RULES of tools/patch_mods.py as committed ---
# Loaded first, because the permission guard checks the names the jars ship under. The server patches
# its mods/ with the same rules, so both get the same bytes. Loading it has no side effects.
patch_mods = types.ModuleType("patch_mods")
patch_mods.__file__ = os.path.join(REPO, "tools", "patch_mods.py")
exec(compile(git("show", "HEAD:tools/patch_mods.py"), "HEAD:tools/patch_mods.py", "exec"), patch_mods.__dict__)
if not all(hasattr(patch_mods, f) for f in ("patch_for_pack", "pack_jar_name")):
    sys.exit("tools/patch_mods.py in git HEAD has no patch_for_pack or pack_jar_name, commit it together with this script")

# --- permission guard: every bundled jar needs modrinth-permissions/<jar>.md with Status OK ---
# Runs before anything is written or moved. <jar> is the name the jar ships under, the name
# publish_modrinth.py finds in the .mrpack too. --allow-unpermitted builds a private/draft pack under a
# separate name (SkyTekx3-<ver>-UNPERMITTED.mrpack), which publish_modrinth.py never picks up; it also
# re-checks the jars inside whatever .mrpack it uploads (tools/mrpack_permissions.py).
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import fetch_local_mods
import mrpack_permissions

out = REPO+f"/SkyTekx3-{VERSION}.mrpack"
bad = mrpack_permissions.check(REPO, [patch_mods.pack_jar_name(fn) for fn in mrpack_permissions.local_mod_jars(REPO)])
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
# Read from git HEAD like the overrides, so an uncommitted edit cannot slip into the pack.
index_json = git("show", "HEAD:modrinth.index.json").decode("utf-8")
index = json.loads(index_json)
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
# A jar that PACK_RULES ships patched from local-mods/ must not come from the index too.
twice = [p for p in paths if p.startswith("mods/") and patch_mods.pack_jar_name(p[5:]) != p[5:]]
if twice:
    problems.append("the index lists %s, which PACK_RULES ships patched from local-mods/" % twice)
if problems:
    sys.exit("modrinth.index.json does not match this build: " + "; ".join(problems))

# --- bundled jars that git names by a pointer instead of holding them (tools/fetch_local_mods.py) ---
# Their license allows them in the .mrpack but not as a copy in the public repository (ZeroCore 2).
# The pointers come from git HEAD like every override. Each jar comes from the cache or its URL and
# must match the size and SHA-1 of its pointer, or the build stops before it moves or writes anything.
# No commit reachable from HEAD may hold the jar itself, at any path: the license forbids that copy,
# and pushing HEAD would publish all of them (git log --find-object with the blob id of the jar).
pointed = {}
held = []
for rel in git("ls-tree", "-r", "-z", "--name-only", "HEAD", "--", "local-mods").decode("utf-8").split("\0"):
    if not rel.endswith(fetch_local_mods.SUFFIX):
        continue
    try:
        p = fetch_local_mods.parse(os.path.basename(rel), git("show", "HEAD:" + rel))
        name = os.path.dirname(rel) + "/" + p["file"]
        if git("ls-tree", "--name-only", "HEAD", "--", name).strip():
            raise fetch_local_mods.PointerError("git tracks %s as well, keep the jar or the pointer" % name)
        pointed[name] = fetch_local_mods.fetch(p)
    except fetch_local_mods.PointerError as e:
        sys.exit("refusing to build: %s: %s" % (rel, e))
    print("fetched %s for %s (%d bytes, SHA-1 %s, cache %s)" % (p["file"], rel, p["size"], p["sha1"],
                                                               fetch_local_mods.cache_dir()))
    commits = mrpack_permissions.history_commits(REPO, pointed[name])
    if commits is None:
        sys.exit("refusing to build: git log --find-object failed for %s" % name)
    if commits:
        held.append("git history still holds %s (blob %s) in %s, rewrite it before any push"
                    % (p["file"], mrpack_permissions._blob_id(pointed[name]), " ".join(commits)))
if held:
    print("\n".join(held))
    if "--allow-unpermitted" not in sys.argv:
        sys.exit("refusing to build: a push of this history would repost the jars above, or pass "
                 "--allow-unpermitted for a private/draft build that must not be uploaded")
    if not out.endswith("-UNPERMITTED.mrpack"):
        out = REPO+f"/SkyTekx3-{VERSION}-UNPERMITTED.mrpack"
        print("--allow-unpermitted: building a private/draft pack as", out, "-- do NOT upload it")

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
    # What bundled jars must travel with: the sources of LGPL ports and of the LittleTiles build, the
    # license text of MIT jars that carry none inside (Simply Jetpacks 2, EnderIO Legacy Machines, Flux
    # Networks, Extreme Reactors 2, SkyTekx Molten)
    # and the license texts of the parts of the Ender IO build with their NOTICE.
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

# --- the override files, with the PACK_RULES jars patched and renamed, before the .mrpack is written ---
# The pointed jars join the files of git HEAD under the path a tracked copy would have.
sources = [(m.name, lambda m=m: tree.extractfile(m).read()) for m in tree.getmembers() if m.isfile()]
sources += [(rel, lambda data=data: data) for rel, data in pointed.items()]
entries, renamed = [], []
for rel, read in sorted(sources, key=lambda s: s[0]):
    dst = pack_path(rel)
    if not dst: continue
    data = read()
    if rel.startswith("local-mods/"):
        try:
            name, data = patch_mods.patch_for_pack(os.path.basename(rel), data)
        except ValueError as e:
            sys.exit(f"PACK_RULES: {e}")
        if name != os.path.basename(rel):
            dst = os.path.dirname(dst) + "/" + name
            renamed.append(f"{rel} -> {dst}")
    entries.append((dst, data))
shipped = {}
for dst, _ in entries:
    shipped[dst] = shipped.get(dst, 0) + 1
clash = sorted(p for p, n in shipped.items() if n > 1)
if clash:
    sys.exit("two override files would land at the same path: %s" % clash)

# --- assemble the .mrpack (out is set by the permission guard above) ---
if os.path.exists(out): os.remove(out)
counts = {}
with zipfile.ZipFile(out, "w", zipfile.ZIP_DEFLATED) as z:
    z.writestr("modrinth.index.json", index_json)
    for dst, data in entries:
        z.writestr(dst, data)
        top = "/".join(dst.split("/")[1:4]) if dst.startswith("overrides/world/") else dst.split("/")[1]
        counts[top] = counts.get(top, 0) + 1
    z.writestr("client-overrides/servers.dat", servers_dat(SERVERS))
print("bundled jars patched by PACK_RULES:", renamed)
print("bundled jars fetched by pointer:", sorted(pointed))
print("overrides from git HEAD:", counts)
print("client-overrides/servers.dat:", SERVERS)
size = os.path.getsize(out)
print(f"WROTE {out} ({size} bytes) | {len(files)} mod files | client-only {len(moved)}")
print("server mods/ jar count:", len([n for n in os.listdir(REPO+'/mods') if n.endswith('.jar')]))
