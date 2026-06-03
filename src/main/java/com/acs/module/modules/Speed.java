package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;

public class Speed extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);

    public Speed() {
        super("Speed", "Increases your movement speed on land", Category.MOVEMENT);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.forwardSpeed != 0 || mc.player.sidewaysSpeed != 0) {
            float yaw = mc.player.getYaw();
            double mx = 0, mz = 0;
            if (mc.options.forwardKey.isPressed()) {
                mx += Math.cos(Math.toRadians(yaw + 90));
                mz += Math.sin(Math.toRadians(yaw + 90));
            }
            if (mc.options.backKey.isPressed()) {
                mx -= Math.cos(Math.toRadians(yaw + 90));
                mz -= Math.sin(Math.toRadians(yaw + 90));
            }
            if (mc.options.leftKey.isPressed()) {
                mx += Math.cos(Math.toRadians(yaw));
                mz += Math.sin(Math.toRadians(yaw));
            }
            if (mc.options.rightKey.isPressed()) {
                mx -= Math.cos(Math.toRadians(yaw));
                mz -= Math.sin(Math.toRadians(yaw));
            }

            double len = Math.sqrt(mx * mx + mz * mz);
            if (len > 0) {
                // 0.2873 is the base movement speed value in Minecraft
                mx = (mx / len) * speed.getValue() * 0.2873;
                mz = (mz / len) * speed.getValue() * 0.2873;
            }

            mc.player.setVelocity(mx, mc.player.getVelocity().y, mz);
        }
    }
}
