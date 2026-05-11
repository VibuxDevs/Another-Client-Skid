package com.acs.gui.hud;

import com.acs.gui.hud.components.ArrayListHud;
import com.acs.gui.hud.components.Watermark;
import net.minecraft.client.gui.DrawContext;
import java.util.ArrayList;
import java.util.List;

public class HudManager {
    public static final HudManager INSTANCE = new HudManager();
    private final List<HudComponent> components = new ArrayList<>();

    public HudManager() {
        components.add(new Watermark(5, 5));
        components.add(new ArrayListHud(5, 5));
        components.add(new com.acs.gui.hud.components.Welcomer(0, 5));
    }

    public void render(DrawContext context, float delta) {
        for (HudComponent component : components) {
            if (component.isEnabled()) {
                component.render(context, delta);
            }
        }
    }

    public List<HudComponent> getComponents() {
        return components;
    }
}
