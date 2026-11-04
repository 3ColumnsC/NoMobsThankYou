package com.threecolumnsstudio.nomobsthankyou.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYou;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

public final class ConfigIO {

    public static final String PRESETS_FILE = "nomobsthankyou-presets.json";
    public static final String OVERRIDES_FILE = "nomobsthankyou-overrides.json";

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private ConfigIO() {}

    public static ConfigPresets readPresets(Path configDir, List<String> errors) {
        Path path = configDir.resolve(PRESETS_FILE);
        if (!Files.exists(path)) {
            writeDefaultPresets(configDir);
            return new ConfigPresets();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root == null || !root.isJsonObject()) {
                throw new JsonParseException("Root must be a JSON object");
            }

            JsonObject object = root.getAsJsonObject();
            ConfigPresets presets = new ConfigPresets();
            presets.enabled = readBoolean(object, "enabled");
            presets.disableAllMobs = readBoolean(object, "disableAllMobs");
            presets.onlyVillagers = readBoolean(object, "onlyVillagers");
            presets.disableVillagers = readBoolean(object, "disableVillagers");
            presets.disableBosses = readBoolean(object, "disableBosses");
            return presets;
        } catch (Exception e) {
            NoMobsThankYou.LOGGER.warn("Could not read presets config, restoring defaults", e);
            errors.add("Errors in " + PRESETS_FILE + ", restored to defaults");
            writeDefaultPresets(configDir);
            return new ConfigPresets();
        }
    }

    public static ConfigOverrides readOverrides(Path configDir, List<String> errors) {
        Path path = configDir.resolve(OVERRIDES_FILE);
        if (!Files.exists(path)) {
            writeDefaultOverrides(configDir);
            return new ConfigOverrides();
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonElement root = JsonParser.parseReader(reader);
            if (root == null || !root.isJsonObject()) {
                throw new JsonParseException("Root must be a JSON object");
            }

            JsonObject object = root.getAsJsonObject();
            ConfigOverrides overrides = new ConfigOverrides();
            overrides.remove = sanitizeList(readStringList(object, "remove"));
            overrides.keep = sanitizeList(readStringList(object, "keep"));
            return overrides;
        } catch (Exception e) {
            NoMobsThankYou.LOGGER.warn("Could not read overrides config, restoring defaults", e);
            errors.add("Errors in " + OVERRIDES_FILE + ", restored to defaults");
            writeDefaultOverrides(configDir);
            return new ConfigOverrides();
        }
    }

    private static Boolean readBoolean(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return null;
        }
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
            throw new JsonParseException("'" + key + "' must be a boolean");
        }
        return element.getAsBoolean();
    }

    private static List<String> readStringList(JsonObject object, String key) {
        JsonElement element = object.get(key);
        if (element == null || element.isJsonNull()) {
            return new ArrayList<>();
        }
        if (!element.isJsonArray()) {
            throw new JsonParseException("'" + key + "' must be an array of strings");
        }

        List<String> entries = new ArrayList<>();
        for (JsonElement entry : element.getAsJsonArray()) {
            if (entry == null || !entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) {
                throw new JsonParseException("'" + key + "' must contain only strings");
            }
            entries.add(entry.getAsString());
        }
        return entries;
    }

    private static List<String> sanitizeList(List<String> entries) {
        return entries.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(entry -> entry.toLowerCase(Locale.ROOT))
                .filter(entry -> !entry.isEmpty())
                .collect(Collectors.toList());
    }

    public static void writeDefaultPresets(Path configDir) {
        try (Writer writer = Files.newBufferedWriter(configDir.resolve(PRESETS_FILE))) {
            ConfigPresets defaults = new ConfigPresets();
            defaults.enabled = true;
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
