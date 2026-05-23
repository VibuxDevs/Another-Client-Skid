package com.acs.module.modules.movement;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.util.math.Vec3d;
import com.acs.settings.NumberSetting;
import com.acs.settings.BooleanSetting;

public class Velocity extends Module {
    
    private final NumberSetting horizontal = new NumberSetting("Horizontal", 0.0, 0.0, 100.0, 1.0);
    private final NumberSetting vertical = new NumberSetting("Vertical", 0.0, 0.0, 100.0, 1.0);
    private final BooleanSetting explosions = new BooleanSetting("Explosions", true);
    private final BooleanSetting fishingHooks = new BooleanSetting("FishingHooks", true);

    public Velocity() {
        super("Velocity", "Modifies knockback taken", Category.MOVEMENT);
        addSetting(horizontal);
        addSetting(vertical);
        addSetting(explosions);
        addSetting(fishingHooks);
    }

    // Handled in mixins typically, but keeping state here
    public double getHorizontal() { return horizontal.getValue() / 100.0; }
    public double getVertical() { return vertical.getValue() / 100.0; }
    public boolean handleExplosions() { return explosions.getValue(); }
    public boolean handleFishingHooks() { return fishingHooks.getValue(); }
}
