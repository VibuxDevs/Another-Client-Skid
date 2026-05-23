package com.acs.module.modules.movement;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;

public class Flight extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);

    public Flight() {
        super("Flight", "Allows you to fly", Category.MOVEMENT);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        mc.player.getAbilities().flying = true;
        mc.player.getAbilities().setFlySpeed(speed.getValue().floatValue() * 0.05f);
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        
        if (!mc.player.isCreative() && !mc.player.isSpectator()) {
            mc.player.getAbilities().flying = false;
        }
        mc.player.getAbilities().setFlySpeed(0.05f); // default speed
    }
}
