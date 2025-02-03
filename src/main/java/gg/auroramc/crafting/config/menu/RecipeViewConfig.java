package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

@Getter
public class RecipeViewConfig extends AuroraConfig {
    private String title = "Recipe";
    private Map<String, Integer> resultSlot = Map.of("default", 25);
    private Map<String, ItemConfig> customItems;
    private Map<String, ItemConfig> items;

    public RecipeViewConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder() + "/menus", "recipe_view.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("menus/recipe_view.yml", false);
        }
    }

    @Override
    protected List<Consumer<YamlConfiguration>> getMigrationSteps() {
        return List.of(
                (yaml) -> {
                    // released config file was broken, so we need to fix it here
                    if (yaml.getInt("config-version", 0) == 0 && !yaml.isInt("result-slot")) {
                        yaml.set("config-version", 1);
                        return;
                    }
                    yaml.set("result-slot.default", yaml.getInt("result-slot", 25));
                    yaml.set("config-version", 1);
                }
        );
    }
}
