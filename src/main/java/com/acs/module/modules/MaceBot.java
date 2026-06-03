package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.NumberSetting;
import com.acs.utils.FriendManager;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.util.Hand;

public class MaceBot extends Module {
    private final NumberSetting speed = new NumberSetting("Speed", 1.5, 0.5, 5.0, 0.1);
    private final NumberSetting height = new NumberSetting("HeightOffset", 15.0, 5.0, 30.0, 1.0);
    private final BooleanSetting useFireworks = new BooleanSetting("UseFireworks", true);

    private int fireworkCooldown = 0;

    public MaceBot() {
        super("MaceBot", "Automatically flies with Elytra and drops on players to Mace them", Category.EXPLOITS);
        addSetting(speed);
        addSetting(height);
        addSetting(useFireworks);
    }

    @Override
    public void onEnable() {
        fireworkCooldown = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;

        if (fireworkCooldown > 0) {
            fireworkCooldown--;
        }

        // Ensure player is holding or selects the Mace
        int maceSlot = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.MACE) {
                maceSlot = i;
                break;
            }
        }
        if (maceSlot != -1) {
            mc.player.getInventory().selectedSlot = maceSlot;
        } else {
            // No Mace found in hotbar, cannot exploit
            return;
        }

        // Check if Elytra is equipped
        boolean hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).getItem() == Items.ELYTRA;
        if (!hasElytra) return;

        // Auto-activate Elytra
        if (!mc.player.isFallFlying()) {
            mc.player.jump();
            mc.player.networkHandler.sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
            return;
        }

        // Find nearest target player (not self, not friend)
        PlayerEntity target = null;
        double nearestDist = Double.MAX_VALUE;

        for (PlayerEntity entity : mc.world.getPlayers()) {
            if (entity == mc.player) continue;
            if (FriendManager.INSTANCE.isFriend(entity.getName().getString())) continue;
            if (!entity.isAlive()) continue;

            double dist = mc.player.distanceTo(entity);
            if (dist < nearestDist) {
                nearestDist = dist;
                target = entity;
            }
        }

        if (target == null) return;

        double dx = target.getX() - mc.player.getX();
        double dy = (target.getY() + height.getValue()) - mc.player.getY();
        double dz = target.getZ() - mc.player.getZ();
        double horizontalDist = Math.sqrt(dx * dx + dz * dz);

        if (horizontalDist < 2.0) {
            // Directly above target, dive down fast
            mc.player.setVelocity(0, -2.0, 0);
            
            // Aim straight down
            mc.player.setYaw((float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f);
            mc.player.setPitch(90.0f);
        } else {
            // Check if we need to boost height using fireworks
            if (useFireworks.getValue() && mc.player.getY() < target.getY() + height.getValue() - 2.0 && fireworkCooldown == 0) {
                int fireworkSlot = -1;
                for (int i = 0; i < 9; i++) {
                    if (mc.player.getInventory().getStack(i).getItem() == Items.FIREWORK_ROCKET) {
                        fireworkSlot = i;
                        break;
                    }
                }

                if (fireworkSlot != -1) {
                    int prevSlot = mc.player.getInventory().selectedSlot;
                    mc.player.getInventory().selectedSlot = fireworkSlot;
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.player.getInventory().selectedSlot = prevSlot;
                    fireworkCooldown = 30; // 1.5 second cooldown between boosts
                    return;
                }
            }

            // Glide toward the Y-offset point above the target
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (dist > 0) {
                double s = speed.getValue();
                double vx = (dx / dist) * s;
                double vy = (dy / dist) * s;
                double vz = (dz / dist) * s;
                mc.player.setVelocity(vx, vy, vz);

                // Rotate body and head to face the waypoint
                float yaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
                float pitch = (float) -Math.toDegrees(Math.atan2(vy, Math.sqrt(vx * vx + vz * vz)));
                mc.player.setYaw(yaw);
                mc.player.setPitch(pitch);
            }
        }

        // Attack if target gets in reach during fall/dive
        if (mc.player.distanceTo(target) <= 4.0f) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }
}
