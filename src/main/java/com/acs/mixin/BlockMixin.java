package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.render.XRay;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public class BlockMixin {

    @Inject(method = "shouldDrawSide", at = @At("HEAD"), cancellable = true)
    private static void onShouldDrawSide(BlockState state, BlockView world, BlockPos pos, Direction side, BlockPos blockPos, CallbackInfoReturnable<Boolean> cir) {
        XRay xray = (XRay) ModuleManager.INSTANCE.getModuleByName("XRay");
        if (xray != null && xray.isEnabled()) {
            Block block = state.getBlock();
            String name = net.minecraft.registry.Registries.BLOCK.getId(block).getPath();
            if (name.contains("ore") || name.contains("ancient_debris") || name.contains("diamond") || name.contains("emerald") || name.contains("gold") || name.contains("iron") || name.contains("coal") || name.contains("lapis") || name.contains("redstone")) {
                cir.setReturnValue(true);
            } else {
                cir.setReturnValue(false);
            }
        }
    }
}
