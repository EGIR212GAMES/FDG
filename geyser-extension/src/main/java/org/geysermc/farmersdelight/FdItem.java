package org.geysermc.farmersdelight;

import org.geysermc.geyser.api.item.custom.v2.CustomItemBedrockOptions;
import org.geysermc.geyser.api.item.custom.v2.NonVanillaCustomItemDefinition;
import org.geysermc.geyser.api.item.custom.v2.component.java.JavaConsumable;
import org.geysermc.geyser.api.item.custom.v2.component.java.JavaFoodProperties;
import org.geysermc.geyser.api.item.custom.v2.component.java.JavaItemDataComponents;
import org.geysermc.geyser.api.util.CreativeCategory;
import org.geysermc.geyser.api.util.Identifier;

public record FdItem(
        String javaName,
        String bedrockId,
        String displayName,
        String translationKey,
        int maxStackSize,
        int nutrition,
        float saturation,
        boolean consumable,
        float consumeSeconds
) {
    public NonVanillaCustomItemDefinition toDefinition(int javaId) {
        NonVanillaCustomItemDefinition.Builder builder = NonVanillaCustomItemDefinition.builder(
                Identifier.of(this.javaName),
                Identifier.of(this.bedrockId),
                javaId
        );
        builder.displayName(this.displayName);
        builder.bedrockOptions(CustomItemBedrockOptions.builder()
                .creativeCategory(CreativeCategory.ITEMS)
                .icon(this.iconKey()));
        builder.component(JavaItemDataComponents.MAX_STACK_SIZE, this.maxStackSize);
        // Do not pass the mod's Java translation key to Geyser. Geyser resolves translationString
        // from its own locale tables, which do not contain Farmer's Delight keys; Bedrock would
        // therefore display the raw key (for example, "item.farmersdelight.hamburger").
        // displayName above is the explicit fallback shown to Bedrock players.
        if (this.nutrition > 0) {
            builder.component(JavaItemDataComponents.FOOD, JavaFoodProperties.builder()
                    .nutrition(this.nutrition)
                    .saturation(this.saturation)
                    .build());
        }
        if (this.consumable) {
            builder.component(JavaItemDataComponents.CONSUMABLE, JavaConsumable.builder()
                    .consumeSeconds(this.consumeSeconds)
                    .build());
        }
        return builder.build();
    }

    private String iconKey() {
        return this.bedrockId.replace(':', '.').replace('/', '_');
    }

    static FdItem plain(String path, String displayName) {
        return new FdItem("farmersdelight:" + path, "farmersdelight:" + path, displayName, null, 64, 0, 0f, false, 0f);
    }

    static FdItem food(String path, String displayName, int nutrition, float saturation) {
        return new FdItem("farmersdelight:" + path, "farmersdelight:" + path, displayName, "item.farmersdelight." + path, 64, nutrition, saturation, true, 1.6f);
    }

    static FdItem bowlFood(String path, String displayName, int nutrition, float saturation) {
        return new FdItem("farmersdelight:" + path, "farmersdelight:" + path, displayName, "item.farmersdelight." + path, 16, nutrition, saturation, true, 1.6f);
    }

    static FdItem drink(String path, String displayName, int nutrition, float saturation) {
        return new FdItem("farmersdelight:" + path, "farmersdelight:" + path, displayName, "item.farmersdelight." + path, 16, nutrition, saturation, true, 1.2f);
    }
}
