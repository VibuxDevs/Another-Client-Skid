package com.acs.module.modules.player;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.NumberSetting;
import com.acs.settings.StringSetting;

import java.util.Random;

public class Spammer extends Module {
    private final StringSetting message = new StringSetting("Message", "Another Client Skid on Top!");
    private final NumberSetting delay = new NumberSetting("Delay (s)", 5.0, 0.5, 30.0, 0.5);
    private final BooleanSetting antiSpam = new BooleanSetting("AntiSpam Bypass", true);

    private long lastSentTime = 0;
    private final Random random = new Random();

    public Spammer() {
        super("Spammer", "Spams messages in the chat automatically with anti-spam bypass options", Category.MISC);
        addSetting(message);
        addSetting(delay);
        addSetting(antiSpam);
    }

    @Override
    public void onEnable() {
        lastSentTime = System.currentTimeMillis();
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        long requiredDelayMs = (long) (delay.getValue() * 1000.0);

        if (now - lastSentTime >= requiredDelayMs) {
            String baseMessage = message.getValue();
            if (baseMessage == null || baseMessage.isEmpty()) return;

            String messageToSend = baseMessage;
            if (antiSpam.getValue()) {
                // Generate a random 4-letter suffix to bypass duplicate message filters
                StringBuilder suffix = new StringBuilder(" [");
                for (int i = 0; i < 4; i++) {
                    suffix.append((char) ('a' + random.nextInt(26)));
                }
                suffix.append("]");
                messageToSend += suffix.toString();
            }

            if (mc.player.networkHandler != null) {
                mc.player.networkHandler.sendChatMessage(messageToSend);
            }
            lastSentTime = now;
        }
    }
}
