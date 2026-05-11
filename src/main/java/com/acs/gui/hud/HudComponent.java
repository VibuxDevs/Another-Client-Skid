package com.acs.gui.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

public abstract class HudComponent {
    protected static final MinecraftClient mc = MinecraftClient.getInstance();
    private final String name;
    private int x, y;
    private boolean enabled = true;

    public HudComponent(String name, int x, int y) {
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public abstract void render(DrawContext context, float delta);

    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
    public void setX(int x) { this.x = x; }
    public void setY(int y) { this.y = y; }
    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }
}
