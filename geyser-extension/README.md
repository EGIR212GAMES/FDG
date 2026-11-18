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
config/Geyser-<platform>/extensions/farmersdelight-geyser-extension/farmersdelight.properties
```

Format:

```
item.farmersdelight.cooked_rice=42
item.farmersdelight.hamburger=73
```

This file is generated on the server by the bridge mod (see `../server_tests/bridge-mod/`).
Items without an entry are skipped with a warning.

> The food values in `FdItems.java` are taken from the mod's `FoodValues`.

## Real textures (no placeholders)

Build the Bedrock resource pack from the actual FD mod jar:

```
python server_tests/tools/build_bedrock_rp.py \
    --mod-jar path/to/farmers-delight-refabricated.jar \
    --out bedrock-resourcepack-built --mcpack bedrock-resourcepack-built.mcpack
```

This extracts the real PNG textures and generates `item_texture.json`/`terrain_texture.json`.
Place the `.mcpack` into Geyser's `packs/`.

## Required setup on Geyser

- `gameplay.enable-custom-content: true` in Geyser's `config.yml`.
- Bedrock resource pack (built from `../bedrock-resourcepack/`) in Geyser's `packs/`.
- Block mappings (`custom_mappings/farmersdelight_blocks.json`) in Geyser's `custom_mappings/`.

## Critical prerequisite: the server bridge

Polymer hides FD blocks/items behind vanilla states for clients without the Polymer module
(Geyser looks like a plain Java client). This extension can only map real `farmersdelight:*`
network content, so a **server-side bridge mod** must:

1. For Floodgate players, send the authentic `farmersdelight:*` block/item ids instead of Polymer's
   fallback (un-mask).
2. Dump the items' network ids into `farmersdelight.properties`.

See `../server_tests/bridge-mod/README.md` for the design and hook points.
