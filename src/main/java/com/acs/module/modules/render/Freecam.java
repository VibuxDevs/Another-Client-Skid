package com.acs.module.modules.render;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.client.network.OtherClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

public class Freecam extends Module {

    private OtherClientPlayerEntity dummy;
    private Vec3d savedPos;
    private float savedYaw;
    private float savedPitch;

    public Freecam() {
        super("Freecam", "Leaves your body and allows you to move freely", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null) return;
        
        savedPos = mc.player.getPos();
        savedYaw = mc.player.getYaw();
        savedPitch = mc.player.getPitch();

        dummy = new OtherClientPlayerEntity(mc.world, mc.player.getGameProfile());
        dummy.copyPositionAndRotation(mc.player);
        dummy.setBoundingBox(mc.player.getBoundingBox());
        dummy.getInventory().clone(mc.player.getInventory());
        
        mc.world.addEntity(dummy);
        
        mc.player.noClip = true;
    }

    @Override
    public void onDisable() {
        if (mc.player == null || mc.world == null) return;
        
        mc.player.noClip = false;
        if (savedPos != null) {
            mc.player.updatePositionAndAngles(savedPos.x, savedPos.y, savedPos.z, savedYaw, savedPitch);
        }
        
        if (dummy != null) {
            mc.world.removeEntity(dummy.getId(), net.minecraft.entity.Entity.RemovalReason.DISCARDED);
            dummy = null;
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        mc.player.getAbilities().flying = true;
        mc.player.noClip = true;
    }
}
