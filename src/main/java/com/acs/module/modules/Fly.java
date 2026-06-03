package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;
import com.acs.settings.ModeSetting;
import java.util.Arrays;

public class Fly extends Module {
    private final ModeSetting mode = new ModeSetting("Mode", "Vanilla", Arrays.asList("Vanilla", "Velocity"));
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);

    public Fly() {
        super("Fly", "Allows you to fly or glide in the air", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mode.getValue().equals("Vanilla")) {
            mc.player.getAbilities().flying = true;
            mc.player.getAbilities().setFlySpeed(speed.getValue().floatValue() * 0.05f);
        } else if (mode.getValue().equals("Velocity")) {
            mc.player.getAbilities().flying = false;
            double s = speed.getValue();
            double mx = 0, mz = 0;
            
            // Calculate movement vectors based on player rotation
            float yaw = mc.player.getYaw();
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

            double my = 0;
            if (mc.options.jumpKey.isPressed()) my += s;
            if (mc.options.sneakKey.isPressed()) my -= s;

            // Normalize and apply speed
            double len = Math.sqrt(mx * mx + mz * mz);
            if (len > 0) {
                mx = (mx / len) * s;
                mz = (mz / len) * s;
            }

            mc.player.setVelocity(mx, my, mz);
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = false;
        if (!mc.player.isCreative()) {
            mc.player.getAbilities().allowFlying = false;
        }
    }
}
