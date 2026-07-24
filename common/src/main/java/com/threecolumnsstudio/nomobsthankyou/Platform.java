package com.threecolumnsstudio.nomobsthankyou;

import java.nio.file.Path;

public interface Platform {

    Path getConfigDir();

    static Platform get() {
        return PlatformHolder.INSTANCE;
    }

    static void set(Platform platform) {
        PlatformHolder.INSTANCE = platform;
    }
}

class PlatformHolder {
    static Platform INSTANCE;
}
