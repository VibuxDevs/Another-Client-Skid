package com.acs.settings;

public class BooleanSetting extends Setting<Boolean> {
    public BooleanSetting(String name, boolean value) {
        super(name, value);
    }
    public void toggle() {
        this.value = !this.value;
    }
}
