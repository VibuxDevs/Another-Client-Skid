package com.acs.module.modules.render;

import com.acs.module.Category;
import com.acs.module.Module;
import com.acs.settings.NumberSetting;
import com.acs.settings.BooleanSetting;

public class ESP extends Module {

    private final BooleanSetting players = new BooleanSetting("Players", true);
    private final BooleanSetting mobs = new BooleanSetting("Mobs", false);
    private final BooleanSetting animals = new BooleanSetting("Animals", false);
    private final BooleanSetting items = new BooleanSetting("Items", true);
    
    private final NumberSetting lineWidth = new NumberSetting("Line Width", 1.5, 0.1, 5.0, 0.1);

    public ESP() {
        super("ESP", "Highlights entities through walls", Category.RENDER);
        addSetting(players);
        addSetting(mobs);
        addSetting(animals);
        addSetting(items);
        addSetting(lineWidth);
    }
    
    // Rendering logic typically handled in mixins (e.g. WorldRendererMixin)
    public boolean shouldRenderPlayers() { return players.getValue(); }
    public boolean shouldRenderMobs() { return mobs.getValue(); }
    public boolean shouldRenderAnimals() { return animals.getValue(); }
    public boolean shouldRenderItems() { return items.getValue(); }
    public float getLineWidth() { return lineWidth.getValue().floatValue(); }
}
