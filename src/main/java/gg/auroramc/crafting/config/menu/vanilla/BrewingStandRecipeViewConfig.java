package gg.auroramc.crafting.config.menu.vanilla;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;

import static gg.auroramc.crafting.config.ConfigManager.VANILLA_RECIPE_VIEW_PATH;

@Getter
public class BrewingStandRecipeViewConfig extends AuroraConfig {
    private String title = "Brewing recipes";
    private Integer rows = 6;
    private Map<String, ItemConfig> customItems;

    private Items items;
    private Slots slots;

    @Getter
    public static final class Slots {
        private List<Integer> result = List.of(40);
        private List<Integer> ingredient = List.of(22);
        private Integer input = 13;
        private Integer prevRecipe = 52;
        private Integer nextRecipe = 53;
    }

    @Getter
    public static final class Items {
        private ItemConfig filler;
        private ItemConfig back;
    }

    public BrewingStandRecipeViewConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder() + "/" + VANILLA_RECIPE_VIEW_PATH, "brewing_stand.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource(VANILLA_RECIPE_VIEW_PATH + "/brewing_stand.yml", false);
        }
    }
}
