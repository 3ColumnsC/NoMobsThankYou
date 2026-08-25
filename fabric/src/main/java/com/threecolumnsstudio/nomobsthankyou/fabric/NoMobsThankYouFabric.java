package com.threecolumnsstudio.nomobsthankyou.fabric;

import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouConfig;
import com.threecolumnsstudio.nomobsthankyou.Platform;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYou;
import com.threecolumnsstudio.nomobsthankyou.config.ConfigIO;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.Predicate;

public class NoMobsThankYouFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Platform.set(new FabricPlatform());
        NoMobsThankYou.init();

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (String error : NoMobsThankYouConfig.getLoadErrors()) {
                handler.getPlayer().sendSystemMessage(
                    Component.literal("§e[NoMobsThankYou] " + error));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            Predicate<CommandSourceStack> checkPermission = source ->
                source.hasPermission(2);

            dispatcher.register(
                Commands.literal("nomobsthankyou")
                    .then(Commands.literal("reload")
                        .requires(checkPermission)
                        .executes(ctx -> {
                            NoMobsThankYouConfig.reload();
                            List<String> errors = NoMobsThankYouConfig.getLoadErrors();
                            if (errors.isEmpty()) {
                                ctx.getSource().sendSuccess(() ->
                                    Component.literal("NoMobsThankYou config reloaded"), true);
                            } else {
                                ctx.getSource().sendSuccess(() ->
                                    Component.literal("§e[NoMobsThankYou] Config reloaded with errors"), true);
                                for (String error : errors) {
                                    ctx.getSource().sendSuccess(() ->
                                        Component.literal("§e[NoMobsThankYou] " + error), true);
                                }
                            }
                            return 1;
                        })
                    )
                    .then(Commands.literal("status")
                        .requires(checkPermission)
                        .executes(ctx -> {
                            String preset = NoMobsThankYouConfig.getActivePresetName();
                            int count = NoMobsThankYouConfig.getBlockedCount();
                            String remove = String.join(", ", NoMobsThankYouConfig.getOverrideRemoveList());
                            String keep = String.join(", ", NoMobsThankYouConfig.getOverrideKeepList());

                            StringBuilder sb = new StringBuilder(
                                "--- NoMobsThankYou Status ---\n"
                                + "Preset: " + (preset != null ? preset + " (" + count + ")" : "none")
                                + "\nOverride remove: [" + remove + "]"
                                + "\nOverride keep: [" + keep + "]"
                            );
                            for (String error : NoMobsThankYouConfig.getLoadErrors()) {
                                sb.append("\n§eError: ").append(error);
                            }
                            Component msg = Component.literal(sb.toString());
                            ctx.getSource().sendSuccess(() -> msg, false);
                            return 1;
                        })
                    )
                    .then(Commands.literal("clean")
                        .requires(checkPermission)
                        .then(Commands.literal("presets")
                            .executes(ctx -> {
                                NoMobsThankYouConfig.cleanPresets();
                                ctx.getSource().sendSuccess(() ->
                                    Component.literal("Presets reset to defaults and config reloaded"), true);
                                return 1;
                            })
                        )
                        .then(Commands.literal("list")
                            .executes(ctx -> {
                                NoMobsThankYouConfig.cleanOverrides();
                                ctx.getSource().sendSuccess(() ->
                                    Component.literal("Override lists cleared and config reloaded"), true);
                                return 1;
                            })
                        )
                    )
                    .then(Commands.literal("open")
                        .requires(checkPermission)
                        .then(Commands.literal("presets")
                            .executes(ctx -> {
                                NoMobsThankYou.openConfigFile(
                                    ctx.getSource(),
                                    Platform.get().getConfigDir().resolve(ConfigIO.PRESETS_FILE),
                                    "presets"
                                );
                                return 1;
                            })
                        )
                        .then(Commands.literal("list")
                            .executes(ctx -> {
                                NoMobsThankYou.openConfigFile(
                                    ctx.getSource(),
                                    Platform.get().getConfigDir().resolve(ConfigIO.OVERRIDES_FILE),
                                    "overrides"
                                );
                                return 1;
                            })
                        )
                    )
            );
        });
    }
}