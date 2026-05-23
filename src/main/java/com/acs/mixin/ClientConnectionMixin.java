package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.render.Freecam;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientConnection.class)
public class ClientConnectionMixin {

    @Inject(method = "send(Lnet/minecraft/network/packet/Packet;)V", at = @At("HEAD"), cancellable = true)
    public void onSendPacket(Packet<?> packet, CallbackInfo ci) {
        Freecam freecam = (Freecam) ModuleManager.INSTANCE.getModuleByName("Freecam");
        if (freecam != null && freecam.isEnabled()) {
            if (packet instanceof PlayerMoveC2SPacket) {
                ci.cancel();
                return;
            }
        }
        
        com.acs.module.modules.player.NoFall noFall = (com.acs.module.modules.player.NoFall) ModuleManager.INSTANCE.getModuleByName("NoFall");
        if (noFall != null && noFall.isEnabled()) {
            if (packet instanceof PlayerMoveC2SPacket movePacket) {
                if (net.minecraft.client.MinecraftClient.getInstance().player != null && net.minecraft.client.MinecraftClient.getInstance().player.fallDistance > 3.0f) {
                    ((PlayerMoveC2SPacketAccessor) movePacket).setOnGround(true);
                }
            }
        }
    }
}
