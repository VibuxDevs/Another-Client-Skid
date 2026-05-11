package com.acs.gui.clickgui;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.module.ModuleManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.List;

public class Frame {
    private final Category category;
    private int x, y, width, height;
    private boolean dragging;
    private int dragX, dragY;
    private boolean open = true;
    private final List<ModuleButton> buttons = new ArrayList<>();
    private float animFactor = 0f;

    public Frame(Category category, int x, int y, int width, int height) {
        this.category = category;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        
        int offset = height;
        for (Module module : ModuleManager.INSTANCE.getModulesByCategory(category)) {
            buttons.add(new ModuleButton(this, module, offset));
            offset += height;
        }
    }

    public void render(DrawContext context, int mouseX, int mouseY, float delta, String search) {
        animFactor = com.acs.utils.AnimationUtils.moveTowards(animFactor, 1f, 0.05f);
        
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }

        // Clip/Scale effect: we'll only draw if animFactor > 0
        if (animFactor <= 0) return;

        context.getMatrices().push();
        context.getMatrices().translate(x + width / 2f, y + height / 2f, 0);
        context.getMatrices().scale(animFactor, animFactor, 1f);
        context.getMatrices().translate(-(x + width / 2f), -(y + height / 2f), 0);

        List<ModuleButton> filtered = buttons.stream()
                .filter(b -> b.getModule().getName().toLowerCase().contains(search.toLowerCase()))
                .collect(java.util.stream.Collectors.toList());

        context.fill(x - 1, y - 1, x + width + 1, y + height + (open ? getFilteredHeight(filtered) : 0) + 1, 0xFF000000);
        context.fillGradient(x, y, x + width, y + height, 0xFFFF0000, 0xFF990000);
        context.fill(x, y + height - 1, x + width, y + height, 0x77000000);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, category.getName(), x + 3, y + 3, 0xFFFFFFFF);
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, open ? "-" : "+", x + width - 10, y + 3, 0xFFFFFFFF);

        if (open) {
            int currentOffset = height;
            for (ModuleButton button : filtered) {
                button.setOffset(currentOffset);
                button.render(context, mouseX, mouseY, delta);
                currentOffset += button.getHeight();
            }
        }
        
        context.getMatrices().pop();
    }

    private int getFilteredHeight(List<ModuleButton> filtered) {
        int h = 0;
        for (ModuleButton button : filtered) h += button.getHeight();
        return h;
    }

    private int getTotalButtonsHeight() {
        int h = 0;
        for (ModuleButton button : buttons) h += button.getHeight();
        return h;
    }

    public void updateButtonOffsets() {
        int currentOffset = height;
        for (ModuleButton button : buttons) {
            button.setOffset(currentOffset);
            currentOffset += button.getHeight();
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (isHovered(mouseX, mouseY)) {
            if (button == 0) {
                dragging = true;
                dragX = (int) (mouseX - x);
                dragY = (int) (mouseY - y);
                return true;
            } else if (button == 1) {
                open = !open;
                return true;
            }
        }
        
        if (open) {
            for (ModuleButton moduleButton : buttons) {
                if (moduleButton.mouseClicked(mouseX, mouseY, button)) return true;
            }
        }
        return false;
    }

    public void mouseReleased(double mouseX, double mouseY, int button) {
        if (button == 0) dragging = false;
    }

    public boolean isHovered(double mouseX, double mouseY) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean keyPressed(int key) {
        for (ModuleButton button : buttons) {
            if (button.keyPressed(key)) return true;
        }
        return false;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
