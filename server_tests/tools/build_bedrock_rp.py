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


def main():
    ap = argparse.ArgumentParser()
    ap.add_argument("--mod-jar", required=True, type=Path)
    ap.add_argument("--skeleton", type=Path, default=Path("bedrock-resourcepack"))
    ap.add_argument("--out", type=Path, default=Path("bedrock-resourcepack-built"))
    ap.add_argument("--mcpack", type=Path, default=Path("bedrock-resourcepack-built.mcpack"))
    args = ap.parse_args()

    out = args.out
    if out.exists():
        shutil.rmtree(out)
    out.mkdir(parents=True, exist_ok=True)

    write_json(out / "manifest.json", MANIFEST)

    item_textures, block_textures = extract_textures(args.mod_jar, out)

    write_json(out / "textures/item_texture.json", {
        "resource_pack_name": NS,
        "texture_name": "atlas.items",
        "texture_data": item_textures,
    })
    write_json(out / "textures/terrain_texture.json", {
        "resource_pack_name": NS,
        "texture_name": "atlas.terrain",
        "texture_data": block_textures,
    })

    if args.skeleton.exists():
        geo = args.skeleton / "models"
        if geo.exists():
            shutil.copytree(geo, out / "models", dirs_exist_ok=True)

    if args.mcpack:
        with zipfile.ZipFile(args.mcpack, "w", zipfile.ZIP_DEFLATED) as z:
            for f in out.rglob("*"):
                if f.is_file():
                    z.write(f, f.relative_to(out))

    print(f"Items mapped: {len(item_textures)}")
    print(f"Blocks mapped: {len(block_textures)}")
    print(f"Resource pack written to: {out}")
    print(f"mcpack written to: {args.mcpack}")


if __name__ == "__main__":
    main()
