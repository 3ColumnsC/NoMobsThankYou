package com.threecolumnsstudio.nomobsthankyou.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYou;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ConfigIO {

    public static final String PRESETS_FILE = "nomobsthankyou-presets.json";
    public static final String OVERRIDES_FILE = "nomobsthankyou-overrides.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigIO() {}

    public static ConfigPresets readPresets(Path configDir, List<String> errors) {
        Path path = configDir.resolve(PRESETS_FILE);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ConfigPresets presets = GSON.fromJson(reader, ConfigPresets.class);
                if (presets == null) return new ConfigPresets();
                return presets;
            } catch (Exception e) {
                NoMobsThankYou.LOGGER.warn("Could not read presets config, using defaults", e);
                errors.add("Errors in " + PRESETS_FILE + ", using default config");
            }
        } else {
            writeDefaultPresets(configDir);
        }
        return new ConfigPresets();
    }

    public static ConfigOverrides readOverrides(Path configDir, List<String> errors) {
        Path path = configDir.resolve(OVERRIDES_FILE);
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ConfigOverrides overrides = GSON.fromJson(reader, ConfigOverrides.class);
                if (overrides == null) return new ConfigOverrides();
                overrides.remove = sanitizeList(overrides.remove);
                overrides.keep = sanitizeList(overrides.keep);
                return overrides;
            } catch (Exception e) {
                NoMobsThankYou.LOGGER.warn("Could not read overrides config, using defaults", e);
                errors.add("Errors in " + OVERRIDES_FILE + ", using default config");
            }
        } else {
            writeDefaultOverrides(configDir);
        }
        return new ConfigOverrides();
    }

    private static List<String> sanitizeList(List<String> entries) {
        if (entries == null) return new ArrayList<>();
        return entries.stream().filter(Objects::nonNull).collect(Collectors.toList());
    }

    public static void writeDefaultPresets(Path configDir) {
        try (Writer writer = Files.newBufferedWriter(configDir.resolve(PRESETS_FILE))) {
            ConfigPresets defaults = new ConfigPresets();
            defaults.disableAllMobs = false;
            defaults.onlyVillagers = false;
            defaults.disableVillagers = false;
            defaults.disableBosses = false;
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            NoMobsThankYou.LOGGER.error("Could not save default presets", e);
        }
    }

    public static void writeDefaultOverrides(Path configDir) {
        try (Writer writer = Files.newBufferedWriter(configDir.resolve(OVERRIDES_FILE))) {
            ConfigOverrides defaults = new ConfigOverrides();
            defaults.remove = new ArrayList<>();
            defaults.keep = new ArrayList<>();
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            NoMobsThankYou.LOGGER.error("Could not save default overrides", e);
        }
    }
}