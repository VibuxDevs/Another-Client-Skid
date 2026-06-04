package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.render.XRay;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.Direction;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractBlock.AbstractBlockState.class)
public class AbstractBlockStateMixin {

    @Inject(method = "getRenderType", at = @At("HEAD"), cancellable = true)
    public void onGetRenderType(CallbackInfoReturnable<net.minecraft.block.BlockRenderType> cir) {
        XRay xray = (XRay) ModuleManager.INSTANCE.getModuleByName("XRay");
        if (xray != null && xray.isEnabled()) {
            AbstractBlock.AbstractBlockState state = (AbstractBlock.AbstractBlockState) (Object) this;
            Block block = state.getBlock();
            if (!isSupportedBlock(block)) {
                cir.setReturnValue(net.minecraft.block.BlockRenderType.INVISIBLE);
            }
        }
    }

    @Inject(method = "isSideInvisible", at = @At("HEAD"), cancellable = true)
    public void onIsSideInvisible(BlockState state, Direction direction, CallbackInfoReturnable<Boolean> cir) {
        XRay xray = (XRay) ModuleManager.INSTANCE.getModuleByName("XRay");
        if (xray != null && xray.isEnabled()) {
            AbstractBlock.AbstractBlockState thisState = (AbstractBlock.AbstractBlockState) (Object) this;
            Block block = thisState.getBlock();
            if (isSupportedBlock(block)) {
                cir.setReturnValue(false);
            } else {
                cir.setReturnValue(true);
            }
        }
    }

    private boolean isSupportedBlock(Block block) {
        String name = net.minecraft.registry.Registries.BLOCK.getId(block).getPath();
        return name.contains("ore") || 
               name.contains("debris") || 
               name.contains("chest") || 
               name.contains("spawner") || 
               name.contains("portal") || 
               name.contains("shulker") || 
               name.contains("diamond") || 
               name.contains("emerald") || 
               name.contains("gold") || 
               name.contains("iron") || 
               name.contains("coal") || 
               name.contains("lapis") || 
               name.contains("redstone") || 
               name.contains("copper") || 
               name.contains("quartz") ||
               name.contains("raw_") ||
               name.contains("lava") ||
               name.contains("water");
    }
}
