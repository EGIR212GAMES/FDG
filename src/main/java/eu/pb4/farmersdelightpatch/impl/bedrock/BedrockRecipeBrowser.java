package eu.pb4.farmersdelightpatch.impl.bedrock;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import vectorwing.farmersdelight.common.crafting.CookingPotRecipe;
import vectorwing.farmersdelight.common.crafting.CuttingBoardRecipe;

import java.util.ArrayList;
import java.util.List;

/**
 * Minimal Bedrock recipe browser for Farmer's Delight (an approximation of the Polydex in-world
 * viewer). Lists cooking-pot and cutting-board recipes as native Bedrock forms.
 */
public final class BedrockRecipeBrowser {

    private static final int MAX_RECIPES = 40;

    private BedrockRecipeBrowser() {
    }

    public static void open(ServerPlayer player) {
        SimpleForm form = SimpleForm.builder()
                .title("Farmer's Delight Recipes")
                .content("Select a category.")
                .button("Cooking Pot")
                .button("Cutting Board")
                .button("Close")
                .validResultHandler((SimpleFormResponse resp) -> {
                    int id = resp.clickedButtonId();
                    if (id == 0) {
                        listCategory(player, true);
                    } else if (id == 1) {
                        listCategory(player, false);
                    }
                })
                .build();
        FloodgateApi.getInstance().sendForm(player.getUUID(), form);
    }

    private static void listCategory(ServerPlayer player, boolean cooking) {
        MinecraftServer server = player.level().getServer();
        List<RecipeHolder<?>> recipes = new ArrayList<>();
        for (RecipeHolder<?> holder : server.getRecipeManager().getRecipes()) {
            if (cooking ? holder.value() instanceof CookingPotRecipe : holder.value() instanceof CuttingBoardRecipe) {
                recipes.add(holder);
            }
        }
        if (recipes.isEmpty()) {
            SimpleForm none = SimpleForm.builder()
                    .title(cooking ? "Cooking Pot" : "Cutting Board")
                    .content("No recipes found.")
                    .button("Close")
                    .build();
            FloodgateApi.getInstance().sendForm(player.getUUID(), none);
            return;
        }

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(cooking ? "Cooking Pot" : "Cutting Board")
                .content("Select a recipe.");

        int shown = Math.min(recipes.size(), MAX_RECIPES);
        for (int i = 0; i < shown; i++) {
            RecipeHolder<?> holder = recipes.get(i);
            builder.button(recipeTitle(cooking, holder.value()));
        }
        builder.button("Close");

        builder.validResultHandler((SimpleFormResponse resp) -> {
            int id = resp.clickedButtonId();
            if (id >= 0 && id < shown) {
                detail(player, cooking, (RecipeHolder<?>) recipes.get(id));
            }
        });

        FloodgateApi.getInstance().sendForm(player.getUUID(), builder.build());
    }

    private static void detail(ServerPlayer player, boolean cooking, RecipeHolder<?> holder) {
        String content;
        if (cooking) {
            content = cookingDetail((CookingPotRecipe) holder.value());
        } else {
            content = cuttingDetail((CuttingBoardRecipe) holder.value());
        }
        SimpleForm form = SimpleForm.builder()
                .title(recipeTitle(cooking, holder.value()))
                .content(content)
                .button("Back")
                .button("Close")
                .validResultHandler((SimpleFormResponse resp) -> {
                    if (resp.clickedButtonId() == 0) {
                        listCategory(player, cooking);
                    }
                })
                .build();
        FloodgateApi.getInstance().sendForm(player.getUUID(), form);
    }

    private static String recipeTitle(boolean cooking, Object value) {
        if (cooking) {
            return "Cooking: " + itemName(((CookingPotRecipe) value).result());
        }
        CuttingBoardRecipe r = (CuttingBoardRecipe) value;
        List<?> results = r.getRollableResults();
        if (results.isEmpty()) {
            return "Cutting: ?";
        }
        return "Cutting: " + itemName(((vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult) results.get(0)).stack());
    }

    private static String cookingDetail(CookingPotRecipe recipe) {
        StringBuilder sb = new StringBuilder();
        sb.append("Inputs:\n");
        for (Ingredient ing : recipe.input()) {
            sb.append(" - ").append(ingredientLabel(ing)).append('\n');
        }
        if (!recipe.container().create().isEmpty()) {
            sb.append("Container: ").append(itemName(recipe.container())).append('\n');
        }
        sb.append("Output: ").append(itemName(recipe.result())).append('\n');
        if (recipe.getCookTime() > 0) {
            sb.append("Cook time: ").append(recipe.getCookTime()).append(" ticks\n");
        }
        return sb.toString();
    }

    private static String cuttingDetail(CuttingBoardRecipe recipe) {
        StringBuilder sb = new StringBuilder();
        sb.append("Input: ").append(ingredientLabel(recipe.getInput())).append('\n');
        sb.append("Tool: ").append(ingredientLabel(recipe.getTool())).append('\n');
        sb.append("Outputs:\n");
        for (vectorwing.farmersdelight.common.crafting.ingredient.ChanceResult result : recipe.getRollableResults()) {
            int chance = (int) Math.round(result.chance() * 100);
            sb.append(" - ").append(itemName(result.stack()))
                    .append(" (").append(chance).append("%)\n");
        }
        return sb.toString();
    }

    private static String ingredientLabel(Ingredient ing) {
        return ing.getSingleItem()
                .map(holder -> holder.value().getDefaultInstance().getHoverName().getString())
                .orElse("?");
    }

    private static String itemName(net.minecraft.world.item.ItemStackTemplate template) {
        if (template == null || template.create().isEmpty()) {
            return "empty";
        }
        return template.create().getHoverName().getString();
    }
}
