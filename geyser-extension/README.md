# FarmersDelight Geyser extension

Maps Farmer's Delight items (registry namespace `farmersdelight`) to custom Bedrock items so
Bedrock players (via Geyser + Floodgate) see the correct icon, name and eating behaviour.

## Build

```
./gradlew :geyser-extension:build
```

Output: `geyser-extension/build/libs/farmersdelight-geyser-extension-1.0.0.jar`.

Install: place the jar into Geyser's `extensions/` folder and restart Geyser.

## Item ids (`farmersdelight.properties`)

Non-vanilla items on Java are identified by a *network* id that depends on the registry order of all
mods on **your** server. The extension reads those ids from
`farmersdelight.properties` in its data folder:

```
config/Geyser-<platform>/extensions/geyser-extension/farmersdelight.properties
```

Format:

```
item.farmersdelight.cooked_rice=42
item.farmersdelight.hamburger=73
```

This file is generated on the server by the bridge mod (see `../server_tests/bridge-mod/`).
Run both `/fdg dumpids` and `/fdg dumpblocks`; the latter creates `farmersdelight_blocks.properties` in the same extension data folder.
The extension applies the special food/tool settings in `FdItems.java` and the block placement
settings in `FdBlocks.java`. It also automatically registers other Java item IDs from
`farmersdelight.properties` when their icon shorthand exists in the generated item-icon manifest.
This covers additional items such as canvas-sign variants, pie blocks and tatami variants without
requiring a hand-maintained Java list.

> The food values in `FdItems.java` are taken from the mod's `FoodValues`.

## Real textures (no placeholders)

Build the Bedrock resource pack from the actual FD mod jar:

```
python server_tests/tools/build_bedrock_rp.py \
    --mod-jar path/to/farmers-delight-refabricated.jar \
    --skeleton bedrock-resourcepack --out bedrock-resourcepack-built --mcpack bedrock-resourcepack-built.mcpack
```

The generator extracts the real PNGs, writes `item_texture.json`/`terrain_texture.json`, and updates
`geyser-extension/src/main/resources/farmersdelight-item-icons.txt` from the exact item atlas keys.
It also derives the Bedrock manifest version from the pack contents, invalidating stale client-side
resource-pack caches when textures change. **Run the generator before rebuilding the extension** so
the new JAR contains the matching icon manifest. Place the generated `.mcpack` into Geyser's `packs/`.
The root `bedrock-resourcepack/` folder is only a model/manifest skeleton; do not install it directly.

## Required setup on Geyser

- `gameplay.enable-custom-content: true` in Geyser's `config.yml`.
- A generated Bedrock resource pack (built from the actual Farmer's Delight JAR) in Geyser's `packs/`.
- The extension registers block states and block items through the Geyser API.

Do **not** install the old `farmersdelight_blocks.json` in `config/custom_mappings/`.
If it was copied there by an earlier setup, delete that file (or replace it with the empty
`blocks: {}` version from this repository) before restarting Geyser. The old JSON mapping
registers a second custom-block definition for the same Java item identifiers and can override
the item mapping created by this extension.

Block items are registered as non-vanilla custom items with a `geyser:block_placer` component,
so they have a real Bedrock inventory mapping as well as the custom block they place.

## Critical prerequisite: the server bridge

Polymer hides FD blocks/items behind vanilla states for clients without the Polymer module
(Geyser looks like a plain Java client). This extension can only map real `farmersdelight:*`
network content, so a **server-side bridge mod** must:

1. For Floodgate players, send the authentic `farmersdelight:*` block/item ids instead of Polymer's
   fallback (un-mask).
2. Dump the items' network ids into `farmersdelight.properties`.

See `../server_tests/bridge-mod/README.md` for the design and hook points.
