package com.acs.module.modules.client;

import com.acs.gui.clickgui.ClickGUI;
import com.acs.module.Category;
import com.acs.module.Module;
import org.lwjgl.glfw.GLFW;

public class ClickGuiModule extends Module {
    public ClickGuiModule() {
        super("ClickGUI", "Opens the Click GUI", Category.CLIENT);
        setKey(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    @Override
    public void onEnable() {
        if (mc.player != null) {
            mc.setScreen(ClickGUI.INSTANCE);
        }
        setEnabled(false);
    }
}
