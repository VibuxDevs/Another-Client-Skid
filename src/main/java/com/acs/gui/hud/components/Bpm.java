package com.acs.gui.hud.components;

import com.acs.gui.hud.HudComponent;
import net.minecraft.client.gui.DrawContext;

public class Bpm extends HudComponent {
    public Bpm(int x, int y) {
        super("Bpm", x, y);
    }

    @Override
    public void render(DrawContext context, float delta) {
        double bpm = 0;
        if (mc.player != null) {
            double diffX = mc.player.getX() - mc.player.prevX;
            double diffZ = mc.player.getZ() - mc.player.prevZ;
            double speedPerTick = Math.sqrt(diffX * diffX + diffZ * diffZ);
            bpm = speedPerTick * 20.0 * 60.0;
        }

        String text = String.format("\u00A77BPM: \u00A7a%.1f", bpm);
        context.drawTextWithShadow(mc.textRenderer, text, getX(), getY(), 0xFFFFFFFF);
    }
}
