package com.acs.module.modules;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", "Prevents taking fall damage", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.player.networkHandler == null) return;
        if (mc.player.fallDistance > 2.0f) {
            mc.player.networkHandler.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
        }
    }
}
