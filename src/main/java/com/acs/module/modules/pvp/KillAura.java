package com.acs.module.modules.pvp;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.NumberSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.util.Hand;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class KillAura extends Module {
    private final NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.1);
    private final BooleanSetting waitCooldown = new BooleanSetting("Wait Cooldown", true);
    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);

    public KillAura() {
        super("KillAura", "Attacks entities around you", Category.COMBAT);
        addSetting(range);
        addSetting(waitCooldown);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (waitCooldown.getValue() && mc.player.getAttackCooldownProgress(0.5f) < 1.0f) return;

        List<Entity> targets = StreamSupport.stream(mc.world.getEntities().spliterator(), false)
                .filter(this::isValidTarget)
                .sorted(Comparator.comparingDouble(e -> mc.player.distanceTo(e)))
                .collect(Collectors.toList());

        if (!targets.isEmpty()) {
            Entity target = targets.get(0);
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private boolean isValidTarget(Entity entity) {
        if (entity == mc.player || !entity.isAlive()) return false;
        if (mc.player.distanceTo(entity) > range.getValue()) return false;

        if (entity instanceof PlayerEntity && players.getValue()) return true;
        if (entity instanceof HostileEntity && mobs.getValue()) return true;
        if (entity instanceof AnimalEntity && animals.getValue()) return true;

        return false;
    }
}
