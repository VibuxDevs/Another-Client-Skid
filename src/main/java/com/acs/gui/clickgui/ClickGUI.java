package com.acs.gui.clickgui;

import com.acs.gui.hud.HudManager;
import com.acs.gui.hud.HudComponent;
import com.acs.module.Category;
import com.acs.manager.FriendManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

import java.util.ArrayList;
import java.util.List;

public class ClickGUI extends Screen {
    public static final ClickGUI INSTANCE = new ClickGUI();
    private final List<Frame> frames = new ArrayList<>();
    private final ParticleSystem particleSystem = new ParticleSystem(100);
    private  final MinecraftClient mc = MinecraftClient.getInstance();
    private int currentPage = 0;
    private String searchText = "";
    private boolean searchFocused = false;

    // Friends page state
    private String friendInput = "";
    private boolean friendInputFocused = false;
    private String friendStatus = "";


public ClickGUI() {
    super(Text.literal("ACS Client"));
    frames.clear();
    int x = mc.getWindow().getScaledWidth();
        frames.add(new Frame(Category.RENDER,  1 * x/8-100,  35, 100, 14));
        frames.add(new Frame(Category.PLAYER,  2 * x/8-100,  35, 100, 14));
        frames.add(new Frame(Category.COMBAT,  3 * x/8-100,  35, 100, 14));
        frames.add(new Frame(Category.MOVEMENT,4 * x/8-100,  35, 100, 14));
        frames.add(new Frame(Category.EXPLOITS,5 * x/8-100,  35, 100, 14));
        frames.add(new Frame(Category.MISC,    6 * x/8-100,  35, 100, 14));
        frames.add(new Frame(Category.WORLD,   7 * x/8-100,  35, 100, 14));
        frames.add(new Frame(Category.CLIENT,  8 * x/8-100,  35, 100, 14));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        context.fill(0, 0, width, height, 0xA0000000);
        particleSystem.render(context, width, height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Header bar
        context.fill(0, 0, width, 25, 0xFF000000);
        context.fillGradient(0, 24, width, 25, 0xFFFF0000, 0x00FF0000);

        // Search bar — highlight border when focused
        int searchBorder = searchFocused ? 0xFFFF0000 : 0xFF333333;
        context.fill(4, 4, 156, 21, searchBorder);
        context.fill(5, 5, 155, 20, 0xFF111111);
        context.fill(5, 5, 7, 20, 0xFFFF0000);
        String displaySearch = searchText.isEmpty() ? (searchFocused ? "_" : "Search...") : searchText + (searchFocused ? "_" : "");
        context.drawTextWithShadow(this.textRenderer, displaySearch, 12, 8, searchText.isEmpty() && !searchFocused ? 0xFF555555 : 0xFFFFFFFF);

        int centerX = this.width / 2;
        renderTab(context, "Modules",  centerX - 80, 5, currentPage == 0);
        renderTab(context, "UI",       centerX - 20, 5, currentPage == 1);
        renderTab(context, "Friends",  centerX + 30, 5, currentPage == 2);
        renderTab(context, "Settings", centerX + 80, 5, currentPage == 3);

        if (currentPage == 0) {
            for (Frame frame : frames) {
                frame.render(context, mouseX, mouseY, delta, searchText);
            }
        } else if (currentPage == 1) {
            renderUIPage(context, mouseX, mouseY);
        } else if (currentPage == 2) {
            renderFriendsPage(context, mouseX, mouseY);
        } else if (currentPage == 3) {
            renderSettingsPage(context, mouseX, mouseY);
        }
    }

    private void renderUIPage(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, "§cHUD Components", centerX, 35, 0xFFFFFFFF);
        context.fill(centerX - 100, 44, centerX + 100, 45, 0xFFFF0000);

        int y = 55;
        for (HudComponent component : HudManager.INSTANCE.getComponents()) {
            boolean hovered = mouseX >= centerX - 100 && mouseX <= centerX + 100 && mouseY >= y && mouseY <= y + 14;
            context.fill(centerX - 101, y - 1, centerX + 101, y + 15, 0xFF000000);
            context.fill(centerX - 100, y, centerX + 100, y + 14, hovered ? 0xFF222222 : 0xFF111111);
            context.fill(centerX - 100, y, centerX - 98, y + 14, component.isEnabled() ? 0xFFFF0000 : 0xFF444444);
            context.drawTextWithShadow(this.textRenderer, component.getName(), centerX - 93, y + 2, component.isEnabled() ? 0xFFFFFFFF : 0xFF888888);
            context.drawTextWithShadow(this.textRenderer, component.isEnabled() ? "ON" : "OFF", centerX + 85 - this.textRenderer.getWidth(component.isEnabled() ? "ON" : "OFF"), y + 2, component.isEnabled() ? 0xFF00FF00 : 0xFFFF4444);
            y += 17;
        }
    }

    private void renderFriendsPage(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, "§cFriends List", centerX, 35, 0xFFFFFFFF);
        context.fill(centerX - 100, 44, centerX + 100, 45, 0xFFFF0000);

        // Friend input box
        int inputBorder = friendInputFocused ? 0xFFFF0000 : 0xFF333333;
        context.fill(centerX - 101, 51, centerX + 51, 66, inputBorder);
        context.fill(centerX - 100, 52, centerX + 50, 65, 0xFF111111);
        String dispInput = friendInput + (friendInputFocused ? "_" : "");
        context.drawTextWithShadow(this.textRenderer, dispInput.isEmpty() && !friendInputFocused ? "Player name..." : dispInput, centerX - 95, 56, friendInput.isEmpty() && !friendInputFocused ? 0xFF555555 : 0xFFFFFFFF);

        // Add/Remove buttons
        boolean addHover = mouseX >= centerX + 55 && mouseX <= centerX + 100 && mouseY >= 51 && mouseY <= 66;
        context.fill(centerX + 54, 51, centerX + 101, 66, 0xFF000000);
        context.fill(centerX + 55, 52, centerX + 100, 65, addHover ? 0xFF1a4d1a : 0xFF112211);
        context.drawCenteredTextWithShadow(this.textRenderer, "Add", centerX + 77, 56, 0xFF00FF00);

        if (!friendStatus.isEmpty()) {
            context.drawCenteredTextWithShadow(this.textRenderer, friendStatus, centerX, 70, 0xFFFFAA00);
        }

        int y = 80;
        for (String friend : FriendManager.INSTANCE.getFriends()) {
            boolean hovered = mouseX >= centerX - 100 && mouseX <= centerX + 100 && mouseY >= y && mouseY <= y + 14;
            context.fill(centerX - 101, y - 1, centerX + 101, y + 15, 0xFF000000);
            context.fill(centerX - 100, y, centerX + 100, y + 14, hovered ? 0xFF222222 : 0xFF111111);
            context.fill(centerX - 100, y, centerX - 98, y + 14, 0xFF00CC00);
            context.drawTextWithShadow(this.textRenderer, friend, centerX - 93, y + 2, 0xFF00FF00);
            context.drawTextWithShadow(this.textRenderer, "✗", centerX + 87, y + 2, 0xFFFF4444);
            y += 17;
        }
    }

    private void renderSettingsPage(DrawContext context, int mouseX, int mouseY) {
        int centerX = this.width / 2;
        context.drawCenteredTextWithShadow(this.textRenderer, "§cGlobal Settings", centerX, 35, 0xFFFFFFFF);
        context.fill(centerX - 100, 44, centerX + 100, 45, 0xFFFF0000);
        context.drawCenteredTextWithShadow(this.textRenderer, "§7More settings coming soon...", centerX, 60, 0xFF888888);
    }

    private void renderTab(DrawContext context, String text, int x, int y, boolean active) {
        int bgColor = active ? 0xFFFF0000 : 0xFF111111;
        context.fill(x - 1, y - 1, x + 51, y + 16, 0xFF000000);
        context.fill(x, y, x + 50, y + 15, bgColor);
        if (active) context.fill(x, y + 14, x + 50, y + 15, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, text, x + (50 - this.textRenderer.getWidth(text)) / 2, y + 3, active ? 0xFFFFFFFF : 0xFFAAAAAA);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int centerX = this.width / 2;

        // Tab clicks
        if (mouseY >= 5 && mouseY <= 20) {
            if (mouseX >= centerX - 80 && mouseX <= centerX - 30) { currentPage = 0; return true; }
            if (mouseX >= centerX - 20 && mouseX <= centerX + 30) { currentPage = 1; return true; }
            if (mouseX >= centerX + 30 && mouseX <= centerX + 80) { currentPage = 2; return true; }
            if (mouseX >= centerX + 80 && mouseX <= centerX + 130){ currentPage = 3; return true; }
        }

        // Search bar focus
        if (mouseX >= 5 && mouseX <= 155 && mouseY >= 5 && mouseY <= 20) {
            searchFocused = true;
            friendInputFocused = false;
            return true;
        } else {
            searchFocused = false;
        }

        if (currentPage == 0) {
            for (Frame frame : frames) {
                if (frame.mouseClicked(mouseX, mouseY, button)) return true;
            }
        } else if (currentPage == 1) {
            // HUD component toggles
            int y = 55;
            for (HudComponent component : HudManager.INSTANCE.getComponents()) {
                if (mouseX >= centerX - 100 && mouseX <= centerX + 100 && mouseY >= y && mouseY <= y + 14) {
                    component.setEnabled(!component.isEnabled());
                    return true;
                }
                y += 17;
            }
        } else if (currentPage == 2) {
            // Add friend button
            if (mouseX >= centerX + 55 && mouseX <= centerX + 100 && mouseY >= 51 && mouseY <= 66) {
                if (!friendInput.isEmpty()) {
                    FriendManager.INSTANCE.addFriend(friendInput);
                    friendStatus = "Added " + friendInput + "!";
                    friendInput = "";
                }
                return true;
            }
            // Remove friend (X button)
            int y = 80;
            for (String friend : new ArrayList<>(FriendManager.INSTANCE.getFriends())) {
                if (mouseX >= centerX + 87 && mouseX <= centerX + 100 && mouseY >= y && mouseY <= y + 14) {
                    FriendManager.INSTANCE.removeFriend(friend);
                    friendStatus = "Removed " + friend;
                    return true;
                }
                y += 17;
            }
            // Friend input focus
            if (mouseX >= centerX - 100 && mouseX <= centerX + 50 && mouseY >= 51 && mouseY <= 66) {
                friendInputFocused = true;
                searchFocused = false;
                return true;
            } else {
                friendInputFocused = false;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        for (Frame frame : frames) frame.mouseReleased(mouseX, mouseY, button);
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchFocused) {
            searchText += chr;
            return true;
        }
        if (friendInputFocused) {
            friendInput += chr;
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        // Route to frames for bind listening first
        if (currentPage == 0) {
            for (Frame frame : frames) {
                if (frame.keyPressed(keyCode)) return true;
            }
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_BACKSPACE) {
            if (searchFocused && !searchText.isEmpty()) {
                searchText = searchText.substring(0, searchText.length() - 1);
                return true;
            }
            if (friendInputFocused && !friendInput.isEmpty()) {
                friendInput = friendInput.substring(0, friendInput.length() - 1);
                return true;
            }
        }
        if (keyCode == org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE) {
            searchFocused = false;
            friendInputFocused = false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
