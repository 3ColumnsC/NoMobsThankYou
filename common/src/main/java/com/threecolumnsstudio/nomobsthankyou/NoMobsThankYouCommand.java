package com.threecolumnsstudio.nomobsthankyou;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.threecolumnsstudio.nomobsthankyou.config.ConfigIO;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.permissions.Permissions;

import java.util.List;
import java.util.function.Predicate;

public final class NoMobsThankYouCommand {

    private NoMobsThankYouCommand() {}

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(tree());
    }

    private static LiteralArgumentBuilder<CommandSourceStack> tree() {
        Predicate<CommandSourceStack> checkPermission = source ->
                source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);

        return Commands.literal("nmt")
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
                            boolean enabled = NoMobsThankYouConfig.isEnabled();
                            String preset = NoMobsThankYouConfig.getActivePresetName();
                            int count = NoMobsThankYouConfig.getBlockedCount();
                            String remove = String.join(", ", NoMobsThankYouConfig.getOverrideRemoveList());
                            String keep = String.join(", ", NoMobsThankYouConfig.getOverrideKeepList());

                            StringBuilder sb = new StringBuilder(
                                    "--- NoMobsThankYou Status ---\n"
                                    + "Enabled: " + (enabled ? "yes" : "no")
                                    + "\nPreset: " + (preset != null ? preset : "none")
                                    + "\nBlocked mobs: " + count
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
                );
    }
}
