---
type: Reference
title: Create Integrated Farming port
description: The Forge 1.20.1 port of Create Integrated Farming 1.4.3 in ports/createintegratedfarming, what it and its library Create Dragons Plus add to the pack, what was left out, its license duties, the tests of 2026-11-05 on Forge and Arclight, the roost seed fix of 2026-11-06 and the steps for the server.
tags: [modpack, mods, create, port, farming, createdragonsplus, arclight]
timestamp: 2026-11-06
---

# Create Integrated Farming port

Create: Integrated Farming (mod id `create_integrated_farming`) is a Create addon by DragonsPlus with
roosts for chickens, fishing nets for contraptions and the Vacuum Harvester. Upstream builds it only for
NeoForge 1.21.1. The fork in [ports/createintegratedfarming/](../../ports/createintegratedfarming) ports
release 1.4.3 to Minecraft 1.20.1 with Forge 47 and Create 6.0.8 for the pack. It keeps every registry
id, block state, NBT key and config key of 1.4.3. The work is done for SkyTekx, a modpack by ts4.party.

| Property | Value |
|---|---|
| Upstream | The DragonsPlusMinecraft/CreateIntegratedFarming repository, branch `1.21.1/6.0.0-dev` at 1f63780 (2026-09-21, release 1.4.3). All 285 Java files of the Modrinth sources jar of 1.4.3 are byte identical to that commit (`UPSTREAM.md`) |
| Fork | Folder `ports/createintegratedfarming/`, Java 17, ForgeGradle 6.0.6, Forge 47.4.0 userdev, Create 6.0.8-289 (`UPSTREAM.md`, Build changes) |
| Version | Version 1.4.3-skytekx.1 in `create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar` |
| Ships as | A jar in `local-mods/`, SHA-1 9f2cfe23df7e531c31102ce319f68d7da8c2487e, 488,909 bytes since 5539f6e, the build of cf51b7d with the roost seed fix. A build, a clean rebuild and a build of a git archive export are alike. The build of 26fc840 (SHA-1 74b2b560, 488,836 bytes) lacks that fix, and the first bundled build of 32d35c9 (SHA-1 0d447e2c, 488,236 bytes) also lacks the review fixes |
| Needs | Create 6.0.8 and Create: Dragons Plus 1.11.9 on both sides. Create: Dragons Plus is a new index entry (c15417f) |
| Uses when present | Farmer's Delight, Supplementaries, Twilight Delight with the Twilight Forest fork, and Lava Fishing, all in the pack |
| Network | Channel `create_integrated_farming:main`, protocol 1. A client without the jar cannot join |
| License | LGPL 3.0 or later, with MIT parts from Create and Farmer's Delight, see [license](#license) |

## What it adds

| Block | Use |
|---|---|
| Roost and Chicken Roost | An empty roost catches an adult chicken, from the hand, from a lead or out of a spawner. The Chicken Roost lays an egg every 6,000 to 11,999 ticks from its own loot table `create_integrated_farming:gameplay/roost/chicken`. Seeds by hand, by belt or by Mechanical Arm shorten the wait by 2,400 ticks, and a Spout can feed it a food fluid. Hoppers and funnels take the eggs from every side but its front. A lead lets the chicken out again |
| Fishing Net | A sail like actor for contraptions. Moving through water it rolls the vanilla fishing loot table, and it catches small water creatures and drops their loot and Experience Nuggets |
| Lava Fishing Net | The same in lava, with the catches of Lava Fishing, see [Lava Fishing](#lava-fishing) |
| Vacuum Harvester | A kinetic block (8 stress units per RPM) that charges and then harvests ripe crops in a radius of 10 blocks, one block above and below it, and replants them from their own drops or its 18 slot inventory. On a contraption it harvests where it goes |

It also changes Create's own machines. The Mechanical Harvester gets the custom harvest rules of the
mod, for example glow berries that keep their vines, the saws take plants in the tags
`create:vertical_plants` and `create:vertical_plants/fragile`, and a leashed mob no longer sits down on
a seat by itself, unless the server config `leashedEntitySitsAutomatically` says so. The recipes are a
shapeless Vacuum Harvester (Mechanical Harvester, Encased Fan, Brass Casing, Chute), the Roost from
bamboo, wheat and Farmer's Delight canvas, the Fishing Net from the safety net of Farmer's Delight, and
the Lava Fishing Net from chains, blaze rods and andesite alloy. Without Farmer's Delight hay bales and
brown wool take the place of canvas and safety net.

The integrations of the pack:

- Farmer's Delight: the harvesters pick tomatoes, also on ropes, rice panicles and mushroom colonies,
  the basket takes items from belts, and from belt funnels while it faces up, and is a Mechanical Arm target, and a Spout with water
  speeds up organic compost.
- Supplementaries: the harvesters pick flax, both halves of it.
- Twilight Delight with the Twilight Forest: a Mechanical Arm on a Huge Water Lily collects the lily
  pads around it, and the Mechanical Harvester leaves the huge lilies in place while it replants.

### Lava Fishing

Upstream only registers the Lava Fishing Net next to Nether Depths Upgrade, Tide, Starcatcher or
Confluence, and the pack has none of them. The fork adds an integration for Lava Fishing 1.2.1 (mod id
`lavafishing`), which the pack carries, after upstream's Nether Depths Upgrade integration
(16dc400). The net rolls `lavafishing:gameplay/fishing/nether/fishing` in the Nether and
`lavafishing:gameplay/fishing/lava/fishing` elsewhere, catches the lava creatures of Lava Fishing, and
exists with its recipe as long as Lava Fishing is loaded.

### Chicken food

Upstream's synced data maps for chicken food become plain data files that the mod reads on every data
pack reload and sends to the clients. A data pack can add foods in
`data/create_integrated_farming/data_maps/item/chicken_food.json` and
`.../fluid/chicken_food.json`, in the NeoForge data map format with the 1.20.1 form of the int providers
(`min_inclusive` and `max_inclusive` under `value`). An item entry can name a remainder in
`using_converts_to`, as the 1.21.1 object `{"id": "minecraft:bowl"}` or as a plain item id (042f279).
The fork keeps upstream's `MODPACK_README.md` unchanged, but that file describes the 1.21.1 build:
its `#minecraft:chicken_food`, `#c:plantoil`, int providers without `value` and the duck, goose,
turkey and Vanilla Backport roosts do not exist here (`UPSTREAM.md`, source change 11).
The defaults are the item tag `create_integrated_farming:chicken_food` and the fluid tag
`forge:plantoil` with 100 mB per feeding, which Immersive Engineering fills in the pack. The item tag
holds the six chicken foods of vanilla 1.20.1 and, as optional entries since cf51b7d, the cabbage seeds,
tomato seeds and rice of Farmer's Delight and the flax seeds of Supplementaries. Those two mods add
these four items to `Chicken.FOOD_ITEMS` at common setup: Farmer's Delight 1.20.1-1.3.4 in
`CommonSetup#registerItemSetAdditions`, Supplementaries 1.20-3.1.44 in `ModSetup#registerMobFoods`
through Moonlight's `RegHelper#registerChickenFood` (javap of the pack jars). So the roost takes what a
vanilla chicken eats in the pack, as upstream's `#minecraft:chicken_food` key does on 1.21.1. A scan of
the 271 jars of a full pack boot copy on 2026-11-06 for the field `f_28233_` and for Moonlight's
`registerChickenFood` found no other mod that changes that list. The Expanded Delight port adds no
chicken food, so its asparagus and chili pepper seeds stay out. A mod that adds a chicken food later
needs an entry in this tag, since the roost reads only its food map.

## Left out

The file `ports/createintegratedfarming/UPSTREAM.md` lists every change. The main omissions:

- The integrations of 26 mods that the pack does not carry, from Atmospheric to Windswept (a8dc899),
  with their duck, goose and turkey roosts and the warm and cold chicken roosts of Vanilla Backport.
  The warm and cold chickens are 1.21.5 content.
- A datagen run. The datagen classes compile, but the files under `src/generated/` are upstream's
  output, converted by hand.
- Spotless, which rewrote the sources on every upstream build.

Naturalist, which the pack carries, has ducks. A roost for them would be new work beyond upstream, and
nobody has checked whether its ducks lay eggs.

## Pack fit

- The mod adds no material, so AlmostUnified has nothing to unify. Its tags use `forge:` names
  (`forge:rods/wooden`, `forge:plantoil`), and `config/almostunified/unify.json` needs no change.
- The Create addons of the pack, Create Crafts & Additions and Steam 'n' Rails, have no farming blocks.
  The other farm automation of the pack (Industrial Foregoing, Cyclic, Farming for Blockheads, Mystical
  Agriculture) uses its own blocks and shares no id or recipe with this mod.
- It has no worldgen.

### Create: Dragons Plus

Create: Dragons Plus 1.11.9 is more than a library. The integration test counted 19 blocks, 21 items,
34 fluids and 186 recipes of its own in a test copy without the other optional mods (KubeJS check
`cif_cdp`). The paths below are in `CreateDragonsPlus-1.11.9.jar`.

- Liquid dyes of the 16 vanilla colors with buckets, and 32 mixing recipes that turn dyes into liquid
  dye and back (`data/create_dragons_plus/recipes/mixing/`). Its server config also lets liquid dye
  color items and mobs it touches, and its common config makes colored concrete where liquid dye meets
  lava.
- Dragon's breath as a fluid with a bucket and a cauldron, filling and emptying recipes for bottles
  (`recipes/filling/dragon_breath.json`, `recipes/emptying/dragon_breath.json`), automated brewing
  recipes for it, and a common config switch that lets pointed dripstone slowly fill cauldrons with it
  (the mixin into `PointedDripstoneBlock`). That last part multiplies dragon's breath, which otherwise
  only the Ender Dragon gives.
- The Fluid Hatch (`recipes/crafting/fluid_hatch.json`). The Blaze Upgrade Smithing Template is
  registered, but its recipe `recipes/crafting/blaze_upgrade_smithing_template.json` waits for a common
  config switch that is off by default, and it did not load. Two rare Create packages
  (`rare_blaze_package`, `rare_marble_gate_package`) are registered too.
- Four new kinds of fan processing for Create's Encased Fan. Bulk Coloring runs behind liquid dye.
  Bulk Ending runs behind a dragon head or dragon's breath and turns cobblestone into end stone, stone
  bricks and their slab, stairs and wall into end stone bricks, leather into phantom membrane and apples
  into chorus fruit (`recipes/ending/`). Bulk Freezing runs behind powder snow and turns ice into
  packed ice, packed ice into blue ice and magma cream into slime balls (`recipes/freezing/`). Bulk
  Sanding runs behind sand and uses 141 sandpaper polishing recipes for polished, oxidized and waxed
  blocks, which the mod builds at load time and which are no file in the jar.
- Fluids and dyes for Dye Depot, Dyenamics and Arts and Crafts, which the pack does not carry, so they
  stay unregistered. An Aether part (`create_dragons_plus.aether.mixins.json`, fan enchanting and
  incubation behind a Golden Aercloud) that the pack's Aether may turn on. The test copies had no
  Aether, so nobody verified it.

None of this is a material of AlmostUnified, and no recipe of another mod changes. Nobody checked the
ending and freezing recipes or the dragon's breath duplication against the progression of the pack,
so the owner decides whether they stay, see [open items](../open-items.md). The server config
`world/serverconfig/create_dragons_plus-server.toml` turns each fan processing type off
(`enableBulkColoring`, `enableBulkFreezing`, `enableBulkSanding`, `enableBulkEnding`), and the common
config `config/create_dragons_plus-common.toml` turns the fluids, the Fluid Hatch and the generated
recipes off. Single recipes can go in `kubejs/server_scripts/recipe_fixes.js`, for example
`event.remove({id: 'create_dragons_plus:ending/end_stone_from_cobblestone'})`.

## License

The code is LGPL 3.0 or later. The files `mods.toml` and `gradle.properties` and every source header
say `LGPL-3.0-or-later`, and Modrinth lists LGPL-3.0-only. The license review of 2026-11-05 allowed the
public fork and the bundled jar under twelve conditions, which the permission record
`modrinth-permissions/create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar.md` lists one by one.
In short:

- The jar carries upstream's `LICENSE.txt` (the LGPL and GPL texts), Create's MIT notice as
  `LICENSE-CREATE.txt` for one method and two texture details from Create's MIT era files,
  `FARMERS_DELIGHT_LICENSE.txt` next to the basket and safety net textures, and a `NOTICE` that marks
  the build as modified and names the upstream commit, the date and the changes (GPL 5(a)). The same
  files travel in `licenses/create-integrated-farming-1.20.1-1.4.3-skytekx.1/`.
- The complete source of the jar is `ports/createintegratedfarming/` at the commit that last changed
  the jar in `local-mods/` (5539f6e, with the fork unchanged since cf51b7d), and it has to stay public
  there as long as a pack that ships the jar is offered (GPL 6(d)). The source link on master makes `tools/mrpack_permissions.py` refuse the jar until
  master holds that directory and this build.
- Every upstream header keeps "Copyright (C) 2025 DragonsPlus". The three classes of the Lava Fishing
  integration are modified copies of upstream's Nether Depths Upgrade integration and name DragonsPlus
  and SkyTekx on their copyright line (c07c056).
- No Create asset of 2025-09-21 or later is copied. The models refer to Create textures by name.
- Create: Dragons Plus comes from the Modrinth CDN, so the pack does not redistribute it.

Not verified: the 32 by 32 texture `textures/block/chicken.png` looks like pixel art derived from the
vanilla chicken. Nobody compared it with the vanilla files.

## Tests

The port's tests below ran the first build of 32d35c9. The [integration tests](#integration-tests)
ran the build of 26fc840 and an Arclight copy.

A plain Forge 47.4.18 test server with 3 GB of heap ran the jar on 2026-11-05 (test copy outside the
repository, not in git). The first boot had only Create 6.0.8, Create: Dragons Plus 1.11.9 and the port.
The later boots added Farmer's Delight 1.20.1-1.3.4, Supplementaries 1.20-3.1.44 with Moonlight, Twilight
Delight 2.2.8 with the Twilight Forest fork, Lava Fishing 1.2.1 with Aquaculture 2.5.7 and Kotlin for
Forge, and KubeJS 2001.6.5 for the checks.

| Check | Result |
|---|---|
| Boot | Every boot reached `Done` with no FATAL line, no crash and no ERROR or WARN line from the mod. The full boots logged three ERROR lines of Twilight Delight 2.2.8, which tags three of its own items that do not exist (`diet:sugars`, `farmersdelight:snacks`, `farmersdelight:sweets`). The first boot logged the vanilla line `No key layers` of a flat world without settings |
| Registry | Five blocks and items (Roost, Chicken Roost, Fishing Net, Lava Fishing Net, Vacuum Harvester), two block entity types, the creative tab and three arm interaction point types (`chicken_coop`, `basket`, `huge_water_lily`). Without Lava Fishing the Lava Fishing Net is absent, as upstream intends |
| Recipes | KubeJS counted 4 recipes of the mod with Farmer's Delight and Lava Fishing (fishing net, lava fishing net, roost, vacuum harvester) |
| Mixins | The flag `-Dmixin.debug.export` showed the six handlers in Create's `HarvesterMovementBehaviour`, `SawBlockEntity`, `SawMovementBehaviour`, `SeatBlock`, `TreeCutter` and `BlockBreakingMovementBehaviour` |
| Vacuum Harvester | At 256 RPM it harvested ripe wheat, Farmer's Delight tomatoes, rice panicles and a brown mushroom colony, Supplementaries flax, glow berries and sweet berries, left unripe wheat alone and replanted each crop |
| Roost | Seeds by hand cut 2,400 ticks and started a cooldown of 400 to 800 ticks, dirt was refused, a belt input from the back fed it, an egg appeared after the timer, a roost item caught a chicken and a lead let it out leashed. A Spout fed it 250 mB of water through a test data pack that made water a food, which also showed that a data pack reload reaches the food map |
| Capabilities | The harvester offers its inventory on every side but the bottom, the roost on every side but its front, and neither accepts inserted items |
| Fishing nets | On a Mechanical Bearing a Fishing Net caught carp, smallmouth bass, minnow and salmon of Aquaculture and turned four cod into cod and four Experience Nuggets. A Lava Fishing Net caught 17 lava fish of three kinds and the junk and treasure of Lava Fishing in about seven minutes of running, with the cooldown multiplier of the test config at 1 instead of 8 |
| Basket | A belt insertion through the behaviour of Create: Dragons Plus put five apples into a Farmer's Delight basket |
| Dedicated server | The class loading log of the full boot holds no class of the mod's `client` package and, of `net.minecraft.client`, only the server side `LanServerPinger` |
| Restart | Inventories, timers and contraption contents survived a save and a restart |

The first full boot crashed on the first harvest next to tomatoes with a `NoSuchMethodError` for
`TomatoBlock.getAge`, because reobfuscation did not see the class hierarchy of Farmer's Delight. Commit
69bc4e3 fixed the build, and the later boots ran with it.

### Integration tests

The integration review of 2026-11-05 ran two more test copies outside the repository, driven over
RCON.

- The review's Arclight copy ran the repo's hardened
  `arclight-forge-1.20.1-1.0.6-6de9fec-safeiter2.jar` (SHA-1 70b44a84) with the first build of
  32d35c9, Create 6.0.8, Create: Dragons Plus 1.11.9, Farmer's Delight, Supplementaries with
  Moonlight, Lava Fishing with Aquaculture and Kotlin for Forge, and Twilight Delight 2.2.8 with the
  Twilight Forest fork. Its `latest.log` shows `Done (3.666s)`,
  no FATAL line and no failed mixin. The mixins of Create: Dragons Plus into `ServerPlayer`,
  `BottleItem`, `ConcretePowderBlock`, `PointedDripstoneBlock`, `RecipeManager` and
  `ReloadableServerResources` applied. The only ERROR lines were the flat world line `No key layers`
  and the three Twilight Delight tag lines. The commands `setblock` and `data get` showed the expected
  roost and harvester NBT, the Lava Fishing Net was registered, a destroyed roost dropped itself, and the RCON
  stop ended with Arclight's usual `force exiting` line.
- The port's Forge test copy ran the build of 26fc840 with the same mods and KubeJS and reached
  `Done (14.444s)` with no FATAL line and no ERROR line besides the three Twilight Delight tag lines.
  A test data pack gave bread `"using_converts_to": {"id": "minecraft:bowl"}` and apples a plain
  `"minecraft:stick"`. Both loaded, and the map wrote both back in the object form. The roost
  schematic of `ponder/roost/operate.nbt`, placed as a structure, held its egg as
  `{Slot: 0, id: "minecraft:egg", Count: 1b}`, while the file before dd98051 loaded as `Items: []`. The
  four recipe advancements that load next to Farmer's Delight reward
  `create_integrated_farming:crafting/fishing_net`, `crafting/lava_fishing_net`, `crafting/roost` and
  `crafting/vacuum_harvester`, and those recipes exist. This first run ended when the memory limit of
  the test machine, which other test servers shared, killed the java process after a later `/reload`.
  A second boot of the same world reached `Done (9.700s)` with the same ERROR lines and still held the
  egg. Two feedings of bread at that roost dropped two bowls, and the server stopped over RCON with
  exit code 0.

Not verified: the Ponder scenes, the JEI category Roosting, the renderers and the client prediction of
feeding by hand, since no client joined. The Mechanical Harvester rules, the saw tags, the seat rule and
the Twilight Delight lily pads were only checked as applied mixins and registrations. Nobody booted the
full pack with the port and Create: Dragons Plus, on Forge or on Arclight, so their mixins never met
those of the other pack mods, and the Aether part of Create: Dragons Plus never ran. The fishing nets
use a Forge `FakePlayer` per contraption, as Create's deployer does, and the Arclight copy never ran a
net.

### Seed test of 2026-11-06

The build of cf51b7d (SHA-1 `9f2cfe23`) on a plain Forge 47.4.18 server with Create 6.0.8, Create:
Dragons Plus 1.11.9, Farmer's Delight 1.20.1-1.3.4, Supplementaries 1.20-3.1.44 with Moonlight and
KubeJS 2001.6.5 for the checks, all as the files of the index, `-Xmx3G` and a flat world
(`bp2/nm/newmods-fix-cif/server/` in the session's scratch directory). The boot reached `Done` in
5.7 s with 0 ERROR and 0 FATAL lines, and the server stopped over RCON with exit code 0.

- The food map resolved ten items: the six vanilla foods, `farmersdelight:cabbage_seeds`,
  `farmersdelight:tomato_seeds`, `farmersdelight:rice` and `supplementaries:flax_seeds`. Each of the
  ten also passed `Chicken.FOOD_ITEMS`, while dirt and bread failed both.
- A Forge `FakePlayer` fed one cabbage seed through the block's `use` method to a Chicken Roost that
  the test script had placed. The seed was consumed, `EggLayTime` fell from 11980 to 9580 and `FeedCooldown` became
  536.
- Two tomato seeds through the belt input at the back of a fresh roost left one, with the same cut of
  2,400 ticks and a cooldown of 653.
- A simulated feeding took rice, flax seeds and wheat seeds and refused dirt and bread.
- A copy without Farmer's Delight and Supplementaries (`bp2/nm/newmods-fix-cif/server-nooptional/`)
  loaded the tag with its six vanilla entries and logged no ERROR line up to `Done`. The ERROR lines
  after `Done` came from the test script, which could not read the private field
  `Chicken.FOOD_ITEMS` there. In the pack the access transformers of Farmer's Delight and Moonlight
  make that field public.

## Steps for the server

Git does not hold `mods/`. With the deploy of the next pack, while the java process is gone:

1. Copy `local-mods/create-integrated-farming-1.20.1-1.4.3-skytekx.1.jar` into `mods/` and check its
   SHA-1 `9f2cfe23df7e531c31102ce319f68d7da8c2487e`.
2. Download `CreateDragonsPlus-1.11.9.jar` from the index entry (the `cdn.modrinth.com` URL of
   `modrinth.index.json`) into `mods/` and check its SHA-1 `cb249347ad15914619a81371b4cfa35f74f575ed`.
3. Start the server and check that it reaches `Done` with the [ERROR baseline](../server/logs-and-crashes.md#boot-error-baseline)
   and that `world/serverconfig/create_integrated_farming-server.toml` and
   `world/serverconfig/create_dragons_plus-server.toml` appear. The first boot also writes
   `config/create_dragons_plus-common.toml`. Commit it before `tools/build_mrpack.py`, which takes the
   overrides from git HEAD, and change it or the server configs only while the java process is gone.

Clients need both jars too, which the pack delivers. Neither mod has worldgen, so no chunk needs a
new pregen. Removing the mod later turns its blocks into air and drops the contents of roosts and
harvesters. Before a public pack, merge and push the branch with `ports/createintegratedfarming/` to
master and fetch, see [publish](publish.md).

Related: [mod set](mod-set.md#bundled-jars), [publish](publish.md), [open items](../open-items.md).
