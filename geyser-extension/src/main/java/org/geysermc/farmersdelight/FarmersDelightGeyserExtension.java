package org.geysermc.farmersdelight;

import org.geysermc.event.subscribe.Subscribe;
import org.geysermc.geyser.api.block.custom.CustomBlockData;
import org.geysermc.geyser.api.block.custom.component.BoxComponent;
import org.geysermc.geyser.api.block.custom.component.CustomBlockComponents;
import org.geysermc.geyser.api.block.custom.component.GeometryComponent;
import org.geysermc.geyser.api.block.custom.component.MaterialInstance;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBlockState;
import org.geysermc.geyser.api.block.custom.nonvanilla.JavaBoundingBox;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomBlocksEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserDefineCustomItemsEvent;
import org.geysermc.geyser.api.event.lifecycle.GeyserPostInitializeEvent;
import org.geysermc.geyser.api.extension.Extension;
import org.geysermc.geyser.api.util.CreativeCategory;
import org.geysermc.geyser.api.item.custom.v2.NonVanillaCustomItemDefinition;
import org.geysermc.geyser.api.item.custom.v2.CustomItemBedrockOptions;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserBlockPlacer;
import org.geysermc.geyser.api.item.custom.v2.component.geyser.GeyserItemDataComponents;
import org.geysermc.geyser.api.item.custom.v2.component.java.JavaItemDataComponents;
import org.geysermc.geyser.api.util.Identifier;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Geyser extension that maps Farmer's Delight items (registry namespace {@code farmersdelight})
 * to custom Bedrock items so Bedrock players see the correct icon, name and behaviour,
 * and maps Farmer's Delight blocks to custom Bedrock blocks.
 *
 * <p>Because the Java <em>network</em> ids depend on the registry order of all mods on a given
 * server, the extension reads them from {@code farmersdelight.properties} (items) and
 * {@code farmersdelight_blocks.properties} (blocks) in its data folder. These files are generated
 * by {@code server_tests/generate-item-ids.(bat|sh)} / the {@code /fdg dumpids} and
 * {@code /fdg dumpblocks} commands on the target server.</p>
 */
public class FarmersDelightGeyserExtension implements Extension {

    private static final String ITEM_PROPERTIES = "farmersdelight.properties";
    private static final String BLOCK_PROPERTIES = "farmersdelight_blocks.properties";

    private final Map<String, Integer> javaIds = new HashMap<>();

    @Subscribe
    public void onPostInitialize(GeyserPostInitializeEvent event) {
        this.loadItemIds();
        this.logger().info("Loaded " + this.javaIds.size() + " Farmer's Delight item ids from " + ITEM_PROPERTIES);
    }

    @Subscribe
    public void onDefineCustomItems(GeyserDefineCustomItemsEvent event) {
        this.loadItemIds();
        Set<String> registeredItems = new HashSet<>();

        // These entries have explicit food/tool settings and should take precedence over the
        // generic icon-only fallback below.
        for (FdItem item : FdItems.ALL) {
            registeredItems.add(item.javaName());
            int javaId = this.javaIds.getOrDefault(item.javaName(), -1);
            if (javaId < 0) {
                this.logger().warning("No network id recorded for " + item.javaName() + " - run the id generator on the server.");
                continue;
            }
            try {
                event.register(item.toDefinition(javaId));
            } catch (RuntimeException e) {
                this.logger().warning("Failed to register item " + item.javaName() + ": " + e.getMessage());
            }
        }

        // Block items are non-vanilla Java items too. registerItemOverride() only replaces an
        // existing Java item mapping; it does not create the mapping for a modded item. Register
        // each known block item as a real non-vanilla custom item and attach a Bedrock block placer.
        for (FdBlocks.FdBlock block : FdBlocks.ALL) {
            String javaName = "farmersdelight:" + block.javaPath();
            registeredItems.add(javaName);
            int javaId = this.javaIds.getOrDefault(javaName, -1);
            if (javaId < 0) {
                this.logger().warning("No network id recorded for block item " + javaName
                        + " - run /fdg dumpids on the server.");
                continue;
            }

            // Geyser prefixes CustomBlockData identifiers with "geyser_custom:", not "geyser:".
            String bedrockBlockIdentifier = "geyser_custom:farmersdelight_" + block.javaPath().replace('/', '_');
            try {
                NonVanillaCustomItemDefinition definition = NonVanillaCustomItemDefinition.builder(
                                Identifier.of(javaName), Identifier.of(javaName), javaId)
                        .displayName(FdTranslations.translate("block.farmersdelight." + block.javaPath(), block.displayName()))
                        .bedrockOptions(CustomItemBedrockOptions.builder()
                                .creativeCategory(CreativeCategory.CONSTRUCTION))
                        .component(JavaItemDataComponents.MAX_STACK_SIZE, 64)
                        .component(GeyserItemDataComponents.BLOCK_PLACER,
                                GeyserBlockPlacer.of(Identifier.of(bedrockBlockIdentifier), true))
                        .build();
                event.register(definition);
            } catch (RuntimeException e) {
                this.logger().warning("Failed to register block item " + javaName + ": " + e.getMessage());
            }
        }

        // FdItems and FdBlocks intentionally contain the items with special behaviour. The mod
        // has additional registry items (canvas signs, pie blocks, tatami variants, newer items,
        // etc.) that also have PNGs in the resource pack. Register every remaining Java item ID
        // only when its exact icon shorthand exists in the generated atlas manifest.
        Set<String> iconKeys = this.loadItemIconKeys();
        int autoRegistered = 0;
        List<Map.Entry<String, Integer>> remainingItems = this.javaIds.entrySet().stream()
                .filter(entry -> !registeredItems.contains(entry.getKey()))
                .sorted(Map.Entry.comparingByKey())
                .toList();
        for (Map.Entry<String, Integer> entry : remainingItems) {
            String javaName = entry.getKey();
            if (entry.getValue() < 0) {
                this.logger().warning("Invalid negative network ID for " + javaName + "; skipping automatic registration.");
                continue;
            }
            String iconKey = itemIconKey(javaName);
            if (!iconKeys.contains(iconKey)) {
                this.logger().warning("No resource-pack icon " + iconKey + " for " + javaName
                        + "; item was not auto-registered. Rebuild the Bedrock pack from the matching Farmer's Delight JAR.");
                continue;
            }

            String path = pathOf(javaName);
            int maxStackSize = path.endsWith("_knife") ? 1 : 64;
            try {
                NonVanillaCustomItemDefinition definition = NonVanillaCustomItemDefinition.builder(
                                Identifier.of(javaName), Identifier.of(javaName), entry.getValue())
                        .displayName(displayNameFromPath(path))
                        .bedrockOptions(CustomItemBedrockOptions.builder()
                                .creativeCategory(CreativeCategory.ITEMS)
                                .icon(iconKey))
                        .component(JavaItemDataComponents.MAX_STACK_SIZE, maxStackSize)
                        .build();
                event.register(definition);
                autoRegistered++;
            } catch (RuntimeException e) {
                this.logger().warning("Failed to register additional item " + javaName + ": " + e.getMessage());
            }
        }
        this.logger().info("Automatically registered " + autoRegistered
                + " additional Farmer's Delight items using resource-pack icon keys.");
    }

    @Subscribe
    public void onDefineCustomBlocks(GeyserDefineCustomBlocksEvent event) {
        List<String> lines = this.readLines(BLOCK_PROPERTIES);
        Map<String, CustomBlockData> blocks = new HashMap<>();

        // Phase 1: register the custom block data first (required before any override references it).
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || !line.startsWith("block.")) {
                continue;
            }
            int eq = line.lastIndexOf('=');
            if (eq < 0) {
                continue;
            }
            String ident = line.substring(6, eq).trim();
            String path = pathOf(ident);
            FdBlocks.FdBlock fb = path == null ? null : FdBlocks.byPath(path);
            if (fb == null || blocks.containsKey(path)) {
                continue;
            }
            try {
                CustomBlockData cbd = buildBlock(fb, path);
                event.register(cbd);
                blocks.put(path, cbd);
            } catch (RuntimeException e) {
                this.logger().warning("Failed to register block " + path + ": " + e.getMessage());
            }
        }

        // Phase 2: register overrides for each dumped Java block state.
        for (String raw : lines) {
            String line = raw.trim();
            if (line.isEmpty() || !line.startsWith("block.")) {
                continue;
            }
            int eq = line.lastIndexOf('=');
            if (eq < 0) {
                continue;
            }
            String ident = line.substring(6, eq).trim();
            String path = pathOf(ident);
            CustomBlockData cbd = path == null ? null : blocks.get(path);
            if (cbd == null) {
                continue;
            }
            String[] parts = line.substring(eq + 1).split(",");
            if (parts.length < 5) {
                continue;
            }
            try {
                int javaId = Integer.parseInt(parts[0].trim());
                int groupId = Integer.parseInt(parts[1].trim());
                float hardness = Float.parseFloat(parts[2].trim());
                boolean waterlogged = Boolean.parseBoolean(parts[3].trim());
                boolean canBreak = Boolean.parseBoolean(parts[4].trim());

                // The exact runtime ID is generated from Block.BLOCK_STATE_REGISTRY on the server.
                // Let Geyser validate it against the vanilla state registry for its Java protocol.
                if (javaId < 0) {
                    continue;
                }

                JavaBoundingBox[] collision = path.equals("cooking_pot")
                        ? new JavaBoundingBox[]{new JavaBoundingBox(0.5, 0.3125, 0.5, 0.75, 0.625, 0.75)}
                        : new JavaBoundingBox[]{new JavaBoundingBox(0.5, 0.5, 0.5, 1.0, 1.0, 1.0)};
                JavaBlockState jbs = JavaBlockState.builder()
                        .identifier(ident)
                        .javaId(javaId)
                        .stateGroupId(groupId)
                        .blockHardness(hardness)
                        .waterlogged(waterlogged)
                        .canBreakWithHand(canBreak)
                        .collision(collision)
                        .build();
                event.registerOverride(jbs, cbd.blockStateBuilder().build());
            } catch (RuntimeException e) {
                this.logger().warning("Failed to override " + ident + ": " + e.getMessage());
            }
        }

    }

    private static String pathOf(String ident) {
        int bracket = ident.indexOf('[');
        String base = bracket >= 0 ? ident.substring(0, bracket) : ident;
        int colon = base.indexOf(':');
        return colon >= 0 ? base.substring(colon + 1) : base;
    }

    private static CustomBlockData buildBlock(FdBlocks.FdBlock fb, String path) {
        String renderMethod = switch (path) {
            case "wooden_basket", "bamboo_basket", "stove", "cooking_pot", "skillet", "cutting_board", "canvas_rug" -> "alpha_test";
            default -> "opaque";
        };
        // The cooking pot is smaller than a full block. A full cube here causes the Bedrock
        // placement prediction/collision test to reject valid placements near the player.
        BoxComponent blockBox = path.equals("cooking_pot")
                ? new BoxComponent(-6, 0, -6, 12, 10, 12)
                : BoxComponent.fullBox();
        CustomBlockComponents components = CustomBlockComponents.builder()
                // Explicit selection/collision boxes make the custom block targetable and breakable
                // on Bedrock instead of depending on client defaults.
                .collisionBoxes(blockBox)
                .selectionBox(blockBox)
                .geometry(GeometryComponent.builder().identifier(fb.geometry()).build())
                .materialInstance("*", MaterialInstance.builder().texture(fb.texture()).renderMethod(renderMethod).build())
                .displayName(FdTranslations.translate("block.farmersdelight." + path, fb.displayName()))
                .build();
        return CustomBlockData.builder()
                .name("farmersdelight_" + path.replace('/', '_'))
                // The Java block item is registered separately as a non-vanilla custom item
                // with BLOCK_PLACER. Do not also create Geyser's generic custom-block item.
                .includedInCreativeInventory(false)
                .creativeCategory(CreativeCategory.CONSTRUCTION)
                .components(components)
                .build();
    }

    private Set<String> loadItemIconKeys() {
        try (var stream = FarmersDelightGeyserExtension.class.getResourceAsStream("/farmersdelight-item-icons.txt")) {
            if (stream == null) {
                this.logger().warning("Missing farmersdelight-item-icons.txt in the extension JAR. "
                        + "Run the Bedrock resource-pack builder, then rebuild the extension.");
                return Set.of();
            }
            return new String(stream.readAllBytes(), StandardCharsets.UTF_8)
                    .lines()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                    .collect(Collectors.toUnmodifiableSet());
        } catch (IOException e) {
            this.logger().warning("Could not read item icon manifest: " + e.getMessage());
            return Set.of();
        }
    }

    private static String itemIconKey(String javaIdentifier) {
        return javaIdentifier.replace(':', '.').replace('/', '_');
    }

    private static String displayNameFromPath(String path) {
        StringBuilder displayName = new StringBuilder();
        for (String word : path.replace('/', ' ').replace('_', ' ').split("\\s+")) {
            if (word.isEmpty()) {
                continue;
            }
            if (displayName.length() > 0) {
                displayName.append(' ');
            }
            displayName.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        String fallback = displayName.length() == 0 ? path : displayName.toString();
        return FdTranslations.translate("item.farmersdelight." + path, fallback);
    }

    private void loadItemIds() {
        this.javaIds.clear();
        for (String raw : this.readLines(ITEM_PROPERTIES)) {
            String line = raw.trim();
            if (line.isEmpty() || line.startsWith("#")) {
                continue;
            }
            int eq = line.lastIndexOf('=');
            if (eq < 0) {
                continue;
            }
            String key = line.substring(0, eq).trim();
            String value = line.substring(eq + 1).trim();
            if (!key.startsWith("item.")) {
                continue;
            }
            try {
                this.javaIds.put(key.substring("item.".length()), Integer.parseInt(value));
            } catch (NumberFormatException e) {
                this.logger().warning("Invalid id for " + key + ": " + value);
            }
        }
    }

    private List<String> readLines(String file) {
        Path path = this.dataFolder().resolve(file);
        if (!Files.isRegularFile(path)) {
            return List.of();
        }
        try {
            return Files.readAllLines(path);
        } catch (IOException e) {
            this.logger().warning("Could not read " + path + ": " + e.getMessage());
            return new ArrayList<>();
        }
    }
}
