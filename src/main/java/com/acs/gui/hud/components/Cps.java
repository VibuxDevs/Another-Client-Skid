package com.acs.gui.hud.components;

import com.acs.gui.hud.HudComponent;
import com.acs.module.ModuleManager;
import com.acs.module.modules.pvp.CrystalAura;
import net.minecraft.client.gui.DrawContext;

public class Cps extends HudComponent {
    public Cps(int x, int y) {
        super("Cps", x, y);
    }

    @Override
    public void render(DrawContext context, float delta) {
        CrystalAura crystalAura = (CrystalAura) ModuleManager.INSTANCE.getModuleByName("CrystalAura");

        String text;
        if (crystalAura != null && crystalAura.isEnabled()) {
            int cps = (int) crystalAura.getCurrentCps();
            text = "\u00A77CPS: \u00A7c" + cps;
        } else {
            text = "\u00A77CPS \u00A7f: \u00A7cdisabled";
        }

        context.drawTextWithShadow(mc.textRenderer, text, getX(), getY(), 0xFFFFFFFF);
    }
}
