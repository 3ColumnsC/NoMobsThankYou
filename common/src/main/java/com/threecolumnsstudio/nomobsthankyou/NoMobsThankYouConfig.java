package com.threecolumnsstudio.nomobsthankyou;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class NoMobsThankYouConfig {

    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static volatile Set<String> entitiesToRemove = Collections.emptySet();
    private static volatile ConfigOverrides loadedOverrides = new ConfigOverrides();
    private static volatile String activePresetName = null;
    private static volatile boolean loaded = false;
    private static volatile List<String> loadErrors = Collections.emptyList();

    public static boolean shouldRemove(EntityType<?> type) {
        if (!loaded) return false;
        String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
        if (entitiesToRemove.contains(id)) return true;
        String namespace = id.split(":")[0];
        return entitiesToRemove.contains(namespace + ":*");
    }

    public static void load() {
        loadErrors = Collections.emptyList();
        Path configDir = Platform.get().getConfigDir();
        Path presetsPath = configDir.resolve("nomobsthankyou-presets.json");
        Path overridesPath = configDir.resolve("nomobsthankyou-overrides.json");

        List<String> errors = new ArrayList<>();
        ConfigPresets presets = loadPresets(presetsPath, errors);
        ConfigOverrides overrides = loadOverrides(overridesPath, errors);

        Set<String> toRemove = new HashSet<>();

        String activePreset = getActivePreset(presets);
        if (activePreset != null) {
            toRemove.addAll(expandPreset(activePreset));
        }

        for (String entry : overrides.remove) {
            if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2);
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                    String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                    if (type.getCategory() == MobCategory.MISC) {
                        if (!id.equals("minecraft:villager") && !id.equals("minecraft:iron_golem")
                            && !id.equals("minecraft:snow_golem")) {
                            continue;
                        }
                    }
                    if (id.startsWith(namespace + ":")) {
                        toRemove.add(id);
                    }
                }
            } else {
                toRemove.add(entry);
            }
        }

        for (String entry : overrides.keep) {
            if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2);
                toRemove.removeIf(id -> id.startsWith(namespace + ":"));
            } else {
                toRemove.remove(entry);
            }
        }

        entitiesToRemove = Collections.unmodifiableSet(toRemove);
        loadedOverrides = overrides;
        activePresetName = activePreset;
        loadErrors = Collections.unmodifiableList(new ArrayList<>(errors));
        loaded = true;

        NoMobsThankYou.LOGGER.info("Blocking {} entities from spawning", toRemove.size());
        if (activePreset != null) {
            NoMobsThankYou.LOGGER.info("Active preset: {}", activePreset);
        }
    }

    public static void cleanPresets() {
        Path presetsPath = Platform.get().getConfigDir().resolve("nomobsthankyou-presets.json");
        saveDefaultPresets(presetsPath);
        load();
    }

    public static void cleanOverrides() {
        Path overridesPath = Platform.get().getConfigDir().resolve("nomobsthankyou-overrides.json");
        saveDefaultOverrides(overridesPath);
        load();
    }

    public static void reload() {
        load();
    }

    private static ConfigPresets loadPresets(Path path, List<String> errors) {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ConfigPresets presets = GSON.fromJson(reader, ConfigPresets.class);
                if (presets == null) return new ConfigPresets();
                return presets;
            } catch (Exception e) {
                NoMobsThankYou.LOGGER.warn("Could not read presets config, using defaults", e);
                errors.add("Errors in nomobsthankyou-presets.json, using default config");
            }
        } else {
            saveDefaultPresets(path);
        }
        return new ConfigPresets();
    }

    private static ConfigOverrides loadOverrides(Path path, List<String> errors) {
        if (Files.exists(path)) {
            try (Reader reader = Files.newBufferedReader(path)) {
                ConfigOverrides overrides = GSON.fromJson(reader, ConfigOverrides.class);
                if (overrides == null) return new ConfigOverrides();
                if (overrides.remove == null) overrides.remove = new ArrayList<>();
                if (overrides.keep == null) overrides.keep = new ArrayList<>();
                return overrides;
            } catch (Exception e) {
                NoMobsThankYou.LOGGER.warn("Could not read overrides config, using defaults", e);
                errors.add("Errors in nomobsthankyou-overrides.json, using default config");
            }
        } else {
            saveDefaultOverrides(path);
        }
        return new ConfigOverrides();
    }

    private static void saveDefaultPresets(Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) {
            ConfigPresets defaults = new ConfigPresets();
            defaults.disableAllMobs = false;
            defaults.villagersAllowed = false;
            defaults.disableMonsters = false;
            defaults.disablePassiveMobs = false;
            defaults.disableVillagers = false;
            defaults.disableBosses = false;
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            NoMobsThankYou.LOGGER.error("Could not save default presets", e);
        }
    }

    private static void saveDefaultOverrides(Path path) {
        try (Writer writer = Files.newBufferedWriter(path)) {
            ConfigOverrides defaults = new ConfigOverrides();
            defaults.remove = new ArrayList<>();
            defaults.keep = new ArrayList<>();
            GSON.toJson(defaults, writer);
        } catch (IOException e) {
            NoMobsThankYou.LOGGER.error("Could not save default overrides", e);
        }
    }

    private static String getActivePreset(ConfigPresets presets) {
        List<String> active = new ArrayList<>();
        if (presets.disableAllMobs != null && presets.disableAllMobs) active.add("disableAllMobs");
        if (presets.villagersAllowed != null && presets.villagersAllowed) active.add("villagersAllowed");
        if (presets.disableMonsters != null && presets.disableMonsters) active.add("disableMonsters");
        if (presets.disablePassiveMobs != null && presets.disablePassiveMobs) active.add("disablePassiveMobs");
        if (presets.disableVillagers != null && presets.disableVillagers) active.add("disableVillagers");
        if (presets.disableBosses != null && presets.disableBosses) active.add("disableBosses");

        if (active.size() > 1) {
            NoMobsThankYou.LOGGER.warn("Multiple presets active ({}). No preset will be applied.",
                    String.join(", ", active));
            return null;
        }
        return active.isEmpty() ? null : active.get(0);
    }

    private static Set<String> expandPreset(String preset) {
        Set<String> set = new HashSet<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            MobCategory cat = type.getCategory();
            if (preset.equals("disableAllMobs") || preset.equals("villagersAllowed")) {
                if (cat == MobCategory.MISC) {
                    if (!id.equals("minecraft:villager") && !id.equals("minecraft:iron_golem")
                        && !id.equals("minecraft:snow_golem")) {
                        continue;
                    }
                }
            }
            switch (preset) {
                case "disableAllMobs":
                    set.add(id);
                    break;
                case "villagersAllowed":
                    if (!id.equals("minecraft:villager")
                            && !id.equals("minecraft:wandering_trader")) {
                        set.add(id);
                    }
                    break;
                case "disableMonsters":
                    if (cat == MobCategory.MONSTER) {
                        set.add(id);
                    }
                    break;
                case "disablePassiveMobs":
                    if (cat == MobCategory.CREATURE
                            || cat == MobCategory.WATER_CREATURE
                            || cat == MobCategory.WATER_AMBIENT
                            || cat == MobCategory.AMBIENT
                            || cat == MobCategory.AXOLOTLS
                            || cat == MobCategory.UNDERGROUND_WATER_CREATURE) {
                        if (!id.equals("minecraft:wandering_trader")) {
                            set.add(id);
                        }
                    }
                    break;
                case "disableVillagers":
                    if (id.equals("minecraft:villager")
                            || id.equals("minecraft:wandering_trader")) {
                        set.add(id);
                    }
                    break;
                case "disableBosses":
                    if (id.equals("minecraft:wither")
                            || id.equals("minecraft:ender_dragon")
                            || id.equals("minecraft:elder_guardian")) {
                        set.add(id);
                    }
                    break;
            }
        }
        return set;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static String getActivePresetName() {
        return activePresetName;
    }

    public static int getBlockedCount() {
        return entitiesToRemove.size();
    }

    public static List<String> getOverrideRemoveList() {
        return Collections.unmodifiableList(loadedOverrides.remove);
    }

    public static List<String> getOverrideKeepList() {
        return Collections.unmodifiableList(loadedOverrides.keep);
    }

    public static List<String> getLoadErrors() {
        return loadErrors;
    }

    public static String getLoadError() {
        return loadErrors.isEmpty() ? null : loadErrors.stream().collect(Collectors.joining("\n"));
    }
}
