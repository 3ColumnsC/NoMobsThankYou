package com.threecolumnsstudio.nomobsthankyou.neoforge;

import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouConfig;
import com.threecolumnsstudio.nomobsthankyou.Platform;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYou;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.permissions.Permissions;
import net.minecraft.network.chat.Component;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;

import java.util.List;
import java.util.function.Predicate;

@Mod(NoMobsThankYou.MOD_ID)
public class NoMobsThankYouNeoForge {

    public NoMobsThankYouNeoForge(IEventBus modEventBus) {
        Platform.set(new NeoforgePlatform());
        NoMobsThankYou.init();

        NeoForge.EVENT_BUS.addListener(EntityJoinLevelEvent.class, event -> {
            if (!event.loadedFromDisk() && NoMobsThankYouConfig.shouldRemove(event.getEntity().getType())) {
                event.setCanceled(true);
            }
        });

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            for (String error : NoMobsThankYouConfig.getLoadErrors()) {
                event.getEntity().sendSystemMessage(
                    Component.literal("§e[NoMobsThankYou] " + error));
            }
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) -> {
            Predicate<CommandSourceStack> checkPermission = source ->
                source.permissions().hasPermission(Permissions.COMMANDS_GAMEMASTER);

            event.getDispatcher().register(
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
                                    Platform.get().getConfigDir().resolve("nomobsthankyou-presets.json"),
                                    "presets"
                                );
                                return 1;
                            })
                        )
                        .then(Commands.literal("list")
                            .executes(ctx -> {
                                NoMobsThankYou.openConfigFile(
                                    ctx.getSource(),
                                    Platform.get().getConfigDir().resolve("nomobsthankyou-overrides.json"),
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
