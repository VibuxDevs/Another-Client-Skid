package com.acs.module.modules.movement;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.NumberSetting;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

public class ElytraFly extends Module {

    private final NumberSetting speed = new NumberSetting("Speed", 1.5, 0.1, 5.0, 0.1);
    private final NumberSetting fallSpeed = new NumberSetting("Fall Speed", 0.0, 0.0, 0.2, 0.01);
    private final BooleanSetting autoFly = new BooleanSetting("Auto Fly", true);

    public ElytraFly() {
        super("ElytraFly", "Grants full control over Elytra flight", Category.MOVEMENT);
        addSetting(speed);
        addSetting(fallSpeed);
        addSetting(autoFly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (autoFly.getValue() && !mc.player.isOnGround() && !mc.player.isFallFlying() && mc.player.fallDistance > 0.1f) {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            }
        }

        if (mc.player.isFallFlying()) {
            double currentSpeed = speed.getValue();
            
            Vec3d forward = Vec3d.fromPolar(0, mc.player.getYaw());
            Vec3d right = Vec3d.fromPolar(0, mc.player.getYaw() + 90);

            double velX = 0;
            double velY = -fallSpeed.getValue();
            double velZ = 0;

            if (mc.options.jumpKey.isPressed()) velY += currentSpeed;
            if (mc.options.sneakKey.isPressed()) velY -= currentSpeed;

            if (mc.options.forwardKey.isPressed()) {
                velX += forward.x * currentSpeed;
                velZ += forward.z * currentSpeed;
            }
            if (mc.options.backKey.isPressed()) {
                velX -= forward.x * currentSpeed;
                velZ -= forward.z * currentSpeed;
            }
            if (mc.options.rightKey.isPressed()) {
                velX += right.x * currentSpeed;
                velZ += right.z * currentSpeed;
            }
            if (mc.options.leftKey.isPressed()) {
                velX -= right.x * currentSpeed;
                velZ -= right.z * currentSpeed;
            }

            mc.player.setVelocity(velX, velY, velZ);
        }
    }
}
