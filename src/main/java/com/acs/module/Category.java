package com.acs.module;

public enum Category {
    COMBAT("Combat"),
    MOVEMENT("Movement"),
    RENDER("Render"),
    PLAYER("Player"),
    EXPLOITS("Exploits"),
    MISC("Misc"),
    WORLD("World"),
<<<<<<< Updated upstream
    CLIENT("Client");
=======
    CRASH("Crash");
>>>>>>> Stashed changes

    private final String name;

    Category(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
