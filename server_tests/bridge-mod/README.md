# Server bridge (implemented inside this patch)

The bridge is implemented **inside the Polymer patch mod** (no separate jar needed):

- `src/main/java/eu/pb4/farmersdelightpatch/impl/bedrock/BedrockSupport.java`
  — detects Bedrock players via the Floodgate API (reflection; no hard dependency).
- `src/main/java/eu/pb4/farmersdelightpatch/mixin/bedrock/`
  — un-masks items (`PolyBaseItemMixin`, `PolySkilletItemMixin`) and blocks
    (`BaseFactoryBlockMixin`, `MapPolymerBlockMixin`, `PolymerFenceGateBlockMixin`,
    `WaterloggableFactoryBlockMixin`, `StateCopyFactoryBlockMixin`) for Bedrock players:
    for Floodgate players the authentic `farmersdelight:*` id is sent instead of Polymer's fallback.
- `src/main/java/eu/pb4/farmersdelightpatch/impl/bedrock/BedrockCommands.java`
  — command `/fdg dumpids [path]` writes the network id mapping and `/fdg status`.

## What it does

1. For Bedrock players, block/item packets carry the real FD registry values, so Geyser's custom
   content mapping (the `geyser-extension` + `custom_mappings` + Bedrock resource pack) becomes active.
2. `/fdg dumpids` writes `farmersdelight.properties` in the format consumed by the extension.

## Enable/disable

- The bridge is off unless Floodgate is present (checked at runtime).
- Set system property `-Dfarmersdelight.bedrock-bridge=false` to force-disable.

## Usage

1. Install Floodgate-Fabric on the server.
2. Start, join as Bedrock player, run `/fdg dumpids`.
3. Copy the produced `farmersdelight.properties` into Geyser's extension data folder
   (`config/Geyser-Fabric/extensions/geyser-extension/`).
4. Ensure Geyser has `enable-custom-content: true`, the Bedrock resource pack, and the block mappings.

## Fixed limitations

- **Cooking pot GUI** (Bedrock): when a Bedrock player opens a cooking pot, `CookingPotBedrockUi`
  sends a native Bedrock form (Cumulus `SimpleForm`) instead of the Java sgui screen. The form shows
  ingredients/container/result and a "Take result" button (server-side quick-move of the meal slot).
- **Textures**: `../tools/build_bedrock_rp.py` extracts the real PNG textures from the FD mod jar
  and generates `item_texture.json`/`terrain_texture.json` + a `.mcpack`.
- **Food values**: `FdItems.java` now uses the exact values from the mod's `FoodValues`.
- **Block visuals**: custom block mapping (JSON) + Bedrock geometry are provided.

## Implemented (this iteration)

- **Bedrock block geometry** for stove / cooking pot / skillet / cutting board / basket
  (`bedrock-resourcepack/models/blocks/farmersdelight.geo.json`) replaces the placeholder cubes.
- **`/fdg recipes`** — native Bedrock recipe browser (SimpleForm) listing cooking-pot and cutting-board
  recipes with inputs, tool, container and outputs (an approximation of the Polydex viewer).
- **Cooking-pot form** now also has an "Empty ingredients to inventory" action.

## Remaining limits

- Bedrock forms are modal: no real drag-and-drop into the pot; interactions are button-based.
- Dynamic multi-part item-display models (stove/board contents, cooking animation) are not animated for
  Bedrock; those blocks render as static geometry.
- Live balancing and end-to-end testing on a real server is still required.
