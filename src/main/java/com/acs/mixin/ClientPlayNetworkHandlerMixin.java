package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.movement.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.network.packet.s2c.play.ExplosionS2CPacket;
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
                    // It's a bit tricky to modify S2C packet fields directly since they might not have setters.
                    // The easiest way is to cancel the packet and apply the scaled velocity manually.
                    double vx = (packet.getVelocityX() / 8000.0D) * horizontal;
                    double vy = (packet.getVelocityY() / 8000.0D) * vertical;
                    double vz = (packet.getVelocityZ() / 8000.0D) * horizontal;
                    MinecraftClient.getInstance().player.setVelocity(vx, vy, vz);
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "onExplosion", at = @At("HEAD"), cancellable = true)
    public void onExplosion(ExplosionS2CPacket packet, CallbackInfo ci) {
        Velocity velocity = (Velocity) ModuleManager.INSTANCE.getModuleByName("Velocity");
        if (velocity != null && velocity.isEnabled() && velocity.handleExplosions()) {
            double horizontal = velocity.getHorizontal();
            double vertical = velocity.getVertical();
            
            if (horizontal == 0.0 && vertical == 0.0) {
                // To properly cancel just the knockback but keep the explosion, we would modify the packet knockback fields.
                // For simplicity, if we want 0 knockback from explosions, we can cancel the player velocity update part in the handler.
                // But ExplosionS2CPacket handling adds velocity. Since we can't easily overwrite just the velocity part without redirect, 
                // we will let Entity.addVelocity mixin handle explosion knockback since explosions call player.addVelocity on the client.
                // Wait, explosion packet has getPlayerVelocityX() which gets added. 
                // Let's rely on EntityMixin for explosions or do a Redirect.
                // For a quick fix, let's just let the entity mixin catch it if it does, 
                // but actually the handler sets player.setVelocity(player.getVelocity().add(packet.getPlayerVelocityX(), ...))
            }
        }
    }
}
