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
        com.acs.module.modules.exploits.Blink blink = (com.acs.module.modules.exploits.Blink) ModuleManager.INSTANCE.getModuleByName("Blink");
        if (blink != null && blink.isEnabled()) {
            if (blink.onSendPacket(packet)) {
                ci.cancel();
                return;
            }
        }

        Freecam freecam = (Freecam) ModuleManager.INSTANCE.getModuleByName("Freecam");
        if (freecam != null && freecam.isEnabled()) {
            if (packet instanceof PlayerMoveC2SPacket) {
                ci.cancel();
                return;
            }
        }

        com.acs.module.modules.exploits.ReverseGhostBlock rgb = (com.acs.module.modules.exploits.ReverseGhostBlock) ModuleManager.INSTANCE.getModuleByName("ReverseGhostBlock");
        if (rgb != null && rgb.isEnabled()) {
            if (packet instanceof net.minecraft.network.packet.c2s.play.PlayerInteractBlockC2SPacket interactPacket) {
                net.minecraft.util.hit.BlockHitResult hit = interactPacket.getBlockHitResult();
                if (hit != null) {
                    net.minecraft.util.math.BlockPos targetPos = hit.getBlockPos();
                    net.minecraft.util.math.Direction side = hit.getSide();
                    net.minecraft.util.math.BlockPos placedPos = targetPos.offset(side);
                    rgb.addGhostBlock(placedPos);
                }
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
