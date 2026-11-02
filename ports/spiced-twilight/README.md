# Spiced Twilight (Forge 1.20.1 backport)

Unofficial Forge 1.20.1 backport of [Spiced Twilight](https://github.com/baguchi/SpicedTwilight) by baguchi
(upstream 1.21.1-2.0.0), built against the SkyTekx Twilight Forest fork in [`../twilightforest`](../twilightforest).
It adds a Maze Slime Beetle to the Labyrinth, lets Helmet Crabs hide in their helmet, gives kobolds
biome-specific armor and baby variants, gives the Fire Beetle's breath a wind-up, lets the Lich turn
minions into Skeleton Druids, and adds Fire Beetle Powder (brewing fuel), Hydra Hide and the Cinder Pouch.
The Naga jump attack is not ported. [`NOTICE`](NOTICE) lists every change against upstream.

License: LGPL-3.0-or-later ([`LICENSE.md`](LICENSE.md), plus the GPL-3.0 text in [`COPYING`](COPYING)).

## Build

Java 17. Build the Twilight Forest fork first, it is the compile dependency:

```bash
(cd ../twilightforest && ./gradlew build)
./gradlew build
```

The jar lands in `build/libs/spiced_twilight-<version>.jar`. The pack ships it from `local-mods/`. The build is
byte-reproducible, so after a source change rebuild, copy the jar to `local-mods/` and update the SHA-1 and size in
`modrinth-permissions/spiced_twilight-<version>.jar.md`; the shipped jar must always match this source.
`tf_version` in `gradle.properties` must equal the fork's `mod_version`, because it names the fork jar.
