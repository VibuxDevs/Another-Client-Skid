package com.acs.gui;

import com.acs.mixin.MinecraftClientAccessor;
import com.acs.utils.MicrosoftAuth;
import com.acs.utils.AltManager;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.session.Session;
import net.minecraft.text.Text;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AltSwitcherScreen extends Screen {
    private final Screen parent;
    private TextFieldWidget usernameField;
    private String status = "Idle";
    private Thread pollThread;

    public AltSwitcherScreen(Screen parent) {
        super(Text.literal("Alt Switcher"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        AltManager.load();
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        // Add username input field
        usernameField = new TextFieldWidget(
            this.textRenderer,
            centerX - 150,
            centerY - 40,
            140,
            20,
            Text.literal("Username")
        );
        usernameField.setMaxLength(16);
        usernameField.setText("");
        this.addSelectableChild(usernameField);

        // Add offline login button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Login (Offline)"), button -> {
            stopPolling();
            String username = usernameField.getText().trim();
            if (!username.isEmpty()) {
                try {
                    UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + username).getBytes(StandardCharsets.UTF_8));
                    Session session = new Session(
                        username,
                        uuid,
                        "",
                        Optional.empty(),
                        Optional.empty(),
                        Session.AccountType.MOJANG
                    );
                    ((MinecraftClientAccessor) client).setSession(session);
                    status = "Logged in as: " + username;
                    AltManager.addAlt(false, username, "", "");
                    this.clearAndInit();
                } catch (Exception e) {
                    status = "Error: " + e.getMessage();
                }
            } else {
                status = "Username cannot be empty!";
            }
        }).dimensions(centerX - 150, centerY, 140, 20).build());

        // Add Microsoft login button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Login (Microsoft)"), button -> {
            stopPolling();
            status = "Requesting device code...";
            new Thread(() -> {
                try {
                    MicrosoftAuth.DeviceCodeInfo info = MicrosoftAuth.getDeviceCode();
                    status = "Code: " + info.userCode + " (copied). Link: " + info.verificationUri;
                    
                    // Copy code to clipboard on client thread
                    client.execute(() -> {
                        client.keyboard.setClipboard(info.userCode);
                    });

                    // Start background polling
                    pollThread = new Thread(() -> {
                        while (pollThread != null && !Thread.currentThread().isInterrupted()) {
                            MicrosoftAuth.LoginResult result = MicrosoftAuth.pollToken(info.deviceCode);
                            if (result.error == null) {
                                try {
                                    UUID uuid = parseDashlessUuid(result.uuid);
                                    Session session = new Session(
                                        result.username,
                                        uuid,
                                        result.token,
                                        Optional.empty(),
                                        Optional.empty(),
                                        Session.AccountType.MOJANG
                                    );
                                    client.execute(() -> {
                                        ((MinecraftClientAccessor) client).setSession(session);
                                    });
                                    status = "Logged in as: " + result.username;
                                    AltManager.addAlt(true, result.username, result.uuid, result.refreshToken);
                                    client.execute(this::clearAndInit);
                                } catch (Exception e) {
                                    status = "Session error: " + e.getMessage();
                                }
                                break;
                            } else if (!"PENDING".equals(result.error)) {
                                status = "Error: " + result.error;
                                break;
                            }

                            try {
                                Thread.sleep(info.interval * 1000L);
                            } catch (InterruptedException e) {
                                break;
                            }
                        }
                    });
                    pollThread.start();

                } catch (Exception e) {
                    status = "Failed to start MS Auth: " + e.getMessage();
                }
            }).start();
        }).dimensions(centerX - 150, centerY + 25, 140, 20).build());

        // Add back button
        this.addDrawableChild(ButtonWidget.builder(Text.literal("Back"), button -> {
            stopPolling();
            client.setScreen(parent);
        }).dimensions(centerX - 150, centerY + 50, 140, 20).build());

        // Draw the saved alts list
        List<AltManager.AltEntry> entries = AltManager.getAlts();
        int altY = centerY - 40;
        for (int i = 0; i < Math.min(entries.size(), 5); i++) {
            AltManager.AltEntry alt = entries.get(i);
            // Login button for this alt
            String displayName = alt.username + " (" + (alt.isMicrosoft ? "MS" : "Off") + ")";
            this.addDrawableChild(ButtonWidget.builder(Text.literal(displayName), btn -> {
                stopPolling();
                if (alt.isMicrosoft) {
                    status = "Refreshing " + alt.username + "...";
                    new Thread(() -> {
                        MicrosoftAuth.LoginResult res = MicrosoftAuth.loginWithRefreshToken(alt.refreshToken);
                        if (res.error == null) {
                            try {
                                UUID uuid = parseDashlessUuid(res.uuid);
                                Session session = new Session(
                                    res.username,
                                    uuid,
                                    res.token,
                                    Optional.empty(),
                                    Optional.empty(),
                                    Session.AccountType.MOJANG
                                );
                                client.execute(() -> {
                                    ((MinecraftClientAccessor) client).setSession(session);
                                });
                                status = "Logged in as: " + res.username;
                                AltManager.addAlt(true, res.username, res.uuid, res.refreshToken);
                                client.execute(this::clearAndInit);
                            } catch (Exception e) {
                                status = "Session error: " + e.getMessage();
                            }
                        } else {
                            status = "Failed: " + res.error;
                        }
                    }).start();
                } else {
                    // Offline login
                    try {
                        UUID uuid = UUID.nameUUIDFromBytes(("OfflinePlayer:" + alt.username).getBytes(StandardCharsets.UTF_8));
                        Session session = new Session(
                            alt.username,
                            uuid,
                            "",
                            Optional.empty(),
                            Optional.empty(),
                            Session.AccountType.MOJANG
                        );
                        ((MinecraftClientAccessor) client).setSession(session);
                        status = "Logged in as: " + alt.username;
                    } catch (Exception e) {
                        status = "Error: " + e.getMessage();
                    }
                }
            }).dimensions(centerX + 10, altY + i * 22, 135, 20).build());

            // Delete button for this alt
            this.addDrawableChild(ButtonWidget.builder(Text.literal("X"), btn -> {
                AltManager.removeAlt(alt.username);
                this.clearAndInit();
            }).dimensions(centerX + 150, altY + i * 22, 20, 20).build());
        }
    }

    private void stopPolling() {
        if (pollThread != null) {
            pollThread.interrupt();
            pollThread = null;
        }
    }

    private UUID parseDashlessUuid(String dashless) {
        if (dashless == null || dashless.length() != 32) {
            return UUID.randomUUID();
        }
        String withDashes = dashless.substring(0, 8) + "-" +
                             dashless.substring(8, 12) + "-" +
                             dashless.substring(12, 16) + "-" +
                             dashless.substring(16, 20) + "-" +
                             dashless.substring(20);
        return UUID.fromString(withDashes);
    }

    @Override
    public void close() {
        stopPolling();
        super.close();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);

        // Render title
        context.drawCenteredTextWithShadow(this.textRenderer, "Alt Switcher", this.width / 2, 20, 0xFFFF0000);

        // Render current session info
        String currentSession = "Current User: " + client.getSession().getUsername() + " (" + client.getSession().getUuidOrNull() + ")";
        context.drawCenteredTextWithShadow(this.textRenderer, currentSession, this.width / 2, 50, 0xFFAAAAAA);

        // Render status
        context.drawCenteredTextWithShadow(this.textRenderer, "Status: " + status, this.width / 2, 70, 0xFFFF5555);

        // Render labels
        context.drawTextWithShadow(this.textRenderer, "New Username:", this.width / 2 - 150, this.height / 2 - 52, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Saved Accounts:", this.width / 2 + 10, this.height / 2 - 52, 0xFFFFFFFF);

        if (AltManager.getAlts().isEmpty()) {
            context.drawTextWithShadow(this.textRenderer, "No saved accounts", this.width / 2 + 10, this.height / 2 - 35, 0xFF888888);
        }

        usernameField.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        usernameField.mouseClicked(mouseX, mouseY, button);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (usernameField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (usernameField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }
}
