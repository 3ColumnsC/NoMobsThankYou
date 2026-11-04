package com.threecolumnsstudio.nomobsthankyou;

import com.threecolumnsstudio.nomobsthankyou.config.ConfigIO;
import com.threecolumnsstudio.nomobsthankyou.config.ConfigOverrides;
import com.threecolumnsstudio.nomobsthankyou.config.ConfigPresets;
import com.threecolumnsstudio.nomobsthankyou.preset.Preset;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.EntityType;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class NoMobsThankYouConfig {

    private static volatile Set<Identifier> entitiesToRemove = Collections.emptySet();
    private static volatile ConfigOverrides loadedOverrides = new ConfigOverrides();
    private static volatile String activePresetName = null;
    private static volatile boolean enabled = true;
    private static volatile boolean loaded = false;
    private static volatile List<String> loadErrors = Collections.emptyList();

    private NoMobsThankYouConfig() {}

    public static boolean shouldRemove(EntityType<?> type) {
        if (!loaded) return false;
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key != null && entitiesToRemove.contains(key);
    }

    public static void load() {
        loadErrors = Collections.emptyList();
        Path configDir = Platform.get().getConfigDir();

        List<String> errors = new ArrayList<>();
        ConfigPresets presets = ConfigIO.readPresets(configDir, errors);
        ConfigOverrides overrides = ConfigIO.readOverrides(configDir, errors);

        boolean isEnabled = !Boolean.FALSE.equals(presets.enabled);
        Set<Identifier> toRemove = new HashSet<>();
        Preset activePreset = isEnabled ? activePreset(presets, errors) : null;

        if (activePreset != null) {
            toRemove.addAll(activePreset.blockedEntityIds());
        }
        applyRemoveOverrides(overrides.remove, toRemove, errors);
        applyKeepOverrides(overrides.keep, toRemove, errors);

        if (!isEnabled) {
            toRemove.clear();
        }

        entitiesToRemove = Collections.unmodifiableSet(toRemove);
        loadedOverrides = overrides;
        enabled = isEnabled;
        activePresetName = activePreset != null ? activePreset.jsonKey() : null;
        loadErrors = Collections.unmodifiableList(new ArrayList<>(errors));
        loaded = true;

        if (!isEnabled) {
            NoMobsThankYou.LOGGER.info("NoMobsThankYou is disabled via config");
            return;
        }

        NoMobsThankYou.LOGGER.info("Blocking {} entities from spawning", toRemove.size());
        if (activePreset != null) {
            NoMobsThankYou.LOGGER.info("Active preset: {}", activePreset.jsonKey());
        }
    }

    public static void reload() {
        load();
    }

    public static void cleanPresets() {
        ConfigIO.writeDefaultPresets(Platform.get().getConfigDir());
        load();
    }

    public static void cleanOverrides() {
        ConfigIO.writeDefaultOverrides(Platform.get().getConfigDir());
        load();
    }

    private static Preset activePreset(ConfigPresets presets, List<String> errors) {
        List<Preset> active = new ArrayList<>();
        if (Boolean.TRUE.equals(presets.disableAllMobs)) active.add(Preset.DISABLE_ALL_MOBS);
        if (Boolean.TRUE.equals(presets.onlyVillagers)) active.add(Preset.ONLY_VILLAGERS);
        if (Boolean.TRUE.equals(presets.disableVillagers)) active.add(Preset.DISABLE_VILLAGERS);
        if (Boolean.TRUE.equals(presets.disableBosses)) active.add(Preset.DISABLE_BOSSES);

        if (active.size() > 1) {
            String names = String.join(", ", active.stream().map(Preset::jsonKey).toList());
            NoMobsThankYou.LOGGER.warn("Multiple presets active ({}). No preset will be applied.", names);
            errors.add("Multiple presets active (" + names + "), none applied");
            return null;
        }
        return active.isEmpty() ? null : active.get(0);
    }

    private static void applyRemoveOverrides(List<String> remove, Set<Identifier> toRemove, List<String> errors) {
        for (String entry : remove) {
            if (entry.equals("*")) {
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                    Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    if (id == null || Preset.isExcludedMisc(type, id.toString())) {
                        continue;
                    }
                    toRemove.add(id);
                }
            } else if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2);
                if (!validateNamespace(namespace, errors)) {
                    continue;
                }
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                    Identifier id = BuiltInRegistries.ENTITY_TYPE.getKey(type);
                    if (id == null || Preset.isExcludedMisc(type, id.toString())) {
                        continue;
                    }
                    if (id.getNamespace().equals(namespace)) {
                        toRemove.add(id);
                    }
                }
            } else {
                Identifier id = parseEntityId(entry, errors);
                if (id != null) {
                    toRemove.add(id);
                }
            }
        }
    }

    private static void applyKeepOverrides(List<String> keep, Set<Identifier> toRemove, List<String> errors) {
        for (String entry : keep) {
            if (entry.equals("*")) {
                toRemove.clear();
            } else if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2);
                if (!validateNamespace(namespace, errors)) {
                    continue;
                }
                toRemove.removeIf(id -> id.getNamespace().equals(namespace));
            } else {
                Identifier id = parseEntityId(entry, errors);
                if (id != null) {
                    toRemove.remove(id);
                }
            }
        }
    }

    private static Identifier parseEntityId(String entry, List<String> errors) {
        Identifier id = Identifier.tryParse(entry);
        if (id == null) {
            errors.add("Invalid entity id '" + entry + "'");
            return null;
        }
        if (!BuiltInRegistries.ENTITY_TYPE.containsKey(id)) {
            errors.add("Unknown entity id '" + entry + "'");
            return null;
        }
        return id;
    }

    private static boolean validateNamespace(String namespace, List<String> errors) {
        if (namespace.isEmpty() || !Identifier.isValidNamespace(namespace)) {
            errors.add("Invalid namespace '" + namespace + "'");
            return false;
        }

        boolean exists = BuiltInRegistries.ENTITY_TYPE.keySet().stream()
                .anyMatch(id -> id.getNamespace().equals(namespace));
        if (!exists) {
            errors.add("Unknown namespace '" + namespace + "'");
            return false;
        }
        return true;
    }

    public static boolean isLoaded() {
        return loaded;
    }

    public static boolean isEnabled() {
        return enabled;
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
}
