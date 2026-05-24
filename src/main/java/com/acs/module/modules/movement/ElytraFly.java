package com.acs.module.modules.movement;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.ModeSetting;
import com.acs.settings.NumberSetting;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.math.Vec3d;

import java.util.Arrays;

public class ElytraFly extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Control", Arrays.asList("Control", "Bounce"));
    
    // Control Mode Settings
    private final NumberSetting speed = new NumberSetting("Speed", 1.5, 0.1, 5.0, 0.1);
    private final NumberSetting fallSpeed = new NumberSetting("Fall Speed", 0.0, 0.0, 0.2, 0.01);
    private final BooleanSetting autoFly = new BooleanSetting("Auto Fly", true);

    // Bounce Mode Settings
    private final NumberSetting bounceSpeed = new NumberSetting("Bounce Speed", 1.5, 0.5, 5.0, 0.1);

    // Infinite Durability
    private final BooleanSetting infiniteDurability = new BooleanSetting("Infinite Durability", true);
    private int swapTimer = 0;

    public ElytraFly() {
        super("ElytraFly", "Grants full control or bouncing mechanics over Elytra flight", Category.MOVEMENT);
        addSetting(mode);
        addSetting(speed);
        addSetting(fallSpeed);
        addSetting(autoFly);
        addSetting(bounceSpeed);
        addSetting(infiniteDurability);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isFallFlying() && infiniteDurability.getValue()) {
            swapTimer++;
            if (swapTimer >= 15) {
                swapTimer = 0;
                if (mc.interactionManager != null) {
                    // Swap chestplate (slot 6) with hotbar slot 0, then swap it back
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 6, 0, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, 6, 0, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    }
                }
            }
        }

        if (mode.getValue().equals("Bounce")) {
            if (!mc.player.getInventory().getArmorStack(2).getItem().toString().contains("elytra")) return;

            if (mc.player.isOnGround()) {
                mc.player.jump();
            } else {
                if (!mc.player.isFallFlying()) {
                    mc.player.startFallFlying();
                    if (mc.getNetworkHandler() != null) {
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    }
                }
            }
        } else {
            if (autoFly.getValue() && !mc.player.isOnGround() && !mc.player.isFallFlying() && mc.player.fallDistance > 0.1f) {
                if (mc.getNetworkHandler() != null) {
                    mc.player.startFallFlying();
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                }
            }
        }
    }

    public void handleTravel(ClientPlayerEntity player) {
        if (mode.getValue().equals("Bounce")) {
            handleBounceMode(player);
        } else {
            handleControlMode(player);
        }
    }

    private void handleControlMode(ClientPlayerEntity player) {
        double currentSpeed = speed.getValue();
        
        Vec3d forward = Vec3d.fromPolar(0, player.getYaw());
        Vec3d right = Vec3d.fromPolar(0, player.getYaw() + 90);

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

        player.setVelocity(velX, velY, velZ);
        player.move(net.minecraft.entity.MovementType.SELF, player.getVelocity());
    }

    private void handleBounceMode(ClientPlayerEntity player) {
        double currentSpeed = bounceSpeed.getValue();
        Vec3d forward = Vec3d.fromPolar(0, player.getYaw());
        Vec3d right = Vec3d.fromPolar(0, player.getYaw() + 90);

        double velX = 0;
        double velZ = 0;

        boolean moving = mc.options.forwardKey.isPressed() || mc.options.backKey.isPressed() || 
                         mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();

        if (moving) {
            if (mc.options.forwardKey.isPressed()) {
                velX += forward.x;
                velZ += forward.z;
            }
            if (mc.options.backKey.isPressed()) {
                velX -= forward.x;
                velZ -= forward.z;
            }
            if (mc.options.rightKey.isPressed()) {
                velX += right.x;
                velZ += right.z;
            }
            if (mc.options.leftKey.isPressed()) {
                velX -= right.x;
                velZ -= right.z;
            }

            Vec3d moveDir = new Vec3d(velX, 0, velZ).normalize();
            velX = moveDir.x * currentSpeed;
            velZ = moveDir.z * currentSpeed;
        }

        double velY = player.getVelocity().y - 0.08;
        if (velY < -3.0) velY = -3.0;

        player.setVelocity(velX, velY, velZ);
        player.move(net.minecraft.entity.MovementType.SELF, player.getVelocity());
    }
}
