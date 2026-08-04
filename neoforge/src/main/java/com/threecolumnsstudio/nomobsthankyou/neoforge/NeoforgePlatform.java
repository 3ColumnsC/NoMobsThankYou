package com.threecolumnsstudio.nomobsthankyou.neoforge;

import com.threecolumnsstudio.nomobsthankyou.Platform;
import net.neoforged.fml.loading.FMLPaths;

import java.nio.file.Path;

public class NeoforgePlatform implements Platform {

    @Override
    public Path getConfigDir() {
        return FMLPaths.CONFIGDIR.get();
    }
}
