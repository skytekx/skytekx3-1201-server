#!/usr/bin/env python3
"""Comprehensive Modrinth resolver for SkyTekx3: records .mrpack metadata + downloads jars + resolves deps."""
import json, os, sys, time, urllib.request, urllib.error

PAT = sys.argv[1]
REPO = "/home/snekmin/git/skytekx3-1201-server"
MODS = os.path.join(REPO, "mods")
os.makedirs(MODS, exist_ok=True)
UA = "skytekx3-builder/1.0 (skytekx@erebos.xyz)"
HEAD = {"Authorization": PAT, "User-Agent": UA}

SLUGS = [
 # engine + libs
 "kubejs","rhino","architectury-api","cloth-config","polymorph","lootjs","geckolib","curios",
 "patchouli","balm","supermartijn642s-config-lib","supermartijn642s-core-lib","moonlight","collective",
 "resourceful-lib","resourceful-config","prism-lib","almostunified","cucumber","glodium","placebo","mantle",
 # perf
 "ferrite-core","modernfix","radium","rubidium","embeddium","entityculling","noisium","spark","chunky",
 "ksyxis","memoryleakfix","saturn","clumps","get-it-together-drops","packet-fixer",
 # ux
 "jade","emi","jei","xaeros-minimap","xaeros-world-map","fallingtree","mouse-tweaks","appleskin","betterf3",
 # tech / identity
 "create","createaddition","create-steam-n-rails","mekanism","ae2","botania","quark","tinkers-construct",
 "tinkers-levelling-addon","industrial-foregoing","cc-tweaked","comforts","natures-compass","waystones",
 "kleeslabs","akashic-tome","cofh-core","thermal-foundation","thermal-expansion","thermal-dynamics",
 "draconic-evolution","brandons-core","railcraft-reborn","ironchests","storagedrawers","decocraft",
 "reliquary-reincarnations","pams-harvestcraft-2-food-core","ad-astra","supplementaries",
 # adventure / curated new
 "blue-skies","when-dungeons-arise","yungs-better-dungeons","yungs-api","alexs-mobs","naturalist",
 "sophisticated-backpacks","simple-voice-chat","carry-on","apotheosis","l2hostility","l2library",
 "hexxit-gear-r","asgard-shield-reloaded","special-mobs","mo-creatures-nostalgia-edition","spartan-weaponry",
 "epic-knights-shields-armor-and-weapons","better-combat","farmers-delight","forbidden-arcanus",
 "ars-nouveau","irons-spells-n-spellbooks","aquaculture",
]

def get(u):
    return json.load(urllib.request.urlopen(urllib.request.Request(u, headers=HEAD), timeout=40))

def proj(idslug):
    return get(f"https://api.modrinth.com/v2/project/{idslug}")

def best(idslug):
    vs = get(f"https://api.modrinth.com/v2/project/{idslug}/version?loaders=%5B%22forge%22%5D&game_versions=%5B%221.20.1%22%5D")
    if not vs: return None
    rel = [v for v in vs if v.get("version_type")=="release"] or vs
    return rel[0]

def pick_forge(files):
    for f in files:
        n=f["filename"].lower()
        if "neoforge" in n or "fabric" in n: continue
        if f.get("primary"): return f
    for f in files:
        n=f["filename"].lower()
        if "neoforge" not in n and "fabric" not in n: return f
    return files[0] if files else None

index=[]; report=[]; seen=set(); queue=list(SLUGS); deps_added=[]
while queue:
    key=queue.pop(0)
    if key in seen: continue
    seen.add(key)
    rec={"key":key,"status":"","file":""}
    try:
        p=proj(key)
        pid=p["id"]
        if pid in seen and pid!=key: report.append({"key":key,"status":"dup_id"}); continue
        seen.add(pid)
        v=best(pid)
        if not v: rec["status"]="NO_FORGE_1201"; report.append(rec); print("--",key,"no forge1201"); continue
        f=pick_forge(v.get("files",[]))
        if not f: rec["status"]="NO_FILE"; report.append(rec); print("--",key,"no file"); continue
        env={"client":p.get("client_side","required"),"server":p.get("server_side","required")}
        # normalize unknown
        for s in ("client","server"):
            if env[s] not in ("required","optional","unsupported"): env[s]="required"
        index.append({
            "path":"mods/"+f["filename"],
            "hashes":{"sha1":f["hashes"]["sha1"],"sha512":f["hashes"]["sha512"]},
            "env":env,
            "downloads":[f["url"]],
            "fileSize":f["size"],
        })
        rec.update(status="OK",file=f["filename"],version=v.get("version_number"),env=env,slug=p.get("slug"),title=p.get("title"))
        report.append(rec)
        dest=os.path.join(MODS,f["filename"])
        if not os.path.exists(dest):
            with urllib.request.urlopen(urllib.request.Request(f["url"],headers={"User-Agent":UA}),timeout=180) as r, open(dest,"wb") as o:
                o.write(r.read())
            print("OK",key,"->",f["filename"],rec["version"],env)
        else:
            print("==",key,"exists",f["filename"])
        # required deps
        for d in v.get("dependencies",[]):
            if d.get("dependency_type")=="required" and d.get("project_id") and d["project_id"] not in seen:
                queue.append(d["project_id"]); deps_added.append((key,d["project_id"]))
    except urllib.error.HTTPError as e:
        rec["status"]=f"HTTP_{e.code}"; report.append(rec); print("!!",key,"HTTP",e.code)
    except Exception as e:
        rec["status"]=f"ERR_{type(e).__name__}"; report.append(rec); print("!!",key,e)
    time.sleep(0.12)

SC="/tmp/claude-1000/-home-snekmin/5bd3e1de-030d-4270-9929-e638c4c6f4a6/scratchpad"
json.dump(index, open(SC+"/mrpack_files.json","w"), indent=1)
json.dump(report, open(SC+"/resolve_report.json","w"), indent=1)
ok=[r for r in report if r["status"]=="OK"]
print(f"\n=== RESOLVED OK: {len(ok)} | index entries: {len(index)} | deps auto-added: {len(deps_added)} ===")
print("FAILED:", [r['key'] for r in report if r['status']!='OK'])
