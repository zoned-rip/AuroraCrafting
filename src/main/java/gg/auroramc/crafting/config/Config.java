package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.util.List;

@Getter
public class Config extends AuroraConfig {
    private Boolean debug = false;
    private String language = "en";
    private CommandAliasConfig commandAliases;
    private Boolean includeVanillaRecipes = false;
    private Boolean includeOtherPluginRecipes = false;
    private Boolean openInsteadOfCraftingTable = false;
    private Boolean openShiftClickCraftingTable = false;
    private Integer clickCooldown = 75;
    private Integer shiftClickCooldown = 200;

    @Getter
    public static final class CommandAliasConfig {
        private List<String> craft = List.of("craft");
        private List<String> recipes = List.of("recipes");
        private List<String> merchants = List.of("merchants");
    }

    public Config(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder(), "config.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("config.yml", false);
        }
    }
}
