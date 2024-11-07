package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.util.Map;

@Getter
public class RecipeViewConfig extends AuroraConfig {
    private Integer resultSlot = 26;
    private Map<String, ItemConfig> customItems;

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
}
