package com.acs.module.modules.combat;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    
    private final NumberSetting healthThreshold = new NumberSetting("Health", 16.0, 1.0, 36.0, 1.0);

    public AutoTotem() {
        super("AutoTotem", "Automatically places a totem in your offhand", Category.COMBAT);
        addSetting(healthThreshold);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;
        
        float health = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        
        if (health <= healthThreshold.getValue()) {
            int totemSlot = getTotemSlot();
            if (totemSlot != -1) {
                moveToOffhand(totemSlot);
            }
        }
    }
    
    private int getTotemSlot() {
        for (int i = 9; i < 45; i++) {
            if (mc.player.getInventory().getStack(i >= 36 ? i - 36 : i).getItem() == Items.TOTEM_OF_UNDYING) {
                return i;
            }
        }
        return -1;
    }
    
    private void moveToOffhand(int slot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player); // 45 is offhand slot in player inventory
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.PICKUP, mc.player);
    }
}
