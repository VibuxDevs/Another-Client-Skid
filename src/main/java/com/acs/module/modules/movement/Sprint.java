package com.acs.module.modules.movement;

import com.acs.module.Category;
import com.acs.module.Module;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", "Automatically sprints", Category.MOVEMENT);
        addSetting(new com.acs.settings.ModeSetting("Mode", "Rage", java.util.Arrays.asList("Rage", "Legit")));
    }

    public void onTick() {
        if (mc.player != null && (mc.player.forwardSpeed > 0 || mc.player.sidewaysSpeed > 0)) {
            mc.player.setSprinting(true);
        }
    }
}
