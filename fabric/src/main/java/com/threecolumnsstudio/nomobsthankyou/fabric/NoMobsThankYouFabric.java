package com.threecolumnsstudio.nomobsthankyou.fabric;

import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYou;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouCommand;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouConfig;
import com.threecolumnsstudio.nomobsthankyou.Platform;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.network.chat.Component;

public class NoMobsThankYouFabric implements ModInitializer {

    @Override
    public void onInitialize() {
        Platform.set(new FabricPlatform());
        NoMobsThankYou.init();

        ServerLifecycleEvents.SERVER_STARTING.register(server -> NoMobsThankYouConfig.load());

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            for (String error : NoMobsThankYouConfig.getLoadErrors()) {
                handler.getPlayer().sendSystemMessage(
                        Component.literal("§e[NoMobsThankYou] " + error));
            }
        });

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) ->
                NoMobsThankYouCommand.register(dispatcher));
    }
}
