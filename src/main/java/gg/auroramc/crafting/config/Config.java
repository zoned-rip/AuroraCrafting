package gg.auroramc.crafting.config;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.function.Consumer;

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
    private Boolean autoDiscoverVanillaRecipes = false;

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

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    yaml.set("config-version", 1);
                    yaml.set("auto-discover-vanilla-recipes", false);
                    yaml.setComments("auto-discover-vanilla-recipes", List.of("Should players auto discover vanilla recipes?"));
                }
        );
    }
}
