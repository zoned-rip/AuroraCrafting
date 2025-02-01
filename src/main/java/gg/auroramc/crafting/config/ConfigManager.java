package gg.auroramc.crafting.config;

import gg.auroramc.crafting.AuroraCrafting;
import gg.auroramc.crafting.config.menu.*;
import lombok.Getter;
import lombok.SneakyThrows;

import java.io.File;
import java.io.IOException;
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
    private MerchantsConfig merchantsConfig;
    private DisabledRecipesConfig disabledRecipesConfig;

    // menus
    private Map<String, WorkbenchConfig> workbenchConfig;
    private RecipeViewConfig recipeViewConfig;
    private RecipeBookMenuConfig recipeBookMenuConfig;
    private RecipeBookCategoryConfig recipeBookCategoryConfig;
    private MerchantsMenuConfig merchantsMenuConfig;

    private Map<String, RecipesConfig> recipes;

    private List<CookingRecipesConfig.RecipeConfig> blastingRecipes;
    private List<CookingRecipesConfig.RecipeConfig> smokingRecipes;
    private List<CookingRecipesConfig.RecipeConfig> furnaceRecipes;
    private List<CookingRecipesConfig.RecipeConfig> campfireRecipes;

    private List<SmithingTransformRecipesConfig.RecipeConfig> smithingTransformRecipes;

    public ConfigManager(AuroraCrafting plugin) {
        this.plugin = plugin;
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

        MerchantsConfig.saveDefault(plugin);
        merchantsConfig = new MerchantsConfig(plugin);
        merchantsConfig.load();

        DisabledRecipesConfig.saveDefault(plugin);
        disabledRecipesConfig = new DisabledRecipesConfig(plugin);
        disabledRecipesConfig.load();

        workbenchConfig = loadWorkBenches();

        RecipeBookConfig.saveDefault(plugin);
        recipeBookConfig = new RecipeBookConfig(plugin);
        recipeBookConfig.load();

        RecipeViewConfig.saveDefault(plugin);
        recipeViewConfig = new RecipeViewConfig(plugin);
        recipeViewConfig.load();

        RecipeBookMenuConfig.saveDefault(plugin);
        recipeBookMenuConfig = new RecipeBookMenuConfig(plugin);
        recipeBookMenuConfig.load();

        RecipeBookCategoryConfig.saveDefault(plugin);
        recipeBookCategoryConfig = new RecipeBookCategoryConfig(plugin);
        recipeBookCategoryConfig.load();

        MerchantsMenuConfig.saveDefault(plugin);
        merchantsMenuConfig = new MerchantsMenuConfig(plugin);
        merchantsMenuConfig.load();

        recipes = getRecipesConfigs();

        blastingRecipes = getCookingRecipesConfigs("blasting_recipes").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        smokingRecipes = getCookingRecipesConfigs("smoking_recipes").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        furnaceRecipes = getCookingRecipesConfigs("furnace_recipes").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        campfireRecipes = getCookingRecipesConfigs("campfire_recipes").stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());

        smithingTransformRecipes = getSmithingTransformRecipesConfigs().stream()
                .flatMap(recipesConfig -> recipesConfig.getRecipes().stream())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    private Map<String, WorkbenchConfig> loadWorkBenches() {
        if (!new File(plugin.getDataFolder() + "workbenches").exists()) {
            if (new File(plugin.getDataFolder() + "/menus", "workbench.yml").exists()) {
                Files.createDirectories(Path.of(plugin.getDataFolder().getPath(), "workbenches"));
                Files.move(Path.of(plugin.getDataFolder().getPath(), "menus", "workbench.yml"), Path.of(plugin.getDataFolder().getPath(), "workbenches", "default.yml"));
            } else {
                if (!Files.exists(Path.of(plugin.getDataFolder().getPath(), "workbenches", "default.yml"))) {
                    plugin.saveResource("workbenches/default.yml", false);
                }

            }
        }

        var map = new HashMap<String, WorkbenchConfig>();

        try (Stream<Path> paths = Files.walk(Path.of(plugin.getDataFolder().getPath(), "workbenches"), 1)) {
            var fileList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml"))
                    .map(Path::toFile)
                    .toList();

            for (var file : fileList) {
                var workbenchConfig = new WorkbenchConfig(file, file.getName().replace(".yml", ""));
                workbenchConfig.load();
                map.put(file.getName().replace(".yml", ""), workbenchConfig);
            }

            return map;
        }
    }

    private Map<String, RecipesConfig> getRecipesConfigs() {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), "recipes");

        if (Files.notExists(recipesFolder)) {
            try {
                Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        var recipes = new HashMap<String, RecipesConfig>();

        try (Stream<Path> paths = Files.walk(recipesFolder, 5)) {
            var fileList = paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .toList();

            for (var file : fileList) {
                try {
                    RecipesConfig recipesConfig = new RecipesConfig(file);
                    recipesConfig.load();
                    recipes.put(file.getName().replace(".yml", ""), recipesConfig);
                } catch (Exception e) {
                    AuroraCrafting.logger().severe("Failed to load recipe file: " + file.getName());
                    e.printStackTrace();
                }
            }

            return recipes;
        } catch (IOException e) {
            e.printStackTrace();
            return recipes;
        }
    }

    @SneakyThrows
    private List<CookingRecipesConfig> getCookingRecipesConfigs(String folder) {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), folder);

        if (Files.notExists(recipesFolder)) {
            Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
            plugin.saveResource(folder + "/example.yml", false);
        }

        try (Stream<Path> paths = Files.walk(recipesFolder, 5)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .map((file) -> {
                        CookingRecipesConfig recipesConfig = new CookingRecipesConfig(file);
                        recipesConfig.load();
                        return recipesConfig;
                    })
                    .collect(Collectors.toList());
        }
    }

    @SneakyThrows
    private List<SmithingTransformRecipesConfig> getSmithingTransformRecipesConfigs() {
        Path recipesFolder = Path.of(plugin.getDataFolder().getPath(), "smithing_recipes");

        if (Files.notExists(recipesFolder)) {
            Files.createDirectories(recipesFolder); // Create folder if it doesn't exist
            plugin.saveResource("smithing_recipes/example.yml", false);
        }

        try (Stream<Path> paths = Files.walk(recipesFolder, 5)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".yml") || path.toString().endsWith(".yaml"))
                    .map(Path::toFile)
                    .map((file) -> {
                        SmithingTransformRecipesConfig recipesConfig = new SmithingTransformRecipesConfig(file);
                        recipesConfig.load();
                        return recipesConfig;
                    })
                    .collect(Collectors.toList());
        }
    }
}
