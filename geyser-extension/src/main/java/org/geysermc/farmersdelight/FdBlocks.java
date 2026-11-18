package org.geysermc.farmersdelight;

import java.util.List;

/**
 * Farmer's Delight block catalogue for Bedrock custom-block registration.
 * {@code geometry} may be a custom RP geometry ("geometry.farmersdelight.*") or
 * the built-in "minecraft:geometry.full_block" for full-cube blocks.
 * {@code texture} must match a key present in the resource pack's terrain_texture.json.
 */
public final class FdBlocks {

    public record FdBlock(String javaPath, String geometry, String texture, String displayName) {
    }

    private FdBlocks() {
    }

    public static final List<FdBlock> ALL = List.of(
            new FdBlock("stove", "geometry.farmersdelight.stove", "farmersdelight.stove_side", "Stove"),
            new FdBlock("cooking_pot", "geometry.farmersdelight.cooking_pot", "farmersdelight.cooking_pot_side", "Cooking Pot"),
            new FdBlock("skillet", "geometry.farmersdelight.skillet", "farmersdelight.skillet_side", "Skillet"),
            new FdBlock("cutting_board", "geometry.farmersdelight.cutting_board", "farmersdelight.cutting_board", "Cutting Board"),
            new FdBlock("wooden_basket", "geometry.farmersdelight.basket", "farmersdelight.wooden_basket_side", "Wooden Basket"),
            new FdBlock("bamboo_basket", "geometry.farmersdelight.basket", "farmersdelight.bamboo_basket_side", "Bamboo Basket"),

            new FdBlock("organic_compost", "minecraft:geometry.full_block", "farmersdelight.organic_compost_stage0", "Organic Compost"),
            new FdBlock("rich_soil", "minecraft:geometry.full_block", "farmersdelight.rich_soil", "Rich Soil"),
            new FdBlock("rice_bale", "minecraft:geometry.full_block", "farmersdelight.rice_bale_side", "Rice Bale"),
            new FdBlock("rice_bag", "minecraft:geometry.full_block", "farmersdelight.rice_bag_side", "Rice Bag"),
            new FdBlock("straw_bale", "minecraft:geometry.full_block", "farmersdelight.straw_bale_side", "Straw Bale"),
            new FdBlock("carrot_crate", "minecraft:geometry.full_block", "farmersdelight.carrot_crate_side", "Carrot Crate"),
            new FdBlock("potato_crate", "minecraft:geometry.full_block", "farmersdelight.potato_crate_side", "Potato Crate"),
            new FdBlock("beetroot_crate", "minecraft:geometry.full_block", "farmersdelight.beetroot_crate_side", "Beetroot Crate"),
            new FdBlock("cabbage_crate", "minecraft:geometry.full_block", "farmersdelight.cabbage_crate_side", "Cabbage Crate"),
            new FdBlock("tomato_crate", "minecraft:geometry.full_block", "farmersdelight.tomato_crate_side", "Tomato Crate"),
            new FdBlock("onion_crate", "minecraft:geometry.full_block", "farmersdelight.onion_crate_side", "Onion Crate"),
            new FdBlock("tatami", "minecraft:geometry.full_block", "farmersdelight.tatami", "Tatami"),
            new FdBlock("canvas_rug", "minecraft:geometry.full_block", "farmersdelight.canvas_rug", "Canvas Rug")
    );

    public static FdBlock byPath(String path) {
        for (FdBlock b : ALL) {
            if (b.javaPath().equals(path)) {
                return b;
            }
        }
        return null;
    }
}
