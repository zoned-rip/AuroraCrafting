package gg.auroramc.crafting.util;

import com.google.common.collect.Maps;
import gg.auroramc.crafting.config.ConfigManager;
import gg.auroramc.crafting.config.CookingRecipesConfig;
import gg.auroramc.crafting.config.SmithingTransformRecipesConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class RecipeRegistrar {
    private static final Map<RecipeType, Set<NamespacedKey>> registeredRecipes = Maps.newHashMap();

    private static NamespacedKey key(String id) {
        return new NamespacedKey("aurora", id);
    }

    public static void handleCookingDiff(RecipeType recipeType, List<CookingRecipesConfig.RecipeConfig> recipes) {
        var oldSet = registeredRecipes.get(recipeType);
        if (oldSet != null) {
            for (var oldRecipe : oldSet) {
                Bukkit.removeRecipe(oldRecipe);
            }
        }

        var newSet = new HashSet<NamespacedKey>(recipes.size());

        for (var recipe : recipes) {
            boolean added = Bukkit.addRecipe(RecipeAdapter.adapt(recipeType, recipe));
            if (added) {
                newSet.add(key(recipe.getId()));
            }
        }

        registeredRecipes.put(recipeType, newSet);
    }

    public static void handleSmithingDiff(RecipeType recipeType, List<SmithingTransformRecipesConfig.RecipeConfig> recipes) {
        var oldSet = registeredRecipes.get(recipeType);
        if (oldSet != null) {
            for (var oldRecipe : oldSet) {
                Bukkit.removeRecipe(oldRecipe);
            }
        }

        var newSet = new HashSet<NamespacedKey>(recipes.size());

        for (var recipe : recipes) {
            boolean added = Bukkit.addRecipe(RecipeAdapter.adapt(recipeType, recipe));
            if (added) {
                newSet.add(key(recipe.getId()));
            }
        }

        registeredRecipes.put(recipeType, newSet);
    }

    public static void discoverRecipes(Player player) {
        var alreadyDiscovered = player.getDiscoveredRecipes();

        for (var entry : registeredRecipes.entrySet()) {
            for (var key : entry.getValue()) {
                if (!alreadyDiscovered.contains(key)) {
                    player.discoverRecipe(key);
                }
            }
        }
    }

    public static void updateClientRecipes() {
        Bukkit.updateRecipes();
    }

    public static void reloadRecipes(ConfigManager configManager) {
        handleCookingDiff(RecipeType.BLASTING, configManager.getBlastingRecipes());
        handleCookingDiff(RecipeType.CAMPFIRE, configManager.getCampfireRecipes());
        handleCookingDiff(RecipeType.SMOKING, configManager.getSmokingRecipes());
        handleCookingDiff(RecipeType.FURNACE, configManager.getFurnaceRecipes());
        handleSmithingDiff(RecipeType.SMITHING_TRANSFORM, configManager.getSmithingTransformRecipes());
        updateClientRecipes();
    }
}
