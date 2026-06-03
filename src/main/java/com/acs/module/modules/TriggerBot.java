package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class TriggerBot extends Module {
    public TriggerBot() {
        super("TriggerBot", "Automatically attacks when looking at a living target", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null || mc.crosshairTarget == null) return;

        if (mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            EntityHitResult hitResult = (EntityHitResult) mc.crosshairTarget;
            Entity entity = hitResult.getEntity();

            if (entity instanceof LivingEntity && entity.isAlive() && entity != mc.player) {
                if (mc.player.getAttackCooldownProgress(0.0f) >= 1.0f) {
                    mc.interactionManager.attackEntity(mc.player, entity);
                    mc.player.swingHand(Hand.MAIN_HAND);
                }
            }
        }
    }
}
