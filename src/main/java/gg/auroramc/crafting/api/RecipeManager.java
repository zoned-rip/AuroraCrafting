package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RecipeManager {
    private final AuroraCrafting plugin;
    private final Map<String, AuroraRecipe> recipes = new LinkedHashMap<>();

    public RecipeManager(AuroraCrafting plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        recipes.clear();
        plugin.getConfigManager().getConfig().getRecipes().forEach(recipeConfig -> {
            var recipe = new AuroraRecipe(recipeConfig.getId(), getItemPair(recipeConfig.getResult()), recipeConfig.getPermission());

            for (var ingredient : recipeConfig.getIngredients()) {
                recipe.addIngredient(getItemPair(ingredient));
            }
            var key = recipe.asLookupKey();
            recipes.put(key, recipe);
            AuroraCrafting.logger().debug("Loaded recipe: " + key);
        });
    }

    private ItemPair getItemPair(String item) {
        var split = item.split("/");
        if (split[0].isEmpty()) {
            return new ItemPair(TypeId.from(Material.AIR), 0);
        }
        return new ItemPair(TypeId.fromDefault(split[0]), Integer.parseInt(split[1]));
    }

    public AuroraRecipe getRecipe(List<ItemStack> matrix) {
        var key = asLookupKey(matrix);
        AuroraCrafting.logger().debug("Trying to find recipe: " + key);
        return recipes.get(key);
    }

    public Collection<AuroraRecipe> getRecipes() {
        return recipes.values();
    }

    private String asLookupKey(List<ItemStack> matrix) {
        var key = new StringBuilder();
        for (var ingredient : matrix) {
            key.append(AuroraAPI.getItemManager().resolveId(ingredient));
            key.append(";");
        }
        return key.toString();
    }
}
