package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

public class AutoTotem extends Module {
    public AutoTotem() {
        super("AutoTotem", "Automatically equips totems in your offhand", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;

        // Skip if offhand is already a totem
        if (mc.player.getOffHandStack().getItem() == Items.TOTEM_OF_UNDYING) return;

        // Find a totem in the main inventory
        PlayerInventory inv = mc.player.getInventory();
        int totemSlot = -1;
        for (int i = 0; i < 36; i++) {
            if (inv.getStack(i).getItem() == Items.TOTEM_OF_UNDYING) {
                totemSlot = i;
                break;
            }
        }

        if (totemSlot != -1) {
            // Map PlayerInventory slot index to PlayerScreenHandler slot index
            int slotId = totemSlot;
            if (slotId < 9) {
                slotId += 36; // Hotbar slots mapping in PlayerScreenHandler
            }

            // Perform slot swap packet sequence
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 45, 0, SlotActionType.PICKUP, mc.player);
            mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, slotId, 0, SlotActionType.PICKUP, mc.player);
        }
    }
}
