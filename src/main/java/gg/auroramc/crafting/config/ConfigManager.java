package gg.auroramc.crafting.config;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.config.menu.RecipeBookCategoryConfig;
import gg.auroramc.crafting.config.menu.RecipeBookMenuConfig;
import gg.auroramc.crafting.config.menu.RecipeViewConfig;
import gg.auroramc.crafting.config.menu.WorkbenchConfig;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Getter
public class ConfigManager {
    private final AuroraCrafting plugin;

    private Config config;
    private RecipeBookConfig recipeBookConfig;
    private MessageConfig messageConfig;

    // menus
    private WorkbenchConfig workbenchConfig;
    private RecipeViewConfig recipeViewConfig;
    private RecipeBookMenuConfig recipeBookMenuConfig;
    private RecipeBookCategoryConfig recipeBookCategoryConfig;
    private List<RecipesConfig.RecipeConfig> recipes;

    public ConfigManager(AuroraCrafting plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        if (!new File(plugin.getDataFolder(), "config.yml").exists()) {
            plugin.saveResource("recipes/example.yml", false);
        }

        Config.saveDefault(plugin);
        config = new Config(plugin);
        config.load();

        MessageConfig.saveDefault(plugin, config.getLanguage());
        messageConfig = new MessageConfig(plugin, config.getLanguage());
        messageConfig.load();

        RecipeBookConfig.saveDefault(plugin);
        recipeBookConfig = new RecipeBookConfig(plugin);
        recipeBookConfig.load();

        WorkbenchConfig.saveDefault(plugin);
        workbenchConfig = new WorkbenchConfig(plugin);
        workbenchConfig.load();

        RecipeViewConfig.saveDefault(plugin);
        recipeViewConfig = new RecipeViewConfig(plugin);
        recipeViewConfig.load();

        RecipeBookMenuConfig.saveDefault(plugin);
        recipeBookMenuConfig = new RecipeBookMenuConfig(plugin);
        recipeBookMenuConfig.load();

        RecipeBookCategoryConfig.saveDefault(plugin);
        recipeBookCategoryConfig = new RecipeBookCategoryConfig(plugin);
        recipeBookCategoryConfig.load();

        recipes = getRecipesConfigs().stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private List<RecipesConfig> getRecipesConfigs() {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), "recipes");

        if (Files.notExists(recipesFolder)) {
            Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
        }

        try (Stream<Path> paths = Files.walk(recipesFolder, 1)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .map((file) -> {
                        RecipesConfig recipesConfig = new RecipesConfig(file);
                        recipesConfig.load();
                        return recipesConfig;
                    })
                    .collect(Collectors.toList());
        }
    }
}
