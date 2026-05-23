package com.acs.module.modules.combat;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.SlotActionType;

public class AutoArmor extends Module {

    public AutoArmor() {
        super("AutoArmor", "Automatically equips best armor", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.player.currentScreenHandler == mc.player.playerScreenHandler) {
            for (int type = 0; type < 4; type++) {
                int bestArmorSlot = getBestArmorSlot(type);
                if (bestArmorSlot != -1) {
                    equipArmor(bestArmorSlot);
                }
            }
        }
    }

    private int getBestArmorSlot(int type) {
        int bestSlot = -1;
        int bestProtection = 0;

        // Currently equipped armor
        ItemStack equipped = mc.player.getInventory().getArmorStack(type);
        if (equipped.getItem() instanceof ArmorItem) {
            bestProtection = ((ArmorItem) equipped.getItem()).getProtection();
        }

        for (int i = 9; i < 45; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i >= 36 ? i - 36 : i);
            if (stack.getItem() instanceof ArmorItem armor) {
                // Check if it's the correct slot type (helmet, chestplate, etc)
                if (armor.getType().getEquipmentSlot().getEntitySlotId() == type) {
                    if (armor.getProtection() > bestProtection) {
                        bestProtection = armor.getProtection();
                        bestSlot = i;
                    }
                }
            }
        }
        return bestSlot;
    }

    private void equipArmor(int slot) {
        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slot, 0, SlotActionType.QUICK_MOVE, mc.player);
    }
}
