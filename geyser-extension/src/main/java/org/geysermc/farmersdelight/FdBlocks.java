package org.geysermc.farmersdelight;

import java.util.List;

/**
 * Farmer's Delight block catalogue for Bedrock custom-block registration.
 * Geometry and per-block texture atlas identifiers are generated from the original
 * model/blockstate resources in the supplied Farmer's Delight mod jar.
 */
public final class FdBlocks {

    public record FdBlock(String javaPath, String geometry, String texture, String displayName) {
    }

    private FdBlocks() {
    }

    public static final List<FdBlock> ALL = List.of(
            new FdBlock("stove", "geometry.farmersdelight.stove", "farmersdelight.fdg_stove_atlas", "Stove"),
            new FdBlock("cooking_pot", "geometry.farmersdelight.cooking_pot", "farmersdelight.fdg_cooking_pot_atlas", "Cooking Pot"),
            new FdBlock("skillet", "geometry.farmersdelight.skillet", "farmersdelight.fdg_skillet_atlas", "Skillet"),
            new FdBlock("cutting_board", "geometry.farmersdelight.cutting_board", "farmersdelight.fdg_cutting_board_atlas", "Cutting Board"),
            new FdBlock("wooden_basket", "geometry.farmersdelight.wooden_basket", "farmersdelight.fdg_wooden_basket_atlas", "Wooden Basket"),
            new FdBlock("bamboo_basket", "geometry.farmersdelight.bamboo_basket", "farmersdelight.fdg_bamboo_basket_atlas", "Bamboo Basket"),

            new FdBlock("organic_compost", "geometry.farmersdelight.organic_compost", "farmersdelight.fdg_organic_compost_atlas", "Organic Compost"),
            new FdBlock("rich_soil", "geometry.farmersdelight.rich_soil", "farmersdelight.fdg_rich_soil_atlas", "Rich Soil"),
            new FdBlock("rice_bale", "geometry.farmersdelight.rice_bale", "farmersdelight.fdg_rice_bale_atlas", "Rice Bale"),
            new FdBlock("rice_bag", "geometry.farmersdelight.rice_bag", "farmersdelight.fdg_rice_bag_atlas", "Rice Bag"),
            new FdBlock("straw_bale", "geometry.farmersdelight.straw_bale", "farmersdelight.fdg_straw_bale_atlas", "Straw Bale"),
            new FdBlock("carrot_crate", "geometry.farmersdelight.carrot_crate", "farmersdelight.fdg_carrot_crate_atlas", "Carrot Crate"),
            new FdBlock("potato_crate", "geometry.farmersdelight.potato_crate", "farmersdelight.fdg_potato_crate_atlas", "Potato Crate"),
            new FdBlock("beetroot_crate", "geometry.farmersdelight.beetroot_crate", "farmersdelight.fdg_beetroot_crate_atlas", "Beetroot Crate"),
            new FdBlock("cabbage_crate", "geometry.farmersdelight.cabbage_crate", "farmersdelight.fdg_cabbage_crate_atlas", "Cabbage Crate"),
            new FdBlock("tomato_crate", "geometry.farmersdelight.tomato_crate", "farmersdelight.fdg_tomato_crate_atlas", "Tomato Crate"),
            new FdBlock("onion_crate", "geometry.farmersdelight.onion_crate", "farmersdelight.fdg_onion_crate_atlas", "Onion Crate"),
            new FdBlock("tatami", "geometry.farmersdelight.tatami", "farmersdelight.fdg_tatami_atlas", "Tatami"),
            new FdBlock("canvas_rug", "geometry.farmersdelight.canvas_rug", "farmersdelight.fdg_canvas_rug_atlas", "Canvas Rug")
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
