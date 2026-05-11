package com.acs.config;

import com.acs.module.Module;
import com.acs.module.ModuleManager;
import com.acs.settings.BooleanSetting;
import com.acs.settings.ModeSetting;
import com.acs.settings.NumberSetting;
import com.acs.settings.Setting;
import com.google.gson.*;
import net.minecraft.client.MinecraftClient;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    public static final ConfigManager INSTANCE = new ConfigManager();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private Path getConfigDir() {
        Path dir = MinecraftClient.getInstance().runDirectory.toPath().resolve("acs").resolve("configs");
        try { Files.createDirectories(dir); } catch (Exception ignored) {}
        return dir;
    }

    public void save(String name) {
        JsonObject root = new JsonObject();
        JsonArray modules = new JsonArray();

        for (Module module : ModuleManager.INSTANCE.getModules()) {
            JsonObject obj = new JsonObject();
            obj.addProperty("name", module.getName());
            obj.addProperty("enabled", module.isEnabled());
            obj.addProperty("key", module.getKey());

            JsonArray settings = new JsonArray();
            for (Setting<?> setting : module.getSettings()) {
                JsonObject s = new JsonObject();
                s.addProperty("name", setting.getName());
                s.addProperty("value", setting.getValue().toString());
                settings.add(s);
            }
            obj.add("settings", settings);
            modules.add(obj);
        }

        root.add("modules", modules);

        try (Writer w = Files.newBufferedWriter(getConfigDir().resolve(name + ".json"))) {
            GSON.toJson(root, w);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean load(String name) {
        Path file = getConfigDir().resolve(name + ".json");
        if (!Files.exists(file)) return false;

        try (Reader r = Files.newBufferedReader(file)) {
            JsonObject root = GSON.fromJson(r, JsonObject.class);
            JsonArray modules = root.getAsJsonArray("modules");

            for (JsonElement el : modules) {
                JsonObject obj = el.getAsJsonObject();
                String modName = obj.get("name").getAsString();
                Module module = ModuleManager.INSTANCE.getModuleByName(modName);
                if (module == null) continue;

                boolean enabled = obj.get("enabled").getAsBoolean();
                int key = obj.get("key").getAsInt();

                if (module.isEnabled() != enabled) module.setEnabled(enabled);
                module.setKey(key);

                if (obj.has("settings")) {
                    for (JsonElement se : obj.getAsJsonArray("settings")) {
                        JsonObject sObj = se.getAsJsonObject();
                        String sName = sObj.get("name").getAsString();
                        String sVal = sObj.get("value").getAsString();
                        for (Setting<?> setting : module.getSettings()) {
                            if (!setting.getName().equals(sName)) continue;
                            if (setting instanceof BooleanSetting bs) bs.setValue(Boolean.parseBoolean(sVal));
                            else if (setting instanceof ModeSetting ms) ms.setValue(sVal);
                            else if (setting instanceof NumberSetting ns) ns.setValue(Double.parseDouble(sVal));
                        }
                    }
                }
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public String[] listConfigs() {
        File[] files = getConfigDir().toFile().listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return new String[0];
        String[] names = new String[files.length];
        for (int i = 0; i < files.length; i++) names[i] = files[i].getName().replace(".json", "");
        return names;
    }
}
