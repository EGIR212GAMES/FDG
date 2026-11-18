package eu.pb4.farmersdelightpatch.impl.bedrock;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public final class BedrockCommands {

    private static final String ITEM_FILE = "farmersdelight.properties";
    private static final String BLOCK_FILE = "farmersdelight_blocks.properties";

    private BedrockCommands() {
    }

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("fdg")
                    .then(Commands.literal("dumpids")
                            .executes(ctx -> dumpIds(ctx.getSource(), null))
                            .then(Commands.argument("path", StringArgumentType.string())
                                    .executes(ctx -> dumpIds(ctx.getSource(), StringArgumentType.getString(ctx, "path")))))
                    .then(Commands.literal("dumpblocks")
                            .executes(ctx -> dumpBlocks(ctx.getSource(), null))
                            .then(Commands.argument("path", StringArgumentType.string())
                                    .executes(ctx -> dumpBlocks(ctx.getSource(), StringArgumentType.getString(ctx, "path")))))
                    .then(Commands.literal("status")
                            .executes(ctx -> status(ctx.getSource())))
                    .then(Commands.literal("recipes")
                            .executes(ctx -> {
                                var p = ctx.getSource().getPlayer();
                                if (p == null) {
                                    ctx.getSource().sendFailure(Component.literal("Players only"));
                                    return Command.SINGLE_SUCCESS;
                                }
                                BedrockRecipeBrowser.open(p);
                                return Command.SINGLE_SUCCESS;
                            })));
        });
    }

    private static int dumpIds(CommandSourceStack source, String pathArg) {
        List<String> lines = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            Identifier id = BuiltInRegistries.ITEM.getKey(item);
            if (id == null || !id.getNamespace().equals("farmersdelight")) {
                continue;
            }
            lines.add("item." + id + "=" + BuiltInRegistries.ITEM.getId(item));
        }
        lines.sort(String::compareTo);
        return writeLines(source, lines, resolveTarget(source, pathArg, ITEM_FILE));
    }

    private static int dumpBlocks(CommandSourceStack source, String pathArg) {
        List<String> lines = new ArrayList<>();
        for (Block block : BuiltInRegistries.BLOCK) {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !id.getNamespace().equals("farmersdelight")) {
                continue;
            }
            int groupId = BuiltInRegistries.BLOCK.getId(block);
            for (BlockState state : block.getStateDefinition().getPossibleStates()) {
                String ident = formatState(id, state);
                boolean waterlogged = state.hasProperty(BlockStateProperties.WATERLOGGED)
                        && Boolean.TRUE.equals(state.getValue(BlockStateProperties.WATERLOGGED));
                // Global block-state network id is not exposed via the public API; fall back to the block id.
                lines.add("block." + ident + "=" + groupId + "," + groupId + ",1.5," + waterlogged + ",true");
            }
        }
        lines.sort(String::compareTo);
        return writeLines(source, lines, resolveTarget(source, pathArg, BLOCK_FILE));
    }

    private static int writeLines(CommandSourceStack source, List<String> lines, Path target) {
        try {
            if (target.getParent() != null) {
                Files.createDirectories(target.getParent());
            }
            Files.write(target, lines);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Failed to write " + target + ": " + e.getMessage()));
            return Command.SINGLE_SUCCESS;
        }
        source.sendSuccess(() -> Component.literal("Wrote " + lines.size() + " entries to " + target), true);
        return Command.SINGLE_SUCCESS;
    }

    private static String formatState(Identifier id, BlockState state) {
        StringBuilder sb = new StringBuilder(id.toString()).append('[');
        boolean[] first = {true};
        state.getValues().forEach(v -> {
            if (!first[0]) {
                sb.append(',');
            }
            first[0] = false;
            sb.append(v.property().getName()).append('=').append(v.valueName());
        });
        return sb.append(']').toString();
    }

    private static int status(CommandSourceStack source) {
        boolean floodgate = BedrockSupport.isFloodgateAvailable();
        source.sendSuccess(() -> Component.literal("Floodgate available: " + floodgate + ", bridge enabled: " + BedrockSupport.ENABLED), false);
        return Command.SINGLE_SUCCESS;
    }

    private static Path resolveTarget(CommandSourceStack source, String pathArg, String defaultFile) {
        Path gameDir = FabricLoader.getInstance().getGameDir();
        if (pathArg != null && !pathArg.isBlank()) {
            Path p = Path.of(pathArg);
            return p.isAbsolute() ? p : gameDir.resolve(pathArg);
        }
        String sub = System.getProperty("farmersdelight.geyser-extension-dir", "config/Geyser-Fabric/extensions/geyser-extension");
        return gameDir.resolve(sub).resolve(defaultFile);
    }
}
