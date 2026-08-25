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

    private static volatile Set<String> entitiesToRemove = Collections.emptySet();
    private static volatile ConfigOverrides loadedOverrides = new ConfigOverrides();
    private static volatile String activePresetName = null;
    private static volatile boolean loaded = false;
    private static volatile List<String> loadErrors = Collections.emptyList();

    private NoMobsThankYouConfig() {}

    public static boolean shouldRemove(EntityType<?> type) {
        if (!loaded) return false;
        Identifier key = BuiltInRegistries.ENTITY_TYPE.getKey(type);
        return key != null && entitiesToRemove.contains(key.toString());
    }

    public static void load() {
        loadErrors = Collections.emptyList();
        Path configDir = Platform.get().getConfigDir();

        List<String> errors = new ArrayList<>();
        ConfigPresets presets = ConfigIO.readPresets(configDir, errors);
        ConfigOverrides overrides = ConfigIO.readOverrides(configDir, errors);

        Set<String> toRemove = new HashSet<>();
        Preset activePreset = activePreset(presets);
        if (activePreset != null) {
            toRemove.addAll(activePreset.blockedEntityIds());
        }
        applyRemoveOverrides(overrides.remove, toRemove);
        applyKeepOverrides(overrides.keep, toRemove);

        entitiesToRemove = Collections.unmodifiableSet(toRemove);
        loadedOverrides = overrides;
        activePresetName = activePreset != null ? activePreset.jsonKey() : null;
        loadErrors = Collections.unmodifiableList(new ArrayList<>(errors));
        loaded = true;

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

    private static Preset activePreset(ConfigPresets presets) {
        List<Preset> active = new ArrayList<>();
        if (Boolean.TRUE.equals(presets.disableAllMobs)) active.add(Preset.DISABLE_ALL_MOBS);
        if (Boolean.TRUE.equals(presets.onlyVillagers)) active.add(Preset.ONLY_VILLAGERS);
        if (Boolean.TRUE.equals(presets.disableVillagers)) active.add(Preset.DISABLE_VILLAGERS);
        if (Boolean.TRUE.equals(presets.disableBosses)) active.add(Preset.DISABLE_BOSSES);

        if (active.size() > 1) {
            String names = String.join(", ", active.stream().map(Preset::jsonKey).toList());
            NoMobsThankYou.LOGGER.warn("Multiple presets active ({}). No preset will be applied.", names);
            return null;
        }
        return active.isEmpty() ? null : active.get(0);
    }

    private static void applyRemoveOverrides(List<String> remove, Set<String> toRemove) {
        for (String entry : remove) {
            if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2);
                for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
                    String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
                    if (Preset.isExcludedMisc(type, id)) {
                        continue;
                    }
                    if (id.startsWith(namespace + ":")) {
                        toRemove.add(id);
                    }
                }
            } else {
                toRemove.add(entry);
            }
        }
    }

    private static void applyKeepOverrides(List<String> keep, Set<String> toRemove) {
        for (String entry : keep) {
            if (entry.endsWith(":*")) {
                String namespace = entry.substring(0, entry.length() - 2);
                toRemove.removeIf(id -> id.startsWith(namespace + ":"));
            } else {
                toRemove.remove(entry);
            }
        }
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
}