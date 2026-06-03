package com.acs.gui;

import com.acs.gui.clickgui.ParticleSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.option.OptionsScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.text.Text;

public class ACSMainMenuScreen extends Screen {
    private final ParticleSystem particleSystem = new ParticleSystem(150);

    public ACSMainMenuScreen() {
        super(Text.literal("ACS Main Menu"));
    }

    @Override
    public void renderBackground(DrawContext context, int mouseX, int mouseY, float delta) {
        // Overriding to prevent vanilla dirt/blur background from rendering on top.
        // Draw our custom background gradient and particles.
        context.fill(0, 0, this.width, this.height, 0xFF050505);
        context.fillGradient(0, 0, this.width, this.height, 0x30FF0000, 0x05000000);
        particleSystem.render(context, this.width, this.height);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Render the background first (calls renderBackground above)
        super.render(context, mouseX, mouseY, delta);

        // Center card (Glassmorphic panel)
        int cardWidth = 200;
        int cardHeight = 240;
        int startX = (this.width - cardWidth) / 2;
        int startY = (this.height - cardHeight) / 2;

        // Draw card background (semi-transparent dark)
        context.fill(startX - 1, startY - 1, startX + cardWidth + 1, startY + cardHeight + 1, 0xFF440000); // glowing border
        context.fill(startX, startY, startX + cardWidth, startY + cardHeight, 0xE0101010);

        // Draw logo
        String logoTitle = "ACS CLIENT";
        String logoSub = "Another Client Skid";
        context.drawCenteredTextWithShadow(this.textRenderer, logoTitle, this.width / 2, startY + 20, 0xFFFF0000);
        context.drawCenteredTextWithShadow(this.textRenderer, logoSub, this.width / 2, startY + 32, 0xFF888888);

        // Draw line separator
        context.fill(startX + 20, startY + 48, startX + cardWidth - 20, startY + 49, 0x44FF0000);

        // Render custom buttons
        int buttonY = startY + 65;
        int buttonHeight = 24;
        int buttonSpacing = 8;

        drawCustomButton(context, "Singleplayer", startX + 15, buttonY, cardWidth - 30, buttonHeight, mouseX, mouseY);
        buttonY += buttonHeight + buttonSpacing;

        drawCustomButton(context, "Multiplayer", startX + 15, buttonY, cardWidth - 30, buttonHeight, mouseX, mouseY);
        buttonY += buttonHeight + buttonSpacing;

        drawCustomButton(context, "Options", startX + 15, buttonY, cardWidth - 30, buttonHeight, mouseX, mouseY);
        buttonY += buttonHeight + buttonSpacing;

        drawCustomButton(context, "Quit", startX + 15, buttonY, cardWidth - 30, buttonHeight, mouseX, mouseY);

        // Bottom info
        context.drawTextWithShadow(this.textRenderer, "ACS Client v1.0.0", 5, this.height - 12, 0xFF555555);
        if (client != null && client.getSession() != null) {
            String username = client.getSession().getUsername();
            context.drawTextWithShadow(this.textRenderer, "Logged in as: " + username, this.width - this.textRenderer.getWidth("Logged in as: " + username) - 5, this.height - 12, 0xFF555555);
        }
    }

    private void drawCustomButton(DrawContext context, String label, int x, int y, int w, int h, int mouseX, int mouseY) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int borderCol = hovered ? 0xFFFF0000 : 0xFF222222;
        int bgCol = hovered ? 0xFF1C0505 : 0xFF0D0D0D;
        int textCol = hovered ? 0xFFFFFFFF : 0xFFAAAAAA;

        context.fill(x - 1, y - 1, x + w + 1, y + h + 1, borderCol);
        context.fill(x, y, x + w, y + h, bgCol);
        context.drawCenteredTextWithShadow(this.textRenderer, label, x + w / 2, y + (h - 8) / 2, textCol);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 && client != null) {
            int cardWidth = 200;
            int cardHeight = 240;
            int startX = (this.width - cardWidth) / 2;
            int startY = (this.height - cardHeight) / 2;

            int buttonY = startY + 65;
            int buttonHeight = 24;
            int buttonSpacing = 8;
            int btnW = cardWidth - 30;

            // Singleplayer
            if (mouseX >= startX + 15 && mouseX <= startX + 15 + btnW && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                client.setScreen(new SelectWorldScreen(this));
                return true;
            }
            buttonY += buttonHeight + buttonSpacing;

            // Multiplayer
            if (mouseX >= startX + 15 && mouseX <= startX + 15 + btnW && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                client.setScreen(new MultiplayerScreen(this));
                return true;
            }
            buttonY += buttonHeight + buttonSpacing;

            // Options
            if (mouseX >= startX + 15 && mouseX <= startX + 15 + btnW && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                client.setScreen(new OptionsScreen(this, client.options));
                return true;
            }
            buttonY += buttonHeight + buttonSpacing;

            // Quit
            if (mouseX >= startX + 15 && mouseX <= startX + 15 + btnW && mouseY >= buttonY && mouseY <= buttonY + buttonHeight) {
                client.scheduleStop();
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
