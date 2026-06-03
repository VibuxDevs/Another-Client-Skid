package com.acs.mixin;

import com.acs.module.Module;
import com.acs.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MinecraftClient.class)
public class MinecraftClientMixin {
<<<<<<< Updated upstream
    @org.spongepowered.asm.mixin.Shadow
    private int itemUseCooldown;
=======
    @Shadow private int itemUseCooldown;
>>>>>>> Stashed changes

    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        if (MinecraftClient.getInstance().player != null) {
            Module fastPlace = ModuleManager.INSTANCE.getModuleByName("FastPlace");
            if (fastPlace != null && fastPlace.isEnabled()) {
                itemUseCooldown = 0;
            }

            for (Module module : ModuleManager.INSTANCE.getModules()) {
                if (module.isEnabled()) {
                    module.onTick();
                }
            }
            
            com.acs.module.modules.player.FastPlace fastPlace = (com.acs.module.modules.player.FastPlace) ModuleManager.INSTANCE.getModuleByName("FastPlace");
            if (fastPlace != null && fastPlace.isEnabled()) {
                if (itemUseCooldown > fastPlace.getDelay()) {
                    itemUseCooldown = fastPlace.getDelay();
                }
            }
        }
    }
}
