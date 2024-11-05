package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;

import java.io.File;
import java.util.List;
import java.util.Map;

@Getter
public class WorkbenchConfig extends AuroraConfig {
    private ItemConfig filler;
    private Map<String, ItemConfig> customItems;
    private String title = "Workbench";
    private Integer rows = 6;
    private Integer resultSlot = 25;
    private List<Integer> matrixSlots = List.of(10, 11, 12, 19, 20, 21, 28, 29, 30);
    private ItemConfig invalidResultItem;

    public WorkbenchConfig(AuroraCrafting plugin) {
        super(getFile(plugin));
    }

    public static File getFile(AuroraCrafting plugin) {
        return new File(plugin.getDataFolder() + "/menus", "workbench.yml");
    }

    public static void saveDefault(AuroraCrafting plugin) {
        if (!getFile(plugin).exists()) {
            plugin.saveResource("menus/workbench.yml", false);
        }
    }
}
