package com.acs.module.modules.world;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;
import fi.dy.masa.litematica.world.SchematicWorldHandler;
import fi.dy.masa.litematica.world.WorldSchematic;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Printer extends Module {

    private final NumberSetting range = new NumberSetting("Range", 4.0, 1.0, 6.0, 0.1);
    private final NumberSetting delay = new NumberSetting("Delay", 1.0, 0.0, 10.0, 1.0);
    private final NumberSetting blocksPerTick = new NumberSetting("Blocks/Tick", 1.0, 1.0, 10.0, 1.0);

    private int delayTimer = 0;

    public Printer() {
        super("Printer", "Automatically builds Litematica schematics", Category.WORLD);
        addSetting(range);
        addSetting(delay);
        addSetting(blocksPerTick);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null)
            return;

        if (delayTimer > 0) {
            delayTimer--;
            return;
        }

        try {
            WorldSchematic schematicWorld = SchematicWorldHandler.getSchematicWorld();
            if (schematicWorld == null)
                return;

            int r = range.getValue().intValue();
            BlockPos playerPos = mc.player.getBlockPos();
            int placedThisTick = 0;
            int maxBlocks = blocksPerTick.getValue().intValue();

            for (int x = -r; x <= r; x++) {
                for (int y = -r; y <= r; y++) {
                    for (int z = -r; z <= r; z++) {
                        if (placedThisTick >= maxBlocks) {
                            if (delay.getValue().intValue() > 0) {
                                delayTimer = delay.getValue().intValue();
                            }
                            return;
                        }

                        BlockPos pos = playerPos.add(x, y, z);

                        if (mc.player.squaredDistanceTo(Vec3d.ofCenter(pos)) > range.getValue() * range.getValue()) {
                            continue;
                        }

                        BlockState expectedState = schematicWorld.getBlockState(pos);
                        if (expectedState == null || expectedState.isAir())
                            continue;

                        BlockState currentState = mc.world.getBlockState(pos);
                        if (currentState.getBlock() == expectedState.getBlock())
                            continue;
                        if (!currentState.isReplaceable())
                            continue;

                        int slot = findBlockInHotbar(expectedState.getBlock());
                        if (slot != -1) {
                            placeBlock(pos, slot);
                            placedThisTick++;
                        }
                    }
                }
            }

            if (placedThisTick > 0 && delay.getValue().intValue() > 0) {
                delayTimer = delay.getValue().intValue();
            }
        } catch (NoClassDefFoundError | Exception e) {
            // Litematica is not installed or error occurred
        }
    }

    private void placeBlock(BlockPos pos, int slot) {
        if (mc.interactionManager == null || mc.player == null)
            return;

        int oldSlot = mc.player.getInventory().selectedSlot;
        if (oldSlot != slot) {
            mc.player.getInventory().selectedSlot = slot;
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler()
                        .sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(slot));
            }
        }

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

        BlockHitResult hitResult = new BlockHitResult(
                Vec3d.ofCenter(placePos).add(Vec3d.of(placeDir.getVector()).multiply(0.5)), placeDir, placePos, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hitResult);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (oldSlot != slot) {
            mc.player.getInventory().selectedSlot = oldSlot;
            if (mc.getNetworkHandler() != null) {
                mc.getNetworkHandler()
                        .sendPacket(new net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket(oldSlot));
            }
        }
    }

    private int findBlockInHotbar(Block block) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (!stack.isEmpty() && stack.getItem() instanceof BlockItem) {
                if (((BlockItem) stack.getItem()).getBlock() == block) {
                    return i;
                }
            }
        }
        return -1;
    }
}
