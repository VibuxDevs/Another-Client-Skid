package com.acs.gui.clickgui;

import com.acs.module.Module;
import com.acs.settings.BooleanSetting;
import com.acs.settings.ModeSetting;
import com.acs.settings.NumberSetting;
import com.acs.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import org.lwjgl.glfw.GLFW;

public class ModuleButton {
    public static NumberSetting activeSlider = null;
    public static int sliderX = 0;
    public static int sliderWidth = 0;

    private final Module module;
    private final Frame parent;
    private int offset;
    private boolean extended = false;
    private boolean bindListening = false;

    public ModuleButton(Frame parent, Module module, int offset) {
        this.module = module;
        this.parent = parent;
        this.offset = offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public Module getModule() {
        return module;
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        int x = parent.getX();
        int y = parent.getY() + offset;
        int w = parent.getWidth();
        int h = parent.getHeight();

        boolean hovered = isHoveredRow(mouseX, mouseY, y, h);
        context.fill(x, y, x + w, y + h, hovered ? 0xFF222222 : 0xFF111111);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, module.getName(), x + 4, y + 2, module.isEnabled() ? 0xFF00FFFF : 0xFFCCCCCC);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, extended ? "▲" : "▼", x + w - 11, y + 2, 0xFF555555);

        if (extended) {
            int sY = y + h;
            for (Setting<?> setting : module.getSettings()) {
                if (!setting.isVisible()) continue;
                boolean sHovered = isHoveredRow(mouseX, mouseY, sY, h);
                context.fill(x, sY, x + w, sY + h, sHovered ? 0xFF1a1a1a : 0xFF080808);
                
                if (setting instanceof NumberSetting ns) {
                    double percent = (ns.getValue() - ns.getMin()) / (ns.getMax() - ns.getMin());
                    if (percent < 0) percent = 0;
                    if (percent > 1) percent = 1;
                    int sliderRight = x + 2 + (int)((w - 4) * percent);
                    context.fill(x + 2, sY + 2, sliderRight, sY + h - 2, 0x3500FFFF);
                    context.fill(x + 2, sY + h - 2, sliderRight, sY + h, 0xFF00FFFF);
                }

                context.fill(x, sY, x + 2, sY + h, 0xFF440000);
                
                String valStr = setting.getValue().toString();
                if (setting instanceof NumberSetting ns) {
                    valStr = String.format("%.2f", ns.getValue());
                    if (valStr.endsWith(".00")) {
                        valStr = valStr.substring(0, valStr.length() - 3);
                    } else if (valStr.contains(".") && valStr.endsWith("0")) {
                        valStr = valStr.substring(0, valStr.length() - 1);
                    }
                }
                
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, setting.getName(), x + 6, sY + 2, 0xFF888888);
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, valStr, x + w - MinecraftClient.getInstance().textRenderer.getWidth(valStr) - 4, sY + 2, 0xFFFF6666);
                sY += h;
            }

            boolean bindHovered = isHoveredRow(mouseX, mouseY, sY, h);
            context.fill(x, sY, x + w, sY + h, bindListening ? 0xFF003300 : (bindHovered ? 0xFF1a1a1a : 0xFF080808));
            context.fill(x, sY, x + 2, sY + h, 0xFF004444);
            int k = module.getKey();
            String keyStr = bindListening ? "§a> PRESS A KEY <" : ((k == 0) ? "NONE" : (GLFW.glfwGetKeyName(k, 0) != null ? GLFW.glfwGetKeyName(k, 0).toUpperCase() : "KEY_" + k));
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, "Bind", x + 6, sY + 2, 0xFF888888);
            context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, keyStr, x + w - MinecraftClient.getInstance().textRenderer.getWidth(keyStr) - 4, sY + 2, bindListening ? 0xFF00FF00 : 0xFF6666FF);
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int x = parent.getX();
        int y = parent.getY() + offset;
        int h = parent.getHeight();
        int w = parent.getWidth();

        if (isHoveredRow(mouseX, mouseY, y, h)) {
            if (button == 0) { module.toggle(); return true; }
            if (button == 1) { extended = !extended; bindListening = false; parent.updateButtonOffsets(); return true; }
        }

        if (extended) {
            int sY = y + h;
            for (Setting<?> setting : module.getSettings()) {
                if (!setting.isVisible()) continue;
                if (isHoveredRow(mouseX, mouseY, sY, h)) {
                    if (button == 0) {
                        if (setting instanceof BooleanSetting) ((BooleanSetting) setting).toggle();
                        else if (setting instanceof ModeSetting) ((ModeSetting) setting).cycle();
                        else if (setting instanceof NumberSetting ns) {
                            activeSlider = ns;
                            sliderX = x;
                            sliderWidth = w;
                            
                            double percent = (mouseX - x) / (double) w;
                            if (percent < 0) percent = 0;
                            if (percent > 1) percent = 1;
                            double val = ns.getMin() + percent * (ns.getMax() - ns.getMin());
                            val = Math.round(val / ns.getIncrement()) * ns.getIncrement();
                            if (val < ns.getMin()) val = ns.getMin();
                            if (val > ns.getMax()) val = ns.getMax();
                            ns.setValue(val);
                        }
                    }
                    return true;
                }
                sY += h;
            }

            if (isHoveredRow(mouseX, mouseY, sY, h)) {
                if (button == 0) {
                    bindListening = !bindListening;
                } else if (button == 1) {
                    module.setKey(0);
                    bindListening = false;
                }
                return true;
            }
        }

        return false;
    }

    public boolean keyPressed(int key) {
        if (!bindListening) return false;
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            bindListening = false;
        } else {
            module.setKey(key);
            bindListening = false;
        }
        return true;
    }

    public boolean isBindListening() {
        return bindListening;
    }

    public int getHeight() {
        if (!extended) return parent.getHeight();
        int visibleSettings = 0;
        for (Setting<?> setting : module.getSettings()) {
            if (setting.isVisible()) visibleSettings++;
        }
        return parent.getHeight() * (1 + visibleSettings + 1);
    }

    private boolean isHoveredRow(double mouseX, double mouseY, int rowY, int rowH) {
        return mouseX >= parent.getX() && mouseX <= parent.getX() + parent.getWidth()
            && mouseY >= rowY && mouseY <= rowY + rowH;
    }
}
