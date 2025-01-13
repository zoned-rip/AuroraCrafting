package gg.auroramc.crafting.config.menu;

import gg.auroramc.aurora.api.config.AuroraConfig;
import gg.auroramc.aurora.api.config.decorators.IgnoreField;
import gg.auroramc.aurora.api.config.premade.ItemConfig;
import gg.auroramc.crafting.AuroraCrafting;
import lombok.Getter;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

@Getter
public class WorkbenchConfig extends AuroraConfig {
    @IgnoreField
    private final String id;

    private ItemConfig filler;
    private Map<String, ItemConfig> customItems;
    private String title = "Workbench";
    private Integer rows = 6;
    private Integer resultSlot = 25;
    private List<Integer> matrixSlots = List.of(10, 11, 12, 19, 20, 21, 28, 29, 30);
    private Set<Integer> quickCraftingSlots = Set.of(16, 25, 34);
    private ItemConfig invalidResultItem;
    private ItemConfig emptyQuickCraftItem;
    private ItemConfig noPermissionQuickCraftItem;
    private String commandCompletion;

    public WorkbenchConfig(File file, String id) {
        super(file);
        this.id = id;
    }

    @Override
    public void load() {
        super.load();
        if(commandCompletion == null) {
            commandCompletion = id;
        }
    }
}
