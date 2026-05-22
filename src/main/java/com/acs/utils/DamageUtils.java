package com.acs.utils;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class DamageUtils {
    private static final double CRYSTAL_DAMAGE_BASE = 12.0;
    private static final double CRYSTAL_FALLOFF = 0.75;
    private static final double CRYSTAL_MAX_RANGE = 12.0;
    
    private static final double SWORD_DAMAGE_BASE = 7.0;
    private static final double CRITICAL_MULTIPLIER = 1.5;
    private static final double ARMOR_REDUCTION_FACTOR = 0.04;
    private static final double ARMOR_TOUGHNESS_FACTOR = 0.02;

    /**
     * Calculate damage from an end crystal to a target player
     * @param target Target player
     * @param crystalPos Position of the crystal
     * @return Estimated damage
     */
public static float calculateCrystalDamage(PlayerEntity target, BlockPos crystalPos) {
    return calculateCrystalDamage(target, crystalPos, 0);
}

    /**
     * Calculate damage from an end crystal with position extrapolation
     * @param target Target player
     * @param crystalPos Position of the crystal
     * @param extrapolation Ticks to predict ahead (0 = no prediction)
     * @return Estimated damage
     */
    public static float calculateCrystalDamage(PlayerEntity target, BlockPos crystalPos, double extrapolation) {
        Vec3d targetPos = extrapolation > 0 
            ? target.getPos().add(target.getVelocity().multiply(extrapolation / 20.0)) 
            : target.getPos();
        
        double distToTarget = targetPos.distanceTo(Vec3d.ofCenter(crystalPos));
        
        if (distToTarget > CRYSTAL_MAX_RANGE) {
            return 0.5f;
        }
        float exposure = 1.0f - (float)(distToTarget / (6.0 * 2.0));
        float damage = (int)((exposure * exposure + exposure) / 2.0f * 7.0f * 12.0f + 1);
        
        damage = applyArmorReduction(damage, target);
        
        return Math.max(0.5f, damage);
    }

    /**
     * Calculate sword damage with optional critical hit
     * @param attacker Attacking player
     * @param target Target player
     * @param criticalHit Whether this is a critical hit
     * @return Estimated damage
     */
    public static float calculateSwordDamage(PlayerEntity attacker, PlayerEntity target, boolean criticalHit) {
        float damage = (float) SWORD_DAMAGE_BASE;
        
        if (criticalHit) {
            damage *= CRITICAL_MULTIPLIER;
        }
        
        // Apply armor reduction
        damage = applyArmorReduction(damage, target);
        
        return Math.max(0.5f, damage);
    }

    /**
     * Calculate sword damage with sharpness level
     * @param attacker Attacking player
     * @param target Target player
     * @param sharpnessLevel Sharpness enchantment level (0-5)
     * @param criticalHit Whether this is a critical hit
     * @return Estimated damage
     */
    public static float calculateSwordDamage(PlayerEntity attacker, PlayerEntity target, int sharpnessLevel, boolean criticalHit) {
        float damage = (float) SWORD_DAMAGE_BASE;
        
        damage += sharpnessLevel * 1.5f;
        
        if (criticalHit) {
            damage *= CRITICAL_MULTIPLIER;
        }
        
        damage = applyArmorReduction(damage, target);
        
        return Math.max(0.5f, damage);
    }

    /**
     * Apply armor and toughness reduction to damage
     * @param damage Base damage
     * @param target Target player with armor
     * @return Reduced damage
     */
    private static float applyArmorReduction(float damage, PlayerEntity target) {
        float armor = target.getArmor();
        float armorToughness = (float) target.getAttributeValue(net.minecraft.entity.attribute.EntityAttributes.GENERIC_ARMOR_TOUGHNESS);

        float reduction = Math.max(armor / 5.0f, armor - damage / (2.0f + armorToughness / 4.0f));
        reduction = Math.min(reduction, 20.0f) / 25.0f;
        
        return damage * (1.0f - reduction);
    }

    /**
     * Check if damage would be lethal
     * @param damage Damage amount
     * @param target Target player
     * @return True if damage would kill the player
     */
    public static boolean isLethal(float damage, PlayerEntity target) {
        return damage >= target.getHealth() + target.getAbsorptionAmount();
    }

    /**
     * Check if damage is sufficient (above minimum threshold)
     * @param damage Damage amount
     * @param minimumDamage Minimum required damage
     * @return True if damage meets threshold
     */
    public static boolean isAboveMinimum(float damage, double minimumDamage) {
        return damage > minimumDamage;
    }
}
