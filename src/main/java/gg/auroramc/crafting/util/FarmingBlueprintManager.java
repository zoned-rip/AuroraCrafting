package gg.auroramc.crafting.util;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class FarmingBlueprintManager {

    private final JavaPlugin plugin;
    public static final Map<String, String> blueprintPrefixMap = new HashMap<>();

    public FarmingBlueprintManager(JavaPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        blueprintPrefixMap.clear();

        File file = new File(plugin.getDataFolder(), "MinionsId.yml");
        if (!file.exists()) {
            plugin.saveResource("farming-blueprints.yml", false);
        }

        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);
        var section = config.getConfigurationSection("farming-blueprints");
        if (section == null) return;

        for (String prefix : section.getKeys(false)) {
            String robotType = section.getString(prefix);
            if (robotType != null) {
                blueprintPrefixMap.put(prefix, robotType);
            }
        }

        plugin.getLogger().info("Loaded " + blueprintPrefixMap.size() + " farming blueprint mappings.");
    }

    /**
     * Returns the robot type for the given blueprint ID, or null if not a farming blueprint.
     */
    public String getRobotType(String blueprintId) {
        for (var entry : blueprintPrefixMap.entrySet()) {
            if (blueprintId.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    /**
     * Extracts the tier number from the blueprint ID given a matching prefix.
     * Returns -1 if parsing fails.
     */
    public int getTier(String blueprintId) {
        for (String prefix : blueprintPrefixMap.keySet()) {
            if (blueprintId.startsWith(prefix)) {
                try {
                    return Integer.parseInt(blueprintId.replace(prefix, ""));
                } catch (NumberFormatException ignored) {}
            }
        }
        return -1;
    }

    /**
     * Returns true if the blueprint ID matches any farming blueprint prefix.
     */
    public boolean isFarmingBlueprint(String blueprintId) {
        return getRobotType(blueprintId) != null;
    }
}