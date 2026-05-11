package com.acs.settings;

public class NumberSetting extends Setting<Double> {
    private final double min, max, increment;

    public NumberSetting(String name, double value, double min, double max, double increment) {
        super(name, value);
        this.min = min;
        this.max = max;
        this.increment = increment;
    }

    public double getMin() { return min; }
    public double getMax() { return max; }
    public double getIncrement() { return increment; }
}
