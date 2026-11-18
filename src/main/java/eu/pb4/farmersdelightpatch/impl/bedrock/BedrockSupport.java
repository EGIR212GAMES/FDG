package eu.pb4.farmersdelightpatch.impl.bedrock;

import eu.pb4.polymer.common.api.PolymerCommonUtils;
import net.fabricmc.fabric.api.networking.v1.context.PacketContext;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.lang.reflect.Method;
import java.util.UUID;

/**
 * Detects Bedrock players (connected via Geyser + Floodgate) without a hard dependency on Floodgate.
 * Uses the Floodgate API reflectively when present; otherwise falls back to a best-effort heuristic.
 */
public final class BedrockSupport {
    public static final boolean ENABLED = Boolean.parseBoolean(System.getProperty("farmersdelight.bedrock-bridge", "true"));

    private static boolean floodgateChecked;
    private static boolean floodgateAvailable;
    private static Method floodgateIsBedrock;

    private BedrockSupport() {
    }

    public static boolean isBedrockPlayer(PacketContext context) {
        if (!ENABLED || context == null) {
            return false;
        }
        Player player = PolymerCommonUtils.getPlayer(context);
        return player instanceof ServerPlayer sp && isBedrockPlayer(sp);
    }

    public static boolean isBedrockPlayer(ServerPlayer player) {
        if (!ENABLED || player == null) {
            return false;
        }
        if (!initFloodgate()) {
            return false;
        }
        try {
            return (Boolean) floodgateIsBedrock.invoke(null, player.getUUID());
        } catch (Throwable t) {
            return false;
        }
    }

    private static synchronized boolean initFloodgate() {
        if (floodgateChecked) {
            return floodgateAvailable;
        }
        floodgateChecked = true;
        try {
            Class<?> apiClass = Class.forName("org.geysermc.floodgate.api.FloodgateApi");
            Object api = apiClass.getMethod("getInstance").invoke(null);
            floodgateIsBedrock = apiClass.getMethod("isFloodgatePlayer", UUID.class);
            floodgateAvailable = floodgateIsBedrock != null && api != null;
        } catch (Throwable t) {
            floodgateAvailable = false;
        }
        return floodgateAvailable;
    }

    public static boolean isFloodgateAvailable() {
        return initFloodgate();
    }
}
