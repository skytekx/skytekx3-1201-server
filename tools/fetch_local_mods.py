#!/usr/bin/env python3
"""Fetch the bundled jars that local-mods/ names by a pointer file instead of holding them.

Some jars may ride inside the .mrpack, but their license forbids a copy of its own on another website,
and a jar in this public git repository would be one (ZeroCore 2, see
modrinth-permissions/ZeroCore2-1.20.1-2.1.48.jar.md). Git does not track such a jar. In its place it
tracks local-mods/<jar>.fetch.json:

    {"file": "<jar>", "url": "https://...", "sha1": "<40 hex digits>", "size": <bytes>,
     "source": "<the page or project the file comes from>", "reason": "<why git holds no copy>"}

tools/build_mrpack.py reads each pointer from git HEAD, fetches the jar into a cache outside the tree,
checks its size and SHA-1 and bundles it exactly as it bundles a tracked jar. This script does the same
for operators who mirror local-mods/ into mods/ by hand:

    python3 tools/fetch_local_mods.py               # put every pointed jar into local-mods/
    python3 tools/fetch_local_mods.py --dest DIR    # put them into DIR instead, mods/ for example
    python3 tools/fetch_local_mods.py --check       # only list the pointers and the jars still missing

The cache is $SKYTEKX3_FETCH_CACHE, else $XDG_CACHE_HOME/skytekx3/local-mods, else
~/.cache/skytekx3/local-mods. A cached copy counts only while its size and SHA-1 match. A pointer
that does not parse, a failed download or a wrong size or SHA-1 stops the script with exit code 1,
and no file that failed the check is written anywhere. Into the repository's own local-mods/ it writes
only jars that git ignores (.gitignore), so a pointed jar cannot slip into a commit.

Standard library only. Importing it has no side effects: build_mrpack.py, mrpack_permissions.py and
publish_modrinth.py use its functions.
"""
import argparse
import hashlib
import http.client
import json
import os
import re
import subprocess
import sys
import tempfile
import urllib.error
import urllib.request

SUFFIX = ".fetch.json"
KEYS = ("file", "url", "sha1", "size", "source", "reason")
UA = "skytekx/skytekx3-1201-server fetch_local_mods (+https://github.com/skytekx)"
TIMEOUT = 120
_SHA1 = re.compile(r"[0-9a-f]{40}")


class PointerError(Exception):
    """A pointer that does not parse, or a jar that cannot be fetched or fails its check."""


def parse(name, raw):
    """The pointer dict of the pointer file `name` (its base name) with the bytes `raw`, or
    PointerError. The jar it names must be the file name without SUFFIX."""
    if not name.endswith(".jar" + SUFFIX):
        raise PointerError("a pointer file is named <jar>%s" % SUFFIX)
    try:
        p = json.loads(raw)
    except (UnicodeDecodeError, ValueError) as e:
        raise PointerError("not valid JSON: %s" % e)
    if not isinstance(p, dict):
        raise PointerError("not a JSON object")
    missing = [k for k in KEYS if k not in p]
    unknown = sorted(set(p) - set(KEYS))
    if missing or unknown:
        raise PointerError("keys missing %s, unknown %s, it needs exactly %s" % (missing, unknown, list(KEYS)))
    if p["file"] != name[:-len(SUFFIX)]:
        raise PointerError("its file %r is not the name of the pointer without %s" % (p["file"], SUFFIX))
    if "/" in p["file"] or "\\" in p["file"] or p["file"].startswith("."):
        raise PointerError("its file %r is not a plain file name" % p["file"])
    if not isinstance(p["url"], str) or not p["url"].startswith("https://"):
        raise PointerError("its url is not an https URL")
    if not isinstance(p["sha1"], str) or not _SHA1.fullmatch(p["sha1"]):
        raise PointerError("its sha1 is not 40 lowercase hex digits")
    if type(p["size"]) is not int or p["size"] <= 0:
        raise PointerError("its size is not a positive whole number of bytes")
    for k in ("source", "reason"):
        if not isinstance(p[k], str) or not p[k].strip():
            raise PointerError("its %s is empty" % k)
    return p


def read_dir(path):
    """The parsed pointers in the folder `path` (local-mods/ on disk), sorted by jar name."""
    if not os.path.isdir(path):
        return []
    out = []
    for fn in sorted(os.listdir(path)):
        if fn.endswith(SUFFIX):
            with open(os.path.join(path, fn), "rb") as fh:
                try:
                    out.append(parse(fn, fh.read()))
                except PointerError as e:
                    raise PointerError("%s: %s" % (fn, e))
    return out


def problem(p, data):
    """None when `data` are the bytes that the pointer `p` names, else the reason they are not."""
    if len(data) != p["size"]:
        return "size %d, the pointer says %d" % (len(data), p["size"])
    sha1 = hashlib.sha1(data).hexdigest()
    if sha1 != p["sha1"]:
        return "SHA-1 %s, the pointer says %s" % (sha1, p["sha1"])
    return None


def cache_dir():
    """The download cache, always outside the repository unless the caller points it there."""
    if os.environ.get("SKYTEKX3_FETCH_CACHE"):
        return os.environ["SKYTEKX3_FETCH_CACHE"]
    base = os.environ.get("XDG_CACHE_HOME") or os.path.join(os.path.expanduser("~"), ".cache")
    return os.path.join(base, "skytekx3", "local-mods")


def _write(path, data):
    """Write `data` to `path` through a temporary file in the same folder, so no half file is left.
    The file gets the mode of a plain new file (0666 less the umask), not the 0600 of mkstemp."""
    os.makedirs(os.path.dirname(path), exist_ok=True)
    fd, tmp = tempfile.mkstemp(dir=os.path.dirname(path), prefix=".fetch-")
    try:
        with os.fdopen(fd, "wb") as fh:
            fh.write(data)
        umask = os.umask(0)
        os.umask(umask)
        os.chmod(tmp, 0o666 & ~umask)
        os.replace(tmp, path)
    except BaseException:
        if os.path.exists(tmp):
            os.remove(tmp)
        raise


def fetch(p, cache=None):
    """The checked bytes of the jar that the pointer `p` names, from the cache or downloaded into it.
    Raises PointerError when the download fails or its size or SHA-1 differ from the pointer."""
    path = os.path.join(cache or cache_dir(), p["sha1"], p["file"])
    if os.path.isfile(path):
        with open(path, "rb") as fh:
            data = fh.read()
        if problem(p, data) is None:
            return data
        # A damaged copy, or a pointer that is wrong itself. Only a checked download replaces it.
    req = urllib.request.Request(p["url"], headers={"User-Agent": UA})
    try:
        with urllib.request.urlopen(req, timeout=TIMEOUT) as r:
            data = r.read(p["size"] + 1)  # One byte more than the pointer allows shows a wrong file
    except (urllib.error.URLError, http.client.HTTPException, OSError, ValueError) as e:
        raise PointerError("download of %s failed: %s" % (p["url"], e))
    why = problem(p, data)
    if why:
        raise PointerError("%s from %s has %s" % (p["file"], p["url"], why))
    _write(path, data)
    return data


def _ignored(repo, rel):
    """True when git ignores `rel` in `repo`, False when it would track it, None without git."""
    try:
        r = subprocess.run(["git", "-C", repo, "check-ignore", "-q", "--", rel], capture_output=True)
    except OSError:
        return None
    return {0: True, 1: False}.get(r.returncode)


def main(argv=None):
    repo = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
    local_mods = os.path.join(repo, "local-mods")
    ap = argparse.ArgumentParser(description="Fetch the jars that local-mods/ names by a pointer file.")
    ap.add_argument("--dest", help="folder for the jars (default: local-mods/ of this repository)")
    ap.add_argument("--cache", help="download cache (default: %s)" % cache_dir())
    ap.add_argument("--check", action="store_true", help="only list the pointers and the jars still missing")
    args = ap.parse_args(argv)
    dest = os.path.abspath(args.dest or local_mods)
    try:
        pointers = read_dir(local_mods)
    except PointerError as e:
        sys.exit("fetch_local_mods: %s" % e)
    if not pointers:
        print("fetch_local_mods: no pointer files in", local_mods)
        return 0
    in_repo = os.path.realpath(dest) == os.path.realpath(local_mods)
    failed = 0
    for p in pointers:
        target = os.path.join(dest, p["file"])
        have = None
        if os.path.isfile(target):
            with open(target, "rb") as fh:
                have = problem(p, fh.read())
        if os.path.isfile(target) and have is None:
            print("ok       %s (%d bytes, SHA-1 %s)" % (target, p["size"], p["sha1"]), flush=True)
            continue
        if args.check:
            print("missing  %s%s" % (target, " (%s)" % have if have else ""), flush=True)
            failed += 1
            continue
        if in_repo and _ignored(repo, "local-mods/" + p["file"]) is False:
            print("refused  %s: git does not ignore local-mods/%s, add it to .gitignore first"
                  % (target, p["file"]), file=sys.stderr)
            failed += 1
            continue
        try:
            data = fetch(p, args.cache)
        except PointerError as e:
            print("failed   %s: %s" % (target, e), file=sys.stderr)
            failed += 1
            continue
        _write(target, data)
        print("%s %s (%d bytes, SHA-1 %s) from %s"
              % ("replaced" if have else "fetched ", target, p["size"], p["sha1"], p["url"]), flush=True)
    if failed:
        print("fetch_local_mods: %d of %d pointers %s" % (failed, len(pointers), "missing" if args.check else "failed"),
              file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
