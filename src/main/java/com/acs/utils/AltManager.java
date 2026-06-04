package com.acs.utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import net.fabricmc.loader.api.FabricLoader;

public class AltManager {
    public static class AltEntry {
        public boolean isMicrosoft;
        public String username;
        public String uuid;
        public String refreshToken;

        public AltEntry(boolean isMicrosoft, String username, String uuid, String refreshToken) {
            this.isMicrosoft = isMicrosoft;
            this.username = username;
            this.uuid = uuid;
            this.refreshToken = refreshToken;
        }
    }

    private static final List<AltEntry> alts = new ArrayList<>();
    
    private static File getConfigFile() {
        return new File(FabricLoader.getInstance().getConfigDir().toFile(), "acs_alts.txt");
    }

    public static List<AltEntry> getAlts() {
        return alts;
    }

    public static void load() {
        alts.clear();
        File file = getConfigFile();
        if (!file.exists()) return;
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;
                String[] parts = line.split(":", 4);
                if (parts.length < 2) continue;
                if ("offline".equalsIgnoreCase(parts[0])) {
                    alts.add(new AltEntry(false, parts[1], "", ""));
                } else if ("microsoft".equalsIgnoreCase(parts[0]) && parts.length >= 4) {
                    alts.add(new AltEntry(true, parts[1], parts[2], parts[3]));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void save() {
        File file = getConfigFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            for (AltEntry alt : alts) {
                if (alt.isMicrosoft) {
                    writer.write("microsoft:" + alt.username + ":" + alt.uuid + ":" + alt.refreshToken);
                } else {
                    writer.write("offline:" + alt.username);
                }
                writer.newLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void addAlt(boolean isMicrosoft, String username, String uuid, String refreshToken) {
        // Remove existing alt with same username case-insensitive
        alts.removeIf(alt -> alt.username.equalsIgnoreCase(username));
        alts.add(new AltEntry(isMicrosoft, username, uuid, refreshToken));
        save();
    }

    public static void removeAlt(String username) {
        alts.removeIf(alt -> alt.username.equalsIgnoreCase(username));
        save();
    }
}
