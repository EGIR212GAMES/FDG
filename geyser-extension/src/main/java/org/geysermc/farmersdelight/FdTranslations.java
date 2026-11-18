package org.geysermc.farmersdelight;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/** Russian display names sourced from Farmer's Delight's own ru_ru.json locale. */
final class FdTranslations {
    private static final Properties RUSSIAN = load();

    private FdTranslations() {
    }

    static String translate(String key, String fallback) {
        return RUSSIAN.getProperty(key, fallback);
    }

    private static Properties load() {
        Properties translations = new Properties();
        try (InputStream stream = FdTranslations.class.getResourceAsStream("/farmersdelight-ru.properties")) {
            if (stream == null) {
                return translations;
            }
            translations.load(new InputStreamReader(stream, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            // Keep the extension usable with its English fallback names if the optional locale file is absent.
        }
        return translations;
    }
}
