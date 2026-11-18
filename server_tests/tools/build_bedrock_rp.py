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
    args = ap.parse_args()

    out = args.out
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True, exist_ok=True)

    item_textures, block_textures = extract_textures(args.mod_jar, out)

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

    if args.skeleton.exists():
        geo = args.skeleton / "models"
        if geo.exists():
            shutil.copytree(geo, out / "models", dirs_exist_ok=True)

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
    print(f"Blocks mapped: {len(block_textures)}")
    print(f"Resource pack version: {'.'.join(map(str, pack_version))}")
    print(f"Item icon manifest written to: {args.icon_manifest}")
    print(f"Resource pack written to: {out}")
    print(f"mcpack written to: {args.mcpack}")


if __name__ == "__main__":
    main()
