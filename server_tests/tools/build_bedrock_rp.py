#!/usr/bin/env python3
"""
Build a Bedrock resource pack for Farmer's Delight from the mod jar.

Extracts the real PNG textures from the Farmer's Delight mod jar and generates
textures/item_texture.json, textures/terrain_texture.json and a .mcpack archive,
so Bedrock players see the authentic FD item/block textures (no placeholders).

Usage:
    python build_bedrock_rp.py --mod-jar path/to/farmers-delight-refabricated.jar \
        [--skeleton path/to/bedrock-resourcepack] [--out dir] [--mcpack out.mcpack]
"""
import argparse
import hashlib
import json
import shutil
import zipfile
from pathlib import Path

NS = "farmersdelight"
ITEM_DIR = f"assets/{NS}/textures/item"
BLOCK_DIR = f"assets/{NS}/textures/block"

MANIFEST = {
    "format_version": 2,
    "header": {
        "name": "Farmers Delight - Geyser Resource Pack",
        "description": "Bedrock resource pack for Farmer's Delight custom items and blocks via the Geyser bridge.",
        "uuid": "a3b8c1d2-e4f5-4a6b-8c7d-9e0f1a2b3c4d",
        "version": [1, 0, 1],
        "min_engine_version": [1, 21, 0],
    },
    "modules": [
        {
            "type": "resources",
            "uuid": "b4c9d2e3-f5a6-4b7c-9d8e-af01b2c3d4e5",
            "version": [1, 0, 1],
        }
    ],
}


def sanitize(name: str) -> str:
    return name.replace(":", ".").replace("/", "_")


def extract_textures(mod_jar: Path, out: Path):
    item_textures = {}
    block_textures = {}
    with zipfile.ZipFile(mod_jar) as z:
        for entry in z.namelist():
            if not entry.endswith(".png"):
                continue
            if entry.startswith(ITEM_DIR + "/"):
                rel = entry[len(ITEM_DIR) + 1: -4]
                item_textures[sanitize(f"{NS}:{rel}")] = f"textures/items/{rel}"
                dest = out / "textures/items" / f"{rel}.png"
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(z.read(entry))
            elif entry.startswith(BLOCK_DIR + "/"):
                rel = entry[len(BLOCK_DIR) + 1: -4]
                block_textures[sanitize(f"{NS}:{rel}")] = f"textures/blocks/{rel}"
                dest = out / "textures/blocks" / f"{rel}.png"
                dest.parent.mkdir(parents=True, exist_ok=True)
                dest.write_bytes(z.read(entry))
    return item_textures, block_textures


def write_json(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    path.write_text(json.dumps(data, indent=2), encoding="utf-8")


def atlas_entries(textures):
    """Return Bedrock's texture-data entry format for a single texture per key."""
    return {name: {"textures": [path]} for name, path in sorted(textures.items())}


def content_version(out: Path):
    """Derive a Bedrock pack revision from its contents to invalidate stale client caches."""
    digest = hashlib.sha256()
    for file in sorted(path for path in out.rglob("*") if path.is_file() and path.name != "manifest.json"):
        digest.update(file.relative_to(out).as_posix().encode("utf-8"))
        digest.update(b"\0")
        digest.update(file.read_bytes())
    # Bedrock expects a three-part integer version. A 16-bit content revision is ample here.
    revision = int.from_bytes(digest.digest()[:2], "big") + 1
    return [1, 0, revision]



# These are the block items exposed by FdBlocks.java. Their model, blockstate,
# and texture definitions are read from the exact Farmer's Delight jar supplied
# to this builder; no hand-authored substitute geometry is used.
BLOCK_DEFAULT_STATES = {
    "stove": {"facing": "north", "lit": "false"},
    "cooking_pot": {"facing": "north", "support": "none", "waterlogged": "false"},
    "skillet": {"facing": "north", "support": "false"},
    "cutting_board": {"facing": "north"},
    "wooden_basket": {"facing": "up"},
    "bamboo_basket": {"facing": "up"},
    "organic_compost": {"composting": "0"},
    "rich_soil": {},
    "rice_bale": {"facing": "up"},
    "rice_bag": {},
    "straw_bale": {"axis": "y"},
    "carrot_crate": {},
    "potato_crate": {},
    "beetroot_crate": {},
    "cabbage_crate": {},
    "tomato_crate": {},
    "onion_crate": {},
    "tatami": {"facing": "up", "paired": "false"},
    "canvas_rug": {},
}


def _read_json_from_jar(z, path):
    try:
        return json.loads(z.read(path).decode("utf-8"))
    except KeyError:
        return None


def _builtin_model_elements(parent):
    """Small set of vanilla parent models used by Farmer's Delight block models."""
    cube = {
        "from": [0, 0, 0], "to": [16, 16, 16],
        "faces": {face: {"texture": "#" + face} for face in
                  ("north", "east", "south", "west", "up", "down")},
    }
    if parent in ("block/cube_all", "minecraft:block/cube_all"):
        for face in cube["faces"].values():
            face["texture"] = "#all"
        return [cube]
    if parent in ("block/cube_bottom_top", "minecraft:block/cube_bottom_top"):
        for face in ("north", "east", "south", "west"):
            cube["faces"][face]["texture"] = "#side"
        cube["faces"]["up"]["texture"] = "#top"
        cube["faces"]["down"]["texture"] = "#bottom"
        return [cube]
    if parent in ("block/orientable_with_bottom", "minecraft:block/orientable_with_bottom"):
        for face in ("east", "south", "west"):
            cube["faces"][face]["texture"] = "#side"
        cube["faces"]["north"]["texture"] = "#front"
        cube["faces"]["up"]["texture"] = "#top"
        cube["faces"]["down"]["texture"] = "#bottom"
        return [cube]
    if parent in ("block/cube_column", "minecraft:block/cube_column"):
        for face in ("north", "east", "south", "west"):
            cube["faces"][face]["texture"] = "#side"
        for face in ("up", "down"):
            cube["faces"][face]["texture"] = "#end"
        return [cube]
    if parent in ("block/cube", "minecraft:block/cube"):
        return [cube]
    if parent in ("block/thin_block", "minecraft:block/thin_block"):
        return [{
            "from": [0, 0, 0], "to": [16, 1, 16],
            "faces": {
                "north": {"texture": "#texture"}, "east": {"texture": "#texture"},
                "south": {"texture": "#texture"}, "west": {"texture": "#texture"},
                "up": {"texture": "#texture"}, "down": {"texture": "#texture"},
            },
        }]
    return None


def _resolve_model(z, model_id, visiting=None):
    """Resolve a Java block model's parents and texture variables."""
    visiting = set() if visiting is None else visiting
    model_id = model_id.split("#", 1)[0]
    if model_id in visiting:
        raise ValueError(f"Cyclic model parent chain at {model_id}")
    visiting.add(model_id)

    if ":" in model_id:
        namespace, name = model_id.split(":", 1)
    else:
        # Unqualified Java model parents such as "block/block" are vanilla assets.
        namespace, name = "minecraft", model_id
    if name.startswith("block/"):
        rel = name[len("block/"):]
    else:
        rel = name
    model_path = f"assets/{namespace}/models/block/{rel}.json"
    raw = _read_json_from_jar(z, model_path)
    if raw is None and namespace == "minecraft":
        # Vanilla parent JSON files are not packaged in the Farmer's Delight jar.
        # Synthesize the common cube parents below; the generic block parent only
        # contributes texture variables and intentionally has no elements.
        builtin = _builtin_model_elements(model_id)
        if builtin is not None:
            return {"textures": {}, "elements": builtin}
        if model_id in ("block/block", "minecraft:block/block"):
            return {"textures": {}, "elements": None}
        raise FileNotFoundError(f"Unsupported vanilla parent model {model_id}")
    if raw is None:
        raise FileNotFoundError(f"Missing model {model_path} referenced by a Farmer's Delight blockstate")

    parent_id = raw.get("parent")
    parent_model = _resolve_model(z, parent_id, visiting.copy()) if parent_id else {
        "textures": {}, "elements": None, "parent": None
    }
    textures = dict(parent_model.get("textures", {}))
    textures.update(raw.get("textures", {}))
    elements = raw.get("elements", parent_model.get("elements"))
    if elements is None and parent_id:
        elements = _builtin_model_elements(parent_id)
    if elements is None:
        raise ValueError(f"Model {model_id} has no elements and uses an unsupported parent {parent_id!r}")
    return {"textures": textures, "elements": elements}


def _choose_blockstate_model(blockstate, defaults):
    """Choose the variant matching the Java block's default state."""
    variants = blockstate.get("variants", {})
    candidates = []
    for key, value in variants.items():
        required = {}
        if key:
            for pair in key.split(","):
                if "=" in pair:
                    name, state = pair.split("=", 1)
                    required[name.strip()] = state.strip()
        if all(defaults.get(name) == state for name, state in required.items()):
            candidates.append((len(required), key, value))
    if candidates:
        # Prefer the most specific matching key, but keep the first definition on ties.
        candidates.sort(key=lambda entry: (-entry[0], list(variants).index(entry[1])))
        choice = candidates[0][2]
    elif variants:
        choice = next(iter(variants.values()))
    else:
        choice = None
    if isinstance(choice, list):
        choice = choice[0] if choice else None
    if not isinstance(choice, dict) or "model" not in choice:
        raise ValueError("Blockstate has no usable default model variant")
    rotation = {axis: choice.get(axis, 0) for axis in ("x", "y", "z")}
    return choice["model"], rotation


def _resolve_texture_ref(textures, reference):
    seen = set()
    while isinstance(reference, str) and reference.startswith("#"):
        key = reference[1:]
        if key in seen or key not in textures:
            raise ValueError(f"Unresolved model texture variable {reference}")
        seen.add(key)
        reference = textures[key]
    if not isinstance(reference, str):
        raise ValueError(f"Invalid model texture reference: {reference!r}")
    return reference


def _texture_asset_path(reference):
    if ":" in reference:
        namespace, texture_path = reference.split(":", 1)
    else:
        namespace, texture_path = NS, reference
    if texture_path.startswith("textures/"):
        texture_path = texture_path[len("textures/"):]
    return f"assets/{namespace}/textures/{texture_path}.png"


def _bedrock_uv(face, tile, source_size):
    """Convert Java 0..16 UV coordinates into atlas pixel coordinates."""
    uv = face.get("uv")
    if uv is None:
        uv = [0, 0, 16, 16]
    sx, sy = source_size
    tx, ty = tile
    x1 = tx + float(uv[0]) * sx / 16.0
    y1 = ty + float(uv[1]) * sy / 16.0
    x2 = tx + float(uv[2]) * sx / 16.0
    y2 = ty + float(uv[3]) * sy / 16.0
    result = {"uv": [round(x1, 4), round(y1, 4)],
              "uv_size": [round(x2 - x1, 4), round(y2 - y1, 4)]}
    if face.get("rotation") is not None:
        result["rotation"] = face["rotation"]
    return result


def _model_to_bedrock(z, block_name, model_id, rotation, out):
    """Convert the selected original Java model to Bedrock geometry + a per-block atlas."""
    try:
        from PIL import Image
    except ImportError as exc:
        raise RuntimeError("Pillow is required to create multi-texture Bedrock block atlases; install it with: python -m pip install Pillow") from exc

    model = _resolve_model(z, model_id)
    texture_images = {}
    face_textures = []
    for element in model["elements"]:
        resolved_faces = {}
        for face_name, face in element.get("faces", {}).items():
            texture_ref = _resolve_texture_ref(model["textures"], face.get("texture"))
            asset_path = _texture_asset_path(texture_ref)
            if asset_path not in texture_images:
                try:
                    import io
                    image = Image.open(io.BytesIO(z.read(asset_path))).convert("RGBA")
                except KeyError as exc:
                    raise FileNotFoundError(f"Model {model_id} references missing texture {asset_path}") from exc
                texture_images[asset_path] = image
            resolved_faces[face_name] = (face, asset_path)
        face_textures.append(resolved_faces)

    if not texture_images:
        raise ValueError(f"Model {model_id} contains no textured faces")

    # Keep source texture dimensions and pack each one into its own cell. UVs are
    # transformed to atlas pixels, so distinct side/top/parts textures stay exact.
    padding = 2
    cell_w = max(image.width for image in texture_images.values()) + padding * 2
    cell_h = max(image.height for image in texture_images.values()) + padding * 2
    columns = max(1, int(len(texture_images) ** 0.5))
    while columns * columns < len(texture_images):
        columns += 1
    rows = (len(texture_images) + columns - 1) // columns
    atlas = Image.new("RGBA", (columns * cell_w, rows * cell_h), (0, 0, 0, 0))
    texture_tiles = {}
    for index, (asset_path, image) in enumerate(sorted(texture_images.items())):
        x = (index % columns) * cell_w + padding
        y = (index // columns) * cell_h + padding
        atlas.paste(image, (x, y))
        texture_tiles[asset_path] = ((x, y), image.size)

    atlas_name = f"fdg_{block_name}_atlas"
    atlas_path = out / "textures/blocks" / f"{atlas_name}.png"
    atlas_path.parent.mkdir(parents=True, exist_ok=True)
    atlas.save(atlas_path)

    bones = []
    min_x = min_y = min_z = 0.0
    max_x = max_y = max_z = 16.0
    for index, element in enumerate(model["elements"]):
        start = element.get("from", [0, 0, 0])
        end = element.get("to", [16, 16, 16])
        min_x, min_y, min_z = min(min_x, start[0]), min(min_y, start[1]), min(min_z, start[2])
        max_x, max_y, max_z = max(max_x, end[0]), max(max_y, end[1]), max(max_z, end[2])
        cube = {
            "origin": [round(start[0] - 8, 4), start[1], round(start[2] - 8, 4)],
            "size": [round(end[0] - start[0], 4), round(end[1] - start[1], 4), round(end[2] - start[2], 4)],
            "uv": {},
        }
        for face_name, (face, asset_path) in face_textures[index].items():
            tile, source_size = texture_tiles[asset_path]
            cube["uv"][face_name] = _bedrock_uv(face, tile, source_size)
        bone = {"name": f"part_{index}_{str(element.get('name', 'cube')).replace(' ', '_')}",
                "pivot": [0, 0, 0], "cubes": [cube]}
        element_rotation = element.get("rotation")
        if element_rotation:
            axis = element_rotation.get("axis", "y")
            angle = float(element_rotation.get("angle", 0))
            pivot = element_rotation.get("origin", [8, 8, 8])
            bone["pivot"] = [round(pivot[0] - 8, 4), pivot[1], round(pivot[2] - 8, 4)]
            bone["rotation"] = [angle if axis == "x" else 0,
                                angle if axis == "y" else 0,
                                angle if axis == "z" else 0]
        bones.append(bone)

    # Current Bedrock block items are placed in the default Java orientation. The
    # selected blockstate variant is therefore deliberately the default state above.
    geo = {
        "description": {
            "identifier": f"geometry.farmersdelight.{block_name}",
            "texture_width": atlas.width,
            "texture_height": atlas.height,
            "visible_bounds_width": max(2, min(8, (max(max_x - min_x, max_z - min_z) + 15) / 16)),
            "visible_bounds_height": max(2, min(8, (max_y - min_y + 15) / 16)),
            "visible_bounds_offset": [0, max_y / 32.0, 0],
        },
        "bones": bones,
    }
    return geo, {f"{NS}.{atlas_name}": f"textures/blocks/{atlas_name}"}


def generate_bedrock_block_models(mod_jar: Path, out: Path):
    """Build actual Bedrock geometry for each block item from the source mod models."""
    geometries = []
    terrain_textures = {}
    failures = []
    with zipfile.ZipFile(mod_jar) as z:
        for block_name, defaults in BLOCK_DEFAULT_STATES.items():
            blockstate_path = f"assets/{NS}/blockstates/{block_name}.json"
            try:
                blockstate = _read_json_from_jar(z, blockstate_path)
                if blockstate is None:
                    raise FileNotFoundError(blockstate_path)
                model_id, rotation = _choose_blockstate_model(blockstate, defaults)
                geometry, atlas = _model_to_bedrock(z, block_name, model_id, rotation, out)
                geometries.append(geometry)
                terrain_textures.update(atlas)
                if any(rotation.values()):
                    print(f"Warning: {block_name} default blockstate has rotation {rotation}; geometry uses model-local orientation.")
            except Exception as exc:
                failures.append((block_name, str(exc)))

    if failures:
        details = "\n".join(f"  - {name}: {reason}" for name, reason in failures)
        raise RuntimeError("Could not generate original Bedrock models for all registered Farmer's Delight blocks:\n" + details)

    write_json(out / "models/blocks/farmersdelight.geo.json", {
        "format_version": "1.12.0",
        "minecraft:geometry": geometries,
    })
    return terrain_textures


def extract_russian_translations(mod_jar: Path, output: Path):
    """Copy the official Farmer's Delight Russian block/item names for Geyser's explicit displayName fields."""
    source = f"assets/{NS}/lang/ru_ru.json"
    with zipfile.ZipFile(mod_jar) as z:
        raw = _read_json_from_jar(z, source)
    if raw is None:
        raise FileNotFoundError(f"The supplied Farmer's Delight jar has no {source}")
    translations = {key: value for key, value in raw.items()
                    if key.startswith(("item.farmersdelight.", "block.farmersdelight."))}
    output.parent.mkdir(parents=True, exist_ok=True)
    lines = []
    for key, value in sorted(translations.items()):
        # java.util.Properties is loaded through a UTF-8 Reader, so unicode can stay literal.
        escaped = str(value).replace("\\", "\\\\").replace("\n", "\\n").replace("\r", "\\r")
        lines.append(f"{key}={escaped}")
    output.write_text("\n".join(lines) + "\n", encoding="utf-8")
    return len(translations)

def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--mod-jar", required=True, type=Path)
    ap.add_argument("--skeleton", type=Path, default=Path("bedrock-resourcepack"))
    ap.add_argument("--out", type=Path, default=Path("bedrock-resourcepack-built"))
    ap.add_argument("--mcpack", type=Path, default=Path("bedrock-resourcepack-built.mcpack"))
    ap.add_argument(
        "--icon-manifest",
        type=Path,
        default=Path(__file__).resolve().parents[2]
        / "geyser-extension/src/main/resources/farmersdelight-item-icons.txt",
        help="Write the exact item_texture.json shorthand keys consumed by the Geyser extension.",
    )
    ap.add_argument(
        "--translation-output",
        type=Path,
        default=Path(__file__).resolve().parents[2]
        / "geyser-extension/src/main/resources/farmersdelight-ru.properties",
        help="Write the original Farmer's Delight Russian item/block names for the Geyser extension.",
    )
    args = ap.parse_args()

    out = args.out
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True, exist_ok=True)

    item_textures, block_textures = extract_textures(args.mod_jar, out)

    if args.skeleton.exists():
        geo = args.skeleton / "models"
        if geo.exists():
            shutil.copytree(geo, out / "models", dirs_exist_ok=True)

    # Replace the old hand-written sample geometry with models converted from the exact mod jar.
    block_textures.update(generate_bedrock_block_models(args.mod_jar, out))
    translated = extract_russian_translations(args.mod_jar, args.translation_output)

    write_json(out / "textures/item_texture.json", {
        "resource_pack_name": NS,
        "texture_name": "atlas.items",
        "texture_data": atlas_entries(item_textures),
    })
    write_json(out / "textures/terrain_texture.json", {
        "resource_pack_name": NS,
        "texture_name": "atlas.terrain",
        "texture_data": atlas_entries(block_textures),
    })

    # Keep the Java extension in sync with the exact set of texture shorthands in this pack.
    args.icon_manifest.parent.mkdir(parents=True, exist_ok=True)
    args.icon_manifest.write_text("\n".join(sorted(item_textures)) + "\n", encoding="utf-8")

    manifest = json.loads(json.dumps(MANIFEST))
    pack_version = content_version(out)
    manifest["header"]["version"] = pack_version
    manifest["modules"][0]["version"] = pack_version
    write_json(out / "manifest.json", manifest)

    if args.mcpack:
        with zipfile.ZipFile(args.mcpack, "w", zipfile.ZIP_DEFLATED) as z:
            for f in out.rglob("*"):
                if f.is_file():
                    z.write(f, f.relative_to(out))

    print(f"Items mapped: {len(item_textures)}")
    print(f"Block terrain textures mapped: {len(block_textures)}")
    print(f"Original Russian item/block translations written: {translated}")
    print(f"Russian translations written to: {args.translation_output}")
    print(f"Resource pack version: {'.'.join(map(str, pack_version))}")
    print(f"Item icon manifest written to: {args.icon_manifest}")
    print(f"Resource pack written to: {out}")
    print(f"mcpack written to: {args.mcpack}")


if __name__ == "__main__":
    main()
