package com.threecolumnsstudio.nomobsthankyou;

import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public final class NoMobsThankYou {

    public static final String MOD_ID = "nomobsthankyou";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private NoMobsThankYou() {}

    public static void init() {
        NoMobsThankYouConfig.load();
        LOGGER.info("{} initialized", MOD_ID);
    }

    public static void openConfigFile(CommandSourceStack source, Path path, String configName) {
        String pathStr = path.toAbsolutePath().normalize().toString();

        if (Desktop.isDesktopSupported()) {
            try {
                Desktop desktop = Desktop.getDesktop();
                if (desktop.isSupported(Desktop.Action.EDIT)) {
                    desktop.edit(path.toFile());
                    source.sendSuccess(() -> Component.literal("Opening " + configName + " config..."), true);
                    return;
                }
            } catch (Exception ignored) { }
        }

        String os = System.getProperty("os.name").toLowerCase(Locale.ROOT);
        try {
            if (os.contains("win")) {
                new ProcessBuilder("notepad.exe", pathStr).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", pathStr).start();
            } else {
                new ProcessBuilder("xdg-open", pathStr).start();
            }
            source.sendSuccess(() -> Component.literal("Opening " + configName + " config..."), true);
        } catch (IOException e) {
            source.sendFailure(Component.literal("Could not open editor, try manually: " + pathStr));
        }
    }
}
