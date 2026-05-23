package com.acs.module.modules.movement;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import com.acs.settings.NumberSetting;

public class Step extends Module {

    private final NumberSetting height = new NumberSetting("Height", 2.0, 1.0, 4.0, 0.5);

    public Step() {
        super("Step", "Allows you to step up blocks instantly", Category.MOVEMENT);
        addSetting(height);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attribute != null) {
            attribute.setBaseValue(height.getValue().floatValue());
        }
    }

    @Override
    public void onDisable() {
        if (mc.player != null) {
            EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
            if (attribute != null) {
                attribute.setBaseValue(0.6f); // Default Minecraft step height
            }
        }
    }
}
