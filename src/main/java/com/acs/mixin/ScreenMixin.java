package com.acs.mixin;

import com.acs.gui.clickgui.ParticleSystem;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class ScreenMixin {
    @Unique
    private final ParticleSystem acs$particleSystem = new ParticleSystem(120);

    @Inject(method = "renderBackground", at = @At("HEAD"), cancellable = true)
    private void onRenderBackground(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
        Screen screen = (Screen) (Object) this;
        if (net.minecraft.client.MinecraftClient.getInstance().world != null) {
            // In-game screen background: translucent dark red tint to keep game world visible
            context.fillGradient(0, 0, screen.width, screen.height, 0xC00A0202, 0x80020000);
        } else {
            // Out of game screen background: full premium opaque gradient with particles
            context.fill(0, 0, screen.width, screen.height, 0xFF050505);
            context.fillGradient(0, 0, screen.width, screen.height, 0x30FF0000, 0x05000000);
            acs$particleSystem.render(context, screen.width, screen.height);
        }
        ci.cancel();
    }
}
