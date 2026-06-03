package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.entity.Entity;

public class BoatPhase extends Module {
    public BoatPhase() {
        super("BoatPhase", "Allows riding vehicles (boats) to phase through solid blocks", Category.EXPLOITS);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle != null) {
            vehicle.noClip = true;
            mc.player.noClip = true;
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        mc.player.noClip = false;
        Entity vehicle = mc.player.getVehicle();
        if (vehicle != null) {
            vehicle.noClip = false;
        }
    }
}
