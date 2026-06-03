package com.acs.mixin;

import com.acs.module.Module;
import com.acs.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerInteractionManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayerInteractionManager.class)
public class ClientPlayerInteractionManagerMixin {
    @Inject(method = "attackEntity", at = @At("HEAD"))
    private void onAttackEntity(PlayerEntity player, Entity target, CallbackInfo ci) {
        Module maceKill = ModuleManager.INSTANCE.getModuleByName("MaceKill");
        if (maceKill != null && maceKill.isEnabled()) {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null && mc.player.getMainHandStack().getItem() == Items.MACE) {
                double height = 20.0;
                com.acs.settings.Setting<?> heightSetting = maceKill.getSettings().stream()
                        .filter(s -> s.getName().equalsIgnoreCase("Height"))
                        .findFirst().orElse(null);
                if (heightSetting != null && heightSetting.getValue() instanceof Double) {
                    height = (Double) heightSetting.getValue();
                }

                double x = mc.player.getX();
                double y = mc.player.getY();
                double z = mc.player.getZ();

                // Spoof the server into thinking we fell from high up to trigger Mace critical damage
                if (mc.player.networkHandler != null) {
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + height, z, false));
                    mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
                }
            }
        }
    }
}
