# Server tests (Bedrock 26.50)

Test server used to verify the Geyser bridge for this Polymer patch. Target: **Bedrock 26.50** /
Java 26.3, Fabric loader `0.19.5`.

## Layout

- `run.ps1` / `run.sh` — bootstrap a Fabric server (downloads Fabric installer + server jar).
- `mods/` — put jars here (see below).
- `geyser/config.yml` — Geyser-Fabric config (custom content enabled, packs).
- `floodgate/config.yml` — Floodgate-Fabric config.
- `packs/` — copy of the built Bedrock resource pack (`bedrock-resourcepack.zip`).

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
3. Start the server and run `/fdg dumpids` and `/fdg dumpblocks`.
4. Copy `farmersdelight.properties` and `farmersdelight_blocks.properties` into
   `config/Geyser-Fabric/extensions/geyser-extension/`.
5. Install the model-converter dependency and build the Bedrock resource pack from the exact
   Farmer's Delight JAR (run from `server_tests/`):
   ```
   python -m pip install -r requirements.txt
   python tools/build_bedrock_rp.py --mod-jar mods/farmers-delight-refabricated.jar \
       --skeleton ../bedrock-resourcepack --out packs/rp-built --mcpack packs/farmers-delight-rp.mcpack
   ```
   The generator converts the original block models from the mod jar, creates per-block atlases,
   refreshes the item icon manifest and Russian locale file, and writes a content-derived manifest
   version so Bedrock clients do not keep a stale cached pack.
6. Rebuild the Geyser extension **after** running the pack generator, so the extension JAR contains
   the exact icon-key list for that resource pack:
   ```
   ./gradlew :geyser-extension:build
   ```
   Install the newly built JAR and the newly generated `packs/farmers-delight-rp.mcpack`.
7. Ensure Geyser has `gameplay.enable-custom-content: true` and restart the server.
8. Bedrock players reconnect and accept the updated resource pack if prompted.

## Critical note: the server bridge

Polymer hides Farmer's Delight blocks/items behind vanilla states **for non-Polymer clients**
(Geyser is seen as a plain Java client). Therefore the Geyser extension alone is not enough: the
bridge is implemented **inside this patch mod** — for Floodgate players the server sends the
*authentic* `farmersdelight:*` block/item ids (instead of Polymer's fallback) and `/fdg dumpids`
plus `/fdg dumpblocks` report the item and block-state IDs used by the extension. See `bridge-mod/README.md` for details.
