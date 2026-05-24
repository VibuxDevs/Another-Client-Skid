package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.movement.ElytraFly;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "travel", at = @At("HEAD"), cancellable = true)
    public void onTravel(Vec3d movementInput, CallbackInfo ci) {
        if ((Object) this instanceof ClientPlayerEntity player) {
            com.acs.module.modules.movement.ElytraFly elytraFly = (com.acs.module.modules.movement.ElytraFly) ModuleManager.INSTANCE.getModuleByName("ElytraFly");
            if (elytraFly != null && elytraFly.isEnabled() && player.isFallFlying()) {
                elytraFly.handleTravel(player);
                ci.cancel();
            }
        }
    }
}
