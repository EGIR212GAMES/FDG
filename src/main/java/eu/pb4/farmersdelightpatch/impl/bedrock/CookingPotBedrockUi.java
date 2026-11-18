package eu.pb4.farmersdelightpatch.impl.bedrock;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.item.ItemStack;
import org.geysermc.cumulus.form.SimpleForm;
import org.geysermc.cumulus.response.SimpleFormResponse;
import org.geysermc.floodgate.api.FloodgateApi;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMealSlot;
import vectorwing.farmersdelight.common.block.entity.container.CookingPotMenu;

/**
 * Bedrock alternative for the Java cooking-pot screen. Sends a native Bedrock form
 * (Cumulus/SimpleForm) instead of an sgui screen, which Bedrock cannot render.
 * The form shows the pot contents and lets the player take the cooked result or
 * move ingredient stacks back into the inventory.
 */
public final class CookingPotBedrockUi {

    private static final int MEAL_SLOT = 6;
    private static final int INGREDIENT_SLOTS = 6;

    private CookingPotBedrockUi() {
    }

    public static void open(ServerPlayer player, CookingPotMenu menu) {
        String title = menu.blockEntity.getDisplayName().getString();
        StringBuilder content = new StringBuilder();

        boolean heated = menu.isHeated();
        content.append("State: ").append(heated ? "heated (simmering)" : "not heated").append('\n');

        for (int i = 0; i < INGREDIENT_SLOTS; i++) {
            ItemStack stack = menu.slots.get(i).getItem();
            content.append("Ingredient ").append(i + 1).append(": ")
                    .append(stack.isEmpty() ? "empty" : stack.getHoverName().getString())
                    .append(" x").append(stack.getCount()).append('\n');
        }

        ItemStack meal = ((CookingPotMealSlot) menu.slots.get(MEAL_SLOT)).getItem();
        content.append("Result: ")
                .append(meal.isEmpty() ? "none" : meal.getHoverName().getString())
                .append('\n');

        ItemStack container = menu.blockEntity.getContainer();
        if (!container.isEmpty()) {
            content.append("Container: ").append(container.getHoverName().getString()).append('\n');
        }

        SimpleForm.Builder builder = SimpleForm.builder()
                .title(title)
                .content(content.toString())
                .closedOrInvalidResultHandler(() -> { });

        final int takeBtn;
        if (!meal.isEmpty()) {
            takeBtn = 0;
            builder.button("Take result");
        } else {
            takeBtn = -1;
        }
        builder.button("Empty ingredients to inventory");
        final int emptyBtn = takeBtn == -1 ? 0 : 1;
        builder.button("Close");

        builder.validResultHandler((SimpleFormResponse response) -> {
            int id = response.clickedButtonId();
            player.level().getServer().execute(() -> {
                if (id == takeBtn) {
                    menu.clicked(MEAL_SLOT, 0, ContainerInput.QUICK_MOVE, player);
                } else if (id == emptyBtn) {
                    for (int i = 0; i < INGREDIENT_SLOTS; i++) {
                        menu.clicked(i, 0, ContainerInput.QUICK_MOVE, player);
                    }
                }
            });
        });

        FloodgateApi.getInstance().sendForm(player.getUUID(), builder.build());
    }
}
