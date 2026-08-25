package com.threecolumnsstudio.nomobsthankyou.preset;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

import java.util.HashSet;
import java.util.Set;

public enum Preset {
    DISABLE_ALL_MOBS("disableAllMobs") {
        @Override
        public boolean blocks(EntityType<?> type, String id) {
            return !isExcludedMisc(type, id);
        }
    },
    ONLY_VILLAGERS("onlyVillagers") {
        @Override
        public boolean blocks(EntityType<?> type, String id) {
            return !isExcludedMisc(type, id)
                    && !id.equals("minecraft:villager")
                    && !id.equals("minecraft:wandering_trader");
        }
    },
    DISABLE_VILLAGERS("disableVillagers") {
        @Override
        public boolean blocks(EntityType<?> type, String id) {
            return id.equals("minecraft:villager") || id.equals("minecraft:wandering_trader");
        }
    },
    DISABLE_BOSSES("disableBosses") {
        @Override
        public boolean blocks(EntityType<?> type, String id) {
            return id.equals("minecraft:wither")
                    || id.equals("minecraft:ender_dragon")
                    || id.equals("minecraft:elder_guardian");
        }
    };

    private final String jsonKey;

    Preset(String jsonKey) {
        this.jsonKey = jsonKey;
    }

    public String jsonKey() {
        return jsonKey;
    }

    public abstract boolean blocks(EntityType<?> type, String id);

    public Set<String> blockedEntityIds() {
        Set<String> blocked = new HashSet<>();
        for (EntityType<?> type : BuiltInRegistries.ENTITY_TYPE) {
            String id = BuiltInRegistries.ENTITY_TYPE.getKey(type).toString();
            if (blocks(type, id)) {
                blocked.add(id);
            }
        }
        return blocked;
    }

    public static boolean isExcludedMisc(EntityType<?> type, String id) {
        if (type.getCategory() != MobCategory.MISC) {
            return false;
        }
        return !id.equals("minecraft:villager")
                && !id.equals("minecraft:iron_golem")
                && !id.equals("minecraft:snow_golem")
                && !id.equals("minecraft:copper_golem");
    }
}