package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.NumberSetting;
import net.minecraft.entity.Entity;

public class BoatFly extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 1.0, 0.1, 5.0, 0.1);
    private final NumberSetting verticalSpeed = new NumberSetting("VerticalSpeed", 0.5, 0.1, 3.0, 0.1);
    private final BooleanSetting glide = new BooleanSetting("Glide", true);

    public BoatFly() {
        super("BoatFly", "Allows you to fly while riding a boat", Category.MOVEMENT);
        addSetting(speed);
        addSetting(verticalSpeed);
        addSetting(glide);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle == null) return;

        // Sync rotations to make steering easier
        vehicle.setYaw(mc.player.getYaw());

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
            mx = (mx / len) * speed.getValue();
            mz = (mz / len) * speed.getValue();
        }

        double my = 0;
        if (mc.options.jumpKey.isPressed()) {
            my = verticalSpeed.getValue();
        } else if (mc.options.sneakKey.isPressed()) {
            my = -verticalSpeed.getValue();
        } else {
            if (glide.getValue()) {
                my = 0;
            } else {
                my = vehicle.getVelocity().y;
            }
        }

        vehicle.setVelocity(mx, my, mz);
        
        // Reset fall distance to prevent damage on landing
        mc.player.fallDistance = 0;
        vehicle.fallDistance = 0;
    }
}
