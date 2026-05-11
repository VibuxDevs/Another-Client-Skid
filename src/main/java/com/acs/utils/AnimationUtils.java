package com.acs.utils;

public class AnimationUtils {
    public static float lerp(float start, float end, float delta) {
        return start + delta * (end - start);
    }

    public static float moveTowards(float current, float target, float speed) {
        if (current < target) {
            current = Math.min(current + speed, target);
        } else if (current > target) {
            current = Math.max(current - speed, target);
        }
        return current;
    }
}
