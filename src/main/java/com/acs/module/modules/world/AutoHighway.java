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

public class AutoHighway extends Module {

    private final NumberSetting width = new NumberSetting("Width", 3.0, 1.0, 5.0, 1.0);
    private final BooleanSetting buildWalls = new BooleanSetting("Build Walls", false);
    private final NumberSetting wallHeight = new NumberSetting("Wall Height", 2.0, 1.0, 4.0, 1.0);
    private final BooleanSetting autoWalk = new BooleanSetting("Auto Walk", true);
    private final BooleanSetting autoMine = new BooleanSetting("Auto Mine", true);

    private int startY;
    private Direction startDir = null;

    public AutoHighway() {
        super("AutoHighway", "Automatically builds and clears obsidian highways along axes", Category.WORLD);
        
        wallHeight.setVisible(() -> buildWalls.getValue());
        
        addSetting(width);
        addSetting(buildWalls);
        addSetting(wallHeight);
        addSetting(autoWalk);
        addSetting(autoMine);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            startY = mc.player.getBlockPos().getY();
            startDir = mc.player.getMovementDirection();
            if (startDir == Direction.UP || startDir == Direction.DOWN) {
                startDir = Direction.NORTH;
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null && mc.options.forwardKey != null && autoWalk.getValue()) {
            mc.options.forwardKey.setPressed(false);
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || startDir == null) return;

        if (autoWalk.getValue()) {
            mc.player.setYaw(startDir.asRotation());
            mc.options.forwardKey.setPressed(true);
        }

        BlockPos playerPos = mc.player.getBlockPos();
        Direction dir = startDir;

        int dx = 0;
        int dz = 0;
        if (dir == Direction.NORTH || dir == Direction.SOUTH) {
            dx = 1;
        } else {
            dz = 1;
        }

        int w = width.getValue().intValue();
        int minOffset = -w / 2;
        int maxOffset = w / 2;
        if (w % 2 == 0) {
            maxOffset = w / 2 - 1;
        }

        int checkAhead = 1;
        
        for (int i = minOffset; i <= maxOffset; i++) {
            int targetX = playerPos.getX() + dir.getOffsetX() * checkAhead + dx * i;
            int targetZ = playerPos.getZ() + dir.getOffsetZ() * checkAhead + dz * i;

            BlockPos buildPos = new BlockPos(targetX, startY - 1, targetZ);

            // Replace floor if it is not Obsidian
            net.minecraft.block.BlockState floorState = mc.world.getBlockState(buildPos);
            if (floorState.getBlock() != Blocks.OBSIDIAN) {
                if (autoMine.getValue()) {
                    mineBlockIfSolid(buildPos);
                }
                if (mc.world.getBlockState(buildPos).isReplaceable()) {
                    tryPlaceObsidian(buildPos);
                }
            }

            // Build walls or clear the walking path
            boolean isOuter = (i == minOffset || i == maxOffset);
            if (isOuter && buildWalls.getValue()) {
                int wh = wallHeight.getValue().intValue();
                for (int h = 0; h < wh; h++) {
                    BlockPos wallPos = new BlockPos(targetX, startY + h, targetZ);
                    if (autoMine.getValue()) {
                        mineBlockIfSolid(wallPos);
                    }
                    if (mc.world.getBlockState(wallPos).isReplaceable()) {
                        tryPlaceObsidian(wallPos);
                    }
                }
            } else {
                if (autoMine.getValue()) {
                    mineBlockIfSolid(new BlockPos(targetX, startY, targetZ));
                    mineBlockIfSolid(new BlockPos(targetX, startY + 1, targetZ));
                    mineBlockIfSolid(new BlockPos(targetX, startY + 2, targetZ));
                }
            }
        }
    }

    private void tryPlaceObsidian(BlockPos pos) {
        int obbySlot = getObsidianSlot();
        if (obbySlot != -1) {
            int oldSlot = mc.player.getInventory().selectedSlot;
            if (oldSlot != obbySlot) {
                mc.player.getInventory().selectedSlot = obbySlot;
                mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(obbySlot));
            }

            placeBlock(pos);

            if (oldSlot != obbySlot) {
                mc.player.getInventory().selectedSlot = oldSlot;
                mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(oldSlot));
            }
        }
    }

    private void placeBlock(BlockPos pos) {
        if (mc.interactionManager == null || mc.player == null || mc.world == null) return;

        Direction placeDir = Direction.UP;
        BlockPos placePos = pos;

        for (Direction d : Direction.values()) {
            BlockPos neighbor = pos.offset(d);
            if (!mc.world.getBlockState(neighbor).isReplaceable()) {
                placePos = neighbor;
                placeDir = d.getOpposite();
                break;
            }
        }

        BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(placePos).add(Vec3d.of(placeDir.getVector()).multiply(0.5)),
                placeDir,
                placePos,
                false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    private void mineBlockIfSolid(BlockPos pos) {
        if (mc.world == null || mc.interactionManager == null || mc.player == null) return;
        net.minecraft.block.BlockState state = mc.world.getBlockState(pos);
        if (!state.isAir() && !state.isLiquid() && state.getHardness(mc.world, pos) >= 0) {
            int toolSlot = getBestTool(pos);
            if (toolSlot != -1) {
                int oldSlot = mc.player.getInventory().selectedSlot;
                if (oldSlot != toolSlot) {
                    mc.player.getInventory().selectedSlot = toolSlot;
                    mc.getNetworkHandler().sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(toolSlot));
                }
            }
            mc.interactionManager.updateBlockBreakingProgress(pos, Direction.UP);
            mc.player.swingHand(Hand.MAIN_HAND);
        }
    }

    private int getBestTool(BlockPos pos) {
        if (mc.world == null || mc.player == null) return -1;
        float bestSpeed = 1f;
        int bestSlot = -1;
        net.minecraft.block.BlockState state = mc.world.getBlockState(pos);
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty()) {
                float speed = stack.getMiningSpeedMultiplier(state);
                if (speed > bestSpeed) {
                    bestSpeed = speed;
                    bestSlot = i;
                }
            }
        }
        return bestSlot;
    }

    private int getObsidianSlot() {
        if (mc.player == null) return -1;
        // Search hotbar first
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == net.minecraft.item.Items.OBSIDIAN) {
                return i;
            }
        }
        // Search inventory and swap to slot 0
        for (int i = 9; i < 36; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() == net.minecraft.item.Items.OBSIDIAN) {
                if (mc.interactionManager != null) {
                    mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, i, 0, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
                }
                return 0;
            }
        }
        return -1;
    }
}
