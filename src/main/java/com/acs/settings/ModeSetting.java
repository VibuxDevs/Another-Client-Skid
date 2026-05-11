package com.acs.settings;

import java.util.List;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String value, List<String> modes) {
        super(name, value);
        this.modes = modes;
        this.index = modes.indexOf(value);
    }

    public void cycle() {
        index++;
        if (index >= modes.size()) index = 0;
        value = modes.get(index);
    }

    public List<String> getModes() {
        return modes;
    }
}
