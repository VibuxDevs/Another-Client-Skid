package com.acs.module.modules.world;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

    private final BooleanSetting tower = new BooleanSetting("Tower", true);
    private final NumberSetting extend = new NumberSetting("Extend", 0.0, 0.0, 5.0, 1.0);
    private final BooleanSetting keepY = new BooleanSetting("Keep Y", false);
    private final BooleanSetting safeWalk = new BooleanSetting("SafeWalk", true);

    private int startY;

    public Scaffold() {
        super("Scaffold", "Automatically places blocks under you", Category.WORLD);
        addSetting(tower);
        addSetting(extend);
        addSetting(keepY);
        addSetting(safeWalk);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            startY = (int) mc.player.getY();
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (keepY.getValue() && !mc.player.getAbilities().flying && !mc.options.jumpKey.isPressed()) {
            // Adjust startY if we fall below it so we don't scaffold in the air above us
            if (mc.player.getY() < startY) {
                startY = (int) mc.player.getY();
            }
        } else {
            startY = (int) mc.player.getY();
        }

        // Tower logic
        if (tower.getValue() && mc.options.jumpKey.isPressed() && mc.player.getVelocity().x == 0 && mc.player.getVelocity().z == 0) {
            mc.player.setVelocity(0, 0.42, 0);
        }

        int ext = extend.getValue().intValue();
        Vec3d velocity = mc.player.getVelocity();
        Vec3d forward = new Vec3d(velocity.x, 0, velocity.z).normalize();
        if (velocity.x == 0 && velocity.z == 0) forward = Vec3d.ZERO;

        int blockSlot = getBlockSlot();
        if (blockSlot == -1) return;

        for (int i = 0; i <= ext; i++) {
            BlockPos targetPos = new BlockPos(
                    (int) Math.floor(mc.player.getX() + forward.x * i),
                    startY - 1,
                    (int) Math.floor(mc.player.getZ() + forward.z * i)
            );

            if (mc.world.getBlockState(targetPos).isReplaceable()) {
                int oldSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = blockSlot;

                placeBlock(targetPos);

                mc.player.getInventory().selectedSlot = oldSlot;
                // If we placed a block, don't place more in the same tick unless you want fast extend
                break;
            }
        }
    }

    private void placeBlock(BlockPos pos) {
        if (mc.interactionManager == null || mc.player == null) return;
        
        // Find a neighboring block to place against, since placing in mid-air doesn't always work on servers
        Direction placeDir = Direction.UP;
        BlockPos placePos = pos;
        
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = pos.offset(dir);
            if (!mc.world.getBlockState(neighbor).isReplaceable()) {
                placePos = neighbor;
                placeDir = dir.getOpposite();
                break;
            }
        }

        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(placePos).add(Vec3d.of(placeDir.getVector()).multiply(0.5)), placeDir, placePos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private int getBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                if (((BlockItem) stack.getItem()).getBlock() != Blocks.SAND && ((BlockItem) stack.getItem()).getBlock() != Blocks.GRAVEL) {
                    return i;
                }
            }
        }
        return -1;
    }
    
    public boolean shouldSafeWalk() {
        return this.isEnabled() && safeWalk.getValue();
    }
}
