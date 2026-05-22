package com.acs.mixin;

import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatScreen.class)
public class ChatScreenMixin {

    @Shadow
    protected TextFieldWidget chatField;

    private String currentSuggestion = null;

    @Inject(method = "updateCommandSuggestions", at = @At("HEAD"), cancellable = true)
    private void onUpdateSuggestions(CallbackInfo ci) {
        String text = chatField.getText();
        if (!text.startsWith(".")) return;

        String partial = text.substring(1).toLowerCase();
        String[] knownCommands = {"toggle", "t", "bind", "b", "config", "cfg", "help", "h"};

        currentSuggestion = null;
        for (String cmd : knownCommands) {
            if (cmd.startsWith(partial) && !cmd.equals(partial)) {
                currentSuggestion = "." + cmd;
                break;
            }
        }

        if (currentSuggestion != null && partial.length() > 0) {
            chatField.setSuggestion(currentSuggestion.substring(text.length()));
        } else {
            chatField.setSuggestion(null);
        }

        ci.cancel();
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (keyCode != 258) return;

        String text = chatField.getText();
        if (!text.startsWith(".")) return;

        if (currentSuggestion != null) {
            chatField.setText(currentSuggestion);
            chatField.setCursorToEnd(false);
            chatField.setSuggestion(null);
            currentSuggestion = null;
            cir.setReturnValue(true);
        }
    }
}