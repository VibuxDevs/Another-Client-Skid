package com.acs.settings;

import java.util.function.Supplier;

public abstract class Setting<T> {
    private final String name;
    protected T value;
    private Supplier<Boolean> visibility = () -> true;

    public Setting(String name, T value) {
        this.name = name;
        this.value = value;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isVisible() {
        return visibility.get();
    }

    public void setVisible(Supplier<Boolean> visibility) {
        this.visibility = visibility;
    }
}
