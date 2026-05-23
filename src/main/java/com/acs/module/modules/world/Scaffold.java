package com.acs.module.modules.world;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", "Automatically places blocks under you", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        
        BlockPos belowPlayer = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(belowPlayer).isReplaceable()) {
            int blockSlot = getBlockSlot();
            if (blockSlot != -1) {
                int oldSlot = mc.player.getInventory().selectedSlot;
                mc.player.getInventory().selectedSlot = blockSlot;
                
                placeBlock(belowPlayer);
                
                mc.player.getInventory().selectedSlot = oldSlot;
            }
        }
    }

    private void placeBlock(BlockPos pos) {
        if (mc.interactionManager == null || mc.player == null) return;
        
        // Very basic placement logic, assumes we can place from above for now
        BlockHitResult hitResult = new BlockHitResult(Vec3d.ofCenter(pos).add(0, 0.5, 0), Direction.UP, pos, false);
        mc.getNetworkHandler().sendPacket(new PlayerInteractBlockC2SPacket(Hand.MAIN_HAND, hitResult, 0));
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
}
