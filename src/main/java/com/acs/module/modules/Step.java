package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;
import net.minecraft.entity.attribute.EntityAttributeInstance;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.attribute.EntityAttributes;
import net.minecraft.util.Identifier;

public class Step extends Module {
    private final NumberSetting height = new NumberSetting("Height", 1.0, 0.5, 3.0, 0.5);
    private static final Identifier MODIFIER_ID = Identifier.of("acs", "step_height");

    public Step() {
        super("Step", "Allows you to step up blocks automatically", Category.MOVEMENT);
        addSetting(height);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_ID);
            double target = height.getValue();
            double diff = target - 0.6;
            if (diff > 0) {
                EntityAttributeModifier modifier = new EntityAttributeModifier(MODIFIER_ID, diff, EntityAttributeModifier.Operation.ADD_VALUE);
                attribute.addTemporaryModifier(modifier);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.player == null) return;
        EntityAttributeInstance attribute = mc.player.getAttributeInstance(EntityAttributes.GENERIC_STEP_HEIGHT);
        if (attribute != null) {
            attribute.removeModifier(MODIFIER_ID);
        }
    }
}
