package gg.auroramc.crafting.util;

import com.google.common.collect.Maps;
import gg.auroramc.crafting.config.ConfigManager;
import gg.auroramc.crafting.config.CookingRecipesConfig;
import gg.auroramc.crafting.config.SmithingTransformRecipesConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Recipe;

import java.util.*;

public class RecipeRegistrar {
    private static final Map<RecipeType, Set<NamespacedKey>> registeredRecipes = Maps.newHashMap();
    private static final Map<String, Recipe> removedRecipes = Maps.newHashMap();

    private static NamespacedKey key(String id) {
        return new NamespacedKey("aurora", id);
    }

    public static int reRegisterCurrenRecipes() {
        int count = 0;

        for (var entry : registeredRecipes.entrySet()) {
            for (var key : entry.getValue()) {
                var res = Bukkit.addRecipe(Bukkit.getRecipe(key));
                if (res) {
                    count++;
                }
            }
        }

        if (count > 0) {
            updateClientRecipes();
        }

        return count;
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

    public static void removeVanillaRecipes(Set<String> recipes, boolean force) {
        if (force) {
            for (var recipe : recipes) {
                var recipeKey = NamespacedKey.fromString(recipe);
                var oldRecipe = Bukkit.getRecipe(recipeKey);

                var success = Bukkit.removeRecipe(recipeKey);
                if (success && oldRecipe != null) {
                    removedRecipes.put(recipe, oldRecipe);
                }
            }
            return;
        }

        var removedEntries = new HashMap<>(removedRecipes);
        for (var recipe : removedEntries.entrySet()) {
            if (!recipes.contains(recipe.getKey())) {
                Bukkit.addRecipe(recipe.getValue());
                removedRecipes.remove(recipe.getKey());
            }
        }

        for (var recipe : recipes) {
            if (removedRecipes.containsKey(recipe)) {
                continue;
            }
            var recipeKey = NamespacedKey.fromString(recipe);
            var oldRecipe = Bukkit.getRecipe(recipeKey);

            var success = Bukkit.removeRecipe(recipeKey);
            if (success) {
                removedRecipes.put(recipe, oldRecipe);
            }
        }
    }

    public static void reloadRecipes(ConfigManager configManager) {
        removeVanillaRecipes(configManager.getDisabledRecipesConfig().getRecipes(), false);
        handleCookingDiff(RecipeType.BLASTING, configManager.getBlastingRecipes());
        handleCookingDiff(RecipeType.CAMPFIRE, configManager.getCampfireRecipes());
        handleCookingDiff(RecipeType.SMOKING, configManager.getSmokingRecipes());
        handleCookingDiff(RecipeType.FURNACE, configManager.getFurnaceRecipes());
        handleSmithingDiff(RecipeType.SMITHING_TRANSFORM, configManager.getSmithingTransformRecipes());
        updateClientRecipes();
    }
}
