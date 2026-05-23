package com.acs.module.modules.render;

import com.acs.module.Category;
import com.acs.module.Module;

public class XRay extends Module {

    public XRay() {
        super("XRay", "Allows you to see ores through walls", Category.RENDER);
    }

    @Override
    public void onEnable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }

    @Override
    public void onDisable() {
        if (mc.worldRenderer != null) {
            mc.worldRenderer.reload();
        }
    }
}
