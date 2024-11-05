package gg.auroramc.crafting.config;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.config.menu.WorkbenchConfig;
import lombok.Getter;

@Getter
public class ConfigManager {
    private Config config;
    private MessageConfig messageConfig;
    private WorkbenchConfig workbenchConfig;
    private final AuroraCrafting plugin;

    public ConfigManager(AuroraCrafting plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        Config.saveDefault(plugin);
        config = new Config(plugin);
        config.load();

        MessageConfig.saveDefault(plugin, config.getLanguage());
        messageConfig = new MessageConfig(plugin, config.getLanguage());
        messageConfig.load();

        WorkbenchConfig.saveDefault(plugin);
        workbenchConfig = new WorkbenchConfig(plugin);
        workbenchConfig.load();
    }
}
