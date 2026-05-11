package com.acs.gui.hud.components;

import com.acs.gui.hud.HudComponent;
import net.minecraft.client.gui.DrawContext;

public class Watermark extends HudComponent {
    public Watermark(int x, int y) {
        super("Watermark", x, y);
    }

    @Override
    public void render(DrawContext context, float delta) {
        String text = "ACS Client \u00A7cBeta \u00A7f| \u00A77v1.0.0";
        context.drawTextWithShadow(mc.textRenderer, text, getX(), getY(), 0xFFFFFFFF);
    }
}
