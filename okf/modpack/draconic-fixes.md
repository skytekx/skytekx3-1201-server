---
type: Mod
title: Draconic Evolution fixes
description: Why the pack keeps the stock Draconic Evolution 3.1.2.621 instead of a backport of its 1.21 line, which bugs the pack's own addon SkyTekx DE Fixes 1.0.0 fixes in it through Mixin after the 1.21 branches, which two fixes the pack's KubeJS scripts apply, and how the addon stays within the Draconic Evolution license.
tags: [modpack, mods, draconicevolution, mixin]
timestamp: 2026-11-06
---

# Draconic Evolution fixes

The pack runs the stock Draconic Evolution 3.1.2.621 with Brandon's Core 3.2.1.302 and CodeChicken Lib
4.4.0.528, all three as index entries from the Modrinth CDN (`modrinth.index.json`). The owner asked
for a backport of Draconic Evolution, and the backport research of 2026-11-04 found nothing worth
porting except a handful of bug fixes (`research-result.json` of that run, entry
`draconic-evolution`, not in the repo, which quotes the request without its time). Since 2026-11-05
the pack's own addon SkyTekx DE Fixes fixes most of those bugs in the stock jars, and the pack's KubeJS
scripts fix two more.

| Property | Value |
|---|---|
| Jar | File [local-mods/skytekx_defixes-1.0.0.jar](../../local-mods/skytekx_defixes-1.0.0.jar), 21,305 bytes, SHA-1 `403e01aacc5b78e9d74ea961c4974ca68d60838b` (c93babf) |
| Mod | The id is `skytekx_defixes`, the display name "SkyTekx DE Fixes" and the author "SkyTekx". The license is MIT for our code, with portions under the Draconic Evolution license, see [license](#license) |
| Needs | Draconic Evolution exactly 3.1.2.621 and Brandon's Core exactly 3.2.1.302 (`META-INF/mods.toml` of the jar), both loaded before it |
| Sides | Required on both sides, see [sides](#sides) |
| Source | Folder [ports/skytekx-defixes/](../../ports/skytekx-defixes) in this repo, with a README that lists every fix and a NOTICE with the credits and the license terms |
| Build | Gradle 8.2.1, NeoGradle 6.0.21, Librarian 1.2.0.7-dev-SNAPSHOT, Parchment `2023.07.30-1.20.1`, MixinGradle 0.7.38, Mixin 0.8.5, Java 17, Forge 47.1.70 as the NeoForged artifact, like the Spiced Twilight port. The plugin versions are pinned |

## Why Draconic Evolution is not backported

The 1.20 branch of Draconic-Inc/Draconic-Evolution ends at 4e7fc310 of 2025-07-21, and 3.1.2.621 is
its last build, the one in the index. Every later fix went to the branch `1.21` only, whose head
6251e32b of 2026-04-09 is 78 commits ahead without merges (`git log origin/1.20..origin/1.21` in a
clone of 2026-11-05). Three reasons speak against a port of that line:

- It holds no new content. Both branches register the same 131 entries in `DEContent`, and the
  reactor, fusion crafting, the energy core, the guardian and the modules work the same. The diff of
  777 files is the API churn of the 1.20.4 and 1.21.1 ports, and 38 of its fixes repair bugs that those
  ports brought in and that 1.20.1 never had (research of 2026-11-04).
- It would break TiCEX. Its Draconic Evolution module binds the 1.20.1 module API and mixes into
  `ModuleEntity` (`ticex.draconicevolution.mixins.json` in
  `ticex-mc1.20.1-0.7.0-hotfix-all.jar`), which the 1.21 line rewrote around data components.
- The license forbids it. Draconic Evolution and Brandon's Core use the "Don't Be a Jerk" license of
  CoFH (`LICENSE.md` of both repositories). It reserves all rights to the code, forbids redistributing
  it "in its entirety" except in mod packs, and forbids any code with "50% or more Functionally
  Equivalent Statements". A modified Draconic Evolution jar in `local-mods/` would need the copyright
  holder's prior written permission. The license does allow anyone to "Copy portions of this code for use
  in other projects" and to "Write your own code that uses this code as a dependency", which it calls
  addon mods, and it demands a visible repository for such code.

So the pack ships the stock jars, which the mod pack exception covers, and puts the fixes into an addon
of its own, whose source is public in `ports/skytekx-defixes/`. Pull requests against the 1.20 branch,
which the license invites, would be a second way. None was opened.

## License

The license review of 2026-11-05 found that a first draft of the addon may break the clause against
code with "50% or more Functionally Equivalent Statements". The license defines such a statement as a
fragment that "regardless of whitespace and object names, achieves the same result". About 39 of the 70
Java statements of that draft reproduced upstream code, and all six of its data files were material of
Draconic Evolution, one of them its own recipe file with one value changed. The addon was reworked
before it reached the public repository, and the draft is in no commit of its history, so no copy of
it ships or is published. Written permission of the copyright holder would settle the question for
good, and nobody has asked for it yet, see [open items](../open-items.md#not-verified). The addon
lowers the share instead:

- The chest fix works by a mechanism of our own rather than upstream's, see the table below.
- The shift and scroll fix of the Advanced Dislocator is gone, because any version of it would
  reproduce most of upstream's statements.
- The recipe and the tool tags moved out of the jar into the pack's KubeJS scripts, so no data file of
  Draconic Evolution ships.
- The death message has an English text of our own.

The jar has 31 executable Java statements and one language entry. Of the statements, 11 achieve the
same result as an upstream statement: the block properties chain of `OpaqueMachineProperties`, the null
check of the crystal part offset, `Float.MAX_VALUE / 100` in four handlers, the Draconic tech level, the
water animal check, the `claim.xp` key and the two statements of the F1 check. That is 11 of 32 or 34
percent, counted per statement without imports, annotations, declarations without code and constants,
and per entry of a data file (`NOTICE` of the jar). `LICENSE` of the jar lists those portions as
Copyright (c) 2013-2023 brandon3055 under the Draconic Evolution license and not MIT, and `mods.toml`,
the permission file and the permission README say MIT for our code with portions under that license.

The license also says "Maintain a visible repository of your code". The jar, its `mods.toml`, its
permission file and `NOTICE` point at `tree/master/ports/skytekx-defixes` on GitHub, which holds
nothing until the branch with `ports/` is on master. Since 8d7f17d `tools/mrpack_permissions.py` refuses
the jar in a public build or upload until the local `origin/master` holds that directory and this build
of the jar. The probe that the tests use sits in `ports/skytekx-defixes/probe/`, outside the jar.

## What the fix pack changes

| Fix | Upstream | What changes on 1.20.1 |
|---|---|---|
| Draconium Chest dupe | Commit e405445b (same bug) | Every open menu of a chest had its own crafting result over the one shared grid, so a second player could take a result whose ingredients the first had used. Upstream moved the result into the chest. Our fix keeps the result in the menu, remembers the grid it came from and computes it again before any click on the server once the grid has changed. Nothing is saved, so chests keep their format |
| Insta kill damage | Commit 4683a54f | The damage of `Float.MAX_VALUE` (Chaos Crystal placed outside creative) or `MAX_VALUE / 5` (Mob Grinder, Chaos Guardian laser, /kill through modular armor) overflowed to infinity under damage multipliers such as those of Apothic Attributes. It is `MAX_VALUE / 100` now |
| Chaos Crystal crash | Commit 8ec180ab | The shape of a crystal part without a parent position threw a NullPointerException. It is the unmoved shape now |
| Chaotic Shield Capacity recipe | Commit 0c85f0d9 | The recipe takes an Awakened Core instead of a Chaotic Core. Not in the jar: a `replaceInput` in `kubejs/server_scripts/recipe_fixes.js` swaps the one ingredient |
| Tool tags | Commit 4d360c95 | The Wyvern, Draconic and Chaotic tools join the vanilla tags for pickaxes, axes, shovels, swords and hoes, and both staffs the first four. Not in the jar: `kubejs/server_scripts/tag_fixes.js` adds them |
| Souls of water animals | Commit d297c265 | Squid, fish and other water animals drop souls with the passive chance instead of the hostile one |
| Chaos Implosion message | Commit 9282fa7c | The death message has an English text instead of the raw key, "%1$s was caught in a Chaos Implosion", which is our own wording, from the mod's own language file |
| Light through two blocks | Commit 51ab0afb | The Dislocator Receptacle and the Creative OP Capacitor occlude and block light like the full cubes they are |
| Grinder label | Commit 3ae9baa2 | The Claim XP button of the Mob Grinder says "Claim XP" instead of "Collect XP". The kill box half of the commit is not needed, because the box has the right size on 1.20.1 and only grew in the 1.20.4 port |
| Draconic tool tier | Commit d6a3ceae | The Draconic tools had harvest level 3, below the Wyvern tools with 32, and the draconium speed, damage, swing speed and enchantability. They have level 128, speed 25, damage bonus 1.75 and enchantability 25 now |
| Advanced Dislocator scroll | Commit 4d6fc7bf | Not applied, see [license](#license). Shift and scroll with an Advanced Dislocator still does nothing |
| HUD in F1 | Brandon's Core commit 6d6c5a5 | Upstream hides the shield, energy and item HUDs of Draconic Evolution in F1 mode on the GUI path of 1.21. On 1.20.1 `GameRenderer.render` skips `Gui.render`, and with it the Forge `RenderGuiEvent`, while `hideGui` is set and no screen is open (javap of the mapped Forge 1.20.1-47.4.0 jar, bytecode offsets 562 to 613 of `render`). So on stock 1.20.1 the HUDs can only show in F1 mode while a screen is open, and the fix mirrors upstream there |

The links to each upstream commit and the details of the code are in the README of
`ports/skytekx-defixes/`. All commits are on the branch `1.21` of Draconic-Inc/Draconic-Evolution and
Draconic-Inc/BrandonsCore, and none is an ancestor of the 1.20 branch (checked with
`git merge-base --is-ancestor` on 2026-11-05).

The mixins follow the rules of the pack for Arclight. They target only classes of Draconic Evolution
and Brandon's Core, so none touches a Minecraft class that the Bukkit patches of Arclight change, and
none uses `@Overwrite`. They leave `ModuleEntity` alone, which TiCEX mixes into. The chest hook sits in
`DETileMenu.clicked`, the override of Draconic Evolution, not in the Minecraft menu class, and acts only
on the chest menu on the server. Every injector has
`require = 1`, so a changed target stops the boot instead of doing nothing silently. Every target was
checked with `javap` against the pinned jars, and `mods.toml` pins the exact versions, so an update of
Draconic Evolution or Brandon's Core stops the boot with a clear message until the fix pack is checked
against the new jars.

## Sides

The fix pack is required on both sides. The Draconic tool tier decides the mining speed on the client
as well as on the server. Its channel `skytekx_defixes:main` carries no messages and is there for
the login check: it accepts only its own protocol version on the other side, so by the code a
client without the mod cannot join a server with it and the other way round. No client has tried that yet. Pack 3.1.3
went live without it, so like the other new mods of the next pack after 3.1.3, the jar reaches the
server with the deploy of that pack.

## Build and update

The build in `ports/skytekx-defixes/` (`./gradlew build` with Java 17) compiles against the exact files
of the index. An ivy repository in `build.gradle` fetches them from the Modrinth CDN, and the task
`verifyPackJars` checks their SHA-1 against `gradle.properties`. The jar is byte reproducible. Two clean
builds of 9417e97 on 2026-11-05 gave the SHA-1 of the jar in `local-mods/`.

After a change of the source, rebuild, copy the jar to `local-mods/` and update the SHA-1 and size in
`modrinth-permissions/skytekx_defixes-1.0.0.jar.md`. The jar's NOTICE names the directory at the commit
that last changed the jar in `local-mods/` as its exact source. Its permission file, its `mods.toml` and
its NOTICE point at `tree/master/ports/skytekx-defixes` on GitHub, so the branch that carries `ports/`
has to be on master before a public upload, and the build tools check that, see [license](#license).
Keep the share of reproduced statements in view with every change, and recount it in `NOTICE`.

## Tests

- A Forge 47.4.18 test server with stock Draconic Evolution, Brandon's Core and CodeChicken Lib booted
  on 2026-11-05 with the probe from `ports/skytekx-defixes/probe/` and KubeJS. Two players opened one
  Draconium Chest, A took the result of one log and B clicked the stale result. With pick up, shift
  click and number key B got four more planks, which reproduced the dupe. The Draconic pickaxe had level
  3, the recipe took a Chaotic Core, both blocks let light through and the crystal shape threw the
  NullPointerException (`ia/deboot/stock/console.log` in the scratch folder of the integration, not in
  the repo).
- The same server with the jar of c93babf, TiCEX 0.7.0-hotfix and its dependencies and the pack's
  `recipe_fixes.js` and `tag_fixes.js` reached `Done` in 3.4 seconds. The probe found the dupe closed
  for all three kinds of click, a grid of two logs still crafting twice, the Awakened Core in the
  recipe, every tool and both staffs in their tags, level 128 for the Draconic pickaxe, both blocks
  opaque, the crystal shape whole, the death message "%1$s was caught in a Chaos Implosion", no key of
  the mod in the saved chest or its item, and the channel registered. KubeJS loaded both scripts with 0
  errors. The 19 ERROR lines are 18 of TiCEX, which lacks its optional mods such as Mekanism and Iron's
  Spellbooks, and one of the flat test world. Started with `-Dmixin.debug.export=true`, it exported
  `DETileMenu` and `DraconiumChestMenu` with the new handlers next to the other targets and the mixin of
  TiCEX in `ModuleEntity`.
- The full pack of 9638ffb with this jar reached `Done` on Arclight on 2026-11-05 after 15.8 seconds
  with 0 FATAL lines and the same 3 ERROR lines as the boot before the addon, see
  [mod set](mod-set.md#bundled-jars). Its debug log shows 9 of the 10 server mixins applied,
  `DETileMenuMixin` and `DraconiumChestMenuMixin` among them. The tenth, for `LaserBeamPhase`, applies
  when the first Chaos Guardian loads that class. KubeJS modified one recipe more than before, the
  Chaotic Shield Capacity.
- The 2 client mixins applied in a client data run of the Forge development environment with the first
  draft, whose client mixins are the same as in the jar. Nobody has played with the fix pack in a real
  client yet, see [open items](../open-items.md#not-verified).

Related: [mod set](mod-set.md#bundled-jars), [publish](publish.md), [SkyTekx Compat](../content/compat-mod.md),
[Arclight](../server/arclight.md).
