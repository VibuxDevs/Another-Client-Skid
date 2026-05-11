package com.acs;

import com.acs.command.CommandManager;
import com.acs.module.ModuleManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;

public class Main implements ClientModInitializer {
    @Override
    public void onInitializeClient() {
        ModuleManager manager = ModuleManager.INSTANCE;

        ClientSendMessageEvents.ALLOW_CHAT.register(message -> {
            if (message.startsWith(CommandManager.PREFIX)) {
                CommandManager.INSTANCE.handleChat(message);
                return false;
            }
            return true;
        });

        System.out.println("ACS Client Initialized!");
    }
}
