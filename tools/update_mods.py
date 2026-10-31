#!/usr/bin/env python3
"""Update the Modrinth mods of SkyTekx3 to their newest Forge 1.20.1 builds.

    python3 tools/update_mods.py                      # dry run: list available updates
    python3 tools/update_mods.py --apply              # update modrinth.index.json and swap the jars
    python3 tools/update_mods.py --channels release   # only release builds (default: release,beta,alpha)
    python3 tools/update_mods.py --apply --add-deps   # also add new required dependencies to the pack

With --apply every updated entry of modrinth.index.json gets the new path, hashes, size and download URL
(its env stays), the new jar is downloaded and sha512 verified into mods/ (or client-only-mods/ when the
server side is unsupported) and the old jar is removed. Afterwards run tools/patch_mods.py, then boot test.
Required dependencies of the new versions that the pack does not contain yet are reported, and with
--add-deps they are added too (newest Forge 1.20.1 build, env taken from the project's client and server side).
Uses only public Modrinth endpoints, no token needed.
"""
import argparse, hashlib, json, os, sys, urllib.parse, urllib.request

REPO = os.path.normpath(os.path.join(os.path.dirname(__file__), ".."))
INDEX = os.path.join(REPO, "modrinth.index.json")
API = "https://api.modrinth.com/v2"
UA = {"User-Agent": "skytekx/skytekx3-1201-server update_mods (+https://github.com/skytekx)", "Content-Type": "application/json"}


def call(path, body=None):
    req = urllib.request.Request(API + path, headers=UA, method="POST" if body is not None else "GET",
                                 data=json.dumps(body).encode() if body is not None else None)
    return json.load(urllib.request.urlopen(req, timeout=60))


def pick_file(version):
    files = version.get("files", [])
    forge = [f for f in files if "neoforge" not in f["filename"].lower() and "fabric" not in f["filename"].lower()]
    for pool in (forge, files):
        for f in pool:
            if f.get("primary"):
                return f
        if pool:
            return pool[0]
    return None


def download(url, dest, sha512):
    data = urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": UA["User-Agent"]}), timeout=300).read()
    if hashlib.sha512(data).hexdigest() != sha512:
        raise RuntimeError(f"sha512 mismatch for {url}")
    tmp = dest + ".part"
    with open(tmp, "wb") as f:
        f.write(data)
    os.replace(tmp, dest)


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--apply", action="store_true")
    ap.add_argument("--channels", default="release,beta,alpha")
    ap.add_argument("--add-deps", action="store_true")
    args = ap.parse_args()
    channels = set(args.channels.split(","))

    index = json.load(open(INDEX))
    files = index["files"]
    hashes = [f["hashes"]["sha1"] for f in files]
    current = call("/version_files", {"hashes": hashes, "algorithm": "sha1"})
    latest = call("/version_files/update", {"hashes": hashes, "algorithm": "sha1",
                                            "loaders": ["forge"], "game_versions": ["1.20.1"]})
    pack_projects = {v["project_id"] for v in current.values()}
    titles = {}
    pids = sorted(pack_projects)
    for i in range(0, len(pids), 100):
        for p in call("/projects?ids=" + urllib.parse.quote(json.dumps(pids[i:i + 100]))):
            titles[p["id"]] = p["title"]

    updates = []
    for entry in files:
        h = entry["hashes"]["sha1"]
        cur, new = current.get(h), latest.get(h)
        if not cur or not new or new["id"] == cur["id"] or new["version_type"] not in channels:
            continue
        f = pick_file(new)
        if not f:
            print(f"!! no usable file in {titles.get(cur['project_id'])} {new['version_number']}")
            continue
        updates.append((entry, cur, new, f))

    missing_deps = []
    for entry, cur, new, f in updates:
        for d in new.get("dependencies", []):
            if d.get("dependency_type") == "required" and d.get("project_id") and d["project_id"] not in pack_projects:
                missing_deps.append((titles.get(cur["project_id"], cur["project_id"]), d["project_id"], d.get("version_id")))

    print(f"{len(updates)} updates ({','.join(sorted(channels))}) of {len(files)} index files")
    for entry, cur, new, f in sorted(updates, key=lambda u: titles.get(u[1]["project_id"], "").lower()):
        print(f"  {titles.get(cur['project_id'], '?')[:36]:36s} {cur['version_number'][:26]:26s} -> "
              f"{new['version_number'][:28]:28s} {new['version_type']:7s} {f['filename']}")
    if missing_deps:
        print("Required dependencies NOT in the pack:")
        for who, pid, vid in missing_deps:
            print(f"  {who}: project {pid} (version {vid})")
    if not args.apply:
        print("Dry run. Re-run with --apply to update the index and the jars.")
        return

    for entry, cur, new, f in updates:
        old_name = os.path.basename(entry["path"])
        side_dir = "client-only-mods" if entry.get("env", {}).get("server") == "unsupported" else "mods"
        dest = os.path.join(REPO, side_dir, f["filename"])
        os.makedirs(os.path.dirname(dest), exist_ok=True)
        download(f["url"], dest, f["hashes"]["sha512"])
        if old_name != f["filename"]:
            for d in ("mods", "client-only-mods"):
                p = os.path.join(REPO, d, old_name)
                if os.path.exists(p):
                    os.remove(p)
        entry["path"] = "mods/" + f["filename"]
        entry["hashes"] = {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]}
        entry["downloads"] = [f["url"]]
        entry["fileSize"] = f["size"]
        print(f"  updated {old_name} -> {side_dir}/{f['filename']}")

    if args.add_deps:
        for who, pid, vid in missing_deps:
            if any(e.get("_added_pid") == pid for e in files):
                continue
            proj = call(f"/project/{pid}")
            q = urllib.parse.quote
            versions = call(f"/project/{pid}/version?loaders={q(json.dumps(['forge']))}&game_versions={q(json.dumps(['1.20.1']))}")
            version = next((v for v in versions if v["id"] == vid), None) if vid else None
            version = version or (versions[0] if versions else None)
            f = pick_file(version) if version else None
            if not f:
                print(f"!! dependency {proj['title']} has no Forge 1.20.1 build, add it by hand")
                continue
            env = {s: (proj.get(f"{s}_side") if proj.get(f"{s}_side") in ("required", "optional", "unsupported") else "required")
                   for s in ("client", "server")}
            side_dir = "client-only-mods" if env["server"] == "unsupported" else "mods"
            download(f["url"], os.path.join(REPO, side_dir, f["filename"]), f["hashes"]["sha512"])
            files.append({"path": "mods/" + f["filename"],
                          "hashes": {"sha1": f["hashes"]["sha1"], "sha512": f["hashes"]["sha512"]},
                          "env": env, "downloads": [f["url"]], "fileSize": f["size"], "_added_pid": pid})
            print(f"  added dependency {proj['title']} {version['version_number']} ({who} needs it) env={env}")
        for e in files:
            e.pop("_added_pid", None)

    with open(INDEX, "w") as fh:
        json.dump(index, fh, indent=2)
        fh.write("\n")
    print(f"Wrote {INDEX}. Next: python3 tools/patch_mods.py, then a boot test.")


if __name__ == "__main__":
    main()
