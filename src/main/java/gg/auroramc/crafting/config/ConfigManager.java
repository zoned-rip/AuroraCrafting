package gg.auroramc.crafting.config;

import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

@Getter
public class ConfigManager {
    private Config config;
    private MessageConfig messageConfig;
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
    }
}
