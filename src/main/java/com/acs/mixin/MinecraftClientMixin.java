package com.acs.mixin;

import com.acs.module.Module;
import com.acs.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            for (Module module : ModuleManager.INSTANCE.getModules()) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
        }
    }
}
