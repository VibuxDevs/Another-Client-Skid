package com.acs.module.modules.render;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.client.option.SimpleOption;
import com.acs.mixin.SimpleOptionAccessor;

import java.lang.reflect.Field;

public class Fullbright extends Module {
    private double savedGamma = 1.0;

    public Fullbright() {
        super("Fullbright", "Makes everything bright", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.options != null) {
            savedGamma = mc.options.getGamma().getValue();
            setGamma(16.0);
        }
    }

    @Override
    public void onTick() {
        if (mc.options != null) {
            setGamma(16.0);
        }
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            setGamma(savedGamma);
        }
    }

    private void setGamma(double value) {
        try {
            SimpleOptionAccessor accessor = (SimpleOptionAccessor) (Object) mc.options.getGamma();
            accessor.setValueDirect(value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
