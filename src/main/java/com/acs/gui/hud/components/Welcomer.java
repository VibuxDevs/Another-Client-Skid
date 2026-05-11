package com.acs.gui.hud.components;

import com.acs.gui.hud.HudComponent;
import net.minecraft.client.gui.DrawContext;

public class Welcomer extends HudComponent {
    public Welcomer(int x, int y) {
        super("Welcomer", x, y);
    }

    @Override
    public void render(DrawContext context, float delta) {
        String text = "Welcome back, " + mc.getSession().getUsername() + "!";
        int screenWidth = mc.getWindow().getScaledWidth();
        context.drawCenteredTextWithShadow(mc.textRenderer, text, screenWidth / 2, getY(), 0xFFFF0000);
    }
}
