package com.acs.mixin;

import com.acs.module.ModuleManager;
import com.acs.module.modules.movement.Velocity;
import com.acs.module.modules.render.ESP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "addVelocity", at = @At("HEAD"), cancellable = true)
    public void onAddVelocity(net.minecraft.util.math.Vec3d velocityVector, CallbackInfo ci) {
        Entity entity = (Entity) (Object) this;
        // Basic Velocity check for the player
        if (entity instanceof PlayerEntity && entity.getUuid().equals(net.minecraft.client.MinecraftClient.getInstance().getSession().getUuidOrNull())) {
            Velocity velocity = (Velocity) ModuleManager.INSTANCE.getModuleByName("Velocity");
            if (velocity != null && velocity.isEnabled()) {
                double horizontal = velocity.getHorizontal();
                double vertical = velocity.getVertical();
                
                if (horizontal == 0.0 && vertical == 0.0) {
                    ci.cancel();
                } else {
                    entity.setVelocity(entity.getVelocity().add(velocityVector.x * horizontal, velocityVector.y * vertical, velocityVector.z * horizontal));
                    entity.velocityModified = true;
                    ci.cancel();
                }
            }
        }
    }

    @Inject(method = "isGlowing", at = @At("HEAD"), cancellable = true)
    private void onIsGlowing(CallbackInfoReturnable<Boolean> cir) {
        ESP esp = (ESP) ModuleManager.INSTANCE.getModuleByName("ESP");
        if (esp != null && esp.isEnabled()) {
            Entity entity = (Entity) (Object) this;
            if (entity instanceof PlayerEntity && esp.shouldRenderPlayers()) {
                cir.setReturnValue(true);
            } else if (entity instanceof HostileEntity && esp.shouldRenderMobs()) {
                cir.setReturnValue(true);
            } else if (entity instanceof AnimalEntity && esp.shouldRenderAnimals()) {
                cir.setReturnValue(true);
            } else if (entity instanceof ItemEntity && esp.shouldRenderItems()) {
                cir.setReturnValue(true);
            }
        }
    }
}
