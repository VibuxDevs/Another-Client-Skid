package com.acs.module.modules.pvp;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;

public class KillAura extends Module {
    public KillAura() {
        super("KillAura", "Attacks players around you", Category.COMBAT);
    }

    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity && entity != mc.player) {
                if (mc.player.distanceTo(entity) <= 4.0f) {
                    mc.interactionManager.attackEntity(mc.player, entity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    break;
                }
            }
        }
    }
}
