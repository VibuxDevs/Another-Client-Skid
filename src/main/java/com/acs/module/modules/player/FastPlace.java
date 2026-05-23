package com.acs.module.modules.player;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.PlayerInteractItemC2SPacket;
import net.minecraft.util.Hand;

public class FastPlace extends Module {

    private final NumberSetting delay = new NumberSetting("Delay", 0.0, 0.0, 4.0, 1.0);

    public FastPlace() {
        super("FastPlace", "Allows you to place items faster", Category.PLAYER);
        addSetting(delay);
    }
    
    @Override
    public void onTick() {
        if (mc.player == null) return;
        
        // This is typically handled by mixing into MinecraftClient.doItemUse()
        // Setting itemUseCooldown to the desired delay.
    }
    
    public int getDelay() {
        return delay.getValue().intValue();
    }
}
