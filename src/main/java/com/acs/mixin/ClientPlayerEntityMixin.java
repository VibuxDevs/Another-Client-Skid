package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.world.Scaffold;
import net.minecraft.client.network.ClientPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ClientPlayerEntity.class)
public class ClientPlayerEntityMixin {

    @Inject(method = "clipAtLedge", at = @At("HEAD"), cancellable = true)
    protected void onClipAtLedge(CallbackInfoReturnable<Boolean> cir) {
        Scaffold scaffold = (Scaffold) ModuleManager.INSTANCE.getModuleByName("Scaffold");
        if (scaffold != null && scaffold.shouldSafeWalk()) {
            cir.setReturnValue(true);
        }
    }
}
