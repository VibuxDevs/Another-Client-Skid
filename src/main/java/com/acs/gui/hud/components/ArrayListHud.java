package com.acs.gui.hud.components;

import com.acs.gui.hud.HudComponent;
import com.acs.module.Module;
import com.acs.module.ModuleManager;
import net.minecraft.client.gui.DrawContext;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class ArrayListHud extends HudComponent {
    public ArrayListHud(int x, int y) {
        super("ArrayList", x, y);
    }

    @Override
    public void render(DrawContext context, float delta) {
        List<Module> enabledModules = ModuleManager.INSTANCE.getModules().stream()
                .filter(Module::isEnabled)
                .sorted(Comparator.comparingInt(m -> -mc.textRenderer.getWidth(m.getName())))
                .collect(Collectors.toList());

        int offset = 0;
        int screenWidth = mc.getWindow().getScaledWidth();

        for (Module module : enabledModules) {
            String name = module.getName();
            int width = mc.textRenderer.getWidth(name);
            // Render on the right side by default for OyVey style
            context.drawTextWithShadow(mc.textRenderer, name, screenWidth - width - 5, getY() + offset, 0xFFFF0000);
            offset += mc.textRenderer.fontHeight + 1;
        }
    }
}
