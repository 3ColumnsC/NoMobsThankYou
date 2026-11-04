package com.threecolumnsstudio.nomobsthankyou.neoforge;

import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYou;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouCommand;
import com.threecolumnsstudio.nomobsthankyou.NoMobsThankYouConfig;
import com.threecolumnsstudio.nomobsthankyou.Platform;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerAboutToStartEvent;

@Mod(NoMobsThankYou.MOD_ID)
public class NoMobsThankYouNeoForge {

    public NoMobsThankYouNeoForge(IEventBus modEventBus) {
        Platform.set(new NeoforgePlatform());
        NoMobsThankYou.init();

        NeoForge.EVENT_BUS.addListener((ServerAboutToStartEvent event) -> NoMobsThankYouConfig.load());

        NeoForge.EVENT_BUS.addListener(PlayerEvent.PlayerLoggedInEvent.class, event -> {
            for (String error : NoMobsThankYouConfig.getLoadErrors()) {
                event.getEntity().sendSystemMessage(
                        Component.literal("§e[NoMobsThankYou] " + error));
            }
        });

        NeoForge.EVENT_BUS.addListener((RegisterCommandsEvent event) ->
                NoMobsThankYouCommand.register(event.getDispatcher()));
    }
}
