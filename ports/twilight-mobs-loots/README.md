# Twilight Mobs Loots (1.20.1 Forge backport)

Unofficial Minecraft 1.20.1 / Forge 47 backport of
[Twilight Mobs Loots](https://github.com/wdd1118/Twilight-Mobs-Loots) 1.1.1 by wdd1118 (MIT, see
[`LICENSE`](LICENSE) and [`NOTICE`](NOTICE)). Built for the SkyTekx 3 pack against the Twilight Forest
fork in [`../twilightforest`](../twilightforest).

Trophies for 48 regular Twilight Forest mobs (the bosses already have Twilight Forest trophies):

- A mob killed by a player drops its trophy with 10% chance (Armored Giant and Giant Miner 50%),
  +5% per Looting level. The pool is injected into `twilightforest:entities/<mob>` (and the 16
  `bighorn_sheep/<color>` tables).
- Place it on the floor or a wall. Using a placed trophy plays the mob's sound and particles.
- Use it in the air to wear it on the head, or put it in the Curios `head` slot or the new `trophy` slot.

Mod id `twilight_mobs_loots` (unchanged from upstream). Version `1.1.1-1.20.1-skytekx.1`.
Requires `twilightforest` `[4.3.2508-skytekx.1,)` and Forge `[47,)`. Curios `[5.8,6)` is optional.

## Build

The Twilight Forest fork is consumed from its own build output, so build it first:

```bash
(cd ../twilightforest && ./gradlew build)   # -> ../twilightforest/build/libs/twilightforest-1.20.1-4.3.2508-skytekx.1-universal.jar
./gradlew build                             # -> build/libs/twilight_mobs_loots-1.1.1-1.20.1-skytekx.1.jar
```

Use Java 17. Another directory with the fork jar can be passed with `-Ptf_libs=<dir>`.

## Not ported

- Twilight Moonshine Moon Rabbit trophy compat (Curios renderer bridge and tag entries): that mod has
  no 1.20.1 build.
- Re-registering Twilight Forest's nine boss trophies as curios with this mod's Curios renderer: the
  1.20.1 fork already registers them (curio capability plus `CurioHeadRenderer`). They are still added
  to the `curios:trophy` slot tag.
