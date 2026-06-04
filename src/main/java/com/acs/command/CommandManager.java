package com.acs.command;

import com.acs.config.ConfigManager;
import com.acs.module.Module;
import com.acs.module.ModuleManager;
import com.acs.settings.Setting;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

import java.util.Arrays;

public class CommandManager {
    public static final CommandManager INSTANCE = new CommandManager();
    public static final String PREFIX = ".";

    public boolean handleChat(String message) {
        if (!message.startsWith(PREFIX)) return false;
        String[] parts = message.substring(PREFIX.length()).split(" ");
        if (parts.length == 0) return false;

        String cmd = parts[0].toLowerCase();
        String[] args = Arrays.copyOfRange(parts, 1, parts.length);

        switch (cmd) {
            case "config", "cfg" -> handleConfig(args);
            case "toggle", "t"   -> handleToggle(args);
            case "bind", "b"     -> handleBind(args);
            case "set"           -> handleSet(args);
            case "tp"            -> handleTp(args);
            case "help", "h"     -> handleHelp();
            default -> chat("§cUnknown command. Use §f.help");
        }
        return true;
    }

    private void handleTp(String[] args) {
        if (args.length < 3) {
            chat("§cUsage: .tp <x> <y> <z>");
            return;
        }
        try {
            double x = resolveCoord(args[0], MinecraftClient.getInstance().player.getX());
            double y = resolveCoord(args[1], MinecraftClient.getInstance().player.getY());
            double z = resolveCoord(args[2], MinecraftClient.getInstance().player.getZ());
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null) {
                mc.player.setPosition(x, y, z);
                mc.player.networkHandler.sendPacket(new net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket.PositionAndOnGround(
                    x, y, z, mc.player.isOnGround()
                ));
                chat(String.format("§aTeleported to §f%.1f, %.1f, %.1f", x, y, z));
            }
        } catch (NumberFormatException e) {
            chat("§cInvalid coordinates.");
        }
    }

    private double resolveCoord(String arg, double current) {
        if (arg.startsWith("~")) {
            if (arg.length() == 1) return current;
            return current + Double.parseDouble(arg.substring(1));
        }
        return Double.parseDouble(arg);
    }

    private void handleConfig(String[] args) {
        if (args.length < 1) { chat("§cUsage: .config <save|load|list> [name]"); return; }
        switch (args[0].toLowerCase()) {
            case "save" -> {
                String name = args.length > 1 ? args[1] : "default";
                ConfigManager.INSTANCE.save(name);
                chat("§aConfig saved: §f" + name);
            }
            case "load" -> {
                String name = args.length > 1 ? args[1] : "default";
                if (ConfigManager.INSTANCE.load(name)) chat("§aConfig loaded: §f" + name);
                else chat("§cConfig not found: §f" + name);
            }
            case "list" -> {
                String[] configs = ConfigManager.INSTANCE.listConfigs();
                if (configs.length == 0) chat("§7No configs found.");
                else chat("§aConfigs: §f" + String.join("§7, §f", configs));
            }
            default -> chat("§cUsage: .config <save|load|list> [name]");
        }
    }

    private void handleToggle(String[] args) {
        if (args.length < 1) { chat("§cUsage: .toggle <module>"); return; }
        String name = String.join(" ", args);
        Module m = ModuleManager.INSTANCE.getModuleByName(name);
        if (m == null) { chat("§cModule not found: §f" + name); return; }
        m.toggle();
        chat((m.isEnabled() ? "§aEnabled: " : "§cDisabled: ") + "§f" + m.getName());
    }

    private void handleBind(String[] args) {
        if (args.length < 2) { chat("§cUsage: .bind <module> <key|none>"); return; }
        String keyArg = args[args.length - 1];
        String modName = String.join(" ", Arrays.copyOfRange(args, 0, args.length - 1));
        Module m = ModuleManager.INSTANCE.getModuleByName(modName);
        if (m == null) { chat("§cModule not found: §f" + modName); return; }
        if (keyArg.equalsIgnoreCase("none")) {
            m.setKey(0);
            chat("§aUnbound: §f" + m.getName());
            return;
        }
        int key = resolveKey(keyArg.toUpperCase());
        if (key == -1) { chat("§cUnknown key: §f" + keyArg); return; }
        m.setKey(key);
        chat("§aBound §f" + m.getName() + " §ato §f" + keyArg.toUpperCase());
    }

    private void handleHelp() {
        chat("§c§lACS Commands:");
        chat("§f.config <save|load|list> [name] §7- Manage configs");
        chat("§f.toggle <module> §7- Toggle a module");
        chat("§f.bind <module> <key|none> §7- Set a keybind");
        chat("§f.set <module> <setting> <value> §7- Set module setting value");
        chat("§f.tp <x> <y> <z> §7- Teleport to coordinates (relative ~ supported)");
        chat("§f.help §7- Show this message");
    }

    private int resolveKey(String name) {
        return switch (name) {
            case "F1"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F1;
            case "F2"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F2;
            case "F3"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F3;
            case "F4"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F4;
            case "F5"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F5;
            case "F6"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F6;
            case "F7"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F7;
            case "F8"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F8;
            case "F9"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_F9;
            case "F10" -> org.lwjgl.glfw.GLFW.GLFW_KEY_F10;
            case "F11" -> org.lwjgl.glfw.GLFW.GLFW_KEY_F11;
            case "F12" -> org.lwjgl.glfw.GLFW.GLFW_KEY_F12;
            case "R"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_R;
            case "T"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_T;
            case "Y"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_Y;
            case "U"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_U;
            case "I"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_I;
            case "O"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_O;
            case "P"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_P;
            case "G"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_G;
            case "H"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_H;
            case "J"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_J;
            case "K"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_K;
            case "L"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_L;
            case "Z"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
            case "X"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_X;
            case "C"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_C;
            case "V"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_V;
            case "B"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_B;
            case "N"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_N;
            case "M"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_M;
            case "HOME"        -> org.lwjgl.glfw.GLFW.GLFW_KEY_HOME;
            case "END"         -> org.lwjgl.glfw.GLFW.GLFW_KEY_END;
            case "INSERT"      -> org.lwjgl.glfw.GLFW.GLFW_KEY_INSERT;
            case "DELETE"      -> org.lwjgl.glfw.GLFW.GLFW_KEY_DELETE;
            case "PAGE_UP"     -> org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_UP;
            case "PAGE_DOWN"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_PAGE_DOWN;
            case "RIGHT_SHIFT" -> org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_SHIFT;
            case "LEFT_SHIFT"  -> org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_SHIFT;
            case "RIGHT_ALT"   -> org.lwjgl.glfw.GLFW.GLFW_KEY_RIGHT_ALT;
            case "LEFT_ALT"    -> org.lwjgl.glfw.GLFW.GLFW_KEY_LEFT_ALT;
            default -> -1;
        };
    }

    private void handleSet(String[] args) {
        if (args.length < 3) {
            chat("§cUsage: .set <module> <setting> <value>");
            return;
        }
        String modName = args[0];
        String settingName = args[1];
        String valueStr = String.join(" ", Arrays.copyOfRange(args, 2, args.length));

        Module m = ModuleManager.INSTANCE.getModuleByName(modName);
        if (m == null) {
            chat("§cModule not found: §f" + modName);
            return;
        }

        Setting<?> setting = m.getSettings().stream()
                .filter(s -> s.getName().equalsIgnoreCase(settingName))
                .findFirst().orElse(null);

        if (setting == null) {
            chat("§cSetting not found: §f" + settingName);
            return;
        }

        try {
            if (setting instanceof com.acs.settings.BooleanSetting bs) {
                bs.setValue(Boolean.parseBoolean(valueStr));
            } else if (setting instanceof com.acs.settings.NumberSetting ns) {
                ns.setValue(Double.parseDouble(valueStr));
            } else if (setting instanceof com.acs.settings.ModeSetting ms) {
                if (ms.getModes().contains(valueStr)) {
                    ms.setValue(valueStr);
                } else {
                    chat("§cInvalid mode. Available: §f" + String.join(", ", ms.getModes()));
                    return;
                }
            } else if (setting instanceof com.acs.settings.StringSetting ss) {
                ss.setValue(valueStr);
            }
            chat(String.format("§aSet §f%s §7(§f%s§7) to §f%s", setting.getName(), m.getName(), valueStr));
        } catch (Exception e) {
            chat("§cFailed to parse/set value: §f" + valueStr);
        }
    }

    private void chat(String msg) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) mc.player.sendMessage(Text.literal(msg), false);
    }
}
