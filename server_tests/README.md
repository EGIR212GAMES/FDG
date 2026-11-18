# Server tests (Bedrock 26.52)

Test server used to verify the Geyser bridge for this Polymer patch. Target: **Bedrock 26.52** /
Java 26.3, Fabric loader `0.19.5`.

## Layout

- `run.ps1` / `run.sh` — bootstrap a Fabric server (downloads Fabric installer + server jar).
- `mods/` — put jars here (see below).
- `geyser/config.yml` — Geyser-Fabric config (custom content enabled, packs).
- `floodgate/config.yml` — Floodgate-Fabric config.
- `packs/` — copy of the built Bedrock resource pack (`bedrock-resourcepack.zip`).
- `custom_mappings/` — copy of `geyser-extension/src/main/resources/custom_mappings/farmersdelight_blocks.json`.

## Required mods (server, Fabric 26.3)

| Mod | Purpose |
|-----|---------|
| `fabric-api` | required by everything |
| `polymer` bundle (core+blocks+resource-pack+autohost+virtual-entity) | server-side content |
| `farmers-delight-refabricated` 26.3-3.6.21 | the content mod |
| `farmers-delight-polymer-patch` | this repo (server-side port) |
| `geyser-fabric` | Bedrock protocol bridge |
| `floodgate-fabric` | Bedrock authentication (Xbox) + UUIDs |
| our `farmersdelight-geyser-extension.jar` | placed into Geyser's `extensions/` folder |

## Setup steps

1. `./run.ps1` (or `./run.sh`) to create the Fabric server.
2. Copy the mod jars into `mods/`.
3. Generate the item-id mapping:
   - Start the server, join as Bedrock, then run the dump command of the bridge mod on the server
     (see `bridge-mod/`), which writes `farmersdelight.properties` with real network ids.
   - Copy that file into Geyser's extension data folder
     (`config/Geyser-Fabric/extensions/farmersdelight-geyser-extension/`).
4. Copy `custom_mappings/farmersdelight_blocks.json` into
   `config/Geyser-Fabric/custom_mappings/`.
5. Build the resource pack from the real FD textures:
   ```
   python tools/build_bedrock_rp.py --mod-jar mods/farmers-delight-refabricated.jar \
       --out ../bedrock-resourcepack-built --mcpack bedrock-resourcepack-built.mcpack
   ```
   Put the `.mcpack` into `packs/`.
6. Ensure Geyser config has `gameplay.enable-custom-content: true`.
7. Start the server. Bedrock players connect to the Bedrock port (default 19132).

## Critical note: the server bridge

Polymer hides Farmer's Delight blocks/items behind vanilla states **for non-Polymer clients**
(Geyser is seen as a plain Java client). Therefore the Geyser extension alone is not enough: the
bridge is implemented **inside this patch mod** — for Floodgate players the server sends the
*authentic* `farmersdelight:*` block/item ids (instead of Polymer's fallback) and `/fdg dumpids`
reports their network ids. See `bridge-mod/README.md` for details.
