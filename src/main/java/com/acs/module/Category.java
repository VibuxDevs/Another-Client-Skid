package com.acs.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    EXPLOITS("Exploits"),
    MISC("Misc"),
    WORLD("World"),
    CLIENT("Client"),
    CRASH("Crash");

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
