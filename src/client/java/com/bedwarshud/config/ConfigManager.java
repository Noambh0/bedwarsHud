package com.bedwarshud.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;
import org.slf4j.Logger;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class ConfigManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_PATH =
            FabricLoader.getInstance().getConfigDir().resolve("bedwarshud.json");

    private final Logger logger;
    private BedWarsHudConfig config;

    public ConfigManager(Logger logger) {
        this.logger = logger;
        this.config = load();
    }

    public BedWarsHudConfig get() {
        return config;
    }

    public void reload() {
        config = load();
    }

    public void save() {
        config.sanitize();
        Path temporaryPath = CONFIG_PATH.resolveSibling(CONFIG_PATH.getFileName() + ".tmp");
        try {
            Files.createDirectories(CONFIG_PATH.getParent());
            try (Writer writer = Files.newBufferedWriter(temporaryPath)) {
                GSON.toJson(config, writer);
            }
            try {
                Files.move(temporaryPath, CONFIG_PATH,
                        StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (java.nio.file.AtomicMoveNotSupportedException ignored) {
                Files.move(temporaryPath, CONFIG_PATH, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            logger.error("Could not save Bed Wars HUD config", exception);
        }
    }

    private BedWarsHudConfig load() {
        if (!Files.exists(CONFIG_PATH)) {
            BedWarsHudConfig defaults = new BedWarsHudConfig();
            config = defaults;
            save();
            return defaults;
        }

        try (Reader reader = Files.newBufferedReader(CONFIG_PATH)) {
            BedWarsHudConfig loaded = GSON.fromJson(reader, BedWarsHudConfig.class);
            if (loaded == null) {
                throw new IOException("Config file contained no JSON object");
            }
            loaded.sanitize();
            return loaded;
        } catch (IOException | RuntimeException exception) {
            logger.error("Could not read Bed Wars HUD config; defaults will be used", exception);
            return new BedWarsHudConfig();
        }
    }
}

