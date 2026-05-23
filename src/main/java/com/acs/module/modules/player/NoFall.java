package com.acs.module.modules.player;

import com.acs.module.Category;
import com.acs.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Prevents fall damage", Category.PLAYER);
    }

    @Override
    public void onTick() {
        // Handled in ClientConnectionMixin
    }
}
