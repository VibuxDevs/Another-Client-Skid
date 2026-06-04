package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket;
import net.minecraft.network.packet.s2c.play.BlockUpdateS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    public void onEntityVelocityUpdate(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (MinecraftClient.getInstance().player == null) return;
        
        if (packet.getId() == MinecraftClient.getInstance().player.getId()) {
            Velocity velocity = (Velocity) ModuleManager.INSTANCE.getModuleByName("Velocity");
            if (velocity != null && velocity.isEnabled()) {
                double horizontal = velocity.getHorizontal();
                double vertical = velocity.getVertical();
                
                if (horizontal == 0.0 && vertical == 0.0) {
                    ci.cancel();
                } else {
                    double vx = (packet.getVelocityX() / 8000.0D) * horizontal;
                    double vy = (packet.getVelocityY() / 8000.0D) * vertical;
                    double vz = (packet.getVelocityZ() / 8000.0D) * horizontal;
                    MinecraftClient.getInstance().player.setVelocity(vx, vy, vz);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "onExplosion", at = @At("HEAD"))
    public void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        com.acs.module.modules.exploits.CoordLogger coordLogger = (com.acs.module.modules.exploits.CoordLogger) ModuleManager.INSTANCE.getModuleByName("CoordLogger");
        if (coordLogger != null) {
            coordLogger.handleExplosion(packet.getX(), packet.getY(), packet.getZ());
        }

        Velocity velocity = (Velocity) ModuleManager.INSTANCE.getModuleByName("Velocity");
        if (velocity != null && velocity.isEnabled() && velocity.handleExplosions()) {
            double horizontal = velocity.getHorizontal();
            double vertical = velocity.getVertical();
            
            ExplosionS2CPacketAccessor accessor = (ExplosionS2CPacketAccessor) packet;
            accessor.setPlayerVelocityX((float) (packet.getPlayerVelocityX() * horizontal));
            accessor.setPlayerVelocityY((float) (packet.getPlayerVelocityY() * vertical));
            accessor.setPlayerVelocityZ((float) (packet.getPlayerVelocityZ() * horizontal));
        }
    }

    @Inject(method = "onPlaySound", at = @At("HEAD"))
    public void onPlaySound(net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket packet, CallbackInfo ci) {
        com.acs.module.modules.exploits.CoordLogger coordLogger = (com.acs.module.modules.exploits.CoordLogger) ModuleManager.INSTANCE.getModuleByName("CoordLogger");
        if (coordLogger != null) {
            coordLogger.handleSound(packet.getSound(), packet.getX(), packet.getY(), packet.getZ());
        }
    }

    @Inject(method = "onWorldEvent", at = @At("HEAD"))
    public void onWorldEvent(net.minecraft.network.packet.s2c.play.WorldEventS2CPacket packet, CallbackInfo ci) {
        com.acs.module.modules.exploits.CoordLogger coordLogger = (com.acs.module.modules.exploits.CoordLogger) ModuleManager.INSTANCE.getModuleByName("CoordLogger");
        if (coordLogger != null) {
            coordLogger.handleWorldEvent(packet.getEventId(), packet.getPos());
        }
    }

    @Inject(method = "onEntitySpawn", at = @At("HEAD"))
    public void onEntitySpawn(EntitySpawnS2CPacket packet, CallbackInfo ci) {
        com.acs.module.modules.exploits.Randar randar = (com.acs.module.modules.exploits.Randar) ModuleManager.INSTANCE.getModuleByName("Randar");
        if (randar != null) {
            randar.handleEntitySpawn(packet);
        }
    }

    @Inject(method = "onBlockUpdate", at = @At("HEAD"))
    public void onBlockUpdate(BlockUpdateS2CPacket packet, CallbackInfo ci) {
        com.acs.module.modules.exploits.Nocom nocom = (com.acs.module.modules.exploits.Nocom) ModuleManager.INSTANCE.getModuleByName("Nocom");
        if (nocom != null) {
            nocom.handleBlockUpdate(packet);
        }
    }
}
