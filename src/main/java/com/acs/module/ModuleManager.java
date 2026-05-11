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
        addModule(new com.acs.module.modules.KillAura());
        addModule(new com.acs.module.modules.Sprint());
        addModule(new com.acs.module.modules.Fullbright());
        addModule(new com.acs.module.modules.ClickGuiModule());
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
