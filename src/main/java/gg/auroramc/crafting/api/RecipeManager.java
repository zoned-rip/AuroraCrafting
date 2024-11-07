package gg.auroramc.crafting.api;

import gg.auroramc.aurora.api.AuroraAPI;
import gg.auroramc.aurora.api.item.TypeId;
import gg.auroramc.crafting.AuroraCrafting;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RecipeManager {
    private final AuroraCrafting plugin;
    private final Map<String, AuroraRecipe> shapedRecipeLookup = new HashMap<>();
    private final Map<String, AuroraRecipe> shapelessRecipeLookup = new HashMap<>();
    private final Map<String, AuroraRecipe> recipeIdLookup = new HashMap<>();
    private final Map<String, List<AuroraRecipe>> recipeCategoryLookup = new HashMap<>();

    public RecipeManager(AuroraCrafting plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        shapedRecipeLookup.clear();
        shapelessRecipeLookup.clear();
        recipeIdLookup.clear();
        recipeCategoryLookup.clear();

        for (var category : plugin.getConfigManager().getRecipeBookConfig().getCategories()) {
            if (category.getId() == null || category.getId().isEmpty()) {
                AuroraCrafting.logger().severe("Category id can't null or empty!");
                continue;
            }
            recipeCategoryLookup.put(category.getId(), new ArrayList<>());
        }

        plugin.getConfigManager().getRecipes().forEach(recipeConfig -> {
            var recipe = RecipeFactory.createRecipe(
                    recipeConfig.getId(),
                    getItemPair(recipeConfig.getResult()),
                    recipeConfig.getShapeless(),
                    recipeConfig.getPermission(),
                    recipeConfig.getLockedLore()
            );

            for (var ingredient : recipeConfig.getIngredients()) {
                recipe.addIngredient(getItemPair(ingredient));
            }

            var key = recipe.asLookupKey();

            if (recipe instanceof ShapelessAuroraRecipe) {
                shapelessRecipeLookup.put(key, recipe);
            } else {
                shapedRecipeLookup.put(key, recipe);
            }

            if (recipe.getId() != null && !recipe.getId().isEmpty()) {
                if (recipeIdLookup.put(recipeConfig.getId(), recipe) != null) {
                    AuroraCrafting.logger().severe("Duplicate recipe id found: " + recipeConfig.getId());
                }

                for (var category : plugin.getConfigManager().getRecipeBookConfig().getCategories()) {
                    if (category.getRecipes().contains(recipeConfig.getId()) || category.getFiles().contains(recipeConfig.getSourceFile())) {
                        recipeCategoryLookup.get(category.getId()).add(recipe);
                    }
                }
            }

            AuroraCrafting.logger().debug("Loaded recipe: " + recipeConfig.getId());
        });
    }

    private ItemPair getItemPair(String item) {
        var split = item.split("/");
        if (split[0].isEmpty()) {
            return new ItemPair(TypeId.from(Material.AIR), 0);
        }
        return new ItemPair(TypeId.fromDefault(split[0]), Integer.parseInt(split[1]));
    }

    public @Nullable AuroraRecipe getRecipeByMatrix(List<ItemStack> matrix) {
        var recipe = shapedRecipeLookup.get(asShapedLookupKey(matrix));
        if (recipe == null) {
            recipe = shapelessRecipeLookup.get(asShapelessLookupKey(matrix));
        }
        return recipe;
    }

    public @NotNull List<AuroraRecipe> getRecipesByCategory(String category) {
        return recipeCategoryLookup.getOrDefault(category, new ArrayList<>());
    }

    public @Nullable AuroraRecipe getRecipeById(String id) {
        return recipeIdLookup.get(id);
    }


    private String asShapedLookupKey(List<ItemStack> matrix) {
        var key = new StringBuilder();
        for (var ingredient : matrix) {
            key.append(AuroraAPI.getItemManager().resolveId(ingredient));
            key.append(";");
        }
        return key.toString();
    }

    private String asShapelessLookupKey(List<ItemStack> matrix) {
        var idList = new ArrayList<TypeId>(matrix.size());

        for (var ingredient : matrix) {
            var id = AuroraAPI.getItemManager().resolveId(ingredient);
            if (id.id().equals("air")) continue;
            idList.add(id);
        }

        idList.sort(Comparator.comparing(TypeId::id));

        var key = new StringBuilder();

        for (var id : idList) {
            key.append(id.toString());
            key.append(";");
        }

        return key.toString();
    }

    public @NotNull List<AuroraRecipe> getCraftableRecipes(Player player, int maxCount) {
        var craftableRecipes = new ArrayList<AuroraRecipe>();

        for (var recipe : shapedRecipeLookup.values()) {
            if (recipe.hasPermission(player) && recipe.getQuickCraftTimes(player) > 0) {
                craftableRecipes.add(recipe);
                if(maxCount >= craftableRecipes.size()) break;
            }
        }

        if(maxCount <= craftableRecipes.size()) return craftableRecipes;

        for (var recipe : shapelessRecipeLookup.values()) {
            if (recipe.hasPermission(player) && recipe.getQuickCraftTimes(player) > 0) {
                craftableRecipes.add(recipe);
                if(maxCount >= craftableRecipes.size()) break;
            }
        }

        return craftableRecipes;
    }
}
