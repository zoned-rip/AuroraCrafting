package gg.auroramc.crafting;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.AuroraLogger;
import gg.auroramc.crafting.api.RecipeManager;
import gg.auroramc.crafting.api.event.PlayerCraftItemEvent;
import gg.auroramc.crafting.command.CommandManager;
import gg.auroramc.crafting.config.ConfigManager;
import gg.auroramc.crafting.hooks.HookManager;
import gg.auroramc.crafting.listener.CraftingTableInteractListener;
import gg.auroramc.crafting.menu.MenuListener;
import gg.auroramc.crafting.util.RecipeRegistrar;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

public class AuroraCrafting extends JavaPlugin {
    @Getter
    private ConfigManager configManager;

    private CommandManager commandManager;
    @Getter
    private RecipeManager recipeManager;

    @Getter
    private static AuroraCrafting instance;
    private static AuroraLogger l;

    public static AuroraLogger logger() {
        return l;
    }

    @Override
    public void onLoad() {
        instance = this;
        configManager = new ConfigManager(this);
        l = AuroraAPI.createLogger("AuroraLevels", () -> configManager.getConfig().getDebug());

        HookManager.loadHooks(this);
    }

    @Override
    public void onEnable() {
        recipeManager = new RecipeManager(this);
        commandManager = new CommandManager(this);
        commandManager.reload();
        Bukkit.getPluginManager().registerEvents(new MenuListener(this), this);
        if (configManager.getConfig().getOpenInsteadOfCraftingTable() || configManager.getConfig().getOpenShiftClickCraftingTable()) {
            Bukkit.getPluginManager().registerEvents(new CraftingTableInteractListener(this), this);
        }

        RecipeRegistrar.reloadRecipes(configManager);

        HookManager.enableHooks(this);
    }

    @Override
    public void onDisable() {
        commandManager.unregisterCommands();
    }

    public void reload() {
        configManager.reload();
        commandManager.reload();
        recipeManager.reload();
        RecipeRegistrar.reloadRecipes(configManager);
    }

    public void callCraftEvent(Player player, ItemStack item, int amount) {
        Bukkit.getPluginManager().callEvent(new PlayerCraftItemEvent(player, item, amount));
    }
}
