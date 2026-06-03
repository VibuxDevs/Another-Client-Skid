package com.acs.module;

import com.acs.settings.Setting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ModuleManager {
    public static final ModuleManager INSTANCE = new ModuleManager();
    private final List<Module> modules = new ArrayList<>();
    private final Map<Module, List<Setting<?>>> settings = new HashMap<>();

    public ModuleManager() {
<<<<<<< Updated upstream
        addModule(new com.acs.module.modules.pvp.KillAura());
        addModule(new com.acs.module.modules.pvp.CrystalAura());
        addModule(new com.acs.module.modules.combat.AutoTotem());
        addModule(new com.acs.module.modules.combat.AutoArmor());
        addModule(new com.acs.module.modules.movement.Sprint());
        addModule(new com.acs.module.modules.movement.Step());
        addModule(new com.acs.module.modules.movement.Velocity());
        addModule(new com.acs.module.modules.movement.Flight());
        addModule(new com.acs.module.modules.render.Fullbright());
        addModule(new com.acs.module.modules.render.ESP());
        addModule(new com.acs.module.modules.render.Freecam());
        addModule(new com.acs.module.modules.render.XRay());
        addModule(new com.acs.module.modules.world.Scaffold());
        addModule(new com.acs.module.modules.world.Printer());
        addModule(new com.acs.module.modules.world.AutoHighway());
        addModule(new com.acs.module.modules.player.FastPlace());
        addModule(new com.acs.module.modules.player.NoFall());
        addModule(new com.acs.module.modules.movement.ElytraFly());
        addModule(new com.acs.module.modules.client.ClickGuiModule());
        addModule(new com.acs.module.modules.exploits.ReverseGhostBlock());
        addModule(new com.acs.module.modules.exploits.CoordLogger());
        addModule(new com.acs.module.modules.exploits.ClickTP());
        addModule(new com.acs.module.modules.exploits.Blink());
=======
        addModule(new com.acs.module.modules.KillAura());
        addModule(new com.acs.module.modules.Sprint());
        addModule(new com.acs.module.modules.Fullbright());
        addModule(new com.acs.module.modules.ClickGuiModule());
        addModule(new com.acs.module.modules.Fly());
        addModule(new com.acs.module.modules.NoFall());
        addModule(new com.acs.module.modules.FastPlace());
        addModule(new com.acs.module.modules.AutoTotem());
        addModule(new com.acs.module.modules.Speed());
        addModule(new com.acs.module.modules.Step());
        addModule(new com.acs.module.modules.Velocity());
        addModule(new com.acs.module.modules.TriggerBot());
        addModule(new com.acs.module.modules.MaceKill());
        addModule(new com.acs.module.modules.MaceBot());
        addModule(new com.acs.module.modules.BoatFly());
        addModule(new com.acs.module.modules.BoatPhase());
>>>>>>> Stashed changes
    }

    private void addModule(Module module) {
        modules.add(module);
        settings.put(module, new ArrayList<>());
    }

    public List<Module> getModules() {
        return modules;
    }

    public List<Module> getModulesByCategory(Category category) {
        return modules.stream().filter(m -> m.getCategory() == category).collect(Collectors.toList());
    }

    public List<Setting<?>> getSettingsForModule(Module module) {
        return settings.get(module);
    }

    public Module getModuleByName(String name) {
        return modules.stream()
                .filter(m -> m.getName().equalsIgnoreCase(name))
                .findFirst().orElse(null);
    }
}
