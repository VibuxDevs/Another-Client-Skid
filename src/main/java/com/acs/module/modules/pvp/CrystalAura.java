package com.acs.module.modules.pvp;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.NumberSetting;
import com.acs.utils.DamageUtils;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.HandSwingC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class CrystalAura extends Module {

    public CrystalAura() {
        super("CrystalAura", "Automatically places and attacks end crystals", Category.COMBAT);
        initializeSettings();
    }

    public boolean attackEnabled     = true;
    public double  attackSpeed       = 20.0;
    public double  attackRange       = 6.0;
    public boolean instantAttack     = true;

    public boolean placeEnabled      = true;
    public double  placeSpeed        = 20.0;
    public double  placeRange        = 6.0;

    public boolean autoSwitch        = true;
    public double  minimumDamage     = 6.0;
    public double  maximumSelfDamage = 10.0;
    public double  lethalMultiplier  = 1.5;
    public boolean antiSuicide       = true;
    public double  enemyRange        = 10.0;
    public double  extrapolation     = 0.0;

    private long             lastAttackTime        = 0;
    private long             lastPlaceTime         = 0;
    private EndCrystalEntity targetCrystal         = null;
    private BlockPos         targetPlacement       = null;
    private long             lastCpsUpdateTime     = 0;
    private int              crystalsHitThisSecond = 0;
    private double           currentCps            = 0.0;

    private void initializeSettings() {
        addSetting(new BooleanSetting("Attack Enabled",   attackEnabled));
        addSetting(new NumberSetting("Attack Speed",      attackSpeed,       0.1, 20.0, 0.5));
        addSetting(new NumberSetting("Attack Range",      attackRange,       0.0,  6.0, 0.5));
        addSetting(new BooleanSetting("Instant Attack",   instantAttack));
        addSetting(new BooleanSetting("Place Enabled",    placeEnabled));
        addSetting(new NumberSetting("Place Speed",       placeSpeed,        0.1, 20.0, 0.5));
        addSetting(new NumberSetting("Place Range",       placeRange,        0.0, 20.0, 0.5));
        addSetting(new BooleanSetting("Auto Switch",      autoSwitch));
        addSetting(new NumberSetting("Minimum Damage",    minimumDamage,     0.0, 20.0, 0.5));
        addSetting(new NumberSetting("Max Self Damage",   maximumSelfDamage, 0.0, 36.0, 0.5));
        addSetting(new NumberSetting("Lethal Multiplier", lethalMultiplier,  0.0,  2.0, 0.1));
        addSetting(new BooleanSetting("Anti Suicide",     antiSuicide));
        addSetting(new NumberSetting("Enemy Range",       enemyRange,        0.0, 50.0, 1.0));
        addSetting(new NumberSetting("Extrapolation",     extrapolation,     0.0, 20.0, 1.0));
    }

    @Override
    public void onEnable() {}

    @Override
    public void onDisable() {}

    @Override
    public void onTick() {
        syncSettings();
        if (mc.player == null || mc.world == null) return;
        if (attackEnabled) attackCrystals();
        if (placeEnabled)  placeCrystals();
    }

    private void syncSettings() {
        for (com.acs.settings.Setting<?> setting : getSettings()) {
            String name  = setting.getName();
            Object value = setting.getValue();

            if (value instanceof Boolean b) {
                switch (name) {
                    case "Attack Enabled"  -> attackEnabled     = b;
                    case "Instant Attack"  -> instantAttack     = b;
                    case "Place Enabled"   -> placeEnabled      = b;
                    case "Auto Switch"     -> autoSwitch        = b;
                    case "Anti Suicide"    -> antiSuicide       = b;
                }
            } else if (value instanceof Double d) {
                switch (name) {
                    case "Attack Speed"      -> attackSpeed       = d;
                    case "Attack Range"      -> attackRange       = d;
                    case "Place Speed"       -> placeSpeed        = d;
                    case "Place Range"       -> placeRange        = d;
                    case "Minimum Damage"    -> minimumDamage     = d;
                    case "Max Self Damage"   -> maximumSelfDamage = d;
                    case "Lethal Multiplier" -> lethalMultiplier  = d;
                    case "Enemy Range"       -> enemyRange        = d;
                    case "Extrapolation"     -> extrapolation     = d;
                }
            }
        }
    }

    private void attackCrystals() {
        long currentTime = System.currentTimeMillis();
        long attackDelay = instantAttack ? 0 : (long) (1000.0 / attackSpeed);
        if (currentTime - lastAttackTime < attackDelay) return;

        EndCrystalEntity bestCrystal = null;
        float bestDamage = 0.0f;

        for (Entity entity : mc.world.getEntities()) {
            if (!(entity instanceof EndCrystalEntity crystal)) continue;
            if (!crystal.isAlive()) continue;

            double distSq = crystal.getBoundingBox().squaredMagnitude(mc.player.getEyePos());
            if (distSq > MathHelper.square(attackRange)) continue;
            if (!mc.world.getWorldBorder().contains(crystal.getBlockPos())) continue;

            float selfDamage = DamageUtils.calculateCrystalDamage(mc.player, crystal.getBlockPos(), extrapolation);
            if (selfDamage > maximumSelfDamage) continue;
            if (antiSuicide && DamageUtils.isLethal(selfDamage, mc.player)) continue;

            for (PlayerEntity enemy : mc.world.getPlayers()) {
                if (enemy == mc.player || !enemy.isAlive()) continue;
                if (mc.player.squaredDistanceTo(enemy) > MathHelper.square(enemyRange)) continue;

                float damage = DamageUtils.calculateCrystalDamage(enemy, crystal.getBlockPos(), extrapolation);
                if (!DamageUtils.isAboveMinimum(damage, minimumDamage)) continue;

                float effectiveDamage = DamageUtils.isLethal(damage, enemy)
                        ? damage * (float) lethalMultiplier
                        : damage;

                if (effectiveDamage > bestDamage) {
                    bestDamage = effectiveDamage;
                    bestCrystal = crystal;
                }
            }
        }

        if (bestCrystal != null && bestCrystal.isAlive()) {
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler().sendPacket(
                        PlayerInteractEntityC2SPacket.attack(bestCrystal, mc.player.isSneaking()));
                mc.getNetworkHandler().sendPacket(new HandSwingC2SPacket(Hand.MAIN_HAND));
            }
            lastAttackTime = currentTime;
            targetCrystal  = bestCrystal;
            updateCrystalHitCount();
        }
    }

    private void placeCrystals() {
        long currentTime = System.currentTimeMillis();
        long placeDelay  = (long) (1000.0 / placeSpeed);
        if (currentTime - lastPlaceTime < placeDelay) return;

        BlockPos bestPlacement  = null;
        float    bestDamage     = 0.0f;
        double   closestToEnemy = Double.MAX_VALUE;
        int      searchRadius   = (int) Math.ceil(placeRange);

        for (int x = -searchRadius; x <= searchRadius; x++) {
            for (int y = -searchRadius; y <= searchRadius; y++) {
                for (int z = -searchRadius; z <= searchRadius; z++) {
                    BlockPos position = mc.player.getBlockPos().add(x, y, z);

                    double distSq = mc.player.getPos().squaredDistanceTo(
                            Vec3d.of(position).add(0.5, 1.0, 0.5));
                    if (distSq > MathHelper.square(placeRange)) continue;

                    if (!mc.world.getWorldBorder().contains(position)) continue;

                    if (mc.world.getBlockState(position).getBlock() != Blocks.OBSIDIAN &&
                        mc.world.getBlockState(position).getBlock() != Blocks.BEDROCK) continue;

                    if (!mc.world.getBlockState(position.up()).isAir()) continue;

                    Box placementBox = new Box(position.up());
                    if (mc.world.getOtherEntities(null, placementBox).stream()
                            .anyMatch(e -> e.isAlive() && !(e instanceof EndCrystalEntity))) continue;

                    float selfDamage = DamageUtils.calculateCrystalDamage(mc.player, position, extrapolation);
                    if (selfDamage > maximumSelfDamage) continue;
                    if (antiSuicide && DamageUtils.isLethal(selfDamage, mc.player)) continue;

                    for (PlayerEntity player : mc.world.getPlayers()) {
                        if (player == mc.player || !player.isAlive()) continue;
                        if (mc.player.squaredDistanceTo(player) > MathHelper.square(enemyRange)) continue;

                        float  damage      = DamageUtils.calculateCrystalDamage(player, position, extrapolation);
                        double distToEnemy = Vec3d.ofCenter(position).squaredDistanceTo(player.getPos());

                        if (DamageUtils.isAboveMinimum(damage, minimumDamage)) {
                            if (damage > bestDamage || (damage == bestDamage && distToEnemy < closestToEnemy)) {
                                bestPlacement  = position;
                                bestDamage     = damage;
                                closestToEnemy = distToEnemy;
                            }
                        }
                    }
                }
            }
        }

        if (bestPlacement != null && bestDamage > 0) {
            boolean hasCrystal = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
            if (!hasCrystal && autoSwitch) {
                findAndSwitchToCrystal();
                hasCrystal = mc.player.getMainHandStack().getItem() == Items.END_CRYSTAL || mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL;
            }
            if (hasCrystal) {
                placeCrystalAtPosition(bestPlacement);
                lastPlaceTime   = currentTime;
                targetPlacement = bestPlacement;
            }
        }
    }

    private void placeCrystalAtPosition(BlockPos position) {
        if (mc.getNetworkHandler() == null || mc.player == null) return;
        Hand hand = mc.player.getOffHandStack().getItem() == Items.END_CRYSTAL
                ? Hand.OFF_HAND
                : Hand.MAIN_HAND;

        BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(position).add(0, 0.5, 0),
                Direction.UP,
                position,
                false);

        mc.interactionManager.interactBlock(mc.player, hand, hitResult);
        mc.player.swingHand(hand);
    }

    private void findAndSwitchToCrystal() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() == Items.END_CRYSTAL) {
                mc.player.getInventory().selectedSlot = i;
                if (mc.getNetworkHandler() != null) {
                    mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(i));
                }
                return;
            }
        }
    }

    private void updateCrystalHitCount() {
        long currentTime = System.currentTimeMillis();
        crystalsHitThisSecond++;
        if (currentTime - lastCpsUpdateTime > 1000) {
            currentCps            = crystalsHitThisSecond;
            crystalsHitThisSecond = 0;
            lastCpsUpdateTime     = currentTime;
        }
    }

    public double getCurrentCps() {
        return currentCps;
    }
}