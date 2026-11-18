package org.geysermc.farmersdelight;

import java.util.List;

/**
 * Farmer's Delight item catalogue. Registry names mirror {@code ModItems} from the mod.
 * Nutrition/saturation values are taken from the mod's {@code FoodValues}.
 */
public final class FdItems {

    private FdItems() {
    }

    public static final List<FdItem> ALL = List.of(
            // Tools
            new FdItem("farmersdelight:flint_knife", "farmersdelight:flint_knife", "Flint Knife", "item.farmersdelight.flint_knife", 1, 0, 0f, false, 0f),
            new FdItem("farmersdelight:iron_knife", "farmersdelight:iron_knife", "Iron Knife", "item.farmersdelight.iron_knife", 1, 0, 0f, false, 0f),
            new FdItem("farmersdelight:golden_knife", "farmersdelight:golden_knife", "Golden Knife", "item.farmersdelight.golden_knife", 1, 0, 0f, false, 0f),
            new FdItem("farmersdelight:diamond_knife", "farmersdelight:diamond_knife", "Diamond Knife", "item.farmersdelight.diamond_knife", 1, 0, 0f, false, 0f),
            new FdItem("farmersdelight:netherite_knife", "farmersdelight:netherite_knife", "Netherite Knife", "item.farmersdelight.netherite_knife", 1, 0, 0f, false, 0f),

            // Materials
            FdItem.plain("straw", "Straw"),
            FdItem.plain("canvas", "Canvas"),
            FdItem.plain("tree_bark", "Tree Bark"),
            FdItem.plain("rope", "Rope"),
            FdItem.plain("rice_panicle", "Rice Panicle"),
            FdItem.plain("rice", "Rice"),

            // Crops
            FdItem.food("cabbage", "Cabbage", 2, 0.4f),
            FdItem.food("tomato", "Tomato", 1, 0.3f),
            FdItem.food("onion", "Onion", 2, 0.4f),
            FdItem.plain("cabbage_seeds", "Cabbage Seeds"),
            FdItem.plain("tomato_seeds", "Tomato Seeds"),
            FdItem.plain("rotten_tomato", "Rotten Tomato"),

            // Foodstuffs
            FdItem.food("fried_egg", "Fried Egg", 4, 0.4f),
            FdItem.drink("milk_bottle", "Milk Bottle", 0, 0f),
            FdItem.drink("hot_cocoa", "Hot Cocoa", 0, 0f),
            FdItem.drink("apple_cider", "Apple Cider", 0, 0f),
            FdItem.drink("melon_juice", "Melon Juice", 0, 0f),
            FdItem.bowlFood("tomato_sauce", "Tomato Sauce", 4, 0.4f),
            FdItem.food("wheat_dough", "Wheat Dough", 2, 0.3f),
            FdItem.food("raw_pasta", "Raw Pasta", 2, 0.3f),
            FdItem.food("pumpkin_slice", "Pumpkin Slice", 3, 0.3f),
            FdItem.food("cabbage_leaf", "Cabbage Leaf", 1, 0.4f),
            FdItem.food("minced_beef", "Minced Beef", 2, 0.3f),
            FdItem.food("beef_patty", "Beef Patty", 4, 0.8f),
            FdItem.food("chicken_cuts", "Chicken Cuts", 1, 0.3f),
            FdItem.food("cooked_chicken_cuts", "Cooked Chicken Cuts", 3, 0.6f),
            FdItem.food("bacon", "Bacon", 2, 0.3f),
            FdItem.food("cooked_bacon", "Cooked Bacon", 4, 0.8f),
            FdItem.food("cod_slice", "Cod Slice", 1, 0.1f),
            FdItem.food("cooked_cod_slice", "Cooked Cod Slice", 3, 0.5f),
            FdItem.food("salmon_slice", "Salmon Slice", 1, 0.1f),
            FdItem.food("cooked_salmon_slice", "Cooked Salmon Slice", 3, 0.8f),
            FdItem.food("mutton_chops", "Mutton Chops", 1, 0.3f),
            FdItem.food("cooked_mutton_chops", "Cooked Mutton Chops", 3, 0.8f),
            FdItem.food("ham", "Ham", 5, 0.3f),
            FdItem.food("smoked_ham", "Smoked Ham", 10, 0.8f),
            FdItem.food("pie_crust", "Pie Crust", 2, 0.2f),

            // Sweets
            FdItem.food("cake_slice", "Cake Slice", 2, 0.1f),
            FdItem.bowlFood("apple_pie_slice", "Apple Pie Slice", 3, 0.3f),
            FdItem.bowlFood("sweet_berry_cheesecake_slice", "Sweet Berry Cheesecake Slice", 3, 0.3f),
            FdItem.bowlFood("chocolate_pie_slice", "Chocolate Pie Slice", 3, 0.3f),
            FdItem.bowlFood("pumpkin_pie_slice", "Pumpkin Pie Slice", 3, 0.3f),
            FdItem.food("sweet_berry_cookie", "Sweet Berry Cookie", 2, 0.1f),
            FdItem.food("honey_cookie", "Honey Cookie", 2, 0.1f),
            FdItem.food("melon_popsicle", "Melon Popsicle", 3, 0.2f),
            FdItem.drink("glow_berry_custard", "Glow Berry Custard", 7, 0.6f),
            FdItem.bowlFood("fruit_salad", "Fruit Salad", 6, 0.6f),

            // Basic meals
            FdItem.bowlFood("mixed_salad", "Mixed Salad", 6, 0.6f),
            FdItem.bowlFood("nether_salad", "Nether Salad", 5, 0.4f),
            FdItem.food("barbecue_stick", "Barbecue Stick", 8, 0.9f),
            FdItem.food("egg_sandwich", "Egg Sandwich", 8, 0.8f),
            FdItem.food("chicken_sandwich", "Chicken Sandwich", 10, 0.8f),
            FdItem.food("hamburger", "Hamburger", 11, 0.8f),
            FdItem.food("bacon_sandwich", "Bacon Sandwich", 10, 0.8f),
            FdItem.food("mutton_wrap", "Mutton Wrap", 10, 0.8f),
            FdItem.food("dumplings", "Dumplings", 8, 0.8f),
            FdItem.food("stuffed_potato", "Stuffed Potato", 10, 0.7f),
            FdItem.food("cabbage_rolls", "Cabbage Rolls", 5, 0.5f),
            FdItem.food("salmon_roll", "Salmon Roll", 7, 0.6f),
            FdItem.food("cod_roll", "Cod Roll", 7, 0.6f),
            FdItem.food("kelp_roll", "Kelp Roll", 12, 2.4f),
            FdItem.food("kelp_roll_slice", "Kelp Roll Slice", 6, 0.5f),

            // Soups & stews
            FdItem.bowlFood("cooked_rice", "Cooked Rice", 6, 0.4f),
            FdItem.bowlFood("bone_broth", "Bone Broth", 8, 0.7f),
            FdItem.bowlFood("beef_stew", "Beef Stew", 12, 0.8f),
            FdItem.bowlFood("chicken_soup", "Chicken Soup", 12, 0.8f),
            FdItem.bowlFood("vegetable_soup", "Vegetable Soup", 12, 0.8f),
            FdItem.bowlFood("fish_stew", "Fish Stew", 12, 0.8f),
            FdItem.bowlFood("fried_rice", "Fried Rice", 12, 0.8f),
            FdItem.bowlFood("pumpkin_soup", "Pumpkin Soup", 14, 0.75f),
            FdItem.bowlFood("baked_cod_stew", "Baked Cod Stew", 14, 0.75f),
            FdItem.bowlFood("noodle_soup", "Noodle Soup", 14, 0.75f),
            FdItem.bowlFood("onion_soup", "Onion Soup", 12, 0.8f),

            // Plated meals
            FdItem.bowlFood("bacon_and_eggs", "Bacon and Eggs", 10, 0.6f),
            FdItem.bowlFood("pasta_with_meatballs", "Pasta with Meatballs", 12, 0.8f),
            FdItem.bowlFood("pasta_with_mutton_chop", "Pasta with Mutton Chop", 12, 0.8f),
            FdItem.bowlFood("mushroom_rice", "Mushroom Rice", 12, 0.8f),
            FdItem.bowlFood("roasted_mutton_chops", "Roasted Mutton Chops", 14, 0.75f),
            FdItem.bowlFood("vegetable_noodles", "Vegetable Noodles", 14, 0.75f),
            FdItem.bowlFood("steak_and_potatoes", "Steak and Potatoes", 12, 0.8f),
            FdItem.bowlFood("ratatouille", "Ratatouille", 10, 0.6f),
            FdItem.bowlFood("squid_ink_pasta", "Squid Ink Pasta", 14, 0.75f),
            FdItem.bowlFood("grilled_salmon", "Grilled Salmon", 14, 0.75f),

            // Feasts (consumable portions)
            FdItem.bowlFood("roast_chicken", "Roast Chicken", 14, 0.75f),
            FdItem.bowlFood("stuffed_pumpkin", "Stuffed Pumpkin", 14, 0.75f),
            FdItem.bowlFood("honey_glazed_ham", "Honey Glazed Ham", 14, 0.75f),
            FdItem.bowlFood("shepherds_pie", "Shepherd's Pie", 14, 0.75f),
            FdItem.bowlFood("gleaming_salad", "Gleaming Salad", 14, 0.75f),

            // Pet food
            FdItem.bowlFood("dog_food", "Dog Food", 4, 0.2f),
            FdItem.plain("horse_feed", "Horse Feed")
    );

    public static List<FdItem> all() {
        return ALL;
    }
}
